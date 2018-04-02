package parser;

import java.util.Stack;

import dataStructures.ConsoleErrorListener;
import dataStructures.ESQFOperatorType;
import dataStructures.ESQFTokentype;
import dataStructures.IBuildableIndexTree;
import dataStructures.IErrorListener;
import dataStructures.ITokenSource;
import dataStructures.IndexTree;
import dataStructures.IndexTreeElement;
import dataStructures.SQFToken;

public class SQFParser {

	protected static int BINARY_INFO_LENGTH = 2;
	protected static int BINARY_INFO_PRECEDENCE = 0;
	protected static int BINARY_INFO_LEVEL = 1;

	/**
	 * The default error listener that will report errors to the console
	 */
	protected static IErrorListener defaultListener = new ConsoleErrorListener();

	/**
	 * An enum describing the state in which the parser currently is in in terms of
	 * its expectation regarding any arguments
	 */
	protected enum EParserArgumentState {
		WAITING, PROVIDING, EXPECTING;
	}

	/**
	 * An enum describing the current context within the parsed code
	 */
	protected enum ECodeContext {
		CODE, INLINE, ARRAY
	}

	/**
	 * A class holding information about the current parser-state
	 */
	protected class ParserState {
		/**
		 * The current parser-argument-state
		 */
		public EParserArgumentState argumentState;
		/**
		 * A stack containing all currently pending binary operators. That are all
		 * binary operators involved in the current expression in a way so that a
		 * following operator has to check against them.
		 * 
		 * The stored array has a length of {@link #BINARY_INFO_LENGTH} and will contain
		 * the precedence of the respective operator at index
		 * {@link #BINARY_INFO_PRECEDENCE} and the level of that operator in the
		 * {@link #tree()} at index {@link #BINARY_INFO_LEVEL}
		 */
		public Stack<int[]> pendingBinaryOperators;
		/**
		 * A stack containing all active levels. An active level is a level in a
		 * {@linkplain IBuildableIndexTree} that is still expecting a child node (that
		 * means there is still an operator expecting a right argument)
		 */
		public Stack<Integer> pendingLevels;

		/**
		 * The AST representing the given input as correlated by this parser
		 */
		public IBuildableIndexTree tree;
		/**
		 * The current code context
		 */
		public ECodeContext codeContext;
		/**
		 * indicates whether the parser has just processed a macro "call"
		 */
		public boolean justProcessedMacro;

		public ParserState(EParserArgumentState argumentState) {
			this(argumentState, new Stack<>(), new Stack<>(), new IndexTree(), ECodeContext.CODE);
		}

		public ParserState(EParserArgumentState state, Stack<int[]> pendingBinaryOperators,
				Stack<Integer> pendingLevels, IBuildableIndexTree tree, ECodeContext ctx) {
			this.argumentState = state;
			this.pendingBinaryOperators = pendingBinaryOperators;
			this.pendingLevels = pendingLevels;
			this.tree = tree;
			this.codeContext = ctx;
		}

		public void reset() {
			argumentState = EParserArgumentState.WAITING;
			pendingBinaryOperators.clear();
			pendingLevels.clear();
			tree = new IndexTree();
		}
	}

	/**
	 * The parser state this parser is currently in
	 */
	protected ParserState parserState;

	/**
	 * The token source to operate on
	 */
	protected ITokenSource<SQFToken> source;
	/**
	 * The error listener to report to
	 */
	protected IErrorListener errorListener;

	/**
	 * A stack containing all pending brackets
	 */
	protected Stack<SQFToken> bracketStack;
	/**
	 * A stack containing parser-states
	 */
	protected Stack<ParserState> states;
	/**
	 * The index of the currently processed token. Changes to this value will
	 * manipulate the token that will be read next
	 */
	protected int currentTokenIndex;
	/**
	 * The nodes associated with bracket expressions
	 */
	protected Stack<IndexTreeElement> bracketNodes;
	/**
	 * Indicates whether error messages about missing terminators should get
	 * suppressed (because they are being processed elsewhere)
	 */
	protected boolean suppressMissingTerminator;


	public SQFParser() {
		this(defaultListener);
	}

	public SQFParser(IErrorListener errorListener) {
		this.errorListener = errorListener;

		parserState = new ParserState(EParserArgumentState.WAITING, new Stack<>(), new Stack<>(), new IndexTree(),
				ECodeContext.CODE);

		bracketStack = new Stack<>();
		states = new Stack<>();
		bracketNodes = new Stack<>();
	}

	/**
	 * Gets the resulting abstract syntax tree corresponding to the provided token
	 * stream
	 * 
	 * @return The tree or <code>null</code> if {@link #parse()} hasn't been invoked
	 *         yet
	 */
	public IBuildableIndexTree tree() {
		return parserState.tree;
	}

	/**
	 * Parses the given input from the beginning to the end
	 * 
	 * @param source
	 *            The token source to use for parsing
	 */
	public void parse(ITokenSource<SQFToken> source) {
		parse(source, 0, null);
	}

	/**
	 * Starts parsing the respective content. The result of the parsing is reflected
	 * in {@link #tree()}.
	 * 
	 * @param source
	 *            The token source to use for parsing
	 * @param start
	 *            The token index to start parsing at. If the input should be
	 *            considered from the start then this should be zero
	 * @param stopAt
	 *            The token type to stop parsing at. The parser will proceed until
	 *            it sees a token of the given type. This token will be processed
	 *            but the parser will stop parsing right after that token. This
	 *            field may be null.
	 * @return The index of the last processed token
	 */
	public int parse(ITokenSource<SQFToken> source, int start, ESQFTokentype stopAt) {
		reset();
		this.source = source;

		// iterate through all tokens
		int size = source.size();

		for (currentTokenIndex = start; currentTokenIndex < size; currentTokenIndex++) {
			SQFToken currentToken = source.get(currentTokenIndex);

			switch (currentToken.operatorType()) {
			case BINARY:
				binary(currentToken);
				break;
			case UNARY:
				unary();
				break;
			case NULAR:
				nular();
				break;
			case MACRO:
				macro(currentToken);
				break;
			case OTHER:
				other(currentToken);
				break;
			}

			if (currentToken.type() == stopAt) {
				return currentTokenIndex;
			}
			if (currentToken.operatorType() == ESQFOperatorType.MACRO) {
				parserState.justProcessedMacro = true;
			} else {
				// check what kind of token it is
				switch (currentToken.type()) {
				case WHITESPACE:
					// Macros might be followed by WS
					break;
				default:
					parserState.justProcessedMacro = false;
					break;
				}
			}
		}

		if (!bracketStack.isEmpty()) {
			// merge all sub-states into the current one
			for (SQFToken unclosedBracket : bracketStack) {
				unbalancedBracket(true, unclosedBracket);
				// "close" all pending brackets
				closeBracket(false);
			}
		}

		if (!states.isEmpty()) {
			throw new IllegalStateException("There are remaining unprocessed states left!");
		}

		return size - 1;
	}

	/**
	 * Matches "non-normal" constructs (neither nular, unary nor binary)
	 * 
	 * @param token
	 *            The token corresponding to said construct
	 */
	protected void other(SQFToken token) {
		switch (token.type()) {
		case WHITESPACE:
		case PREPROCESSOR:
		case COMMENT:
			// ignore
			break;
		case SQUARE_BRACKET_OPEN:
		case CURLY_BRACKET_OPEN:
			createBracketSubNode(token);
			parserState.codeContext = (token.type() == ESQFTokentype.CURLY_BRACKET_OPEN) ? ECodeContext.INLINE
					: ECodeContext.ARRAY;
		case PARENTHESIS_OPEN:
			states.push(parserState);
			parserState = new ParserState(EParserArgumentState.WAITING);

			// remember bracket
			bracketStack.push(token);
			break;
		case PARENTHESIS_CLOSE:
			if (!bracketStack.isEmpty() && bracketStack.peek().type() == ESQFTokentype.PARENTHESIS_OPEN) {
				bracketStack.pop();
				closeBracket(false);
			} else {
				unbalancedBracket(false, token);
			}
			break;
		case CURLY_BRACKET_CLOSE:
			if (!bracketStack.isEmpty() && bracketStack.peek().type() == ESQFTokentype.CURLY_BRACKET_OPEN) {
				bracketStack.pop();
				closeBracket(true);
			} else {
				unbalancedBracket(false, token);
			}
			break;
		case SQUARE_BRACKET_CLOSE:
			if (!bracketStack.isEmpty() && bracketStack.peek().type() == ESQFTokentype.SQUARE_BRACKET_OPEN) {
				bracketStack.pop();
				closeBracket(true);
			} else {
				unbalancedBracket(false, token);
			}
			break;

		case COMMA:
			if (getEnclosingContext() != ECodeContext.ARRAY) {
				errorListener.error("',' outside of array context!", token);
			}
		case SEMICOLON:
			if (getEnclosingContext() == ECodeContext.ARRAY && token.type() == ESQFTokentype.SEMICOLON) {
				errorListener.error("';' instead of ',' in array context!", token);
			}
			// end of statement
			endStatement(token);
			break;

		case SUBSTRING:
			break;
		case SUBSTRING_END:
			break;
		case ERROR_TOKEN:
			// ignore error tokens
			break;
		case MACRO:
		case ID:
		case NUMBER:
		case OPERATOR:
		case STRING:
			throw new IllegalStateException("Token type " + token.type() + " must not occur here!");
		}
	}

	/**
	 * Closes the currently active bracket expression
	 * 
	 * @param addCurrentToken
	 *            Indicates if the current token (the closing bracket) should be
	 *            added to the tree. In order to do this there has to be an empty
	 *            "bracket node" to add to ({@link #bracketNodes} may not be empty)
	 */
	protected void closeBracket(boolean addCurrentToken) {
		ParserState subState = parserState;
		parserState = states.pop();

		if (addCurrentToken) {
			subState.tree.newBranch(currentTokenIndex);
		}

		addSubTree(subState.tree, addCurrentToken);
		// clear bracket context
		parserState.codeContext = ECodeContext.CODE;
		if (addCurrentToken) {
			bracketNodes.pop();
		}
	}

	protected void createBracketSubNode(SQFToken token) {
		IndexTreeElement subNode;
		if (parserState.pendingLevels.isEmpty()) {
			subNode = parserState.tree.newEmptyBranch();
		} else {
			subNode = parserState.tree.addEmpty(parserState.pendingLevels.pop());
		}

		parserState.tree.add(currentTokenIndex);
		bracketNodes.push(subNode);
		// Add the empty node as the pending level
		parserState.pendingLevels.push(subNode.getLevel());
	}

	protected void endStatement(SQFToken token) {
		switch (parserState.argumentState) {
		case EXPECTING:
			errorListener.error("Missing argument to the right", token);
		case PROVIDING:
		case WAITING:
			// add ending token as separate branch to the tree
			startBranch(currentTokenIndex);

			endBranch();
			break;
		}
	}

	protected int nular() {
		switch (parserState.argumentState) {
		case EXPECTING:
			// group this token as child of expecting block
			int level = parserState.tree.add(parserState.pendingLevels.pop(), currentTokenIndex);
			// switch state to providing
			parserState.argumentState = EParserArgumentState.PROVIDING;

			return level;
		case PROVIDING:
			// error -> missing terminator
			missingTerminator(source.get(currentTokenIndex - 1));
		case WAITING:
			endBranch();
			// set parser state to providing
			parserState.argumentState = EParserArgumentState.PROVIDING;
			// set this token as the current providing block
			startBranch(currentTokenIndex);

			return 0;
		default:
			throw new IllegalStateException("Unexpected program flow!");
		}
	}

	protected void unary() {
		switch (parserState.argumentState) {
		case EXPECTING:
			// group this token as child of currently expecting block
			int level = parserState.tree.add(parserState.pendingLevels.pop(), currentTokenIndex);
			// set this token as the current expecting block
			parserState.pendingLevels.push(level);
			break;
		case PROVIDING:
			// error -> missing terminator
			missingTerminator(source.get(currentTokenIndex - 1));
		case WAITING:
			endBranch();
			// set parser state to expecting
			parserState.argumentState = EParserArgumentState.EXPECTING;
			// set this as the current expecting block
			level = startBranch(currentTokenIndex);
			parserState.pendingLevels.push(level);
			break;
		}
	}

	protected void binary(SQFToken token) {
		switch (parserState.argumentState) {
		case EXPECTING:
			// this binary operator is used as a unary one
			// group this token as the child of the current expecting one
			int level = parserState.tree.add(parserState.pendingLevels.pop(), currentTokenIndex);
			// set this as the current expecting block
			parserState.pendingLevels.push(level);
			break;
		case PROVIDING:
			// check if providing block is binary and of lower precedence
			if (!parserState.pendingBinaryOperators.isEmpty() && checkBinaryPrecedence(token.precendence())) {
				level = parserState.tree.insert(parserState.pendingBinaryOperators.pop()[BINARY_INFO_LEVEL],
						currentTokenIndex);

				// set parser state to expecting
				parserState.argumentState = EParserArgumentState.EXPECTING;
				// set this as current expecting block
				parserState.pendingLevels.push(level);
				// add this as a pending binary operator
				parserState.pendingBinaryOperators.push(new int[] { token.precendence(), level });
			} else {
				// group providing block as first child of this token
				parserState.tree.makeTopElement(currentTokenIndex);
				// set parser state to expecting
				parserState.argumentState = EParserArgumentState.EXPECTING;
				// set this as the current expecting block
				parserState.pendingLevels.push(0);
				// add this as a pending binary operator
				parserState.pendingBinaryOperators.push(new int[] { token.precendence(), 0 });
			}
			break;
		case WAITING:
			endBranch();
			// this binary operator is used as a unary one
			// create new branch with this token
			level = startBranch(currentTokenIndex);
			parserState.pendingLevels.push(level);
			// set parser state to expecting
			parserState.argumentState = EParserArgumentState.EXPECTING;
			break;
		}
	}

	/**
	 * Checks all pending binary operators for their precedence in order to
	 * determine if an operator with the given precedence will have to reorder the
	 * tree by overtaking the right parameter of such an pending operator. For that
	 * {@link #pendingBinaryOperators} will be iterated and all operators that have
	 * higher or equal precedence are being removed from it.
	 * 
	 * If there is a pending operator of lower precedence, it will be the first
	 * element in {@link #pendingBinaryOperators} after this function has returned.
	 * Otherwise {@link #pendingBinaryOperators} will be empty
	 * 
	 * @param precedence
	 *            The precedence to check against
	 * @return Whether a pending operator with lower precedence has been found
	 */
	private boolean checkBinaryPrecedence(int precedence) {
		int i = parserState.pendingBinaryOperators.size() - 1;
		while (i >= 0) {
			int[] current = parserState.pendingBinaryOperators.get(i--);

			if (current[BINARY_INFO_PRECEDENCE] > precedence) {
				// the new operator has higher precedence
				// but pending operator back on stack
				parserState.pendingBinaryOperators.push(current);
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets called whenever a new branch is being completed. This method cleans up
	 * all caching that is done for a branch
	 */
	protected void endBranch() {
		parserState.pendingBinaryOperators.clear();
		parserState.pendingLevels.clear();
		parserState.argumentState = EParserArgumentState.WAITING;
	}

	protected int startBranch(int index) {
		if (parserState.codeContext == ECodeContext.CODE) {
			parserState.tree.newBranch(index);
			return 0;
		} else {
			// it's either inside an inline code or an array -> add to bracket-node
			return bracketNodes.peek().add(index);
		}
	}



	/**
	 * Adds the given sub-tree as a nular node to the tree of this parser
	 * 
	 * @param subTree
	 *            The sub-tree to add
	 * @param addToBracketNode
	 *            Indicates whether the given sub-tree should be added to a
	 *            (beforehand created) empty "bracket-node"
	 */
	protected void addSubTree(IBuildableIndexTree subTree, boolean addToBracketNode) {
		switch (parserState.argumentState) {
		case EXPECTING:
			// group this token as child of expecting block
			parserState.tree.add(parserState.pendingLevels.pop(), subTree);
			// switch state to providing
			parserState.argumentState = EParserArgumentState.PROVIDING;
			break;
		case PROVIDING:
			// error -> missing ';'
			missingTerminator(source.get(currentTokenIndex - 1));
		case WAITING:
			endBranch();
			// set parser state to providing
			parserState.argumentState = EParserArgumentState.PROVIDING;

			if (!addToBracketNode) {
				// Indices greater than zero indicate a non-empty node
				parserState.tree.merge(subTree);
			} else {
				// add to empty node
				parserState.tree.add(bracketNodes.peek().getLevel(), subTree);
			}
			break;
		}
	}

	/**
	 * Notifies the error listener about a missing terminator. Depending on the
	 * content this might be a semicolon or a comma
	 * 
	 * @param The
	 *            token after which the terminator was expected
	 */
	protected void missingTerminator(SQFToken token) {
		if (suppressMissingTerminator) {
			return;
		}

		if (parserState.justProcessedMacro) {
			// terminator could be in macro -> ignore
			return;
		}
		errorListener.error("Missing " + (getEnclosingContext() == ECodeContext.ARRAY ? "','" : "';'"), token);
	}

	protected void unbalancedBracket(boolean isOpening, SQFToken unbalanced) {
		errorListener.error((isOpening ? "Unclosed opening bracket!" : "Unopened closing bracket!"), unbalanced);
	}

	/**
	 * Gets the code context of the enclosing parser state (the next one in
	 * {@link #states}). If {@link #states} is empty then {@link ECodeContext#CODE}
	 * is assumed as a default
	 */
	protected ECodeContext getEnclosingContext() {
		if (states.isEmpty()) {
			return ECodeContext.CODE;
		} else {
			return states.peek().codeContext;
		}
	}

	/**
	 * Matches a macro-usage (potentially with arguments)
	 */
	protected void macro(SQFToken token) {
		int size = source.size();

		// determine whether the macro has arguments
		if (size - 1 == currentTokenIndex
				|| source.get(currentTokenIndex + 1).type() != ESQFTokentype.PARENTHESIS_OPEN) {
			// no arguments for the macro -> match macro as a nular expression
			nular();
		} else {
			// The macro has arguments -> binary structure
			binary(token);

			currentTokenIndex++;

			SQFToken currentToken = source.get(currentTokenIndex);
			int openedParenthesis = 0;
			int level = -1;

			// the arguments are following in a parenthesis-structure
			do {
				switch (currentToken.type()) {
				case PARENTHESIS_CLOSE:
					if (!bracketStack.isEmpty() && bracketStack.peek().type() == ESQFTokentype.PARENTHESIS_OPEN) {
						bracketStack.pop();
					} else {
						unbalancedBracket(false, token);
					}
					openedParenthesis--;
					break;

				case PARENTHESIS_OPEN:
					bracketStack.push(currentToken);
					openedParenthesis++;
					break;

				case SQUARE_BRACKET_OPEN:
				case SQUARE_BRACKET_CLOSE:
					errorListener.error("Array constructs not allowed as macro arguments!", currentToken);
					break;

				default:
					// consume anything
					break;
				}

				// add token to tree
				if (level < 0) {
					level = parserState.tree.add(currentTokenIndex) - 1;
				} else {
					parserState.tree.add(level, currentTokenIndex);
				}

				if (openedParenthesis > 0) {
					currentTokenIndex++;
					if (currentTokenIndex < size) {
						currentToken = source.get(currentTokenIndex);
					}
				}
			} while (currentTokenIndex < size && openedParenthesis > 0);

			// set parser state to providing
			parserState.argumentState = EParserArgumentState.PROVIDING;
		}
	}

	/**
	 * Resets this parser so that it can start parsing again
	 */
	public void reset() {
		parserState = new ParserState(EParserArgumentState.WAITING);
		bracketStack.clear();
		bracketNodes.clear();

		currentTokenIndex = -1;
	}

	/**
	 * Resets the error listener for this parser
	 */
	public void resetErrorListener() {
		errorListener = defaultListener;
	}

	/**
	 * Sets the error listener for this parser
	 * 
	 * @param listener
	 *            The new listener to report errors to
	 */
	public void setErrorListener(IErrorListener listener) {
		assert (listener != null);
		errorListener = listener;
	}

	/**
	 * Whether error messages for missing terminators (';' or ',' in array context)
	 * should get suppressed
	 * 
	 * @param suppress
	 *            Whether to suppress the messages
	 */
	public void suppressMissingTerminatorErrorMessages(boolean suppress) {
		suppressMissingTerminator = suppress;
	}

	/**
	 * Indicates whether error messages for missing terminators (';' or ',' in array
	 * context) are being suppressed.
	 */
	public boolean isSuppressingMissingTerminatorErrorMessages() {
		return suppressMissingTerminator;
	}
}
