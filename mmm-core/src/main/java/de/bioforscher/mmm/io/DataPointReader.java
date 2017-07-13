package de.bioforscher.mmm.io;

import de.bioforscher.mmm.ItemsetMiner;
import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.DataPointIdentifier;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser.MultiParser;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParserOptions;
import de.bioforscher.singa.chemistry.physical.branches.StructuralModel;
import de.bioforscher.singa.chemistry.physical.leaves.AtomContainer;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;
import de.bioforscher.singa.chemistry.physical.model.StructuralEntityFilter.LeafFilter;
import de.bioforscher.singa.chemistry.physical.model.Structure;
import de.bioforscher.singa.chemistry.physical.model.Structures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A reader for {@link DataPoint}s from PDB files. One needs to supply a {@link DataPointReaderConfiguration} object that specifies all configurations.
 * <p>
 *
 * @author fk
 */
public class DataPointReader {

    private static final Logger logger = LoggerFactory.getLogger(ItemsetMiner.class);

    private MultiParser multiParser;
    private StructureParserOptions structureParserOptions;
    private Predicate<LeafSubstructure<?, ?>> leafSubstructureFilter;
    private List<String> labelWhiteList;

    public DataPointReader(DataPointReaderConfiguration dataPointReaderConfiguration, List<Path> structurePaths) {
        createStructureParserOptions();
        multiParser = StructureParser.local()
                                     .paths(structurePaths)
                                     .everything()
                                     .setOptions(structureParserOptions);
        createLeafSubstructureFilter(dataPointReaderConfiguration);
        labelWhiteList = dataPointReaderConfiguration.getLigandLabelWhitelist();
        logger.info("structure reader initialized with {} structures from paths", multiParser.getNumberOfQueuedStructures());
    }

    public DataPointReader(DataPointReaderConfiguration dataPointReaderConfiguration, Path chainListPath) {
        createStructureParserOptions();

        if (dataPointReaderConfiguration.getPdbLocation() != null) {
            multiParser = StructureParser.local()
                                         .localPDB(new StructureParser.LocalPDB(dataPointReaderConfiguration.getPdbLocation()))
                                         .chainList(chainListPath, dataPointReaderConfiguration.getChainListSeparator())
                                         .setOptions(structureParserOptions);
        } else {
            multiParser = StructureParser.online()
                                         .chainList(chainListPath, dataPointReaderConfiguration.getChainListSeparator())
                                         .setOptions(structureParserOptions);
        }
        createLeafSubstructureFilter(dataPointReaderConfiguration);
        labelWhiteList = dataPointReaderConfiguration.getLigandLabelWhitelist();
        logger.info("structure reader initialized with {} structures from chain list", multiParser.getNumberOfQueuedStructures());
    }

    public static boolean hasValidLabel(LeafSubstructure<?, ?> leafSubstructureToCheck, List<String> labelWhiteList) {
        return !(leafSubstructureToCheck instanceof AtomContainer) || labelWhiteList.contains(leafSubstructureToCheck.getFamily().getThreeLetterCode());
    }

    private void createStructureParserOptions() {
        // create structure parser options
        structureParserOptions = new StructureParserOptions();
        structureParserOptions.retrieveLigandInformation(true);
        structureParserOptions.omitHydrogens(true);
    }

    /**
     * Determines the {@link Predicate} to be used to filter {@link LeafSubstructure}s.
     *
     * @param dataPointReaderConfiguration The {@link DataPointReaderConfiguration} holding the desired filter information.
     */
    private void createLeafSubstructureFilter(DataPointReaderConfiguration dataPointReaderConfiguration) {
        leafSubstructureFilter = LeafFilter.isAminoAcid();
        if (dataPointReaderConfiguration.isParseNucleotides()) {
            leafSubstructureFilter = leafSubstructureFilter.or(LeafFilter.isNucleotide());
        }
        if (dataPointReaderConfiguration.isParseLigands()) {
            leafSubstructureFilter = leafSubstructureFilter.or(LeafFilter.isAtomContainer());
        }
        if (!dataPointReaderConfiguration.isParseWater()) {
            leafSubstructureFilter = leafSubstructureFilter.and(leafSubstructure -> !leafSubstructure.getFamily().getThreeLetterCode().equals("HOH"));
        }
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
            Structure structure = multiParser.next();
            if (Structures.isAlphaCarbonStructure(structure) || Structures.isBackboneStructure(structure)) {
                logger.warn("detected alpha carbon/backbone only structure, skipping {}", structure);
                continue;
            }
            dataPoints.add(toDataPoint(structure, structure.getPdbIdentifier(), structure.getFirstModel().get().getFirstChain().get().getChainIdentifier()));
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
        // only consider first model
        if (structure.getAllModels().size() > 1) {
            logger.info("multi-model structure {} detected, using only first model", structure);
        }
        StructuralModel firstModel = structure.getFirstModel().orElseThrow(() -> new RuntimeException("no model found for structure " + structure));
        List<Item<String>> items = firstModel.getLeafSubstructures().stream()
                                             .filter(leafSubstructureFilter)
                                             .filter(leafSubstructure -> leafSubstructureFilter.test(leafSubstructure) &&
                                                                         hasValidLabel(leafSubstructure, labelWhiteList))
                                             .map(this::toItem)
                                             .collect(Collectors.toList());
        DataPointIdentifier dataPointIdentifier = new DataPointIdentifier(pdbIdentifier, chainIdentifier);
        return new DataPoint<>(items, dataPointIdentifier);
    }

    private Item<String> toItem(LeafSubstructure<?, ?> leafSubstructure) {
        return new Item<>(leafSubstructure.getFamily().getThreeLetterCode(), leafSubstructure);
    }
}
