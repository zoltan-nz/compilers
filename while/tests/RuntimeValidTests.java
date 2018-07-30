package whilelang.testing;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import whilelang.ast.WhileFile;
import whilelang.compiler.*;
import whilelang.util.Interpreter;

@RunWith(Parameterized.class)
public class RuntimeValidTests {
	private static final String WHILE_SRC_DIR = "tests/valid/".replace('/', File.separatorChar);
	
	private final String testName;
	
	public RuntimeValidTests(String testName) {
		this.testName = testName;
	}

	// Here we enumerate all available test cases.
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> testcases = new ArrayList<Object[]>();
		for (File f : new File(WHILE_SRC_DIR).listFiles()) {
			if (f.isFile()) {
				String name = f.getName();
				if (name.endsWith(".while")) {
					// Get rid of ".while" extension
					String testName = name.substring(0, name.length() - 6);
					testcases.add(new Object[] { testName });
				}
			}
		}		
		return testcases;
	}
	
	@Test
	public void valid() throws IOException {
		runTest(this.testName);
	}
	
	/**
	 * Run the interpreter over a given source file. This should not produce any
	 * exceptions.
	 * 
	 * @param filename
	 * @throws IOException 
	 */
	private void runTest(String testname) throws IOException {
		WhileCompiler compiler = new WhileCompiler(WHILE_SRC_DIR + testname + ".while");
		WhileFile ast = compiler.compile();
		new Interpreter().run(ast);
	}	
}
