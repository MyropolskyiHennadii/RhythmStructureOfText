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
        log.info("===================Entrance parameters / ===================");
        log.info("Entrance from local address:" + request.getLocalAddr() +
                "; remote address: "+ request.getRemoteAddr());
        log.info("Remote host " + request.getRemoteHost());
        log.info("Server port {}", request.getServerPort());
        log.info("Local port {}", request.getLocalPort() +
                "; remote port " + request.getRemotePort());
        log.info("Server info:", request.getServletContext().getServerInfo());
        log.info("===================End entrance parameters /HOME ===================");
        model.addAttribute("setAttributePage", ViewNames.SET_ANALYSIS_ATTRIBUTES);
        return ViewNames.HOME;
    }

/*    @GetMapping("/")
    public String goHomePage(Model model, HttpServletRequest request) {
        log.info("===================Entrance parameters /===================");
        log.info("Entrance from local address:" + request.getLocalAddr() +
                "; remote address: "+ request.getRemoteAddr());
        log.info("Remote host " + request.getRemoteHost());
        log.info("Server port {}", request.getServerPort());
        log.info("Local port {}", request.getLocalPort() +
                "; remote port " + request.getRemotePort());
        log.info("Server info:", request.getServletContext().getServerInfo());
        log.info("===================End entrance parameters /===================");
        model.addAttribute("setAttributePage", ViewNames.SET_ANALYSIS_ATTRIBUTES);
        return ViewNames.HOME;
    }*/
}
