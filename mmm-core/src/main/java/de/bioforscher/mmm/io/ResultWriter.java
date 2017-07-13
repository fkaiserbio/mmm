package de.bioforscher.mmm.io;

import de.bioforscher.mmm.ItemsetMiner;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusAlignment;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureWriter;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
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
                    .filter(path -> path.getFileName().startsWith(outputPath.getFileName()))
                    .count();
            File movedFile = outputPath.getParent().resolve(outputPath.getFileName() + "." + outputFolderCount).toFile();
            logger.info("output folder already present, will be renamed to {}", movedFile);
            new File(outputPath.toUri()).renameTo(movedFile);
        }

        logger.debug("creating path {}", outputPath);
        Files.createDirectories(outputPath);
        this.itemsetMiner = itemsetMiner;
        logger.info("results will be written to {}", itemsetMinerConfiguration.getOutputLocation());
    }

    public void writeItemsetMinerConfiguration() throws IOException {
        Files.write(outputPath.resolve("itemset-miner_config.json"), itemsetMinerConfiguration.toJson().getBytes());
        logger.info("itemset miner configuration saved to {}", itemsetMinerConfiguration.getOutputLocation());
    }

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

    public void writeClusteredItemsets() throws IOException {

        // determine count of extracted itemsets
        Optional<Integer> extractedItemsetsCount = itemsetMiner.getTotalExtractedItemsets().values().stream()
                .map(List::size)
                .reduce(Integer::sum);

        logger.info("writing {} clustered itemsets with {} observations in total", itemsetMiner.getTotalClusteredItemsets().size(), extractedItemsetsCount.orElse(0));

        // write clusters
        List<Itemset<LabelType>> totalItemsets = itemsetMiner.getTotalItemsets();
        for (int i = 0; i < totalItemsets.size(); i++) {
            Itemset<LabelType> itemset = totalItemsets.get(i);
            int rank = i + 1;
            Path itemsetPath = outputPath.resolve("clustered_itemsets").resolve(rank + "_" + itemset.toSimpleString());
            ConsensusAlignment consensusAlignment = itemsetMiner.getTotalClusteredItemsets().get(itemset);
            consensusAlignment.writeClusters(itemsetPath);
        }
    }
}
