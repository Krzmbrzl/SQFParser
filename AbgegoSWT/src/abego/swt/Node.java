package abego.swt;

/**
 * An abstract implementation of a {@linkplain INode} that is not a root node
 * 
 * @author Raven
 *
 */
public abstract class Node implements INode {

	@Override
	public boolean isRoot() {
		return false;
	}

}
