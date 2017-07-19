package de.bioforscher.mmm.web;

import de.bioforscher.mmm.web.model.MmmJob;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author fk
 */
@Controller
public class SubmitController {

    @PostMapping("/submit")
    public String configurationSubmit(@ModelAttribute("job") MmmJob job, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "error";
        }
        return "result";
    }
}
