package de.bioforscher.mmm.io;

import de.bioforscher.mmm.ItemsetMiner;
import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.DataPointIdentifier;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser.MultiParser;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParserOptions;
import de.bioforscher.singa.chemistry.physical.branches.StructuralModel;
import de.bioforscher.singa.chemistry.physical.leaves.AminoAcid;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;
import de.bioforscher.singa.chemistry.physical.model.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A reader for {@link DataPoint}s from PDB files. One needs to supply a list of PDB-IDs and chain IDs in the format [PDB-ID]_[chain-ID] separated by line breaks.
 * <p>
 * @author fk
 */
public class DataPointReader {

    private static final Logger logger = LoggerFactory.getLogger(ItemsetMiner.class);

    private final MultiParser multiParser;
    private final StructureParserOptions structureParserOptions;

    public DataPointReader(StructureParserOptions structureParserOptions, Path chainListPath, String separator) {
        this.structureParserOptions = structureParserOptions;
        multiParser = StructureParser.online()
                                     .chainList(chainListPath, separator)
                                     .setOptions(structureParserOptions);
        logger.info("structure reader initialized with {} structures", multiParser.getNumberOfQueuedStructures());
    }

    /**
     * Reads the {@link DataPoint}s from the given input list.
     *
     * @return The {@link DataPoint}s.
     */
    public List<DataPoint<String>> readDataPoints() {
        int queuedStructures = multiParser.getNumberOfQueuedStructures();
        List<DataPoint<String>> dataPoints = new ArrayList<>();
        while (multiParser.hasNext()) {
            dataPoints.add(toDataPoint(multiParser.next(), multiParser.getCurrentPdbIdentifier(), multiParser.getCurrentChainIdentifier()));
            int remainingStructures = multiParser.getNumberOfRemainingStructures();
            if (remainingStructures % 10 == 0) {
                logger.info("read {} out of {} structures", queuedStructures - remainingStructures, queuedStructures);
            }
        }
        return dataPoints;
    }

    /**
     * Converts the given {@link Structure} to a {@link DataPoint}.
     *
     * @param structure       The {@link Structure} to be converted.
     * @param pdbIdentifier   The PDB identifier of the {@link Structure}.
     * @param chainIdentifier The chain identifier of the {@link Structure};
     * @return A new {@link DataPoint}.
     */
    private DataPoint<String> toDataPoint(Structure structure, String pdbIdentifier, String chainIdentifier) {
        // FIXME only consider first model
        if (structure.getAllModels().size() > 1) {
            logger.info("multi-model structure {} detected, using only first model", structure);
        }
        StructuralModel firstModel = structure.getFirstModel().orElseThrow(() -> new RuntimeException("no model found for structure " + structure));
        List<Item<String>> items = firstModel.getLeafSubstructures().stream()
                                             // FIXME only amino acids are considered
                                             .filter(AminoAcid.class::isInstance)
                                             .map(this::toItem)
                                             .collect(Collectors.toList());
        DataPointIdentifier dataPointIdentifier = new DataPointIdentifier(pdbIdentifier, chainIdentifier);
        return new DataPoint<>(items, dataPointIdentifier);
    }

    private Item<String> toItem(LeafSubstructure<?, ?> leafSubstructure) {
        return new Item<>(leafSubstructure.getFamily().getThreeLetterCode(), leafSubstructure);
    }
}
