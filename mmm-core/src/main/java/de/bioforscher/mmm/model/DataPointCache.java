package de.bioforscher.mmm.model;

import de.bioforscher.singa.chemistry.physical.atoms.representations.RepresentationSchemeType;
import de.bioforscher.singa.mathematics.matrices.LabeledSymmetricMatrix;
import de.bioforscher.singa.mathematics.metrics.model.VectorMetricProvider;
import de.bioforscher.singa.mathematics.vectors.Vector3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fk
 */
public abstract class DataPointCache<LabelType extends Comparable<LabelType>> {

    private static final Logger logger = LoggerFactory.getLogger(DataPointCache.class);

    protected final Map<DataPoint<LabelType>, LabeledSymmetricMatrix<Item<LabelType>>> squaredDistanceMatrices;
    protected RepresentationSchemeType representationSchemeType;

    public DataPointCache(RepresentationSchemeType representationSchemeType) {
        this.representationSchemeType = representationSchemeType;
        // initialize cache for distance matrices
        squaredDistanceMatrices = new HashMap<>();
    }

    protected synchronized LabeledSymmetricMatrix<Item<LabelType>> obtainSquaredDistanceMatrix(DataPoint<LabelType> dataPoint) {
        // try to obtain cached distance matrix, otherwise calculate
        LabeledSymmetricMatrix<Item<LabelType>> squaredDistanceMatrix;
        if (squaredDistanceMatrices.containsKey(dataPoint)) {
            logger.trace("using stored squared distance matrix for data point {}", dataPoint);
            squaredDistanceMatrix = squaredDistanceMatrices.get(dataPoint);
        } else {
            logger.trace("calculating squared distance matrix for data point {} de novo", dataPoint);
            squaredDistanceMatrix = calculateSquaredDistanceMatrix(dataPoint);
            squaredDistanceMatrices.put(dataPoint, squaredDistanceMatrix);
        }
        return squaredDistanceMatrix;
    }

    private LabeledSymmetricMatrix<Item<LabelType>> calculateSquaredDistanceMatrix(DataPoint<LabelType> dataPoint) {
        List<Vector3D> itemPositions = new ArrayList<>();
        for (Item<LabelType> item : dataPoint.getItems()) {
            if (representationSchemeType != null) {
                item.getPosition(representationSchemeType).ifPresent(itemPositions::add);
            } else {
                item.getPosition().ifPresent(itemPositions::add);
            }
        }
        double[][] distanceValues = VectorMetricProvider.SQUARED_EUCLIDEAN_METRIC.calculateDistancesPairwise(itemPositions).getElements();
        LabeledSymmetricMatrix<Item<LabelType>> distanceMatrix = new LabeledSymmetricMatrix<>(distanceValues);
        distanceMatrix.setRowLabels(dataPoint.getItems());
        return distanceMatrix;
    }
}
