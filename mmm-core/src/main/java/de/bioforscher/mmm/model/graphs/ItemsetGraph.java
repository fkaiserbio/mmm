package de.bioforscher.mmm.model.graphs;

import de.bioforscher.singa.mathematics.graphs.model.AbstractGraph;
import de.bioforscher.singa.mathematics.vectors.Vector2D;

/**
 * @author fk
 */
public class ItemsetGraph<LabelType extends Comparable<LabelType>> extends AbstractGraph<ItemsetNode<LabelType>, ItemsetEdge<LabelType>, Vector2D> {

    @Override
    public int addEdgeBetween(int identifier, ItemsetNode<LabelType> source, ItemsetNode<LabelType> target) {
        return addEdgeBetween(new ItemsetEdge<>(identifier), source, target);
    }

    @Override
    public int addEdgeBetween(ItemsetNode<LabelType> source, ItemsetNode<LabelType> target) {
        return addEdgeBetween(nextEdgeIdentifier(), source, target);
    }
}
