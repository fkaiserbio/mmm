package de.bioforscher.mmm.classify;

import de.bioforscher.mmm.ItemsetMiner;
import de.bioforscher.mmm.ItemsetMinerRunner;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusAlignment;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusContainer;
import de.bioforscher.singa.chemistry.algorithms.superimposition.fit3d.Fit3D;
import de.bioforscher.singa.chemistry.algorithms.superimposition.fit3d.Fit3DBuilder;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser.MultiParser;
import de.bioforscher.singa.chemistry.physical.atoms.Atom;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import de.bioforscher.singa.chemistry.physical.families.AminoAcidFamily;
import de.bioforscher.singa.chemistry.physical.leaves.AminoAcid;
import de.bioforscher.singa.chemistry.physical.leaves.AtomContainer;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;
import de.bioforscher.singa.chemistry.physical.model.StructuralEntityFilter.AtomFilter;
import de.bioforscher.singa.mathematics.graphs.trees.BinaryTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class FingerprintMiner {

    private static final Logger logger = LoggerFactory.getLogger(FingerprintMiner.class);
    private static final Predicate<Itemset<?>> TOP_SCORING_FILTER = itemset -> itemset.getItems().size() >= 3;
    private static final Comparator<Itemset<?>> TOP_SCORING_COMPARATOR = Comparator.comparing(Itemset::getConsensus);
    private static final int TOP_SCORING_LIMIT = 3;

    private final ItemsetMinerConfiguration<String> itemsetMinerConfiguration;
    private Path familyChainListPath;
    private Path decoyChainListPath;
    private ItemsetMiner<String> itemsetMiner;
    private List<Itemset<String>> topScoringItemsets;

    public FingerprintMiner(ItemsetMinerConfiguration<String> itemsetMinerConfiguration, Path familyChainListPath, Path decoyChainListPath) throws
                                                                                                                                            IOException,
                                                                                                                                            URISyntaxException,
                                                                                                                                            FingerprintMinerException {
        this.itemsetMinerConfiguration = itemsetMinerConfiguration;
        this.familyChainListPath = familyChainListPath;
        this.decoyChainListPath = decoyChainListPath;

        mineFamily();
        selectTopScoringItemsets();
        createDecoy();
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
        String outputLocation = args[1];
        Path inputListsPath = Paths.get(inputListsLocation);
        List<Path> inputLists = Files.list(inputListsPath).collect(Collectors.toList());
        for (Path inputList : inputLists) {
            logger.info("mining family {}", inputList);
            itemsetMinerConfiguration.setInputListLocation(inputList.toString());
            Path familyOutputLocation = Paths.get(outputLocation).resolve(inputList.getFileName());
            itemsetMinerConfiguration.setOutputLocation(familyOutputLocation.toString());
            // run fingerprint miner
            FingerprintMiner fingerprintMiner = new FingerprintMiner(itemsetMinerConfiguration, inputListsPath, Paths.get(decoyChainListResource.toURI()));
        }
    }

    private void selectTopScoringItemsets() throws FingerprintMinerException {
        logger.info("selecting top-scoring itemsets");
        topScoringItemsets = itemsetMiner.getTotalItemsets().stream()
                                         .filter(TOP_SCORING_FILTER)
                                         .sorted(TOP_SCORING_COMPARATOR)
                                         .collect(Collectors.toList());
        logger.info("top-scoring itemsets are \n{}", topScoringItemsets);
        logger.info("found {} top-scoring itemsets", topScoringItemsets.size());
        if (topScoringItemsets.isEmpty()) {
            throw new FingerprintMinerException("no top-scoring itemsets found");
        }
    }

    private void createDecoy() {
        logger.info("creating decoy dataset using Fit3D");
        for (Itemset<String> topScoringItemset : topScoringItemsets) {
            ConsensusAlignment consensusAlignment = itemsetMiner.getTotalClusteredItemsets().get(topScoringItemset);
            // get consensus motif of largest cluster
            consensusAlignment.getClusters().sort(Comparator.comparing(binaryTree -> binaryTree.getLeafNodes().size()));
            BinaryTree<ConsensusContainer> largestCluster = consensusAlignment.getClusters().get(consensusAlignment.getClusters().size() - 1);
            logger.info("largest cluster contains {} itemset observations", largestCluster.getLeafNodes().size());
            // convert consensus motif to artificial amino acid motif
            StructuralMotif consensusMotif = largestCluster.getRoot().getData().getStructuralMotif();
            StructuralMotif artificialSearchMotif = convertConsensusMotif(consensusMotif);

            logger.info("running Fit3D search for {} in decoy dataset", topScoringItemset);
            // create multi-parser for decoy data set
            // TODO use local PDB version
            MultiParser multiParser = StructureParser.online()
                                                     .chainList(decoyChainListPath);
            // run Fit3D search
            Fit3D run = Fit3DBuilder.create()
                                    .query(artificialSearchMotif)
                                    .targets(multiParser)
                                    .maximalParallelism()
                                    .atomFilter(AtomFilter.isArbitrary())
                                    .rmsdCutoff(1.0)
                                    .run();

            // TODO continue here
        }
    }

    /**
     * Converts the given consensus motif, which is composed of {@link AtomContainer}s, to a {@link StructuralMotif} composed of {@link AminoAcid}s.
     *
     * @param consensusMotif The artifical consensus motif.
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
