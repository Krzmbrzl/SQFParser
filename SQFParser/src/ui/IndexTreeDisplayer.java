package ui;

import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import abego.swt.AbegoTreeComposite;
import abego.swt.INode;
import abego.swt.Node;
import abego.swt.RootNode;
import abego.swt.SWTNodeExtentProvider;
import dataStructures.IBuildableIndexTree;
import dataStructures.IToken;
import dataStructures.ITokenSource;
import dataStructures.IndexTreeElement;

public class IndexTreeDisplayer<T extends IToken> extends Composite {

	/**
	 * The tree to display
	 */
	protected IBuildableIndexTree tree;
	/**
	 * The buffer containing the corresponding tokens
	 */
	protected ITokenSource<? extends IToken> tokenSource;
	/**
	 * The abego tree to display
	 */
	protected DefaultTreeForTreeLayout<INode> abegoTree;
	/**
	 * The tree composite responsible for displaying the respective tree
	 */
	protected AbegoTreeComposite<INode> treeComposite;
	/**
	 * If this value is set it indicates which element from the tree to display
	 */
	protected IndexTreeElement displayedElement;


	public IndexTreeDisplayer(Composite parent, int style, IBuildableIndexTree tree,
			ITokenSource<? extends IToken> tokenSource) {
		super(parent, style);

		this.tree = tree;
		this.tokenSource = tokenSource;

		updateTree();
	}

	protected void updateTree() {
		INode root = new RootNode();

		abegoTree = new DefaultTreeForTreeLayout<INode>(root);

		if (displayedElement == null) {
			for (IndexTreeElement currentBranch : tree.branches()) {
				addToTree(root, abegoTree, currentBranch);
			}
		} else {
			// display only the display-element
			addToTree(root, abegoTree, displayedElement);
		}

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		if (treeComposite != null) {
			// get rid of the old one
			treeComposite.dispose();
		}

		treeComposite = new AbegoTreeComposite<>(this, SWT.NONE, new TreeLayout<INode>(abegoTree,
				new SWTNodeExtentProvider(new GC(this)), new DefaultConfiguration<>(20, 20)));

		treeComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		
		this.layout(true);
	}

	/**
	 * Sets the tree that should be displayed
	 * 
	 * @param element
	 *            The element to display or <code>null</code> to display the whole
	 *            tree
	 */
	public void setDisplayedElement(IndexTreeElement element) {
		if (element != null && !tree.contains(element)) {
			throw new IllegalArgumentException("The given element has to be contained in the tree");
		}
		displayedElement = element;

		updateTree();
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
		INode node = new Node() {

			@Override
			public String getDisplayText() {
				return IndexTreeDisplayer.this.getDisplayText(child);
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
		if (tokenSource == null) {
			return element.getDisplayText();
		} else {
			int index = element.getIndex();

			if (index < 0) {
				// It doesn't correspond to an index
				return element.getDisplayText();
			} else {
				return tokenSource.get(index).getText();
			}
		}
	}
	
	@Override
	public Point computeSize(int wHint, int hHint) {
		return treeComposite.computeSize(wHint, hHint);
	}
}
