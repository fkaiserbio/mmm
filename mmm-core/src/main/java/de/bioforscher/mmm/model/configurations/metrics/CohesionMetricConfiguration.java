package de.bioforscher.mmm.model.configurations.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.configurations.Jsonizable;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.representations.RepresentationSchemeType;

/**
 * The {@link Jsonizable} configuration of the {@link de.bioforscher.mmm.model.metrics.CohesionMetric}.
 *
 * @author fk
 */
@JsonTypeName("COHESION")
public class CohesionMetricConfiguration<LabelType extends Comparable<LabelType>> implements ExtractionMetricConfiguration<LabelType>, Jsonizable<CohesionMetricConfiguration> {

    /**
     * the default maximal cohesion allowed
     */
    public static final double DEFAULT_MAXIMAL_COHESION = 10.0;

    /**
     * the default level of parallelism
     */
    public static final int DEFAULT_LEVEL_OF_PARALLELISM = -1;

    @JsonProperty("maximal-cohesion")
    private double maximalCohesion = DEFAULT_MAXIMAL_COHESION;
    @JsonProperty("vertex-one")
    private boolean vertexOne;
    @JsonProperty("level-of-parallelism")
    private int levelOfParallelism = DEFAULT_LEVEL_OF_PARALLELISM;
    @JsonProperty("representation-scheme-type")
    private RepresentationSchemeType representationSchemeType;

    public RepresentationSchemeType getRepresentationSchemeType() {
        return representationSchemeType;
    }

    public void setRepresentationSchemeType(RepresentationSchemeType representationSchemeType) {
        this.representationSchemeType = representationSchemeType;
    }

    public int getLevelOfParallelism() {
        return levelOfParallelism;
    }

    public void setLevelOfParallelism(int levelOfParallelism) {
        this.levelOfParallelism = levelOfParallelism;
    }

    public boolean isVertexOne() {
        return vertexOne;
    }

    public void setVertexOne(boolean vertexOne) {
        this.vertexOne = vertexOne;
    }

    public double getMaximalCohesion() {
        return maximalCohesion;
    }

    public void setMaximalCohesion(double maximalCohesion) {
        this.maximalCohesion = maximalCohesion;
    }

    @Override public String toString() {
        return "CohesionMetricConfiguration{" +
               "maximalCohesion=" + maximalCohesion +
               ", vertexOne=" + vertexOne +
               ", levelOfParallelism=" + levelOfParallelism +
               ", representationSchemeType=" + representationSchemeType +
               '}';
    }
}
