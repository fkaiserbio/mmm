package de.bioforscher.mmm.io;

import de.bioforscher.mmm.ItemsetMiner;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.singa.chemistry.algorithms.superimposition.affinity.AffinityAlignment;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusAlignment;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureWriter;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

/**
 * Writes all relevant results of an {@link ItemsetMiner} run based on the configuration made in {@link ItemsetMinerConfiguration}.
 *
 * @author fk
 */
public class ResultWriter<LabelType extends Comparable<LabelType>> {

    private static final Logger logger = LoggerFactory.getLogger(ResultWriter.class);

    private final ItemsetMinerConfiguration<LabelType> itemsetMinerConfiguration;
    private final Path outputPath;
    private final ItemsetMiner<LabelType> itemsetMiner;

    public ResultWriter(ItemsetMinerConfiguration<LabelType> itemsetMinerConfiguration, ItemsetMiner<LabelType> itemsetMiner) throws IOException {
        this.itemsetMinerConfiguration = itemsetMinerConfiguration;
        outputPath = Paths.get(itemsetMinerConfiguration.getOutputLocation());

        if (outputPath.toFile().exists()) {
            long outputFolderCount = Files.list(outputPath.getParent())
                                          .filter(path -> path.getFileName().toString().startsWith(outputPath.getFileName().toString()))
                                          .count();
            Path movedPath = outputPath.getParent().resolve(outputPath.getFileName() + "." + outputFolderCount);
            logger.info("output folder already present, will be renamed to {}", movedPath);
            Files.move(outputPath, movedPath);
        }

        logger.debug("creating path {}", outputPath);
        Files.createDirectories(outputPath);
        this.itemsetMiner = itemsetMiner;
        logger.info("results will be written to {}", itemsetMinerConfiguration.getOutputLocation());
    }

    /**
     * Writes the {@link ItemsetMinerConfiguration} that was used.
     *
     * @throws IOException
     */
    public void writeItemsetMinerConfiguration() throws IOException {
        Files.write(outputPath.resolve("itemset-miner_config.json"), itemsetMinerConfiguration.toJson().getBytes());
        logger.info("itemset miner configuration saved to {}", itemsetMinerConfiguration.getOutputLocation());
    }

    /**
     * Writes all extracted {@link Itemset}s (only available if {@link de.bioforscher.mmm.model.metrics.ExtractionMetric} was used).
     *
     * @throws IOException
     */
    public void writeExtractedItemsets() throws IOException {

        // determine count of extracted itemsets
        Optional<Integer> extractedItemsetsCount = itemsetMiner.getTotalExtractedItemsets().values().stream()
                                                               .map(List::size)
                                                               .reduce(Integer::sum);

        logger.info("writing {} extracted itemsets", extractedItemsetsCount.orElse(0));

        // write extracted itemsets
        List<Itemset<LabelType>> totalItemsets = itemsetMiner.getTotalItemsets();
        for (int i = 0; i < totalItemsets.size(); i++) {
            Itemset<LabelType> itemset = totalItemsets.get(i);
            int rank = i + 1;
            Path itemsetPath = outputPath.resolve("extracted_itemsets").resolve(rank + "_" + itemset.toSimpleString());
            for (Itemset<LabelType> extractedItemset : itemsetMiner.getTotalExtractedItemsets().get(itemset)) {
                Optional<StructuralMotif> structuralMotifOptional = extractedItemset.getStructuralMotif();
                if (structuralMotifOptional.isPresent()) {
                    StructuralMotif structuralMotif = structuralMotifOptional.get();
                    StructureWriter.writeBranchSubstructure(structuralMotif, itemsetPath.resolve(structuralMotif + ".pdb"));
                } else {
                    logger.warn("no extracted itemset observations available for itemset {}", itemset);
                }
            }
        }
    }

    /**
     * Writes all clustered {@link Itemset}s (only available if {@link de.bioforscher.mmm.model.metrics.ConsensusMetric} was used).
     *
     * @throws IOException
     */
    public void writeClusteredItemsets() throws IOException {

        // determine count of extracted itemsets
        Optional<Integer> extractedItemsetsCount = itemsetMiner.getTotalExtractedItemsets().values().stream()
                                                               .map(List::size)
                                                               .reduce(Integer::sum);

        int extractedItemsetCount = extractedItemsetsCount.orElse(0);
        logger.info("writing {} clustered itemsets with {} observations in total", itemsetMiner.getTotalClusteredItemsets().size(), extractedItemsetCount);

        int patternCount = String.valueOf(extractedItemsetCount).length();
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < patternCount; i++) {
            pattern.append("0");
        }
        DecimalFormat rankFormatter = new DecimalFormat(pattern.toString());

        // write clusters
        List<Itemset<LabelType>> totalItemsets = itemsetMiner.getTotalItemsets();
        for (int i = 0; i < totalItemsets.size(); i++) {
            Itemset<LabelType> itemset = totalItemsets.get(i);
            int rank = i + 1;
            String rankString = rankFormatter.format(rank);
            Path itemsetPath = outputPath.resolve("clustered_itemsets").resolve(rankString + "_" + itemset.toSimpleString());
            ConsensusAlignment consensusAlignment = itemsetMiner.getTotalClusteredItemsets().get(itemset);
            consensusAlignment.writeClusters(itemsetPath);
        }
    }

    public void writeAffinityItemsets() throws IOException {
        // determine count of extracted itemsets
        Optional<Integer> extractedItemsetsCount = itemsetMiner.getTotalExtractedItemsets().values().stream()
                                                               .map(List::size)
                                                               .reduce(Integer::sum);

        int extractedItemsetCount = extractedItemsetsCount.orElse(0);
        logger.info("writing {} affinity itemsets with {} observations in total", itemsetMiner.getTotalAffinityItemsets().size(), extractedItemsetCount);

        int patternCount = String.valueOf(extractedItemsetCount).length();
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < patternCount; i++) {
            pattern.append("0");
        }
        DecimalFormat rankFormatter = new DecimalFormat(pattern.toString());

        // write clusters
        List<Itemset<LabelType>> totalItemsets = itemsetMiner.getTotalItemsets();
        for (int i = 0; i < totalItemsets.size(); i++) {
            Itemset<LabelType> itemset = totalItemsets.get(i);
            int rank = i + 1;
            String rankString = rankFormatter.format(rank);
            Path itemsetPath = outputPath.resolve("affinity_itemsets").resolve(rankString + "_" + itemset.toSimpleString());
            AffinityAlignment affinityAlignment = itemsetMiner.getTotalAffinityItemsets().get(itemset);
            affinityAlignment.writeClusters(itemsetPath);
        }
    }
}
