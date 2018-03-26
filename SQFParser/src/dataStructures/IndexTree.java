package dataStructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class IndexTree implements IBuildableIndexTree {

	/**
	 * A list holding all branches of this tree
	 */
	List<IndexTreeElement> branches;
	/**
	 * The currently processed and changeable branch
	 */
	IndexTreeElement currentBranch;

	public IndexTree() {
		branches = new ArrayList<>();
	}

	@Override
	public void newBranch(int index) {
		currentBranch = new IndexTreeElement(index);
		branches.add(currentBranch);
	}

	@Override
	public void makeTopElement(int index) {
		IndexTreeElement newBranch = new IndexTreeElement(index);

		newBranch.add(currentBranch);

		currentBranch = newBranch;
		// update reference in branch-list
		branches.set(branches.size() - 1, currentBranch);
	}

	@Override
	public int add(int level, int index) {
		return currentBranch.add(index, level);
	}

	@Override
	public int add(int index) {
		return currentBranch.add(index, IndexTreeElement.LOWEST_LEVEL);
	}

	@Override
	public int insert(int level, int index) {
		return currentBranch.insert(index, level);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (IndexTreeElement currentBranch : branches) {
			builder.append("----------------------------------------------------------------\n");
			builder.append(currentBranch.toString());
			builder.append('\n');
		}

		return builder.toString();
	}

	@Override
	public Iterator<IndexTreeElement> branchIterator() {
		return branches.iterator();
	}

	@Override
	public int add(int level, IBuildableIndexTree tree) {
		IndexTreeElement element = currentBranch.get(level);

		for (IndexTreeElement currentBranch : tree.branches()) {
			element.add(currentBranch);
		}

		return element.get(IndexTreeElement.LOWEST_LEVEL).getLevel();
	}

	@Override
	public void merge(IBuildableIndexTree tree) {
		for (IndexTreeElement currentBranch : tree.branches()) {
			branches.add(currentBranch);
		}

		currentBranch = branches.get(branches.size() - 1);
	}

	@Override
	public Collection<? extends IndexTreeElement> branches() {
		return branches;
	}

	@Override
	public IndexTreeElement addEmpty(int level) {
		IndexTreeElement empty = new IndexTreeElement(IndexTreeElement.EMPTY);
		currentBranch.get(level).add(empty);
		return empty;
	}

	@Override
	public IndexTreeElement newEmptyBranch() {
		currentBranch = new IndexTreeElement(IndexTreeElement.EMPTY);
		branches.add(currentBranch);

		return currentBranch;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IndexTree)) {
			return false;
		}
		IndexTree compare = (IndexTree) obj;

		return branches.equals(compare.branches);
	}

	@Override
	public void clear() {
		branches.clear();
		currentBranch = null;
	}
}
