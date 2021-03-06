package jebl.gui.trees.treeviewer.decorators;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id: AttributeBranchDecorator.java 181 2006-01-23 17:31:10Z rambaut $
 */
public class AttributeBranchDecorator implements BranchDecorator {
    public AttributeBranchDecorator(String attributeName, Map<Object, Paint> paintMap) {
        this.attributeName = attributeName;
        this.paintMap = paintMap;
    }

    public Paint getBranchPaint(jebl.evolution.trees.Tree tree, jebl.evolution.graphs.Node node) {
        Paint paint = getPaint(node.getAttribute(attributeName));
        if (paint == null) return Color.BLACK;
        return paint;
    }

    protected Paint getPaint(Object value) {
        if (value != null) {
            return paintMap.get(value);
        }
        return null;
    }

    protected final String attributeName;

    protected Map<Object, Paint> paintMap = new HashMap<Object, Paint>();
}
