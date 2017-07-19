package de.bioforscher.mmm.web.model;

import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.Size;
import java.util.UUID;

/**
 * @author fk
 */
public class MmmJob {

    @Id
    private String id;
    private String jobId;
    private String resultDirectory;
    private String address;

    @Size(min = 2, max = 30)
    private String email;

    private ItemsetMinerConfiguration<String> configuration;

    public MmmJob() {
        jobId = UUID.randomUUID().toString();
    }


    @Override public String toString() {
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
