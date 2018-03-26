package dataStructures;

import java.io.IOException;
import java.io.InputStream;

public class CharacterInputStream1 {

	// TODO: Process file in two threads -> BlockingQueue
	// File reading via FileChannels

	/**
	 * The offset of the latest read character in the inputStream
	 */
	int offset;

	/**
	 * How many characters are being unread at the moment
	 */
	int unreadOffset;

	/**
	 * The buffer for all read characters -> random access possible
	 */
	protected CharacterBuffer characters;

	/**
	 * The inputStream to use
	 */
	protected InputStream in;


	public CharacterInputStream1(InputStream in) {
		this.in = in;
		characters = new CharacterBuffer();
	}


	/**
	 * Gets the next character in this stream
	 * 
	 * @throws IOException
	 */
	public int read() throws IOException {
		// TODO: use CharacterBuffer as pushBack destination for unread characters

		// copied from super class to get rid of extra bit-operation
		if (unreadOffset > 0) {
			return characters.get(offset - unreadOffset--);
		}
		int c = in.read();

		offset++;

		characters.append(c);

		return c;
	}

	/**
	 * Unreads the last read character so that it will be returned the next time
	 * {@link #read()} is invoked
	 * 
	 * @throws IOException
	 */
	public void unread() throws IOException {
		unreadOffset++;
	}

	/**
	 * Gets the offset of the next character to be read in the inputStream
	 */
	public int getOffset() {
		return offset - unreadOffset;
	}

	/**
	 * Peeks at the next character in the stream. This does not change the offset
	 * nor will it affect the character read next
	 * 
	 * @return The peeked character
	 * @throws IOException
	 */
	public int peek() throws IOException {
		int next = read();
		unread();

		return next;
	}

	/**
	 * Checks if this stream contains another character in the stream (i.e. the next
	 * read character != -1)
	 * 
	 * @throws IOException
	 */
	public boolean hasNext() throws IOException {
		return peek() != -1;
	}

	/**
	 * Gets the character buffer containing all previously read characters
	 */
	public CharacterBuffer getBuffer() {
		return characters;
	}
}
