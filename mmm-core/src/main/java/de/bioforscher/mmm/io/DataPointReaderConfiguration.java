package de.bioforscher.mmm.io;

import de.bioforscher.mmm.model.configurations.Jsonizable;

/**
 * @author fk
 */
public class DataPointReaderConfiguration implements Jsonizable<DataPointReaderConfiguration> {

    private static final String DEFAULT_CHAIN_LIST_SEPARATOR = "\t";
    private String pdbLocation;
    private String chainListSeparator = DEFAULT_CHAIN_LIST_SEPARATOR;
    private boolean parseLigands;
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
