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

package whilelang.ast;

import java.util.*;

import whilelang.util.Pair;
import whilelang.util.SyntacticElement;

/**
 * <p>
 * Represents a type as denoted in a source file (a.k.a a <i>syntactic
 * type</i>). As such types come directly from source code, they may be
 * incorrect in some fashion. For example, the type <code>{void f}</code> could
 * be written by a programmer, but is invalid type and should (eventually)
 * result in a syntax error.
 * </p>
 * 
 * @author David J. Pearce
 * 
 */
public interface Type extends SyntacticElement {

	/**
	 * Represents the special <code>void</code> type which can only be used in
	 * special circumstance (e.g. for a function return).
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static final class Void extends SyntacticElement.Impl implements
			Type {

		public Void(Attribute... attributes) {
			super(attributes);
		}
		
		public String toString() {
			return "void";
		}
	}

	/**
	 * Represents the <code>bool</code> type which contains the values
	 * <code>true</code> and <code>false</code>.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static final class Bool extends SyntacticElement.Impl implements
			Type {

		public Bool(Attribute... attributes) {
			super(attributes);
		}
		
		public String toString() {
			return "bool";
		}
	}

	/**
	 * Represents the <code>int</code> type which describes the set of all
	 * integers described in 32bit twos compliment form. For example, this is
	 * identical to a Java <code>int</code>.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static final class Int extends SyntacticElement.Impl implements Type {

		public Int(Attribute... attributes) {
			super(attributes);
		}
		
		public String toString() {
			return "int";
		}
	}

	/**
	 * Represents the <code>char</code> type which describes the set of all 7bit
	 * ASCII characters. Observe that this is stricly less than that described
	 * by Java's <code>char</code> type, which represents the set of UTF16
	 * values.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static final class Char extends SyntacticElement.Impl implements
			Type {

		public Char(Attribute... attributes) {
			super(attributes);
		}
		
		public String toString() {
			return "char";
		}
	}

	/**
	 * Represents the <code>string</code> type which describes any sequence of
	 * <code>char</code> values.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static final class Strung extends SyntacticElement.Impl implements
			Type {
		public Strung(Attribute... attributes) {
			super(attributes);
		}
		
		public String toString() {
			return "string";
		}
	}

	/**
	 * Represents a named type which has yet to be expanded in the given
	 * context.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static final class Named extends SyntacticElement.Impl implements
			Type {

		private final String name;

		public Named(String name, Attribute... attributes) {
			super(attributes);
			this.name = name;
		}

		public String toString() {
			return getName();
		}

		/**
		 * Get the name used by this type.
		 * 
		 * @return
		 */
		public String getName() {
			return name;
		}		
	}

	/**
	 * Represents the array type <code>T[]</code> which describes any sequence
	 * of zero or more values of type <code>T</code>.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static final class Array extends SyntacticElement.Impl implements
			Type {

		private final Type element;

		public Array(Type element, Attribute... attributes) {
			super(attributes);
			this.element = element;
		}

		/**
		 * Get the element type of this list.
		 * 
		 * @return
		 */
		public Type getElement() {
			return element;
		}
		
		public String toString() {
			return "[" + element + "]";
		}
	}

	/**
	 * Represents a record type, such as <code>{int x, int y}</code>, which
	 * consists of one or more (named) field types. Observe that records exhibit
	 * <i>depth</i> subtyping, but not <i>width</i> subtyping.
	 * 
	 * @author David J. Pearce
	 * 
	 */
	public static final class Record extends SyntacticElement.Impl implements Type {

		private final ArrayList<Pair<Type,String>> fields;

		public Record(List<Pair<Type,String>> fields, Attribute... attributes) {
			super(attributes);
			if (fields.size() == 0) {
				throw new IllegalArgumentException(
						"Cannot create type tuple with no fields");
			}
			this.fields = new ArrayList<Pair<Type,String>>(fields);
		}

		/**
		 * Get the fields which make up this record type. This are stored in the
		 * order they are declared in the source file.
		 * 
		 * @return
		 */
		public List<Pair<Type,String>> getFields() {
			return fields;
		}
		
		public String toString() {
			String r = "";

			for (int i = 0; i != fields.size(); ++i) {
				if (i != 0) {
					r = r + ",";
				}
				Pair<Type, String> field = fields.get(i);
				r = r + field.first() + " " + field.second();
			}

			return "{" + r + "}";
		}
	}	
}
