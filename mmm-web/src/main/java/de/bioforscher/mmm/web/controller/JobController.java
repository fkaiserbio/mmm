package de.bioforscher.mmm.web.controller;

import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.web.model.MmmJob;
import de.bioforscher.mmm.web.model.MmmJobRepository;
import de.bioforscher.mmm.web.model.MmmJobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fk
 */
@RestController
@RequestMapping(value = "/rest/")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Autowired
    private MmmJobRepository jobRepository;

//    @ModelAttribute("itemsetComparatorTypes")
//    public List<ItemsetComparatorType> populateItemsetComparatorTypes() {
//        return Stream.of(ItemsetComparatorType.values()).collect(Collectors.toList());
//    }

//    @GetMapping("/configuration/input")
//    public String configurationForm(Model model) {
//        // create a new configuration object
//        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
//        // create a new job object
//        MmmJob mmmJob = new MmmJob();
//        mmmJob.setConfiguration(itemsetMinerConfiguration);
//        model.addAttribute("job", mmmJob);
//        logger.info("created job {}", mmmJob);
//        return "configuration/input";
//    }


    @GetMapping(value = "/createJob")
    @ResponseBody
    public MmmJob createJob() {
        // create a new configuration object
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        // create a new job object
        MmmJob mmmJob = new MmmJob();
        mmmJob.setConfiguration(itemsetMinerConfiguration);
        return mmmJob;
    }

//    @PostMapping("/result")
//    public String result(@Valid @ModelAttribute("job") MmmJob job, BindingResult result) {
//        System.out.println(job);
//        return "result";
//    }

    @PostMapping("/submitJob")
    @ResponseBody
    public MmmJob submitJob(@RequestBody MmmJob job) {
        System.out.println("received job" + job);
        // assign unique ID to job
        job.setJobId(UUID.randomUUID().toString());
        // update status
        job.setStatus(MmmJobStatus.QUEUED);
        jobRepository.save(job);
        executorService.submit(new MmmJobRunner(job));
        return job;
        //        if (result.hasErrors()) {
//            List<String> errors = result.getAllErrors().stream()
//                                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
//                                        .collect(Collectors.toList());
//            return "/result";
//        } else {
//            return "/result";
//        }
    }

    @RequestMapping("/getJob/{jobId}")
    @ResponseBody
    public MmmJob getJob(@PathVariable(value = "jobId") String jobId) {


        MmmJob job = jobRepository.findByJobId(jobId);

        System.out.println("getting results for job " + job);
        return job;
    }

    public class MmmJobRunner implements Runnable {

        private MmmJob job;

        public MmmJobRunner(MmmJob job) {
            this.job = job;
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
            job.setStatus(MmmJobStatus.FINISHED);
            jobRepository.save(job);
        }
    }

}
