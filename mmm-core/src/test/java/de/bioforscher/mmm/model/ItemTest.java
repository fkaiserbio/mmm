package de.bioforscher.mmm.model;

import de.bioforscher.singa.mathematics.vectors.Vectors3D;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.representations.RepresentationSchemeType;
import de.bioforscher.singa.structure.model.families.AminoAcidFamily;
import de.bioforscher.singa.structure.model.identifiers.LeafIdentifier;
import de.bioforscher.singa.structure.model.interfaces.AminoAcid;
import de.bioforscher.singa.structure.model.interfaces.Atom;
import de.bioforscher.singa.structure.model.oak.OakAminoAcid;
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
        //FIXME optional check
        assertEquals(aminoAcid.getAtomByName("CA").get().getPosition(), item.getPosition(RepresentationSchemeType.ALPHA_CARBON).orElseThrow(NoSuchElementException::new));
    }

    @Test
    public void shouldEqualBasedOnStringLabel() {
        Item<String> item1 = new Item<>("A", new OakAminoAcid(new LeafIdentifier(0), AminoAcidFamily.ALANINE));
        Item<String> item2 = new Item<>("A", new OakAminoAcid(new LeafIdentifier(1), AminoAcidFamily.ALANINE));
        assertEquals(item1, item2);
    }

    @Test
    public void shouldEqualBasedOnIntegerLabel() {
        Item<Integer> item1 = new Item<>(1, new OakAminoAcid(new LeafIdentifier(0), AminoAcidFamily.ALANINE));
        Item<Integer> item2 = new Item<>(1, new OakAminoAcid(new LeafIdentifier(0), AminoAcidFamily.ALANINE));
        assertEquals(item1, item2);
    }
}