package de.bioforscher.mmm.model.plip;

import de.bioforscher.singa.core.utility.Resources;
import de.bioforscher.singa.structure.parser.plip.InteractionContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public abstract class AbstractPlipRequest {

    protected static String PLIP_REST_PROVIDER_CREDENTIALS;

    static {
        // load PLIP credentials
        InputStream baseConfigurationResource = Resources.getResourceAsStream("plip_credentials.txt");
        if (baseConfigurationResource != null) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(baseConfigurationResource))) {
                List<String> fileContent = bufferedReader.lines().collect(Collectors.toList());
                if (fileContent.size() != 1) {
                    throw new RuntimeException("credentials file must contain exactly one line in the format user:password");
                }
                PLIP_REST_PROVIDER_CREDENTIALS = fileContent.get(0);
            } catch (IOException e) {
                throw new RuntimeException("failed to load PLIP credentials, please provide file plip_credentials.txt under class resources");
            }
        }
    }

    protected final String plipUrl;

    public AbstractPlipRequest(String plipUrl) {
        this.plipUrl = plipUrl;
    }

    public abstract Optional<InteractionContainer> queryInteractions();
}
