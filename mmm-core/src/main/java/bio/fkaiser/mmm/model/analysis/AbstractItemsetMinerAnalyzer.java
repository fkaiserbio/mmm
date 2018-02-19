package bio.fkaiser.mmm.model.analysis;

import bio.fkaiser.mmm.ItemsetMiner;

/**
 * An abstract class that should be extended by classes analyzing the results of an {@link ItemsetMiner} run.
 *
 * @author fk
 */
public abstract class AbstractItemsetMinerAnalyzer<LabelType extends Comparable<LabelType>> implements ItemsetMinerAnalyzer<LabelType> {

    protected final ItemsetMiner<LabelType> itemsetMiner;

    public AbstractItemsetMinerAnalyzer(ItemsetMiner<LabelType> itemsetMiner) {
        this.itemsetMiner = itemsetMiner;
    }
}
