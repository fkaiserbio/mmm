package de.bioforscher.mmm.classify.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.bioforscher.mmm.model.Itemset;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusAlignment;
import de.bioforscher.singa.chemistry.algorithms.superimposition.consensus.ConsensusContainer;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureRepresentation;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import de.bioforscher.singa.mathematics.graphs.trees.BinaryTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author fk
 */
public class ItemsetLibrary {

    public static final TypeReference<ItemsetLibrary> TYPE_REFERENCE = new TypeReference<ItemsetLibrary>() {
    };
    private static final Logger logger = LoggerFactory.getLogger(ItemsetLibrary.class);
    private List<ItemsetLibraryEntry> entries;

    private ItemsetLibrary() {
    }

    private ItemsetLibrary(List<ItemsetLibraryEntry> entries) {
        this.entries = entries;
    }

    public static ItemsetLibrary of(Map<Itemset<String>, ConsensusAlignment> clusteredItemsets, int minimalClusterSize) {
        List<ItemsetLibraryEntry> entries = new ArrayList<>();
        for (Map.Entry<Itemset<String>, ConsensusAlignment> entry : clusteredItemsets.entrySet()) {
            Itemset<String> itemset = entry.getKey();
            // determine largest cluster
            TreeSet<BinaryTree<ConsensusContainer>> clusters = new TreeSet<>(Comparator.comparing(BinaryTree::size));
            clusters.addAll(entry.getValue().getClusters());
            BinaryTree<ConsensusContainer> largestCluster = clusters.last();
            if (largestCluster.size() < minimalClusterSize) {
                logger.info("itemset {} not added to the library, largest cluster size not sufficient", itemset);
                continue;
            }
            StructuralMotif structuralMotif = clusters.last().getRoot().getData().getStructuralMotif();
            String pdbLines = StructureRepresentation.composePdbRepresentation(structuralMotif.getLeafSubstructures());
            ItemsetLibraryEntry libraryEntry = new ItemsetLibraryEntry(entry.getKey().toSimpleString(), pdbLines);
            entries.add(libraryEntry);
        }
        return new ItemsetLibrary(entries);
    }

    public static ItemsetLibrary of(List<Itemset<String>> itemsets) {
        List<ItemsetLibraryEntry> entries = new ArrayList<>();
        for (Itemset<String> itemset : itemsets) {
            // TODO implement proper exception
            StructuralMotif structuralMotif =
                    itemset.getStructuralMotif().orElseThrow(() -> new UnsupportedOperationException("itemset libraries can only be constructed out of itemset observations"));
            String pdbLines = StructureRepresentation.composePdbRepresentation(structuralMotif.getLeafSubstructures());
            ItemsetLibraryEntry entry = new ItemsetLibraryEntry(itemset.toSimpleString(), pdbLines);
            entries.add(entry);
        }
        return new ItemsetLibrary(entries);
    }

    public static ItemsetLibrary readFromPath(Path libraryPath) throws IOException {
        try (GZIPInputStream zip = new GZIPInputStream(new FileInputStream(libraryPath.toFile()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(zip, "UTF-8"))) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
            return mapper.readValue(reader, TYPE_REFERENCE);
        }
    }

    public static ItemsetLibrary fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        return mapper.readValue(json, TYPE_REFERENCE);
    }

    public List<ItemsetLibraryEntry> getEntries() {
        return entries;
    }

    public void writeToPath(Path libraryPath) throws IOException {
        try (GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(libraryPath.toFile()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"))) {
            writer.append(toJson());
        }
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        return mapper.writeValueAsString(this);
    }
}
