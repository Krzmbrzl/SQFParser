package dataStructures;

public interface IErrorListener {

	/**
	 * Reports an occurred error
	 * 
	 * @param msg
	 *            The error message
	 * @param token
	 *            The token at which the error occurred
	 */
	public void error(String msg, SQFToken token);
}
