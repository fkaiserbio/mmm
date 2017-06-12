package de.bioforscher.mmm.classify;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.Jsonizable;

/**
 * @author fk
 */
public class FingerPrintMinerConfiguration implements Jsonizable<FingerPrintMinerConfiguration> {

    @JsonProperty("decoy-rmsd-cutoff")
    private double decoyRmsdCutoff;

    @JsonProperty("itemset-miner-configuration")
    private ItemsetMinerConfiguration<String> itemsetMinerConfiguration;
}
