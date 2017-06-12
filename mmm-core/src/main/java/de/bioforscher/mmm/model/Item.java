package de.bioforscher.mmm.model;

import de.bioforscher.singa.chemistry.physical.atoms.Atom;
import de.bioforscher.singa.chemistry.physical.atoms.representations.RepresentationScheme;
import de.bioforscher.singa.chemistry.physical.atoms.representations.RepresentationSchemeFactory;
import de.bioforscher.singa.chemistry.physical.atoms.representations.RepresentationSchemeType;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;
import de.bioforscher.singa.mathematics.vectors.Vector3D;

import java.util.Optional;

/**
 * @author fk
 */
public class Item<LabelType extends Comparable<LabelType>> implements Comparable<Item<LabelType>> {

    private LabelType label;
    private LeafSubstructure<?, ?> leafSubstructure;
    private int sequencePosition;

    public Item(LabelType label) {
        this.label = label;
    }

    public Item(LabelType label, LeafSubstructure<?, ?> leafSubstructure) {
        this.label = label;
        this.leafSubstructure = leafSubstructure;
        this.sequencePosition = leafSubstructure.getLeafIdentifier().getIdentifier();
    }

    public int getSequencePosition() {
        return sequencePosition;
    }

    public Optional<LeafSubstructure<?, ?>> getLeafSubstructure() {
        return Optional.ofNullable(leafSubstructure);
    }

    public void setLeafSubstructure(LeafSubstructure<?, ?> leafSubstructure) {
        this.leafSubstructure = leafSubstructure;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Item<?> item = (Item<?>) o;

        return label.equals(item.label);
    }

    @Override public int hashCode() {
        return label.hashCode();
    }

    public LabelType getLabel() {
        return label;
    }

    public void setLabel(LabelType label) {
        this.label = label;
    }

    @Override public String toString() {
        return label.toString();
    }

    /**
     * Returns the geometric center of all {@link Atom}s of the associated {@link LeafSubstructure} if any.
     *
     * @return Optional of the position.
     */
    public Optional<Vector3D> getPosition() {
        Vector3D position = null;
        // try to determine position
        if (leafSubstructure != null) {
            position = leafSubstructure.getPosition();
        }
        return Optional.ofNullable(position);
    }

    /**
     * Returns the position of the associated {@link LeafSubstructure} (if any) according to the given {@link RepresentationSchemeType}.
     *
     * @param representationSchemeType The {@link RepresentationSchemeType} used to determine the position.
     * @return Optional of the position.
     */
    public Optional<Vector3D> getPosition(RepresentationSchemeType representationSchemeType) {
        Vector3D position = null;
        if (leafSubstructure != null) {
            RepresentationScheme representationScheme = RepresentationSchemeFactory.createRepresentationScheme(representationSchemeType);
            position = representationScheme.determineRepresentingAtom(leafSubstructure).getPosition();
        }
        return Optional.ofNullable(position);
    }

    @Override public int compareTo(Item<LabelType> o) {
        return label.compareTo(o.getLabel());
    }

    public Item<LabelType> getCopy() {
        return new Item<>(label);
    }

    public Item<LabelType> getDeepCopy() {
        return new Item<>(label, leafSubstructure.getCopy());
    }

}
