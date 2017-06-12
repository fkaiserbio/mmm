package de.bioforscher.mmm.model;

import de.bioforscher.singa.chemistry.physical.families.AminoAcidFamily;
import de.bioforscher.singa.chemistry.physical.leaves.AminoAcid;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author fk
 */
public class ItemTest {

    @Test
    public void shouldEqualBasedOnLabel() {
        Item<String> item1 = new Item<>("A", new AminoAcid(0, AminoAcidFamily.ALANINE));
        Item<String> item2 = new Item<>("A", new AminoAcid(1, AminoAcidFamily.ALANINE));
        assertEquals(item1, item2);
    }
}