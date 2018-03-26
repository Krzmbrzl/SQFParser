package dataStructures;

public interface IToken {

	/**
	 * Gets the precedence of the operator represented by this token
	 */
	public int precendence();

	/**
	 * Gets the starting offset of this token
	 */
	public int start();

	/**
	 * Gets the end offset of this token
	 */
	public int stop();

	/**
	 * Gets the length of this token
	 */
	public int length();

	/**
	 * Gets the text corresponding to this token
	 */
	public String getText();
}
