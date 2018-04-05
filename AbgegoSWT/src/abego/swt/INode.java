package abego.swt;

public interface INode {

	/**
	 * Gets the text that should be used in order to display this node
	 */
	public String getDisplayText();

	/**
	 * Whether this node represents a root node
	 */
	public boolean isRoot();
}
