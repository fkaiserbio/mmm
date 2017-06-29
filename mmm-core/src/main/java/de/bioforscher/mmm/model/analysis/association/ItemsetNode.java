package de.bioforscher.mmm.model.analysis.association;

import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.singa.mathematics.geometry.faces.Rectangle;
import de.bioforscher.singa.mathematics.graphs.model.AbstractNode;
import de.bioforscher.singa.mathematics.vectors.Vector2D;
import de.bioforscher.singa.mathematics.vectors.Vectors;

/**
 * @author fk
 */
public class ItemsetNode<LabelType extends Comparable<LabelType>> extends AbstractNode<ItemsetNode<LabelType>, Vector2D> {

    public static final Rectangle GRAPH_BOUNDING_BOX = new Rectangle(100, 100);

    private Itemset<LabelType> itemset;

    public ItemsetNode(int identifier, Itemset<LabelType> itemset) {
        super(identifier, Vectors.generateRandom2DVector(GRAPH_BOUNDING_BOX));
        this.itemset = itemset;
    }

    public Itemset<LabelType> getItemset() {
        return itemset;
    }
}
