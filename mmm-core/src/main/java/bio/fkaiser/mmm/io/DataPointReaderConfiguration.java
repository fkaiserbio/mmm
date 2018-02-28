package bio.fkaiser.mmm.io;

import bio.fkaiser.mmm.model.configurations.Jsonizable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Jsonizable} configuration of the {@link DataPointReader}.
 *
 * @author fk
 */
@JsonTypeName("DATA_POINT_READER_CONFIGURATION")
public class DataPointReaderConfiguration implements Jsonizable<DataPointReaderConfiguration> {

    private static final String DEFAULT_CHAIN_LIST_SEPARATOR = "\t";
    private static final PDBSequenceCluster DEFAULT_SEQUENCE_CLUSTER = PDBSequenceCluster.IDENTITY_90;

    @JsonProperty("pdb-location")
    private String pdbLocation;
    @JsonProperty("chain-list-separator")
    private String chainListSeparator = DEFAULT_CHAIN_LIST_SEPARATOR;
    @JsonProperty("parse-ligands")
    private boolean parseLigands;
    @JsonProperty("pdb-sequence-cluster")
    private PDBSequenceCluster pdbSequenceCluster = DEFAULT_SEQUENCE_CLUSTER;
    @JsonProperty("ligand-label-whitelist")
    private List<String> ligandLabelWhitelist = new ArrayList<>();
    @JsonProperty("parse-nucleotides")
    private boolean parseNucleotides;
    @JsonProperty("parse-water")
    private boolean parseWater;
    @JsonProperty("consecutive-sequence-numbering")
    private boolean consecutiveSequenceNumbering;

    /**
     * Adds the specified label to the allowed labels for ligands.
     *
     * @param ligandLabel The label (three-letter code).
     */
    public void addToLigandLabelWhiteList(String ligandLabel) {
        ligandLabelWhitelist.add(ligandLabel);
    }

    public String getChainListSeparator() {
        return chainListSeparator;
    }

    public void setChainListSeparator(String chainListSeparator) {
        this.chainListSeparator = chainListSeparator;
    }

    public List<String> getLigandLabelWhitelist() {
        return ligandLabelWhitelist;
    }

    public void setLigandLabelWhitelist(List<String> ligandLabelWhitelist) {
        this.ligandLabelWhitelist = ligandLabelWhitelist;
    }

    public String getPdbLocation() {
        return pdbLocation;
    }

    public void setPdbLocation(String pdbLocation) {
        this.pdbLocation = pdbLocation;
    }

    public PDBSequenceCluster getPdbSequenceCluster() {
        return pdbSequenceCluster;
    }

    public void setPdbSequenceCluster(PDBSequenceCluster pdbSequenceCluster) {
        this.pdbSequenceCluster = pdbSequenceCluster;
    }

    public boolean isConsecutiveSequenceNumbering() {
        return consecutiveSequenceNumbering;
    }

    public void setConsecutiveSequenceNumbering(boolean consecutiveSequenceNumbering) {
        this.consecutiveSequenceNumbering = consecutiveSequenceNumbering;
    }

    public boolean isParseLigands() {
        return parseLigands;
    }

    public void setParseLigands(boolean parseLigands) {
        this.parseLigands = parseLigands;
    }

    public boolean isParseNucleotides() {
        return parseNucleotides;
    }

    public void setParseNucleotides(boolean parseNucleotides) {
        this.parseNucleotides = parseNucleotides;
    }

    public boolean isParseWater() {
        return parseWater;
    }

    public void setParseWater(boolean parseWater) {
        this.parseWater = parseWater;
    }

    public enum PDBSequenceCluster {
        IDENTITY_100, IDENTITY_95, IDENTITY_90, IDENTITY_70, IDENTITY_50, IDENTITY_40, IDENTITY_30;

        public int getIdentity() {
            return Integer.parseInt(this.name().split("_")[1]);
        }
    }
}
