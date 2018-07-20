package bio.fkaiser.mmm.model.graphs;

import bio.singa.mathematics.graphs.model.AbstractMapGraph;
import bio.singa.mathematics.vectors.Vector2D;

/**
 * A {@link bio.singa.mathematics.graphs.model.Graph} for {@link bio.fkaiser.mmm.model.Itemset}s.
 *
 * @author fk
 */
public class ItemsetGraph<LabelType extends Comparable<LabelType>> extends AbstractMapGraph<ItemsetNode<LabelType>, ItemsetEdge<LabelType>, Vector2D, Integer> {

    private int nextNodeIdentifier;

    @Override
    public int addEdgeBetween(int identifier, ItemsetNode<LabelType> source, ItemsetNode<LabelType> target) {
        return addEdgeBetween(new ItemsetEdge<>(identifier), source, target);
    }

    @Override
    public Integer nextNodeIdentifier() {
        return nextNodeIdentifier++;
    }

    @Override
    public int addEdgeBetween(ItemsetNode<LabelType> source, ItemsetNode<LabelType> target) {
        return addEdgeBetween(nextEdgeIdentifier(), source, target);
    }
}
