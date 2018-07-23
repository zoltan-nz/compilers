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

import java.util.*;

import whilelang.ast.Expr;
import whilelang.ast.Stmt;
import whilelang.ast.WhileFile;
import whilelang.util.Pair;

/**
 * Responsible for checking that all variables are defined before they are used.
 * The algorithm for checking this involves a depth-first search through the
 * control-flow graph of the method. Throughout this, a list of the defined
 * variables is maintained.
 * 
 * @author David J. Pearce
 * 
 */
public class DefiniteAssignment {
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
		// First, initialise the environment with all parameters (since these
		// are assumed to be definitely assigned)
		Defs environment = new Defs();
		for (WhileFile.Parameter p : fd.getParameters()) {
			environment = environment.add(p.name());
		}

		// Second, check all statements in the method body
		check(fd.getBody(), environment);
	}

	/**
	 * Check that all variables used in a given list of statements are
	 * definitely assigned. Furthermore, update the set of definitely assigned
	 * variables to include any which are definitely assigned at the end of
	 * these statements.
	 * 
	 * @param statements
	 *            The list of statements to check.
	 * @param environment
	 *            The set of variables which are definitely assigned.
	 */
	public ControlFlow check(List<Stmt> statements, Defs environment) {
		Defs nextEnvironment = environment;
		Defs breakEnvironment = null;
		for (Stmt s : statements) {
			ControlFlow nf = check(s, nextEnvironment);
			nextEnvironment = nf.nextEnvironment;
			breakEnvironment = join(breakEnvironment,nf.breakEnvironment);
		}
		return new ControlFlow(nextEnvironment,breakEnvironment);
	}

	/**
	 * Check that all variables used in a given statement are definitely
	 * assigned. Furthermore, update the set of definitely assigned variables to
	 * include any which are definitely assigned after this statement.
	 * 
	 * @param statement
	 *            The statement to check.
	 * @param environment
	 *            The set of variables which are definitely assigned.
	 * @return The updated set of variables which are now definitely assigned,
	 *         or null if the method has terminated.
	 */
	public ControlFlow check(Stmt stmt, Defs environment) {
		if (stmt instanceof Stmt.Assert) {
			return check((Stmt.Assert) stmt, environment);
		} else if (stmt instanceof Stmt.Assign) {
			return check((Stmt.Assign) stmt, environment);
		} else if (stmt instanceof Stmt.Break) {
			return check((Stmt.Break) stmt, environment);
		} else if (stmt instanceof Stmt.Continue) {
			return check((Stmt.Continue) stmt, environment);
		} else if (stmt instanceof Stmt.Print) {
			return check((Stmt.Print) stmt, environment);
		} else if (stmt instanceof Stmt.Return) {
			return check((Stmt.Return) stmt, environment);
		} else if (stmt instanceof Stmt.VariableDeclaration) {
			return check((Stmt.VariableDeclaration) stmt, environment);
		} else if (stmt instanceof Expr.Invoke) {
			check((Expr.Invoke) stmt, environment);
			return new ControlFlow(environment,null);
		} else if (stmt instanceof Stmt.IfElse) {
			return check((Stmt.IfElse) stmt, environment);
		} else if (stmt instanceof Stmt.For) {
			return check((Stmt.For) stmt, environment);
		} else if (stmt instanceof Stmt.While) {
			return check((Stmt.While) stmt, environment);
		} else if (stmt instanceof Stmt.Switch) {
			return check((Stmt.Switch) stmt, environment);
		} else {
			internalFailure("unknown statement encountered (" + stmt + ")", file.filename, stmt);
			return null;
		}
	}

	public ControlFlow check(Stmt.Assert stmt, Defs environment) {
		check(stmt.getExpr(), environment);
		return new ControlFlow(environment,null);		
	}

	public ControlFlow check(Stmt.Assign stmt, Defs environment) {
		check(stmt.getRhs(), environment);
		if (stmt.getLhs() instanceof Expr.Variable) {
			Expr.Variable var = (Expr.Variable) stmt.getLhs();
			environment = environment.add(var.getName());
		} else {
			check(stmt.getLhs(), environment);
		}
		return new ControlFlow(environment,null);
	}

	public ControlFlow check(Stmt.Break stmt, Defs environment) {
		// Here we just move the current environment into the "break"
		// control-flow position.
		return new ControlFlow(null,environment);		
	}
	
	public ControlFlow check(Stmt.Continue stmt, Defs environment) {
		// Here we can just treat a continue in the same way as a return
		// statement. It makes no real difference.
		return new ControlFlow(null,null);		
	}
	
	public ControlFlow check(Stmt.Print stmt, Defs environment) {
		check(stmt.getExpr(), environment);
		return new ControlFlow(environment,null);
	}

	public ControlFlow check(Stmt.Return stmt, Defs environment) {
		if(stmt.getExpr() != null) {
			check(stmt.getExpr(), environment);
		}
		// In this case, control does not continue after this statement so we
		// return no execution path.
		return new ControlFlow(null,null);
	}

	public ControlFlow check(Stmt.VariableDeclaration stmt, Defs environment) {
		if (environment.contains(stmt.getName())) {
			syntaxError("variable already declared: " + stmt.getName(), file.filename, stmt);
		} else if (stmt.getExpr() != null) {
			check(stmt.getExpr(), environment);
			environment = environment.add(stmt.getName());
		}
		return new ControlFlow(environment,null);
	}

	public ControlFlow check(Stmt.IfElse stmt, Defs environment) {
		check(stmt.getCondition(), environment);
		ControlFlow left = check(stmt.getTrueBranch(), environment);
		ControlFlow right = check(stmt.getFalseBranch(), environment);
		// Now, merge all generated control-flow paths together
		return left.merge(right);
	}

	public ControlFlow check(Stmt.For stmt, Defs environment) {
		ControlFlow loop = check(stmt.getDeclaration(), environment);
		check(stmt.getCondition(), loop.nextEnvironment);
		check(stmt.getIncrement(), loop.nextEnvironment);
		//
		check(stmt.getBody(), loop.nextEnvironment);
		//
		return new ControlFlow(environment,null);
	}

	public ControlFlow check(Stmt.While stmt, Defs environment) {
		check(stmt.getCondition(), environment);
		//
		check(stmt.getBody(), environment);
		//
		return new ControlFlow(environment,null);
	}
	
	public ControlFlow check(Stmt.Switch stmt, Defs environment) {
		Defs nextEnvironment = environment;
		Defs breakEnvironment = null;
		
		check(stmt.getExpr(), environment);
		for(Stmt.Case c : stmt.getCases()) {
			ControlFlow cf = check(c.getBody(), environment);
			breakEnvironment = join(breakEnvironment,cf.breakEnvironment);
			if(c.isDefault()) {
				// We have a default case which will catch everything else
				nextEnvironment = cf.nextEnvironment;
			}
		}
		breakEnvironment = join(nextEnvironment,breakEnvironment);
		return new ControlFlow(breakEnvironment,null);
	}

	/**
	 * Check that all variables used in a given expression are definitely
	 * assigned.
	 * 
	 * @param expr
	 *            The expression to check.
	 * @param environment
	 *            The set of variables which are definitely assigned.
	 */
	public void check(Expr expr, Defs environment) {
		if (expr instanceof Expr.Binary) {
			check((Expr.Binary) expr, environment);
		} else if (expr instanceof Expr.Constant) {
			check((Expr.Constant) expr, environment);
		} else if (expr instanceof Expr.IndexOf) {
			check((Expr.IndexOf) expr, environment);
		} else if (expr instanceof Expr.Invoke) {
			check((Expr.Invoke) expr, environment);
		} else if (expr instanceof Expr.ArrayGenerator) {
			check((Expr.ArrayGenerator) expr, environment);
		} else if (expr instanceof Expr.ArrayInitialiser) {
			check((Expr.ArrayInitialiser) expr, environment);
		} else if (expr instanceof Expr.RecordAccess) {
			check((Expr.RecordAccess) expr, environment);
		} else if (expr instanceof Expr.RecordConstructor) {
			check((Expr.RecordConstructor) expr, environment);
		} else if (expr instanceof Expr.Unary) {
			check((Expr.Unary) expr, environment);
		} else if (expr instanceof Expr.Variable) {
			check((Expr.Variable) expr, environment);
		} else {
			internalFailure("unknown expression encountered (" + expr + ")", file.filename, expr);
		}
	}

	public void check(Expr.Binary expr, Defs environment) {
		check(expr.getLhs(), environment);
		check(expr.getRhs(), environment);
	}

	public void check(Expr.Constant expr, Defs environment) {
		// Constants are obviousy already defined ;)
	}

	public void check(Expr.IndexOf expr, Defs environment) {
		check(expr.getSource(), environment);
		check(expr.getIndex(), environment);
	}

	public void check(Expr.Invoke expr, Defs environment) {
		for (Expr arg : expr.getArguments()) {
			check(arg, environment);
		}
	}

	public void check(Expr.ArrayGenerator expr, Defs environment) {
		check(expr.getValue(), environment);
		check(expr.getSize(), environment);
	}

	public void check(Expr.ArrayInitialiser expr, Defs environment) {
		for (Expr arg : expr.getArguments()) {
			check(arg, environment);
		}
	}

	public void check(Expr.RecordAccess expr, Defs environment) {
		check(expr.getSource(), environment);
	}

	public void check(Expr.RecordConstructor expr, Defs environment) {
		for (Pair<String, Expr> arg : expr.getFields()) {
			check(arg.second(), environment);
		}
	}

	public void check(Expr.Unary expr, Defs environment) {
		check(expr.getExpr(), environment);
	}

	public void check(Expr.Variable expr, Defs environment) {
		if (environment == null || !environment.contains(expr.getName())) {
			// This variable is not definitely assigned.
			syntaxError("variable " + expr.getName() + " is not definitely assigned", file.filename, expr);
		}
	}
	
	private class ControlFlow {
		/**
		 * The set of definitely assigned variables on this path which fall
		 * through to the next logical statement.
		 */
		public final Defs nextEnvironment;
		
		/**
		 * The set of definitely assigned variables on this path which are on
		 * the control-flow path caused by a break statement.
		 */
		public final Defs breakEnvironment;		
		
		public ControlFlow(Defs nextEnvironment, Defs breakEnvironment) {
			this.nextEnvironment = nextEnvironment;
			this.breakEnvironment = breakEnvironment;
		}
		
		public ControlFlow merge(ControlFlow other) {
			Defs n = join(nextEnvironment,other.nextEnvironment);
			Defs b = join(breakEnvironment,other.breakEnvironment);
			return new ControlFlow(n,b);
		}			
	}

	private static Defs join(Defs left, Defs right) {
		if(left == null && right == null) {
			return null;
		} else if(left == null) {
			return right;
		} else if(right == null) {
			return left;
		} else {
			return left.join(right);
		}
	}
	
	/**
	 * A simple class representing an immutable set of definitely assigned
	 * variables.
	 * 
	 * @author David J. Pearce
	 *
	 */
	private class Defs {
		private HashSet<String> variables;

		public Defs() {
			this.variables = new HashSet<String>();
		}

		public Defs(Defs defs) {
			this.variables = new HashSet<String>(defs.variables);
		}

		public boolean contains(String var) {
			return variables.contains(var);
		}

		/**
		 * Add a variable to the set of definitely assigned variables, producing
		 * an updated set.
		 * 
		 * @param var
		 * @return
		 */
		public Defs add(String var) {
			Defs r = new Defs(this);
			r.variables.add(var);
			return r;
		}

		/**
		 * Remove a variable from the set of definitely assigned variables, producing
		 * an updated set.
		 * 
		 * @param var
		 * @return
		 */
		public Defs remove(String var) {
			Defs r = new Defs(this);
			r.variables.remove(var);
			return r;
		}
		
		/**
		 * Join two sets together, where the result contains a variable only if
		 * it is definitely assigned on both branches.
		 * 
		 * @param other
		 * @return
		 */
		public Defs join(Defs other) {
			Defs r = new Defs();
			for (String var : variables) {
				if (other.contains(var)) {
					r.variables.add(var);
				}
			}
			return r;
		}
		
		/**
		 * Useful for debugging
		 */
		public String toString() {
			return variables.toString();
		}
	}
}
