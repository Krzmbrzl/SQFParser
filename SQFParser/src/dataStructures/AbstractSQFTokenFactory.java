package dataStructures;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSQFTokenFactory {

	/**
	 * The default entry which is nular
	 */
	protected static LookupTableEntry DEFAULT = new LookupTableEntry(0, ESQFOperatorType.NULAR);
	/**
	 * Alias for {@link #DEFAULT}
	 */
	protected static LookupTableEntry NULAR = DEFAULT;
	/**
	 * The default unary lookup entry
	 */
	protected static LookupTableEntry UNARY = new LookupTableEntry(0, ESQFOperatorType.UNARY);
	/**
	 * The default binary lookup entry. It is for "ordinary" binary operators that
	 * are not listed in {@link #specialOperators}
	 */
	protected static LookupTableEntry BINARY = new LookupTableEntry(5, ESQFOperatorType.BINARY);
	/**
	 * The default entry for macros
	 */
	protected static LookupTableEntry MACRO = new LookupTableEntry(0, ESQFOperatorType.MACRO);
	/**
	 * The entry for the "other" type
	 */
	protected static LookupTableEntry OTHER = new LookupTableEntry(0, ESQFOperatorType.OTHER);

	/**
	 * A map of operators with "special" precedence
	 */
	protected static Map<String, LookupTableEntry> specialOperators;

	private boolean initialized;


	protected static class LookupTableEntry {
		int precedence;
		ESQFOperatorType operatorType;

		public LookupTableEntry(int precedence, ESQFOperatorType operatorType) {
			this.precedence = precedence;
			this.operatorType = operatorType;
		}

		public int getPrecedence() {
			return precedence;
		}

		public ESQFOperatorType getOperatorType() {
			return operatorType;
		}
	}

	/**
	 * The table that contains all operator and their respective precedence and
	 * operator type
	 */
	protected HashMap<String, LookupTableEntry> lookupTable;

	/**
	 * The default character buffer
	 */
	protected ICharacterBuffer buffer;


	public AbstractSQFTokenFactory(CharacterBuffer buffer) {
		this();
		this.buffer = buffer;
	}

	public AbstractSQFTokenFactory() {
		lookupTable = new HashMap<>();

		if (specialOperators == null) {
			setUpSpecialOperators();
		}
	}


	/**
	 * Produces a token and sets its precedence and operator type as determined by
	 * {@link #lookupTable}
	 * 
	 * @param type
	 *            The token type
	 * @param start
	 *            The start index of the token (inclusive)
	 * @param end
	 *            The end index of the token (exclusive)
	 * @param buffer
	 *            The characterBuffer corresponding to this token
	 * @return The created token
	 */
	public SQFToken produce(ESQFTokentype type, int start, int end, ICharacterBuffer buffer) {
		if (!initialized) {
			initialize();
		}
		LookupTableEntry entry = type == ESQFTokentype.MACRO ? null : lookupTable.get(buffer.getText(start, end - start).toLowerCase());

		if (entry == null) {
			switch (type) {
			case COMMENT:
			case CURLY_BRACKET_CLOSE:
			case CURLY_BRACKET_OPEN:
			case ERROR_TOKEN:
			case PARENTHESIS_CLOSE:
			case PARENTHESIS_OPEN:
			case PREPROCESSOR:
			case SEMICOLON:
			case SQUARE_BRACKET_CLOSE:
			case SQUARE_BRACKET_OPEN:
			case SUBSTRING:
			case SUBSTRING_END:
			case WHITESPACE:
			case COMMA:
				entry = OTHER;
				break;
			case MACRO:
				entry = MACRO;
				break;
			default:
				entry = DEFAULT;
				break;
			}
		}

		return new SQFToken(type, start, end, entry.getPrecedence(), entry.getOperatorType(), buffer);
	}

	/**
	 * Produces a token and sets its precedence and operator type as determined by
	 * {@link #lookupTable}. This method assumes that {@link #buffer} has been set
	 * by using the respective constructor
	 * 
	 * @param type
	 *            The token type
	 * @param start
	 *            The start index of the token (inclusive)
	 * @param end
	 *            The end index of the token (exclusive)
	 * @return The created token
	 */
	public SQFToken produce(ESQFTokentype type, int start, int end) {
		return produce(type, start, end, buffer);
	}

	/**
	 * Sets the {@linkplain ICharacterBuffer} that should be associated with the
	 * created tokens
	 * 
	 * @param buffer
	 *            The buffer to associate the tokens with
	 */
	public void setBuffer(ICharacterBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * Fills {@link #specialOperators} with content (all keys are in lowercase)
	 */
	protected void setUpSpecialOperators() {
		specialOperators = new HashMap<String, LookupTableEntry>();

		// assignment
		specialOperators.put("=", new LookupTableEntry(10, ESQFOperatorType.BINARY));
		// logic
		specialOperators.put("or", new LookupTableEntry(8, ESQFOperatorType.BINARY));
		specialOperators.put("||", new LookupTableEntry(8, ESQFOperatorType.BINARY));
		specialOperators.put("and", new LookupTableEntry(7, ESQFOperatorType.BINARY));
		specialOperators.put("&&", new LookupTableEntry(7, ESQFOperatorType.BINARY));
		// comparison
		specialOperators.put("!=", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		specialOperators.put("<", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		specialOperators.put(">", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		specialOperators.put("<=", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		specialOperators.put(">=", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		specialOperators.put("==", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		specialOperators.put(">>", new LookupTableEntry(6, ESQFOperatorType.BINARY));
		// else
		specialOperators.put("else", new LookupTableEntry(4, ESQFOperatorType.BINARY));
		// binary math operators
		specialOperators.put("+", new LookupTableEntry(3, ESQFOperatorType.BINARY));
		specialOperators.put("-", new LookupTableEntry(3, ESQFOperatorType.BINARY));
		specialOperators.put("min", new LookupTableEntry(3, ESQFOperatorType.BINARY));
		specialOperators.put("max", new LookupTableEntry(3, ESQFOperatorType.BINARY));
		specialOperators.put("*", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		specialOperators.put("/", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		specialOperators.put("%", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		specialOperators.put("mod", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		specialOperators.put("atan2", new LookupTableEntry(2, ESQFOperatorType.BINARY));
		specialOperators.put("^", new LookupTableEntry(1, ESQFOperatorType.BINARY));
	}

	/**
	 * Checks whether the given operator has a "special" precedence and thus has to
	 * use the {@linkplain LookupTableEntry} as mapped in {@link #specialOperators}
	 * 
	 * @param operator
	 *            The operator-name to check (has to be lowercase)
	 */
	protected boolean hasSpecialPrecedence(String operator) {
		return specialOperators.containsKey(operator);
	}

	/**
	 * Initializes this factory by populating the {@link #lookupTable} with the
	 * respective entries. The respective operator names need to be <b>lowercase</b>
	 * as that's how it will be searched for them
	 */
	protected abstract void doInitialize();

	/**
	 * Initializes this factory
	 */
	public void initialize() {
		doInitialize();

		initialized = true;
	}

}
