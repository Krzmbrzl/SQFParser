package dataStructures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TokenBuffer<T extends IToken> implements ITokenSource<T> {
	protected List<T> tokens;

	public TokenBuffer() {
		this(10);
	}

	public TokenBuffer(int initialCapacity) {
		tokens = new ArrayList<>(initialCapacity);
	}

	/**
	 * Gets the size of this buffer (how many tokens are contained in it)
	 */
	public int size() {
		return tokens.size();
	}

	/**
	 * Adds the given token to the end of this buffer
	 * 
	 * @param token
	 *            The token to end
	 */
	public void add(T token) {
		tokens.add(token);
	}

	/**
	 * Adds the given token at the given index in the buffer. If there is a token
	 * already occupying this location it and all following tokens will be shifted
	 * to the right
	 * 
	 * @param index
	 *            The index to add the token at
	 * @param token
	 *            The token to add
	 */
	public void add(int index, T token) {
		tokens.add(index, token);
	}

	/**
	 * Gets an iterator for this buffer
	 */
	public Iterator<T> iterator() {
		return tokens.iterator();
	}

	/**
	 * Gets the Token at the specified index
	 * 
	 * @param index
	 *            The index of the token to obtain
	 * @return The respective token
	 */
	public T get(int index) {
		return tokens.get(index);
	}
}
