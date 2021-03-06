package bio.fkaiser.mmm.classify.model;

import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.Itemset;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class ItemsetLibraryEntry {

    @JsonProperty("identifier")
    private String identifier;
    @JsonProperty("labels")
    private List<String> labels;
    @JsonProperty("pdb-lines")
    private String pdbLines;
    @JsonProperty("size")
    private int size;

    public ItemsetLibraryEntry() {
    }

    public ItemsetLibraryEntry(Itemset<String> itemset, String pdbLines) {
        identifier = itemset.toSimpleString();
        labels = itemset.getItems().stream()
                        .map(Item::getLabel)
                        .collect(Collectors.toList());
        size = itemset.getItems().size();
        this.pdbLines = pdbLines;
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPdbLines() {
        return pdbLines;
    }

    public int getSize() {
        return size;
    }
}
