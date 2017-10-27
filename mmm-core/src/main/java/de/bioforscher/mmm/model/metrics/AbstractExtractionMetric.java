package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.DataPointCache;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.representations.RepresentationSchemeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract {@link ExtractionDependentMetric} that provides common functionality for {@link ExtractionMetric}s.
 *
 * @author fk
 */
public abstract class AbstractExtractionMetric<LabelType extends Comparable<LabelType>> extends DataPointCache<LabelType> implements ExtractionMetric<LabelType> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractExtractionMetric.class);

    protected final List<DataPoint<LabelType>> dataPoints;
    Map<Itemset<LabelType>, List<Itemset<LabelType>>> extractedItemsets;

    AbstractExtractionMetric(List<DataPoint<LabelType>> dataPoints, RepresentationSchemeType representationSchemeType) {
        super(representationSchemeType);
        this.dataPoints = dataPoints;
        extractedItemsets = new HashMap<>();
    }

    @Override
    public RepresentationSchemeType getRepresentationSchemeType() {
        return representationSchemeType;
    }

    public Map<Itemset<LabelType>, List<Itemset<LabelType>>> getExtractedItemsets() {
        return extractedItemsets;
    }

    protected abstract void filterExtractedItemsets();

    synchronized void addToExtractedItemsets(Itemset<LabelType> itemset, Itemset<LabelType> extractedItemset) {
        if (extractedItemsets.containsKey(itemset)) {
            extractedItemsets.get(itemset).add(extractedItemset);
        } else {
            List<Itemset<LabelType>> itemsets = new ArrayList<>();
            itemsets.add(extractedItemset);
            extractedItemsets.put(itemset, itemsets);
        }
    }
}

