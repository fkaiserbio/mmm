package de.bioforscher.mmm.classify.model;

import de.bioforscher.mmm.ItemsetMinerRunner;
import de.bioforscher.mmm.io.DataPointReaderConfiguration;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.CohesionMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SupportMetricConfiguration;
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
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException, URISyntaxException {
        SupportMetricConfiguration<String> supportMetricConfiguration = new SupportMetricConfiguration<>();
        supportMetricConfiguration.setMinimalSupport(0.8);
        itemsetMinerConfiguration.addSimpleMetricConfiguration(supportMetricConfiguration);
        CohesionMetricConfiguration<String> cohesionMetricConfiguration = new CohesionMetricConfiguration<>();
        cohesionMetricConfiguration.setMaximalCohesion(6.0);
        itemsetMinerConfiguration.setExtractionMetricConfiguration(cohesionMetricConfiguration);
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

}