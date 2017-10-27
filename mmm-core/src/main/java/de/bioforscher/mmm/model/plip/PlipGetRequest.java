package de.bioforscher.mmm.model.plip;

import de.bioforscher.singa.structure.parser.plip.InteractionContainer;
import de.bioforscher.singa.structure.parser.plip.PlipParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Optional;

/**
 * @author fk
 */
public class PlipGetRequest extends AbstractPlipRequest {

    private static final Logger logger = LoggerFactory.getLogger(PlipGetRequest.class);

    private final String pdbIdentifier;
    private final String chainIdentifier;

    public PlipGetRequest(String plipUrl, String pdbIdentifier, String chainIdentifier) {
        super(plipUrl);
        this.pdbIdentifier = pdbIdentifier;
        this.chainIdentifier = chainIdentifier;
    }

    /**
     * Queries the PLIP REST server for interactions for a given PDB chain.
     *
     * @return Map of interactions or {@link Optional#empty()} if no interactions were found or the query failed.
     */
    @Override
    public Optional<InteractionContainer> queryInteractions() {
        try {
            // connect to the PLIP REST API and obtain interaction data
            URL url = new URL(plipUrl + "/" + pdbIdentifier + "/" + chainIdentifier);
            logger.debug("querying PLIP REST service: {}", url);
            String encoding = Base64.getEncoder().encodeToString(PLIP_REST_PROVIDER_CREDENTIALS.getBytes());
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            connection.connect();
            try (InputStream inputStream = connection.getInputStream()) {
                return Optional.of(PlipParser.parse(pdbIdentifier, inputStream));
            }
        } catch (IOException e) {
            logger.warn("failed to obtain PLIP results from server {} for {}_{}", plipUrl, pdbIdentifier, chainIdentifier, e);
        }
        return Optional.empty();
    }
}
