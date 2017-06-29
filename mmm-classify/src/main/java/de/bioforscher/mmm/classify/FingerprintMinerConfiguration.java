package de.bioforscher.mmm.classify;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.Jsonizable;

/**
 * @author fk
 */
public class FingerprintMinerConfiguration implements Jsonizable<FingerprintMinerConfiguration> {

    @JsonProperty("input-list-location")
    private String inputListLocation;

    @JsonProperty("blacklist-location")
    private String blacklistLocation;

    @JsonProperty("decoy-list-location")
    private String decoyListLocation;
    @JsonProperty("decoy-rmsd-cutoff")
    private double decoyRmsdCutoff;
    @JsonProperty("itemset-miner-configuration")
    private ItemsetMinerConfiguration<String> itemsetMinerConfiguration;

    public ItemsetMinerConfiguration<String> getItemsetMinerConfiguration() {
        return itemsetMinerConfiguration;
    }

    public void setItemsetMinerConfiguration(ItemsetMinerConfiguration<String> itemsetMinerConfiguration) {
        this.itemsetMinerConfiguration = itemsetMinerConfiguration;
    }

    public String getInputListLocation() {
        return inputListLocation;
    }

    public void setInputListLocation(String inputListLocation) {
        this.inputListLocation = inputListLocation;
    }

    public String getBlacklistLocation() {
        return blacklistLocation;
    }

    public void setBlacklistLocation(String blacklistLocation) {
        this.blacklistLocation = blacklistLocation;
    }

    public String getDecoyListLocation() {
        return decoyListLocation;
    }

    public void setDecoyListLocation(String decoyListLocation) {
        this.decoyListLocation = decoyListLocation;
    }

    public double getDecoyRmsdCutoff() {
        return decoyRmsdCutoff;
    }

    public void setDecoyRmsdCutoff(double decoyRmsdCutoff) {
        this.decoyRmsdCutoff = decoyRmsdCutoff;
    }
}
