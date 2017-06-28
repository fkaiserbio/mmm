package de.bioforscher.mmm;

import JavaMI.MutualInformation;
import de.bioforscher.mmm.model.Distribution;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.metrics.ConsensusMetric;
import de.bioforscher.mmm.model.metrics.DistributionMetric;
import de.bioforscher.mmm.model.metrics.EvaluationMetric;
import de.bioforscher.singa.core.utility.Pair;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * @author fk
 */
public class ItemsetMinerRunnerTest {

    @Test
    public void shouldRunItemsetMiner() throws IOException, URISyntaxException {
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration =
                ItemsetMinerConfiguration.from(Paths.get("/home/fkaiser/Workspace/IdeaProjects/mmm/mmm-core/src/test/resources/itemset-miner_config.json"));
        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);
        List<DistributionMetric<String>> distributionMetrics = new ArrayList<>();
        for (EvaluationMetric<String> evaluationMetric : itemsetMinerRunner.getItemsetMiner().getEvaluationMetrics()) {
            if (evaluationMetric instanceof DistributionMetric) {
                distributionMetrics.add((DistributionMetric<String>) evaluationMetric);
            }
        }

        TreeMap<Double, Pair<Itemset<String>>> mutualInformation = new TreeMap<>(Collections.reverseOrder());

        List<Itemset<String>> totalItemsets = itemsetMinerRunner.getItemsetMiner().getTotalItemsets();
        for (Itemset<String> itemsetOne : totalItemsets) {
            Distribution itemsetOneDistribution = distributionMetrics.stream()
                                                                     .map(distributionMetric -> distributionMetric.getDistributions().get(itemsetOne))
                                                                     .filter(dist -> dist.getDistributionMetricType() == ConsensusMetric.class)
                                                                     .findFirst().get();
            for (Itemset<String> itemsetTwo : totalItemsets) {
                Distribution itemsetTwoDistribution = distributionMetrics.stream()
                                                                         .map(distributionMetric -> distributionMetric.getDistributions().get(itemsetTwo))
                                                                         .filter(dist -> dist.getDistributionMetricType() == ConsensusMetric.class)
                                                                         .findFirst().get();

                if (itemsetOne.equals(itemsetTwo)) {
                    continue;
                }

                // cap to smaller observations
                List<Double> itemsetOneObservations = itemsetOneDistribution.getObservations();
                List<Double> itemsetTwoObservations = itemsetTwoDistribution.getObservations();
                int itemsetOneDistributionSize = itemsetOneObservations.size();
                int itemsetTwoDistributionSize = itemsetTwoObservations.size();

                if (itemsetOneDistributionSize > itemsetTwoDistributionSize) {
                    itemsetOneObservations = itemsetOneObservations.subList(0, itemsetTwoDistributionSize);
                } else if (itemsetTwoDistributionSize > itemsetOneDistributionSize) {
                    itemsetTwoObservations = itemsetTwoObservations.subList(0, itemsetOneDistributionSize);
                }

                double mi = MutualInformation.calculateMutualInformation(itemsetOneObservations.stream().mapToDouble(Double::doubleValue).toArray(),
                                                                         itemsetTwoObservations.stream().mapToDouble(Double::doubleValue).toArray());
                mutualInformation.put(mi, new Pair<>(itemsetOne, itemsetTwo));
            }
        }
        mutualInformation.forEach((key, value) -> System.out.println(value.getFirst().toSimpleString() + "-->" + value.getSecond().toSimpleString() + "," + key));
    }
}