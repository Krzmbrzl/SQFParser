package abego.swt;

import java.awt.geom.Rectangle2D;

import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class AbegoTreeComposite<T extends INode> extends Composite implements PaintListener {

	/**
	 * The arc-size to use when drawing the boxes for the given nodes
	 */
	protected int arcSize = 5;

	/**
	 * The treelayout to represent
	 */
	protected TreeLayout<T> treeLayout;

	/**
	 * The color to use for the borders of the node-boxes
	 */
	protected Color borderColor;
	/**
	 * The color to use for the background of the drawn boxes
	 */
	protected Color boxBackground;
	/**
	 * How thick the lines are currently drawn
	 */
	protected int lineWidth = 1;

	public AbegoTreeComposite(Composite parent, int style, TreeLayout<T> layout) {
		super(parent, style);

		this.treeLayout = layout;

		boxBackground = getBackground();
		borderColor = getDisplay().getSystemColor(SWT.COLOR_BLACK);

		this.addPaintListener(this);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Rectangle2D bounds = treeLayout.getBounds();
		return new Point((int) Math.ceil(bounds.getWidth() + lineWidth), (int) Math.ceil(bounds.getHeight()) + lineWidth);
	}

	@Override
	public void paintControl(PaintEvent e) {
		lineWidth = e.gc.getLineWidth();
		drawLines(e.gc, getTree().getRoot());

		for (T node : treeLayout.getNodeBounds().keySet()) {
			drawBox(e.gc, node);
		}
	}

	protected TreeForTreeLayout<T> getTree() {
		return treeLayout.getTree();
	}

	/**
	 * Draws the lines that connect the given parent node with all of its children
	 * and the recursively all children with their children and so on.
	 * 
	 * @param gc
	 *            The GC to use for the drawing
	 * @param parent
	 *            The parent node to start the drawing/connecting at
	 */
	protected void drawLines(GC gc, T parent) {
		if (!getTree().isLeaf(parent)) {
			Rectangle2D.Double b1 = treeLayout.getNodeBounds().get(parent);
			double x1 = b1.getCenterX();
			double y1 = b1.getCenterY();


			for (T child : getTree().getChildren(parent)) {
				Rectangle2D.Double b2 = treeLayout.getNodeBounds().get(child);
				gc.drawLine((int) x1, (int) y1, (int) b2.getCenterX(), (int) b2.getCenterY());

				drawLines(gc, child);
			}
		}
	}

	/**
	 * Draws the box for the given node. A box consists of a frame, a background
	 * color and the text corresponding to the given node
	 * 
	 * @param gc
	 *            The GC to use for the drawing
	 * @param node
	 *            The node to draw the box for
	 */
	protected void drawBox(GC gc, T node) {
		int correcter = gc.getFontMetrics().getAscent() / 4;

		Rectangle2D.Double box = getBoundsOfNode(node);

		if (box.getWidth() == 0) {
			return;
		}

		gc.setAntialias(SWT.ON);

		// draw the box in the background
		gc.setBackground(boxBackground);
		gc.fillRoundRectangle((int) box.x, (int) box.y, (int) box.width, (int) box.height, arcSize, arcSize);

		// add a border to the box
		gc.setBackground(borderColor);
		gc.drawRoundRectangle((int) box.x, (int) box.y, (int) box.width, (int) box.height, arcSize, arcSize);

		// draw the text on top of the box (possibly multiple lines)
		gc.setForeground(getForeground());
		gc.drawText(node.getDisplayText(), (int) box.x + correcter + 1, (int) box.y + correcter,
				SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
	}

	/**
	 * Calculates the bounds of the given node
	 * 
	 * @param node
	 *            The node whose bounds should be calculated
	 * @return The bounds of that particular node
	 */
	protected Rectangle2D.Double getBoundsOfNode(T node) {
		return treeLayout.getNodeBounds().get(node);
	}

	/**
	 * Sets the arc size for this composite. It is used on the drawn boxes for the
	 * nodes of the tree
	 * 
	 * @param arcSize
	 *            The arc size to use
	 */
	public void setArcSize(int arcSize) {
		if (arcSize < 0) {
			throw new IllegalArgumentException("Arc size may not be smaller than zero!");
		}

		this.arcSize = arcSize;
	}

	/**
	 * Sets the color that is being used for the border of the drawn boxes for the
	 * individual nodes
	 * 
	 * @param borderColor
	 *            The color to use
	 */
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * Sets the color that is being used for the background of the drawn boxes for
	 * the individual nodes
	 * 
	 * @param boxBackground
	 *            The color to use
	 */
	public void setBoxBackground(Color boxBackground) {
		this.boxBackground = boxBackground;
	}

	@Override
	public Point computeSize(int wHint, int hHint) {
		return computeSize(wHint, hHint, false);
	}

}
