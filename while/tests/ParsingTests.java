package whilelang.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import whilelang.ast.WhileFile;
import whilelang.compiler.DefiniteAssignment;
import whilelang.compiler.Lexer;
import whilelang.compiler.Parser;
import whilelang.compiler.TypeChecker;
import whilelang.compiler.WhileCompiler;

@RunWith(Parameterized.class)
public class ParsingTests {
private static final String WHILE_SRC_DIR = "tests/invalid-parsing/".replace('/', File.separatorChar);
	
	private final String testName;
	
	public ParsingTests(String testName) {
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
		File srcFile = new File(WHILE_SRC_DIR + testname + ".while");
		// First, lex and parse the input file. If any errors occur here then
		// they are genuine errors.
		Lexer lexer = new Lexer(srcFile.getPath());
		Parser parser = new Parser(srcFile.getPath(), lexer.scan());

		// Second, run type checker over the AST. We expect every test to
		// throw an error here. So, tests which do should pass, tests which
		// don't should fail. 
		try {
			WhileFile ast = parser.read();
			fail();
		} catch (Exception e) {
			// Success!
		}
	}
}
