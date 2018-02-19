package bio.fkaiser.mmm.model.graphs;

import de.bioforscher.singa.mathematics.graphs.model.AbstractEdge;

/**
 * An implementation of an {@link de.bioforscher.singa.mathematics.graphs.model.Edge} for the {@link ItemsetGraph}.
 *
 * @author fk
 */
public class ItemsetEdge<LabelType extends Comparable<LabelType>> extends AbstractEdge<ItemsetNode<LabelType>> {

    public ItemsetEdge(int identifier) {
        super(identifier);
    }
}
