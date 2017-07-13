package de.bioforscher.mmm.model.mapping.rules;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.mmm.model.mapping.MappingRule;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link MappingRule} for functional groups of amino acids. The following types are grouped according to functional chemical groups:
 * <p>
 * <pre>
 *      aromatic (aro)              F,Y,W
 *      negatively charged (neg)    D,E
 *      positively charged (ppos)   K,R,H
 *      polar, uncharged (pol)      P,N,Q,C,T,S
 *      nonpolar, aliphatic (upo)   G,A,V,L,M,I
 * </pre>
 *
 * @author fk
 */
@JsonTypeName("FUNCTIONAL_GROUPS")
public class FunctionalGroupsMappingRule implements MappingRule<String> {

    public static Map<String, String> FUNCTIONAL_GROUP_MAPPING = new HashMap<>();

    static {
        FUNCTIONAL_GROUP_MAPPING.put("Ala", "upo");
        FUNCTIONAL_GROUP_MAPPING.put("Arg", "pos");
        FUNCTIONAL_GROUP_MAPPING.put("Asn", "pol");
        FUNCTIONAL_GROUP_MAPPING.put("Asp", "neg");
        FUNCTIONAL_GROUP_MAPPING.put("Cys", "pol");
        FUNCTIONAL_GROUP_MAPPING.put("Gln", "pol");
        FUNCTIONAL_GROUP_MAPPING.put("Glu", "neg");
        FUNCTIONAL_GROUP_MAPPING.put("Gly", "upo");
        FUNCTIONAL_GROUP_MAPPING.put("His", "pos");
        FUNCTIONAL_GROUP_MAPPING.put("Ile", "upo");
        FUNCTIONAL_GROUP_MAPPING.put("Leu", "upo");
        FUNCTIONAL_GROUP_MAPPING.put("Lys", "pos");
        FUNCTIONAL_GROUP_MAPPING.put("Met", "upo");
        FUNCTIONAL_GROUP_MAPPING.put("Phe", "aro");
        FUNCTIONAL_GROUP_MAPPING.put("Pro", "pol");
        FUNCTIONAL_GROUP_MAPPING.put("Ser", "pol");
        FUNCTIONAL_GROUP_MAPPING.put("Thr", "pol");
        FUNCTIONAL_GROUP_MAPPING.put("Trp", "aro");
        FUNCTIONAL_GROUP_MAPPING.put("Tyr", "aro");
        FUNCTIONAL_GROUP_MAPPING.put("Val", "upo");
    }

    @Override
    public Optional<Item<String>> mapItem(Item<String> item) {
        // do not substitute unknown label
        if (!FUNCTIONAL_GROUP_MAPPING.containsKey(item.getLabel())) {
            return Optional.of(item);
        }
        item.setLabel(FUNCTIONAL_GROUP_MAPPING.get(item.getLabel()));
        return Optional.of(item);
    }

    @Override
    public Item<String> apply(Item<String> item) {
        return mapItem(item).orElse(null);
    }
}
