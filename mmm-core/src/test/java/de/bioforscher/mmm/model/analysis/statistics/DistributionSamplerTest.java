package de.bioforscher.mmm.model.analysis.statistics;

import de.bioforscher.mmm.ItemsetMinerRunner;
import de.bioforscher.mmm.model.Distribution;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.analysis.ItemsetMinerAnalyzer;
import de.bioforscher.mmm.model.analysis.association.MutualInformationAnalyzer;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.metrics.CohesionMetric;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringJoiner;

/**
 * @author fk
 */
public class DistributionSamplerTest {

    @Test
    public void shouldSampleDistributions() throws IOException, URISyntaxException {

        ItemsetMinerConfiguration<String> configuration = ItemsetMinerConfiguration.from(Paths.get("/home/fkaiser/Workspace/IdeaProjects/mmm/mmm-core/src/test/resources/mmm_config.json"));

        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(configuration);


        ItemsetMinerAnalyzer<String> mutualInformationAnalyzer = new MutualInformationAnalyzer<>(itemsetMinerRunner.getItemsetMiner(), CohesionMetric.class, 0.1, true);

//        SignificanceEstimatorConfiguration significanceEstimatorConfiguration = new SignificanceEstimatorConfiguration();
//        significanceEstimatorConfiguration.setSignificanceType(SignificanceEstimatorType.COHESION);
//        significanceEstimatorConfiguration.setKsCutoff(0.1);
//        SignificanceEstimator<String> significanceEstimator = new SignificanceEstimator<>(itemsetMinerRunner.getItemsetMiner(), significanceEstimatorConfiguration);
//        for (Map.Entry<SignificanceEstimator<String>.Significance, Itemset<String>> significanceItemsetEntry : significanceEstimator.getSignificantItemsets().entrySet()) {
//            System.out.println(significanceItemsetEntry.getKey().getpValue() + " " + significanceItemsetEntry.getKey().getKs() + " " + significanceItemsetEntry.getValue());
//        }
//        System.out.println(significanceEstimator);

//        Files.createDirectories(Paths.get("/tmp/background_distributions/"));
//
//        for (Itemset<String> itemset : itemsetMinerRunner.getItemsetMiner().getTotalClusteredItemsets().keySet()) {
//            Distribution backgroundDistribution = distributionSampler.getBackgroundDistributions().get(itemset);
//            double[] backgroundDistributionValues = backgroundDistribution.getObservations().stream()
//                                                                          .mapToDouble(Double::doubleValue).toArray();
//            writeDistribution(itemset, backgroundDistribution);
//
//            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(backgroundDistributionValues);
//            double mean = descriptiveStatistics.getMean();
//            double standardDeviation = descriptiveStatistics.getStandardDeviation();
//            NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation);
//
//            double pValue = TestUtils.kolmogorovSmirnovTest(normalDistribution, backgroundDistributionValues, false);
//            System.out.println("KGS: " + pValue);
//            System.out.println(itemset + " " + normalDistribution.cumulativeProbability(itemset.getConsensus()));
//        }

//        Itemset<String> bestItemset = itemsetMinerRunner.getItemsetMiner().getTotalClusteredItemsets().firstKey();
//
//        System.out.println(bestItemset.getCohesion());
//        System.out.println();
//        StringJoiner originalDistribution = new StringJoiner("\n");
//        distributionSampler.getOriginalDistributions().get(bestItemset).getObservations().stream().map(String::valueOf).forEach(originalDistribution::add);
//        Files.write(Paths.get("/tmp/original.csv"), ("original\n" + originalDistribution.toString()).getBytes());
//        System.out.println();
//
//
//        StringJoiner backgroundDistribution = new StringJoiner("\n");
//        distributionSampler.getBackgroundDistributions().get(bestItemset).getObservations().stream().map(String::valueOf).forEach(backgroundDistribution::add);
//        Files.write(Paths.get("/tmp/background.csv"), ("background\n" + backgroundDistribution.toString()).getBytes());


//        System.out.println(originalDistribution);
//        System.out.println(backgroundDistributions.size());

//        for (double value : originalDistribution.getObservations()) {
//            System.out.println(value);
//        }
//        System.out.println();
//
//        for (double value : backgroundDistribution.getObservations()) {
//            System.out.println(value);
//        }

//        System.out.println();
    }

    private void writeDistribution(Itemset<String> itemset, Distribution backgroundDistribution) throws IOException {
        StringJoiner joiner = new StringJoiner("\n");
        backgroundDistribution.getObservations().stream().map(String::valueOf).forEach(joiner::add);
        Files.write(Paths.get("/tmp/background_distributions/" + itemset.toSimpleString() + "_background.csv"), ("background\n" + joiner.toString()).getBytes());
    }
}