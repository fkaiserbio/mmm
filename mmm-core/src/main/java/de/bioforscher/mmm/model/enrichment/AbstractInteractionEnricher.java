package de.bioforscher.mmm.model.enrichment;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.pliprestprovider.model.InteractionType;
import de.bioforscher.pliprestprovider.model.PlipInteraction;
import de.bioforscher.singa.chemistry.descriptive.elements.ElementProvider;
import de.bioforscher.singa.chemistry.physical.atoms.Atom;
import de.bioforscher.singa.chemistry.physical.atoms.RegularAtom;
import de.bioforscher.singa.chemistry.physical.families.LigandFamily;
import de.bioforscher.singa.chemistry.physical.leaves.AtomContainer;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;
import de.bioforscher.singa.chemistry.physical.model.LeafIdentifier;
import de.bioforscher.singa.mathematics.graphs.model.Node;
import de.bioforscher.singa.mathematics.vectors.Vector;
import de.bioforscher.singa.mathematics.vectors.Vector3D;
import de.bioforscher.singa.mathematics.vectors.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract class for interaction information {@link DataPointEnricher}s.
 *
 * @author fk
 */
public abstract class AbstractInteractionEnricher implements DataPointEnricher<String> {

    public static final Map<InteractionType, String> INTERACTION_LABEL_MAP;
    public static final List<InteractionType> ACTIVE_INTERACTIONS;

    private static final Logger logger = LoggerFactory.getLogger(AbstractInteractionEnricher.class);

    static String PLIP_REST_PROVIDER_CREDENTIALS;

    static {

        // load PLIP credentials
        URL baseConfigurationResource = Thread.currentThread().getContextClassLoader()
                                              .getResource("plip_credentials.txt");
        if (baseConfigurationResource != null) {
            try {
                PLIP_REST_PROVIDER_CREDENTIALS = Files.readAllLines(Paths.get(baseConfigurationResource.toURI())).get(0);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("failed to load PLIP credentials, please provide file plip_credentials.txt under class resources");
            }
        }

        INTERACTION_LABEL_MAP = new HashMap<>();
        INTERACTION_LABEL_MAP.put(InteractionType.HALOGEN_BOND, "hal");
        INTERACTION_LABEL_MAP.put(InteractionType.HYDROGEN_BOND, "hyb");
        INTERACTION_LABEL_MAP.put(InteractionType.HYDROPHOBIC, "hyp");
        INTERACTION_LABEL_MAP.put(InteractionType.METAL_COMPLEX, "mec");
        INTERACTION_LABEL_MAP.put(InteractionType.PI_CATION, "pic");
        INTERACTION_LABEL_MAP.put(InteractionType.PI_STACKING, "pis");
        INTERACTION_LABEL_MAP.put(InteractionType.SALT_BRIDGE, "sab");
        INTERACTION_LABEL_MAP.put(InteractionType.WATER_BRIDGE, "wab");

        ACTIVE_INTERACTIONS = new ArrayList<>();
        ACTIVE_INTERACTIONS.add(InteractionType.HYDROGEN_BOND);
        ACTIVE_INTERACTIONS.add(InteractionType.METAL_COMPLEX);
        ACTIVE_INTERACTIONS.add(InteractionType.PI_CATION);
        ACTIVE_INTERACTIONS.add(InteractionType.PI_STACKING);
        ACTIVE_INTERACTIONS.add(InteractionType.SALT_BRIDGE);
//        ACTIVE_INTERACTIONS.add(InteractionType.WATER_BRIDGE);
    }

    /**
     * Adds the given {@link PlipInteraction} to the specified {@link DataPoint}.
     *
     * @param interaction The {@link PlipInteraction} to be added.
     * @param dataPoint   The {@link DataPoint} to which the {@link PlipInteraction} should be added.
     */
    void addInteractionItem(PlipInteraction interaction, DataPoint<String> dataPoint) {

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
                                          .mapToInt(Node::getIdentifier)
                                          .max()
                                          .orElseThrow(() -> new RuntimeException("failed to determine next atom identifer")) + 1;

        Vector interactionCentroid = Vectors.getCentroid(interaction.getInteractionCoordinates().stream()
                                                                    .map(Vector3D::new)
                                                                    .collect(Collectors.toList()));

        // create new atom container
        LigandFamily family = new LigandFamily("X", INTERACTION_LABEL_MAP.get(interaction.getInteractionType()));
        AtomContainer<LigandFamily> atomContainer = new AtomContainer<>(new LeafIdentifier(dataPoint.getDataPointIdentifier().getPdbIdentifier(),
                                                                                           0,
                                                                                           dataPoint.getDataPointIdentifier().getChainIdentifier(),
                                                                                           nextLeafIdentifier), family);

        Atom interactionPseudoAtom = new RegularAtom(nextAtomIdentifier, ElementProvider.UNKOWN, "CA", interactionCentroid.as(Vector3D.class));
        atomContainer.addNode(interactionPseudoAtom);
        Item<String> interactionItem = new Item<>(INTERACTION_LABEL_MAP.get(interaction.getInteractionType()), atomContainer);
        dataPoint.getItems().add(interactionItem);

        logger.debug("added {} to data point {}", interactionItem, dataPoint);
    }
}
