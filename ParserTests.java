package test;

import static org.junit.Assert.fail;

import java.io.StringReader;

import lexer.Lexer;

import org.junit.Test;

import parser.Parser;

public class ParserTests {
	private void runtest(String src) {
		runtest(src, true);
	}

	private void runtest(String src, boolean succeed) {
		Parser parser = new Parser();
		try {
			parser.parse(new Lexer(new StringReader(src)));
			if(!succeed) {
				fail("Test was supposed to fail, but succeeded");
			}
		} catch (beaver.Parser.Exception e) {
			if(succeed) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}


		@Test
		public void testEmptyModule() {
			runtest("module Test { }");
		}

		@Test
		public void testSingleImport() {
			runtest("module Test { import AnotherModule; }");
		}

		@Test
		public void testMultipleImports() {
			runtest("module Test { import Module1; import Module2; }");
		}

		@Test
		public void testFunctionDeclaration() {
			runtest("module Test { public int myFunction(int a, boolean b) { return a; } }");
		}

		@Test
		public void testFieldDeclaration() {
			runtest("module Test { public int myField; }");
		}

		@Test
		public void testTypeDeclaration() {
			runtest("module Test { public type myType = \"example\"; }");
		}

		// Edge case test cases
		@Test
		public void testEmptyImport() {
			runtest("module Test { import ; }", false); 
		}

		@Test
		public void testMissingSemicolonInImport() {
			runtest("module Test { import AnotherModule }", false); 
		}

		@Test
		public void testNestedBlockStatements() {
			runtest("module Test { public void func() { { int x; { int y; } } } }");
		}

		@Test
		public void testIfStatementWithoutElse() {
			runtest("module Test { public void func() { if (true) { int x; } } }");
		}

		@Test
		public void testIfStatementWithElse() {
			runtest("module Test { public void func() { if (true) { int x; } else { int y; } } }");
		}

		@Test
		public void testWhileStatement() {
			runtest("module Test { public void func() { while (true) { int x; } } }");
		}

		@Test
		public void testBreakStatement() {
			runtest("module Test { public void func() { while (true) { break; } } }");
		}

		@Test
		public void testReturnStatement() {
			runtest("module Test { public int func() { return 5; } }");
		}

		@Test
		public void testExpressionStatement() {
			runtest("module Test { public void func() { int x = 5; x = x + 1; } }"); //Failing
		}
		
		@Test
		public void testInvalidExpression() {
			runtest("module Test { public void func() { int x = ; } }", false); 
		}

		@Test
		public void testNestedArrayAccess() {
			runtest("module Test { public void func() { int[][] arr; int x = arr[2][3]; } }"); //Failing
		}

		@Test
		public void testInvalidArrayAccess() {
			runtest("module Test { public void func() { int[] arr; int x = arr[]; } }", false); 
		}

		@Test
		public void testParenthesizedExpression() {
			runtest("module Test { public void func() { int x = (3 + (2 * 5)); } }"); //Failing
		}

		@Test
		public void testInvalidModuleSyntax() {
			runtest("module { }", false); 
		}

		@Test
		public void testMissingCurlyBraces() {
			runtest("module Test", false); 
		}

		@Test
		public void testPublicAccessibilitySpecifier() {
			runtest("module Test { public int a; }");
		}

		@Test
		public void testPrimitiveTypeDeclarations() {
			runtest("module Test { int a; boolean b; void func() { } }");
		}

		@Test
		public void testInvalidTypeDeclarations() {
			runtest("module Test { invalidType x; }"); // Doubt
		}


		@Test
		public void testComplexModule() {
			runtest(
				"module ComplexModule { " +
				"import Module1; " +
				"import Module2; " +
				"public int x; " +
				"public boolean y; " +
				"public void func(int a, boolean b) { " +
				"if (a > 0) { return; } else { x = a; } " +
				"while (b) { x = x + 1; break; } " +
				"} " +
				"public type MyType = \"example\"; " +
				"}"
			);
		}
	}