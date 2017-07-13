package de.bioforscher.mmm.model.enrichment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.pliprestprovider.model.InteractionType;
import de.bioforscher.pliprestprovider.model.PlipInteraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author fk
 */
@JsonTypeName("INTRA_CHAIN_INTERACTION")
public class IntraChainInteractionEnricher extends AbstractInteractionEnricher {

    private static final Logger logger = LoggerFactory.getLogger(IntraChainInteractionEnricher.class);
    private static final String PLIP_REST_PROVIDER_URL = "https://biosciences.hs-mittweida.de/plip/interaction/";

    @Override
    public void enrichDataPoint(DataPoint<String> dataPoint) {

        logger.debug("enriching data point {} with interaction information", dataPoint);

        String pdbIdentifier = dataPoint.getDataPointIdentifier().getPdbIdentifier();
        String chainIdentifier = dataPoint.getDataPointIdentifier().getChainIdentifier();

        Optional<Map<InteractionType, List<PlipInteraction>>> optionalInteractions = queryInteractions(pdbIdentifier, chainIdentifier);

        if (optionalInteractions.isPresent()) {
            Map<InteractionType, List<PlipInteraction>> interactions = optionalInteractions.get();
            for (InteractionType activeInteraction : ACTIVE_INTERACTIONS) {
                logger.debug("enriching data point {} with interactions of type {}", dataPoint, activeInteraction);
                if (interactions.containsKey(activeInteraction)) {
                    interactions.get(activeInteraction).forEach(interaction -> addInteractionItem(interaction, dataPoint));
                }
            }
        }
    }

    @Override public String toString() {
        return "IntraChainInteractionEnricher{}";
    }

    private Optional<Map<InteractionType, List<PlipInteraction>>> queryInteractions(String pdbIdentifier, String chainIdentifier) {
        try {
            // connect to the PLIP REST API and obtain interaction data
            URL url = new URL(PLIP_REST_PROVIDER_URL + pdbIdentifier + "/" + chainIdentifier);
            logger.debug("querying PLIP REST service: {}", url);
            String encoding = new sun.misc.BASE64Encoder().encode(PLIP_REST_PROVIDER_CREDENTIALS.getBytes());
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            connection.connect();
            try (InputStream inputStream = connection.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
                TypeReference<Map<InteractionType, List<PlipInteraction>>> typeReference = new TypeReference<Map<InteractionType, List<PlipInteraction>>>() {
                };
                return Optional.of(mapper.readValue(inputStream, typeReference));
            }
        } catch (IOException e) {
            logger.warn("failed to obtain PLIP results from server for {}_{}", pdbIdentifier, chainIdentifier, e);
        }
        return Optional.empty();
    }
}
