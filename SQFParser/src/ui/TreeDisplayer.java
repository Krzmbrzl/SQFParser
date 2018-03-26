package ui;

import java.util.Iterator;

import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import abego.swt.AbegoTreeComposite;
import abego.swt.INode;
import abego.swt.SWTNodeExtentProvider;
import dataStructures.IBuildableIndexTree;
import dataStructures.IToken;
import dataStructures.IndexTreeElement;
import dataStructures.TokenBuffer;

public class TreeDisplayer<T extends IToken> extends Composite {

	/**
	 * The tree to display
	 */
	protected IBuildableIndexTree tree;
	/**
	 * The buffer containing the corresponding tokens
	 */
	protected TokenBuffer<T> tokenBuffer;
	/**
	 * The abego tree to display
	 */
	DefaultTreeForTreeLayout<INode> abegoTree;


	public TreeDisplayer(Composite parent, int style, IBuildableIndexTree tree, TokenBuffer<T> tokenBuffer) {
		super(parent, style);

		this.tree = tree;
		this.tokenBuffer = tokenBuffer;

		initialize();
	}

	protected void initialize() {
		INode root = new INode() {

			@Override
			public String getDisplayText() {
				return "";
			}
		};

		abegoTree = new DefaultTreeForTreeLayout<INode>(root);

		Iterator<IndexTreeElement> it = tree.branchIterator();

		while (it.hasNext()) {
			IndexTreeElement currentBranch = it.next();

			addToTree(root, abegoTree, currentBranch);
		}

		setLayout(new GridLayout(1, true));

		AbegoTreeComposite<INode> comp = new AbegoTreeComposite<>(this, SWT.NONE, new TreeLayout<INode>(abegoTree,
				new SWTNodeExtentProvider(new GC(this)), new DefaultConfiguration<>(20, 20)));

		comp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
	}

	/**
	 * Adds the given children and recursively all their children to the given
	 * parent node
	 * 
	 * @param parent
	 *            The parent node to add to
	 * @param tree
	 *            The tree to contribute to
	 * @param child
	 *            The child to add
	 */
	protected void addToTree(INode parent, DefaultTreeForTreeLayout<INode> tree, IndexTreeElement child) {
		INode node = new INode() {

			@Override
			public String getDisplayText() {
				return TreeDisplayer.this.getDisplayText(child);
			}
		};

		tree.addChild(parent, node);

		if (child.hasChildren()) {
			for (IndexTreeElement currentChild : child.getChildren()) {
				addToTree(node, tree, currentChild);
			}
		}
	}

	/**
	 * Gets the display text for the given element
	 * 
	 * @param element
	 *            The element whose display text should be obtained
	 * @return The respective display text
	 */
	protected String getDisplayText(IndexTreeElement element) {
		if (tokenBuffer == null) {
			return element.getDisplayText();
		} else {
			int index = element.getIndex();

			if (index < 0) {
				// It doesn't correspond to an index
				return element.getDisplayText();
			} else {
				return tokenBuffer.get(index).getText();
			}
		}
	}
}
