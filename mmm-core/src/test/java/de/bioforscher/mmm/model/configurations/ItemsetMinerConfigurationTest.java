package de.bioforscher.mmm.model.configurations;

import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.enrichment.DataPointEnricherType;
import org.junit.Test;

/**
 * @author fk
 */
public class ItemsetMinerConfigurationTest {

    @Test
    public void shouldSerialize(){
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        itemsetMinerConfiguration.setInputListLocation("PF00127_chains.txt");
        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.CONSENSUS);
        itemsetMinerConfiguration.setDataPointEnricherType(DataPointEnricherType.INTERACTION);
        itemsetMinerConfiguration.setOutputLocation("/tmp/itemset-miner");
    }
}