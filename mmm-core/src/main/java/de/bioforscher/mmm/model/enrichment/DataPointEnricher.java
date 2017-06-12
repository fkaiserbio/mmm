package de.bioforscher.mmm.model.enrichment;

import de.bioforscher.mmm.model.DataPoint;

/**
 * @author fk
 */
public interface DataPointEnricher<LabelType extends Comparable<LabelType>> {

    void enrichDataPoint(DataPoint<LabelType> dataPoint);
}
