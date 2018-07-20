package bio.fkaiser.mmm.model.mapping.rules;

import bio.fkaiser.mmm.model.DataPoint;
import bio.fkaiser.mmm.model.Item;
import bio.fkaiser.mmm.model.mapping.MappingRule;
import bio.fkaiser.mmm.model.plip.PlipGetRequest;
import bio.fkaiser.mmm.model.plip.PlipPostRequest;
import bio.singa.structure.model.interfaces.Chain;
import bio.singa.structure.model.interfaces.LeafSubstructure;
import bio.singa.structure.model.interfaces.Structure;
import bio.singa.structure.parser.pdb.structures.StructureParser;
import bio.singa.structure.parser.plip.InteractionContainer;
import bio.singa.structure.parser.plip.PlipShellGenerator;
import bio.singa.structure.parser.plip.PlipShellGenerator.InteractionShell;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author fk
 */
@JsonTypeName("INTERACTION_SHELL")
@JsonIgnoreProperties({"current-interaction-shells"})
public class InteractionShellMappingRule implements MappingRule<String> {

    private static final Logger logger = LoggerFactory.getLogger(InteractionShellMappingRule.class);
    private static final String PLIP_REST_PROVIDER_URL_INTRA_CHAIN = "https://biosciences.hs-mittweida.de/plip/interaction/plain";
    private static final String PLIP_REST_PROVIDER_URL_LIGAND = "https://biosciences.hs-mittweida.de/plip/interaction/calculate/protein";

    @JsonProperty("ligand-label")
    private String ligandLabel;
    @JsonProperty("current-interaction-shells")
    private Map<InteractionShell, List<LeafSubstructure<?>>> currentInteractionShells;

    public InteractionShellMappingRule(String ligandLabel) {
        this.ligandLabel = ligandLabel;
    }

    public InteractionShellMappingRule() {
    }

    public Map<InteractionShell, List<LeafSubstructure<?>>> getCurrentInteractionShells() {
        return currentInteractionShells;
    }

    public void updateCurrentInteractionShells(DataPoint<?> currentDataPoint) throws IOException {
        String pdbIdentifier = currentDataPoint.getDataPointIdentifier().getPdbIdentifier();
        String chainIdentifier = currentDataPoint.getDataPointIdentifier().getChainIdentifier();

        logger.info("obtaining parent structure of data point {}", currentDataPoint);
        Structure structure = StructureParser.pdb()
                                             .pdbIdentifier(pdbIdentifier)
                                             .parse();
        Chain chain = structure.getFirstChain();
        Optional<LeafSubstructure<?>> optionalLigand = selectLigand(chain);

        if (optionalLigand.isPresent()) {

            Optional<InteractionContainer> intraChainInteractions = new PlipGetRequest(PLIP_REST_PROVIDER_URL_INTRA_CHAIN, pdbIdentifier, chainIdentifier).queryInteractions();

            // write PDB structure of data point to temporary file
            Path structureFilePath = Files.createTempFile("mmm_", "_" + currentDataPoint.getDataPointIdentifier().toString() + ".pdb");
            currentDataPoint.writeAsPdb(structureFilePath);
            Optional<InteractionContainer> ligandInteractions = new PlipPostRequest(PLIP_REST_PROVIDER_URL_LIGAND, pdbIdentifier, structureFilePath).queryInteractions();

            if (intraChainInteractions.isPresent() && ligandInteractions.isPresent()) {
                currentInteractionShells = PlipShellGenerator.getInteractionShellsForLigand(chain, optionalLigand.get(), intraChainInteractions.get(), ligandInteractions.get()).getShells();
            } else {
                logger.warn("failed to calculate interaction shells for data point {}", currentDataPoint);
                currentInteractionShells = null;
            }

            // delete temporary structure
            Files.delete(structureFilePath);
        } else {
            logger.warn("failed to calculate interaction shells for data point {}, no matching ligand with label {} found", currentDataPoint, ligandLabel);
            currentInteractionShells = null;
        }
    }

    private Optional<LeafSubstructure<?>> selectLigand(Chain chain) {
        return chain.getAllLeafSubstructures().stream()
                    .filter(leafSubstructure -> leafSubstructure.getFamily().getThreeLetterCode().equals(ligandLabel))
                    .findFirst();
    }

    @Override
    public Optional<Item<String>> mapItem(Item<String> item) {
        LeafSubstructure<?> leafSubstructure = item.getLeafSubstructure()
                                                   .orElseThrow(() -> new UnsupportedOperationException("interaction shells can only be computed for structure-derived items"));
        if (currentInteractionShells != null) {
            for (Map.Entry<InteractionShell, List<LeafSubstructure<?>>> interactionShellEntry : currentInteractionShells.entrySet()) {
                if (interactionShellEntry.getValue().contains(leafSubstructure)) {
                    InteractionShell interactionShell = interactionShellEntry.getKey();
                    logger.info("item {} is shell {}", item, interactionShell);
                    item.setLabel(item.getLabel() + ".s" + interactionShell.ordinal());
                }
            }
        }
        return Optional.of(item);
    }

    @Override
    public Item<String> apply(Item<String> item) {
        return mapItem(item).orElse(null);
    }
}
