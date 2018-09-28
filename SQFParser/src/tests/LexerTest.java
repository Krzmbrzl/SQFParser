package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dataStructures.CharacterInputStream;
import dataStructures.ESQFOperatorType;
import dataStructures.ESQFTokentype;
import dataStructures.IErrorListener;
import dataStructures.SQFTestTokenFactory;
import dataStructures.SQFToken;
import dataStructures.TokenBuffer;
import lexer.SQFLexer;

class LexerTest {

	public static String LEXER_FILE_PATH = System.getProperty("user.home")
			+ "/Documents/Git/SQFParser/SQFParser/src/tests/";

	static SQFLexer lexer;

	@BeforeAll
	public static void setUp() {
		lexer = new SQFLexer();
		lexer.setTokenFactory(new SQFTestTokenFactory());
	}


	@Test
	void WhitespaceTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("    ".getBytes())));

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		SQFToken tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.WHITESPACE, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(4, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream(" 	 ".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.WHITESPACE, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(3, tokenInfo.stop(), "Wrong end index");
	}

	@Test
	void PreprocessorTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("#define test".getBytes())));

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		SQFToken tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.PREPROCESSOR, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(12, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("# define test\n".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0, 14 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.PREPROCESSOR, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(14, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("# define test\\\n 12 #\n".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0, 15, 21 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.PREPROCESSOR, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(21, tokenInfo.stop(), "Wrong end index");
		
		
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("# define test\\\r\n 12 #\n".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0, 16, 22 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.PREPROCESSOR, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(22, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("#ifdef test\n\t 12\n\n #endif".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0, 12, 17, 18 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.PREPROCESSOR, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(25, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(
				new ByteArrayInputStream("#ifndef #else #test\n\t #end if #endif".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0, 20 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.PREPROCESSOR, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(36, tokenInfo.stop(), "Wrong end index");
	}

	@Test
	void CommentTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("// test here".getBytes())));

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		SQFToken tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.COMMENT, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(12, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("// 	 \n".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0, 6 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.COMMENT, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(6, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("/*\n * / 	 \n*/".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0, 3, 11 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.COMMENT, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(13, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("/*\n * / 	 \n*/\n".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(2, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0, 3, 11, 14 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.COMMENT, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(13, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(
				new ByteArrayInputStream("// some stuff\n// On next line as well".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(2, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0, 14 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();
		assertEquals(ESQFTokentype.COMMENT, tokenInfo.type(), "Wrong token type!");
		tokenInfo = tokens.iterator().next();
		assertEquals(ESQFTokentype.COMMENT, tokenInfo.type(), "Wrong token type!");
	}

	@Test
	void StringTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("\"My str # test in here\"".getBytes())));

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		SQFToken tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.STRING, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(23, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("'My str # test in here'".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.STRING, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(23, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("'My str ''# test in'' here'".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.STRING, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(27, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("\"My str \"\"# test in\"\" here\"".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.STRING, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(27, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("\"My \tstr \n \there\"".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0, 10 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.STRING, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(17, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("'My \tstr \n \there'".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0, 10 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.STRING, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(17, tokenInfo.stop(), "Wrong end index");
	}

	@Test
	void StringPreprocessorCombination() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("\"My str \n#test in here\"".getBytes())));

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertEquals(2, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0, 9 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		Iterator<SQFToken> iterator = tokens.iterator();
		SQFToken tokenInfo1 = iterator.next();
		SQFToken tokenInfo2 = iterator.next();

		assertEquals(ESQFTokentype.SUBSTRING, tokenInfo1.type(), "Wrong token type!");
		assertEquals(0, tokenInfo1.start(), "Wrong start index");
		assertEquals(9, tokenInfo1.stop(), "Wrong end index");
		assertEquals(ESQFTokentype.PREPROCESSOR, tokenInfo2.type(), "Wrong token type!");
		assertEquals(9, tokenInfo2.start(), "Wrong start index");
		assertEquals(23, tokenInfo2.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("\"My str \n# test in here\"".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0, 9 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		iterator = tokens.iterator();
		tokenInfo1 = iterator.next();

		assertEquals(ESQFTokentype.STRING, tokenInfo1.type(), "Wrong token type!");
		assertEquals(0, tokenInfo1.start(), "Wrong start index");
		assertEquals(24, tokenInfo1.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("\"My str \n#test in here\n\"".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(3, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0, 9, 23 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		iterator = tokens.iterator();
		tokenInfo1 = iterator.next();
		tokenInfo2 = iterator.next();
		SQFToken tokenInfo3 = iterator.next();

		assertEquals(ESQFTokentype.SUBSTRING, tokenInfo1.type(), "Wrong token type!");
		assertEquals(0, tokenInfo1.start(), "Wrong start index");
		assertEquals(9, tokenInfo1.stop(), "Wrong end index");
		assertEquals(ESQFTokentype.PREPROCESSOR, tokenInfo2.type(), "Wrong token type!");
		assertEquals(9, tokenInfo2.start(), "Wrong start index");
		assertEquals(23, tokenInfo2.stop(), "Wrong end index");
		assertEquals(ESQFTokentype.SUBSTRING_END, tokenInfo3.type(), "Wrong token type!");
		assertEquals(23, tokenInfo3.start(), "Wrong start index");
		assertEquals(24, tokenInfo3.stop(), "Wrong end index");
	}

	@Test
	void NumberTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("123".getBytes())));

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		SQFToken tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(3, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("123.5".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(5, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream(".25".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(3, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("$12aFF".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(6, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("0x12aFF".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(7, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("1e123".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(5, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("1E123".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(5, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("3e123".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(5, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("3E123".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(5, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("3.2E123".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(7, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream(".2e678".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(6, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream(".2e-678".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(7, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream(".2e+678".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.NUMBER, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(7, tokenInfo.stop(), "Wrong end index");
	}

	@Test
	void IDTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("_12a".getBytes())));

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		SQFToken tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.ID, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(4, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("abc3".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.ID, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(4, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("e1c3".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.ID, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(4, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("e12345c3".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.ID, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(8, tokenInfo.stop(), "Wrong end index");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("e12345".getBytes())));

		tokens = lexer.getTokens();

		assertEquals(1, tokens.size(), "Wrong number of tokens");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		tokenInfo = tokens.iterator().next();

		assertEquals(ESQFTokentype.ID, tokenInfo.type(), "Wrong token type!");
		assertEquals(0, tokenInfo.start(), "Wrong start index");
		assertEquals(6, tokenInfo.stop(), "Wrong end index");
	}

	@Test
	void operatorTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("-*/!<>^=+==!=<=>=%&&||;:".getBytes())));

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertEquals(18, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");
		assertArrayEquals(new Integer[] { 0 }, lexer.getNewlineIndicesAsArray(), "Wrong line indices");

		for (int i = 0; i < tokens.size(); i++) {
			SQFToken tokenInfo = tokens.get(i);

			switch (i) {
			case 0:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(0, tokenInfo.start(), "Wrong start index");
				assertEquals(1, tokenInfo.stop(), "Wrong end index");
				break;
			case 1:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(1, tokenInfo.start(), "Wrong start index");
				assertEquals(2, tokenInfo.stop(), "Wrong end index");
				break;
			case 2:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(2, tokenInfo.start(), "Wrong start index");
				assertEquals(3, tokenInfo.stop(), "Wrong end index");
				break;
			case 3:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(3, tokenInfo.start(), "Wrong start index");
				assertEquals(4, tokenInfo.stop(), "Wrong end index");
				break;
			case 4:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(4, tokenInfo.start(), "Wrong start index");
				assertEquals(5, tokenInfo.stop(), "Wrong end index");
				break;
			case 5:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(5, tokenInfo.start(), "Wrong start index");
				assertEquals(6, tokenInfo.stop(), "Wrong end index");
				break;
			case 6:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(6, tokenInfo.start(), "Wrong start index");
				assertEquals(7, tokenInfo.stop(), "Wrong end index");
				break;
			case 7:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(7, tokenInfo.start(), "Wrong start index");
				assertEquals(8, tokenInfo.stop(), "Wrong end index");
				break;
			case 8:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(8, tokenInfo.start(), "Wrong start index");
				assertEquals(9, tokenInfo.stop(), "Wrong end index");
				break;
			case 9:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(9, tokenInfo.start(), "Wrong start index");
				assertEquals(11, tokenInfo.stop(), "Wrong end index");
				break;
			case 10:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(11, tokenInfo.start(), "Wrong start index");
				assertEquals(13, tokenInfo.stop(), "Wrong end index");
				break;
			case 11:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(13, tokenInfo.start(), "Wrong start index");
				assertEquals(15, tokenInfo.stop(), "Wrong end index");
				break;
			case 12:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(15, tokenInfo.start(), "Wrong start index");
				assertEquals(17, tokenInfo.stop(), "Wrong end index");
				break;
			case 13:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(17, tokenInfo.start(), "Wrong start index");
				assertEquals(18, tokenInfo.stop(), "Wrong end index");
				break;
			case 14:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(18, tokenInfo.start(), "Wrong start index");
				assertEquals(20, tokenInfo.stop(), "Wrong end index");
				break;
			case 15:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(20, tokenInfo.start(), "Wrong start index");
				assertEquals(22, tokenInfo.stop(), "Wrong end index");
				break;
			case 16:
				assertEquals(ESQFTokentype.SEMICOLON, tokenInfo.type(), "Wrong token type!");
				assertEquals(22, tokenInfo.start(), "Wrong start index");
				assertEquals(23, tokenInfo.stop(), "Wrong end index");
				break;
			case 17:
				assertEquals(ESQFTokentype.OPERATOR, tokenInfo.type(), "Wrong token type!");
				assertEquals(23, tokenInfo.start(), "Wrong start index");
				assertEquals(24, tokenInfo.stop(), "Wrong end index");
				break;
			}
		}
	}

	@Test
	void inputFileTest() throws FileNotFoundException, IOException {
		CharacterInputStream input = new CharacterInputStream(
				new FileInputStream(new File(LEXER_FILE_PATH + "/LexerInput01")));
		lexer.lex(input);

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertEquals(22, tokens.size(), "Wrong number of tokens (" + tokens.size() + ")");

		ESQFTokentype[] tokenTypes = new ESQFTokentype[tokens.size()];

		for (int i = 0; i < tokens.size(); i++) {
			tokenTypes[i] = tokens.get(i).type();
		}

		assertArrayEquals(new ESQFTokentype[] { ESQFTokentype.COMMENT, ESQFTokentype.WHITESPACE, ESQFTokentype.COMMENT,
				ESQFTokentype.WHITESPACE, ESQFTokentype.STRING, ESQFTokentype.WHITESPACE, ESQFTokentype.STRING,
				ESQFTokentype.WHITESPACE, ESQFTokentype.STRING, ESQFTokentype.WHITESPACE, ESQFTokentype.STRING,
				ESQFTokentype.WHITESPACE, ESQFTokentype.COMMENT, ESQFTokentype.PREPROCESSOR, ESQFTokentype.WHITESPACE,
				ESQFTokentype.PREPROCESSOR, ESQFTokentype.WHITESPACE, ESQFTokentype.PREPROCESSOR,
				ESQFTokentype.WHITESPACE, ESQFTokentype.SUBSTRING, ESQFTokentype.PREPROCESSOR,
				ESQFTokentype.SUBSTRING_END }, tokenTypes, "Wrong token types");
	}

	@Test
	void macros() throws IOException {
		HashSet<String> macros = new HashSet<>();

		macros.add("MY_TEST_MACRO");

		lexer.setMacros(macros);

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("MY_TEST_MACRO".getBytes())));

		TokenBuffer<SQFToken> tokens = lexer.getTokens();

		assertTrue(tokens.size() == 1, "Got " + tokens.size() + " tokens when expecting only one!");
		assertEquals(ESQFOperatorType.MACRO, tokens.get(0).operatorType(), "Expected macro operator type!");
		assertEquals(ESQFTokentype.MACRO, tokens.get(0).type(), "Expected macro type!");

		// macros must be case-sensitive
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("MY_tEST_MACRO".getBytes())));
		tokens = lexer.getTokens();
		assertTrue(tokens.size() == 1, "Got " + tokens.size() + " tokens when expecting only one!");
		assertEquals(ESQFOperatorType.NULAR, tokens.get(0).operatorType(), "Expected nular operator type!");
		assertEquals(ESQFTokentype.ID, tokens.get(0).type(), "Expected ID type!");

		// macro with arguments
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("MY_TEST_MACRO(arg)".getBytes())));
		tokens = lexer.getTokens();
		assertTrue(tokens.size() == 4, "Got " + tokens.size() + " tokens when expecting 3!");
		assertEquals(ESQFOperatorType.MACRO, tokens.get(0).operatorType(), "Expected macro operator type!");
		assertEquals(ESQFTokentype.MACRO, tokens.get(0).type(), "Expected macro type!");
		assertEquals(ESQFOperatorType.OTHER, tokens.get(1).operatorType(), "Expected macro operator type!");
		assertEquals(ESQFTokentype.PARENTHESIS_OPEN, tokens.get(1).type(), "Wrong type!");
		assertEquals(ESQFOperatorType.NULAR, tokens.get(2).operatorType(), "Expected nular operator type!");
		assertEquals(ESQFTokentype.ID, tokens.get(2).type(), "Expected ID type!");
		assertEquals(ESQFOperatorType.OTHER, tokens.get(3).operatorType(), "Expected macro operator type!");
		assertEquals(ESQFTokentype.PARENTHESIS_CLOSE, tokens.get(3).type(), "Wrong type!");

		lexer.reset(true);
	}

	@Test
	void unicodeTest() throws IOException {
		lexer.setErrorListener(new IErrorListener() {

			@Override
			public void error(String msg, SQFToken token) {
				fail("Expected no error but got: " + msg);
			}
		});
		
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("äöü//°".getBytes(StandardCharsets.UTF_8))));

		lexer.resetListener();
	}

	public String getText(int[] tokenInfo, InputStream source) {
		return "";
	}

}
