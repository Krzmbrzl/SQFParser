package dataStructures;

public interface ICharacterBuffer {

	/**
	 * Gets the characters from the given start index (inclusive) to the given end
	 * index (exclusive)
	 * 
	 * @param start
	 *            The start index
	 * @param end
	 *            The end index
	 * @return An int array containing the respective character-codes
	 */
	public int[] get(int start, int end);

	/**
	 * Gets the characters from the given start index (inclusive) to the given end
	 * index (exclusive)
	 * 
	 * @param start
	 *            The start index
	 * @param end
	 *            The end index
	 * @return A char array containing the respective characters
	 */
	public char[] getChar(int start, int end);

	/**
	 * Gets the character-code at the given position
	 * 
	 * @param index
	 *            The index of the character-code to obtain
	 * @return The respective character-code
	 */
	public int get(int index);

	/**
	 * Gets the character at the given position
	 * 
	 * @param index
	 *            The index of the character to obtain
	 * @return The respective character
	 */
	public char getChar(int index);

	/**
	 * Gets the character that correspond to the given token
	 * 
	 * @param token
	 *            The token whose corresponding characters should be obtained
	 * @return A char array containing the respective characters
	 */
	public int[] get(SQFToken token);

	/**
	 * Appends the given character to this buffer
	 * 
	 * @param c
	 *            The character to add
	 */
	public void append(char c);

	/**
	 * Appends the given character to this buffer
	 * 
	 * @param c
	 *            The character to add
	 */
	public void append(int c);

	/**
	 * Gets the text delimited by the given indices
	 * 
	 * @param start
	 *            The index of the first character to include
	 * @param length
	 *            The length of the character sequence to extract
	 * @return The corresponding string
	 */
	public String getText(int start, int length);
}
