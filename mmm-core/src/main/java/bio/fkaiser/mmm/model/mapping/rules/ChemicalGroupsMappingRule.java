package bio.fkaiser.mmm.model.mapping.rules;

import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.mapping.MappingRule;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link MappingRule} for polar and charged amino acids according to
 * <p/>
 * Gutteridge, A. & Thornton, J. M. Understanding
 * nature's catalytic toolkit Trends in biochemical sciences, Elsevier, 2005, 30, 622-629
 * <p/>
 * chemical groups are:
 * <pre>
 *     imidazole (imi)    H
 *     guanidinium (gua)  R
 *     amine (amn)        K
 *     carboxylate (car)  D,E
 *     amide (amd)        N,Q
 *     hydroxyl (hyd)     S,T,Y
 *     thiol (thi)        C
 * </pre>
 * remaining amino acids are classified as "other (oth)".
 *
 * @author fk
 **/
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
