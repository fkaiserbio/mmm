package de.bioforscher.mmm.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.configurations.Jsonizable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fk
 */
@JsonTypeName("DATA_POINT_READER_CONFIGURATION")
public class DataPointReaderConfiguration implements Jsonizable<DataPointReaderConfiguration> {

    private static final String DEFAULT_CHAIN_LIST_SEPARATOR = "\t";

    @JsonProperty("pdb-location")
    private String pdbLocation;
    @JsonProperty("chain-list-separator")
    private String chainListSeparator = DEFAULT_CHAIN_LIST_SEPARATOR;
    @JsonProperty("parse-ligands")
    private boolean parseLigands;
    @JsonProperty("ligand-label-whitelist")
    private List<String> ligandLabelWhitelist = new ArrayList<>();
    @JsonProperty("parse-nucleotides")
    private boolean parseNucleotides;
    @JsonProperty("parse-water")
    private boolean parseWater;

    public List<String> getLigandLabelWhitelist() {
        return ligandLabelWhitelist;
    }

    public void setLigandLabelWhitelist(List<String> ligandLabelWhitelist) {
        this.ligandLabelWhitelist = ligandLabelWhitelist;
    }

    public void addToLigandLabelWhiteList(String ligandLabel) {
        ligandLabelWhitelist.add(ligandLabel);
    }

    public boolean isParseWater() {
        return parseWater;
    }

    public void setParseWater(boolean parseWater) {
        this.parseWater = parseWater;
    }

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
