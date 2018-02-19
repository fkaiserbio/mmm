package bio.fkaiser.mmm.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author fk
 */
@Controller
public class ResultController {

    @RequestMapping("/result/{jobId}")
    public String submit(@PathVariable(value = "jobId") String jobId) {

        System.out.println("getting results for job " + jobId);

        return "result";
    }
}
