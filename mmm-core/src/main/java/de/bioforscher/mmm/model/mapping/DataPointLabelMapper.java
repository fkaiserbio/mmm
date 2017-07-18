package de.bioforscher.mmm.model.mapping;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class that uses a {@link MappingRule} to map the {@link Item}s of {@link DataPoint}s to new labels.
 *
 * @author fk
 */
public class DataPointLabelMapper<LabelType extends Comparable<LabelType>> {

    private List<MappingRule<LabelType>> mappingRules;

    public DataPointLabelMapper(MappingRule<LabelType> mappingRule) {
        mappingRules = Stream.of(mappingRule).collect(Collectors.toList());
    }

    public DataPointLabelMapper(List<MappingRule<LabelType>> mappingRules) {
        this.mappingRules = mappingRules;
    }

    /**
     * Maps the given {@link DataPoint} according to the {@link MappingRule}.
     * <b>NOTE: This operation is not in place, a new {@link DataPoint} will be returned.</b>
     *
     * @param dataPoint The {@link DataPoint} to be mapped.
     * @return A new {@link DataPoint} with mapped {@link Item}s.
     */
    public DataPoint<LabelType> mapDataPoint(DataPoint<LabelType> dataPoint) {
        List<Item<LabelType>> mappedItems = dataPoint.getItems();
        for (MappingRule<LabelType> mappingRule : mappingRules) {
            mappedItems = mappedItems.stream()
                                     .map(mappingRule::mapItem)
                                     .filter(Optional::isPresent)
                                     .map(Optional::get)
                                     .collect(Collectors.toList());
        }
        return new DataPoint<>(mappedItems, dataPoint.getDataPointIdentifier());
    }
}
