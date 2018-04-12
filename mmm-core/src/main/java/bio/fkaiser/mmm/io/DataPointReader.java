package bio.fkaiser.mmm.io;

import bio.fkaiser.mmm.ItemsetMiner;
import bio.fkaiser.mmm.ItemsetMinerException;
import bio.fkaiser.mmm.io.DataPointReaderConfiguration.PDBSequenceCluster;
import bio.fkaiser.mmm.model.DataPoint;
import bio.fkaiser.mmm.model.DataPointIdentifier;
import bio.fkaiser.mmm.model.Item;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import de.bioforscher.singa.structure.model.interfaces.Ligand;
import de.bioforscher.singa.structure.model.interfaces.Model;
import de.bioforscher.singa.structure.model.interfaces.Structure;
import de.bioforscher.singa.structure.model.oak.StructuralEntityFilter;
import de.bioforscher.singa.structure.model.oak.Structures;
import de.bioforscher.singa.structure.parser.pdb.structures.SourceLocation;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser.LocalPDB;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser.MultiParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserException;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserOptions;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserOptions.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A reader for {@link DataPoint}s from PDB files. One needs to supply a {@link DataPointReaderConfiguration} object that specifies all configurations.
 * <p>
 *
 * @author fk
 */
public class DataPointReader {

    private static final Logger logger = LoggerFactory.getLogger(ItemsetMiner.class);
    private static final String PDB_CLUSTER_BASE_URL = "http://www.rcsb.org/pdb/rest/sequenceCluster?cluster=";
    private static final int MINIMAL_REQUIRED_STRUCTURES = 2;

    private final DataPointReaderConfiguration dataPointReaderConfiguration;
    private final List<String> labelWhiteList;

    private MultiParser multiParser;
    private StructureParserOptions structureParserOptions;
    private Predicate<LeafSubstructure> leafSubstructureFilter;

    public DataPointReader(DataPointReaderConfiguration dataPointReaderConfiguration, List<Path> structurePaths) {
        createStructureParserOptions();
        this.dataPointReaderConfiguration = dataPointReaderConfiguration;
        createLeafSubstructureFilter(dataPointReaderConfiguration);
        labelWhiteList = dataPointReaderConfiguration.getLigandLabelWhitelist();
        multiParser = StructureParser.local()
                                     .paths(structurePaths)
                                     .everything()
                                     .setOptions(structureParserOptions);
        logger.info("structure reader initialized with {} structures from paths", multiParser.getNumberOfQueuedStructures());
    }

    public DataPointReader(DataPointReaderConfiguration dataPointReaderConfiguration, Path chainListPath) {
        createStructureParserOptions();
        this.dataPointReaderConfiguration = dataPointReaderConfiguration;
        createLeafSubstructureFilter(dataPointReaderConfiguration);
        labelWhiteList = dataPointReaderConfiguration.getLigandLabelWhitelist();
        if (dataPointReaderConfiguration.getPdbLocation() != null) {
            multiParser = StructureParser.local()
                                         .localPDB(new LocalPDB(dataPointReaderConfiguration.getPdbLocation(), SourceLocation.OFFLINE_PDB))
                                         .chainList(chainListPath, dataPointReaderConfiguration.getChainListSeparator())
                                         .setOptions(structureParserOptions);
        } else {
            multiParser = StructureParser.pdb()
                                         .chainList(chainListPath, dataPointReaderConfiguration.getChainListSeparator())
                                         .setOptions(structureParserOptions);
        }
        logger.info("structure reader initialized with {} structures from chain list", multiParser.getNumberOfQueuedStructures());
    }

    public DataPointReader(DataPointReaderConfiguration dataPointReaderConfiguration, String inputChain) throws IOException {
        createStructureParserOptions();
        this.dataPointReaderConfiguration = dataPointReaderConfiguration;
        createLeafSubstructureFilter(dataPointReaderConfiguration);
        labelWhiteList = dataPointReaderConfiguration.getLigandLabelWhitelist();
        Path chainListPath = initializeFromSingleChain(inputChain);
        if (dataPointReaderConfiguration.getPdbLocation() != null) {
            multiParser = StructureParser.local()
                                         .localPDB(new LocalPDB(dataPointReaderConfiguration.getPdbLocation(), SourceLocation.OFFLINE_PDB))
                                         .chainList(chainListPath, dataPointReaderConfiguration.getChainListSeparator())
                                         .setOptions(structureParserOptions);
        } else {
            multiParser = StructureParser.pdb()
                                         .chainList(chainListPath, dataPointReaderConfiguration.getChainListSeparator())
                                         .setOptions(structureParserOptions);
        }
        logger.info("structure reader initialized with single chain {} as input, found {} representative chains in cluster {}", inputChain, multiParser.getNumberOfQueuedStructures(),
                    dataPointReaderConfiguration.getPdbSequenceCluster());
    }

    /**
     * Checks whether the label of the given {@link LeafSubstructure} is contained in the given white list for labels.
     *
     * @param leafSubstructureToCheck The {@link LeafSubstructure} to check.
     * @param labelWhiteList          The label white list.
     * @return True if label is whitelisted.
     */
    private static boolean hasValidLabel(LeafSubstructure<?> leafSubstructureToCheck, List<String> labelWhiteList) {
        return !(leafSubstructureToCheck instanceof Ligand) || labelWhiteList.contains(leafSubstructureToCheck.getFamily().getThreeLetterCode());
    }

    /**
     * Initializes the {@link MultiParser} from a single given chain, using the PDB REST service to obtain sequence clusters.
     *
     * @param inputChain The input chain in the format [chain-ID].[PDB-ID]
     */
    private Path initializeFromSingleChain(String inputChain) throws IOException {

        PDBSequenceCluster pdbSequenceCluster = dataPointReaderConfiguration.getPdbSequenceCluster();
        logger.info("obtaining PDB cluster for input chain {} and sequence cluster {}", inputChain, pdbSequenceCluster);
        String urlLocation = PDB_CLUSTER_BASE_URL + pdbSequenceCluster.getIdentity() + "&structureId=" + inputChain;
        logger.info("calling PDB REST: {}", urlLocation);

        URL url = new URL(urlLocation);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        Pattern linePattern = Pattern.compile("<pdbChain name=\"([0-9A-Z.]+)\" rank=\"(\\d+)\" />");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        // store already used ranks to avoid mining of homomeric structures
        Set<Integer> ranks = new TreeSet<>();
        StringJoiner stringJoiner = new StringJoiner("\n");
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            Matcher matcher = linePattern.matcher(line);
            if (matcher.matches()) {
                String chainSpecification = matcher.group(1);
                String pdbIdentifier = chainSpecification.split("\\.")[0].toLowerCase();
                String chainIdentifier = chainSpecification.split("\\.")[1];
                int rank = Integer.parseInt(matcher.group(2));
                if (!ranks.contains(rank)) {
                    stringJoiner.add(pdbIdentifier + "\t" + chainIdentifier);
                } else {
                    logger.debug("ignored duplicate homomeric entry {}_{}", pdbIdentifier, chainIdentifier);
                }
                ranks.add(rank);
            }
        }
        bufferedReader.close();
        conn.disconnect();

        Path chainListPath = Files.createTempFile("mmm_", ".txt");
        logger.info("writing temporary chain list to path {}", chainListPath);
        Files.write(chainListPath, stringJoiner.toString().getBytes());
        return chainListPath;
    }

    /**
     * Creates the {@link StructureParserOptions} to be used for the {@link StructureParser}.
     */
    private void createStructureParserOptions() {
        structureParserOptions = StructureParserOptions.withSettings(Setting.GET_LIGAND_INFORMATION, Setting.OMIT_HYDROGENS, Setting.GET_IDENTIFIER_FROM_FILENAME);
    }

    /**
     * Determines the {@link Predicate} to be used to filter {@link LeafSubstructure}s.
     *
     * @param dataPointReaderConfiguration The {@link DataPointReaderConfiguration} holding the desired filter information.
     */
    private void createLeafSubstructureFilter(DataPointReaderConfiguration dataPointReaderConfiguration) {
        leafSubstructureFilter = StructuralEntityFilter.LeafFilter.isAminoAcid();
        if (dataPointReaderConfiguration.isParseNucleotides()) {
            leafSubstructureFilter = leafSubstructureFilter.or(StructuralEntityFilter.LeafFilter.isNucleotide());
        }
        if (dataPointReaderConfiguration.isParseLigands()) {
            leafSubstructureFilter = leafSubstructureFilter.or(StructuralEntityFilter.LeafFilter.isLigand());
        }
        if (!dataPointReaderConfiguration.isParseWater()) {
            leafSubstructureFilter = leafSubstructureFilter.and(leafSubstructure -> !leafSubstructure.getFamily().getThreeLetterCode().equals("HOH"));
        }
        // ensure alpha carbon exists
        leafSubstructureFilter = leafSubstructureFilter.and(leafSubstructure -> leafSubstructure.getAtomByName("CA").isPresent());
    }

    /**
     * Reads the {@link DataPoint}s from the given input list.
     *
     * @return The {@link DataPoint}s.
     */
    public List<DataPoint<String>> readDataPoints() {
        int queuedStructures = multiParser.getNumberOfQueuedStructures();
        if (queuedStructures < MINIMAL_REQUIRED_STRUCTURES) {
            throw new ItemsetMinerException("at least " + MINIMAL_REQUIRED_STRUCTURES + " structures are required as input");
        }
        List<DataPoint<String>> dataPoints = new ArrayList<>();
        while (multiParser.hasNext()) {
            Structure structure;
            try {
                structure = multiParser.next();
            } catch (StructureParserException e) {
                logger.warn("failed to parse structure", e);
                continue;
            }
            if (Structures.isAlphaCarbonStructure(structure) || Structures.isBackboneStructure(structure)) {
                logger.warn("detected alpha carbon/backbone only structure, skipping {}", structure);
                continue;
            }
            dataPoints.add(toDataPoint(structure, structure.getPdbIdentifier(), structure.getFirstModel().getFirstChain().getChainIdentifier()));
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
        Model firstModel = structure.getFirstModel();
        List<Item<String>> items = new ArrayList<>();
        List<LeafSubstructure<?>> leafSubstructures = firstModel.getAllLeafSubstructures();
        for (LeafSubstructure<?> leafSubstructure : leafSubstructures) {
            if (leafSubstructureFilter.test(leafSubstructure) && hasValidLabel(leafSubstructure, labelWhiteList)) {
                Item<String> stringItem = toItem(leafSubstructure, leafSubstructures.indexOf(leafSubstructure));
                items.add(stringItem);
            }
        }
        DataPointIdentifier dataPointIdentifier = new DataPointIdentifier(pdbIdentifier, chainIdentifier);
        return new DataPoint<>(items, dataPointIdentifier);
    }

    /**
     * Converts the given {@link LeafSubstructure} to an {@link Item}.
     *
     * @param leafSubstructure         The {@link LeafSubstructure} to be converted.
     * @param consecutiveSequenceIndex The consecutive index of this {@link Item} in the {@link DataPoint}.
     * @return The converted {@link Item}.
     */
    private Item<String> toItem(LeafSubstructure<?> leafSubstructure, int consecutiveSequenceIndex) {
        if (dataPointReaderConfiguration.isConsecutiveSequenceNumbering()) {
            return new Item<>(leafSubstructure.getFamily().getThreeLetterCode(), leafSubstructure, consecutiveSequenceIndex);
        }
        return new Item<>(leafSubstructure.getFamily().getThreeLetterCode(), leafSubstructure);
    }
}