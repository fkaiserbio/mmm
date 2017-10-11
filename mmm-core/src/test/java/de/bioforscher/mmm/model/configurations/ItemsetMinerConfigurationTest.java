package de.bioforscher.mmm.model.configurations;

import de.bioforscher.mmm.model.ItemsetComparatorType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * @author fk
 */
public class ItemsetMinerConfigurationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        itemsetMinerConfiguration.setInputListLocation("input_list.txt");
        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.CONSENSUS);
        itemsetMinerConfiguration.setOutputLocation("/tmp/itemset-miner");
        itemsetMinerConfiguration.setMaximalEpochs(-1);
        Path configurationPath = folder.getRoot().toPath().resolve("itemset-miner-configuration.json");
        Files.write(configurationPath, itemsetMinerConfiguration.toJson().getBytes());
        ItemsetMinerConfiguration<?> deserializedConfiguration = ItemsetMinerConfiguration.from(configurationPath);
        assertEquals(itemsetMinerConfiguration.getItemsetComparatorType(), deserializedConfiguration.getItemsetComparatorType());
        assertEquals(itemsetMinerConfiguration.getInputListLocation(), deserializedConfiguration.getInputListLocation());
        assertEquals(itemsetMinerConfiguration.getMaximalEpochs(), deserializedConfiguration.getMaximalEpochs());
        assertEquals(itemsetMinerConfiguration.getOutputLocation(), deserializedConfiguration.getOutputLocation());
    }
}