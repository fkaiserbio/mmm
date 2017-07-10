package de.bioforscher.mmm.model.analysis.association;

import de.bioforscher.mmm.model.Itemset;
import org.junit.Test;

/**
 * @author fk
 */
public class MutualInformationTest {

    @Test
    public void shouldComputeMutualInformation() {
        Itemset<String> itemsetOne = new Itemset<>(null);
        Itemset<String> itemsetTwo = new Itemset<>(null);
        itemsetOne.setSupport(0.98);
        itemsetTwo.setSupport(0.93);
        MutualInformation mutualInformation = new MutualInformation(itemsetOne, itemsetTwo);

    }
}