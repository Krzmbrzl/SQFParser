package dataStructures;

import java.util.ArrayList;
import java.util.List;

public class CharacterBuffer {

	protected List<Integer> characters;

	public CharacterBuffer() {
		this(100);
	}

	public CharacterBuffer(int initialCapacity) {
		characters = new ArrayList<>(initialCapacity);
	}

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
	public int[] get(int start, int end) {
		int[] characters = new int[end - start];

		for (int i = start; i < end; i++) {
			characters[i] = this.characters.get(i);
		}

		return characters;
	}

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
	public char[] getChar(int start, int end) {
		char[] characters = new char[end - start];

		for (int i = start; i < end; i++) {
			characters[i] = (char) (int) this.characters.get(i);
		}

		return characters;
	}

	/**
	 * Gets the character-code at the given position
	 * 
	 * @param index
	 *            The index of the character-code to obtain
	 * @return The respective character-code
	 */
	public int get(int index) {
		return characters.get(index);
	}

	/**
	 * Gets the character at the given position
	 * 
	 * @param index
	 *            The index of the character to obtain
	 * @return The respective character
	 */
	public char getChar(int index) {
		return (char) (int) characters.get(index);
	}

	/**
	 * Gets the character that correspond to the given token
	 * 
	 * @param token
	 *            The token whose corresponding characters should be obtained
	 * @return A char array containing the respective characters
	 */
	public int[] get(SQFToken token) {
		return get(token.start, token.stop());
	}

	/**
	 * Appends the given character to this buffer
	 * 
	 * @param c
	 *            The character to add
	 */
	public void append(char c) {
		append((int) c);
	}

	/**
	 * Appends the given character to this buffer
	 * 
	 * @param c
	 *            The character to add
	 */
	public void append(int c) {
		characters.add((int) c);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(characters.size());

		for (int c : characters) {
			builder.append((char) c);
		}

		return builder.toString();
	}

	/**
	 * Gets the text delimited by the given indices
	 * 
	 * @param start
	 *            The index of the first character to include
	 * @param length
	 *            The length of the character sequence to extract
	 * @return The corresponding string
	 */
	public String getText(int start, int length) {
		StringBuilder builder = new StringBuilder(length);

		for (int i = start; i < start + length; i++) {
			builder.append((char) (int) this.characters.get(i));
		}

		return builder.toString();
	}
}
