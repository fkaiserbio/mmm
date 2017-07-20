package de.bioforscher.mmm.web.model;

import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.annotation.Id;

/**
 * @author fk
 */
public class MmmJob implements Runnable {

    @Id
    private String id;

    private String jobId;
    private MmmJobStatus status;
    private String resultDirectory;
    private String address;
    @Email
    private String email;
    private ItemsetMinerConfiguration<String> configuration;

    public MmmJobStatus getStatus() {
        return status;
    }

    public void setStatus(MmmJobStatus status) {
        this.status = status;
    }

    @Override public String toString() {
        return "MmmJob{" +
               "jobId='" + jobId + '\'' +
               ", status=" + status +
               '}';
    }

    public ItemsetMinerConfiguration<String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ItemsetMinerConfiguration<String> configuration) {
        this.configuration = configuration;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getResultDirectory() {
        return resultDirectory;
    }

    public void setResultDirectory(String resultDirectory) {
        this.resultDirectory = resultDirectory;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void run() {
        System.out.println("started job" +
                           "");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("job finished");
        status = MmmJobStatus.FINISHED;
        System.out.println(this);
//        ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(configuration);
    }
}
