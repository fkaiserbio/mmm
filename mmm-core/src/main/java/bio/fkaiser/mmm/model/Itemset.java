package bio.fkaiser.mmm.model;

import de.bioforscher.singa.mathematics.vectors.Vector;
import de.bioforscher.singa.mathematics.vectors.Vector3D;
import de.bioforscher.singa.mathematics.vectors.Vectors;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of an itemset consisting of {@link Item}s. {@link Itemset}s are equal if all their labels are, independent of their order.
 *
 * @author fk
 */
public class Itemset<LabelType extends Comparable<LabelType>> implements Comparable<Itemset<LabelType>> {

    public static final String CSV_HEADER = "items,p-value,ks,support,cohesion,adherence,consensus,affinity,separation\n";

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
    private DataPointIdentifier originDataPointIdentifier;
    private double support;
    private double cohesion;
    private double adherence;
    private double consensus;
    private double affinity;
    private double separation;

    /**
     * storage for statistical model values
     */
    private double pValue;
    private double ks;

    public Itemset(Set<Item<LabelType>> items) {
        this.items = items;
    }

    public Itemset(Set<Item<LabelType>> items, StructuralMotif structuralMotif) {
        this(items);
        this.structuralMotif = structuralMotif;
    }

    public Itemset(Set<Item<LabelType>> items, StructuralMotif structuralMotif, DataPointIdentifier originDataPointIdentifier) {
        this(items, structuralMotif);
        this.originDataPointIdentifier = originDataPointIdentifier;
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

    @Override
    public String toString() {
        return items.stream()
                    .map(Item::toString)
                    .collect(Collectors.joining("-", "{", "}"))
               + "["
               + "support=" + DECIMAL_FORMAT.format(support)
               + ",cohesion=" + ((cohesion == Double.MAX_VALUE) ? "?" : DECIMAL_FORMAT.format(cohesion))
               + ",adherence=" + ((adherence == Double.MAX_VALUE) ? "?" : DECIMAL_FORMAT.format(adherence))
               + ",consensus=" + DECIMAL_FORMAT.format(consensus)
               + ",affinity=" + DECIMAL_FORMAT.format(affinity)
               + ",separation=" + DECIMAL_FORMAT.format(separation)
               + "]";
    }

    /**
     * Returns a CSV representation of this {@link Itemset}.
     *
     * @return The CSV representation as string.
     */
    public String toCSVLine() {
        return items.stream()
                    .map(Item::toString)
                    .collect(Collectors.joining("-", "", ""))
               + "," + pValue
               + "," + ks
               + "," + support
               + "," + ((cohesion == Double.MAX_VALUE) ? "?" : cohesion)
               + "," + ((adherence == Double.MAX_VALUE) ? "?" : adherence)
               + "," + consensus
               + "," + affinity
               + "," + separation;
    }

    public String toSimpleString() {
        return items.stream()
                    .map(Item::toString)
                    .collect(Collectors.joining("-"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Itemset<?> itemset = (Itemset<?>) o;

        return items.equals(itemset.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public int compareTo(Itemset<LabelType> o) {
        return Integer.compare(items.size(), o.getItems().size());
    }

    public double getAdherence() {
        return adherence;
    }

    public void setAdherence(double adherence) {
        this.adherence = adherence;
    }

    public double getAffinity() {
        return affinity;
    }

    public void setAffinity(double affinity) {
        this.affinity = affinity;
    }

    public double getCohesion() {
        return cohesion;
    }

    public void setCohesion(double cohesion) {
        this.cohesion = cohesion;
    }

    public double getConsensus() {
        return consensus;
    }

    public void setConsensus(double consensus) {
        this.consensus = consensus;
    }

    public Itemset<LabelType> getCopy() {
        return new Itemset<>(items.stream().map(Item::getCopy).collect(Collectors.toCollection(TreeSet::new)));
    }

    public Itemset<LabelType> getDeepCopy() {
        return new Itemset<>(items.stream().map(Item::getDeepCopy).collect(Collectors.toSet()), structuralMotif.getCopy());
    }

    public Set<Item<LabelType>> getItems() {
        return items;
    }

    public double getKs() {
        return ks;
    }

    public void setKs(double ks) {
        this.ks = ks;
    }

    public Optional<DataPointIdentifier> getOriginDataPointIdentifier() {
        return Optional.ofNullable(originDataPointIdentifier);
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

    public double getSeparation() {
        return separation;
    }

    public void setSeparation(double separation) {
        this.separation = separation;
    }

    public Optional<StructuralMotif> getStructuralMotif() {
        // try to construct structural motif
        if (structuralMotif == null) {
            List<LeafSubstructure<?>> leafSubstructures = items.stream()
                                                               .map(Item::getLeafSubstructure)
                                                               .filter(Optional::isPresent)
                                                               .map(Optional::get)
                                                               .collect(Collectors.toList());

            // sort leaves based on three letter code
            // FIXME adapt sorting from VCG
            leafSubstructures.sort(Comparator.comparing(leafSubstructure -> leafSubstructure.getFamily().getThreeLetterCode()));
            if (!leafSubstructures.isEmpty()) {
                structuralMotif = StructuralMotif.fromLeafSubstructures(leafSubstructures);
            }
        }
        return Optional.ofNullable(structuralMotif);
    }

    public double getSupport() {
        return support;
    }

    public void setSupport(double support) {
        this.support = support;
    }

    public double getpValue() {
        return pValue;
    }

    public void setpValue(double pValue) {
        this.pValue = pValue;
    }
}
