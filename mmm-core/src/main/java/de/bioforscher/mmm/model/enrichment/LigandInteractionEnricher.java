package de.bioforscher.mmm.model.enrichment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.plip.PlipPostRequest;
import de.bioforscher.singa.chemistry.parser.plip.InteractionContainer;
import de.bioforscher.singa.chemistry.parser.plip.InteractionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Enriches {@link DataPoint}s that contain ligands with interaction information predicted by the Protein-Ligand Interaction Profiler (PLIP). Interactions are abstracted as pseudoatoms defined as the
 * midpoint between interacting atoms.
 *
 * @author fk
 */
@JsonTypeName("LIGAND_INTERACTION")
public class LigandInteractionEnricher extends AbstractInteractionEnricher {

    private static final Logger logger = LoggerFactory.getLogger(LigandInteractionEnricher.class);
    // TODO adapt URL for proteins or nucleotides
    private static final String PLIP_REST_PROVIDER_URL = "https://biosciences.hs-mittweida.de/plip/interaction/calculate/nucleotide";
//    private static final String PLIP_REST_PROVIDER_URL = "https://biosciences.hs-mittweida.de/plip/interaction/calculate/nucleotide";

    @Override
    public String toString() {
        return "LigandInteractionEnricher{}";
    }

    @Override
    public void enrichDataPoint(DataPoint<String> dataPoint) {
        logger.debug("enriching data point {} with ligand interaction information", dataPoint);

        try {
            // write PDB structure of data point to temporary file
            Path structureFilePath = Files.createTempFile("mmm_", "_" + dataPoint.getDataPointIdentifier().toString() + ".pdb");
            dataPoint.writeAsPdb(structureFilePath);

            // submit PLIP POST query
            PlipPostRequest plipPostRequest = new PlipPostRequest(PLIP_REST_PROVIDER_URL, dataPoint.getDataPointIdentifier().getPdbIdentifier(), structureFilePath);

            Optional<InteractionContainer> interactions = plipPostRequest.queryInteractions();
            if (interactions.isPresent()) {
                for (InteractionType activeInteraction : ACTIVE_INTERACTIONS) {
                    logger.debug("enriching data point {} with interactions of type {}", dataPoint, activeInteraction);
                    interactions.get().getInteractions().stream()
                                .filter(interaction -> activeInteraction.getInteractionClass().equals(interaction.getClass()))
                                .forEach(interaction -> addInteractionItem(interaction, dataPoint));
                }
            } else {
                logger.warn("failed to enrich data point {} with ligand interaction data", dataPoint);
            }

            // delete temporary structure
            Files.delete(structureFilePath);

        } catch (IOException e) {
            logger.warn("failed to annotate ligand interactions for data point {}", dataPoint, e);
        }
    }
}
