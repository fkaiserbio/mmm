package de.bioforscher.mmm.model;

import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureWriter;
import de.bioforscher.singa.chemistry.parser.plip.InteractionType;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A representation of one data point, i.e. macromolecular structure.
 *
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

    /**
     * Writes the {@link DataPoint} to the given {@link Path} in PDB format.
     *
     * @param pdbFilePath The target {@link Path} of the PDB file.
     * @throws IOException If the {@link DataPoint} cannot be written.
     */
    public void writeAsPdb(Path pdbFilePath) throws IOException {

        List<LeafSubstructure<?, ?>> leafSubstructures = items.stream()
                .map(Item::getLeafSubstructure)
                .filter(Optional::isPresent)
                .map(Optional::get)
                // ignore interaction representations when writing data points
                .filter(leafSubstructure -> Arrays.stream(InteractionType.values())
                        .noneMatch(interactionType -> interactionType.getThreeLetterCode()
                                .equalsIgnoreCase(leafSubstructure.getFamily()
                                        .getThreeLetterCode())))
                .collect(Collectors.toList());

        StructureWriter.writeLeafSubstructures(leafSubstructures, pdbFilePath);
    }
}
