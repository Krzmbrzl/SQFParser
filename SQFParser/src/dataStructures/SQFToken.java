package dataStructures;

public class SQFToken implements IToken {
	protected ESQFTokentype type;
	protected int start;
	protected int end;
	protected int precedence;
	protected ESQFOperatorType operatorType;
	protected ICharacterBuffer characterSource;


	public SQFToken(ESQFTokentype type, int start, int end, int precedence, ESQFOperatorType operatorType,
			ICharacterBuffer characterSource) {
		this.type = type;
		this.start = start;
		this.end = end;
		this.precedence = precedence;
		this.operatorType = operatorType;
		this.characterSource = characterSource;
	}

	/**
	 * Gets the type of this token
	 */
	public ESQFTokentype type() {
		return type;
	}

	@Override
	public int start() {
		return start;
	}

	@Override
	public int stop() {
		return end;
	}

	@Override
	public int length() {
		return end - start;
	}

	/**
	 * Gets the operator kind corresponding to this token
	 */
	public ESQFOperatorType operatorType() {
		return operatorType;
	}

	@Override
	public int precendence() {
		return precedence;
	}

	@Override
	public String toString() {
		return "SQFToken: " + type + " (" + precedence + ") - " + "[" + start + ":" + end + "]";
	}

	@Override
	public String getText() {
		return characterSource.getText(start(), length());
	}
}
