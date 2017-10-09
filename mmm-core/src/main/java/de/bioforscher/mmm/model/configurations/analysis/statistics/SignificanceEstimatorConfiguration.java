package de.bioforscher.mmm.model.configurations.analysis.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bioforscher.mmm.model.analysis.statistics.SignificanceEstimatorType;

/**
 * @author fk
 */
public class SignificanceEstimatorConfiguration {

    private static final int DEFAULT_LEVEL_OF_PARALLELISM = -1;

    @JsonProperty("significance-type")
    private SignificanceEstimatorType significanceType;
    @JsonProperty("ks-cutoff")
    private double ksCutoff;
    @JsonProperty("level-of-parallelism")
    private int levelOfParallelism = DEFAULT_LEVEL_OF_PARALLELISM;

    public int getLevelOfParallelism() {
        return levelOfParallelism;
    }

    public void setLevelOfParallelism(int levelOfParallelism) {
        this.levelOfParallelism = levelOfParallelism;
    }

    public SignificanceEstimatorType getSignificanceType() {
        return significanceType;
    }

    public void setSignificanceType(SignificanceEstimatorType significanceType) {
        this.significanceType = significanceType;
    }

    public double getKsCutoff() {
        return ksCutoff;
    }

    public void setKsCutoff(double ksCutoff) {
        this.ksCutoff = ksCutoff;
    }
}
