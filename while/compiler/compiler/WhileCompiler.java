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

import java.io.File;
import java.io.IOException;

import whilelang.ast.WhileFile;

/**
 * Encapsulates the process for compiling a While file into its Abstract Syntac
 * Reprentation. This includes the application of all stages in the compilation
 * pipeline (such as type checking, etc).
 * 
 * @author David J. Pearce
 *
 */
public class WhileCompiler {
	private File srcFile;
	
	public WhileCompiler(String filename) {
		this.srcFile = new File(filename);
	}
	
	public WhileFile compile() throws IOException {
		// First, lexing and parsing
		Lexer lexer = new Lexer(srcFile.getPath());
		Parser parser = new Parser(srcFile.getPath(), lexer.scan());
		WhileFile ast = parser.read();

		// Second, type checking
		new TypeChecker().check(ast);

		// Third, unreachable code
		new UnreachableCode().check(ast);

		// Fourth, definite assignment
		new DefiniteAssignment().check(ast);
		
		// Done
		return ast;
	}
}
