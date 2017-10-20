package de.bioforscher.mmm.classify.workflows.cath;

import de.bioforscher.mmm.ItemsetMinerRunner;
import de.bioforscher.mmm.classify.model.ItemsetLibrary;
import de.bioforscher.mmm.classify.workflows.WorkflowTask;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.AffinityMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.singa.chemistry.algorithms.superimposition.affinity.AffinityAlignment;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is TASK_01 to generate motif libraries of CATH superfamilies.
 *
 * @author fk
 */
public class T01_LibraryCreator extends WorkflowTask {

    public static final int MINIMAL_ITEMSET_SIZE = 3;
    private static final double MINIMAL_CLUSTER_RATIO = 0.8;
    private static Logger logger = LoggerFactory.getLogger(T01_LibraryCreator.class);

    private final int rankCutoff;
    private final Path superfamilyStructuresPath;
    private List<Path> topSuperfamilies;
    private TreeMap<Integer, List<Path>> rankedPopulatedSuperfamilies;

    public T01_LibraryCreator(Path outputPath, int rankCutoff, Path superfamilyStructuresPath) throws IOException, URISyntaxException {
        super(outputPath);
        this.rankCutoff = rankCutoff;
        this.superfamilyStructuresPath = superfamilyStructuresPath;
        logger.info("library creation started for the top {} populated superfamilies in {}", rankCutoff, superfamilyStructuresPath);
        findMostPopulatedSuperfamilies();
        createMotifLibraries();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new T01_LibraryCreator(Paths.get(args[0]), Integer.valueOf(args[1]), Paths.get(args[2]));
    }

    /**
     * Reconstructs the superfamily identifier from the given {@link Path}.
     *
     * @param topSuperfamily The {@link Path} of the superfamily.
     * @return The superfamily identifier.
     */
    private static String getSuperfamilyIdentifier(Path topSuperfamily) {
        return topSuperfamily.getParent().getParent().getParent() +
               "_" +
               topSuperfamily.getParent().getParent().getFileName() +
               "_" +
               topSuperfamily.getParent().getFileName() +
               "_" +
               topSuperfamily.getFileName();
    }

    private void createMotifLibraries() throws IOException, URISyntaxException {
        for (Path topSuperfamily : topSuperfamilies) {
            logger.info("creating library for superfamily {}", topSuperfamily);
            String superfamilyIdentifier = getSuperfamilyIdentifier(topSuperfamily);
            ItemsetMinerConfiguration<String> itemsetMinerConfiguration = createItemsetMinerConfiguration(topSuperfamily);
            ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);

            boolean consensusMetricUsed = itemsetMinerConfiguration.getExtractionDependentMetricConfigurations().stream()
                                                                   .anyMatch(configuration -> configuration instanceof ConsensusMetricConfiguration);
            boolean affinityMetricUsed = itemsetMinerConfiguration.getExtractionDependentMetricConfigurations().stream()
                                                                  .anyMatch(configuration -> configuration instanceof AffinityMetricConfiguration);
            if ((consensusMetricUsed && affinityMetricUsed)) {
                throw new IllegalArgumentException("library can be created only when using either consensus or affinity metric");
            }
            ItemsetLibrary itemsetLibrary = null;
            if (consensusMetricUsed) {
                // only consider significant itemsets
                TreeMap<Itemset<String>, ConsensusAlignment> totalClusteredItemsets = itemsetMinerRunner.getItemsetMiner().getTotalClusteredItemsets();
                totalClusteredItemsets.keySet().removeIf(itemset -> !itemsetMinerRunner.getSignificantItemsets().values().contains(itemset));
                itemsetLibrary = ItemsetLibrary.of(totalClusteredItemsets, MINIMAL_ITEMSET_SIZE, MINIMAL_CLUSTER_RATIO);
            } else if (affinityMetricUsed) {
                // only consider significant itemsets
                TreeMap<Itemset<String>, AffinityAlignment> totalAffinityItemsets = itemsetMinerRunner.getItemsetMiner().getTotalAffinityItemsets();
                totalAffinityItemsets.keySet().removeIf(itemset -> !itemsetMinerRunner.getSignificantItemsets().values().contains(itemset));
                itemsetLibrary = ItemsetLibrary.of(totalAffinityItemsets, MINIMAL_ITEMSET_SIZE);
            }
            if (itemsetLibrary != null) {
                itemsetLibrary.writeToPath(outputPath.resolve(superfamilyIdentifier + ".gz"));
            }
        }
    }

    private ItemsetMinerConfiguration<String> createItemsetMinerConfiguration(Path topSuperfamily) throws IOException {
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = ItemsetMinerConfiguration.from(Thread.currentThread().getContextClassLoader().getResourceAsStream("base_configuration.json"));
        itemsetMinerConfiguration.setInputDirectoryLocation(topSuperfamily.toString());
        return itemsetMinerConfiguration;
    }

    /**
     * Finds the top-populated superfamilies in the given CATH domain dataset.
     *
     * @throws IOException If CATH dataset is malformed.
     */
    private void findMostPopulatedSuperfamilies() throws IOException {
        logger.info("finding {} most populated superfamilies", rankCutoff);
        Map<Integer, List<Path>> populatedSuperfamilies = Files.walk(superfamilyStructuresPath, 4)
                                                               .filter(path -> {
                                                                   try {
                                                                       return Files.list(path).anyMatch(path1 -> path1.toString().endsWith(".pdb"));
                                                                   } catch (IOException e) {
                                                                       logger.error("failed to list files in directory {}", path);
                                                                       return false;
                                                                   }
                                                               })
                                                               .collect(Collectors.groupingBy(path -> {
                                                                   try {
                                                                       return (int) Files.list(path).count();
                                                                   } catch (IOException e) {
                                                                       logger.error("failed to count PDB files in directory {}", path);
                                                                       return -1;
                                                                   }
                                                               }));
        rankedPopulatedSuperfamilies = new TreeMap<>(Collections.reverseOrder());
        rankedPopulatedSuperfamilies.putAll(populatedSuperfamilies);
        int rankCounter = 1;
        topSuperfamilies = new ArrayList<>();
        for (Map.Entry<Integer, List<Path>> integerListEntry : rankedPopulatedSuperfamilies.entrySet()) {
            if (rankCounter <= rankCutoff) {
                topSuperfamilies.addAll(integerListEntry.getValue());
            }
            rankCounter++;
        }
        logger.info("top {} populated superfamilies are {}", rankCutoff, topSuperfamilies);
    }
}
