package abego.swt;

import org.abego.treelayout.NodeExtentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

public class SWTNodeExtentProvider implements NodeExtentProvider<INode> {

	protected GC gc;

	public SWTNodeExtentProvider(GC gc) {
		this.gc = gc;
	}

	@Override
	public double getWidth(INode treeNode) {
		if (treeNode.isRoot()) {
			return 0;
		}
		int extent = gc.textExtent(treeNode.getDisplayText(), SWT.DRAW_TAB | SWT.DRAW_DELIMITER).x;
		return (extent == 0) ? 0 : extent + gc.getFontMetrics().getAscent() / 2;
	}

	@Override
	public double getHeight(INode treeNode) {
		if (treeNode.isRoot()) {
			return 0;
		}
		int extent = gc.textExtent(treeNode.getDisplayText(), SWT.DRAW_TAB | SWT.DRAW_DELIMITER).y;
		return (extent == 0) ? 0 : extent + gc.getFontMetrics().getAscent() / 2;
	}

}
