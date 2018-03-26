package dataStructures;

public class SQFTreeWalker extends TreeWalker {

	/**
	 * The listener to notify
	 */
	private ISQFTreeListener listener;


	public SQFTreeWalker(IBuildableIndexTree tree, ITokenSource<? extends SQFToken> source, ISQFTreeListener listener) {
		super(tree, source, null);
		this.listener = listener;
	}

	@Override
	protected void visitNode(IndexTreeElement node) {
		if (node.getIndex() > 0
				&& ((SQFToken) getSource().get(node.getIndex())).operatorType() == ESQFOperatorType.MACRO) {
			// Don't visit macro-children. Assume macro is a nular expression
			notifyListener(false, node);
		} else {
			super.visitNode(node);
		}
	}

	@Override
	protected void notifyListener(boolean enter, IndexTreeElement node) {
		if (enter) {
			// only process exit-nodes so that the tree is walked from bottom to top (the
			// order it will get executed)
			return;
		}

		if (node.getIndex() < 0) {
			// either invalid or empty -> ignore
			return;
		}

		switch (node.getChildrenCount()) {
		case 0:
			// nular expressions can not encounter empty tree nodes
			SQFToken token = (SQFToken) getSource().get(node.getIndex());
			if (token.operatorType == ESQFOperatorType.OTHER) {
				// don't process stuff like brackets
				return;
			}
			listener.nularExpression(token);
			break;

		case 1:
			// unary expression
			token = (SQFToken) getSource().get(node.getIndex());
			if (token.operatorType == ESQFOperatorType.OTHER) {
				// don't process stuff like brackets
				return;
			}
			listener.unaryExpression(token, node);
			break;

		case 2:
			// binary expression
			token = (SQFToken) getSource().get(node.getIndex());
			if (token.operatorType == ESQFOperatorType.OTHER) {
				// don't process stuff like brackets
				return;
			}
			listener.binaryExpression(token, node);
			break;

		default:
			// array, inline code or macro -> not interesting for this purpose
			break;
		}
	}

}
