package textsVocal.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import textsVocal.web.utilsWeb.Mappings;
import textsVocal.web.utilsWeb.ViewNames;

@Controller
public class HomeController {

    @GetMapping(Mappings.HOME)
    public String goHome(Model model) {
        model.addAttribute("setAttributePage", ViewNames.SET_ANALYSIS_ATTRIBUTES);
        return ViewNames.HOME;
    }

}
