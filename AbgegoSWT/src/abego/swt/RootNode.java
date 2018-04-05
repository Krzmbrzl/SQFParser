package abego.swt;

/**
 * A default implementation of a root-node
 * 
 * @author Raven
 *
 */
public class RootNode implements INode {

	@Override
	public String getDisplayText() {
		return "";
	}

	@Override
	public boolean isRoot() {
		return true;
	}

}
