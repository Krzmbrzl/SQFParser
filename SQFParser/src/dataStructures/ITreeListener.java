package dataStructures;

public interface ITreeListener {

	/**
	 * Gets called when entering a tree node
	 * 
	 * @param token
	 *            The token corresponding to the entered node. May be
	 *            <code>null</code> if there is no token corresponding to this node.
	 */
	public void enterNode(IToken token);

	/**
	 * Gets called when exiting a tree node
	 * 
	 * @param token
	 *            The token corresponding to the exited node. May be
	 *            <code>null</code> if there is no token corresponding to this node.
	 */
	public void exitNode(IToken token);

	/**
	 * Gets called when starting to walk the tree
	 */
	public void start();

	/**
	 * Gets called when having finished walking the tree
	 */
	public void finished();
}
