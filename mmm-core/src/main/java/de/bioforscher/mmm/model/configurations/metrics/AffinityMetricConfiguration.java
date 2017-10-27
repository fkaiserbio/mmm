package de.bioforscher.mmm.model.configurations.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.configurations.Jsonizable;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.representations.RepresentationSchemeType;
import de.bioforscher.singa.structure.model.oak.StructuralEntityFilter.AtomFilterType;

/**
 * The {@link Jsonizable} configuration of the {@link de.bioforscher.mmm.model.metrics.ConsensusMetric}.
 *
 * @author fk
 */
@JsonTypeName("AFFINITY")
public class AffinityMetricConfiguration<LabelType extends Comparable<LabelType>> implements ExtractionDependentMetricConfiguration<LabelType>, Jsonizable<AffinityMetricConfiguration> {

    /**
     * the default minimal affinity required
     */
    public static final double DEFAULT_MAXIMAL_AFFINITY = 1.0;

    /**
     * the default level of parallelism
     */
    public static final int DEFAULT_LEVEL_OF_PARALLELISM = -1;

    /**
     * the default {@link AtomFilterType} that will be used
     */
    public static final AtomFilterType DEFAULT_ATOM_FILTER_TYPE = AtomFilterType.ARBITRARY;

    @JsonProperty("maximal-affinity")
    private double maximalAffinity = DEFAULT_MAXIMAL_AFFINITY;
    @JsonProperty("level-of-parallelism")
    private int levelOfParallelism = DEFAULT_LEVEL_OF_PARALLELISM;
    @JsonProperty("atom-filter-type")
    private AtomFilterType atomFilterType = DEFAULT_ATOM_FILTER_TYPE;
    @JsonProperty("representation-scheme-type")
    private RepresentationSchemeType representationSchemeType;
    @JsonProperty("align-within-clusters")
    private boolean alignWithinClusters;

    public RepresentationSchemeType getRepresentationSchemeType() {
        return representationSchemeType;
    }

    public void setRepresentationSchemeType(RepresentationSchemeType representationSchemeType) {
        this.representationSchemeType = representationSchemeType;
    }

    public double getMaximalAffinity() {
        return maximalAffinity;
    }

    public void setMaximalAffinity(double maximalAffinity) {
        this.maximalAffinity = maximalAffinity;
    }

    public int getLevelOfParallelism() {
        return levelOfParallelism;
    }

    public void setLevelOfParallelism(int levelOfParallelism) {
        this.levelOfParallelism = levelOfParallelism;
    }

    public AtomFilterType getAtomFilterType() {
        return atomFilterType;
    }

    public void setAtomFilterType(AtomFilterType atomFilterType) {
        this.atomFilterType = atomFilterType;
    }

    public boolean isAlignWithinClusters() {
        return alignWithinClusters;
    }

    public void setAlignWithinClusters(boolean alignWithinClusters) {
        this.alignWithinClusters = alignWithinClusters;
    }

    @Override public String toString() {
        return "AffinityMetricConfiguration{" +
               "maximalAffinity=" + maximalAffinity +
               ", levelOfParallelism=" + levelOfParallelism +
               ", atomFilterType=" + atomFilterType +
               ", representationSchemeType=" + representationSchemeType +
               ", alignWithinClusters=" + alignWithinClusters +
               '}';
    }
}
