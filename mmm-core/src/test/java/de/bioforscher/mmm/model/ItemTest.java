package de.bioforscher.mmm.model;

import de.bioforscher.singa.chemistry.physical.atoms.Atom;
import de.bioforscher.singa.chemistry.physical.atoms.representations.RepresentationSchemeType;
import de.bioforscher.singa.chemistry.physical.families.AminoAcidFamily;
import de.bioforscher.singa.chemistry.physical.leaves.AminoAcid;
import de.bioforscher.singa.mathematics.vectors.Vectors3D;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author fk
 */
public class ItemTest {

    @Test
    public void shouldGetPosition() {
        AminoAcid aminoAcid = AminoAcidFamily.GLYCINE.getPrototype();
        Item<String> item = new Item<>("A", aminoAcid);
        assertEquals(Vectors3D.getCentroid(aminoAcid.getAllAtoms().stream()
                                                    .map(Atom::getPosition)
                                                    .collect(Collectors.toList())),
                     item.getPosition().orElseThrow(NoSuchElementException::new));
    }

    @Test
    public void shouldGetPositionWithRepresentationScheme() {
        AminoAcid aminoAcid = AminoAcidFamily.GLYCINE.getPrototype();
        Item<String> item = new Item<>("A", aminoAcid);
        assertEquals(aminoAcid.getAlphaCarbon().getPosition(), item.getPosition(RepresentationSchemeType.CA).orElseThrow(NoSuchElementException::new));
    }

    @Test
    public void shouldEqualBasedOnStringLabel() {
        Item<String> item1 = new Item<>("A", new AminoAcid(0, AminoAcidFamily.ALANINE));
        Item<String> item2 = new Item<>("A", new AminoAcid(1, AminoAcidFamily.ALANINE));
        assertEquals(item1, item2);
    }

    @Test
    public void shouldEqualBasedOnIntegerLabel() {
        Item<Integer> item1 = new Item<>(1, new AminoAcid(0, AminoAcidFamily.ALANINE));
        Item<Integer> item2 = new Item<>(1, new AminoAcid(0, AminoAcidFamily.ALANINE));
        assertEquals(item1, item2);
    }
}