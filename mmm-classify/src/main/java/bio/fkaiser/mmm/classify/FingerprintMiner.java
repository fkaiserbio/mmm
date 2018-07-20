package bio.fkaiser.mmm.classify;

import bio.fkaiser.mmm.ItemsetMiner;
import bio.fkaiser.mmm.ItemsetMinerRunner;
import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.configurations.ItemsetMinerConfiguration;
import bio.singa.mathematics.graphs.trees.BinaryTree;
import bio.singa.mathematics.graphs.trees.BinaryTreeNode;
import bio.singa.structure.algorithms.superimposition.SubstructureSuperimposition;
import bio.singa.structure.algorithms.superimposition.consensus.ConsensusAlignment;
import bio.singa.structure.algorithms.superimposition.consensus.ConsensusContainer;
import bio.singa.structure.algorithms.superimposition.fit3d.Fit3D;
import bio.singa.structure.algorithms.superimposition.fit3d.Fit3DBuilder;
import bio.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;
import bio.singa.structure.model.families.AminoAcidFamily;
import bio.singa.structure.model.interfaces.AminoAcid;
import bio.singa.structure.model.interfaces.LeafSubstructure;
import bio.singa.structure.model.oak.OakAminoAcid;
import bio.singa.structure.model.oak.StructuralEntityFilter;
import bio.singa.structure.model.oak.StructuralMotif;
import bio.singa.structure.parser.pdb.structures.SourceLocation;
import bio.singa.structure.parser.pdb.structures.StructureParser;
import bio.singa.structure.parser.pdb.structures.StructureParser.LocalPDB;
import bio.singa.structure.parser.pdb.structures.StructureParser.MultiParser;
import bio.singa.structure.parser.pdb.structures.StructureParserOptions;
import bio.singa.structure.parser.pdb.structures.StructureWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class FingerprintMiner {

    private static final Logger logger = LoggerFactory.getLogger(FingerprintMiner.class);

    private static final double RMSD_CUTOFF = 1.5;
    private static final Predicate<Itemset<?>> TOP_SCORING_FILTER = itemset -> itemset.getItems().size() >= 3;
    private static final Comparator<Itemset<?>> TOP_SCORING_COMPARATOR = Comparator.comparing(Itemset::getConsensus);
    private static final int TOP_SCORING_LIMIT = 3;
    private static final LocalPDB LOCAL_PDB = new LocalPDB("/srv/pdb", SourceLocation.OFFLINE_PDB);

    private final ItemsetMinerConfiguration<String> itemsetMinerConfiguration;
    private final Path familyChainListPath;
    private final Path blacklistChainListPath;
    private final Path decoyChainListPath;
    private final Map<Itemset<String>, BinaryTree<ConsensusContainer>> topScoringClusters;
    private final Map<Itemset<String>, List<List<LeafSubstructure<?>>>> decoyDatasets;
    private ItemsetMiner<String> itemsetMiner;
    private List<Itemset<String>> topScoringItemsets;
    private StructureParserOptions structureParserOptions;

    public FingerprintMiner(FingerprintMinerConfiguration fingerprintMinerConfiguration) throws IOException, URISyntaxException, FingerprintMinerException {
        this.itemsetMinerConfiguration = fingerprintMinerConfiguration.getItemsetMinerConfiguration();
        this.familyChainListPath = Paths.get(fingerprintMinerConfiguration.getInputListLocation());
        this.blacklistChainListPath = Paths.get(fingerprintMinerConfiguration.getBlacklistLocation());
        this.decoyChainListPath = Paths.get(fingerprintMinerConfiguration.getDecoyListLocation());

        logger.info("fingerprint miner initialized with input {}", familyChainListPath);
        logger.info("fingerprint miner initialized with blacklist {}", blacklistChainListPath);
        logger.info("fingerprint miner initialized with decoy {}", decoyChainListPath);

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

    private void outputResults() throws IOException {
        for (Itemset<String> topScoringItemset : topScoringItemsets) {
            logger.info("writing results for {}", topScoringItemset);
            if (!decoyDatasets.containsKey(topScoringItemset)) {
                logger.warn("no decoy dataset could be generated for {}", topScoringItemset);
                continue;
            }
            Path decoyOutputPath = Paths.get(itemsetMinerConfiguration.getOutputLocation()).getParent().resolve(topScoringItemset.toSimpleString()).resolve("decoy");
            Files.createDirectories(decoyOutputPath);
            for (List<LeafSubstructure<?>> leafSubstructures : decoyDatasets.get(topScoringItemset)) {
                StructuralMotif decoyMotif = StructuralMotif.fromLeafSubstructures(leafSubstructures);
                StructureWriter.writeLeafSubstructureContainer(decoyMotif, decoyOutputPath.resolve(decoyMotif.toString() + ".pdb"));
            }
            Path fingerprintOutputPath = Paths.get(itemsetMinerConfiguration.getOutputLocation()).getParent().resolve(topScoringItemset.toSimpleString()).resolve("fingerprint");
            Files.createDirectories(fingerprintOutputPath);
            List<StructuralMotif> fingerprintMotifs = topScoringClusters.get(topScoringItemset).getLeafNodes().stream()
                                                                        .map(BinaryTreeNode::getData)
                                                                        .map(ConsensusContainer::getSuperimposition)
                                                                        .map(SubstructureSuperimposition::getMappedFullCandidate)
                                                                        .map(StructuralMotif::fromLeafSubstructures)
                                                                        .collect(Collectors.toList());
            for (StructuralMotif fingerprintMotif : fingerprintMotifs) {
                StructureWriter.writeLeafSubstructureContainer(fingerprintMotif, fingerprintOutputPath.resolve(fingerprintMotif.toString() + ".pdb"));
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

    private void createDecoy() throws IOException {
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
                                      .atomFilter(StructuralEntityFilter.AtomFilter.isArbitrary())
                                      .rmsdCutoff(RMSD_CUTOFF)
                                      .run();

            List<List<LeafSubstructure<?>>> decoyDataset = fit3d.getMatches().stream()
                                                                .map(Fit3DMatch::getSubstructureSuperimposition)
                                                                .map(SubstructureSuperimposition::getMappedFullCandidate)
                                                                .collect(Collectors.toList());

            // read black list
            List<String> blacklistContent = Files.readAllLines(blacklistChainListPath);

            // filter blacklisted entries
            for (Iterator<List<LeafSubstructure<?>>> iterator = decoyDataset.iterator(); iterator.hasNext(); ) {
                List<LeafSubstructure<?>> decoy = iterator.next();
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
     * Converts the given consensus motif, which is composed of {@link LeafSubstructure}s, to a {@link StructuralMotif} composed of {@link AminoAcid}s.
     *
     * @param consensusMotif The artificial consensus motif.
     * @return A {@link StructuralMotif} composed of {@link AminoAcid}s.
     */
    private StructuralMotif convertConsensusMotif(StructuralMotif consensusMotif) {
        List<LeafSubstructure<?>> aminoAcids = new ArrayList<>();
        for (LeafSubstructure<?> leafSubstructure : consensusMotif.getAllLeafSubstructures()) {
            Optional<AminoAcidFamily> aminoAcidFamily = AminoAcidFamily.getAminoAcidTypeByOneLetterCode(leafSubstructure.getFamily().getOneLetterCode());
            aminoAcidFamily.ifPresent(aminoAcidFamily1 -> {
                AminoAcid aminoAcid = new OakAminoAcid(leafSubstructure.getIdentifier(), aminoAcidFamily1);
                aminoAcids.add(aminoAcid.getCopy());
            });
        }
        return StructuralMotif.fromLeafSubstructures(aminoAcids);
    }

    private void mineFamily() throws IOException, URISyntaxException {
        itemsetMiner = new ItemsetMinerRunner(itemsetMinerConfiguration).getItemsetMiner();
    }
}
