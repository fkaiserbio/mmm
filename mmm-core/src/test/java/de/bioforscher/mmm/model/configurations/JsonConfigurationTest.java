package de.bioforscher.mmm.model.configurations;

import de.bioforscher.mmm.io.DataPointReaderConfiguration;
import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.configurations.metrics.AdherenceMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SeparationMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SupportMetricConfiguration;
import de.bioforscher.mmm.model.enrichment.DataPointEnricherType;
import de.bioforscher.mmm.model.mapping.rules.ChemicalGroupsMappingRule;
import de.bioforscher.singa.chemistry.physical.atoms.representations.RepresentationSchemeType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author fk
 */
public class JsonConfigurationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldSerializeAndDeserializeItemsetMinerConfiguration() throws IOException {

        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        itemsetMinerConfiguration.setInputListLocation("PF00127_chains.txt");
        itemsetMinerConfiguration.setItemsetComparatorType(ItemsetComparatorType.CONSENSUS);
        itemsetMinerConfiguration.setDataPointEnricherType(DataPointEnricherType.INTRA_CHAIN_INTERACTION);
        itemsetMinerConfiguration.setOutputLocation("/tmp/itemset-miner");
        itemsetMinerConfiguration.setMaximalEpochs(-1);

        DataPointReaderConfiguration dataPointReaderConfiguration = new DataPointReaderConfiguration();
        dataPointReaderConfiguration.setPdbLocation("/srv/pdb");
        dataPointReaderConfiguration.setParseLigands(false);
        dataPointReaderConfiguration.setParseNucleotides(false);
        dataPointReaderConfiguration.addToLigandLabelWhiteList("ATP");
        itemsetMinerConfiguration.setDataPointReaderConfiguration(dataPointReaderConfiguration);

        // add data point enricher type
        itemsetMinerConfiguration.setDataPointEnricherType(DataPointEnricherType.INTRA_CHAIN_INTERACTION);

        // add mapping rule type
        itemsetMinerConfiguration.setMappingRule(new ChemicalGroupsMappingRule());

        // create simple metrics
        SupportMetricConfiguration<String> supportMetricConfiguration = new SupportMetricConfiguration<>();
        supportMetricConfiguration.setMinimalSupport(0.9);
        itemsetMinerConfiguration.addSimpleMetricConfiguration(supportMetricConfiguration);

        // create extraction metric
//        CohesionMetricConfiguration<String> cohesionMetricConfiguration = new CohesionMetricConfiguration<>();
//        cohesionMetricConfiguration.setMaximalCohesion(10.0);
//        itemsetMinerConfiguration.setExtractionMetricConfiguration(cohesionMetricConfiguration);

        AdherenceMetricConfiguration<String> adherenceMetricConfiguration = new AdherenceMetricConfiguration<>();
        adherenceMetricConfiguration.setMaximalAdherence(0.3);
        itemsetMinerConfiguration.setExtractionMetricConfiguration(adherenceMetricConfiguration);

        // create extraction-dependent metrics
        SeparationMetricConfiguration<String> separationMetricConfiguration = new SeparationMetricConfiguration<>();
        separationMetricConfiguration.setMaximalSeparation(50.0);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(separationMetricConfiguration);

        ConsensusMetricConfiguration<String> consensusMetricConfiguration = new ConsensusMetricConfiguration<>();
        consensusMetricConfiguration.setRepresentationSchemeType(RepresentationSchemeType.SIDE_CHAIN_CENTROID);
        itemsetMinerConfiguration.addExtractionDependentMetricConfiguration(consensusMetricConfiguration);

        String json = itemsetMinerConfiguration.toJson();

        System.out.println(json);

        Path configurationPath = folder.getRoot().toPath().resolve("itemset-miner-configuration.json");
        Files.write(configurationPath, itemsetMinerConfiguration.toJson().getBytes());
        ItemsetMinerConfiguration<?> deserializedConfiguration = ItemsetMinerConfiguration.from(configurationPath);
        Assert.assertEquals(itemsetMinerConfiguration.getItemsetComparatorType(), deserializedConfiguration.getItemsetComparatorType());
        Assert.assertEquals(itemsetMinerConfiguration.getInputListLocation(), deserializedConfiguration.getInputListLocation());
        Assert.assertEquals(itemsetMinerConfiguration.getOutputLocation(), deserializedConfiguration.getOutputLocation());
    }
}