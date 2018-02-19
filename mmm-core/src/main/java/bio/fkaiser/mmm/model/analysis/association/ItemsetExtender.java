package bio.fkaiser.mmm.model.analysis.association;

import bio.fkaiser.mmm.ItemsetMiner;
import bio.fkaiser.mmm.model.DataPoint;
import bio.fkaiser.mmm.model.DataPointIdentifier;
import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.graphs.ItemsetGraph;
import bio.fkaiser.mmm.model.graphs.ItemsetNode;
import de.bioforscher.singa.mathematics.algorithms.graphs.DisconnectedSubgraphFinder;
import de.bioforscher.singa.structure.algorithms.superimposition.SubstructureSuperimposer;
import de.bioforscher.singa.structure.algorithms.superimposition.SubstructureSuperimposition;
import de.bioforscher.singa.structure.model.families.LigandFamily;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Naiive implementation of {@link Itemset} extension based on a given {@link ItemsetGraph}.
 *
 * @author fk
 */
public class ItemsetExtender<LabelType extends Comparable<LabelType>> {

    private static final Logger logger = LoggerFactory.getLogger(ItemsetExtender.class);

    private final ItemsetMiner<LabelType> itemsetMiner;
    private final Path outputPath;
    private final ItemsetGraph<LabelType> itemsetGraph;

    private LigandFamily referenceLigandFamily;
    private Predicate<LeafSubstructure> alignmentReferenceFilter;
    private Map<DataPointIdentifier, StructuralMotif> mergedMotifs;

    public ItemsetExtender(ItemsetMiner<LabelType> itemsetMiner, ItemsetGraph<LabelType> itemsetGraph, Path outputPath) {
        this.itemsetMiner = itemsetMiner;
        this.itemsetGraph = itemsetGraph;
        this.outputPath = outputPath;
        mergedMotifs = new TreeMap<>(DataPointIdentifier.COMPARATOR);
        findDisconnectedSubgraphs();
    }

    public ItemsetExtender(ItemsetMiner<LabelType> itemsetMiner, ItemsetGraph<LabelType> itemsetGraph, Path outputPath, LigandFamily referenceLigandFamily) {
        this.itemsetMiner = itemsetMiner;
        this.itemsetGraph = itemsetGraph;
        this.outputPath = outputPath;
        this.referenceLigandFamily = referenceLigandFamily;
        if (referenceLigandFamily != null) {
            alignmentReferenceFilter = leafSubstructure -> leafSubstructure.getFamily().equals(referenceLigandFamily);
        }

        mergedMotifs = new TreeMap<>(DataPointIdentifier.COMPARATOR);
        findDisconnectedSubgraphs();
    }


    public Map<DataPointIdentifier, StructuralMotif> getMergedMotifs() {
        return mergedMotifs;
    }

    /**
     * Finds disconnected subgraphs in the given {@link ItemsetGraph}.
     */
    private void findDisconnectedSubgraphs() {

        List<ItemsetGraph<LabelType>> subgraphs = DisconnectedSubgraphFinder.findDisconnectedSubgraphs(itemsetGraph);

        logger.info("found {} disconnected subgraphs", subgraphs.size());

        for (ItemsetGraph<LabelType> subgraph : subgraphs) {

            List<Itemset<LabelType>> subgraphItemsets = subgraph.getNodes().stream()
                                                                .map(ItemsetNode::getItemset)
                                                                .collect(Collectors.toList());

            // collect all extracted itemsets for subgraph
            List<Itemset<LabelType>> extractedItemsets = subgraphItemsets.stream()
                                                                         .map(itemset -> itemsetMiner.getTotalExtractedItemsets().get(itemset))
                                                                         .flatMap(Collection::stream)
                                                                         .collect(Collectors.toList());

            mergeItemsets(extractedItemsets);
            if (referenceLigandFamily != null) {
                logger.info("aligning all merged subgraphs on common ligand {}", referenceLigandFamily);
                alignInLigand(subgraph);
            }
            try {
                writeMergedMotifs(subgraph);
            } catch (IOException e) {
                logger.warn("failed to write merged motifs to {}", outputPath, e);
            }
        }
    }

    /**
     * Aligns all members of the subgraph in the specified ligand.
     *
     * @param subgraph The subgraph of which all members should be aligned in the ligand.
     */
    private void alignInLigand(ItemsetGraph<LabelType> subgraph) {

        // pick first a first reference leaf substructure from the current subgraph
        Optional<LeafSubstructure<?>> referenceLeafSubstructure = mergedMotifs.values().stream()
                                                                              .map(StructuralMotif::getAllLeafSubstructures)
                                                                              .flatMap(Collection::stream)
                                                                              .filter(alignmentReferenceFilter)
                                                                              .findFirst();
        if (referenceLeafSubstructure.isPresent()) {

            logger.info("handling subgraph {}", subgraph);

            for (Map.Entry<DataPointIdentifier, StructuralMotif> entry : mergedMotifs.entrySet()) {

                Optional<LeafSubstructure<?>> candidateLeafSubstructure = entry.getValue().getAllLeafSubstructures().stream()
                                                                               .filter(alignmentReferenceFilter)
                                                                               .findFirst();
                if (candidateLeafSubstructure.isPresent()) {
                    // compute superimposition based on ligand and apply to merged motif
                    List<LeafSubstructure<?>> candidate = new ArrayList<>();
                    candidate.add(candidateLeafSubstructure.get());
                    List<LeafSubstructure<?>> reference = new ArrayList<>();
                    reference.add(referenceLeafSubstructure.get());
                    SubstructureSuperimposition superimposition = SubstructureSuperimposer.calculateSubstructureSuperimposition(reference, candidate);
                    StructuralMotif mappedStructuralMotif = StructuralMotif.fromLeafSubstructures(superimposition.applyTo(entry.getValue().getAllLeafSubstructures()));
                    entry.setValue(mappedStructuralMotif);
                } else {
                    logger.warn("no ligand found to be aligned for structure {}", entry.getKey());
                }
            }
        } else {
            logger.warn("no ligand found to be aligned in all structures for subgraph {}", subgraph);
        }
    }

    /**
     * Writes all merged {@link StructuralMotif}s of the given subgraph.
     *
     * @param subgraph The subgraph of which all {@link StructuralMotif}s should be written.
     * @throws IOException If the merged motifs cannot be written.
     */
    private void writeMergedMotifs(ItemsetGraph<LabelType> subgraph) throws IOException {

        logger.info("writing {} merged motifs", mergedMotifs.size());

        String subgraphItemString = subgraph.getNodes()
                                            .stream()
                                            .map(ItemsetNode::getItemset)
                                            .map(Itemset::getItems)
                                            .flatMap(Collection::stream)
                                            .map(Item::toString)
                                            .distinct()
                                            .sorted()
                                            .collect(Collectors.joining("-"));

        for (Map.Entry<DataPointIdentifier, StructuralMotif> entry : mergedMotifs.entrySet()) {
            StructuralMotif structuralMotif = entry.getValue();
            logger.debug("writing motif {}", structuralMotif);
            StructureWriter.writeLeafSubstructureContainer(entry.getValue(), outputPath.resolve(subgraphItemString).resolve(structuralMotif.toString().split("_")[0] + ".pdb"));
        }
    }

    /**
     * Merges the given extracted {@link Itemset}s for each {@link DataPoint}.
     *
     * @param extractedItemsets The extracted {@link Itemset}s to be merged.
     */
    private void mergeItemsets(List<Itemset<LabelType>> extractedItemsets) {

        // group itemsets by their data point
        Map<DataPointIdentifier, List<Itemset<LabelType>>> itemsetsOfDataPoints = new TreeMap<>(DataPointIdentifier.COMPARATOR);

        for (Itemset<LabelType> extractedItemset : extractedItemsets) {
            if (extractedItemset.getOriginDataPointIdentifier().isPresent()) {
                DataPointIdentifier dataPointIdentifier = extractedItemset.getOriginDataPointIdentifier().get();
                if (itemsetsOfDataPoints.containsKey(dataPointIdentifier)) {
                    itemsetsOfDataPoints.get(dataPointIdentifier).add(extractedItemset);
                } else {
                    List<Itemset<LabelType>> itemsets = new ArrayList<>();
                    itemsets.add(extractedItemset);
                    itemsetsOfDataPoints.put(dataPointIdentifier, itemsets);
                }
            }
        }

        for (Map.Entry<DataPointIdentifier, List<Itemset<LabelType>>> entry : itemsetsOfDataPoints.entrySet()) {
            // collect all leaf substructures
            List<LeafSubstructure<?>> allLeafSubStructures = entry.getValue().stream()
                                                                  .map(Itemset::getStructuralMotif)
                                                                  .filter(Optional::isPresent)
                                                                  .map(Optional::get)
                                                                  .map(StructuralMotif::getAllLeafSubstructures)
                                                                  .flatMap(Collection::stream)
                                                                  .sorted(Comparator.comparing(LeafSubstructure::getIdentifier))
                                                                  .distinct()
                                                                  .collect(Collectors.toList());
            // convert to structural motif
            StructuralMotif mergedMotif = StructuralMotif.fromLeafSubstructures(allLeafSubStructures);

            mergedMotifs.put(entry.getKey(), mergedMotif);
        }
    }
}
