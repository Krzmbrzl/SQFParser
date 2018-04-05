import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import abego.swt.AbegoTreeComposite;
import abego.swt.INode;
import abego.swt.Node;
import abego.swt.RootNode;
import abego.swt.SWTNodeExtentProvider;

public class Activator {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);

		// shell.setLayout(new FillLayout());

		INode root = new RootNode();
		DefaultTreeForTreeLayout<INode> tree = new DefaultTreeForTreeLayout<INode>(root);
		tree.addChild(root, new Node() {

			@Override
			public String getDisplayText() {
				return ";";
			}
		});
		tree.addChild(root, new Node() {

			@Override
			public String getDisplayText() {
				return "Other miau\nhere";
			}
		});

		DefaultConfiguration<INode> config = new DefaultConfiguration<>(20, 20);

		TreeLayout<INode> layout = new TreeLayout<INode>(tree, new SWTNodeExtentProvider(new GC(shell)), config);

		AbegoTreeComposite<INode> comp = new AbegoTreeComposite<>(shell, SWT.NONE, layout);
		comp.setSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));


		shell.open();

		// run the event loop as long as the window is open
		while (!shell.isDisposed()) {
			// read the next OS event queue and transfer it to a SWT event
			if (!display.readAndDispatch()) {
				// if there are currently no other OS event to process
				// sleep until the next OS event is available
				display.sleep();
			}
		}

	}
}
