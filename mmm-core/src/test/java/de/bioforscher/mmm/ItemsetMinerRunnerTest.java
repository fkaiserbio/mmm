package de.bioforscher.mmm;

import de.bioforscher.mmm.io.DataPointReaderConfiguration;
import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.analysis.statistics.SignificanceEstimatorType;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.analysis.statistics.SignificanceEstimatorConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.CohesionMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SeparationMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SupportMetricConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;

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
        itemsetMinerConfiguration.setInputListLocation("/home/fkaiser/Workspace/CloudStation/PhD/Promotion/datasets/pfam/v31/PF00089/PF00089_chains_BLAST_e-80.txt");
        itemsetMinerConfiguration.setOutputLocation(folder.getRoot().toString());
        DataPointReaderConfiguration dataPointReaderConfiguration = new DataPointReaderConfiguration();
        itemsetMinerConfiguration.setDataPointReaderConfiguration(dataPointReaderConfiguration);
    }

    @Test
    public void shouldRun() throws IOException, URISyntaxException {

//        itemsetMinerConfiguration.setMappingRules(Stream.of(new ChemicalGroupsMappingRule()).collect(Collectors.toList()));
        itemsetMinerConfiguration.setMaximalEpochs(3);
//        itemsetMinerConfiguration.setDataPointEnricher(new IntraChainInteractionEnricher());

        SupportMetricConfiguration<String> supportMetricConfiguration = new SupportMetricConfiguration<>();
        supportMetricConfiguration.setMinimalSupport(0.9);
        itemsetMinerConfiguration.addSimpleMetricConfiguration(supportMetricConfiguration);

        CohesionMetricConfiguration<String> cohesionMetricConfiguration = new CohesionMetricConfiguration<>();
        cohesionMetricConfiguration.setMaximalCohesion(10.0);
        cohesionMetricConfiguration.setVertexOne(false);
        itemsetMinerConfiguration.setExtractionMetricConfiguration(cohesionMetricConfiguration);

//        AdherenceMetricConfiguration<String> adherenceMetricConfiguration = new AdherenceMetricConfiguration<>();
//        adherenceMetricConfiguration.setDesiredExtent(6.5);
//        adherenceMetricConfiguration.setDesiredExtent(0.2);
//        adherenceMetricConfiguration.setMaximalAdherence(1.0);
//        itemsetMinerConfiguration.setExtractionMetricConfiguration(adherenceMetricConfiguration);

        SeparationMetricConfiguration<String> separationMetricConfiguration = new SeparationMetricConfiguration<>();
        separationMetricConfiguration.setMaximalSeparation(100);
        separationMetricConfiguration.setOptimalSeparation(5);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(separationMetricConfiguration);

        ConsensusMetricConfiguration<String> consensusMetricConfiguration = new ConsensusMetricConfiguration<>();
        consensusMetricConfiguration.setMaximalConsensus(0.6);
        consensusMetricConfiguration.setClusterCutoffValue(0.5);
        consensusMetricConfiguration.setAlignWithinClusters(true);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(consensusMetricConfiguration);
        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.CONSENSUS);

//        AffinityMetricConfiguration<String> affinityMetricConfiguration = new AffinityMetricConfiguration<>();
//        affinityMetricConfiguration.setMaximalAffinity(1.0);
//        affinityMetricConfiguration.setAlignWithinClusters(false);
//        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(affinityMetricConfiguration);
//        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.AFFINITY);

        SignificanceEstimatorConfiguration significanceEstimatorConfiguration = new SignificanceEstimatorConfiguration();
        significanceEstimatorConfiguration.setSignificanceType(SignificanceEstimatorType.CONSENSUS);
        significanceEstimatorConfiguration.setSampleSize(10);
        significanceEstimatorConfiguration.setSignificanceCutoff(0.1);
        itemsetMinerConfiguration.setSignificanceEstimatorConfiguration(significanceEstimatorConfiguration);

        System.out.println(itemsetMinerConfiguration.toJson());
        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);
        System.out.println();
    }
}