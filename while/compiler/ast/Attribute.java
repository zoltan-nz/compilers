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

public interface Attribute {

  public static class Source implements Attribute {

    public final int start;
    public final int end;

    public Source(int start, int end) {
      this.start = start;
      this.end = end;
    }

    public String toString() {
      return "@" + start + ":" + end;
    }
  }  
  
  public static class Type implements Attribute {

	  public final whilelang.ast.Type type;

	  public Type(whilelang.ast.Type type) {
		  this.type = type;
	  }
  }
}
