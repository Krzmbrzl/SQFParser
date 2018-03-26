package dataStructures;

public class DebugIndexTree extends IndexTree {

	@Override
	public void newBranch(int index) {
		System.out.println("Creating a new branch starting with index " + index);
		super.newBranch(index);
	}

	@Override
	public void makeTopElement(int index) {
		System.out.println("Making index " + index + " the top element of the current branch");
		super.makeTopElement(index);
	}

	@Override
	public int add(int level, int index) {
		System.out.println("Adding index " + index + " to node on level " + level);
		return super.add(level, index);
	}

	@Override
	public int add(int index) {
		System.out.println("Addin index " + index);
		return super.add(index);
	}

	@Override
	public int insert(int level, int index) {
		System.out.println("Inserting index " + index + " on level " + level);
		return super.insert(level, index);
	}

}
