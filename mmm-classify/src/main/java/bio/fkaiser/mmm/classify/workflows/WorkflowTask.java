package bio.fkaiser.mmm.classify.workflows;

/**
 * @author fk
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * An abstract class that should be extended by all workflow steps to provide a README alongside with the results in the output directory.
 * <p>
 * Created by fkaiser on 5/16/17.
 */
public abstract class WorkflowTask {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowTask.class.getName());
    protected final Path outputPath;

    public WorkflowTask(Path outputPath) {
        this.outputPath = outputPath;
        try {
            writeReadme(outputPath);
        } catch (IOException e) {
            logger.warn("failed to write README for {} to {}", getClass().getSimpleName(), outputPath, e);
        }
    }

    /**
     * Writes the associated README to the specified {@link Path}.
     *
     * @param outputPath The {@link Path} to which the README should be written.
     * @throws IOException
     */
    protected void writeReadme(Path outputPath) throws IOException {
        InputStream readmeResource = Thread.currentThread()
                                           .getContextClassLoader()
                                           .getResourceAsStream(getClass().getSimpleName() + "_README.md");
        Files.createDirectories(outputPath);
        String username = System.getProperty("user.name");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String date = formatter.format(now);

        if (readmeResource != null) {
            String readmeContent = new BufferedReader(new InputStreamReader(readmeResource)).lines()
                                                                                            .map(line -> {
                                                                                                if (line.contains("%DATE%") && line.contains("%USER%")) {
                                                                                                    return line.replace("%DATE%", date).replace("%USER%", username);
                                                                                                }
                                                                                                return line;
                                                                                            })
                                                                                            .collect(Collectors.joining("\n"));
            Files.write(outputPath.resolve("README.md"), readmeContent.getBytes());
        }
    }
}