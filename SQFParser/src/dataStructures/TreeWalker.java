package dataStructures;

public class TreeWalker implements ITreeWalker {

	/**
	 * The tree to walk
	 */
	private IBuildableIndexTree tree;
	/**
	 * The listener to notify
	 */
	private ITreeListener listener;
	/**
	 * The token source of the given tree
	 */
	private ITokenSource<? extends IToken> source;

	/**
	 * Creates a new instance of this walker.
	 * 
	 * @param tree
	 *            The tree to walk
	 * @param source
	 *            The token source corresponding to the given tree
	 * @param listener
	 *            The listener to notify during walking
	 */
	public TreeWalker(IBuildableIndexTree tree, ITokenSource<? extends IToken> source, ITreeListener listener) {
		assert (tree != null);
		assert (source != null);

		this.tree = tree;
		this.listener = listener;
		this.source = source;
	}

	@Override
	public void walk() {
		notifyStartOrEnd(true, tree);

		for (IndexTreeElement currentBranch : tree.branches()) {
			visitNode(currentBranch);
		}

		notifyStartOrEnd(false, tree);
	}

	/**
	 * Visits the given {@linkplain IndexTreeElement} and all of its children
	 * recursively. While doing so it will call the respective enter and exit
	 * methods of the specified {@linkplain ITreeListener}
	 * 
	 * @param node
	 *            The node to visit
	 */
	protected void visitNode(IndexTreeElement node) {
		notifyListener(true, node);

		if (node.hasChildren()) {
			for (IndexTreeElement currentChild : node.getChildren()) {
				visitNode(currentChild);
			}
		}

		notifyListener(false, node);
	}

	/**
	 * Notifies the listener
	 * 
	 * @param enter
	 *            Whether this notification occurs while entering the node
	 * @param node
	 *            The node the notification is about
	 */
	protected void notifyListener(boolean enter, IndexTreeElement node) {
		if (enter) {
			listener.enterNode(node.getIndex() > 0 ? source.get(node.getIndex()) : null);
		} else {
			listener.exitNode(node.getIndex() > 0 ? source.get(node.getIndex()) : null);
		}
	}

	/**
	 * Notifies the listener about starting or finishing the parse tree walking
	 * 
	 * @param start
	 *            Whether the walking has just started
	 * @param tree
	 *            The tree as a whole
	 */
	protected void notifyStartOrEnd(boolean start, IBuildableIndexTree tree) {
		if (start) {
			listener.start(tree);
		} else {
			listener.finished(tree);
		}
	}

	/**
	 * Gets the token source corresponding to the walked tree
	 */
	protected ITokenSource<? extends IToken> getSource() {
		return source;
	}

}
