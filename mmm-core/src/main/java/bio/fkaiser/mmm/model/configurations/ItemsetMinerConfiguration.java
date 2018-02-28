package bio.fkaiser.mmm.model.configurations;

import bio.fkaiser.mmm.ItemsetMiner;
import bio.fkaiser.mmm.io.DataPointReaderConfiguration;
import bio.fkaiser.mmm.model.ItemsetComparatorType;
import bio.fkaiser.mmm.model.configurations.analysis.statistics.SignificanceEstimatorConfiguration;
import bio.fkaiser.mmm.model.configurations.metrics.ExtractionDependentMetricConfiguration;
import bio.fkaiser.mmm.model.configurations.metrics.ExtractionMetricConfiguration;
import bio.fkaiser.mmm.model.configurations.metrics.SimpleMetricConfiguration;
import bio.fkaiser.mmm.model.enrichment.DataPointEnricher;
import bio.fkaiser.mmm.model.mapping.MappingRule;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link Jsonizable} configuration of the {@link ItemsetMiner}. This is the global configuration object that sho
 *
 * @author fk
 */
@JsonTypeName("ITEMSET_MINER_CONFIGURATION")
public class ItemsetMinerConfiguration<LabelType extends Comparable<LabelType>> implements Jsonizable<ItemsetMinerConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(ItemsetMinerConfiguration.class);

    private static final ItemsetComparatorType DEFAULT_ITEMSET_COMPARATOR = ItemsetComparatorType.SUPPORT;
    private static final int DEFAULT_MAXIMAL_EPOCHS = -1;

    @JsonProperty("creation-user")
    private String creationUser;
    @JsonProperty("creation-date")
    private String creationDate;
    @JsonProperty("description")
    private String description;
    @JsonProperty("input-chain")
    private String inputChain;
    @JsonProperty("input-list-location")
    private String inputListLocation;
    @JsonProperty("input-directory-location")
    private String inputDirectoryLocation;
    @JsonProperty("output-location")
    private String outputLocation;
    @JsonProperty("data-point-reader-configuration")
    private DataPointReaderConfiguration dataPointReaderConfiguration;
    @JsonProperty("data-point-enricher")
    private DataPointEnricher<LabelType> dataPointEnricher;
    @JsonProperty("mapping-rules")
    private List<MappingRule<LabelType>> mappingRules;
    @JsonProperty("simple-metrics")
    private List<SimpleMetricConfiguration<LabelType>> simpleMetricConfigurations;
    @JsonProperty("extraction-metric")
    private ExtractionMetricConfiguration<LabelType> extractionMetricConfiguration;
    @JsonProperty("extraction-dependent-metric")
    private List<ExtractionDependentMetricConfiguration<LabelType>> extractionDependentMetricConfigurations;
    @JsonProperty("itemset-comparator-type")
    private ItemsetComparatorType itemsetComparatorType = DEFAULT_ITEMSET_COMPARATOR;
    @JsonProperty("maximal-epochs")
    private int maximalEpochs = DEFAULT_MAXIMAL_EPOCHS;
    @JsonProperty("significance-estimator-configuration")
    private SignificanceEstimatorConfiguration significanceEstimatorConfiguration;

    public ItemsetMinerConfiguration() {
        this.creationUser = System.getProperty("user.name");
        this.creationDate = LocalDateTime.now().toString();
        this.mappingRules = new ArrayList<>();
        this.simpleMetricConfigurations = new ArrayList<>();
        this.extractionDependentMetricConfigurations = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static <LabelType extends Comparable<LabelType>> ItemsetMinerConfiguration<LabelType> from(Path configurationPath) throws IOException {
        logger.info("reading configuration from {}", configurationPath);
        String json = Files.lines(configurationPath).collect(Collectors.joining());
        return new ItemsetMinerConfiguration<>().fromJson(json);
    }

    @SuppressWarnings("unchecked")
    public static <LabelType extends Comparable<LabelType>> ItemsetMinerConfiguration<LabelType> from(InputStream inputStream) throws IOException {
        logger.info("reading configuration from given input stream");
        return new ItemsetMinerConfiguration<>().fromJson(inputStream);
    }

    @Override public String toString() {
        return "ItemsetMinerConfiguration{" +
               "creationUser='" + creationUser + '\'' +
               ", creationDate='" + creationDate + '\'' +
               '}';
    }

    public void addSimpleMetricConfiguration(SimpleMetricConfiguration<LabelType> simpleMetricConfiguration) {
        simpleMetricConfigurations.add(simpleMetricConfiguration);
    }

    public void addExtractionDependentMetricConfiguration(ExtractionDependentMetricConfiguration<LabelType> extractionDependentMetricConfiguration) {
        extractionDependentMetricConfigurations.add(extractionDependentMetricConfiguration);
    }

    public void addMappingRule(MappingRule<LabelType> mappingRule) {
        mappingRules.add(mappingRule);
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getCreationUser() {
        return creationUser;
    }

    public DataPointEnricher<LabelType> getDataPointEnricher() {
        return dataPointEnricher;
    }

    public void setDataPointEnricher(DataPointEnricher<LabelType> dataPointEnricher) {
        this.dataPointEnricher = dataPointEnricher;
    }

    public DataPointReaderConfiguration getDataPointReaderConfiguration() {
        return dataPointReaderConfiguration;
    }

    public void setDataPointReaderConfiguration(DataPointReaderConfiguration dataPointReaderConfiguration) {
        this.dataPointReaderConfiguration = dataPointReaderConfiguration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ExtractionDependentMetricConfiguration<LabelType>> getExtractionDependentMetricConfigurations() {
        return extractionDependentMetricConfigurations;
    }

    public ExtractionMetricConfiguration<LabelType> getExtractionMetricConfiguration() {
        return extractionMetricConfiguration;
    }

    public void setExtractionMetricConfiguration(ExtractionMetricConfiguration<LabelType> extractionMetricConfiguration) {
        this.extractionMetricConfiguration = extractionMetricConfiguration;
    }

    public String getInputChain() {
        return inputChain;
    }

    public void setInputChain(String inputChain) {
        this.inputChain = inputChain;
    }

    public String getInputDirectoryLocation() {
        return inputDirectoryLocation;
    }

    public void setInputDirectoryLocation(String inputDirectoryLocation) {
        this.inputDirectoryLocation = inputDirectoryLocation;
    }

    public String getInputListLocation() {
        return inputListLocation;
    }

    public void setInputListLocation(String inputListLocation) {
        this.inputListLocation = inputListLocation;
    }

    public ItemsetComparatorType getItemsetComparatorType() {
        return itemsetComparatorType;
    }

    public void setItemsetComparatorType(ItemsetComparatorType itemsetComparatorType) {
        this.itemsetComparatorType = itemsetComparatorType;
    }

    public List<MappingRule<LabelType>> getMappingRules() {
        return mappingRules;
    }

    public void setMappingRules(List<MappingRule<LabelType>> mappingRules) {
        this.mappingRules = mappingRules;
    }

    public int getMaximalEpochs() {
        return maximalEpochs;
    }

    public void setMaximalEpochs(int maximalEpochs) {
        this.maximalEpochs = maximalEpochs;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }

    public SignificanceEstimatorConfiguration getSignificanceEstimatorConfiguration() {
        return significanceEstimatorConfiguration;
    }

    public void setSignificanceEstimatorConfiguration(SignificanceEstimatorConfiguration significanceEstimatorConfiguration) {
        this.significanceEstimatorConfiguration = significanceEstimatorConfiguration;
    }

    public List<SimpleMetricConfiguration<LabelType>> getSimpleMetricConfigurations() {
        return simpleMetricConfigurations;
    }
}
