package de.bioforscher.mmm.model;

import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;
import de.bioforscher.singa.mathematics.vectors.Vector;
import de.bioforscher.singa.mathematics.vectors.Vector3D;
import de.bioforscher.singa.mathematics.vectors.Vectors;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fk
 */
public class Itemset<LabelType extends Comparable<LabelType>> implements Comparable<Itemset<LabelType>> {

    private static final String EVALUATION_METRIC_VALUES_DECIMAL_FORMAT = "0.0000";
    private static final DecimalFormat DECIMAL_FORMAT;

    static {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DECIMAL_FORMAT = (DecimalFormat) nf;
        DECIMAL_FORMAT.applyPattern(EVALUATION_METRIC_VALUES_DECIMAL_FORMAT);
    }

    private final Set<Item<LabelType>> items;
    private StructuralMotif structuralMotif;
    private Vector3D position;
    private double support;
    private double cohesion;
    private double adherence;
    private double consensus;
    private double separation;

    public Itemset(Set<Item<LabelType>> items) {
        this.items = items;
    }

    public Itemset(Set<Item<LabelType>> items, StructuralMotif structuralMotif) {
        this.items = items;
        this.structuralMotif = structuralMotif;
    }

    /**
     * Creates a new {@link Itemset} out of the given {@link Item}s.
     *
     * @param items       The {@link Item}s that should be part of the {@link Itemset}.
     * @param <LabelType> The type of label.
     * @return The new {@link Itemset}.
     */
    @SafeVarargs
    public static <LabelType extends Comparable<LabelType>> Itemset<LabelType> of(Item<LabelType>... items) {
        return new Itemset<>(Stream.of(items).collect(Collectors.toCollection(TreeSet::new)));
    }

    public static <LabelType extends Comparable<LabelType>> Itemset<LabelType> of(Set<Item<LabelType>> items) {
        return new Itemset<>(new TreeSet<>(items));
    }

    public double getAdherence() {
        return adherence;
    }

    public void setAdherence(double adherence) {
        this.adherence = adherence;
    }

    public double getSeparation() {
        return separation;
    }

    public void setSeparation(double separation) {
        this.separation = separation;
    }

    public double getConsensus() {
        return consensus;
    }

    public void setConsensus(double consensus) {
        this.consensus = consensus;
    }

    public double getCohesion() {
        return cohesion;
    }

    public void setCohesion(double cohesion) {
        this.cohesion = cohesion;
    }

    public double getSupport() {
        return support;
    }

//    @SafeVarargs
//    public static <LabelType extends Comparable<LabelType>> Itemset<LabelType> of(Item<LabelType>...items){
//        TreeSet<Item<LabelType>> itemList = Stream.of(items).collect(Collectors.toCollection(TreeSet::new));
//        for (Item<LabelType> item : itemList) {
//            item.setPosition(null);
//            item.setLeafSubstructure(null);
//        }
//        return new Itemset<>(itemList);
//    }
//
//    public static <LabelType extends Comparable<LabelType>> Itemset<LabelType> of(Set<Item<LabelType>> items){
//        for (Item<LabelType> item : items) {
//            item.setPosition(null);
//            item.setLeafSubstructure(null);
//        }
//        return new Itemset<>(new TreeSet<>(items));
//    }

    public void setSupport(double support) {
        this.support = support;
    }

    public Set<Item<LabelType>> getItems() {
        return items;
    }

    @Override public String toString() {
        return items.stream()
                    .map(Item::toString)
                    .collect(Collectors.joining("-", "{", "}"))
               + "["
               + "support=" + DECIMAL_FORMAT.format(support)
               + ",cohesion=" + ((cohesion == Double.MAX_VALUE) ? "?" : DECIMAL_FORMAT.format(cohesion))
               + ",adherence=" + ((adherence == Double.MAX_VALUE) ? "?" : DECIMAL_FORMAT.format(adherence))
               + ",consensus=" + DECIMAL_FORMAT.format(consensus)
               + ",separation=" + DECIMAL_FORMAT.format(separation)
               + "]";
    }

    public String toSimpleString() {
        return items.stream()
                    .map(Item::toString)
                    .collect(Collectors.joining("-"));
    }

    public Optional<StructuralMotif> getStructuralMotif() {
        // try to construct structural motif
        if (structuralMotif == null) {
            List<LeafSubstructure<?, ?>> leafSubstructures = items.stream()
                                                                  .map(Item::getLeafSubstructure)
                                                                  .filter(Optional::isPresent)
                                                                  .map(Optional::get)
                                                                  .collect(Collectors.toList());

            // sort leaves based on three letter code
            leafSubstructures.sort(Comparator.comparing(leafSubstructure -> leafSubstructure.getFamily().getThreeLetterCode()));
            if (!leafSubstructures.isEmpty()) {
                structuralMotif = StructuralMotif.fromLeaves(leafSubstructures);
            }
        }
        return Optional.ofNullable(structuralMotif);
    }

    public Optional<Vector3D> getPosition() {
        // try to determine position
        if (position == null) {
            List<Vector> allItems = items.stream()
                                         .map(Item::getPosition)
                                         .filter(Optional::isPresent)
                                         .map(Optional::get)
                                         .collect(Collectors.toList());
            if (!allItems.isEmpty()) {
                position = Vectors.getCentroid(allItems).as(Vector3D.class);
            }
        }
        return Optional.ofNullable(position);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Itemset<?> itemset = (Itemset<?>) o;

        return items.equals(itemset.items);
    }

    @Override public int hashCode() {
        return items.hashCode();
    }

    public Itemset<LabelType> getCopy() {
        return new Itemset<>(items.stream().map(Item::getCopy).collect(Collectors.toSet()));
    }

    public Itemset<LabelType> getDeepCopy() {
        return new Itemset<>(items.stream().map(Item::getDeepCopy).collect(Collectors.toSet()), structuralMotif.getCopy());
    }

    @Override public int compareTo(Itemset<LabelType> o) {
        return Integer.compare(items.size(), o.getItems().size());
    }
}
