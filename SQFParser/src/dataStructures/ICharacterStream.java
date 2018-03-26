package dataStructures;

public interface ICharacterStream {

	/**
	 * Gets the next character in the stream
	 */
	public int next();

	/**
	 * Puts the given character back to the top of the stream
	 * 
	 * @param c
	 *            The character to but back
	 */
	public void putBack(int c);
}
