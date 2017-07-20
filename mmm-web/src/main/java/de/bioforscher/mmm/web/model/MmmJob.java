package de.bioforscher.mmm.web.model;

import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.annotation.Id;

/**
 * @author fk
 */
public class MmmJob {

    @Id
    private String id;
    private String jobId;
    private String resultDirectory;
    private String address;
    @Email
    private String email;
    private ItemsetMinerConfiguration<String> configuration;

    @Override
    public String toString() {
        return "MmmJob{" +
               "jobId='" + jobId + '\'' +
               ", configuration=" + configuration +
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
}
