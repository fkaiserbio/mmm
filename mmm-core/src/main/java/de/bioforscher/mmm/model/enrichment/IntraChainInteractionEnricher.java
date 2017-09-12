package de.bioforscher.mmm.model.enrichment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.plip.PlipGetRequest;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.chemistry.parser.plip.InteractionContainer;
import de.bioforscher.singa.chemistry.parser.plip.InteractionType;
import de.bioforscher.singa.chemistry.physical.model.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Enriches {@link DataPoint}s with inter-chain interaction information predicted by the Protein-Ligand Interaction Profiler (PLIP). Interactions are abstracted as pseudoatoms defined as the
 * midpoint between interacting atoms.
 *
 * @author fk
 */
@JsonTypeName("INTRA_CHAIN_INTERACTION")
public class IntraChainInteractionEnricher extends AbstractInteractionEnricher {

    private static final Logger logger = LoggerFactory.getLogger(IntraChainInteractionEnricher.class);
    private static final String PLIP_REST_PROVIDER_URL = "https://biosciences.hs-mittweida.de/plip/interaction/plain";

    private static void validateInteractions(InteractionContainer interactions, DataPoint<String> dataPoint) {
        // TODO avoid fetching of structure every time
        Structure structure = StructureParser.online()
                                             .pdbIdentifier(dataPoint.getDataPointIdentifier().getPdbIdentifier())
                                             .parse();
        interactions.validateWithStructure(structure);
    }

    @Override
    public void enrichDataPoint(DataPoint<String> dataPoint) {

        logger.debug("enriching data point {} with interaction information", dataPoint);

        String pdbIdentifier = dataPoint.getDataPointIdentifier().getPdbIdentifier();
        String chainIdentifier = dataPoint.getDataPointIdentifier().getChainIdentifier();

        PlipGetRequest plipGetRequest = new PlipGetRequest(PLIP_REST_PROVIDER_URL, pdbIdentifier, chainIdentifier);

        Optional<InteractionContainer> optionalInteractions = plipGetRequest.queryInteractions();

        if (optionalInteractions.isPresent()) {
            InteractionContainer interactions = optionalInteractions.get();
            for (InteractionType activeInteraction : ACTIVE_INTERACTIONS) {
                logger.debug("enriching data point {} with interactions of type {}", dataPoint, activeInteraction);
                // validate interactions (necessary for intra-chain interactions)
                validateInteractions(interactions, dataPoint);
                interactions.getInteractions().stream()
                            .filter(interaction -> activeInteraction.getInteractionClass().equals(interaction.getClass()))
                            .forEach(interaction -> addInteractionItem(interaction, dataPoint));
            }
        } else {
            logger.warn("failed to enrich data point {} with intra-interaction data", dataPoint);
        }
    }

    @Override public String toString() {
        return "IntraChainInteractionEnricher{}";
    }
}
