package de.bioforscher.mmm;

import de.bioforscher.mmm.io.DataPointReader;
import de.bioforscher.mmm.io.ResultWriter;
import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.analysis.statistics.SignificanceEstimator;
import de.bioforscher.mmm.model.analysis.statistics.SignificanceEstimator.Significance;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.analysis.statistics.SignificanceEstimatorConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ExtractionDependentMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ExtractionMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SimpleMetricConfiguration;
import de.bioforscher.mmm.model.enrichment.DataPointEnricher;
import de.bioforscher.mmm.model.mapping.DataPointLabelMapper;
import de.bioforscher.mmm.model.mapping.MappingRule;
import de.bioforscher.mmm.model.metrics.EvaluationMetric;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParserOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class requires a full {@link ItemsetMinerConfiguration} but can then be used to mine macromolecular structures in a convenient way.
 *
 * @author fk
 */
public class ItemsetMinerRunner {

    private static final Logger logger = LoggerFactory.getLogger(ItemsetMinerRunner.class);

    private final ItemsetMinerConfiguration<String> itemsetMinerConfiguration;
    private List<DataPoint<String>> dataPoints;
    private List<EvaluationMetric<String>> evaluationMetrics;
    private ItemsetMiner<String> itemsetMiner;
    private TreeMap<Significance, Itemset<String>> significantItemsets;

    public ItemsetMinerRunner(ItemsetMinerConfiguration<String> itemsetMinerConfiguration) throws IOException, URISyntaxException {
        this.itemsetMinerConfiguration = itemsetMinerConfiguration;
        logger.info("configuration created on {} by {}", itemsetMinerConfiguration.getCreationDate(), itemsetMinerConfiguration.getCreationUser());
        readDataPoints();
        enrichDataPoints();
        mapDataPoints();
        createMetrics();
        mineDataPoints();
        calculateSignificance();
        outputResults();
        printReport();
    }

    public ItemsetMinerRunner(ItemsetMinerConfiguration<String> itemsetMinerConfiguration, List<DataPoint<String>> dataPoints) throws IOException {
        this.itemsetMinerConfiguration = itemsetMinerConfiguration;
        logger.info("configuration created on {} by {}", itemsetMinerConfiguration.getCreationDate(), itemsetMinerConfiguration.getCreationUser());
        logger.info("data points of size {} already provided", dataPoints.size());
        this.dataPoints = dataPoints;
        createMetrics();
        mineDataPoints();
        calculateSignificance();
        outputResults();
        printReport();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        Path configurationPath = Paths.get(args[0]);

        // read configuration
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = ItemsetMinerConfiguration.from(configurationPath);

        new ItemsetMinerRunner(itemsetMinerConfiguration);
    }

    private void readDataPoints() throws URISyntaxException, IOException {

        // create structure parser options
        StructureParserOptions structureParserOptions = new StructureParserOptions();
        structureParserOptions.retrieveLigandInformation(true);
        structureParserOptions.omitHydrogens(true);

        logger.info(">>>STEP 1<<< reading data points");
        // input path is resource
        String inputListLocation = itemsetMinerConfiguration.getInputListLocation();

        // decide whether to use directory, chain list, or given IDs
        DataPointReader dataPointReader;
        if (itemsetMinerConfiguration.getInputListLocation() == null) {
            List<Path> structurePaths = Files.list(Paths.get(itemsetMinerConfiguration.getInputDirectoryLocation()))
                                             .filter(path -> path.toFile().isFile())
                                             .collect(Collectors.toList());
            dataPointReader = new DataPointReader(itemsetMinerConfiguration.getDataPointReaderConfiguration(), structurePaths);

        } else {
            Path inputListPath;
            URL inputListResourceURL = Thread.currentThread().getContextClassLoader()
                                             .getResource(inputListLocation);
            if (inputListResourceURL != null) {
                inputListPath = Paths.get(inputListResourceURL.toURI());
                logger.info("found input list in resources");
            } else {
                inputListPath = Paths.get(inputListLocation);
                logger.info("external input list will be used");
            }
            dataPointReader = new DataPointReader(itemsetMinerConfiguration.getDataPointReaderConfiguration(), inputListPath);
        }
        dataPoints = dataPointReader.readDataPoints();
    }

    private void enrichDataPoints() {

        logger.info(">>>STEP 2<<< enriching data points");

        DataPointEnricher<String> dataPointEnricher = itemsetMinerConfiguration.getDataPointEnricher();
        if (dataPointEnricher != null) {
            logger.info("applying data point enricher {}", dataPointEnricher);
            dataPoints.forEach(dataPointEnricher::enrichDataPoint);
        }
    }

    private void mapDataPoints() {

        logger.info(">>>STEP 3<<< mapping data points");

        List<MappingRule<String>> mappingRules = itemsetMinerConfiguration.getMappingRules();
        if (mappingRules != null && !mappingRules.isEmpty()) {
            logger.info("mapping data points according to mapping rules {}", mappingRules);
            DataPointLabelMapper<String> dataPointLabelMapper = new DataPointLabelMapper<>(mappingRules);
            dataPoints = dataPoints.stream()
                                   .map(dataPointLabelMapper::mapDataPoint)
                                   .collect(Collectors.toList());
        } else {
            logger.info("no mapping rule specified");
        }
    }

    private void createMetrics() {

        logger.info(">>>STEP 4<<< creating metrics");

        evaluationMetrics = new ArrayList<>();
        try {
            // create simple evaluation metrics
            for (SimpleMetricConfiguration<String> simpleMetricConfiguration : itemsetMinerConfiguration.getSimpleMetricConfigurations()) {
                logger.info("found simple metric {}, creating instance", simpleMetricConfiguration);
                evaluationMetrics.add(simpleMetricConfiguration.createMetric(dataPoints));
            }
            // create extraction metric if any
            ExtractionMetricConfiguration<String> extractionMetricConfiguration = itemsetMinerConfiguration.getExtractionMetricConfiguration();
            if (extractionMetricConfiguration != null) {
                logger.info("found extraction metric {}, creating instance", extractionMetricConfiguration);
                evaluationMetrics.add(extractionMetricConfiguration.createMetric(dataPoints));
                // create extraction dependent metrics
                for (ExtractionDependentMetricConfiguration<String> extractionDependentMetricConfiguration : itemsetMinerConfiguration.getExtractionDependentMetricConfigurations()) {
                    logger.info("found extraction-dependent metric {}, creating instance", extractionDependentMetricConfiguration);
                    evaluationMetrics.add(extractionDependentMetricConfiguration.createMetric());
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            logger.error("failed to create metric", e);
        }
    }

    private void mineDataPoints() {

        logger.info(">>>STEP 5<<< mining data points");

        itemsetMiner = new ItemsetMiner<>(dataPoints, evaluationMetrics, itemsetMinerConfiguration);
        itemsetMiner.start();
    }

    public TreeMap<Significance, Itemset<String>> getSignificantItemsets() {
        return significantItemsets;
    }

    private void calculateSignificance() throws IOException {

        logger.info(">>>STEP 6<<< calculating significance");

        DecimalFormat decimalFormat;

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        decimalFormat = (DecimalFormat) nf;
        decimalFormat.applyPattern("0.00000000");

        StringJoiner stringJoiner = new StringJoiner("\n", "itemset,p-value,ks\n", "");
        SignificanceEstimatorConfiguration significanceEstimatorConfiguration = itemsetMinerConfiguration.getSignificanceEstimatorConfiguration();

        if (significanceEstimatorConfiguration == null) {
            logger.info("skipped calculation of significance");
            return;
        }

        logger.info("calculating significance for type " + significanceEstimatorConfiguration.getSignificanceType());

        SignificanceEstimator<String> significanceEstimator = new SignificanceEstimator<>(itemsetMiner, significanceEstimatorConfiguration);
        significantItemsets = significanceEstimator.getSignificantItemsets();
        for (Map.Entry<Significance, Itemset<String>> entry : significantItemsets.entrySet()) {
            StringJoiner lineJoiner = new StringJoiner(",");
            lineJoiner.add(entry.getValue().toSimpleString());
            lineJoiner.add(decimalFormat.format(entry.getKey().getPvalue()));
            lineJoiner.add(decimalFormat.format(entry.getKey().getKs()));
            stringJoiner.add(lineJoiner.toString());
        }

        Path significanceOutputPath = Paths.get(itemsetMinerConfiguration.getOutputLocation()).resolve("significance.csv");
        Files.createDirectories(significanceOutputPath.getParent());

        logger.info("writing significance of results to {}", significanceOutputPath);
        Files.write(significanceOutputPath, stringJoiner.toString().getBytes());
    }

    private void outputResults() throws IOException {

        logger.info(">>>STEP 7<<< writing results");

        ResultWriter<String> resultWriter = new ResultWriter<>(itemsetMinerConfiguration, itemsetMiner);
        resultWriter.writeItemsetMinerConfiguration();
        if (!itemsetMiner.getTotalClusteredItemsets().isEmpty()) {
            resultWriter.writeClusteredItemsets();
        } else if (!itemsetMiner.getTotalAffinityItemsets().isEmpty()) {
            resultWriter.writeAffinityItemsets();
        } else if (!itemsetMiner.getTotalExtractedItemsets().isEmpty() && itemsetMiner.getTotalClusteredItemsets().isEmpty()) {
            resultWriter.writeExtractedItemsets();
        }
    }

    private void printReport() throws IOException {

        logger.info(">>>STEP 8<<< printing report");

        StringBuilder report = new StringBuilder()
                .append("\n\n\t>>>Itemset Miner<<<")
                .append("\n\n\tCopyright (c) 2017.\n")
                .append("\tFlorian Kaiser, bioinformatics group Mittweida\n")
                .append("\n====CONFIGURATION====================================================\n")
                .append("\t# of data points\t")
                .append(dataPoints.size())
                .append("\n")
                .append(evaluationMetrics.stream()
                                         .map(EvaluationMetric::toString)
                                         .collect(Collectors.joining("\n\t\t", "\tevaluation metrics:\n\t\t", "\n")));
        report.append("\tsorting by\t\t\t")
              .append(itemsetMinerConfiguration.getItemsetComparatorType())
              .append("\n");

        report.append("\n====RESULTS==========================================================\n");
        report.append("rank\titemset\n");
        List<Itemset<String>> totalItemsets = itemsetMiner.getTotalItemsets();
        int totalItemsetCount = totalItemsets.size();
        int patternCount = String.valueOf(totalItemsetCount).length();
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < patternCount; i++) {
            pattern.append("0");
        }
        DecimalFormat rankFormatter = new DecimalFormat(pattern.toString());
        for (int i = 0; i < totalItemsetCount; i++) {
            int rank = i + 1;
            Itemset<String> itemset = totalItemsets.get(i);
            StringJoiner stringJoiner = new StringJoiner("\t", "", "\n");
            stringJoiner.add(rankFormatter.format(rank) + "/" + totalItemsetCount);
            stringJoiner.add(itemset.toString());
            if (itemsetMiner.getTotalExtractedItemsets() != null && !itemsetMiner.getTotalExtractedItemsets().isEmpty()) {
                stringJoiner.add("(" + itemsetMiner.getTotalExtractedItemsets().get(itemset).size() + " observations)");
            }
            if ((itemsetMiner.getTotalClusteredItemsets() != null && !itemsetMiner.getTotalClusteredItemsets().isEmpty())) {
                stringJoiner.add("(" + itemsetMiner.getTotalClusteredItemsets().get(itemset).getClusters().size() + " clusters)");
            }
            if ((itemsetMiner.getTotalAffinityItemsets() != null && !itemsetMiner.getTotalAffinityItemsets().isEmpty())) {
                stringJoiner.add("(" + itemsetMiner.getTotalAffinityItemsets().get(itemset).getClusters().size() + " clusters)");
            }
            report.append(stringJoiner.toString());
        }

        report.append("=====================================================================\n");
        logger.info("mining report: {}", report.toString());

        Path reportOutputPath = Paths.get(itemsetMinerConfiguration.getOutputLocation()).resolve("report.txt");
        Files.createDirectories(reportOutputPath.getParent());

        logger.info("writing report to {}", reportOutputPath);
        Files.write(reportOutputPath, report.toString().getBytes());
    }


    public ItemsetMinerConfiguration<String> getItemsetMinerConfiguration() {
        return itemsetMinerConfiguration;
    }

    public List<EvaluationMetric<String>> getEvaluationMetrics() {
        return evaluationMetrics;
    }

    public List<DataPoint<String>> getDataPoints() {
        return dataPoints;
    }

    public ItemsetMiner<String> getItemsetMiner() {
        return itemsetMiner;
    }
}
