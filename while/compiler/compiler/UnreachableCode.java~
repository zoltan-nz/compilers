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

import static whilelang.util.SyntaxError.internalFailure;
import static whilelang.util.SyntaxError.syntaxError;

import java.util.List;

import whilelang.ast.Expr;
import whilelang.ast.Stmt;
import whilelang.ast.Type;
import whilelang.ast.WhileFile;
import whilelang.util.SyntacticElement;

/**
 * Responsible for checking that all statements are potentially reachable.
 * Statements which can be shown as definitely unreachable are reported as an
 * error.
 * 
 * @author David J. Pearce
 *
 */
public class UnreachableCode {
	private WhileFile file;

	public void check(WhileFile wf) {
		this.file = wf;

		for (WhileFile.Decl declaration : wf.declarations) {
			if (declaration instanceof WhileFile.MethodDecl) {
				check((WhileFile.MethodDecl) declaration);
			}
		}
	}

	public void check(WhileFile.MethodDecl fd) {
		// Check all statements in the method body
		ControlFlow cf = check(fd.getBody());
		if(cf == ControlFlow.NEXT) {
			checkIsVoid(fd.getRet(),fd.getRet());
		}
	}

	/**
	 * Check that all statements in a given list of statements are reachable,
	 * and return whether or not control will fall through from the end of this
	 * block.
	 * 
	 * @param statements
	 *            The list of statements to check.
	 */
	public ControlFlow check(List<Stmt> statements) {
		ControlFlow fallThru = ControlFlow.NEXT;
		for (Stmt s : statements) {
			if (fallThru != ControlFlow.NEXT && fallThru != ControlFlow.BREAKNEXT) {
				syntaxError("unreachable code", file.filename, s);
			} else {
				fallThru = check(s);
			}
		}
		return fallThru;
	}
	
	/**
	 * Check that all statements contained in a given statement are reachable.
	 * 
	 * @param stmt
	 * @return
	 */
	public ControlFlow check(Stmt stmt) {
		if (stmt instanceof Stmt.Assert ||
				stmt instanceof Stmt.Assign ||
				stmt instanceof Stmt.Print ||
				stmt instanceof Stmt.VariableDeclaration ||
				stmt instanceof Expr.Invoke) {
			// These are all the easy cases!
			return ControlFlow.NEXT;
		} else if (stmt instanceof Stmt.Continue || 
				   stmt instanceof Stmt.Return) {
			// Also easy cases
			return ControlFlow.RETURN;
		} else if (stmt instanceof Stmt.Break) {
			// Also easy cases
			return ControlFlow.BREAK;
		} else if (stmt instanceof Stmt.IfElse) {
			return check((Stmt.IfElse) stmt);
		} else if (stmt instanceof Stmt.For) {
			return check((Stmt.For) stmt);
		} else if (stmt instanceof Stmt.While) {
			return check((Stmt.While) stmt);
		} else if (stmt instanceof Stmt.Switch) {
			return check((Stmt.Switch) stmt);
		} else {
			internalFailure("unknown statement encountered (" + stmt + ")", file.filename, stmt);
			return null; // deadcode (ah, the irony)
		}
	}
	
	public ControlFlow check(Stmt.IfElse stmt) {
		ControlFlow t = check(stmt.getTrueBranch());
		ControlFlow f = check(stmt.getFalseBranch());
		return join(t,f, stmt);
	}
	
	public ControlFlow check(Stmt.Switch stmt) {
		boolean fallThru = true;
		boolean hasBreak = false;
		
		// This algorithm is a bit tricky, and it does assume that default can
		// only come last.  It's possible there are still some bugs in here...
		for (Stmt.Case c : stmt.getCases()) {
			ControlFlow r = check(c.getBody());
			if (r == ControlFlow.BREAK || r == ControlFlow.BREAKNEXT) {
				hasBreak = true;
			}
			if (c.isDefault() && r == ControlFlow.RETURN) {
				fallThru = false;
			} 
		}
		
		if(fallThru || hasBreak) {
			return ControlFlow.NEXT;
		} else {
			return ControlFlow.RETURN;
		}
	}
	
	public ControlFlow check(Stmt.For stmt) {
		check(stmt.getBody());
		return ControlFlow.NEXT;
	}
	
	public ControlFlow check(Stmt.While stmt) {
		check(stmt.getBody());
		return ControlFlow.NEXT;		
	}
	
	/**
	 * Soundly combine two control flow markers together.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	private ControlFlow join(ControlFlow left, ControlFlow right, SyntacticElement element) {
		if(left == right) {
			return left;
		} else if(left == ControlFlow.RETURN) {
			return right; // BREAK or NEXT
		} else if(right == ControlFlow.RETURN) {
			return left; // BREAK or NEXT
		} else if(left == ControlFlow.BREAKNEXT || right == ControlFlow.BREAKNEXT) {
			return ControlFlow.BREAKNEXT;
		} else if(left == ControlFlow.BREAK) {
			// right must be NEXT
			return ControlFlow.BREAKNEXT;
		} else if(right == ControlFlow.BREAK) {
			// left must be NEXT
			return ControlFlow.BREAKNEXT;
		} else {
			internalFailure("unreachable code reached",file.filename,element);
			return null; // deadcode
		}
	}
	
	private enum ControlFlow { NEXT, RETURN, BREAK, BREAKNEXT };
	
	/**
	 * Check that the return type is equivalent to void
	 * 
	 * @param t
	 * @param elem
	 */
	private void checkIsVoid(Type t, SyntacticElement elem) {
		if(t instanceof Type.Void) {
			return;
		} else {
			syntaxError("missing return statement",file.filename,elem);
		}
		
	}
}
