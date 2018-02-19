package bio.fkaiser.mmm.model.metrics.cohesion;

/**
 * * An exception if something goes wrong during the generation of candidates with the {@link VertexCandidateGenerator}.
 *
 * @author fk
 */
public class VertexCandidateGeneratorException extends RuntimeException {

    public VertexCandidateGeneratorException() {
    }

    public VertexCandidateGeneratorException(String message) {
        super(message);
    }

    public VertexCandidateGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
