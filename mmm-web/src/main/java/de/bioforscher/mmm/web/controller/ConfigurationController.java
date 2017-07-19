package de.bioforscher.mmm.web.controller;

import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.web.model.MmmJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fk
 */
@Controller
public class ConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    @ModelAttribute("itemsetComparatorTypes")
    public List<ItemsetComparatorType> populateItemsetComparatorTypes() {
        return Stream.of(ItemsetComparatorType.values()).collect(Collectors.toList());
    }

    @GetMapping("/configuration/input")
    public String configurationForm(Model model) {
        // create a new configuration object
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        // create a new job object
        MmmJob mmmJob = new MmmJob();
        mmmJob.setConfiguration(itemsetMinerConfiguration);
        model.addAttribute("job", mmmJob);
        logger.info("created job {}", mmmJob);
        return "configuration/input";
    }


    @PostMapping("/configuration/input")
    public String configurationFormInput(@Valid @ModelAttribute("job") MmmJob job, BindingResult bindingResult, @RequestParam("file") MultipartFile file) {
        if (bindingResult.hasErrors()) {
            return "configuration/input";
        }
        return "configuration/extraction";
    }
}
