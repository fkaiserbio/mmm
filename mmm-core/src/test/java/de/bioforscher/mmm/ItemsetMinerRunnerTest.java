package de.bioforscher.mmm;

import de.bioforscher.mmm.io.DataPointReaderConfiguration;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SupportMetricConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author fk
 */
public class ItemsetMinerRunnerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private ItemsetMinerConfiguration<String> itemsetMinerConfiguration;

    @Before
    public void setUp() {
        itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        itemsetMinerConfiguration.setMaximalEpochs(3);
        itemsetMinerConfiguration.setInputListLocation("craven2016_WSXWS_motif.txt");
        itemsetMinerConfiguration.setOutputLocation(folder.getRoot().toString());
        DataPointReaderConfiguration dataPointReaderConfiguration = new DataPointReaderConfiguration();
        itemsetMinerConfiguration.setDataPointReaderConfiguration(dataPointReaderConfiguration);
    }

    @Test
    public void shouldRun() throws IOException, URISyntaxException {
        SupportMetricConfiguration<String> supportMetricConfiguration = new SupportMetricConfiguration<>();
        supportMetricConfiguration.setMinimalSupport(0.8);
        itemsetMinerConfiguration.addSimpleMetricConfiguration(supportMetricConfiguration);
        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);
    }
}