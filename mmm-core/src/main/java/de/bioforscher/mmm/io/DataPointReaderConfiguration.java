package de.bioforscher.mmm.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bioforscher.mmm.model.configurations.Jsonizable;

/**
 * @author fk
 */
public class DataPointReaderConfiguration implements Jsonizable<DataPointReaderConfiguration> {

    private static final String DEFAULT_CHAIN_LIST_SEPARATOR = "\t";

    @JsonProperty("pdb-location")
    private String pdbLocation;
    @JsonProperty("chain-list-separator")
    private String chainListSeparator = DEFAULT_CHAIN_LIST_SEPARATOR;
    @JsonProperty("parse-ligands")
    private boolean parseLigands;
    @JsonProperty("parse-nucleotides")
    private boolean parseNucleotides;

    public boolean isParseNucleotides() {
        return parseNucleotides;
    }

    public void setParseNucleotides(boolean parseNucleotides) {
        this.parseNucleotides = parseNucleotides;
    }

    public String getPdbLocation() {
        return pdbLocation;
    }

    public void setPdbLocation(String pdbLocation) {
        this.pdbLocation = pdbLocation;
    }

    public String getChainListSeparator() {
        return chainListSeparator;
    }

    public void setChainListSeparator(String chainListSeparator) {
        this.chainListSeparator = chainListSeparator;
    }

    public boolean isParseLigands() {
        return parseLigands;
    }

    public void setParseLigands(boolean parseLigands) {
        this.parseLigands = parseLigands;
    }
}
