package bio.fkaiser.mmm.classify;

import bio.fkaiser.mmm.model.configurations.ItemsetMinerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class BatchFingerprintMiner {

    private static final Logger logger = LoggerFactory.getLogger(BatchFingerprintMiner.class);

    public static void main(String[] args) throws URISyntaxException, IOException, FingerprintMinerException {
        logger.info("loading base configuration");
        URL baseConfigurationResource = Thread.currentThread().getContextClassLoader()
                                              .getResource("base_configuration.json");
        if (baseConfigurationResource == null) {
            throw new FingerprintMinerException("failed to load base configuration");
        }

        logger.info("loading decoy chain list");
        URL decoyChainListResource = Thread.currentThread().getContextClassLoader()
                                           .getResource("nrPDB_chains_BLAST_10e80");
        if (decoyChainListResource == null) {
            throw new FingerprintMinerException("failed to load decoy chain list");
        }

        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = ItemsetMinerConfiguration.from(Paths.get(baseConfigurationResource.toURI()));

        // use all given inputs in directory
        String inputLocation = args[0];
        String blackListsLocation = args[1];
        String outputLocation = args[2];
        Path inputPath = Paths.get(inputLocation);
        Path blackListsPath = Paths.get(blackListsLocation);
        List<Path> inputLists = Files.list(inputPath).collect(Collectors.toList());
        for (Path inputList : inputLists) {
            logger.info("mining family {}", inputList);
            if (inputList.toFile().isFile()) {
                itemsetMinerConfiguration.setInputListLocation(inputList.toString());
            } else if (inputList.toFile().isDirectory()) {
                itemsetMinerConfiguration.setInputDirectoryLocation(inputList.toString());
            }
            Path familyOutputLocation = Paths.get(outputLocation).resolve(inputList.getFileName()).resolve("itemset-miner");
            itemsetMinerConfiguration.setOutputLocation(familyOutputLocation.toString());
            // assemble path to blacklist
            Path blackList = blackListsPath.resolve(inputList.getFileName() + "_blacklist");
            // run fingerprint miner
            try {
                FingerprintMinerConfiguration fingerprintMinerConfiguration = new FingerprintMinerConfiguration();
                fingerprintMinerConfiguration.setItemsetMinerConfiguration(itemsetMinerConfiguration);
                fingerprintMinerConfiguration.setInputListLocation(inputList.toString());
                fingerprintMinerConfiguration.setBlacklistLocation(blackList.toString());
                fingerprintMinerConfiguration.setDecoyListLocation(Paths.get(decoyChainListResource.toURI()).toString());
                fingerprintMinerConfiguration.setDecoyRmsdCutoff(1.0);

                System.out.println(fingerprintMinerConfiguration.toJson());

                new FingerprintMiner(fingerprintMinerConfiguration);
            } catch (IOException | URISyntaxException | FingerprintMinerException e) {
                logger.warn("failed to mine family {}", inputList, e);
            }
        }
    }
}
