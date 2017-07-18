package de.bioforscher.mmm;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.DataPointIdentifier;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SupportMetricConfiguration;
import de.bioforscher.mmm.model.metrics.EvaluationMetric;
import de.bioforscher.mmm.model.metrics.SupportMetric;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ItemsetMinerTest {

    @Test
    public void shouldCorrectlyGenerateCandidates() {

        List<Item<String>> items1 = Stream.of("A", "B", "C", "D")
                                          .map(Item::new)
                                          .collect(Collectors.toList());

        List<Item<String>> items2 = Stream.of("B", "C", "D")
                                          .map(Item::new)
                                          .collect(Collectors.toList());

        DataPoint<String> dataPoint1 = new DataPoint<>(items1, new DataPointIdentifier("1xxx"));
        DataPoint<String> dataPoint2 = new DataPoint<>(items2, new DataPointIdentifier("1yyy"));

        List<DataPoint<String>> dataPoints = new ArrayList<>();
        dataPoints.add(dataPoint1);
        dataPoints.add(dataPoint2);

        SupportMetricConfiguration<String> supportMetricConfiguration = new SupportMetricConfiguration<>();
        supportMetricConfiguration.setMinimalSupport(0.6);
        EvaluationMetric<String> supportMetric = new SupportMetric<>(dataPoints, supportMetricConfiguration);
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        itemsetMinerConfiguration.setMaximalEpochs(2);
        ItemsetMiner<String> itemsetMiner = new ItemsetMiner<>(dataPoints, Stream.of(supportMetric).collect(Collectors.toList()), itemsetMinerConfiguration);
        itemsetMiner.start();

        // check that second epoch itemsets do not contain item "A"
        assertFalse(itemsetMiner.getTotalItemsets().stream()
                                .filter(itemset -> itemset.getItems().size() == 2)
                                .anyMatch(itemset -> itemset.getItems().contains(new Item<String>("A"))));

        // use different support that allows item "A" to be included in next candidate round
        supportMetricConfiguration.setMinimalSupport(0.1);
        supportMetric = new SupportMetric<>(dataPoints, supportMetricConfiguration);
        itemsetMiner = new ItemsetMiner<>(dataPoints, Stream.of(supportMetric).collect(Collectors.toList()), itemsetMinerConfiguration);
        itemsetMiner.start();
        assertTrue(itemsetMiner.getTotalItemsets().stream()
                               .filter(itemset -> itemset.getItems().size() == 2)
                               .anyMatch(itemset -> itemset.getItems().contains(new Item<String>("A"))));
    }
}