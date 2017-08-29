package de.bioforscher.mmm.model.mapping.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.bioforscher.mmm.model.DataPoint;
import de.bioforscher.mmm.model.Item;
import de.bioforscher.mmm.model.mapping.MappingRule;
import de.bioforscher.mmm.model.plip.PlipGetRequest;
import de.bioforscher.mmm.model.plip.PlipPostRequest;
import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.chemistry.parser.plip.InteractionContainer;
import de.bioforscher.singa.chemistry.parser.plip.PlipShellGenerator;
import de.bioforscher.singa.chemistry.parser.plip.PlipShellGenerator.InteractionShell;
import de.bioforscher.singa.chemistry.physical.branches.Chain;
import de.bioforscher.singa.chemistry.physical.leaves.LeafSubstructure;
import de.bioforscher.singa.chemistry.physical.model.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private Map<InteractionShell, Set<LeafSubstructure<?, ?>>> currentInteractionShells;

    public InteractionShellMappingRule(String ligandLabel) {
        this.ligandLabel = ligandLabel;
    }

    public InteractionShellMappingRule() {
    }

    public Map<InteractionShell, Set<LeafSubstructure<?, ?>>> getCurrentInteractionShells() {
        return currentInteractionShells;
    }

    public void updateCurrentInteractionShells(DataPoint<?> currentDataPoint) throws IOException {
        String pdbIdentifier = currentDataPoint.getDataPointIdentifier().getPdbIdentifier();
        String chainIdentifier = currentDataPoint.getDataPointIdentifier().getChainIdentifier();

        logger.info("obtaining parent structure of data point {}", currentDataPoint);
        Structure structure = StructureParser.online()
                                             .pdbIdentifier(pdbIdentifier)
                                             .parse();
        Chain chain = structure.getFirstChain().orElseThrow(() -> new RuntimeException("parent structure does not contain any chains"));
        Optional<LeafSubstructure<?, ?>> optionalLigand = selectLigand(chain);

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
        } else {
            logger.warn("failed to calculate interaction shells for data point {}, no matching ligand with label {} found", currentDataPoint, ligandLabel);
            currentInteractionShells = null;
        }
    }

    private Optional<LeafSubstructure<?, ?>> selectLigand(Chain chain) {
        return chain.getLeafSubstructures().stream()
                    .filter(leafSubstructure -> leafSubstructure.getFamily().getThreeLetterCode().equals(ligandLabel))
                    .findFirst();
    }

    @Override
    public Optional<Item<String>> mapItem(Item<String> item) {
        LeafSubstructure<?, ?> leafSubstructure = item.getLeafSubstructure()
                                                      .orElseThrow(() -> new UnsupportedOperationException("interaction shells can only be computed for structure-derived items"));
        if (currentInteractionShells != null) {
            for (Map.Entry<InteractionShell, Set<LeafSubstructure<?, ?>>> interactionShellEntry : currentInteractionShells.entrySet()) {
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
