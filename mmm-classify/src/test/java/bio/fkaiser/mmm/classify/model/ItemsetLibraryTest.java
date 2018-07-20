package bio.fkaiser.mmm.classify.model;

import bio.fkaiser.mmm.ItemsetMinerRunner;
import bio.fkaiser.mmm.io.DataPointReaderConfiguration;
import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.ItemsetComparatorType;
import bio.fkaiser.mmm.model.configurations.ItemsetMinerConfiguration;
import bio.fkaiser.mmm.model.configurations.metrics.AffinityMetricConfiguration;
import bio.fkaiser.mmm.model.configurations.metrics.CohesionMetricConfiguration;
import bio.fkaiser.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import bio.fkaiser.mmm.model.configurations.metrics.SupportMetricConfiguration;
import bio.singa.structure.algorithms.superimposition.affinity.AffinityAlignment;
import bio.singa.structure.model.oak.OakStructure;
import bio.singa.structure.parser.pdb.structures.StructureParser;
import bio.singa.structure.parser.pdb.structures.StructureWriter;
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
        itemsetMinerConfiguration.setOutputLocation(folder.getRoot().toString() + "/mmm");
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
            OakStructure structure = (OakStructure) StructureParser.local()
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
        affinityMetricConfiguration.setLevelOfParallelism(1);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(affinityMetricConfiguration);
        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.AFFINITY);
        itemsetMinerConfiguration.setMaximalEpochs(3);
        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);
        TreeMap<Itemset<String>, AffinityAlignment> totalAffinityItemsets = itemsetMinerRunner.getItemsetMiner().getTotalAffinityItemsets();
        ItemsetLibrary itemsetLibrary = ItemsetLibrary.of(totalAffinityItemsets, 3);
        for (ItemsetLibraryEntry entry : itemsetLibrary.getEntries()) {
            OakStructure structure = (OakStructure) StructureParser.local()
                                                                   .inputStream(new ByteArrayInputStream(entry.getPdbLines().getBytes()))
                                                                   .parse();
            StructureWriter.writeStructure(structure, Paths.get(folder.getRoot() + "/" + entry.getIdentifier() + ".pdb"));
        }
        itemsetLibrary.writeToPath(Paths.get(folder.getRoot() + "/library.gz"));
        ItemsetLibrary deserializedLibrary = ItemsetLibrary.readFromPath(Paths.get(folder.getRoot() + "/library.gz"));
        assertEquals(itemsetLibrary.getEntries().size(), deserializedLibrary.getEntries().size());
    }

}