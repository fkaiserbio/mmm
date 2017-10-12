package de.bioforscher.mmm;

import de.bioforscher.mmm.io.DataPointReaderConfiguration;
import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.CohesionMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SeparationMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SupportMetricConfiguration;
import de.bioforscher.mmm.model.enrichment.IntraChainInteractionEnricher;
import de.bioforscher.mmm.model.mapping.rules.ChemicalGroupsMappingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fk
 */
public class ItemsetMinerRunnerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private ItemsetMinerConfiguration<String> itemsetMinerConfiguration;

    @Before
    public void setUp() {
        itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        itemsetMinerConfiguration.setMaximalEpochs(3);
        itemsetMinerConfiguration.setInputListLocation("craven2016_WSXWS_motif.txt");
        itemsetMinerConfiguration.setOutputLocation(folder.getRoot().toString());
        DataPointReaderConfiguration dataPointReaderConfiguration = new DataPointReaderConfiguration();
        itemsetMinerConfiguration.setDataPointReaderConfiguration(dataPointReaderConfiguration);
    }

    @Test
    public void shouldRun() throws IOException, URISyntaxException {

        itemsetMinerConfiguration.setMappingRules(Stream.of(new ChemicalGroupsMappingRule()).collect(Collectors.toList()));
        itemsetMinerConfiguration.setDataPointEnricher(new IntraChainInteractionEnricher());
        itemsetMinerConfiguration.setMaximalEpochs(3);

        SupportMetricConfiguration<String> supportMetricConfiguration = new SupportMetricConfiguration<>();
        supportMetricConfiguration.setMinimalSupport(0.9);
        itemsetMinerConfiguration.addSimpleMetricConfiguration(supportMetricConfiguration);

        CohesionMetricConfiguration<String> cohesionMetricConfiguration = new CohesionMetricConfiguration<>();
        cohesionMetricConfiguration.setMaximalCohesion(10.0);
        cohesionMetricConfiguration.setVertexOne(false);
        itemsetMinerConfiguration.setExtractionMetricConfiguration(cohesionMetricConfiguration);

        SeparationMetricConfiguration<String> separationMetricConfiguration = new SeparationMetricConfiguration<>();
        separationMetricConfiguration.setMaximalSeparation(50);
        separationMetricConfiguration.setOptimalSeparation(5);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(separationMetricConfiguration);

        ConsensusMetricConfiguration<String> consensusMetricConfiguration = new ConsensusMetricConfiguration<>();
        consensusMetricConfiguration.setMaximalConsensus(0.5);
        consensusMetricConfiguration.setClusterCutoffValue(0.5);
        consensusMetricConfiguration.setAlignWithinClusters(true);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(consensusMetricConfiguration);
        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.CONSENSUS);

//        AffinityMetricConfiguration<String> affinityMetricConfiguration = new AffinityMetricConfiguration<>();
//        affinityMetricConfiguration.setMaximalAffinity(1.0);
//        affinityMetricConfiguration.setAlignWithinClusters(true);
//        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(affinityMetricConfiguration);
//        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.AFFINITY);

        new ItemsetMinerRunner(itemsetMinerConfiguration);
    }
}