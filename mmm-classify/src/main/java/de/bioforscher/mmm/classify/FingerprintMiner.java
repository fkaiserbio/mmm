package de.bioforscher.mmm.classify;

import de.bioforscher.mmm.ItemsetMiner;
import de.bioforscher.mmm.ItemsetMinerRunner;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.singa.chemistry.algorithms.superimposition.SubstructureSuperimposition;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusAlignment;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusContainer;
import de.bioforscher.singa.chemistry.algorithms.superimposition.fit3d.Fit3D;
import de.bioforscher.singa.chemistry.algorithms.superimposition.fit3d.Fit3DBuilder;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser.LocalPDB;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser.MultiParser;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParserOptions;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureWriter;
import de.bioforscher.singa.chemistry.physical.atoms.Atom;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import de.bioforscher.singa.chemistry.physical.families.AminoAcidFamily;
import de.bioforscher.singa.chemistry.physical.leaves.AminoAcid;
import de.bioforscher.singa.chemistry.physical.leaves.AtomContainer;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;
import de.bioforscher.singa.chemistry.physical.model.StructuralEntityFilter.AtomFilter;
import de.bioforscher.singa.mathematics.graphs.trees.BinaryTree;
import de.bioforscher.singa.mathematics.graphs.trees.BinaryTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author fk
 *         TODO construct from JSON configuration object
 */
public class FingerprintMiner {

    private static final Logger logger = LoggerFactory.getLogger(FingerprintMiner.class);

    private static final double RMSD_CUTOFF = 1.5;
    private static final Predicate<Itemset<?>> TOP_SCORING_FILTER = itemset -> itemset.getItems().size() >= 3;
    private static final Comparator<Itemset<?>> TOP_SCORING_COMPARATOR = Comparator.comparing(Itemset::getConsensus);
    private static final int TOP_SCORING_LIMIT = 3;
    private static final LocalPDB LOCAL_PDB = new LocalPDB("/srv/pdb");

    private final ItemsetMinerConfiguration<String> itemsetMinerConfiguration;
    private final Path familyChainListPath;
    private final Path blacklistChainListPath;
    private final Path decoyChainListPath;
    private final Map<Itemset<String>, BinaryTree<ConsensusContainer>> topScoringClusters;
    private final Map<Itemset<String>, List<List<LeafSubstructure<?, ?>>>> decoyDatasets;
    private ItemsetMiner<String> itemsetMiner;
    private List<Itemset<String>> topScoringItemsets;
    private StructureParserOptions structureParserOptions;

    public FingerprintMiner(ItemsetMinerConfiguration<String> itemsetMinerConfiguration, Path familyChainListPath, Path blacklistChainListPath, Path decoyChainListPath) throws
                                                                                                                                                                         IOException,
                                                                                                                                                                         URISyntaxException,
                                                                                                                                                                         FingerprintMinerException {
        this.itemsetMinerConfiguration = itemsetMinerConfiguration;
        this.familyChainListPath = familyChainListPath;
        this.blacklistChainListPath = blacklistChainListPath;
        this.decoyChainListPath = decoyChainListPath;
        topScoringClusters = new HashMap<>();
        decoyDatasets = new HashMap<>();

        // create structure parser options for Fit3D search
        structureParserOptions = new StructureParserOptions();
        structureParserOptions.omitHydrogens(true);
        structureParserOptions.retrieveLigandInformation(false);
        structureParserOptions.createEdges(false);

        mineFamily();
        selectTopScoringItemsets();
        createDecoy();
        outputResults();
    }

    public static void main(String[] args) throws URISyntaxException, IOException, FingerprintMinerException {
        logger.info("loading base configuration");
        URL baseConfigurationResource = Thread.currentThread().getContextClassLoader()
                                              .getResource("base_configuration.json");
        if (baseConfigurationResource == null) {
            throw new FingerprintMinerException("failed to load base configuration");
        }

        logger.info("loading decoy chain list");
        URL decoyChainListResource = Thread.currentThread().getContextClassLoader()
                                           .getResource("nrPDB_chains_BLAST_10e80");
        if (decoyChainListResource == null) {
            throw new FingerprintMinerException("failed to load decoy chain list");
        }

        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = ItemsetMinerConfiguration.from(Paths.get(baseConfigurationResource.toURI()));

        // use all given input lists in directory
        String inputListsLocation = args[0];
        String blackListsLocation = args[1];
        String outputLocation = args[2];
        Path inputListsPath = Paths.get(inputListsLocation);
        Path blackListsPath = Paths.get(blackListsLocation);
        List<Path> inputLists = Files.list(inputListsPath).collect(Collectors.toList());
        for (Path inputList : inputLists) {
            logger.info("mining family {}", inputList);
            itemsetMinerConfiguration.setInputListLocation(inputList.toString());
            Path familyOutputLocation = Paths.get(outputLocation).resolve(inputList.getFileName()).resolve("itemset-miner");
            itemsetMinerConfiguration.setOutputLocation(familyOutputLocation.toString());
            // assemble path to blacklist
            Path blackList = blackListsPath.resolve(inputList.getFileName() + "_blacklist");
            // run fingerprint miner
            try {
                new FingerprintMiner(itemsetMinerConfiguration, inputList, blackList, Paths.get(decoyChainListResource.toURI()));
            } catch (IOException | URISyntaxException | FingerprintMinerException e) {
                logger.warn("failed to mine family {}", inputList, e);
            }
        }
    }

    private void outputResults() throws IOException {
        for (Itemset<String> topScoringItemset : topScoringItemsets) {
            logger.info("writing results for {}", topScoringItemset);
            if (!decoyDatasets.containsKey(topScoringItemset)) {
                logger.warn("no decoy dataset could be generated for {}", topScoringItemset);
                continue;
            }
            Path decoyOutputPath = Paths.get(itemsetMinerConfiguration.getOutputLocation()).getParent().resolve(topScoringItemset.toSimpleString()).resolve("decoy");
            Files.createDirectories(decoyOutputPath);
            for (List<LeafSubstructure<?, ?>> leafSubstructures : decoyDatasets.get(topScoringItemset)) {
                StructuralMotif decoyMotif = StructuralMotif.fromLeaves(leafSubstructures);
                StructureWriter.writeBranchSubstructure(decoyMotif, decoyOutputPath.resolve(decoyMotif.toString() + ".pdb"));
            }
            Path fingerprintOutputPath = Paths.get(itemsetMinerConfiguration.getOutputLocation()).getParent().resolve(topScoringItemset.toSimpleString()).resolve("fingerprint");
            Files.createDirectories(fingerprintOutputPath);
            List<StructuralMotif> fingerprintMotifs = topScoringClusters.get(topScoringItemset).getLeafNodes().stream()
                                                                        .map(BinaryTreeNode::getData)
                                                                        .map(ConsensusContainer::getSuperimposition)
                                                                        .map(SubstructureSuperimposition::getMappedFullCandidate)
                                                                        .map(StructuralMotif::fromLeaves)
                                                                        .collect(Collectors.toList());
            for (StructuralMotif fingerprintMotif : fingerprintMotifs) {
                StructureWriter.writeBranchSubstructure(fingerprintMotif, fingerprintOutputPath.resolve(fingerprintMotif.toString() + ".pdb"));
            }
        }
    }

    private void selectTopScoringItemsets() throws FingerprintMinerException {
        logger.info("selecting top-scoring itemsets");
        topScoringItemsets = itemsetMiner.getTotalItemsets().stream()
                                         .filter(TOP_SCORING_FILTER)
                                         .sorted(TOP_SCORING_COMPARATOR)
                                         .limit(TOP_SCORING_LIMIT)
                                         .collect(Collectors.toList());
        logger.info("top-scoring itemsets are \n{}", topScoringItemsets);
        logger.info("found {} top-scoring itemsets", topScoringItemsets.size());
        if (topScoringItemsets.isEmpty()) {
            throw new FingerprintMinerException("no top-scoring itemsets found");
        }
    }

    private void createDecoy() throws IOException, FingerprintMinerException {
        logger.info("creating decoy dataset using Fit3D");
        for (Itemset<String> topScoringItemset : topScoringItemsets) {
            ConsensusAlignment consensusAlignment = itemsetMiner.getTotalClusteredItemsets().get(topScoringItemset);
            // get consensus motif of largest cluster
            consensusAlignment.getClusters().sort(Comparator.comparing(binaryTree -> binaryTree.getLeafNodes().size()));
            BinaryTree<ConsensusContainer> largestCluster = consensusAlignment.getClusters().get(consensusAlignment.getClusters().size() - 1);

            // store largest cluster
            topScoringClusters.put(topScoringItemset, largestCluster);

            logger.info("largest cluster contains {} itemset observations", largestCluster.getLeafNodes().size());
            // convert consensus motif to artificial amino acid motif
            StructuralMotif consensusMotif = largestCluster.getRoot().getData().getStructuralMotif();
            StructuralMotif artificialSearchMotif = convertConsensusMotif(consensusMotif);

            logger.info("running Fit3D search for {} in decoy dataset", topScoringItemset);
            // create multi-parser for decoy data set
            MultiParser multiParser = StructureParser.local()
                                                     .localPDB(LOCAL_PDB)
                                                     .chainList(decoyChainListPath)
                                                     .setOptions(structureParserOptions);
            // run Fit3D search
            Fit3D fit3d = Fit3DBuilder.create()
                                      .query(artificialSearchMotif)
                                      .targets(multiParser)
                                      .maximalParallelism()
                                      .atomFilter(AtomFilter.isArbitrary())
                                      .rmsdCutoff(RMSD_CUTOFF)
                                      .run();

            List<List<LeafSubstructure<?, ?>>> decoyDataset = fit3d.getMatches().values().stream()
                                                                   .map(SubstructureSuperimposition::getMappedFullCandidate)
                                                                   .collect(Collectors.toList());

            // read black list
            List<String> blacklistContent = Files.readAllLines(blacklistChainListPath);

            // filter blacklisted entries
            for (Iterator<List<LeafSubstructure<?, ?>>> iterator = decoyDataset.iterator(); iterator.hasNext(); ) {
                List<LeafSubstructure<?, ?>> decoy = iterator.next();
                String pdbIdentifier = decoy.iterator().next().getPdbIdentifier();
                String chainIdentifier = decoy.iterator().next().getChainIdentifier();
                boolean blacklisted = blacklistContent.stream()
                                                      .anyMatch(line -> line.equals(pdbIdentifier + "\t" + chainIdentifier));
                if (blacklisted) {
                    logger.info("found blacklisted entry {}_{} in decoy dataset, removing", pdbIdentifier, chainIdentifier);
                    iterator.remove();
                }
            }

            if (decoyDataset.isEmpty()) {
                logger.warn("no decoys could be found for itemset {}", topScoringItemset);
                continue;
            }

            logger.info("decoy dataset for itemset {} contains {} entries", topScoringItemset, decoyDataset.size());
            decoyDatasets.put(topScoringItemset, decoyDataset);
        }
    }

    /**
     * Converts the given consensus motif, which is composed of {@link AtomContainer}s, to a {@link StructuralMotif} composed of {@link AminoAcid}s.
     *
     * @param consensusMotif The artificial consensus motif.
     * @return A {@link StructuralMotif} composed of {@link AminoAcid}s.
     */
    private StructuralMotif convertConsensusMotif(StructuralMotif consensusMotif) {
        List<LeafSubstructure<?, ?>> aminoAcids = new ArrayList<>();
        for (LeafSubstructure<?, ?> leafSubstructure : consensusMotif.getLeafSubstructures()) {
            Optional<AminoAcidFamily> aminoAcidFamily = AminoAcidFamily.getAminoAcidTypeByOneLetterCode(leafSubstructure.getFamily().getOneLetterCode());
            if (aminoAcidFamily.isPresent()) {
                AminoAcid aminoAcid = new AminoAcid(leafSubstructure.getLeafIdentifier(), aminoAcidFamily.get());
                leafSubstructure.getAllAtoms().stream()
                                .map(Atom::getCopy)
                                .forEach(aminoAcid::addNode);
                aminoAcids.add(aminoAcid);
            }
        }
        return StructuralMotif.fromLeaves(aminoAcids);
    }

    private void mineFamily() throws IOException, URISyntaxException {
        itemsetMiner = new ItemsetMinerRunner(itemsetMinerConfiguration).getItemsetMiner();
    }
}