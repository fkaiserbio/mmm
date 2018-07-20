package bio.fkaiser.mmm;

import bio.fkaiser.mmm.io.DataPointReaderConfiguration;
import bio.fkaiser.mmm.model.ItemsetComparatorType;
import bio.fkaiser.mmm.model.analysis.statistics.SignificanceEstimatorType;
import bio.fkaiser.mmm.model.configurations.ItemsetMinerConfiguration;
import bio.fkaiser.mmm.model.configurations.analysis.statistics.SignificanceEstimatorConfiguration;
import bio.fkaiser.mmm.model.configurations.metrics.*;
import bio.fkaiser.mmm.model.enrichment.IntraChainInteractionEnricher;
import bio.fkaiser.mmm.model.mapping.rules.ChemicalGroupsMappingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bio.singa.structure.parser.pdb.rest.cluster.PDBSequenceCluster.PDBSequenceClusterIdentity.IDENTITY_70;

/**
 * @author fk
 */
public class ItemsetMinerRunnerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private ItemsetMinerConfiguration<String> itemsetMinerConfiguration;
    private DataPointReaderConfiguration dataPointReaderConfiguration;

    @Before
    public void setUp() {
        itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        itemsetMinerConfiguration.setMaximalEpochs(3);
        itemsetMinerConfiguration.setOutputLocation(folder.getRoot().toString());
        dataPointReaderConfiguration = new DataPointReaderConfiguration();
        dataPointReaderConfiguration.setConsecutiveSequenceNumbering(true);
        itemsetMinerConfiguration.setDataPointReaderConfiguration(dataPointReaderConfiguration);
    }

    @Test
    public void shouldRunAgainstChainList() throws IOException, URISyntaxException {

        itemsetMinerConfiguration.setInputListLocation("craven2016_WSXWS_motif.txt");

//        itemsetMinerConfiguration.setMappingRules(Stream.of(new ChemicalGroupsMappingRule()).collect(Collectors.toList()));
//        itemsetMinerConfiguration.setMaximalEpochs(3);
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
        separationMetricConfiguration.setMaximalSeparation(50);
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

        new ItemsetMinerRunner(itemsetMinerConfiguration);
    }

    @Test
    public void shouldRunAgainstSingleChain() throws IOException, URISyntaxException {

        itemsetMinerConfiguration.setInputChain("9pcy.A");
        itemsetMinerConfiguration.getDataPointReaderConfiguration().setPdbSequenceCluster(IDENTITY_70);

        itemsetMinerConfiguration.setMappingRules(Stream.of(new ChemicalGroupsMappingRule()).collect(Collectors.toList()));
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
        separationMetricConfiguration.setMaximalSeparation(50);
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
        significanceEstimatorConfiguration.setSignificanceCutoff(0.001);
        itemsetMinerConfiguration.setSignificanceEstimatorConfiguration(significanceEstimatorConfiguration);

        new ItemsetMinerRunner(itemsetMinerConfiguration);
    }

    @Test
    public void shouldRunAgainstPdb() throws IOException, URISyntaxException {
        dataPointReaderConfiguration.setMmtf(true);
        dataPointReaderConfiguration.setPdbLocation("/srv/pdb");
        itemsetMinerConfiguration.setInputListLocation("/home/fkaiser/Workspace/CloudStation/PhD/Promotion/datasets/nrpdb/nrpdb_041416/nrpdb_041416_BLAST_e-7.txt");

        itemsetMinerConfiguration.setMappingRules(Stream.of(new ChemicalGroupsMappingRule()).collect(Collectors.toList()));
//        itemsetMinerConfiguration.setDataPointEnricher(new IntraChainInteractionEnricher());

        SupportMetricConfiguration<String> supportMetricConfiguration = new SupportMetricConfiguration<>();
        supportMetricConfiguration.setMinimalSupport(0.9);
        itemsetMinerConfiguration.addSimpleMetricConfiguration(supportMetricConfiguration);


        AdherenceMetricConfiguration<String> adherenceMetricConfiguration = new AdherenceMetricConfiguration<>();
        adherenceMetricConfiguration.setDesiredExtent(6.5);
        adherenceMetricConfiguration.setDesiredExtent(0.2);
        adherenceMetricConfiguration.setMaximalAdherence(1.0);
        itemsetMinerConfiguration.setExtractionMetricConfiguration(adherenceMetricConfiguration);

        SeparationMetricConfiguration<String> separationMetricConfiguration = new SeparationMetricConfiguration<>();
        separationMetricConfiguration.setMaximalSeparation(50);
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
        significanceEstimatorConfiguration.setSampleSize(5);
        significanceEstimatorConfiguration.setSignificanceCutoff(0.001);
        significanceEstimatorConfiguration.setKsCutoff(0.1);
        itemsetMinerConfiguration.setSignificanceEstimatorConfiguration(significanceEstimatorConfiguration);

        new ItemsetMinerRunner(itemsetMinerConfiguration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithMmtfAndIntraChain() throws IOException, URISyntaxException {
        dataPointReaderConfiguration.setMmtf(true);
        itemsetMinerConfiguration.setDataPointEnricher(new IntraChainInteractionEnricher());
        new ItemsetMinerRunner(itemsetMinerConfiguration);
    }
}