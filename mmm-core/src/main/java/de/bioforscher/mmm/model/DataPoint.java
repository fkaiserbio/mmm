package de.bioforscher.mmm.model;

import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureWriter;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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

    public void writeAsPdb(Path pdbFilePath) throws IOException {

        List<LeafSubstructure<?, ?>> leafSubstructures = items.stream()
                                                              .map(Item::getLeafSubstructure)
                                                              .filter(Optional::isPresent)
                                                              .map(Optional::get)
                                                              .collect(Collectors.toList());

        StructureWriter.writeLeafSubstructures(leafSubstructures, pdbFilePath);
    }
}
