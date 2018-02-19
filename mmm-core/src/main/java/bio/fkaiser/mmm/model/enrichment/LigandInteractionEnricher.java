package bio.fkaiser.mmm.model.enrichment;

import bio.fkaiser.mmm.model.DataPoint;
import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.plip.PlipPostRequest;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.singa.structure.model.families.NucleotideFamily;
import de.bioforscher.singa.structure.model.families.StructuralFamily;
import de.bioforscher.singa.structure.model.interfaces.Nucleotide;
import de.bioforscher.singa.structure.parser.plip.InteractionContainer;
import de.bioforscher.singa.structure.parser.plip.InteractionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enriches {@link DataPoint}s that contain ligands with interaction information predicted by the Protein-Ligand Interaction Profiler (PLIP). Interactions are abstracted as pseudoatoms defined as the
 * midpoint between interacting atoms.
 *
 * @author fk
 */
@JsonTypeName("LIGAND_INTERACTION")
public class LigandInteractionEnricher extends AbstractInteractionEnricher {

    private static final Logger logger = LoggerFactory.getLogger(LigandInteractionEnricher.class);
    private static final String PLIP_REST_PROVIDER_URL_PROTEIN = "https://biosciences.hs-mittweida.de/plip/interaction/calculate/nucleotide";
    private static final String PLIP_REST_PROVIDER_URL_NUCLEOTIDE = "https://biosciences.hs-mittweida.de/plip/interaction/calculate/nucleotide";

    /**
     * Tries to determine the type of the given {@link DataPoint}.
     *
     * @param dataPoint The {@link DataPoint} for which the type should be determined.
     * @return True if the {@link DataPoint} is entirely composed of {@link Nucleotide}s.
     */
    private static boolean isNucleotideDataPoint(DataPoint<String> dataPoint) {
        Set<StructuralFamily<?>> familyTypes = dataPoint.getItems().stream()
                                                        .map(Item::getLeafSubstructure)
                                                        .filter(Optional::isPresent)
                                                        .map(Optional::get)
                                                        .map(leafSubstructure -> (StructuralFamily<?>) leafSubstructure.getFamily())
                                                        .distinct()
                                                        .collect(Collectors.toSet());
        return familyTypes.stream()
                          .allMatch(type -> type instanceof NucleotideFamily);
    }

    @Override
    public String toString() {
        return "LigandInteractionEnricher{}";
    }

    @Override
    public void enrichDataPoint(DataPoint<String> dataPoint) {
        logger.debug("enriching data point {} with ligand interaction information", dataPoint);

        String plipUrl;
        if (isNucleotideDataPoint(dataPoint)) {
            logger.debug("data point {} is of type nucleotide", dataPoint);
            plipUrl = PLIP_REST_PROVIDER_URL_NUCLEOTIDE;
        } else {
            plipUrl = PLIP_REST_PROVIDER_URL_PROTEIN;
        }

        try {
            // write PDB structure of data point to temporary file
            Path structureFilePath = Files.createTempFile("mmm_", "_" + dataPoint.getDataPointIdentifier().toString() + ".pdb");
            dataPoint.writeAsPdb(structureFilePath);

            // submit PLIP POST query
            PlipPostRequest plipPostRequest = new PlipPostRequest(plipUrl, dataPoint.getDataPointIdentifier().getPdbIdentifier(), structureFilePath);

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
