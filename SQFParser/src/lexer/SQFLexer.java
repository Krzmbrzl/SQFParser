package lexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataStructures.AbstractSQFTokenFactory;
import dataStructures.CharacterInputStream;
import dataStructures.ConsoleErrorListener;
import dataStructures.ESQFTokentype;
import dataStructures.ICharacterBuffer;
import dataStructures.ICharacterInputStream;
import dataStructures.IErrorListener;
import dataStructures.ITokenSource;
import dataStructures.SQFToken;
import dataStructures.TokenBuffer;

public class SQFLexer implements ITokenSource<SQFToken> {
	/**
	 * The default error listener used if no other is provided
	 */
	protected static final IErrorListener defaultListener = new ConsoleErrorListener();

	/**
	 * A list containing the index of each newline character in the stream
	 */
	protected List<Integer> lineStarts;

	/**
	 * The collection holding the lexed tokens for the current input. A token
	 * consists of a type, start and end attribute
	 */
	protected TokenBuffer<SQFToken> tokens;

	/**
	 * The error listener to use
	 */
	protected IErrorListener errorListener;
	/**
	 * The token factory to use when creating tokens
	 */
	protected AbstractSQFTokenFactory factory;

	/**
	 * The set of known macros
	 */
	protected Set<String> macroSet;



	public SQFLexer(IErrorListener listener, HashSet<String> macros) {
		setMacros(macros);
		setErrorListener(listener);

		lineStarts = new ArrayList<>();
	}

	public SQFLexer() {
		this(defaultListener, new HashSet<String>());
	}

	public SQFLexer(IErrorListener listener) {
		this(listener, new HashSet<String>());
	}

	public SQFLexer(HashSet<String> macros) {
		this(defaultListener, macros);
	}

	/**
	 * Lexes the characters provided by the given input. Before it starts lexing
	 * calling this method triggers a reset of this lexer without clearing the set
	 * of known macros
	 * 
	 * @param input
	 *            The character source
	 * @throws IOException
	 */
	public void lex(ICharacterInputStream input) throws IOException {
		reset(false);

		assert (factory != null);
		factory.setBuffer(input.getBuffer());

		int lastOffset = 0;

		while (input.hasNext()) {
			matchWhitespace(input);
			matchPreprocessor(input, false);
			if (!matchComment(input)) {
				// make sure that everything that can be matched as a comment, WS or
				// Preprocessor is matched first
				matchOperator(input);
				matchString(input);
				matchNumberOrIDOrMacro(input);
				matchBracket(input);
			}

			if (lastOffset == input.getOffset()) {
				// no token has been consumed -> error
				int start = input.getOffset();
				errorListener.error("Recognition error on \'" + (char) input.read() + "\'",
						factory.produce(ESQFTokentype.ERROR_TOKEN, start, input.getOffset()));
			}

			lastOffset = input.getOffset();
		}
	}

	/**
	 * Matches all whitespace character and discards them. Does also populate
	 * {@link #lineStarts} whenever a '\n' character is found
	 * 
	 * @param input
	 *            The InputStream to consume
	 * @throws IOException
	 */
	private void matchWhitespace(ICharacterInputStream input) throws IOException {
		int start = input.getOffset();

		int c = input.read();

		if (!Character.isWhitespace(c)) {
			input.unread();
			return;
		}

		while (Character.isWhitespace(c)) {
			if (c == '\n') {
				lineStarts.add(input.getOffset());
			}

			c = input.read();
		}

		// don't consume last character as it doesn't belong to the WS token
		input.unread();

		if (start != input.getOffset()) {
			// create token
			tokens.add(factory.produce(ESQFTokentype.WHITESPACE, start, input.getOffset()));
		}
	}

	/**
	 * Matches and discards comments (single- and multi-line) while adding all
	 * newlines to {@link #lineStarts}
	 * 
	 * @param input
	 *            The InputStream to consume
	 * @throws IOException
	 */
	private boolean matchComment(ICharacterInputStream input) throws IOException {
		int start = input.getOffset();
		int first = input.read();

		if (first != '/') {
			// input is not a comment
			input.unread();
			return false;
		}

		int second = input.read();

		if (second != '/' && second != '*') {
			// input is not a comment
			input.unread();
			input.unread();
			return false;
		}

		int c = input.read();

		if (second == '/') {
			// single line comment
			while (c != '\n' && c != -1) {
				// consume all characters in the comment until line feed
				c = input.read();
			}
			if (c != -1) {
				lineStarts.add(input.getOffset());
			}
		} else {
			// multiline comment
			while ((c == '*') ? (c = input.read()) != '/' : true && c != -1) {
				// consume all characters in the comment until comment end
				// watch for newlines
				if (c == '\n') {
					lineStarts.add(input.getOffset());
				}
				c = input.read();
			}
		}

		if (c == -1) {
			// don't consume EOF character
			input.unread();
		}

		if (start != input.getOffset()) {
			tokens.add(factory.produce(ESQFTokentype.COMMENT, start, input.getOffset()));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Matches preprocessor statements
	 * 
	 * @param input
	 *            The inputStream to consume
	 * @param bailout
	 *            Whether or not to abort the preprocessor-token-creation if the #
	 *            is followed by whitespace (This is needed if a preprocessor
	 *            statement should be matched inside a String)
	 * @return Whether this function bailed out
	 * @throws IOException
	 */
	private void matchPreprocessor(ICharacterInputStream input, boolean bailout) throws IOException {
		int start = input.getOffset();
		int c = input.read();

		if (c != '#') {
			// it's not the beginning of a preprocessor statement
			input.unread();
			return;
		}

		if (bailout && Character.isWhitespace(input.peek())) {
			// it's not a preprocessor statement and bailout on that is wished -> most
			// likely in a String
			input.unread();

			return;
		}

		boolean escaped = false;
		boolean isIf = false;
		boolean processingIfPart = true;
		boolean foundHashtag = false;

		int counter = 0;

		int[] ifStarter = new int[] { 'i', 'f', 'd', 'e', 'f' };
		int[] ifEnder = new int[] { 'e', 'n', 'd', 'i', 'f' };

		// read until unescaped newline
		c = input.read();
		while ((c != '\n' || escaped || isIf) && c != -1) {
			if (c == '\\') {
				escaped = true;
			} else {
				escaped = false;

				if (c == '\n') {
					lineStarts.add(input.getOffset());
				} else {
					if (!isIf && processingIfPart) {
						if (Character.isWhitespace(c)) {
							if (counter != 0) {
								// whitespace in the middle means there can't be an if(n)def
								processingIfPart = false;
							}
						} else {
							// Treat prep-commands as case-insensitive in the lexer
							c = Character.toLowerCase(c);

							if (c != ifStarter[counter]) {
								// check if it is actually a ifndef
								if (counter != 2 || c != 'n') {
									processingIfPart = false;
								}
								counter--;
							}

							counter++;
						}
					}
				}
			}

			if (processingIfPart && counter == ifStarter.length) {
				isIf = true;
				processingIfPart = false;
				counter = 0; // make counter recycable for end-detection
			}

			if (isIf) {
				// watch out for the #endif
				if (foundHashtag) {
					// Treat prep-commands as case-insensitive in the lexer
					c = Character.toLowerCase(c);

					// search for endif
					if (processingIfPart) {
						if (ifEnder[counter] != c) {
							// false alarm
							processingIfPart = false;
							counter = 0;
							foundHashtag = false;
						} else {
							counter++;
						}

						if (counter == ifEnder.length) {
							// if is finished
							break;
						}
					} else {
						if (c == ifEnder[0]) {
							processingIfPart = true;

							counter++;
						} else {
							// only accept if there is a WS after the #
							if (!Character.isWhitespace(c)) {
								// the found hashtag does not belong to #endif
								foundHashtag = false;
							}
						}
					}
				} else {
					foundHashtag = c == '#';
				}
			}

			c = input.read();
		}

		if (c == '\n') {
			lineStarts.add(input.getOffset());
		}

		if (c == -1) {
			// don't consume EOF character
			input.unread();
		}

		if (start != input.getOffset()) {
			// create token
			tokens.add(factory.produce(ESQFTokentype.PREPROCESSOR, start, input.getOffset()));
		}
	}

	/**
	 * Matches a String enclosed in either double- or single-quotes. "" or '' are
	 * considered escaped quotes and will therefore not trigger an end of the String
	 * matching
	 * 
	 * @param input
	 *            The InputStream to consume
	 * @throws IOException
	 */
	private void matchString(ICharacterInputStream input) throws IOException {
		int start = input.getOffset();
		int starter = input.read();

		if (starter != '"' && starter != '\'') {
			input.unread();
			return;
		}

		boolean producedSubstring = false;

		int c = input.read();

		while (c != -1) {
			if (c == starter) {
				// Look at next character in order to determine whether this quote is escaped
				c = input.read();

				if (c != starter) {
					// unescaped sequence -> break
					if (c != -1) {
						// don*'t unread EOF character as that is done at the end of the function
						input.unread();
					}
					break;
				}
			} else {
				if (c == '\n') {
					lineStarts.add(input.getOffset());

					// check if there is a preprocessor statement within the string
					// first remove leading WS
					while (Character.isWhitespace(c)) {
						c = input.read();
					}

					// Put last read character back because it wasn't WS
					input.unread();

					int subEnd = input.getOffset();

					matchPreprocessor(input, true);

					if (subEnd != input.getOffset()) {
						// create substring token
						// the substring token has to be inserted before the preprocessor token
						tokens.add(tokens.size() - 1, factory.produce(ESQFTokentype.SUBSTRING, start, subEnd));
						start = input.getOffset();
						producedSubstring = true;
					}
				}
			}

			c = input.read();
		}

		if (c == -1) {
			// don't consume EOF character
			input.unread();
		}

		if (start != input.getOffset()) {
			// create token
			tokens.add(factory.produce(producedSubstring ? ESQFTokentype.SUBSTRING_END : ESQFTokentype.STRING, start,
					input.getOffset()));
		}
	}

	/**
	 * Matches a Number or an ID
	 * 
	 * @param input
	 *            The InputStream to consume
	 * @throws IOException
	 */
	private void matchNumberOrIDOrMacro(ICharacterInputStream input) throws IOException {
		int start = input.getOffset();

		int c = input.read();

		if (!Character.isLetterOrDigit(c) && c != '_' && c != '$' && c != '.') {
			input.unread();
			return;
		}

		// assume number at first
		ESQFTokentype type = ESQFTokentype.NUMBER;

		if (!consumeNumber(input, c, false)) {
			// can't be interpreted as a (pure) number

			if (c == '$') {
				// dollar sign can't be part of an ID
				errorListener.error("Unfinished hex-number specification",
						factory.produce(ESQFTokentype.ERROR_TOKEN, input.getOffset() - 1, input.getOffset()));
				input.unread();
				return;
			}

			if (c == '.') {
				// dot sign can't be part of an ID
				// notify error listener about unfinished number
				errorListener.error("Error on '.' - Insert trailing digits to complete Number definition",
						factory.produce(ESQFTokentype.ERROR_TOKEN, input.getOffset() - 1, input.getOffset()));
				input.unread();
				return;
			}

			type = ESQFTokentype.ID;
			String id = (char) c + consumeID(input);

			// check if ID is in fact macro
			if (macroSet.contains(id)) {
				type = ESQFTokentype.MACRO;
			}
		}

		tokens.add(factory.produce(type, start, input.getOffset()));
	}

	/**
	 * Consumes a number. This can either be a normal whole or decimal number, a
	 * decimal number of the form .[0-9]+, a number in scientific notation of for
	 * e(-)Number or a hexadecimal number either starting by 0x or $
	 * 
	 * @param input
	 *            The inputStream to consume
	 * @param startChar
	 *            The starting character of the number to match
	 * @param allowScientific
	 *            Whether the start of a scientific notation is allowed in this
	 *            context. This means that the startChar is 'e' or 'E' and should be
	 *            interpreted as part of the aEb construct used for scientific
	 *            notation
	 * @return Whether this method was able to consume the given input (including
	 *         startChar) as a number
	 * @throws IOException
	 */
	private boolean consumeNumber(ICharacterInputStream input, int startChar, boolean allowScientific)
			throws IOException {
		startChar = Character.toLowerCase(startChar);

		if (!Character.isDigit(startChar) && (startChar != 'e' || !allowScientific) && startChar != '$'
				&& startChar != '.') {
			// it can't be a number
			return false;
		}

		switch (startChar) {
		case 'e':
			// can only be followed by an integer (prepended by optional sign)
			int c = input.read();

			if (c == '+' || c == '-') {
				c = input.read();
			}

			if (!Character.isDigit(c)) {
				// notify error listener about missing digit
				errorListener.error("Error on '" + (char) c + "' - Digits expected to complete scientific notation",
						factory.produce(ESQFTokentype.ERROR_TOKEN, input.getOffset() - 1, input.getOffset()));
				// consume as number anyway
				return true;
			}

			// consume integer-exponent
			do {
				c = input.read();
			} while (Character.isDigit(c));

			// unread last character because it is no longer part of the number
			input.unread();
			return true;
		case '$':
			return consumeHexNumberBody(input);
		case '.':
			c = input.read();
			if (!Character.isDigit(c)) {
				input.unread();
				return false;
			}
		default:
			c = input.read();
			if (Character.isDigit(c)) {
				boolean matchedPeriod = false;

				while (Character.isDigit(c) || c == '.') {
					c = input.read();

					if (c == '.') {
						if (matchedPeriod) {
							// matched second period in one number -> error (but consume period)
							// notify error listener about too many periods
							errorListener.error("Error on '.' - Only one period per number allowed", factory
									.produce(ESQFTokentype.ERROR_TOKEN, input.getOffset() - 1, input.getOffset()));
							// returning true because at this point a digit has already been consumed -> is
							// definitely a number
							return true;
						} else {
							matchedPeriod = true;
						}
					}
				}

				if (Character.toLowerCase(c) == 'e') {
					if (!consumeNumber(input, c, allowScientific)) {
						input.unread();
					}
				} else {
					// unread last character as it is no longer a digit
					input.unread();
				}

				return true;
			} else {
				if (startChar == '0' && Character.toLowerCase(c) == 'x') {
					return consumeHexNumberBody(input);
				} else {
					if (!consumeNumber(input, c, true)) {
						input.unread();
						return true;
					}

					return true;
				}
			}
		}
	}

	/**
	 * Consumes the body of a hexadecimal number specification. That means this
	 * function matches characters 0-9 and a-f (case-insensitive)
	 * 
	 * @param input
	 *            The InputStream to consume
	 * @return Whether this function has successfully consumed a hex-number-body
	 * @throws IOException
	 */
	private boolean consumeHexNumberBody(ICharacterInputStream input) throws IOException {
		int c = Character.toLowerCase(input.read());

		if (Character.isDigit(c) || (c >= 'a' && c <= 'f')) {
			while (Character.isDigit(c) || (c >= 'a' && c <= 'f')) {
				c = Character.toLowerCase(input.read());
			}

			// unread last character as it is no longer part of the hex-number-body
			input.unread();
			return true;
		} else {
			// not a hex-number-body
			input.unread();
			return false;
		}
	}

	/**
	 * Consumes the next characters as an ID. An ID either consists of letters,
	 * digits or an underscore
	 * 
	 * @param input
	 *            The InputStream to consume
	 * @throws IOException
	 */
	private String consumeID(ICharacterInputStream input) throws IOException {
		int c = input.read();

		StringBuilder builder = new StringBuilder();
		builder.append((char) c);

		while (Character.isLetterOrDigit(c) || c == '_') {
			c = input.read();
			builder.append((char) c);
		}

		// Last read character is not part of the ID
		input.unread();
		builder.setLength(builder.length() - 1);

		return builder.toString();
	}

	/**
	 * Matches all known operators that consist of non-alphanumeric symbols. In most
	 * cases this function will consume one character but at maximum it will consume
	 * two.
	 * 
	 * @param input
	 *            The InputStream to consume
	 * @throws IOException
	 */
	private void matchOperator(ICharacterInputStream input) throws IOException {
		int start = input.getOffset();

		int c = input.read();
		ESQFTokentype type = ESQFTokentype.OPERATOR;

		switch (c) {
		case '+':
			break;
		case '-':
			break;
		case '*':
			break;
		case '/':
			break;
		case '^':
			break;
		case '>':
			c = input.read();
			if (c == '>') {
				// operator can't have a trailing '='
				break;
			} else {
				input.unread();
			}
		case '<':
		case '=':
		case '!':
			c = input.read();
			if (c != '=') {
				input.unread();
			}
			break;
		case '%':
			break;
		case '&':
			c = input.read();
			if (c != '&') {
				// notify error listener about missing &
				errorListener.error("Missing second '&'",
						factory.produce(ESQFTokentype.ERROR_TOKEN, input.getOffset() - 1, input.getOffset()));

				// consume as && anyway -> add imaginary second & and unread current character
				// as it is not part of this token
				input.unread();
			}
			break;
		case '|':
			c = input.read();
			if (c != '|') {
				// notify error listener about missing |
				errorListener.error("Missing second '|'",
						factory.produce(ESQFTokentype.ERROR_TOKEN, input.getOffset() - 1, input.getOffset()));

				// consume as || anyway -> add imaginary second | and unread current character
				// as it is not part of this token
				input.unread();
			}
			break;
		case ';':
			type = ESQFTokentype.SEMICOLON;
			break;
		case ':':
			break;
		case ',':
			type = ESQFTokentype.COMMA;
			break;
		default:
			input.unread();
			return;
		}

		tokens.add(factory.produce(type, start, input.getOffset()));
	}

	/**
	 * Matches any kind of bracket
	 * 
	 * @param input
	 *            The InputStream to consume
	 * @throws IOException
	 */
	private void matchBracket(ICharacterInputStream input) throws IOException {
		int start = input.getOffset();

		int c = input.read();

		switch (c) {
		case '(':
			tokens.add(factory.produce(ESQFTokentype.PARENTHESIS_OPEN, start, input.getOffset()));
			break;
		case ')':
			tokens.add(factory.produce(ESQFTokentype.PARENTHESIS_CLOSE, start, input.getOffset()));
			break;
		case '[':
			tokens.add(factory.produce(ESQFTokentype.SQUARE_BRACKET_OPEN, start, input.getOffset()));
			break;
		case ']':
			tokens.add(factory.produce(ESQFTokentype.SQUARE_BRACKET_CLOSE, start, input.getOffset()));
			break;
		case '{':
			tokens.add(factory.produce(ESQFTokentype.CURLY_BRACKET_OPEN, start, input.getOffset()));
			break;
		case '}':
			tokens.add(factory.produce(ESQFTokentype.CURLY_BRACKET_CLOSE, start, input.getOffset()));
			break;
		default:
			input.unread();
		}
	}

	public TokenBuffer<SQFToken> getTokens() {
		return tokens;
	}

	public List<Integer> getNewlineIndices() {
		return lineStarts;
	}

	public Integer[] getNewlineIndicesAsArray() {
		return lineStarts.toArray(new Integer[lineStarts.size()]);
	}

	@Override
	public SQFToken get(int index) {
		return tokens.get(index);
	}

	@Override
	public int size() {
		return tokens.size();
	}

	/**
	 * Sets the set of known macros
	 * 
	 * @param macros
	 *            The macros to recognize as such
	 */
	public void setMacros(Set<String> macros) {
		assert (macros != null);

		macroSet = macros;
	}

	/**
	 * Resets this lexer so that it can be used to start lexing a new input
	 * 
	 * @param clearMacros
	 *            Whether the set of known macros should get cleared as well
	 */
	public void reset(boolean clearMacros) {
		lineStarts.clear();
		lineStarts.add(0); // first line starts right at the beginning
		tokens = new TokenBuffer<>();
		if (clearMacros) {
			macroSet.clear();
		}
	}

	/**
	 * Resets the error listener to the default one (reports errors to the console)
	 */
	public void resetListener() {
		setErrorListener(defaultListener);
	}

	/**
	 * Sets the token factory that should be used in order to produce the tokens.
	 * The {@linkplain ICharacterBuffer} for the factory will be set accordingly
	 * when invoking {@link #lex(CharacterInputStream)}.
	 * 
	 * @param factory
	 *            The {@linkplain AbstractSQFTokenFactory} to use
	 */
	public void setTokenFactory(AbstractSQFTokenFactory factory) {
		assert (factory != null);

		this.factory = factory;
	}

	/**
	 * Sets the error listener for this lexer
	 * 
	 * @param listener
	 *            The listener to report any errors to
	 */
	public void setErrorListener(IErrorListener listener) {
		assert (listener != null);

		errorListener = listener;
	}
}
