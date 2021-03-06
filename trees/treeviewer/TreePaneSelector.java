package jebl.gui.trees.treeviewer;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id: TreePaneSelector.java 614 2007-01-08 04:30:58Z pepster $
 */
public class TreePaneSelector implements MouseListener, MouseMotionListener {
    public enum SelectionMode {
        NODE,
        CLADE,
        TAXA
    }

    public enum DragMode {
        SELECT,
        SCROLL
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    public void setDragMode(DragMode dragMode) {
        this.dragMode = dragMode;
    }

    public TreePaneSelector(TreePane treePane) {
        this.treePane = treePane;
        treePane.addMouseListener(this);
        treePane.addMouseMotionListener(this);
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        final Point mousePoint = mouseEvent.getPoint();
        final jebl.evolution.graphs.Node[] selectedNode = treePane.getNodeAt(mousePoint);
        final boolean doubleClick = mouseEvent.getClickCount() > 1;

        final boolean addToSelection = mouseEvent.isShiftDown() || mouseEvent.isControlDown();

        // keep selection on double click
        if( ! doubleClick ) {
            if ( ! addToSelection ) {
                treePane.clearSelection();
            }
        }

        SelectionMode mode = selectionMode;
        if (mouseEvent.isAltDown()) {
            switch( mode ) {
                case NODE:  mode = SelectionMode.CLADE; break;
                case CLADE: mode = SelectionMode.NODE; break;
                default: break;
            }
        }

        final jebl.evolution.graphs.Node mainSelectedNode = selectedNode[0];

        final boolean alreadySelected = treePane.getSelectedNodes().contains(mainSelectedNode);
        switch (mode) {
            case NODE:
                treePane.addSelectedNode(mainSelectedNode, !(addToSelection && alreadySelected));
                break;
            case CLADE:

                if( doubleClick ) {
                    if( mainSelectedNode != null )
                        treePane.toggleExpandContract(mainSelectedNode);
                } else {
                    treePane.addSelectedClade(selectedNode, !(addToSelection && alreadySelected));
                }
                break;
            case TAXA:
                treePane.addSelectedTaxa(mainSelectedNode);
                break;
            default:
                throw new IllegalArgumentException("Unknown SelectionMode: " + selectionMode.name());
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {
        // This is used for dragging in combination with mouseDragged
        // in the MouseMotionListener, below.
        dragPoint = new Point2D.Double(mouseEvent.getPoint().getX(), mouseEvent.getPoint().getY());
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        if (treePane.getDragRectangle() != null) {
            Set<jebl.evolution.graphs.Node> selectedNodes = treePane.getNodesAt((Graphics2D) treePane.getGraphics(), treePane.getDragRectangle().getBounds());

            if (!mouseEvent.isShiftDown()) {
                treePane.clearSelection();
            }

            SelectionMode mode = selectionMode;
            if (mouseEvent.isAltDown()) {
                if (mode == SelectionMode.NODE) {
                    mode = SelectionMode.CLADE;
                } else if (mode == SelectionMode.CLADE) {
                    mode = SelectionMode.NODE;
                }
            }

            for (jebl.evolution.graphs.Node selectedNode : selectedNodes) {
                switch (mode) {
                    case NODE:
                        treePane.addSelectedNode(selectedNode, true);
                        break;
                    case CLADE:
                        treePane.addSelectedClade(new jebl.evolution.graphs.Node[]{selectedNode, null}, true);
                        break;
                    case TAXA:
                        treePane.addSelectedTaxa(selectedNode);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown SelectionMode: " + selectionMode.name());
                }
            }
        }
        treePane.setDragRectangle(null);
    }

    public void mouseEntered(MouseEvent mouseEvent) {
        if (dragMode == DragMode.SCROLL || mouseEvent.isMetaDown()) {
            treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        } else {

            treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }

    public void mouseExited(MouseEvent mouseEvent) {
    }

    public void mouseMoved(MouseEvent mouseEvent) {

    }

    public void mouseDragged(MouseEvent mouseEvent) {
        //this situation can happen on MacOS, though very rare
        if (dragPoint == null) {
            return;
        }
        if (dragMode == DragMode.SCROLL || mouseEvent.isMetaDown()) {
            // Calculate how far the mouse has been dragged from the point clicked in
            // mousePressed, above.
            int deltaX = (int) (mouseEvent.getX() - dragPoint.getX());
            int deltaY = (int) (mouseEvent.getY() - dragPoint.getY());

            // Get the currently visible window
            Rectangle visRect = treePane.getVisibleRect();

            // Calculate how much we need to scroll
            if (deltaX > 0) {
                deltaX = visRect.x - deltaX;
            } else {
                deltaX = visRect.x + visRect.width - deltaX;
            }

            if (deltaY > 0) {
                deltaY = visRect.y - deltaY;
            } else {
                deltaY = visRect.y + visRect.height - deltaY;
            }

            // Scroll the visible region
            Rectangle r = new Rectangle(deltaX, deltaY, 1, 1);
            treePane.scrollRectToVisible(r);
        } else {
            double x1 = Math.min(dragPoint.getX(), mouseEvent.getPoint().getX());
            double y1 = Math.min(dragPoint.getY(), mouseEvent.getPoint().getY());
            double x2 = Math.max(dragPoint.getX(), mouseEvent.getPoint().getX());
            double y2 = Math.max(dragPoint.getY(), mouseEvent.getPoint().getY());
            treePane.setDragRectangle(new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1));
            treePane.scrollPointToVisible(mouseEvent.getPoint());
        }
    }

    private TreePane treePane;

    private SelectionMode selectionMode = SelectionMode.CLADE;

    private DragMode dragMode = DragMode.SELECT;
    private Point2D dragPoint = null;
}
