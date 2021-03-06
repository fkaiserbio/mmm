package bio.fkaiser.mmm.model.configurations.analysis.statistics;

import bio.fkaiser.mmm.model.analysis.statistics.SignificanceEstimatorType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author fk
 */
public class SignificanceEstimatorConfiguration {

    private static final int DEFAULT_LEVEL_OF_PARALLELISM = -1;
    private static final int DEFAULT_SAMPLE_SIZE = 30;
    private static final double DEFAULT_SIGNIFICANCE_CUTOFF = 1E-3;
    private static final double DEFAULT_KS_CUTOFF = 0.1;

    @JsonProperty("significance-type")
    private SignificanceEstimatorType significanceType;
    @JsonProperty("ks-cutoff")
    private double ksCutoff = DEFAULT_KS_CUTOFF;
    @JsonProperty("significance-cutoff")
    private double significanceCutoff = DEFAULT_SIGNIFICANCE_CUTOFF;
    @JsonProperty("level-of-parallelism")
    private int levelOfParallelism = DEFAULT_LEVEL_OF_PARALLELISM;
    @JsonProperty("sample-size")
    private int sampleSize = DEFAULT_SAMPLE_SIZE;

    public double getKsCutoff() {
        return ksCutoff;
    }

    public void setKsCutoff(double ksCutoff) {
        this.ksCutoff = ksCutoff;
    }

    public int getLevelOfParallelism() {
        return levelOfParallelism;
    }

    public void setLevelOfParallelism(int levelOfParallelism) {
        this.levelOfParallelism = levelOfParallelism;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    public double getSignificanceCutoff() {
        return significanceCutoff;
    }

    public void setSignificanceCutoff(double significanceCutoff) {
        this.significanceCutoff = significanceCutoff;
    }

    public SignificanceEstimatorType getSignificanceType() {
        return significanceType;
    }

    public void setSignificanceType(SignificanceEstimatorType significanceType) {
        this.significanceType = significanceType;
    }
}
