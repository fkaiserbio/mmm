package bio.fkaiser.mmm.model.mapping.rules;

import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.mapping.MappingRule;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.singa.structure.model.interfaces.AminoAcid;

import java.util.Optional;

/**
 * A {@link MappingRule} that allows no amino acids, i.e. they {@link Item}s conatining {@link AminoAcid}s are removed.
 *
 * @author fk
 */
@JsonTypeName("NO_AMINO_ACIDS")
public class NoAminoAcidsMappingRule<LabelType extends Comparable<LabelType>> implements MappingRule<LabelType> {
    @Override
    public Optional<Item<LabelType>> mapItem(Item<LabelType> item) {
        if (item.getLeafSubstructure().isPresent()) {
            if (item.getLeafSubstructure().get() instanceof AminoAcid) {
                return Optional.empty();
            } else {
                return Optional.of(item);
            }
        }
        return Optional.of(item);
    }

    @Override public Item<LabelType> apply(Item<LabelType> item) {
        return mapItem(item).orElse(null);
    }
}
