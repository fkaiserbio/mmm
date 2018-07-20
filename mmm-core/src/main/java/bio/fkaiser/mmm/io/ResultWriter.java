package bio.fkaiser.mmm.io;

import bio.fkaiser.mmm.ItemsetMiner;
import bio.fkaiser.mmm.ItemsetMinerException;
import bio.fkaiser.mmm.model.DataPointIdentifier;
import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.configurations.ItemsetMinerConfiguration;
import bio.fkaiser.mmm.model.metrics.ConsensusMetric;
import bio.fkaiser.mmm.model.metrics.ExtractionMetric;
import bio.singa.core.utility.Resources;
import bio.singa.structure.algorithms.superimposition.affinity.AffinityAlignment;
import bio.singa.structure.algorithms.superimposition.consensus.ConsensusAlignment;
import bio.singa.structure.model.identifiers.LeafIdentifier;
import bio.singa.structure.model.interfaces.LeafSubstructure;
import bio.singa.structure.model.oak.StructuralMotif;
import bio.singa.structure.parser.pdb.structures.StructureWriter;
import bio.singa.structure.parser.pdb.structures.tokens.AtomToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Writes all relevant results of an {@link ItemsetMiner} run based on the configuration made in {@link ItemsetMinerConfiguration}.
 *
 * @author fk
 */
public class ResultWriter<LabelType extends Comparable<LabelType>> {

    private static final Logger logger = LoggerFactory.getLogger(ResultWriter.class);
    private static final int MINIMAL_REFERENCE_STRUCTURE_ITEMSET_SIZE = 3;

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
     * @throws IOException If the {@link ItemsetMinerConfiguration} cannot be written.
     */
    public void writeItemsetMinerConfiguration() throws IOException {
        Files.write(outputPath.resolve("itemset-miner_config.json"), itemsetMinerConfiguration.toJson().getBytes());
        logger.info("itemset miner configuration saved to {}", itemsetMinerConfiguration.getOutputLocation());
    }

    /**
     * Writes all extracted {@link Itemset}s (only available if {@link ExtractionMetric} was used).
     *
     * @throws IOException If extracted {@link Itemset}s cannot be written.
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
                    StructureWriter.writeLeafSubstructureContainer(structuralMotif, itemsetPath.resolve(structuralMotif + ".pdb"));
                } else {
                    logger.warn("no extracted itemset observations available for itemset {}", itemset);
                }
            }
        }
    }

    /**
     * Writes all clustered {@link Itemset}s (only available if {@link ConsensusMetric} was used).
     *
     * @throws IOException If the clustered {@link Itemset}s cannot be written.
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

    public void writeReferenceStructure() throws IOException {
        String inputChain = itemsetMinerConfiguration.getInputChain();
        if (inputChain != null) {

            // parse input structure
            String[] split = inputChain.split("\\.");
            String pdbIdentifier = split[0];
            String chainIdentifier = split[1];

            // get reference data point
            List<LeafSubstructure<?>> referenceLeafSubstructures = itemsetMiner.getDataPoints().stream()
                                                                               .filter(dataPoint -> dataPoint.getDataPointIdentifier().getPdbIdentifier().equals(pdbIdentifier)
                                                                                                    && dataPoint.getDataPointIdentifier().getChainIdentifier().equals(chainIdentifier))
                                                                               .findFirst()
                                                                               .orElseThrow(() -> new ItemsetMinerException("failed to map to reference structure " + inputChain))
                                                                               .getItems().stream()
                                                                               .map(Item::getLeafSubstructure)
                                                                               .filter(Optional::isPresent)
                                                                               .map(Optional::get)
                                                                               .collect(Collectors.toList());

            List<Itemset<LabelType>> correspondingItemsets = new ArrayList<>();
            for (Map.Entry<Itemset<LabelType>, List<Itemset<LabelType>>> entry : itemsetMiner.getTotalExtractedItemsets().entrySet()) {
                // ignore small itemsets to reduce noise
                if (entry.getKey().getItems().size() < MINIMAL_REFERENCE_STRUCTURE_ITEMSET_SIZE) {
                    continue;
                }
                // determine origin data point identifier for input chain
                entry.getValue().stream()
                     .filter(itemset -> {
                         Optional<DataPointIdentifier> optionalIdentifier = itemset.getOriginDataPointIdentifier();
                         if (optionalIdentifier.isPresent()) {
                             DataPointIdentifier identifier = optionalIdentifier.get();
                             return identifier.getPdbIdentifier().equals(pdbIdentifier) && identifier.getChainIdentifier().equals(chainIdentifier);
                         }
                         return false;
                     })
                     .findFirst()
                     .ifPresent(correspondingItemsets::add);
            }

            logger.info("input structure was hit by {} itemsets", correspondingItemsets.size());

            // store absolute structure coverage
            Map<LeafIdentifier, Double> absoluteStructureCoverage = new TreeMap<>();
            for (Itemset<LabelType> correspondingItemset : correspondingItemsets) {
                Optional<StructuralMotif> optionalStructuralMotif = correspondingItemset.getStructuralMotif();
                if (optionalStructuralMotif.isPresent()) {
                    StructuralMotif structuralMotif = optionalStructuralMotif.get();
                    for (LeafSubstructure<?> leafSubstructure : structuralMotif.getAllLeafSubstructures()) {
                        absoluteStructureCoverage.merge(leafSubstructure.getIdentifier(), 1.0, (oldValue, newValue) -> oldValue + newValue);
                    }
                }
            }

            // normalize structure coverage
            double maxValue = absoluteStructureCoverage.values().stream()
                                                       .mapToDouble(Double::doubleValue)
                                                       .max().orElse(1.0);
            Map<LeafIdentifier, Double> relativeStructureCoverage = absoluteStructureCoverage.entrySet().stream()
                                                                                             .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / maxValue));

            DecimalFormat decimalFormat = new DecimalFormat("0.00");

            // encode in B-factors
            List<String> allAtomLines = new ArrayList<>();
            for (LeafSubstructure<?> leafSubstructure : referenceLeafSubstructures) {
                List<String> atomLines = AtomToken.assemblePDBLine(leafSubstructure).stream()
                                                  .map(line -> line.replace("0.00", decimalFormat.format(relativeStructureCoverage.getOrDefault(leafSubstructure.getIdentifier(), 0.0))))
                                                  .collect(Collectors.toList());
                allAtomLines.addAll(atomLines);
            }

            String coverageFileName = pdbIdentifier + "_" + chainIdentifier + "_coverage.pdb";
            Path coverageStructurePath = outputPath.resolve(coverageFileName);
            Files.write(coverageStructurePath, allAtomLines.stream().collect(Collectors.joining("\n")).getBytes());
            logger.info("representation of structure coverage written to {}", coverageStructurePath);

            // write CSV representation of structure coverage
            StringJoiner csvJoiner = new StringJoiner("\n", "residue,absolute,relative\n", "");
            for (Map.Entry<LeafIdentifier, Double> entry : absoluteStructureCoverage.entrySet()) {
                csvJoiner.add(entry.getKey() + "," + entry.getValue() + "," + relativeStructureCoverage.get(entry.getKey()));
            }
            Path csvPath = outputPath.resolve(pdbIdentifier + "_" + chainIdentifier + "_coverage.csv");
            Files.write(csvPath, csvJoiner.toString().getBytes());
            logger.info("CSV representation of structure coverage written to {}", csvPath);

            // write PML file for structure coverage
            InputStream inputStream = Resources.getResourceAsStream("mmm_coverage.pml");
            String pmlContent = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            pmlContent = pmlContent.replace("%COVERAGE_FILE%", coverageFileName);
            Path pmlPath = outputPath.resolve(pdbIdentifier + "_" + chainIdentifier + "_coverage.pml");
            Files.write(pmlPath, pmlContent.getBytes());
            logger.info("PyMol PML file of structure coverage written to {}", pmlPath);

        } else {
            throw new UnsupportedOperationException("writing of reference structure is only available if single chain input was used");
        }
    }

    /**
     * Writes the mines {@link Itemset}s to a CSV file.
     *
     * @throws IOException If writing of CSV file fails.
     */
    public void writeCSV() throws IOException {
        String fileContent = itemsetMiner.getTotalItemsets().stream()
                                         .map(Itemset::toCSVLine)
                                         .collect(Collectors.joining("\n", Itemset.CSV_HEADER, ""));
        Path csvOutputPath = outputPath.resolve("summary.csv");
        logger.info("writing CSV output to {}", csvOutputPath);
        Files.write(csvOutputPath, fileContent.getBytes());
    }
}
