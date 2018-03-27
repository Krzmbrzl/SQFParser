package dataStructures;

import java.io.IOException;

public interface ICharacterInputStream {
	/**
	 * Gets the next character in this stream
	 * 
	 * @throws IOException
	 */
	public int read() throws IOException;

	/**
	 * Unreads the last read character so that it will be returned the next time
	 * {@link #read()} is invoked
	 */
	public void unread();

	/**
	 * Gets the offset of the next character to be read in the inputStream
	 */
	public int getOffset();

	/**
	 * Peeks at the next character in the stream. This does not change the offset
	 * nor will it affect the character read next
	 * 
	 * @return The peeked character
	 * @throws IOException
	 */
	public int peek() throws IOException;

	/**
	 * Checks if this stream contains another character in the stream (i.e. the next
	 * read character != -1)
	 * 
	 * @throws IOException
	 */
	public boolean hasNext() throws IOException;

	/**
	 * Gets the {@linkplain ICharacterBuffer} storing all previously read characters
	 */
	public ICharacterBuffer getBuffer();
}
