package de.bioforscher.mmm.web.controller;

import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.web.model.MmmJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @author fk
 */
@RestController
@RequestMapping(value = "/rest/")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

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
    public MmmJob submit() {
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
    public MmmJob submit(@RequestBody MmmJob job) {
        System.out.println("received job" + job);
        job.setJobId(UUID.randomUUID().toString());
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
    public MmmJob submit(@PathVariable(value = "jobId") String jobId) {

        System.out.println("getting results for job " + jobId);

        return new MmmJob();
    }
}
