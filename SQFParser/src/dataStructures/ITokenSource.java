package dataStructures;

public interface ITokenSource<T extends IToken> {

	/**
	 * Gets the token at the specified index
	 * 
	 * @param index
	 *            The index corresponding to the wished token
	 */
	public T get(int index);

	/**
	 * Gets the amount of tokens in this source
	 */
	public int size();
}
