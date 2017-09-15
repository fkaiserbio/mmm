package de.bioforscher.mmm.classify.model;

import de.bioforscher.mmm.ItemsetMinerRunner;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureWriter;
import de.bioforscher.singa.chemistry.physical.model.Structure;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * @author fk
 */
public class ItemsetLibraryTest {

    @Test
    @Ignore
    public void shouldSerializeToJson() throws IOException, URISyntaxException {
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = ItemsetMinerConfiguration.from(Paths.get("/home/fkaiser/Workspace/IdeaProjects/mmm/mmm-core/src/test/resources/mmm_config.json"));
        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(itemsetMinerConfiguration);

//        // collect top consensus itemsets
//        List<Itemset<String>> itemsets = itemsetMinerRunner.getItemsetMiner().getTotalExtractedItemsets().values().stream()
//                                                           .flatMap(Collection::stream)
//                                                           .collect(Collectors.toList());

        ItemsetLibrary itemsetLibrary = ItemsetLibrary.of(itemsetMinerRunner.getItemsetMiner().getTotalClusteredItemsets(), 3, 10);

        for (ItemsetLibraryEntry entry : itemsetLibrary.getEntries()) {

            Structure structure = StructureParser.local()
                                                 .inputStream(new ByteArrayInputStream(entry.getPdbLines().getBytes()))
                                                 .parse();

            StructureWriter.writeStructure(structure, Paths.get("/tmp/library/" + entry.getIdentifier() + ".pdb"));
        }

        itemsetLibrary.writeToPath(Paths.get("/tmp/library.gz"));

        ItemsetLibrary.readFromPath(Paths.get("/tmp/library.gz"));
    }

}