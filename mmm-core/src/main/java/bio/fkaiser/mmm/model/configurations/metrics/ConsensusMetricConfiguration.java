package bio.fkaiser.mmm.model.configurations.metrics;

import bio.fkaiser.mmm.model.configurations.Jsonizable;
import bio.fkaiser.mmm.model.metrics.ConsensusMetric;
import bio.singa.structure.algorithms.superimposition.fit3d.representations.RepresentationSchemeType;
import bio.singa.structure.model.interfaces.Atom;
import bio.singa.structure.model.oak.StructuralEntityFilter.AtomFilterType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.function.Predicate;

/**
 * The {@link Jsonizable} configuration of the {@link ConsensusMetric}.
 *
 * @author fk
 */
@JsonTypeName("CONSENSUS")
public class ConsensusMetricConfiguration<LabelType extends Comparable<LabelType>> implements ExtractionDependentMetricConfiguration<LabelType>, Jsonizable<ConsensusMetricConfiguration> {

    /**
     * the default maximal consensus score allowed
     */
    public static final double DEFAULT_MAXIMAL_CONSENSUS = 0.5;

    /**
     * the default cutoff value for clusters
     */
    public static final double DEFAULT_CLUSTER_CUTOFF_VALUE = 0.5;

    /**
     * the default level of parallelism
     */
    public static final int DEFAULT_LEVEL_OF_PARALLELISM = -1;

    /**
     * the default {@link AtomFilterType} that will be used
     */
    public static final AtomFilterType DEFAULT_ATOM_FILTER_TYPE = AtomFilterType.ARBITRARY;

    @JsonProperty("maximal-consensus")
    private double maximalConsensus = DEFAULT_MAXIMAL_CONSENSUS;
    @JsonProperty("cluster-cutoff-value")
    private double clusterCutoffValue = DEFAULT_CLUSTER_CUTOFF_VALUE;
    @JsonProperty("level-of-parallelism")
    private int levelOfParallelism = DEFAULT_LEVEL_OF_PARALLELISM;
    @JsonProperty("atom-filter-type")
    private AtomFilterType atomFilterType = DEFAULT_ATOM_FILTER_TYPE;
    @JsonIgnore
    private Predicate<Atom> atomFilter;
    @JsonProperty("representation-scheme-type")
    private RepresentationSchemeType representationSchemeType;
    @JsonProperty("align-within-clusters")
    private boolean alignWithinClusters;

    @Override public String toString() {
        return "ConsensusMetricConfiguration{" +
               "maximalConsensus=" + maximalConsensus +
               ", clusterCutoffValue=" + clusterCutoffValue +
               ", levelOfParallelism=" + levelOfParallelism +
               ", atomFilterType=" + atomFilterType +
               ", representationSchemeType=" + representationSchemeType +
               ", alignWithinClusters=" + alignWithinClusters +
               '}';
    }

    public Predicate<Atom> getAtomFilter() {
        return atomFilter;
    }

    public void setAtomFilter(Predicate<Atom> atomFilter) {
        this.atomFilter = atomFilter;
    }

    public AtomFilterType getAtomFilterType() {
        return atomFilterType;
    }

    public void setAtomFilterType(AtomFilterType atomFilterType) {
        this.atomFilterType = atomFilterType;
    }

    public double getClusterCutoffValue() {
        return clusterCutoffValue;
    }

    public void setClusterCutoffValue(double clusterCutoffValue) {
        this.clusterCutoffValue = clusterCutoffValue;
    }

    public int getLevelOfParallelism() {
        return levelOfParallelism;
    }

    public void setLevelOfParallelism(int levelOfParallelism) {
        this.levelOfParallelism = levelOfParallelism;
    }

    public double getMaximalConsensus() {
        return maximalConsensus;
    }

    public void setMaximalConsensus(double maximalConsensus) {
        this.maximalConsensus = maximalConsensus;
    }

    public RepresentationSchemeType getRepresentationSchemeType() {
        return representationSchemeType;
    }

    public void setRepresentationSchemeType(RepresentationSchemeType representationSchemeType) {
        this.representationSchemeType = representationSchemeType;
    }

    public boolean isAlignWithinClusters() {
        return alignWithinClusters;
    }

    public void setAlignWithinClusters(boolean alignWithinClusters) {
        this.alignWithinClusters = alignWithinClusters;
    }
}
