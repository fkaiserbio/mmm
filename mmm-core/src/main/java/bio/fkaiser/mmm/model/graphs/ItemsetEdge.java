package bio.fkaiser.mmm.model.graphs;

import bio.singa.mathematics.graphs.model.AbstractEdge;

/**
 * An implementation of an {@link bio.singa.mathematics.graphs.model.Edge} for the {@link ItemsetGraph}.
 *
 * @author fk
 */
public class ItemsetEdge<LabelType extends Comparable<LabelType>> extends AbstractEdge<ItemsetNode<LabelType>> {

    public ItemsetEdge(int identifier) {
        super(identifier);
    }
}
