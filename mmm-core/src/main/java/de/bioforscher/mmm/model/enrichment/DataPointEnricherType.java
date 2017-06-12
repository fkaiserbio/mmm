package de.bioforscher.mmm.model.enrichment;

/**
 * @author fk
 */
public enum DataPointEnricherType {

    INTERACTION(InteractionEnricher.class);

    private final Class<?> implementation;

    DataPointEnricherType(Class<?> interactionEnricherClass) {
        implementation = interactionEnricherClass;
    }

    public Class<?> getImplementation() {
        return implementation;
    }
}
