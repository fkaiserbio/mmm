package de.bioforscher.mmm.model.mapping.rules;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.mmm.model.mapping.MappingRule;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@JsonTypeName("CHEMICAL_GROUPS")
public class ChemicalGroupsMappingRule implements MappingRule<String> {

    public static Map<String, String> CHEMICAL_GROUP_MAPPING = new HashMap<>();

    static {
        CHEMICAL_GROUP_MAPPING.put("Ala", "oth");
        CHEMICAL_GROUP_MAPPING.put("Arg", "gua");
        CHEMICAL_GROUP_MAPPING.put("Asn", "amd");
        CHEMICAL_GROUP_MAPPING.put("Asp", "car");
        CHEMICAL_GROUP_MAPPING.put("Cys", "thi");
        CHEMICAL_GROUP_MAPPING.put("Gln", "amd");
        CHEMICAL_GROUP_MAPPING.put("Glu", "car");
        CHEMICAL_GROUP_MAPPING.put("Gly", "oth");
        CHEMICAL_GROUP_MAPPING.put("His", "imi");
        CHEMICAL_GROUP_MAPPING.put("Ile", "oth");
        CHEMICAL_GROUP_MAPPING.put("Leu", "oth");
        CHEMICAL_GROUP_MAPPING.put("Lys", "amn");
        CHEMICAL_GROUP_MAPPING.put("Met", "oth");
        CHEMICAL_GROUP_MAPPING.put("Phe", "oth");
        CHEMICAL_GROUP_MAPPING.put("Pro", "oth");
        CHEMICAL_GROUP_MAPPING.put("Ser", "hyd");
        CHEMICAL_GROUP_MAPPING.put("Thr", "hyd");
        CHEMICAL_GROUP_MAPPING.put("Trp", "oth");
        CHEMICAL_GROUP_MAPPING.put("Tyr", "hyd");
        CHEMICAL_GROUP_MAPPING.put("Val", "oth");
    }

    @Override
    public Optional<Item<String>> mapItem(Item<String> item) {
        // do not substitute unknown label
        if (!CHEMICAL_GROUP_MAPPING.containsKey(item.getLabel())) {
            return Optional.of(item);
        }
        item.setLabel(CHEMICAL_GROUP_MAPPING.get(item.getLabel()));
        return Optional.of(item);
    }

    @Override
    public Item<String> apply(Item<String> item) {
        return mapItem(item).orElse(null);
    }
}
