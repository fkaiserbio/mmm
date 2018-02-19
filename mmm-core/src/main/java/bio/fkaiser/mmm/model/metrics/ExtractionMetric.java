package bio.fkaiser.mmm.model.metrics;

import bio.fkaiser.mmm.model.Itemset;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.representations.RepresentationSchemeType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An {@link EvaluationMetric} capable of extracting {@link Itemset} observations from the data.
 *
 * @author fk
 */
public interface ExtractionMetric<LabelType extends Comparable<LabelType>> extends EvaluationMetric<LabelType> {

    Predicate<EvaluationMetric<?>> EXTRACTION_METRIC_FILTER = evaluationMetric -> evaluationMetric instanceof ExtractionMetric;

    boolean isVertexOne();

    RepresentationSchemeType getRepresentationSchemeType();

    Map<Itemset<LabelType>, List<Itemset<LabelType>>> getExtractedItemsets();

    /**
     * Filters the given {@link Itemset}s according to the metric
     *
     * @param itemsets the itemsets to be filtered
     * @return the filtered itemsets
     */
    Set<Itemset<LabelType>> filterItemsets(Set<Itemset<LabelType>> itemsets);


}
