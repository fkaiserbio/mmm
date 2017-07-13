package de.bioforscher.mmm;

import de.bioforscher.mmm.io.DataPointReader;
import de.bioforscher.mmm.io.ResultWriter;
import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
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

    public ItemsetMinerRunner(ItemsetMinerConfiguration<String> itemsetMinerConfiguration) throws IOException, URISyntaxException {

        this.itemsetMinerConfiguration = itemsetMinerConfiguration;
        logger.info("configuration created on {} by {}", itemsetMinerConfiguration.getCreationDate(), itemsetMinerConfiguration.getCreationUser());

        readDataPoints();
        enrichDataPoints();
        mapDataPoints();
        createMetrics();
        mineDataPoints();
        outputResults();
        printReport();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        Path configurationPath = Paths.get(args[0]);

        // read configuration
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = ItemsetMinerConfiguration.from(configurationPath);

        new ItemsetMinerRunner(itemsetMinerConfiguration);
    }

    public ItemsetMiner<String> getItemsetMiner() {
        return itemsetMiner;
    }

    private void outputResults() throws IOException {

        logger.info(">>>STEP 7<<< writing results");

        ResultWriter<String> resultWriter = new ResultWriter<>(itemsetMinerConfiguration, itemsetMiner);
        resultWriter.writeItemsetMinerConfiguration();
        if (!itemsetMiner.getTotalClusteredItemsets().isEmpty()) {
            resultWriter.writeClusteredItemsets();
        } else if (!itemsetMiner.getTotalExtractedItemsets().isEmpty() && itemsetMiner.getTotalClusteredItemsets().isEmpty()) {
            resultWriter.writeExtractedItemsets();
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

    private void mapDataPoints() {

        logger.info(">>>STEP 3<<< mapping data points");

        MappingRule<String> mappingRule = itemsetMinerConfiguration.getMappingRule();
        if (mappingRule != null) {
            logger.info("mapping data points according to {}", mappingRule);
            DataPointLabelMapper<String> dataPointLabelMapper = new DataPointLabelMapper<>(mappingRule);
            dataPoints = dataPoints.stream()
                                   .map(dataPointLabelMapper::mapDataPoint)
                                   .collect(Collectors.toList());
        } else {
            logger.info("no mapping rule specified");
        }
    }

    private void enrichDataPoints() {

        logger.info(">>>STEP 2<<< enriching data points");

        DataPointEnricher<String> dataPointEnricher = itemsetMinerConfiguration.getDataPointEnricher();
        if (dataPointEnricher != null) {
            logger.info("applying data point enricher {}", dataPointEnricher);
            dataPoints.forEach(dataPointEnricher::enrichDataPoint);
        }
    }

    private void readDataPoints() throws URISyntaxException, IOException {

        // create structure parser options
        StructureParserOptions structureParserOptions = new StructureParserOptions();
        structureParserOptions.retrieveLigandInformation(true);
        structureParserOptions.omitHydrogens(true);

        logger.info(">>>STEP 1<<< reading data points");
        // input path is resource
        String inputListLocation = itemsetMinerConfiguration.getInputListLocation();

        // decide whether to use directory or chain list to mine
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

    private void printReport() throws IOException {

        logger.info(">>>STEP 6<<< printing report");

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
        for (int i = 0; i < totalItemsets.size(); i++) {
            int rank = i + 1;
            Itemset<String> itemset = totalItemsets.get(i);
            StringJoiner stringJoiner = new StringJoiner("\t", "", "\n");
            stringJoiner.add(rank + "/" + totalItemsets.size());
            stringJoiner.add(itemset.toString());
            stringJoiner.add("(" + itemsetMiner.getTotalExtractedItemsets().get(itemset).size() + " observations)");
            report.append(stringJoiner.toString());
        }

        report.append("=====================================================================\n");
        logger.info("mining report: {}", report.toString());

        Path reportOutputPath = Paths.get(itemsetMinerConfiguration.getOutputLocation()).resolve("report.txt");
        Files.createDirectories(reportOutputPath.getParent());

        logger.info("writing report to {}", reportOutputPath);
        Files.write(reportOutputPath, report.toString().getBytes());
    }
}
