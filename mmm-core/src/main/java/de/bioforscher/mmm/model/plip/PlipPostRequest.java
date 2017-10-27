package de.bioforscher.mmm.model.plip;

import de.bioforscher.singa.structure.parser.plip.InteractionContainer;
import de.bioforscher.singa.structure.parser.plip.PlipParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

/**
 * @author fk
 */
public class PlipPostRequest extends AbstractPlipRequest {

    private static final Logger logger = LoggerFactory.getLogger(PlipPostRequest.class);
    private static final String CHARSET = "UTF-8";

    private final String pdbIdentifier;
    private final Path pdbFilePath;

    public PlipPostRequest(String plipUrl, String pdbIdentifier, Path pdbFilePath) {
        super(plipUrl);
        this.pdbIdentifier = pdbIdentifier;
        this.pdbFilePath = pdbFilePath;
        logger.info("creating PLIP POST for PDB file {}", pdbFilePath);
    }

    private String doPlipPost() throws IOException {
        String boundary = Long.toHexString(System.currentTimeMillis());
        String crlf = "\r\n";

        String login = Base64.getEncoder().encodeToString(PLIP_REST_PROVIDER_CREDENTIALS.getBytes());
        URLConnection connection = new URL(plipUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Authorization", "Basic " + login);

        OutputStream output = connection.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, CHARSET), true);
        writer.append("--").append(boundary).append(crlf);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(pdbFilePath.getFileName().toString()).append("\"").append(crlf);
        writer.append("Content-Type: text/plain; charset=" + CHARSET).append(crlf); // Text file itself must be saved in this charset!
        writer.append(crlf).flush();
        Files.copy(pdbFilePath, output);
        output.flush();
        writer.append(crlf).flush();
        writer.append("--").append(boundary).append("--").append(crlf).flush();

        int responseCode = ((HttpURLConnection) connection).getResponseCode();
        logger.info("PLIP REST service response code is " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String outputContent;
        StringBuilder response = new StringBuilder();

        while ((outputContent = in.readLine()) != null) {
            response.append(outputContent);
        }
        in.close();

        return response.toString();
    }

    @Override
    public Optional<InteractionContainer> queryInteractions() {

        try {
            String xml = doPlipPost();
            return Optional.of(PlipParser.parse(pdbIdentifier, xml));
        } catch (IOException e) {
            logger.warn("failed to obtain PLIP results for {} and file {}", pdbIdentifier, pdbFilePath, e);
        }
        return Optional.empty();
    }
}
