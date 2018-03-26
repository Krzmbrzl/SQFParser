package abego.swt;

public class DefaultNodeFactory {

	/**
	 * Takes the given input object and creates a {@link INode} out of it. For that
	 * it is first checked whether the given object already is a node. In that case
	 * the object is returned unmodified. In any other case a new node object is
	 * created whose display text is set to the object's toString method
	 * 
	 * @param input
	 *            The object to create a node from
	 * @return The created node
	 */
	public static INode createNode(Object input) {
		if (input instanceof INode) {
			return (INode) input;
		} else {
			return new INode() {

				@Override
				public String getDisplayText() {
					return input.toString();
				}
			};
		}
	}

}
