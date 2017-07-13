package de.bioforscher.mmm.model.enrichment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.bioforscher.mmm.model.DataPoint;

/**
 * An interface implemented to enrich {@link DataPoint}s.
 *
 * @author fk
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = IntraChainInteractionEnricher.class), @JsonSubTypes.Type(value = LigandInteractionEnricher.class)})
public interface DataPointEnricher<LabelType extends Comparable<LabelType>> {

    void enrichDataPoint(DataPoint<LabelType> dataPoint);
}
