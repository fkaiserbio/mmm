package de.bioforscher.mmm.model.mapping;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.mmm.model.mapping.rules.ChemicalGroupsMappingRule;

import java.util.Optional;
import java.util.function.Function;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = ChemicalGroupsMappingRule.class)})
public interface MappingRule<LabelType extends Comparable<LabelType>> extends Function<Item<LabelType>, Item<LabelType>> {
    Optional<Item<LabelType>> mapItem(Item<LabelType> item);
}
