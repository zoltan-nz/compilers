// This file is part of the WhileLang Compiler (wlc).
//
// The WhileLang Compiler is free software; you can redistribute
// it and/or modify it under the terms of the GNU General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// The WhileLang Compiler is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE. See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public
// License along with the WhileLang Compiler. If not, see
// <http://www.gnu.org/licenses/>
//
// Copyright 2013, David James Pearce.

package whilelang.compiler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import whilelang.util.SyntaxError;

/**
 * Responsible for turning a stream of characters into a sequence of tokens.
 * 
 * @author Daivd J. Pearce
 * 
 */
public class Lexer {

	private String filename;
	private StringBuffer input;
	private int pos;

	public Lexer(String filename) throws IOException {
		this(new InputStreamReader(new FileInputStream(filename), "UTF8"));
		this.filename = filename;
	}

	public Lexer(InputStream instream) throws IOException {
		this(new InputStreamReader(instream, "UTF8"));
	}

	public Lexer(Reader reader) throws IOException {
		BufferedReader in = new BufferedReader(reader);

		StringBuffer text = new StringBuffer();
		String tmp;
		while ((tmp = in.readLine()) != null) {
			text.append(tmp);
			text.append("\n");
		}

		input = text;
	}

	/**
	 * Scan all characters from the input stream and generate a corresponding
	 * list of tokens, whilst discarding all whitespace and comments.
	 * 
	 * @return
	 */
	public List<Token> scan() {
		ArrayList<Token> tokens = new ArrayList<Token>();
		pos = 0;

		while (pos < input.length()) {
			char c = input.charAt(pos);

			if (Character.isDigit(c)) {
				tokens.add(scanNumericConstant());
			} else if (c == '"') {
				tokens.add(scanStringConstant());
			} else if (c == '\'') {
				tokens.add(scanCharacterConstant());
			} else if (isOperatorStart(c)) {
				tokens.add(scanOperator());
			} else if (Character.isJavaIdentifierStart(c)) {
				tokens.add(scanIdentifier());
			} else if (Character.isWhitespace(c)) {
				skipWhitespace();
			} else {
				syntaxError("syntax error");
			}
		}

		return tokens;
	}

	/**
	 * Scan a numeric constant. That is a sequence of digits which gives an
	 * integer constant.
	 * 
	 * @return
	 */
	public Token scanNumericConstant() {
		int start = pos;
		while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
			pos = pos + 1;
		}
		int r = new BigInteger(input.substring(start, pos)).intValue();
		return new Int(r, input.substring(start, pos), start);
	}

	/**
	 * Scan a character constant, such as e.g. 'c'. Observe that care must be
	 * taken to properly handle escape codes. For example, '\n' is a single
	 * character constant which is made up from two characters in the input
	 * string.
	 * 
	 * @return
	 */
	public Token scanCharacterConstant() {
		int start = pos;
		pos++;
		char c = input.charAt(pos++);
		if (c == '\\') {
			// escape code
			switch (input.charAt(pos++)) {
			case 't':
				c = '\t';
				break;
			case 'n':
				c = '\n';
				break;
			default:
				syntaxError("unrecognised escape character", pos);
			}
		}
		if (input.charAt(pos) != '\'') {
			syntaxError("unexpected end-of-character", pos);
		}
		pos = pos + 1;
		return new Char(c, input.substring(start, pos), start);
	}

	public Token scanStringConstant() {
		int start = pos;
		pos++;
		while (pos < input.length()) {
			char c = input.charAt(pos);
			if (c == '"') {
				String v = input.substring(start, ++pos);
				return new Strung(parseString(v), v, start);
			}
			pos = pos + 1;
		}
		syntaxError("unexpected end-of-string", pos - 1);
		return null;
	}

	protected String parseString(String v) {
		/*
		 * Parsing a string requires several steps to be taken. First, we need to
		 * strip quotes from the ends of the string.
		 */
		v = v.substring(1, v.length() - 1);
		int start = pos - v.length();
		// Second, step through the string and replace escaped characters
		for (int i = 0; i < v.length(); i++) {
			if (v.charAt(i) == '\\') {
				if (v.length() <= i + 1) {
					syntaxError("unexpected end-of-string", start + i);
				} else {
					char replace = 0;
					int len = 2;
					switch (v.charAt(i + 1)) {
					case 'b':
						replace = '\b';
						break;
					case 't':
						replace = '\t';
						break;
					case 'n':
						replace = '\n';
						break;
					case 'f':
						replace = '\f';
						break;
					case 'r':
						replace = '\r';
						break;
					case '"':
						replace = '\"';
						break;
					case '\'':
						replace = '\'';
						break;
					case '\\':
						replace = '\\';
						break;
					case 'u':
						len = 6; // unicode escapes are six digits long,
						// including "slash u"
						String unicode = v.substring(i + 2, i + 6);
						replace = (char) Integer.parseInt(unicode, 16); // unicode
						break;
					default:
						syntaxError("unknown escape character", start + i);
					}
					v = v.substring(0, i) + replace + v.substring(i + len);
				}
			}
		}
		return v;
	}

	static final char[] opStarts = { ',', '(', ')', '[', ']', '{', '}', '+', '-',
		'*', '/', '%', '!', '=', '<', '>', ':', ';', '&', '|', '.' };

	public boolean isOperatorStart(char c) {
		for (char o : opStarts) {
			if (c == o) {
				return true;
			}
		}
		return false;
	}

	public Token scanOperator() {
		char c = input.charAt(pos);

		if (c == '.') {			
			return new Dot(pos++);
		} else if (c == ',') {
			return new Comma(pos++);
		} else if (c == ';') {
			return new SemiColon(pos++);
		} else if (c == ':') {
			return new Colon(pos++);
		} else if (c == '|') {
			if ((pos + 1) < input.length() && input.charAt(pos + 1) == '|') {
				pos += 2;
				return new LogicalOr("||", pos - 2);
			} else {
				return new Bar(pos++);
			}
		} else if (c == '(') {
			return new LeftBrace(pos++);
		} else if (c == ')') {
			return new RightBrace(pos++);
		} else if (c == '[') {
			return new LeftSquare(pos++);
		} else if (c == ']') {
			return new RightSquare(pos++);
		} else if (c == '{') {
			return new LeftCurly(pos++);
		} else if (c == '}') {
			return new RightCurly(pos++);
		} else if (c == '+') {			
			return new Plus(pos++);
		} else if (c == '-') {			
			return new Minus(pos++);
		} else if (c == '*') {
			return new Star(pos++);
		} else if (c == '&' && (pos + 1) < input.length()
				&& input.charAt(pos + 1) == '&') {
			pos += 2;
			return new LogicalAnd("&&", pos - 2);
		} else if (c == '/') {			
			return new RightSlash(pos++);
		} else if (c == '%') {			
			return new Percent(pos++);
		} else if (c == '!') {
			if ((pos + 1) < input.length() && input.charAt(pos + 1) == '=') {
				pos += 2;
				return new NotEquals("!=", pos - 2);
			} else {
				return new Shreak(pos++);
			}
		} else if (c == '=') {
			if ((pos + 1) < input.length() && input.charAt(pos + 1) == '=') {
				pos += 2;
				return new EqualsEquals(pos - 2);
			} else {
				return new Equals(pos++);
			}
		} else if (c == '<') {
			if ((pos + 1) < input.length() && input.charAt(pos + 1) == '=') {
				pos += 2;
				return new LessEquals("<=", pos - 2);
			} else {
				return new LeftAngle(pos++);
			}
		} else if (c == '>') {
			if ((pos + 1) < input.length() && input.charAt(pos + 1) == '=') {
				pos += 2;
				return new GreaterEquals(">=", pos - 2);
			} else {
				return new RightAngle(pos++);
			}
		} 

		syntaxError("unknown operator encountered: " + c);
		return null;
	}

	public static final String[] keywords =
		{ "true", "false", "void", "int", "char", "string", "bool", "if",
		  "case", "default", "break", "continue", "while", "else", "is",
		  "for", "assert", "print", "return", "type" }; 

	public Token scanIdentifier() {
		int start = pos;
		while (pos < input.length()
				&& Character.isJavaIdentifierPart(input.charAt(pos))) {
			pos++;
		}
		String text = input.substring(start, pos);

		// now, check for keywords
		for (String keyword : keywords) {
			if (keyword.equals(text)) {
				return new Keyword(text, start);
			}
		}

		// otherwise, must be identifier
		return new Identifier(text, start);
	}
	
	/**
	 * Skip over any whitespace at the current index position in the input
	 * string.
	 * 
	 * @param tokens
	 */
	public void skipWhitespace() {
		while (pos < input.length()
				&& Character.isWhitespace(input.charAt(pos))) {
			pos++;
		}
	}

	/**
	 * Raise a syntax error with a given message at given index.
	 * 
	 * @param msg
	 *            --- message to raise.
	 * @param index
	 *            --- index position to associate the error with.
	 */
	private void syntaxError(String msg, int index) {
		throw new SyntaxError(msg, filename, index, index);
	}

	/**
	 * Raise a syntax error with a given message at the current index.
	 * 
	 * @param msg
	 * @param index
	 */
	private void syntaxError(String msg) {
		throw new SyntaxError(msg, filename, pos, pos);
	}

	/**
	 * The base class for all tokens.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static abstract class Token {

		public final String text;
		public final int start;

		public Token(String text, int pos) {
			this.text = text;
			this.start = pos;
		}

		public int end() {
			return start + text.length() - 1;
		}
	}

	/**
	 * Represents an integer constant. That is, a sequence of 1 or more digits.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static class Int extends Token {

		public final int value;

		public Int(int r, String text, int pos) {
			super(text, pos);
			value = r;
		}
	}

	/**
	 * Represents a character constant. That is, a single digit enclosed in
	 * single quotes.  E.g. 'c'
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static class Char extends Token {

		public final char value;

		public Char(char c, String text, int pos) {
			super(text, pos);
			value = c;
		}
	}

	/**
	 * Represents a variable or function name. That is, a alphabetic character
	 * (or '_'), followed by a sequence of zero or more alpha-numeric
	 * characters.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static class Identifier extends Token {

		public Identifier(String text, int pos) {
			super(text, pos);
		}
	}

	/**
	 * Represents a sequence of zero or more characters which are enclosed in
	 * double quotes. E.g. "This is a String"
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static class Strung extends Token {

		public final String string;

		public Strung(String string, String text, int pos) {
			super(text, pos);
			this.string = string;
		}
	}

	/**
	 * Represents a known keyword. In essence, a keyword is a sequence of one or
	 * more alphabetic characters which is defined in advance.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static class Keyword extends Token {

		public Keyword(String text, int pos) {
			super(text, pos);
		}
	}

	public static class Comma extends Token {

		public Comma(int pos) {
			super(",", pos);
		}
	}

	public static class SemiColon extends Token {
		public SemiColon(int pos) {
			super(";", pos);
		}
	}

	public static class Colon extends Token {
		public Colon(int pos) {
			super(":", pos);
		}
	}
	
	public static class Bar extends Token {
		public Bar(int pos) {
			super("|", pos);
		}
	}

	public static class LeftBrace extends Token {

		public LeftBrace(int pos) {
			super("(", pos);
		}
	}

	public static class RightBrace extends Token {

		public RightBrace(int pos) {
			super(")", pos);
		}
	}

	public static class LeftSquare extends Token {

		public LeftSquare(int pos) {
			super("[", pos);
		}
	}

	public static class RightSquare extends Token {

		public RightSquare(int pos) {
			super("]", pos);
		}
	}

	public static class LeftAngle extends Token {

		public LeftAngle(int pos) {
			super("<", pos);
		}
	}

	public static class RightAngle extends Token {

		public RightAngle(int pos) {
			super(">", pos);
		}
	}

	public static class LeftCurly extends Token {

		public LeftCurly(int pos) {
			super("{", pos);
		}
	}

	public static class RightCurly extends Token {

		public RightCurly(int pos) {
			super("}", pos);
		}
	}

	public static class PlusPlus extends Token {
		public PlusPlus(int pos) {
			super("++", pos);
		}
	}
	
	public static class Plus extends Token {
		public Plus(int pos) {
			super("+", pos);
		}
	}

	public static class Minus extends Token {
		public Minus(int pos) {
			super("-", pos);
		}
	}

	public static class Star extends Token {

		public Star(int pos) {
			super("*", pos);
		}
	}

	public static class LeftSlash extends Token {

		public LeftSlash(int pos) {
			super("\\", pos);
		}
	}
	
	public static class RightSlash extends Token {

		public RightSlash(int pos) {
			super("/", pos);
		}
	}

	public static class Percent extends Token {

		public Percent(int pos) {
			super("%", pos);
		}
	}

	public static class Shreak extends Token {

		public Shreak(int pos) {
			super("!", pos);
		}
	}

	public static class Dot extends Token {

		public Dot(int pos) {
			super(".", pos);
		}
	}

	public static class Equals extends Token {

		public Equals(int pos) {
			super("=", pos);
		}
	}

	public static class EqualsEquals extends Token {

		public EqualsEquals(int pos) {
			super("==", pos);
		}
	}

	public static class NotEquals extends Token {

		public NotEquals(String text, int pos) {
			super(text, pos);
		}
	}

	public static class LessEquals extends Token {

		public LessEquals(String text, int pos) {
			super(text, pos);
		}
	}

	public static class GreaterEquals extends Token {

		public GreaterEquals(String text, int pos) {
			super(text, pos);
		}
	}

	public static class LogicalAnd extends Token {

		public LogicalAnd(String text, int pos) {
			super(text, pos);
		}
	}

	public static class LogicalOr extends Token {

		public LogicalOr(String text, int pos) {
			super(text, pos);
		}
	}

	public static class LogicalNot extends Token {

		public LogicalNot(String text, int pos) {
			super(text, pos);
		}
	}
}
