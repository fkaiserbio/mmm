package de.bioforscher.mmm.model.enrichment;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.singa.chemistry.descriptive.elements.ElementProvider;
import de.bioforscher.singa.mathematics.vectors.Vector;
import de.bioforscher.singa.mathematics.vectors.Vector3D;
import de.bioforscher.singa.mathematics.vectors.Vectors;
import de.bioforscher.singa.structure.model.families.LigandFamily;
import de.bioforscher.singa.structure.model.identifiers.LeafIdentifier;
import de.bioforscher.singa.structure.model.interfaces.Atom;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import de.bioforscher.singa.structure.model.oak.OakAtom;
import de.bioforscher.singa.structure.model.oak.OakLigand;
import de.bioforscher.singa.structure.parser.plip.Interaction;
import de.bioforscher.singa.structure.parser.plip.InteractionContainer;
import de.bioforscher.singa.structure.parser.plip.InteractionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An abstract class for interaction information {@link DataPointEnricher}s.
 *
 * @author fk
 */
public abstract class AbstractInteractionEnricher implements DataPointEnricher<String> {

    // TODO implement option for active interactions
    public static final List<InteractionType> ACTIVE_INTERACTIONS;

    private static final Logger logger = LoggerFactory.getLogger(AbstractInteractionEnricher.class);

    static {
        ACTIVE_INTERACTIONS = new ArrayList<>();
        ACTIVE_INTERACTIONS.add(InteractionType.HYDROGEN_BOND);
        ACTIVE_INTERACTIONS.add(InteractionType.METAL_COMPLEX);
        ACTIVE_INTERACTIONS.add(InteractionType.PI_CATION_INTERACTION);
        ACTIVE_INTERACTIONS.add(InteractionType.PI_STACKING);
        ACTIVE_INTERACTIONS.add(InteractionType.SALT_BRIDGE);
        ACTIVE_INTERACTIONS.add(InteractionType.HALOGEN_BOND);
//        ACTIVE_INTERACTIONS.add(InteractionType.HYDROPHOBIC_INTERACTION);
//        ACTIVE_INTERACTIONS.add(InteractionType.WATER_BRIDGE);
    }

    /**
     * Adds the given {@link InteractionContainer} to the specified {@link DataPoint}.
     *
     * @param interaction The {@link InteractionContainer} to be added.
     * @param dataPoint   The {@link DataPoint} to which the {@link InteractionContainer} should be added.
     */
    void addInteractionItem(Interaction interaction, DataPoint<String> dataPoint) {
        // determine next identifiers
        int nextLeafIdentifier = dataPoint.getItems().stream()
                                          .map(Item::getLeafSubstructure)
                                          .filter(Optional::isPresent)
                                          .map(Optional::get)
                                          .mapToInt(leafSubstructure -> leafSubstructure.getIdentifier().getSerial())
                                          .max()
                                          .orElseThrow(() -> new RuntimeException("failed to determine next leaf identifer")) + 1;
        int nextAtomIdentifier = dataPoint.getItems().stream()
                                          .map(Item::getLeafSubstructure)
                                          .filter(Optional::isPresent)
                                          .map(Optional::get)
                                          .map(LeafSubstructure::getAllAtoms)
                                          .flatMap(Collection::stream)
                                          .mapToInt(Atom::getAtomIdentifier)
                                          .max()
                                          .orElseThrow(() -> new RuntimeException("failed to determine next atom identifer")) + 1;

        // calculate interaction centroid by using PLIP ligand and protein coordinates
        Vector interactionCentroid = Vectors.getCentroid(Stream.of(interaction.getLigandCoordinate(), interaction.getProteinCoordinate())
                                                               .map(Vector3D::new)
                                                               .collect(Collectors.toList()));

        // create new atom container
        String interactionThreeLetterCode = InteractionType.getThreeLetterCode(interaction.getClass());
        LigandFamily family = new LigandFamily("X", interactionThreeLetterCode);
        OakLigand ligandContainer = new OakLigand(new LeafIdentifier(dataPoint.getDataPointIdentifier().getPdbIdentifier(),
                                                                     0,
                                                                     dataPoint.getDataPointIdentifier().getChainIdentifier(),
                                                                     nextLeafIdentifier), family);

        OakAtom interactionPseudoAtom = new OakAtom(nextAtomIdentifier, ElementProvider.UNKOWN, "CA", interactionCentroid.as(Vector3D.class));
        ligandContainer.addAtom(interactionPseudoAtom);
        Item<String> interactionItem = new Item<>(interactionThreeLetterCode, ligandContainer);
        dataPoint.getItems().add(interactionItem);

        logger.debug("added {} to data point {}", interactionItem, dataPoint);
    }
}
