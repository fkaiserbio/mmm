package bio.fkaiser.mmm.model.configurations.metrics;

import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.configurations.Jsonizable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.representations.RepresentationSchemeType;

/**
 * The {@link Jsonizable} configuration of the {@link bio.fkaiser.mmm.model.metrics.AdherenceMetric}.
 *
 * @author fk
 */
@JsonTypeName("ADHERENCE")
public class AdherenceMetricConfiguration<LabelType extends Comparable<LabelType>> implements ExtractionMetricConfiguration<LabelType>, Jsonizable<AdherenceMetricConfiguration> {

    /**
     * the default desired extent
     */
    public static final double DEFAULT_DESIRED_EXTENT = 8.0;

    /**
     * the default delta that is tolerated for the desired extent
     */
    public static final double DEFAULT_DESIRED_EXTENT_DELTA = 1.0;

    /**
     * the default maximal adherence for an itemset
     */
    public static final double DEFAULT_MAXIMAL_ADHERENCE = 0.3;

    /**
     * the default value if VertexOne heuristic should be used
     */
    public static final boolean DEFAULT_VERTEX_ONE = false;

    /**
     * the default minimal observations for an {@link Itemset} to be evaluated positive
     */
    public static final int MINIMAL_OBSERVATIONS = 3;

    /**
     * the default level of parallelism
     */
    public static final int DEFAULT_LEVEL_OF_PARALLELISM = -1;

    @JsonProperty("level-of-parallelism")
    private int levelOfParallelism = DEFAULT_LEVEL_OF_PARALLELISM;
    @JsonProperty("desired-extent")
    private double desiredExtent = DEFAULT_DESIRED_EXTENT;
    @JsonProperty("desired-extent-delta")
    private double desiredExtentDelta = DEFAULT_DESIRED_EXTENT_DELTA;
    @JsonProperty("maximal-adherence")
    private double maximalAdherence = DEFAULT_MAXIMAL_ADHERENCE;
    @JsonProperty("minimal-observations")
    private int minimalObservations = MINIMAL_OBSERVATIONS;
    @JsonProperty("vertex-one")
    private boolean vertexOne = DEFAULT_VERTEX_ONE;
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

    public double getDesiredExtent() {
        return desiredExtent;
    }

    public void setDesiredExtent(double desiredExtent) {
        this.desiredExtent = desiredExtent;
    }

    public double getDesiredExtentDelta() {
        return desiredExtentDelta;
    }

    public void setDesiredExtentDelta(double desiredExtentDelta) {
        this.desiredExtentDelta = desiredExtentDelta;
    }

    public double getMaximalAdherence() {
        return maximalAdherence;
    }

    public void setMaximalAdherence(double maximalAdherence) {
        this.maximalAdherence = maximalAdherence;
    }

    public int getMinimalObservations() {
        return minimalObservations;
    }

    public void setMinimalObservations(int minimalObservations) {
        this.minimalObservations = minimalObservations;
    }

    public boolean isVertexOne() {
        return vertexOne;
    }

    public void setVertexOne(boolean vertexOne) {
        this.vertexOne = vertexOne;
    }

    @Override
    public String toString() {
        return "AdherenceMetricConfiguration{" +
               "levelOfParallelism=" + levelOfParallelism +
               ", desiredExtent=" + desiredExtent +
               ", desiredExtentDelta=" + desiredExtentDelta +
               ", maximalAdherence=" + maximalAdherence +
               ", minimalObservations=" + minimalObservations +
               ", vertexOne=" + vertexOne +
               ", representationSchemeType=" + representationSchemeType +
               '}';
    }
}
