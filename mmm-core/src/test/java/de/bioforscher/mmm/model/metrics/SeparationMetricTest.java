package de.bioforscher.mmm.model.metrics;

import de.bioforscher.mmm.ItemsetMinerRunner;
import de.bioforscher.mmm.io.DataPointReaderConfiguration;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
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

import static org.junit.Assert.assertEquals;

/**
 * @author fk
 */
public class SeparationMetricTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private ItemsetMinerConfiguration<String> itemsetMinerConfiguration;

    @Before
    public void setUp() {
        itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        itemsetMinerConfiguration.setMaximalEpochs(3);
        itemsetMinerConfiguration.setInputDirectoryLocation("src/test/resources/PF00127");
        itemsetMinerConfiguration.setOutputLocation(folder.getRoot().toString());
        DataPointReaderConfiguration dataPointReaderConfiguration = new DataPointReaderConfiguration();
        itemsetMinerConfiguration.setDataPointReaderConfiguration(dataPointReaderConfiguration);
    }

    @Test
    public void calculateSeparationMetricWithRenumberedStructures() throws IOException, URISyntaxException {

        itemsetMinerConfiguration.setMaximalEpochs(3);

        SupportMetricConfiguration<String> supportMetricConfiguration = new SupportMetricConfiguration<>();
        supportMetricConfiguration.setMinimalSupport(0.9);
        itemsetMinerConfiguration.addSimpleMetricConfiguration(supportMetricConfiguration);

        CohesionMetricConfiguration<String> cohesionMetricConfiguration = new CohesionMetricConfiguration<>();
        cohesionMetricConfiguration.setMaximalCohesion(10.0);
        cohesionMetricConfiguration.setVertexOne(false);
        itemsetMinerConfiguration.setExtractionMetricConfiguration(cohesionMetricConfiguration);

        SeparationMetricConfiguration<String> separationMetricConfiguration = new SeparationMetricConfiguration<>();
        separationMetricConfiguration.setMaximalSeparation(100);
        separationMetricConfiguration.setOptimalSeparation(5);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(separationMetricConfiguration);

        ConsensusMetricConfiguration<String> consensusMetricConfiguration = new ConsensusMetricConfiguration<>();
        consensusMetricConfiguration.setMaximalConsensus(0.6);
        consensusMetricConfiguration.setClusterCutoffValue(0.5);
        consensusMetricConfiguration.setAlignWithinClusters(true);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(consensusMetricConfiguration);
        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.SEPARATION);

        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);
        Itemset<String> topScoringItemset = itemsetMinerRunner.getItemsetMiner().getTotalClusteredItemsets().firstEntry().getKey();

        DataPointReaderConfiguration dataPointReaderConfiguration = new DataPointReaderConfiguration();
        dataPointReaderConfiguration.setConsecutiveSequenceNumbering(true);
        itemsetMinerConfiguration.setDataPointReaderConfiguration(dataPointReaderConfiguration);
        itemsetMinerConfiguration.setInputDirectoryLocation("src/test/resources/PF00127_renumbered");
        itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);

        Itemset<String> topScoringConsecutiveItemset = itemsetMinerRunner.getItemsetMiner().getTotalClusteredItemsets().firstEntry().getKey();

        assertEquals(topScoringItemset.getSeparation(), topScoringConsecutiveItemset.getSeparation(), 1E-6);
        assertEquals(topScoringItemset.getConsensus(), topScoringConsecutiveItemset.getConsensus(), 1E-6);
    }
}