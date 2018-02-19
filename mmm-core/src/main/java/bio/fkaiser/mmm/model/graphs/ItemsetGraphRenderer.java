package bio.fkaiser.mmm.model.graphs;

import de.bioforscher.singa.javafx.renderer.graphs.GraphRenderer;

/**
 * A customized {@link GraphRenderer} to display {@link ItemsetGraph}s.
 *
 * @author fk
 */
public class ItemsetGraphRenderer<LabelType extends Comparable<LabelType>> extends GraphRenderer<ItemsetNode<LabelType>, ItemsetEdge<LabelType>, Integer, ItemsetGraph<LabelType>> {

    @Override
    protected void drawNode(ItemsetNode<LabelType> node) {
        // draw node
        getGraphicsContext().setFill(getRenderingOptions().getNodeColor());
        drawPoint(node.getPosition(), getRenderingOptions().getNodeDiameter());
        // draw outline
        getGraphicsContext().setStroke(getRenderingOptions().getEdgeColor());
        circlePoint(node.getPosition(), getRenderingOptions().getNodeDiameter());
        // draw text
        getGraphicsContext().setFill(getRenderingOptions().getIdentifierTextColor());
        drawTextCenteredOnPoint(node.getItemset().toSimpleString(), node.getPosition());
    }
}
