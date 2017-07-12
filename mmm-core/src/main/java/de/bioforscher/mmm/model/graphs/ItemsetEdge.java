package de.bioforscher.mmm.model.graphs;

import de.bioforscher.singa.mathematics.graphs.model.AbstractEdge;

/**
 * @author fk
 */
public class ItemsetEdge<LabelType extends Comparable<LabelType>> extends AbstractEdge<ItemsetNode<LabelType>> {

    public ItemsetEdge(int identifier) {
        super(identifier);
    }
}
