package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.Itemset;

import java.util.List;
import java.util.Map;

/**
 * Abstract {@link ExtractionDependentMetric} that holds extracted {@link Itemset}s.
 *
 * @author fk
 */
public abstract class AbstractExtractionDependentMetric<LabelType extends Comparable<LabelType>> implements ExtractionDependentMetric<LabelType> {
    protected Map<Itemset<LabelType>, List<Itemset<LabelType>>> extractedItemsets;
}
