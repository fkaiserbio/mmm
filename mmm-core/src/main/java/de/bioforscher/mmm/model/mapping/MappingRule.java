package de.bioforscher.mmm.model.mapping;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.mmm.model.mapping.rules.*;

import java.util.Optional;
import java.util.function.Function;

/**
 * An interface for {@link MappingRule}s.
 *
 * @author fk
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = ChemicalGroupsMappingRule.class),
               @JsonSubTypes.Type(value = FunctionalGroupsMappingRule.class),
               @JsonSubTypes.Type(value = NoAminoAcidsMappingRule.class),
               @JsonSubTypes.Type(value = ExcludeFamilyMappingRule.class),
               @JsonSubTypes.Type(value = InteractionShellMappingRule.class)})

public interface MappingRule<LabelType extends Comparable<LabelType>> extends Function<Item<LabelType>, Item<LabelType>> {
    Optional<Item<LabelType>> mapItem(Item<LabelType> item);
}
