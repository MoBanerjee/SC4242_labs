package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import lexer.Lexer;

import org.junit.Test;

import frontend.Token;
import frontend.Token.Type;
import static frontend.Token.Type.*;

/**
 * This class contains unit tests for your lexer. Currently, there is only one test, but you
 * are strongly encouraged to write your own tests.
 */
public class LexerTests {
	// helper method to run tests; no need to change this
	private final void runtest(String input, Token... output) {
		Lexer lexer = new Lexer(new StringReader(input));
		int i=0;
		Token actual=new Token(MODULE, 0, 0, ""), expected;
		try {
			do {
				assertTrue(i < output.length);
				expected = output[i++];
				try {
					actual = lexer.nextToken();
					assertEquals(expected, actual);
				} catch(Error e) {
					if(expected != null)
						fail(e.getMessage());
					/* return; */
				}
			} while(!actual.isEOF());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/** Example unit test. */
	@Test
	public void testKWs() {
		// first argument to runtest is the string to lex; the remaining arguments
		// are the expected tokens
		runtest("module false return while",
				new Token(MODULE, 0, 0, "module"),
				new Token(FALSE, 0, 7, "false"),
				new Token(RETURN, 0, 13, "return"),
				new Token(WHILE, 0, 20, "while"),
				new Token(EOF, 0, 25, ""));
	}

	@Test
	public void testStringLiteralWithDoubleQuote() {
		runtest("\"\"\"",
				new Token(STRING_LITERAL, 0, 0, ""),
				(Token)null,
				new Token(EOF, 0, 3, ""));
	}

	@Test
	public void testStringLiteral() {
		runtest("\"\\n\"", 
				new Token(STRING_LITERAL, 0, 0, "\\n"),
				new Token(EOF, 0, 4, ""));
	}
//	Custom Tests
//	Testing Keywords
	@Test
	public void testKeywords() {
		runtest("boolean break\nelse\tfalse\rif\fimport int module public return true type void while", 
				new Token(BOOLEAN, 0, 0, "boolean"),
				new Token(BREAK,0,8,"break"),
				new Token(ELSE,1,0,"else"),
				new Token(FALSE,1,5,"false"),
				new Token(IF,2,0,"if"),
				new Token(IMPORT,3,0,"import"),
				new Token(INT,3,7,"int"),
				new Token(MODULE,3,11,"module"),
				new Token(PUBLIC,3,18,"public"),
				new Token(RETURN, 3, 25, "return"),
				new Token(TRUE, 3,32, "true"),
				new Token(TYPE, 3, 37, "type"),
				new Token(VOID, 3, 42, "void"),
				new Token(WHILE, 3, 47, "while"),
				new Token(EOF, 3, 52, "")
				
				);
	}

//	Testing Punctuation
	@Test
	public void testPunctuation() {
		runtest(", [] { } ( );",
				new Token(COMMA, 0, 0, ","),
				new Token(LBRACKET, 0, 2, "["),
				new Token(RBRACKET, 0, 3, "]"),
				new Token(LCURLY, 0, 5, "{"),
				new Token(RCURLY, 0, 7, "}"),
				new Token(LPAREN, 0, 9, "("),
				new Token(RPAREN, 0, 11, ")"),
				new Token(SEMICOLON, 0, 12, ";"),
				new Token(EOF, 0, 13, "")
				);
	}


//	Testing Operators
	@Test
	public void testOps() {
		runtest("/=== >>====<=<-!==+*",
				new Token(DIV, 0, 0, "/"),
				new Token(EQEQ, 0, 1, "=="),
				new Token(EQL, 0, 3, "="),
				new Token(GT, 0, 5, ">"),
				new Token(GEQ, 0, 6, ">="),
				new Token(EQEQ, 0, 8, "=="),
				new Token(EQL, 0, 10, "="),
				new Token(LEQ, 0, 11, "<="),
				new Token(LT, 0, 13, "<"),
				new Token(MINUS, 0, 14, "-"),
				new Token(NEQ, 0, 15, "!="),
				new Token(EQL, 0, 17, "="),
				new Token(PLUS, 0, 18, "+"),
				new Token(TIMES, 0, 19, "*"),
				new Token(EOF, 0, 20, "")
				);
	}

// Testing Identifiers with keyword and int literal
   @Test
   public void testId() {
		runtest("ijkb Boolean sc4242 breaker true sc_89yzW _uiux 67M", 
				new Token(ID, 0, 0, "ijkb"),
				new Token(ID, 0, 5, "Boolean"),
				new Token(ID, 0, 13, "sc4242"),
				new Token(ID,0,20,"breaker"),
				new Token(TRUE,0,28,"true"),
				new Token(ID,0,33,"sc_89yzW"),
				(Token)null,
				new Token(ID,0,43,"uiux"),
				new Token(INT_LITERAL,0,48,"67"),
				new Token(ID,0,50,"M"),
				new Token(EOF,0,51,"")
				
				);
   }
// Testing int literal
   @Test
   public void testInteger() {
		runtest("-0090", 
				new Token(MINUS, 0, 0, "-"),
				new Token(INT_LITERAL, 0, 1, "0090"),
				new Token(EOF, 0, 5, "")
				
				);
   }
// Testing string literal
   @Test
   public void testString() {
		runtest("\"56 apples\"", 
				new Token(STRING_LITERAL, 0, 0, "56 apples"),
				new Token(EOF, 0, 11, "")
				
				);
   }
// Testing wrong string literal
   @Test
   public void testWrongString() {
		runtest("\n\"\n\"", 
				(Token)null,
				(Token)null,
				new Token(EOF, 2, 1, "")
				);
   }
}