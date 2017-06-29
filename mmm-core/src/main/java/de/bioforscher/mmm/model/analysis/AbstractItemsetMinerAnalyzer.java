package de.bioforscher.mmm.model.analysis;

import de.bioforscher.mmm.ItemsetMiner;

/**
 * @author fk
 */
public abstract class AbstractItemsetMinerAnalyzer<LabelType extends Comparable<LabelType>> implements ItemsetMinerAnalyzer<LabelType> {

    protected final ItemsetMiner<LabelType> itemsetMiner;

    public AbstractItemsetMinerAnalyzer(ItemsetMiner<LabelType> itemsetMiner) {
        this.itemsetMiner = itemsetMiner;
    }
}
