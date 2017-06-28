package de.bioforscher.mmm.model.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.io.DataPointReaderConfiguration;
import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.configurations.metrics.ExtractionDependentMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ExtractionMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.SimpleMetricConfiguration;
import de.bioforscher.mmm.model.enrichment.DataPointEnricherType;
import de.bioforscher.mmm.model.mapping.MappingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fk
 */
@JsonTypeName("ITEMSET-MINER")
public class ItemsetMinerConfiguration<LabelType extends Comparable<LabelType>> implements Jsonizable<ItemsetMinerConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(ItemsetMinerConfiguration.class);

    private static final ItemsetComparatorType DEFAULT_ITEMSET_COMPARATOR = ItemsetComparatorType.SUPPORT;

    @JsonProperty("creation-user")
    private String creationUser;
    @JsonProperty("creation-date")
    private String creationDate;

    @JsonProperty("input-list-location")
    private String inputListLocation;
    @JsonProperty("input-directory-location")
    private String inputDirectoryLocation;
    @JsonProperty("output-location")
    private String outputLocation;
    @JsonProperty("data-point-reader-configuration")
    private DataPointReaderConfiguration dataPointReaderConfiguration;
    @JsonProperty("data-point-enricher-type")
    private DataPointEnricherType dataPointEnricherType;
    @JsonProperty("mapping-rule")
    private MappingRule<LabelType> mappingRule;
    @JsonProperty("simple-metrics")
    private List<SimpleMetricConfiguration<LabelType>> simpleMetricConfigurations;
    @JsonProperty("extraction-metric")
    private ExtractionMetricConfiguration<LabelType> extractionMetricConfiguration;
    @JsonProperty("extraction-dependent-metric")
    private List<ExtractionDependentMetricConfiguration<LabelType>> extractionDependentMetricConfigurations;
    @JsonProperty("itemset-comparator-type")
    private ItemsetComparatorType itemsetComparatorType = DEFAULT_ITEMSET_COMPARATOR;
    @JsonProperty("maximal-epochs")
    private int maximalEpochs;
    public ItemsetMinerConfiguration() {
        this.creationUser = System.getProperty("user.name");
        this.creationDate = LocalDateTime.now().toString();
        this.simpleMetricConfigurations = new ArrayList<>();
        this.extractionDependentMetricConfigurations = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static <LabelType extends Comparable<LabelType>> ItemsetMinerConfiguration<LabelType> from(Path configurationPath) throws IOException {
        logger.info("reading configuration from {}", configurationPath);
        String json = Files.lines(configurationPath).collect(Collectors.joining());
        return new ItemsetMinerConfiguration<>().fromJson(json);
    }

    public String getInputDirectoryLocation() {
        return inputDirectoryLocation;
    }

    public void setInputDirectoryLocation(String inputDirectoryLocation) {
        this.inputDirectoryLocation = inputDirectoryLocation;
    }

    public DataPointReaderConfiguration getDataPointReaderConfiguration() {
        return dataPointReaderConfiguration;
    }

    public void setDataPointReaderConfiguration(DataPointReaderConfiguration dataPointReaderConfiguration) {
        this.dataPointReaderConfiguration = dataPointReaderConfiguration;
    }

    public ItemsetComparatorType getItemsetComparatorType() {
        return itemsetComparatorType;
    }

    public void setItemsetComparatorType(ItemsetComparatorType itemsetComparatorType) {
        this.itemsetComparatorType = itemsetComparatorType;
    }

    public int getMaximalEpochs() {
        return maximalEpochs;
    }

    public void setMaximalEpochs(int maximalEpochs) {
        this.maximalEpochs = maximalEpochs;
    }

    public MappingRule<LabelType> getMappingRule() {
        return mappingRule;
    }

    public void setMappingRule(MappingRule<LabelType> mappingRule) {
        this.mappingRule = mappingRule;
    }

    public List<SimpleMetricConfiguration<LabelType>> getSimpleMetricConfigurations() {
        return simpleMetricConfigurations;
    }

    public void addSimpleMetricConfiguration(SimpleMetricConfiguration<LabelType> simpleMetricConfiguration) {
        simpleMetricConfigurations.add(simpleMetricConfiguration);
    }

    public DataPointEnricherType getDataPointEnricherType() {
        return dataPointEnricherType;
    }

    public void setDataPointEnricherType(DataPointEnricherType dataPointEnricherType) {
        this.dataPointEnricherType = dataPointEnricherType;
    }

    public String getInputListLocation() {
        return inputListLocation;
    }

    public void setInputListLocation(String inputListLocation) {
        this.inputListLocation = inputListLocation;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }

    public String getCreationUser() {
        return creationUser;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public ExtractionMetricConfiguration<LabelType> getExtractionMetricConfiguration() {
        return extractionMetricConfiguration;
    }

    public void setExtractionMetricConfiguration(ExtractionMetricConfiguration<LabelType> extractionMetricConfiguration) {
        this.extractionMetricConfiguration = extractionMetricConfiguration;
    }

    public List<ExtractionDependentMetricConfiguration<LabelType>> getExtractionDependentMetricConfigurations() {
        return extractionDependentMetricConfigurations;
    }

    public void addExtractionDependentMetricConfiguration(ExtractionDependentMetricConfiguration<LabelType> extractionDependentMetricConfiguration) {
        extractionDependentMetricConfigurations.add(extractionDependentMetricConfiguration);
    }
}
