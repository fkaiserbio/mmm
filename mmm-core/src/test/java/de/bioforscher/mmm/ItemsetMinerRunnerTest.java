package de.bioforscher.mmm;

import de.bioforscher.mmm.model.analysis.association.ItemsetExtender;
import de.bioforscher.mmm.model.analysis.association.MutualInformationAnalyzer;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.metrics.ConsensusMetric;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * @author fk
 */
public class ItemsetMinerRunnerTest {

    @Test
    public void shouldRunOnTrajectory() throws IOException, URISyntaxException {

        ItemsetMinerConfiguration<String> config = ItemsetMinerConfiguration.from(Paths.get("/home/fkaiser/Workspace/IdeaProjects/mmm/mmm-core/src/test/resources/mmm_E2_config.json"));
        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(config);

        MutualInformationAnalyzer<String> mutualInformationAnalyzer = new MutualInformationAnalyzer<>(itemsetMinerRunner.getItemsetMiner(), ConsensusMetric.class, 0.1, true);


        ItemsetExtender<String> itemsetExtender = new ItemsetExtender<>(itemsetMinerRunner.getItemsetMiner(), mutualInformationAnalyzer.getItemsetGraph(), Paths.get("/tmp/mmm_e2/merged"));
        itemsetExtender.getMergedMotifs();
    }
}