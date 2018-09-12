package dataStructures;

/**
 * An interface describing an object that is able to produce tokens
 * 
 * @author Raven
 *
 * @param <T>
 *            The type of token the factory should produce
 */
public interface ITokenFactory<T extends IToken> {

	/**
	 * Produces a token and sets its precedence and operator type as determined by
	 * {@link #lookupTable}. This method assumes that the
	 * {@linkplain ICharacterBuffer} has been set via
	 * {@link #setBuffer(ICharacterBuffer)} beforehand.
	 * 
	 * @param type
	 *            The token type
	 * @param start
	 *            The start index of the token (inclusive)
	 * @param end
	 *            The end index of the token (exclusive)
	 * @return The created token
	 */
	public T produce(Object type, int start, int end);

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
	public T produce(Object type, int start, int end, ICharacterBuffer buffer);

	/**
	 * Sets the {@linkplain ICharacterBuffer} that should be associated with the
	 * created tokens
	 * 
	 * @param buffer
	 *            The buffer to associate the tokens with
	 */
	public void setBuffer(ICharacterBuffer buffer);
}
