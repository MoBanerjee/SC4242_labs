package test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import backend.ProgramCodeGenerator;

import org.junit.Assert;
import lexer.Lexer;
import parser.Parser;
import soot.Printer;
import soot.SootClass;
import soot.jimple.JasminClass;
import soot.util.JasminOutputStream;
import ast.List;
import ast.Module;
import ast.Program;

/**
 * System tests for the compiler: compiles a given program to Java bytecode, then immediately
 * loads it into the running JVM and executes it.
 */
public class CompilerTests {
	// set this flag to true to dump generated Jimple code to standard output
	private static final boolean DEBUG = false;
	
	/**
	 * A simple class loader that allows us to directly load compiled classes.
	 */
	private static class CompiledClassLoader extends URLClassLoader {
		private final Map<String, byte[]> classes = new HashMap<String, byte[]>();
		
		public CompiledClassLoader() {
			super(new URL[0], CompilerTests.class.getClassLoader());
		}
		
		public void addClass(String name, byte[] code) {
			classes.put(name, code);
		}
		
		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			if(classes.containsKey(name)) {
				byte[] code = classes.get(name);
				return defineClass(name, code, 0, code.length);
			}
			return super.findClass(name);
		}
	}

	/**
	 * Test runner class.
	 * 
	 * @param modules_src Array of strings, representing the source code of the program modules
	 * @param main_module the name of the main module
	 * @param main_function the name of the main function
	 * @param parm_types the parameter types of the main function
	 * @param args arguments to pass to the main method
	 * @param expected expected result
	 */
	private void runtest(String[] modules_src, String main_module, String main_function, Class<?>[] parm_types, Object[] args, Object expected) {
		try {
			List<Module> modules = new List<Module>();
			for(String module_src : modules_src) {
				Parser parser = new Parser();
				Module module = (Module)parser.parse(new Lexer(new StringReader(module_src)));
				modules.add(module);
			}
			Program prog = new Program(modules);
			
			prog.namecheck();
			prog.typecheck();
			prog.flowcheck();
			if(prog.hasErrors()) {
				Assert.fail(prog.getErrors().iterator().next().toString());
			}
			
			CompiledClassLoader loader = new CompiledClassLoader();
			try {
				for(SootClass klass : new ProgramCodeGenerator().generate(prog)) {
					if(DEBUG) {
						PrintWriter stdout_pw = new PrintWriter(System.out);
						Printer.v().printTo(klass, stdout_pw);
						stdout_pw.flush();
					}

					String name = klass.getName();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(new JasminOutputStream(baos));
					new JasminClass(klass).print(pw);
					pw.flush();
					loader.addClass(name, baos.toByteArray());
				}

				Class<?> testclass = loader.loadClass(main_module);
				Method method = testclass.getMethod(main_function, parm_types);
				Object actual = method.invoke(null, args);
				if(!method.getReturnType().equals(void.class))
					Assert.assertEquals(expected, actual);
			} finally {
				loader.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} catch(ClassFormatError e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/** Convenience wrapper for runtest with only a single module. Other arguments are the same .*/
	private void runtest(String string, String classname, String methodname, Class<?>[] parmTypes, Object[] args, Object expected) {
		runtest(new String[] { string }, classname, methodname, parmTypes, args, expected);
	}

	@Test public void testAddition() {
		runtest("module Test {" +
				"  public int f() {" +
				"    return 23+19;" +
				"  }" +
				"}",
				"Test",
				"f",
				new Class<?>[0],
				new Object[0],
				42);
	}
	
    @Test
    public void testIntLiteral() {
        runtest("module Test { public int f() { return 42; } }", "Test", "f", new Class<?>[0], new Object[0], 42);
        runtest("module Test { public int f() { return -10; } }", "Test", "f", new Class<?>[0], new Object[0], -10);
        runtest("module Test { public int f() { return 0; } }", "Test", "f", new Class<?>[0], new Object[0], 0);
    }

    @Test
    public void testStringLiteral() { // #Doubt This wont work right??
        runtest("module Test { public String f() { return \"Hello, World!\"; } }", "Test", "f", new Class<?>[0], new Object[0], "Hello, World!");
        runtest("module Test { public String f() { return \"\"; } }", "Test", "f", new Class<?>[0], new Object[0], "");
        runtest("module Test { public String f() { return \"Special characters: !@#$%^&*()\"; } }", "Test", "f", new Class<?>[0], new Object[0], "Special characters: !@#$%^&*()");
    }

    @Test
    public void testBooleanLiteral() {
        runtest("module Test { public int f() { if(true)return 1;else return 0; } }", "Test", "f", new Class<?>[0], new Object[0], 1);
        runtest("module Test { public int f() { if(false)return 1;else return 0; } }", "Test", "f", new Class<?>[0], new Object[0], 0);
    }

    @Test
    public void testArrayLiteral() { // #Doubt How to check this? cause it apparently compares references while equating
        runtest("module Test { public int[] f() { return [1, 2, 3]; } }", "Test", "f", new Class<?>[0], new Object[0], new int[]{1, 2, 3});
        runtest("module Test { public int[] f() { return []; } }", "Test", "f", new Class<?>[0], new Object[0], new int[]{});
        runtest("module Test { public int[] f() { return [42]; } }", "Test", "f", new Class<?>[0], new Object[0], new int[]{42});
    }

    @Test
    public void testArrayIndex() {
        runtest("module Test { public int f() { int[] arr; arr=[5, 10, 15]; return arr[0]; } }", "Test", "f", new Class<?>[0], new Object[0], 5);
        runtest("module Test { public int f() { int[] arr; arr=new int[]{1,2,3}; return arr[1]; } }", "Test", "f", new Class<?>[0], new Object[0], 2); // #Doubt This is invalid right, cause lab2 never had any "new" keyword in cfg?
        runtest("module Test {  public int f() { int[] arr; arr = [5]; arr[0] = 1; arr[1] = 2; arr[2] = 3; return arr[1]; }}", "Test", "f", new Class<?>[0], new Object[0], 2); // #Doubt I thought this is valid, but does not work
        runtest("module Test { public int f() { int[] arr; arr=[5, 10, 15]; return arr[0]; } }", "Test", "f", new Class<?>[0], new Object[0], 5);
        runtest("module Test { public int f() { int[] arr; arr=[7, 14, 21]; return arr[2]; } }", "Test", "f", new Class<?>[0], new Object[0], 21);
    }

	@Test public void testString() { //#Borrowed
		runtest("module Test {" +
	            "public type string = \"java.lang.String\";  " +
				"  public string f() {" +
				"    string x;" +
				"    x = \"Hello World\";   " +
				"    return x;" +
				"  }" +
				"}",
				"Test",
				"f",
				new Class<?>[0],
				new Object[0],
				"Hello World");
	}
    @Test
    public void testBinaryExpr() {
        runtest("module Test { public int f() { return 21 + 21; } }", "Test", "f", new Class<?>[0], new Object[0], 42);
        runtest("module Test { public int f() { return 100 - 58; } }", "Test", "f", new Class<?>[0], new Object[0], 42);
        runtest("module Test { public int f() { return 6 * 7; } }", "Test", "f", new Class<?>[0], new Object[0], 42);
        runtest("module Test { public int f() { return 42 / 7; } }", "Test", "f", new Class<?>[0], new Object[0], 6);
        runtest("module Test { public int f() { return 42 % 5; } }", "Test", "f", new Class<?>[0], new Object[0], 2);
    }

    @Test
    public void testComparisonExpr() {
        runtest("module Test { public boolean f() { return 42 == 42; } }", "Test", "f", new Class<?>[0], new Object[0], true);
        runtest("module Test { public boolean f() { return 42 != 43; } }", "Test", "f", new Class<?>[0], new Object[0], true);
        runtest("module Test { public boolean f() { return 42 < 100; } }", "Test", "f", new Class<?>[0], new Object[0], true);
        runtest("module Test { public boolean f() { return 100 > 42; } }", "Test", "f", new Class<?>[0], new Object[0], true);
        runtest("module Test { public boolean f() { return 42 <= 42; } }", "Test", "f", new Class<?>[0], new Object[0], true);
        runtest("module Test { public boolean f() { return 42 >= 42; } }", "Test", "f", new Class<?>[0], new Object[0], true);
    }

    @Test
    public void testNegExpr() {
        runtest("module Test { public int f() { return -42; } }", "Test", "f", new Class<?>[0], new Object[0], -42);
        runtest("module Test { public int f() { return -0; } }", "Test", "f", new Class<?>[0], new Object[0], 0);
    }

    @Test
    public void testFunctionCall() {
        runtest("module Test { public int f() { return g(42); } public int g(int x) { return x; } }", "Test", "f", new Class<?>[0], new Object[0], 42);
        runtest("module Test { public int f() { return g(10, 20); } public int g(int x, int y) { return x + y; } }", "Test", "f", new Class<?>[0], new Object[0], 30);
    }

    @Test
    public void testMixedExpressions() {
        runtest("module Test { public int f() { return ((10 + 5) * 3) / (2 - 1); } }", "Test", "f", new Class<?>[0], new Object[0], 45);
        runtest("module Test { public int f() { return (5 + 3) * (2 + 6); } }", "Test", "f", new Class<?>[0], new Object[0], 64);
        runtest("module Test { public int f() { return (100 - 50) / 5 + (8 * 4); } }", "Test", "f", new Class<?>[0], new Object[0], 42);
    }
}
