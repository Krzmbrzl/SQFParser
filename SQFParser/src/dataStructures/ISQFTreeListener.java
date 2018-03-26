package dataStructures;

public interface ISQFTreeListener {

	/**
	 * Gets called when encountering a nular expression
	 * 
	 * @param expression
	 *            The token corresponding to the respective nular expression
	 */
	public void nularExpression(SQFToken expression);

	/**
	 * Gets called when encountering a unary expression. That means that the given
	 * node is guaranteed to have exactly one child.The child corresponds to the
	 * argument of the operator. Note that this nodes may be empty
	 * (<code>node.getIndex() == IndexTreeElement.EMPTY</code>) if it corresponds to
	 * an array or inline code!
	 * 
	 * @param expression
	 *            The token corresponding to the unary operator
	 * @param node
	 *            The IndexTree-Node corresponding to this unary expression
	 */
	public void unaryExpression(SQFToken expression, IndexTreeElement node);

	/**
	 * Gets called when encountering a binary expression. That means that the given
	 * node is guaranteed to have exactly two children. The first child correspond
	 * to the left and the second to the right argument of the operator. Note that
	 * each of these nodes may be empty
	 * (<code>node.getIndex() == IndexTreeElement.EMPTY</code>) if it corresponds to
	 * an array or inline code!
	 * 
	 * @param expression
	 *            The token corresponding to the binary operator
	 * @param node
	 *            The IndexTree-Node corresponding to this binary expression.
	 */
	public void binaryExpression(SQFToken expression, IndexTreeElement node);
}
