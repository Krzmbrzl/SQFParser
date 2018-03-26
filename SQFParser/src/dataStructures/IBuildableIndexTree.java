package dataStructures;

import java.util.Collection;
import java.util.Iterator;

public interface IBuildableIndexTree {

	/**
	 * Creates a new branch in this tree starting with the given index
	 * 
	 * @param i
	 *            The index to start the new branch with
	 */
	public void newBranch(int index);

	/**
	 * Takes the given index and makes it the top-level element of the current
	 * branch by grouping the current branch as a child to the newly created index
	 * 
	 * @param index
	 *            The index to use as the new top-level
	 */
	public void makeTopElement(int index);

	/**
	 * Adds the given index from the right on the given level to the current branch.
	 * That means that the rightmost node on the given level is being determined and
	 * the provided index is added as a child node to the found one.
	 * 
	 * @param level
	 *            The level of the node the given index should be added to
	 * @param index
	 *            The index to add to this tree
	 * 
	 * @return The level of the newly added index
	 */
	public int add(int level, int index);

	/**
	 * Adds the lowest, rightmost node in the current branch.
	 * 
	 * @param index
	 *            The index to add to this tree
	 * 
	 * @return The level of the newly added index
	 */
	public int add(int index);

	/**
	 * Inserts the given index from the right to the node on the given level. The
	 * insertion is completed by removing the rightmost child-node of the parent,
	 * and inserting the given index in its place. The removed child-node is then
	 * added as a child to the newly inserted node for this index.
	 * 
	 * @param level
	 *            The level of the node the given index should be inserted to
	 * @param index
	 *            The index to add to this tree
	 * 
	 * @return The level of the newly added index
	 */
	public int insert(int level, int index);

	/**
	 * Gets an iterator for iterating over all branches of this index tree
	 */
	public Iterator<IndexTreeElement> branchIterator();

	/**
	 * Adds the given tree to this one at the specified level. This is done by
	 * grouping all branches of the given tree as a child node to the rightmost node
	 * on the specified level
	 * 
	 * @param level
	 *            The level to add the tree to
	 * @param tree
	 *            The tree to add
	 * @return The new level of the rightmost, lowest node of the added tree
	 */
	public int add(int level, IBuildableIndexTree tree);

	/**
	 * Merges the given tree with this one. This is done by overtaking all branches
	 * from the provided tree and appending them to the list of branches of this
	 * tree.
	 * 
	 * @param tree
	 *            The tree to merge into this one
	 */
	public void merge(IBuildableIndexTree tree);

	/**
	 * Gets the branches of this tree
	 */
	public Collection<? extends IndexTreeElement> branches();

	/**
	 * Adds an empty node as a child to the rightmost element of the given level
	 * 
	 * @param level
	 *            The level to add the empty node to
	 * @return The newly created, empty node
	 */
	public IndexTreeElement addEmpty(int level);

	/**
	 * Creates a new branch with an empty node as the current branch content
	 * 
	 * @return The newly created, empty node
	 */
	public IndexTreeElement newEmptyBranch();

	/**
	 * Clears all contents of this tree
	 */
	public void clear();

	public static final char EMPTY_BRANCH = 'b';
	public static final char EMPTY_NODE = 'n';
	public static final char NEW_BRANCH = ':';


	/**
	 * This method will fill the given tree with input according to the given input
	 * string. IN this String a new tree branch is started with ':' and a new level
	 * has to be encapsulated in parenthesis. An empty node can be created by
	 * passing 'n' instead of a number and an empty branch by using 'b'.
	 * 
	 * Before starting to populate the given tree it is cleared from any previous
	 * content
	 * 
	 * @param tree
	 *            The tree to populate
	 * @param input
	 *            The input string encoding for the tree content
	 * @return The populated tree
	 */
	public static IBuildableIndexTree populateFromString(IBuildableIndexTree tree, String input) {
		tree.clear();

		int brackets = 0;
		boolean createNewBranch = false;
		int bracketChange = 0;
		boolean emptyNode = false;
		boolean emptyBranch = false;

		StringBuilder builder = new StringBuilder();

		char[] chars = input.toCharArray();

		for (int i = 0; i <= chars.length; i++) {
			char currentChar = (i != chars.length) ? chars[i] : (char) -1;
			switch (currentChar) {
			case '(':
				bracketChange = 1;
				break;
			case ')':
				bracketChange = -1;
				break;
			case NEW_BRANCH:
				createNewBranch = true;
				bracketChange = 0;
				break;
			case EMPTY_NODE:
				emptyNode = true;
				bracketChange = 0;
				break;
			case EMPTY_BRANCH:
				emptyBranch = true;
			default:
				bracketChange = 0;
			}
			if (Character.isDigit(currentChar)) {
				builder.append(currentChar);
			} else {
				if (builder.length() > 0 || emptyBranch || emptyNode) {
					if (createNewBranch) {
						tree.newBranch(Integer.parseInt(builder.toString()));
						createNewBranch = false;
					} else {
						if (emptyBranch) {
							tree.newEmptyBranch();
							emptyBranch = false;
						} else {
							if (emptyNode) {
								tree.addEmpty(brackets - 1);
								emptyNode = false;
							} else {
								tree.add(brackets - 1, Integer.parseInt(builder.toString()));

							}
						}
					}
					builder.setLength(0);
				}
			}

			brackets += bracketChange;
		}

		return tree;
	}

	public static String getPopulationString(IBuildableIndexTree tree) {
		StringBuilder builder = new StringBuilder();

		for (IndexTreeElement currentBranch : tree.branches()) {
			builder.append(currentBranch.getPopulationString());
			builder.append(' ');
		}

		return builder.toString().trim();
	}
}
