package dataStructures;

import java.util.HashMap;

public abstract class AbstractSQFTokenFactory {

	/**
	 * The default entry
	 */
	protected static LookupTableEntry DEFAULT = new LookupTableEntry(0, ESQFOperatorType.NULAR);
	/**
	 * The default entry for macros
	 */
	protected static LookupTableEntry MACRO = new LookupTableEntry(0, ESQFOperatorType.MACRO);
	/**
	 * The entry for the "other" type
	 */
	protected static LookupTableEntry OTHER = new LookupTableEntry(0, ESQFOperatorType.OTHER);

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
	protected CharacterBuffer buffer;


	public AbstractSQFTokenFactory(CharacterBuffer buffer) {
		this();
		this.buffer = buffer;
	}

	public AbstractSQFTokenFactory() {
		lookupTable = new HashMap<>();

		initialize();
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
	public SQFToken produce(ESQFTokentype type, int start, int end, CharacterBuffer buffer) {
		LookupTableEntry entry = lookupTable.get(buffer.getText(start, end - start).toLowerCase());

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
	 * Initializes this factory by populating the {@link #lookupTable} with the
	 * respective entries. The respective operator names need to be <b>lowercase</b>
	 * as that's how it will be searched for them
	 */
	public abstract void initialize();

}
