package dataStructures;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IndexTreeElement {
	public static final int INVALID = -2;
	public static final int EMPTY = -1;
	/**
	 * Indicates that the lowest possible level should be chosen
	 */
	public static final int LOWEST_LEVEL = -5;

	protected List<IndexTreeElement> children;
	protected int index = -1;
	/**
	 * The level of this element in the hierarchy
	 */
	protected int level = 0;

	public IndexTreeElement() {
		this(INVALID);
	}

	public IndexTreeElement(int index) {
		this.index = index;
	}

	public IndexTreeElement(List<IndexTreeElement> children) {
		this.children = children;
	}

	/**
	 * Gets the amount of children this element has
	 */
	public int getChildrenCount() {
		return (children == null) ? 0 : children.size();
	}

	/**
	 * Checks whether this element has any children
	 */
	public boolean hasChildren() {
		return children != null;
	}

	/**
	 * Adds the given index as a child-node to this element or one of its children
	 * (depending on the level)
	 * 
	 * @param child
	 *            The index to add
	 * @param level
	 *            The level to add the given index to (may not be lower than this
	 *            element's level)
	 */
	public int add(int child, int level) {
		return add(new IndexTreeElement(child), level);
	}

	/**
	 * Adds the given index as a child-node to this element
	 * 
	 * @param child
	 *            The index to add
	 */
	public int add(int index) {
		return add(new IndexTreeElement(index), getLevel());
	}

	/**
	 * Adds the given IndexTreeElement as a child-node to this element or one of its
	 * children (depending on the level)
	 * 
	 * @param child
	 *            The element to add
	 * @param level
	 *            The level to add the given element to (may not be lower than this
	 *            element's level)
	 */
	public int add(IndexTreeElement child, int level) {
		return get(level).doAdd(child);
	}

	/**
	 * Adds the given IndexTreeElement as a child-node to this element
	 * 
	 * @param child
	 *            The element to add
	 */
	public int add(IndexTreeElement child) {
		return add(child, getLevel());
	}

	/**
	 * Adds the given child to this element
	 * 
	 * @param child
	 *            The child element to add
	 */
	protected int doAdd(IndexTreeElement child) {
		if (!hasChildren()) {
			// initialize children-list
			children = new ArrayList<IndexTreeElement>();
		}
		children.add(child);

		// Make sure the child's level is one unit lower in comparison to this element
		child.changeLevel(getLevel() - child.getLevel() + 1);

		return getLevel() + 1;
	}

	/**
	 * Gets the element with the specified level
	 * 
	 * @param level
	 *            The level to search for. May not be smaller than this element's
	 *            one
	 * @return The respective element
	 */
	public IndexTreeElement get(int level) {
		int currentLevel = getLevel();

		if (level == LOWEST_LEVEL) {
			if (hasChildren()) {
				// delegate to rightmost child
				return children.get(children.size() - 1).get(level);
			} else {
				// this is the lowest level -> return this element
				return this;
			}
		}

		if (level < currentLevel) {
			throw new IllegalArgumentException("The specified level is lower than the current element's one!");
		}

		if (level == currentLevel) {
			// add as a child to this node
			return this;
		} else {
			if (!hasChildren()) {
				throw new IllegalArgumentException(
						"Trying to add to level " + level + " while lowest level was " + currentLevel);
			}
			// delegate to rightmost child
			return children.get(children.size() - 1).get(level);
		}
	}

	/**
	 * Inserts the given element at the specified level. This is done by removing
	 * the rightmost child and adding the new child instead. The removed child will
	 * then be grouped as a child to the newly added element
	 * 
	 * @param child
	 *            The index to insert
	 * @param level
	 *            The level to insert this index at
	 * @return The level of the newly inserted child
	 */
	public int insert(int index, int level) {
		return get(level).doInsert(new IndexTreeElement(index));
	}

	/**
	 * Inserts the given element in this element's child-list. This is done by
	 * removing the rightmost child and adding the new child instead. The removed
	 * child will then be grouped as a child to the newly added element
	 * 
	 * @param child
	 *            The element to insert
	 * @return The level of the newly inserted child
	 */
	protected int doInsert(IndexTreeElement child) {
		if (!hasChildren()) {
			throw new IllegalStateException("Can't insert as there are no children on this element!");
		}

		IndexTreeElement old = children.remove(children.size() - 1);
		doAdd(child);

		child.add(old);

		return child.getLevel();
	}

	/**
	 * Sets the index of this element
	 * 
	 * @param index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Gets the text this element should be displayed as
	 */
	public String getDisplayText() {
		return (getIndex() > 0) ? String.valueOf(index) : "";
	}

	/**
	 * Changes the level of this and all child-elements by the given amount
	 * 
	 * @param amount
	 *            The amount of change (may be negative)
	 */
	public void changeLevel(int amount) {
		level += amount;

		if (level < 0) {
			throw new IllegalStateException("Level may not be negative!");
		}

		if (hasChildren()) {
			// also change the level of all child-elements
			children.forEach(new Consumer<IndexTreeElement>() {

				@Override
				public void accept(IndexTreeElement t) {
					t.changeLevel(amount);
				}
			});
		}
	}

	/**
	 * Gets the level of this element
	 */
	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		String indent = "";

		for (int i = 0; i < level; i++) {
			indent += "\t";
		}

		if (!hasChildren()) {
			if (index >= 0) {
				return indent + String.valueOf(index);
			} else {
				return indent + (index == EMPTY ? "Empty" : "Invalid");
			}

		}

		StringBuilder builder = new StringBuilder();

		builder.append(indent);
		builder.append((index >= 0) ? index : "Empty");
		builder.append("\n");

		for (int i = 0; i < children.size(); i++) {
			IndexTreeElement current = children.get(i);

			builder.append(current.toString() + "\n");
		}

		return builder.toString();
	}

	/**
	 * Gets the index this element represents. Any negative index indicates that
	 * this element does not directly correspond to an index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Gets the children of this element. This may be null in case it doesn't have
	 * any children
	 */
	public List<IndexTreeElement> getChildren() {
		return children;
	}

	/**
	 * Checks if this element is empty. An element is considered empty if it does
	 * not have a valid index set. Note that it still can contain children although
	 * it is considered empty
	 */
	public boolean isEmpty() {
		return index < 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IndexTreeElement)) {
			return false;
		}

		IndexTreeElement compare = (IndexTreeElement) obj;

		if (index != compare.getIndex()) {
			return false;
		}

		if (hasChildren()) {
			return children.equals(compare.children);
		} else {
			return !compare.hasChildren();
		}
	}

	/**
	 * Gets the population String that can be used inside
	 * {@link IBuildableIndexTree#populateFromString(IBuildableIndexTree, String)}
	 */
	public String getPopulationString() {
		StringBuilder builder = new StringBuilder();
		boolean isRoot = getLevel() == 0;

		if (getIndex() < 0) {
			if (getIndex() != EMPTY) {
				throw new IllegalStateException("Node is invalid!");
			} else {
				if (isRoot) {
					builder.append(IBuildableIndexTree.EMPTY_BRANCH);
				} else {
					builder.append(IBuildableIndexTree.EMPTY_NODE);
				}
			}
		} else {
			builder.append((isRoot ? IBuildableIndexTree.NEW_BRANCH : "") + String.valueOf(getIndex()));
		}

		if (hasChildren()) {
			builder.append('(');

			for (IndexTreeElement child : getChildren()) {
				builder.append(child.getPopulationString());
				builder.append(' ');
			}

			builder.append(')');
		}

		return builder.toString().replace(" )", ")");
	}

	/**
	 * Checks whether this element contains the given one in any of its children.
	 * This method will return false, if the given element is equal to this one
	 * 
	 * @param element
	 *            The element to search for
	 * @return Whether or not the given element is contained
	 */
	public boolean contains(IndexTreeElement element) {
		if (!hasChildren()) {
			return false;
		}

		for (IndexTreeElement currentElement : children) {
			if (currentElement.equals(element) || currentElement.contains(element)) {
				return true;
			}
		}

		return false;
	}
}
