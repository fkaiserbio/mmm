package de.bioforscher.mmm.classify;

import de.bioforscher.singa.structure.algorithms.superimposition.consensus.ConsensusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author fk
 */
public class DecoyGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DecoyGenerator.class);

    private final ConsensusContainer rootConsensusObservation;
    private final Path chainListPath;

    public DecoyGenerator(ConsensusContainer rootConsensusObservation, Path chainListPath) {
        this.rootConsensusObservation = rootConsensusObservation;
        this.chainListPath = chainListPath;
    }
}
