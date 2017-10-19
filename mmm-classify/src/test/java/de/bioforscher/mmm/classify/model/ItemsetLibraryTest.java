package de.bioforscher.mmm.classify.model;

import de.bioforscher.mmm.ItemsetMinerRunner;
import de.bioforscher.mmm.io.DataPointReaderConfiguration;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.analysis.statistics.SignificanceEstimatorType;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.analysis.statistics.SignificanceEstimatorConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.AffinityMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.CohesionMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SupportMetricConfiguration;
import de.bioforscher.singa.chemistry.algorithms.superimposition.affinity.AffinityAlignment;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureWriter;
import de.bioforscher.singa.chemistry.physical.model.Structure;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

/**
 * @author fk
 */
public class ItemsetLibraryTest {

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
        SupportMetricConfiguration<String> supportMetricConfiguration = new SupportMetricConfiguration<>();
        supportMetricConfiguration.setMinimalSupport(0.8);
        itemsetMinerConfiguration.addSimpleMetricConfiguration(supportMetricConfiguration);
        CohesionMetricConfiguration<String> cohesionMetricConfiguration = new CohesionMetricConfiguration<>();
        cohesionMetricConfiguration.setMaximalCohesion(6.0);
        itemsetMinerConfiguration.setExtractionMetricConfiguration(cohesionMetricConfiguration);
    }

    @Test
    public void shouldSerializeAndDeserializeWithConsensus() throws IOException, URISyntaxException {
        ConsensusMetricConfiguration<String> consensusMetricConfiguration = new ConsensusMetricConfiguration<>();
        consensusMetricConfiguration.setMaximalConsensus(1.0);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(consensusMetricConfiguration);
        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);
        ItemsetLibrary itemsetLibrary = ItemsetLibrary.of(itemsetMinerRunner.getItemsetMiner().getTotalClusteredItemsets(), 2, 0.1);
        for (ItemsetLibraryEntry entry : itemsetLibrary.getEntries()) {
            Structure structure = StructureParser.local()
                                                 .inputStream(new ByteArrayInputStream(entry.getPdbLines().getBytes()))
                                                 .parse();
            StructureWriter.writeStructure(structure, Paths.get(folder.getRoot() + "/" + entry.getIdentifier() + ".pdb"));
        }
        itemsetLibrary.writeToPath(Paths.get(folder.getRoot() + "/library.gz"));
        ItemsetLibrary deserializedLibrary = ItemsetLibrary.readFromPath(Paths.get(folder.getRoot() + "/library.gz"));
        assertEquals(itemsetLibrary.getEntries().size(), deserializedLibrary.getEntries().size());
    }

    @Test
    public void shouldSerializeAndDeserializeWithAffinity() throws IOException, URISyntaxException {
        AffinityMetricConfiguration<String> affinityMetricConfiguration = new AffinityMetricConfiguration<>();
        affinityMetricConfiguration.setMaximalAffinity(1.0);
        affinityMetricConfiguration.setAlignWithinClusters(true);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(affinityMetricConfiguration);
        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.AFFINITY);
        SignificanceEstimatorConfiguration significanceEstimatorConfiguration = new SignificanceEstimatorConfiguration();
        significanceEstimatorConfiguration.setSignificanceType(SignificanceEstimatorType.AFFINITY);
        itemsetMinerConfiguration.setSignificanceEstimatorConfiguration(significanceEstimatorConfiguration);
        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);
        TreeMap<Itemset<String>, AffinityAlignment> totalAffinityItemsets = itemsetMinerRunner.getItemsetMiner().getTotalAffinityItemsets();
        totalAffinityItemsets.keySet().removeIf(itemset -> !itemsetMinerRunner.getSignificantItemsets().values().contains(itemset));
        ItemsetLibrary itemsetLibrary = ItemsetLibrary.of(totalAffinityItemsets, 3);
        for (ItemsetLibraryEntry entry : itemsetLibrary.getEntries()) {
            Structure structure = StructureParser.local()
                                                 .inputStream(new ByteArrayInputStream(entry.getPdbLines().getBytes()))
                                                 .parse();
            StructureWriter.writeStructure(structure, Paths.get(folder.getRoot() + "/" + entry.getIdentifier() + ".pdb"));
        }
        System.out.println(itemsetLibrary.toJson());
        itemsetLibrary.writeToPath(Paths.get(folder.getRoot() + "/library.gz"));
        ItemsetLibrary deserializedLibrary = ItemsetLibrary.readFromPath(Paths.get(folder.getRoot() + "/library.gz"));
        assertEquals(itemsetLibrary.getEntries().size(), deserializedLibrary.getEntries().size());
    }

}