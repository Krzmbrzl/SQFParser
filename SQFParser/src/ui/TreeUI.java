package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import dataStructures.IBuildableIndexTree;
import dataStructures.IToken;
import dataStructures.ITokenSource;
import dataStructures.IndexTreeElement;

public class TreeUI extends Composite {

	/**
	 * The node-tree that lets the user choose which node to display
	 */
	protected Tree nodeTree;
	/**
	 * The tree to display
	 */
	protected IBuildableIndexTree tree;
	/**
	 * The token source holding the tokens references from {@link #tree}
	 */
	protected ITokenSource<? extends IToken> tokenSource;


	public TreeUI(Composite parent, int style, IBuildableIndexTree tree, ITokenSource<? extends IToken> tokenSource) {
		super(parent, style);

		this.tree = tree;
		this.tokenSource = tokenSource;

		initialize();
	}

	protected void initialize() {
		setLayout(new FillLayout());

		SashForm sash = new SashForm(this, SWT.HORIZONTAL);

		nodeTree = new Tree(sash, SWT.SINGLE);
		ScrolledComposite scroller = new ScrolledComposite(sash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scroller.setExpandVertical(true);
		scroller.setExpandHorizontal(true);

		sash.setWeights(new int[] { 3, 7 });

		if (tree == null) {
			// show text stating that there is no tree to display
			Text dummyText = new Text(scroller, SWT.CENTER | SWT.READ_ONLY);
			dummyText.setText("No tree to display");
			scroller.setContent(dummyText);

			return;
		}

		IndexTreeDisplayer<IToken> displayer = new IndexTreeDisplayer<IToken>(scroller, SWT.NONE, tree, tokenSource);
		scroller.setContent(displayer);
		
		// add node to display complete tree
		TreeItem displayAllItem = new TreeItem(nodeTree, SWT.NONE);
		displayAllItem.setText("Complete tree");

		// add nodes
		for (IndexTreeElement currentBranch : tree.branches()) {
			TreeItem item = new TreeItem(nodeTree, SWT.NONE);
			item.setText(getTextRepresentation(currentBranch));
			item.setData(currentBranch);

			if (currentBranch.hasChildren()) {
				addNodes(currentBranch.getChildren(), item);
			}
		}

		// add selection listener to nodeTree
		nodeTree.addSelectionListener(new SelectionAdapter() {

			private IndexTreeElement previousSelection;

			@Override
			public void widgetSelected(SelectionEvent e) {
				IndexTreeElement selected = (IndexTreeElement) e.item.getData();

				if (selected != null && selected.equals(previousSelection)) {
					System.out.println("Selected again");
				} else {
					previousSelection = selected;
					displayer.setDisplayedElement(selected);
					scroller.setMinSize(displayer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					scroller.layout(true);
				}
			}
		});
	}

	/**
	 * Adds all given elements and their sub-elements as TreeElements to the tree
	 * 
	 * @param nodes
	 *            The nodes to add
	 * @param parent
	 *            The parent to add to
	 */
	protected void addNodes(Iterable<IndexTreeElement> nodes, TreeItem parent) {
		for (IndexTreeElement currentElement : nodes) {
			TreeItem treeItem = new TreeItem(parent, SWT.NONE);
			treeItem.setText(getTextRepresentation(currentElement));
			treeItem.setData(currentElement);

			if (currentElement.hasChildren()) {
				addNodes(currentElement.getChildren(), treeItem);
			}
		}
	}

	protected String getTextRepresentation(IndexTreeElement element) {
		if (element.getIndex() < 0) {
			return (element.getIndex() == IndexTreeElement.EMPTY) ? "Empty" : "Invalid";
		} else {
			return tokenSource.get(element.getIndex()).getText();
		}
	}

}
