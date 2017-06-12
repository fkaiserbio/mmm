package de.bioforscher.mmm.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class DataPoint<LabelType extends Comparable<LabelType>> {
    private final DataPointIdentifier dataPointIdentifier;
    private List<Item<LabelType>> items;

    public DataPoint(List<Item<LabelType>> items, DataPointIdentifier dataPointIdentifier) {
        this.items = items;
        this.dataPointIdentifier = dataPointIdentifier;
    }

    public DataPointIdentifier getDataPointIdentifier() {
        return dataPointIdentifier;
    }

    public List<Item<LabelType>> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return items.stream()
                    .map(Item::toString)
                    .collect(Collectors.joining("-", dataPointIdentifier + "{", "}"));
    }
}
