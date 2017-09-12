package de.bioforscher.mmm.model.metrics.cohesion;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;
import de.bioforscher.singa.mathematics.matrices.LabeledSymmetricMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An implementation of the Vertex(One/All) algorithm according to
 * <p/>
 * C. Zhou, P. Meysman, B. Cule, K. Laukens and B.
 * Goethals, "Discovery of Spatially Cohesive Itemsets in Three-Dimensional Protein Structures," in IEEE/ACM
 * Transactions on Computational Biology and Bioinformatics, vol. 11, no. 5, pp. 814-825, Sept.-Oct. 2014.
 * doi: 10.1109/TCBB.2014.2311795
 *
 * @author fk
 */
public class VertexCandidateGenerator<LabelType extends Comparable<LabelType>> {
    private static final Logger logger = LoggerFactory.getLogger(VertexCandidateGenerator.class);
    private final List<Item<LabelType>> items;
    private final DataPoint<LabelType> dataPoint;
    private final LabeledSymmetricMatrix<Item<LabelType>> squaredDistanceMatrix;
    private final boolean vertexOne;

    public VertexCandidateGenerator(Itemset<LabelType> itemset, DataPoint<LabelType> dataPoint, LabeledSymmetricMatrix<Item<LabelType>> squaredDistanceMatrix, boolean vertexOne) {
        // TODO according to Zhou et al. 2007 this list is sorted (VertexOne: descending support, VertexAll: ascending support)
        items = new ArrayList<>(itemset.getItems());
        this.dataPoint = dataPoint;
        this.squaredDistanceMatrix = squaredDistanceMatrix;
        this.vertexOne = vertexOne;
    }

    private static <LabelType extends Comparable<LabelType>> boolean isRedundant(Itemset<LabelType> candidate, List<Itemset<LabelType>> candidates) {
        List<LeafSubstructure<?, ?>> candidateLeafSubstructures = candidate.getStructuralMotif()
                                                                           .orElseThrow(() -> new VertexCandidateGeneratorException("no structural motif found during redundancy check"))
                                                                           .getLeafSubstructures();
        for (Itemset<LabelType> reference : candidates) {
            List<LeafSubstructure<?, ?>> referenceLeafSubstructures = reference.getStructuralMotif()
                                                                               .orElseThrow(() -> new VertexCandidateGeneratorException("no structural motif found during redundancy check"))
                                                                               .getLeafSubstructures();
            if (candidateLeafSubstructures.equals(referenceLeafSubstructures)) {
                return true;
            }
        }
        return false;
    }

    public List<Itemset<LabelType>> generateCandidates() {
        // collect all matching data point items
        List<List<Item<LabelType>>> matchingDataPointItems = new ArrayList<>();
        for (Item<LabelType> item : items) {
            matchingDataPointItems.add(dataPoint.getItems().stream()
                                                .filter(dataPointItem -> dataPointItem.equals(item))
                                                .collect(Collectors.toList()));
        }

        // return empty list if items are missing in the data point
        if (matchingDataPointItems.stream().anyMatch(List::isEmpty)) {
            return new ArrayList<>();
        }

        // initialize empty candidate list
        List<Itemset<LabelType>> candidates = new ArrayList<>();

        // iterate over all matching data point items
        for (int i = 0; i < matchingDataPointItems.size(); i++) {

            // define list one
            List<Item<LabelType>> listOne = matchingDataPointItems.get(i);

            outerLoop:
            for (Item<LabelType> itemOne : listOne) {
                List<Item<LabelType>> candidateItems = new ArrayList<>();
                candidateItems.add(itemOne);
                for (int j = 0; j < matchingDataPointItems.size() - 1; j++) {

                    // determine pointer for back reference in outer loop
                    int pointer = (j + i + 1) % matchingDataPointItems.size();

                    List<Item<LabelType>> listTwo = matchingDataPointItems.get(pointer);

                    // determine closest item of list
                    Item<LabelType> closestItem = findClosestItem(itemOne, listTwo).orElseThrow(() -> new VertexCandidateGeneratorException("failed to determine closest item"));

                    // break candidate assembly if no closest item could be found
                    if (closestItem == null) {
                        continue outerLoop;
                    }

                    // add closest item to candidate
                    candidateItems.add(closestItem);
                }

                // create new candidate
                List<LeafSubstructure<?, ?>> leafSubstructures = candidateItems.stream()
                                                                               .map(Item::getLeafSubstructure)
                                                                               .filter(Optional::isPresent)
                                                                               .map(Optional::get)
                                                                               .collect(Collectors.toList());

                // leaf substructures are sorted based on the natural ordering of their labels
                TreeMap<LabelType, LeafSubstructure<?, ?>> labelMap = new TreeMap<>();
                for (int k = 0; k < candidateItems.size(); k++) {
                    labelMap.put(candidateItems.get(k).getLabel(), leafSubstructures.get(k));
                }
                List<LeafSubstructure<?, ?>> orderedLeafSubstructures = labelMap.entrySet().stream()
                                                                                .map(Map.Entry::getValue)
                                                                                .collect(Collectors.toList());
                // sort leaves based on three letter code
                // FIXME this has to be adapted when mapping rule is used such that sorting is based on mapped labels
//                leafSubstructures.sort(Comparator.comparing(leafSubstructure -> leafSubstructure.getFamily().getThreeLetterCode()));
                StructuralMotif structuralMotif = StructuralMotif.fromLeafSubstructures(orderedLeafSubstructures);

                Itemset<LabelType> candidate = new Itemset<>(new TreeSet<>(candidateItems), structuralMotif, dataPoint.getDataPointIdentifier());
                logger.trace("generated candidate {}", candidate);

                if (!isRedundant(candidate, candidates)) {
                    candidates.add(candidate);
                }
            }

            // break after first iteration if VertexOne heuristic specified
            if (vertexOne) {
                break;
            }
        }
        return candidates;
    }

    /**
     * Returns the closest {@link Item} of the given list of {@link Item}s in respect to the reference {@link Item}.
     *
     * @param reference The reference {@link Item} to which the closest {@link Item} should be determined.
     * @param items     The candidate {@link Item}s.
     * @return The closest {@link Item} if any.
     */
    private Optional<Item<LabelType>> findClosestItem(Item<LabelType> reference, List<Item<LabelType>> items) {
        Item<LabelType> closestItem = null;
        double closestSquaredDistance = Double.MAX_VALUE;
        for (Item<LabelType> item : items) {
            double squaredDistance = squaredDistanceMatrix.getValueForLabel(reference, item);
            if (squaredDistance < closestSquaredDistance) {
                closestSquaredDistance = squaredDistance;
                closestItem = item;
            }
        }
        return Optional.ofNullable(closestItem);
    }
}
