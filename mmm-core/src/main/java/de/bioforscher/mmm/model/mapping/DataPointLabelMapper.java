package de.bioforscher.mmm.model.mapping;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class DataPointLabelMapper<LabelType extends Comparable<LabelType>> {

    private MappingRule<LabelType> mappingRule;

    public DataPointLabelMapper(MappingRule<LabelType> mappingRule) {
        this.mappingRule = mappingRule;
    }

    public DataPoint<LabelType> mapDataPoint(DataPoint<LabelType> dataPoint) {
        List<Item<LabelType>> mappedItems = dataPoint.getItems().stream()
                                                          .map(mappingRule::mapItem)
                                                          .filter(Optional::isPresent)
                                                          .map(Optional::get)
                                                          .collect(Collectors.toList());
        return new DataPoint<>(mappedItems, dataPoint.getDataPointIdentifier());
    }
}
