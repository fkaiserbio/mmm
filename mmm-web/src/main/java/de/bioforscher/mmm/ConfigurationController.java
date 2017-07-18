package de.bioforscher.mmm;

import de.bioforscher.mmm.model.ItemsetComparatorType;
import de.bioforscher.mmm.model.configurations.ItemsetMinerConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.AdherenceMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.CohesionMetricConfiguration;
import de.bioforscher.mmm.model.configurations.metrics.ExtractionMetricConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fk
 */
@Controller
public class ConfigurationController {

    private List<ExtractionMetricConfiguration<String>> extractionMetricConfigurations = Stream.of(new CohesionMetricConfiguration<String>(), new AdherenceMetricConfiguration<String>()).collect(
            Collectors.toList());

    @ModelAttribute("itemsetComparatorTypes")
    public List<ItemsetComparatorType> populateItemsetComparatorTypes() {
        return Stream.of(ItemsetComparatorType.values()).collect(Collectors.toList());
    }

    @ModelAttribute("extractionMetricConfigurations")
    public List<ExtractionMetricConfiguration<String>> populateExtractionMetricConfigurations() {
        return extractionMetricConfigurations;
    }

    @GetMapping("/configuration")
    public String configurationForm(Model model) {
        ItemsetMinerConfiguration<String> itemsetMinerConfiguration = new ItemsetMinerConfiguration<>();
        model.addAttribute("configuration", itemsetMinerConfiguration);
        return "configuration";
    }

    @PostMapping("/configuration")
    public String configurationSubmit(@ModelAttribute("configuration") ItemsetMinerConfiguration<String> configuration, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "error";
        }
        return "result";
    }
}
