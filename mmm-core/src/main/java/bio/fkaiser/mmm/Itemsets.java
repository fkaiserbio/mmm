package bio.fkaiser.mmm;

import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.Itemset;
import bio.singa.core.utility.Pair;
import bio.singa.mathematics.matrices.Matrices;
import bio.singa.mathematics.matrices.Matrix;
import bio.singa.mathematics.matrices.SymmetricMatrix;
import bio.singa.mathematics.metrics.model.VectorMetricProvider;
import bio.singa.mathematics.vectors.Vector3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods for {@link Itemset}s.
 *
 * @author fk
 */
public class Itemsets {
    public static double calculateMaximalSquaredExtent(Itemset<?> itemset) {

        List<Vector3D> itemPositions = new ArrayList<>();
        for (Item<?> item : itemset.getItems()) {
            item.getPosition().ifPresent(itemPositions::add);
            // TODO representation scheme should be used if specified
//            if (representationSchemeType != null) {
//                item.getPosition(representationSchemeType).ifPresent(itemPositions::add);
//            } else {
//                item.getPosition().ifPresent(itemPositions::add);
//            }
        }
        double[][] distanceValues = VectorMetricProvider.SQUARED_EUCLIDEAN_METRIC.calculateDistancesPairwise(itemPositions).getElements();
        Matrix distanceMatrix = new SymmetricMatrix(distanceValues);
        Pair<Integer> positionOfMaximalElement = Matrices.getPositionsOfMaximalElement(distanceMatrix).stream()
                                                         .findFirst()
                                                         .orElseThrow(() -> new IllegalArgumentException("could not determine extent itemset " + itemset));
        return distanceMatrix.getElement(positionOfMaximalElement.getFirst(), positionOfMaximalElement.getSecond());
    }

    public static boolean containsSharedItems(Itemset<?> itemsetOne, Itemset<?> itemsetTwo) {
        return Collections.disjoint(itemsetOne.getItems(), itemsetTwo.getItems());
    }
}
