package bio.fkaiser.mmm.model.metrics;

import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.Itemset;
import bio.fkaiser.mmm.model.configurations.metrics.SeparationMetricConfiguration;
import de.bioforscher.singa.structure.model.interfaces.AminoAcid;
import de.bioforscher.singa.structure.model.interfaces.Nucleotide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An {@link ExtractionMetric} that forces {@link Itemset}s to have a minimal sequence separation. For further reference see
 * <p/>
 * Kaiser, F. & Labudde, D. IEEE/ACM Trans. Comput. Biol. Bioinform. (under review)
 *
 * @author fk
 */
public class SeparationMetric<LabelType extends Comparable<LabelType>> extends AbstractExtractionDependentMetric<LabelType> {

    public static final Comparator<Itemset<?>> COMPARATOR = Comparator.comparing(Itemset::getSeparation);
    private static final Logger logger = LoggerFactory.getLogger(SeparationMetric.class);
    /**
     * the range for which discrete values of Morse potential function should be calculated
     */
    private static final int MORSE_POTENTIAL_DISCRETE_RANGE = 10000;
    private final HashMap<Integer, Double> morsePotentialDiscrete;

    private final double maximalSeparation;
    private final double optimalSeparation;
    private final double morseWellDepth;
    private final double morseShape;

    public SeparationMetric(SeparationMetricConfiguration<LabelType> separationMetricConfiguration) {
        this.maximalSeparation = separationMetricConfiguration.getMaximalSeparation();
        this.optimalSeparation = separationMetricConfiguration.getOptimalSeparation();
        this.morseWellDepth = separationMetricConfiguration.getMorseWellDepth();
        this.morseShape = separationMetricConfiguration.getMorseShape();
        // initialize Morse potential function with discrete values
        morsePotentialDiscrete = new HashMap<>();
        for (int i = 0; i < MORSE_POTENTIAL_DISCRETE_RANGE; i++) {
            morsePotentialDiscrete.put(i, calculateMorsePotential(morseWellDepth, morseShape, optimalSeparation, i));
        }
    }

    private static double calculateMorsePotential(double d, double a, double re, double r) {
        return d * (1 - Math.exp(-a * (r - re))) * (1 - Math.exp(-a * (r - re))) - d;
    }

    @Override
    public int getMinimalItemsetSize() {
        return 2;
    }

    @Override
    public Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets, Map<Itemset<LabelType>, List<Itemset<LabelType>>> extractedItemsets) {
        this.extractedItemsets = extractedItemsets;
        return calculateSeparation(itemsets).stream()
                                            .filter(itemset -> itemset.getSeparation() <= maximalSeparation)
                                            .collect(Collectors.toSet());
    }

    @Override public String toString() {
        return "SeparationMetric{" +
               "maximalSeparation=" + maximalSeparation +
               '}';
    }

    private Set<Itemset<LabelType>> calculateSeparation(Set<Itemset<LabelType>> itemsets) {

        for (Itemset<LabelType> itemset : extractedItemsets.keySet()) {

            List<Itemset<LabelType>> itemsetObservations = extractedItemsets.get(itemset);

            double itemsetSeparation = 0.0;
            for (Itemset<LabelType> itemsetObservation : itemsetObservations) {

                // sort items according to their ascending position and exclude interaction items
                List<Item<LabelType>> sortedItems = itemsetObservation.getItems().stream()
                                                                      // only consider amino acids or nucleotides for the separation calculation
                                                                      .filter(item -> item.getLeafSubstructure().isPresent() &&
                                                                                      (item.getLeafSubstructure().get() instanceof AminoAcid || item.getLeafSubstructure().get() instanceof Nucleotide))
                                                                      .sorted(Comparator.comparingInt(Item::getSequencePosition))
                                                                      .collect(Collectors.toList());

                // calculate separation of single observation
                double observationSeparation = 0.0;
                for (int i = 0; i < sortedItems.size() - 1; i++) {
                    int gapLength = sortedItems.get(i + 1).getSequencePosition() - sortedItems.get(i).getSequencePosition();
                    // cannot decide due to ambiguous position
                    if (gapLength < 0) {
                        logger.warn("found ambiguous position for observation {}", itemsetObservation);
                    } else {
                        observationSeparation += morsePotentialDiscrete.get(gapLength);
                    }
                }
                // normalize separation of single observation
                observationSeparation /= itemset.getItems().size();

                // sum up itemset separation
                itemsetSeparation += observationSeparation;
            }

            // set normalized separation
            itemset.setSeparation(itemsetSeparation / itemsetObservations.size());
        }
        return itemsets;
    }

    @Override
    public void filterExtractedItemsets() {
        extractedItemsets.entrySet().removeIf(entry -> entry.getKey().getSeparation() > maximalSeparation);
    }
}
