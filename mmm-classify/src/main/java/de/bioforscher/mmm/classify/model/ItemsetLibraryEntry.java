package de.bioforscher.mmm.classify.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author fk
 */
public class ItemsetLibraryEntry {

    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("pdb-lines")
    private String pdbLines;

    public ItemsetLibraryEntry(String identifier, String pdbLines) {
        this.identifier = identifier;
        this.pdbLines = pdbLines;
    }

    public ItemsetLibraryEntry() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPdbLines() {
        return pdbLines;
    }
}
