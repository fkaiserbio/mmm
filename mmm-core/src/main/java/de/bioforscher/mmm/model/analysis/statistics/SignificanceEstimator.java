package de.bioforscher.mmm.model.analysis.statistics;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.ItemsetMiner;
import de.bioforscher.mmm.model.Distribution;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.analysis.statistics.SignificanceEstimatorConfiguration;
import de.bioforscher.mmm.model.metrics.CohesionMetric;
import de.bioforscher.mmm.model.metrics.ConsensusMetric;
import de.bioforscher.mmm.model.metrics.DistributionMetric;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/**
 * Uses the {@link DistributionSampler} to estimate the significance of found {@link Itemset}s regarding a defined {@link DistributionMetric}.
 * Currently supported are {@link CohesionMetric} and {@link ConsensusMetric}.
 *
 * @author fk
 */
@JsonTypeName("SIGNIFICANCE_ESTIMATOR")
public class SignificanceEstimator<LabelType extends Comparable<LabelType>> {

    private static final Logger logger = LoggerFactory.getLogger(SignificanceEstimator.class);

    private final ItemsetMiner<LabelType> itemsetMiner;
    private final SignificanceEstimatorType type;
    private final double ksCutoff;
    private final TreeMap<Significance, Itemset<LabelType>> significantItemsets;
    private Map<Itemset<LabelType>, Distribution> backgroundDistributions;

    public SignificanceEstimator(ItemsetMiner<LabelType> itemsetMiner, SignificanceEstimatorConfiguration configuration) {
        this.itemsetMiner = itemsetMiner;
        type = configuration.getSignificanceType();
        ksCutoff = configuration.getKsCutoff();
        significantItemsets = new TreeMap<>();
        sampleDistributions(configuration.getLevelOfParallelism());
    }

    public TreeMap<Significance, Itemset<LabelType>> getSignificantItemsets() {
        return significantItemsets;
    }

    private void sampleDistributions(int levelOfParallelism) {
        DistributionSampler<LabelType> distributionSampler = new DistributionSampler<>(itemsetMiner, type.getDistributionMetric(), levelOfParallelism);
        backgroundDistributions = distributionSampler.getBackgroundDistributions();
        backgroundDistributions.keySet().forEach(this::determineSignificance);
    }

    private void determineSignificance(Itemset<LabelType> itemset) {

        double[] values = backgroundDistributions.get(itemset).getObservations().stream()
                                                 .mapToDouble(Double::doubleValue).toArray();

        // model normal distribution
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(values);
        double mean = descriptiveStatistics.getMean();
        double standardDeviation = descriptiveStatistics.getStandardDeviation();
        NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation);

        // calculate KS p-value to estimate quality of fit
        double ks = TestUtils.kolmogorovSmirnovTest(normalDistribution, values, false);
        if (ks < ksCutoff) {
            logger.warn("itemset {} background distribution of type {} violates KS-cutoff, skipping", itemset, type);
            return;
        }

        double pValue = Double.NaN;
        if (type == SignificanceEstimatorType.COHESION) {
            pValue = normalDistribution.cumulativeProbability(itemset.getCohesion());
        } else if (type == SignificanceEstimatorType.CONSENSUS) {
            pValue = normalDistribution.cumulativeProbability(itemset.getConsensus());
        }

        logger.debug("p-value for itemset {} is {}", itemset.toSimpleString(), pValue);
        significantItemsets.put(new Significance(pValue, ks), itemset);
    }

    public class Significance implements Comparable<Significance> {

        private final double pValue;
        private final double ks;

        public Significance(double pValue, double ks) {

            this.pValue = pValue;
            this.ks = ks;
        }

        public double getpValue() {
            return pValue;
        }

        public double getKs() {
            return ks;
        }

        @Override public String toString() {
            return "Significance{" +
                   "pValue=" + pValue +
                   ", ks=" + ks +
                   '}';
        }

        @Override public int compareTo(Significance o) {
            return Double.compare(pValue, o.pValue);
        }
    }
}
