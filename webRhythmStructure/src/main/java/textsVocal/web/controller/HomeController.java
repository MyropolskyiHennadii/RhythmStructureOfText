package textsVocal.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import textsVocal.web.utilsWeb.Mappings;
import textsVocal.web.utilsWeb.ViewNames;

import javax.servlet.http.HttpServletRequest;

/**
 * class for homepage control
 */
@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(SetAnalysisAttributesController.class);

    @GetMapping(Mappings.HOME)
    public String goHome(Model model, HttpServletRequest request) {
        log.info("Entrance from {}", request.getRequestURL().toString());
        model.addAttribute("setAttributePage", ViewNames.SET_ANALYSIS_ATTRIBUTES);
        return ViewNames.HOME;
    }

}
