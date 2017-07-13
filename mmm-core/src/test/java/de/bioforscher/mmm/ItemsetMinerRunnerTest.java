package de.bioforscher.mmm;

import de.bioforscher.mmm.model.analysis.association.ItemsetExtender;
import de.bioforscher.mmm.model.analysis.association.MutualInformationAnalyzer;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.metrics.CohesionMetric;
import de.bioforscher.mmm.model.metrics.DistributionMetric;
import de.bioforscher.mmm.model.metrics.EvaluationMetric;
import de.bioforscher.singa.chemistry.physical.families.LigandFamily;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fk
 */
public class ItemsetMinerRunnerTest {

    @Test
    @Ignore
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
        ItemsetMiner<String> itemsetMiner = itemsetMinerRunner.getItemsetMiner();
//        ConfidenceAnalyzer<String> confidenceAnalyzer = new ConfidenceAnalyzer<>(itemsetMiner);
        MutualInformationAnalyzer<String> analyzer = new MutualInformationAnalyzer<>(itemsetMiner, CohesionMetric.class, 1.2, false);
        ItemsetExtender<String> extender = new ItemsetExtender<>(itemsetMiner, analyzer.getItemsetGraph(), Paths.get("/tmp/merged_motifs"), new LigandFamily("EST"));
    }
}