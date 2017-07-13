package de.bioforscher.mmm.model.configurations.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.configurations.Jsonizable;

/**
 * @author fk
 */
@JsonTypeName("SEPARATION")
public class SeparationMetricConfiguration<LabelType extends Comparable<LabelType>> implements ExtractionDependentMetricConfiguration<LabelType>, Jsonizable<SeparationMetricConfiguration> {

    /**
     * the default value for the required maximal separation score
     */
    public static final double DEFAULT_MAXIMAL_SEPARATION = 0.0;
    /**
     * the absolute minimum value for the separation score
     */
    public static final double DEFAULT_MORSE_WELL_DEPTH = 500.0;
    /**
     * the shape of the well of the Morse potential function (smaller values are less aggressive)
     */
    public static final double DEFAULT_MORSE_SHAPE = 0.2;
    /**
     * the default optimal sequence separation (location of the Morse potential function minimum)
     */
    public static final double DEFAULT_OPTIMAL_SEPARATION = 5.0;
    @JsonProperty("maximal-separation")
    private double maximalSeparation = DEFAULT_MAXIMAL_SEPARATION;
    @JsonProperty("optimal-separation")
    private double optimalSeparation = DEFAULT_OPTIMAL_SEPARATION;
    @JsonProperty("morse-well-depth")
    private double morseWellDepth = DEFAULT_MORSE_WELL_DEPTH;
    @JsonProperty("morse-shape")
    private double morseShape = DEFAULT_MORSE_SHAPE;

    public double getMaximalSeparation() {
        return maximalSeparation;
    }

    public void setMaximalSeparation(double maximalSeparation) {
        this.maximalSeparation = maximalSeparation;
    }

    public double getOptimalSeparation() {
        return optimalSeparation;
    }

    public void setOptimalSeparation(double optimalSeparation) {
        this.optimalSeparation = optimalSeparation;
    }

    public double getMorseWellDepth() {
        return morseWellDepth;
    }

    public void setMorseWellDepth(double morseWellDepth) {
        this.morseWellDepth = morseWellDepth;
    }

    public double getMorseShape() {
        return morseShape;
    }

    public void setMorseShape(double morseShape) {
        this.morseShape = morseShape;
    }

    @Override public String toString() {
        return "SeparationMetricConfiguration{" +
               "maximalSeparation=" + maximalSeparation +
               ", optimalSeparation=" + optimalSeparation +
               ", morseWellDepth=" + morseWellDepth +
               ", morseShape=" + morseShape +
               '}';
    }
}
