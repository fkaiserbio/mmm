package de.bioforscher.mmm.model.enrichment;

/**
 * @author fk
 */
public enum DataPointEnricherType {

    INTRA_CHAIN_INTERACTION(IntraChainInteractionEnricher.class),
    LIGAND_INTERACTION(LigandInteractionEnricher.class);

    private final Class<?> implementation;

    DataPointEnricherType(Class<?> interactionEnricherClass) {
        implementation = interactionEnricherClass;
    }

    public Class<?> getImplementation() {
        return implementation;
    }
}
