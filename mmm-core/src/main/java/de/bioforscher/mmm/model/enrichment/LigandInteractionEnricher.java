package de.bioforscher.mmm.model.enrichment;

import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.pliprestprovider.model.InteractionType;
import de.bioforscher.pliprestprovider.model.PlipInteraction;
import de.bioforscher.pliprestprovider.parser.PlipParser;
import de.bioforscher.pliprestprovider.rest.PlipPostRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author fk
 */
public class LigandInteractionEnricher extends AbstractInteractionEnricher {

    private static final Logger logger = LoggerFactory.getLogger(LigandInteractionEnricher.class);
    private static final String PLIP_REST_PROVIDER_URL = "https://biosciences.hs-mittweida.de/plip/interaction/calculate";

    @Override
    public void enrichDataPoint(DataPoint<String> dataPoint) {
        logger.info("enriching data point {} with ligand interaction information", dataPoint);

        try {
            // write PDB structure of data point to temporary file
            Path structureFilePath = Files.createTempFile("mmm_", "_" + dataPoint.getDataPointIdentifier().toString() + ".pdb");
            dataPoint.writeAsPdb(structureFilePath);

            // submit PLIP POST query
            PlipPostRequest plipPostRequest = new PlipPostRequest(PLIP_REST_PROVIDER_URL, PLIP_REST_PROVIDER_CREDENTIALS, structureFilePath);
            String resultXml = plipPostRequest.getResultXml();
            Map<InteractionType, List<PlipInteraction>> interactions = PlipParser.getInvolvedInteractionsFromXml(resultXml);

            for (InteractionType activeInteraction : ACTIVE_INTERACTIONS) {
                logger.debug("enriching data point {} with interactions of type {}", dataPoint, activeInteraction);
                if (interactions.containsKey(activeInteraction)) {
                    interactions.get(activeInteraction).forEach(interaction -> addInteractionItem(interaction, dataPoint));
                }
            }

            Files.delete(structureFilePath);

        } catch (IOException e) {
            logger.warn("failed to annotate ligand interactions for data point {}", dataPoint, e);
        }
    }
}
