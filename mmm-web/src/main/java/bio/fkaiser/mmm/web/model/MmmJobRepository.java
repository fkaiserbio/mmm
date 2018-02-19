package bio.fkaiser.mmm.web.model;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author fk
 */
public interface MmmJobRepository extends MongoRepository<MmmJob, String> {

    MmmJob findByJobId(String jobId);
}