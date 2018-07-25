package dataStructures;

public class SQFTreeWalker extends TreeWalker {

	/**
	 * The listener to notify
	 */
	private ISQFTreeListener listener;
	/**
	 * Indicates whether this is currently in an assignment context
	 */
	protected boolean assignmentContext;
	/**
	 * Indicates whether this is currently in a macro context
	 */
	protected boolean macroContext;


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
			if (node.getIndex() >= 0 && node.getChildrenCount() == 2) {
				assignmentContext = getSource().get(node.getIndex()).getText().equals("=");
			}
			if (node.getIndex() >= 0 && node.getChildrenCount() > 2) {
				if (((SQFToken) getSource().get(node.getIndex())).type() == ESQFTokentype.MACRO) {
					macroContext = true;
				}
			}
			// only process exit-nodes so that the tree is walked from bottom to top (the
			// order it will get executed)
			return;
		}

		if (node.getIndex() < 0) {
			// either invalid or empty

			if (node.getIndex() != IndexTreeElement.EMPTY || !node.hasChildren()) {
				// invalid -> ignore
				return;
			}

			IndexTreeElement child = node.getChildren().get(0);

			if (child.getIndex() < 0) {
				// invalid -> ignore
				return;
			}

			SQFToken token = (SQFToken) getSource().get(child.getIndex());

			if (token.type() == ESQFTokentype.SQUARE_BRACKET_OPEN) {
				// array
				listener.array(node);
			} else {
				if (token.type() == ESQFTokentype.CURLY_BRACKET_OPEN) {
					// inline code
					listener.code(node);
				}
			}

			return;
		}

		switch (node.getChildrenCount()) {
		case 0:
			// nular expressions can not encounter empty tree nodes
			SQFToken token = (SQFToken) getSource().get(node.getIndex());
			if (assignmentContext || macroContext || token.operatorType() == ESQFOperatorType.OTHER
					|| token.operatorType() == ESQFOperatorType.MACRO) {
				// don't process stuff like brackets or macros
				// don't feed in the new variable name as a nular expression
				
				// make sure the actual assignent is fed as a nular expression
				assignmentContext = false;
				
				return;
			}
			listener.nularExpression(token, node);
			break;

		case 1:
			// unary expression
			token = (SQFToken) getSource().get(node.getIndex());
			if (token.operatorType() == ESQFOperatorType.OTHER || token.operatorType() == ESQFOperatorType.MACRO) {
				// don't process stuff like brackets or macros
				return;
			}
			listener.unaryExpression(token, node);
			break;

		case 2:
			// binary expression
			// even if it was an assignment context: it won't matter from now on
			assignmentContext = false;
			token = (SQFToken) getSource().get(node.getIndex());
			if (token.operatorType == ESQFOperatorType.OTHER || token.operatorType == ESQFOperatorType.MACRO) {
				// don't process stuff like brackets or macros
				return;
			}
			listener.binaryExpression(token, node);
			break;

		default:
			// macro -> not interesting for this purpose
			break;
		}
	}

	@Override
	protected void notifyStartOrEnd(boolean start, IBuildableIndexTree tree) {
		if (start) {
			listener.start(tree);
		} else {
			listener.finished(tree);
		}
	}

}
