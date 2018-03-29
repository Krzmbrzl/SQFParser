package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import abego.swt.INode;
import dataStructures.CharacterInputStream;
import dataStructures.IBuildableIndexTree;
import dataStructures.IErrorListener;
import dataStructures.IndexTree;
import dataStructures.IndexTreeElement;
import dataStructures.SQFTestTokenFactory;
import dataStructures.SQFToken;
import dataStructures.TokenBuffer;
import lexer.SQFLexer;
import parser.SQFParser;
import ui.TreeDisplayer;

class ParserTest {
	public static final String DIR = System.getProperty("user.home") + "/Documents/Eclipse-Workspace"
			+ "/SQFParser/src/tests/";

	static IBuildableIndexTree compareTree;
	static SQFLexer lexer;
	static SQFParser parser;


	@BeforeAll
	public static void setUp() {
		compareTree = new IndexTree();
		lexer = new SQFLexer();
		lexer.setTokenFactory(new SQFTestTokenFactory());
		parser = new SQFParser(new IErrorListener() {

			@Override
			public void error(String msg, SQFToken token) {
				fail("Expected parsing to complete without errors but got one: \"" + msg + "\" - " + token + "\n");
			}
		});
	}

	@Test
	public void arithmeticTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2+4".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 2)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");


		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2+4*7".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 3(2 4))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2+4*7^9".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 3(2 5(4 6)))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2+4*7^9/4".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 7(3(2 5(4 6))8))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2+4*7^9/4-2".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":9(1(0 7(3(2 5(4 6))8)) 10)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("4^3*2-4/7".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":5(3(1(0 2)4)7(6 8))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2+-3".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 2(3))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		// displayTree(parser.tree(), lexer.getTokens());
	}

	@Test
	public void parenthesisTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("(2+4)*7".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":5(2(1 3)6)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("7*(2+4)".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 4(3 5))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("7*(2+4)^3".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 7(4(3 5) 8))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("7*(2+4/(3-14))".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 4(3 6(5 9(8 10))))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		// displayTree(parser.tree(), lexer.getTokens());
	}

	@Test
	public void arrayTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("[2]".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 1 2)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("[2+3]".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 2(1 3) 4)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2+[2+3]".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 n(2 4(3 5) 6))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("[2-3*5]+[2+3]".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":7(n(0 2(1 4(3 5)) 6) n(8 10(9 11) 12))");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("[2+[]]".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 2(1 n(3 4)) 5)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("[1,2,3]".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 1 2 3 4 5 6)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("[2+1,2-1*2]".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 2(1 3) 4 6(5 8(7 9))10)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("[[2,7*4],2-1*2]".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 n(1 2 3 5(4 6) 7) 8 10(9 12(11 13)) 14)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		// displayTree(parser.tree(), lexer.getTokens());
	}

	@Test
	public void multipleStatements() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2;3".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":0 :1 :2");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream(";;".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":0 :1");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2+5;3".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 2) :3 :4");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("2+5;3;".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":1(0 2) :3 :4 :5");
		assertEquals(compareTree, parser.tree(), "Trees differ!");
	}

	@Test
	public void inlineCodeTest() throws IOException {
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("{}".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 1)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("{3}".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 1 2)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("{2+3}".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 2(1 3) 4)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("{2+3;}".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 2(1 3) 4 5)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("{2+3;4-5}".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 2(1 3) 4 6(5 7) 8)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("{2+3;4-5;}".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 2(1 3) 4 6(5 7) 8 9)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("{2+3;4-5*2;}".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, "b(0 2(1 3) 4 6(5 8(7 9)) 10 11)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");
	}

	@Test
	public void macroTest() throws IOException {
		HashSet<String> macros = new HashSet<>();
		macros.add("MACRO");

		lexer.setMacros(macros);
		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("MACRO".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":0");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("MACRO()".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":0(1 2)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new ByteArrayInputStream("MACRO(arg) hint \"hi\"".getBytes())));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree, ":0(1 2 3) :5(7)");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.reset(true);
	}

	@Test
	public void sqfSnippetTest() throws IOException {
		lexer.lex(new CharacterInputStream(new FileInputStream(new File(DIR + "SQFSnippet01.sqf"))));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree,
				":3(1 9(5(7) 11(13))) :14 :18(16 20(22)) :23 :28(26 30) :31 :41(33(36(38)) "
						+ "n(43 46(44 48) 49 50)) :51 :58(60(62(64))) :65 :67(69(71(76(74 78)))) :80");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		lexer.lex(new CharacterInputStream(new FileInputStream(new File(DIR + "SQFSnippet02.sqf"))));
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree,
				":31(25(n(2 n(4 6 8) 9 n(11 13 14 16 17 19 21) 23) 27) n(33 35 36 38 39 41 43)) "
						+ ":44 :58(46(52(50 54)) n(60 74(n(62 64 65 67 68 70 72) 80(78 82)) 85 181(n(87 91(89 95(93 97)) "
						+ "98 175(n(100 102 103 105 106 118(112(110 114) 122(124(130(128 132)))) 137 166(160(n(141 n(143 145(147) 149) 150 "
						+ "n(152 154 156) 158) 162) 168) 169 171 173) 177) 179) 183) 184 186)) :187");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		HashSet<String> macros = new HashSet<>();
		macros.add("NOTIFICATION_LOG");
		macros.add("GVAR");
		macros.add("CHECK_TRUE");

		lexer.setMacros(macros);
		CharacterInputStream in = new CharacterInputStream(new FileInputStream(new File(DIR + "SQFSnippet03.sqf")));
		lexer.lex(in);
		parser.parse(lexer);
		IBuildableIndexTree.populateFromString(compareTree,
				":7(3(5) 9(n(11 n(13 14 15 17 18 n(20 21 22) 23) 25))) :26 :28(29 30 31 32 33 34 35 36 37 38 39 40 41) :47(43(45) 55(49(52) n(57 62(59(61) "
						+ "n(64 65(66 67 68) 69)) 70 75(72(74) n(77 78(79 80 81) 82)) 83 88(85(87) "
						+ "n(90 91(92 93 94) 95)) 96 98(n(100 101(102 103 104 105 106 107 108 109 110 111) 112)) 113 115))) :116 :118");
		assertEquals(compareTree, parser.tree(), "Trees differ!");

		 displayTree(parser.tree(), lexer.getTokens());
		lexer.reset(true);
	}


	@SuppressWarnings("unused")
	private static void displayTree(IBuildableIndexTree treeInput, TokenBuffer<SQFToken> buffer) {
		Display display = new Display();
		Shell shell = new Shell(display);

		Iterator<IndexTreeElement> branchIt = treeInput.branchIterator();

		INode root = new INode() {

			@Override
			public String getDisplayText() {
				return "";
			}
		};

		new TreeDisplayer<>(shell, SWT.NONE, treeInput, buffer);
		shell.setLayout(new FillLayout());

		shell.open();

		// run the event loop as long as the window is open
		while (!shell.isDisposed()) {
			// read the next OS event queue and transfer it to a SWT event
			if (!display.readAndDispatch()) {
				// if there are currently no other OS event to process
				// sleep until the next OS event is available
				display.sleep();
			}
		}
	}

}
