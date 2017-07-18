package de.bioforscher.mmm.model.mapping.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.mmm.model.mapping.MappingRule;
import de.bioforscher.singa.chemistry.physical.families.AminoAcidFamily;
import de.bioforscher.singa.chemistry.physical.families.NucleotideFamily;
import de.bioforscher.singa.chemistry.physical.model.StructuralFamily;

import java.util.Optional;

/**
 * @author fk
 */
@JsonTypeName("EXCLUDE_FAMILY")
public class ExcludeFamilyMappingRule<LabelType extends Comparable<LabelType>> implements MappingRule<LabelType> {

    public static final StructuralFamily<?> DEFAULT_FAMILY_TO_EXCLUDE = AminoAcidFamily.UNKNOWN;

    @JsonProperty("family-to-exclude")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({@JsonSubTypes.Type(value = AminoAcidFamily.class, name = "AMINO_ACID"),
                   @JsonSubTypes.Type(value = NucleotideFamily.class, name = "NUCLEOTIDE")})
    private StructuralFamily<?> familyToExclude = DEFAULT_FAMILY_TO_EXCLUDE;

    public ExcludeFamilyMappingRule() {
    }

    public ExcludeFamilyMappingRule(StructuralFamily<?> familyToExclude) {
        this.familyToExclude = familyToExclude;
    }

    @Override
    public Optional<Item<LabelType>> mapItem(Item<LabelType> item) {
        if (item.getLeafSubstructure().isPresent()) {
            if (item.getLeafSubstructure().get().getFamily().equals(familyToExclude)) {
                return Optional.empty();
            } else {
                return Optional.of(item);
            }
        }
        return Optional.of(item);
    }

    @Override
    public Item<LabelType> apply(Item<LabelType> item) {
        return mapItem(item).orElse(null);
    }

    @Override
    public String toString() {
        return "ExcludeFamilyMappingRule{" +
               "familyToExclude=" + familyToExclude +
               '}';
    }

    public StructuralFamily<?> getFamilyToExclude() {
        return familyToExclude;
    }

    public void setFamilyToExclude(StructuralFamily<?> familyToExclude) {
        this.familyToExclude = familyToExclude;
    }
}
