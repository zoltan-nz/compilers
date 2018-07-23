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

package whilelang.util;

import java.util.*;

import whilelang.ast.*;

import static whilelang.util.SyntaxError.*;

/**
 * A simple interpreter for WhileLang programs, which executes them in their
 * Abstract Syntax Tree form directly. The interpreter is not designed to be
 * efficient in anyway, however it's purpose is to provide a reference
 * implementation for the language.
 * 
 * @author David J. Pearce
 * 
 */
public class Interpreter {
	private HashMap<String, WhileFile.Decl> declarations;
	private WhileFile file;
	
	public void run(WhileFile wf) {
		// First, initialise the map of declaration names to their bodies.
		declarations = new HashMap<String,WhileFile.Decl>();
		for(WhileFile.Decl decl : wf.declarations) {
			declarations.put(decl.name(), decl);
		}
		this.file = wf;
		
		// Second, pick the main method (if one exits) and execute it
		WhileFile.Decl main = declarations.get("main");
		if(main instanceof WhileFile.MethodDecl) {
			WhileFile.MethodDecl fd = (WhileFile.MethodDecl) main;
			execute(fd);
		} else {
			System.out.println("Cannot find a main() function");
		}
	}
	
	/**
	 * Execute a given function with the given argument values. If the number of
	 * arguments is incorrect, then an exception is thrown.
	 * 
	 * @param function
	 *            Function declaration to execute.
	 * @param arguments
	 *            Array of argument values.
	 */
	private Object execute(WhileFile.MethodDecl function, Object... arguments) {
		
		// First, sanity check the number of arguments
		if(function.getParameters().size() != arguments.length){
			throw new RuntimeException(
					"invalid number of arguments supplied to execution of function \""
							+ function.getName() + "\"");
		}
		
		// Second, construct the stack frame in which this function will
		// execute.
		HashMap<String,Object> frame = new HashMap<String,Object>();
		for(int i=0;i!=arguments.length;++i) {
			WhileFile.Parameter parameter = function.getParameters().get(i);
			frame.put(parameter.getName(),arguments[i]);
		}
		
		// Third, execute the function body!
		return execute(function.getBody(),frame);
	}
	
	private Object execute(List<Stmt> block, HashMap<String,Object> frame) {
		for(int i=0;i!=block.size();i=i+1) {			
			Object r = execute(block.get(i),frame);			
			if(r != null) {
				return r;
			}
		}
		return null;
	}
	
	/**
	 * Execute a given statement in a given stack frame.
	 * 
	 * @param stmt
	 *            Statement to execute.
	 * @param frame
	 *            Stack frame mapping variables to their current value.
	 * @return
	 */
	private Object execute(Stmt stmt, HashMap<String,Object> frame) {		
		if(stmt instanceof Stmt.Assert) {
			return execute((Stmt.Assert) stmt,frame);
		} else if(stmt instanceof Stmt.Assign) {
			return execute((Stmt.Assign) stmt,frame);
		} else if(stmt instanceof Stmt.For) {
			return execute((Stmt.For) stmt,frame);
		} else if(stmt instanceof Stmt.While) {
			return execute((Stmt.While) stmt,frame);
		} else if(stmt instanceof Stmt.Break) {
			return execute((Stmt.Break) stmt,frame);
		} else if(stmt instanceof Stmt.Continue) {
			return execute((Stmt.Continue) stmt,frame);
		} else if(stmt instanceof Stmt.IfElse) {
			return execute((Stmt.IfElse) stmt,frame);
		} else if(stmt instanceof Stmt.Return) {
			return execute((Stmt.Return) stmt,frame);
		} else if(stmt instanceof Stmt.VariableDeclaration) {
			return execute((Stmt.VariableDeclaration) stmt,frame);
		} else if(stmt instanceof Stmt.Print) {
			return execute((Stmt.Print) stmt,frame);
		} else if(stmt instanceof Expr.Invoke) {
			return execute((Expr.Invoke) stmt,frame);
		} else {
			internalFailure("unknown statement encountered (" + stmt + ")", file.filename,stmt);
			return null;
		} 
	}
	
	private Object execute(Stmt.Assert stmt, HashMap<String,Object> frame) {
		boolean b = (Boolean) execute(stmt.getExpr(),frame);
		if(!b) {
			throw new RuntimeException("assertion failure");
		}
		return null;
	}
	
	private Object execute(Stmt.Assign stmt, HashMap<String,Object> frame) {	
		Expr lhs = stmt.getLhs();
		if(lhs instanceof Expr.Variable) {
			Expr.Variable ev = (Expr.Variable) lhs;
			Object rhs = execute(stmt.getRhs(),frame);
			// We need to perform a deep clone here to ensure the value
			// semantics used in While are preserved.
			frame.put(ev.getName(),deepClone(rhs));
		} else if(lhs instanceof Expr.RecordAccess) {
			Expr.RecordAccess ra = (Expr.RecordAccess) lhs;
			Map<String,Object> src = (Map) execute(ra.getSource(),frame);
			Object rhs = execute(stmt.getRhs(),frame);
			// We need to perform a deep clone here to ensure the value
			// semantics used in While are preserved.
			src.put(ra.getName(), deepClone(rhs));
		} else if(lhs instanceof Expr.IndexOf) {
			Expr.IndexOf io = (Expr.IndexOf) lhs;
			ArrayList<Object> src = (ArrayList) execute(io.getSource(),frame);
			Integer idx = (Integer) execute(io.getIndex(),frame);
			Object rhs = execute(stmt.getRhs(),frame);
			// We need to perform a deep clone here to ensure the value
			// semantics used in While are preserved.
			src.set(idx,deepClone(rhs));
		} else {
			internalFailure("unknown lval encountered (" + lhs + ")", file.filename,stmt);
		}
		
		return null;
	}
	
	private Object execute(Stmt.For stmt, HashMap<String,Object> frame) {
		execute(stmt.getDeclaration(),frame);
		while((Boolean) execute(stmt.getCondition(),frame)) {
			Object ret = execute(stmt.getBody(),frame);
			if(ret == BREAK_CONSTANT) {
				break;
			} else if(ret == CONTINUE_CONSTANT) {
				// continue :)
			} else if(ret != null) {
				return ret;
			}
			execute(stmt.getIncrement(),frame);
		}
		return null;
	}
	
	private Object execute(Stmt.While stmt, HashMap<String,Object> frame) {
		while((Boolean) execute(stmt.getCondition(),frame)) {
			Object ret = execute(stmt.getBody(),frame);
			if(ret == BREAK_CONSTANT) {
				break;
			} else if(ret == CONTINUE_CONSTANT) {
				// continue :)
			} else if(ret != null) {
				return ret;
			}
		}
		return null;
	}
	
	private Object execute(Stmt.IfElse stmt, HashMap<String,Object> frame) {
		boolean condition = (Boolean) execute(stmt.getCondition(),frame);
		if(condition) {
			return execute(stmt.getTrueBranch(),frame);
		} else {
			return execute(stmt.getFalseBranch(),frame);
		}
	}
	
	private Object execute(Stmt.Break stmt, HashMap<String, Object> frame) {
		return BREAK_CONSTANT;
	}
	
	private Object execute(Stmt.Continue stmt, HashMap<String, Object> frame) {
		return CONTINUE_CONSTANT;
	}
	
	private Object execute(Stmt.Return stmt, HashMap<String,Object> frame) {
		Expr re = stmt.getExpr();
		if(re != null) {
			return execute(re,frame);
		} else {
			return Collections.EMPTY_SET; // used to indicate a function has returned
		}
	}
	
	private Object execute(Stmt.VariableDeclaration stmt,
			HashMap<String, Object> frame) {
		Expr re = stmt.getExpr();
		Object value;
		if (re != null) {
			value = execute(re, frame);
		} else {
			value = Collections.EMPTY_SET; // used to indicate a variable has
											// been declared
		}
		// We need to perform a deep clone here to ensure the value
		// semantics used in While are preserved.
		frame.put(stmt.getName(), deepClone(value));
		return null;
	}
	
	private Object execute(Stmt.Print stmt, HashMap<String,Object> frame) {
		String str = toString(execute(stmt.getExpr(),frame));
		System.out.println(str);
		return null;
	}
	
	/**
	 * Execute a given expression in a given stack frame.
	 * 
	 * @param expr
	 *            Expression to execute.
	 * @param frame
	 *            Stack frame mapping variables to their current value.
	 * @return
	 */
	private Object execute(Expr expr, HashMap<String,Object> frame) {
		if(expr instanceof Expr.Binary) {
			return execute((Expr.Binary) expr,frame);
		} else if(expr instanceof Expr.Constant) {
			return execute((Expr.Constant) expr,frame);
		} else if(expr instanceof Expr.Invoke) {
			return execute((Expr.Invoke) expr,frame);
		} else if(expr instanceof Expr.IndexOf) {
			return execute((Expr.IndexOf) expr,frame);
		} else if(expr instanceof Expr.ArrayGenerator) {
			return execute((Expr.ArrayGenerator) expr,frame);
		} else if(expr instanceof Expr.ArrayInitialiser) {
			return execute((Expr.ArrayInitialiser) expr,frame);
		} else if(expr instanceof Expr.RecordAccess) {
			return execute((Expr.RecordAccess) expr,frame);
		} else if(expr instanceof Expr.RecordConstructor) {
			return execute((Expr.RecordConstructor) expr,frame);
		} else if(expr instanceof Expr.Unary) {
			return execute((Expr.Unary) expr,frame);
		} else if(expr instanceof Expr.Variable) {
			return execute((Expr.Variable) expr,frame);
		} else {
			internalFailure("unknown expression encountered (" + expr + ")", file.filename,expr);
			return null;
		} 
	}
	
	private Object execute(Expr.Binary expr, HashMap<String,Object> frame) {
		// First, deal with the short-circuiting operators first
		Object lhs = execute(expr.getLhs(), frame);
		
		switch (expr.getOp()) {
		case AND:
			return ((Boolean)lhs) && ((Boolean)execute(expr.getRhs(), frame));
		case OR:
			return ((Boolean)lhs) || ((Boolean)execute(expr.getRhs(), frame));
		}
		
		// Second, deal the rest.		
		Object rhs = execute(expr.getRhs(), frame);
		
		switch (expr.getOp()) {
		case ADD:
			return ((Integer)lhs) + ((Integer)rhs);
		case SUB:
			return ((Integer)lhs) - ((Integer)rhs);		
		case MUL:
			return ((Integer)lhs) * ((Integer)rhs);			
		case DIV:
			return ((Integer)lhs) / ((Integer)rhs);			
		case REM:
			return ((Integer)lhs) % ((Integer)rhs);			
		case EQ:
			return lhs.equals(rhs);
		case NEQ:
			return !lhs.equals(rhs);
		case LT:
			return ((Integer)lhs) < ((Integer)rhs);			
		case LTEQ:
			return ((Integer)lhs) <= ((Integer)rhs);			
		case GT:
			return ((Integer)lhs) > ((Integer)rhs);
		case GTEQ:
			return ((Integer)lhs) >= ((Integer)rhs);			
		}

		internalFailure("unknown binary expression encountered (" + expr + ")",
				file.filename, expr);
		return null;
	}
	
	private Object execute(Expr.Constant expr, HashMap<String,Object> frame) {
		return expr.getValue();
	}
	
	private Object execute(Expr.Invoke expr, HashMap<String, Object> frame) {
		List<Expr> arguments = expr.getArguments();
		Object[] values = new Object[arguments.size()];
		for (int i = 0; i != values.length; ++i) {
			// We need to perform a deep clone here to ensure the value
			// semantics used in While are preserved.
			values[i] = deepClone(execute(arguments.get(i), frame));
		}
		WhileFile.MethodDecl fun = (WhileFile.MethodDecl) declarations.get(expr
				.getName());
		return execute(fun, values);
	}
	
	private Object execute(Expr.IndexOf expr, HashMap<String,Object> frame) {
		Object _src = execute(expr.getSource(),frame);
		int idx = (Integer) execute(expr.getIndex(),frame);
		if(_src instanceof String) {
			String src = (String) _src;
			return src.charAt(idx);
		} else {
			ArrayList<Object> src = (ArrayList<Object>) _src;
			return src.get(idx);
		}
	}
	
	private Object execute(Expr.ArrayGenerator expr, HashMap<String, Object> frame) {
		Object value = execute(expr.getValue(),frame);
		int size = (Integer) execute(expr.getSize(),frame);
		ArrayList<Object> ls = new ArrayList<Object>();
		for (int i = 0; i < size; ++i) {
			ls.add(value);
		}
		return ls;
	}
	
	private Object execute(Expr.ArrayInitialiser expr,
			HashMap<String, Object> frame) {
		List<Expr> es = expr.getArguments();
		ArrayList<Object> ls = new ArrayList<Object>();
		for (int i = 0; i != es.size(); ++i) {
			ls.add(execute(es.get(i), frame));
		}
		return ls;
	}
	
	private Object execute(Expr.RecordAccess expr, HashMap<String, Object> frame) {
		HashMap<String, Object> src = (HashMap) execute(expr.getSource(), frame);
		return src.get(expr.getName());
	}
	
	private Object execute(Expr.RecordConstructor expr, HashMap<String,Object> frame) {
		List<Pair<String,Expr>> es = expr.getFields();
		HashMap<String,Object> rs = new HashMap<String,Object>();
		
		for(Pair<String,Expr> e : es) {
			rs.put(e.first(),execute(e.second(),frame));
		}
		
		return rs;
	}
	
	private Object execute(Expr.Unary expr, HashMap<String, Object> frame) {
		Object value = execute(expr.getExpr(), frame);
		switch (expr.getOp()) {
		case NOT:
			return !((Boolean) value);
		case NEG:
			return -((Integer) value);
		case LENGTHOF:
			return ((ArrayList) value).size();			
		}

		internalFailure("unknown unary expression encountered (" + expr + ")",
				file.filename, expr);
		return null;
	}
	
	private Object execute(Expr.Variable expr, HashMap<String,Object> frame) {
		return frame.get(expr.getName());
	}
	
	/**
	 * Perform a deep clone of the given object value. This is either a
	 * <code>Boolean</code>, <code>Integer</code>, , <code>Character</code>,
	 * <code>String</code>, <code>ArrayList</code> (for lists) or
	 * <code>HaspMap</code> (for records). Only the latter two need to be
	 * cloned, since the others are immutable.
	 * 
	 * @param o
	 * @return
	 */
	private Object deepClone(Object o) {
		if (o instanceof ArrayList) {
			ArrayList<Object> l = (ArrayList) o;
			ArrayList<Object> n = new ArrayList<Object>();
			for (int i = 0; i != l.size(); ++i) {
				n.add(deepClone(l.get(i)));
			}
			return n;
		} else if (o instanceof HashMap) {
			HashMap<String, Object> m = (HashMap) o;
			HashMap<String, Object> n = new HashMap<String, Object>();
			for (String field : m.keySet()) {
				n.put(field, deepClone(m.get(field)));
			}
			return n;
		} else {
			// other cases can be ignored
			return o;
		}
	}
	
	/**
	 * Convert the given object value to a string. This is either a
	 * <code>Boolean</code>, <code>Integer</code>, <code>Character</code>,
	 * <code>String</code>, <code>ArrayList</code> (for lists) or
	 * <code>HaspMap</code> (for records). The latter two must be treated
	 * recursively.
	 * 
	 * @param o
	 * @return
	 */
	private String toString(Object o) {
		if (o instanceof ArrayList) {
			ArrayList<Object> l = (ArrayList) o;
			String r = "[";
			for (int i = 0; i != l.size(); ++i) {
				if(i != 0) {
					r = r + ", ";
				}
				r += toString(l.get(i));
			}
			return r + "]";
		} else if (o instanceof HashMap) {
			HashMap<String, Object> m = (HashMap) o;
			String r = "{";
			boolean firstTime = true;
			ArrayList<String> fields = new ArrayList<String>(m.keySet());
			Collections.sort(fields);
			for (String field : fields) {
				if(!firstTime) {
					r += ",";
				}
				firstTime=false;
				r += field + ":" + toString(m.get(field));
			}
			return r + "}";
		} else if(o != null) {
			// other cases can use their default toString methods.
			return o.toString();
		} else {
			return "null";
		}
	}
	
	private Object BREAK_CONSTANT = new Object() {};
	private Object CONTINUE_CONSTANT = new Object() {};
}
