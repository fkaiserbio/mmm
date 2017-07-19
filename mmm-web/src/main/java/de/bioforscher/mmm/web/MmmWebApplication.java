package de.bioforscher.mmm.web;

import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.web.model.MmmJob;
import de.bioforscher.mmm.web.model.MmmJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MmmWebApplication implements CommandLineRunner {

    @Autowired
    private MmmJobRepository jobRepository;

    public static void main(String[] args) {


        SpringApplication.run(MmmWebApplication.class, args);
    }


    @Override public void run(String... strings) throws Exception {

        jobRepository.deleteAll();

        MmmJob job = new MmmJob();
        job.setConfiguration(new ItemsetMinerConfiguration<>());
        System.out.println(job);
        // save a test job
        jobRepository.save(job);

        MmmJob sameJob = jobRepository.findByJobId(job.getJobId());
        System.out.println(sameJob);
    }
}
