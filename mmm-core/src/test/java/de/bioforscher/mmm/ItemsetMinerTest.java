package de.bioforscher.mmm;

import de.bioforscher.mmm.io.DataPointReader;
import de.bioforscher.mmm.io.DataPointReaderConfiguration;
import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.CohesionMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SeparationMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SupportMetricConfiguration;
import de.bioforscher.mmm.model.enrichment.InteractionEnricher;
import de.bioforscher.mmm.model.metrics.*;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParserOptions;
import de.bioforscher.singa.chemistry.physical.atoms.representations.RepresentationSchemeType;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ItemsetMinerTest {

    @Test
    public void shouldCorrectlyCreateCandidates() throws Exception {

        Path inputListPath = Paths.get(Thread.currentThread().getContextClassLoader()
                                             .getResource("PF00127_chains.txt")
                                             .toURI());

        // create itemset miner configuration
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.CONSENSUS);

        // create structure parser options
        StructureParserOptions structureParserOptions = new StructureParserOptions();
        structureParserOptions.retrieveLigandInformation(true);
        structureParserOptions.omitHydrogens(true);

        DataPointReaderConfiguration dataPointReaderConfiguration = new DataPointReaderConfiguration();
        dataPointReaderConfiguration.setPdbLocation(null);
        dataPointReaderConfiguration.setParseLigands(true);

        // read data points
        DataPointReader dataPointReader = new DataPointReader(dataPointReaderConfiguration, inputListPath);
        List<DataPoint<String>> dataPoints = dataPointReader.readDataPoints();

//        // map data points
//        DataPointLabelMapper<String, String> dataPointLabelMapper = new DataPointLabelMapper<>(new ChemicalGroupsMappingRule());
//        dataPoints = dataPoints.stream()
//                               .map(dataPointLabelMapper::mapDataPoint)
//                               .collect(Collectors.toList());

        // enrich data points
        InteractionEnricher interactionEnricher = new InteractionEnricher();
        dataPoints.forEach(interactionEnricher::enrichDataPoint);

        // create storage for metrics
        List<EvaluationMetric<String>> evaluationMetrics = new ArrayList<>();

        // create support metric
        SupportMetricConfiguration supportMetricConfiguration = new SupportMetricConfiguration();
        supportMetricConfiguration.setMinimalSupport(0.9);
        SupportMetric<String> supportMetric = new SupportMetric<>(dataPoints, supportMetricConfiguration);
        evaluationMetrics.add(supportMetric);

        // create cohesion metric
        CohesionMetricConfiguration cohesionMetricConfiguration = new CohesionMetricConfiguration();
        cohesionMetricConfiguration.setMaximalCohesion(10.0);
        CohesionMetric<String> cohesionMetric = new CohesionMetric<>(dataPoints, cohesionMetricConfiguration);
        evaluationMetrics.add(cohesionMetric);

        // create consensus metric
        ConsensusMetricConfiguration consensusMetricConfiguration = new ConsensusMetricConfiguration();
        consensusMetricConfiguration.setMaximalConsensus(0.8);
        consensusMetricConfiguration.setAlignWithinClusters(true);
        consensusMetricConfiguration.setClusterCutoffValue(0.5);
        consensusMetricConfiguration.setRepresentationSchemeType(RepresentationSchemeType.SIDE_CHAIN_CENTROID);
        ConsensusMetric<String> consensusMetric = new ConsensusMetric<>(consensusMetricConfiguration);
        evaluationMetrics.add(consensusMetric);

        // create separation metric
        SeparationMetricConfiguration separationMetricConfiguration = new SeparationMetricConfiguration();
        separationMetricConfiguration.setMaximalSeparation(100.0);
        SeparationMetric<String> separationMetric = new SeparationMetric<>(separationMetricConfiguration);
        evaluationMetrics.add(separationMetric);

        ItemsetMiner<String> itemsetMiner = new ItemsetMiner<>(dataPoints, evaluationMetrics, itemsetMinerConfiguration);
        itemsetMiner.start();
    }
}