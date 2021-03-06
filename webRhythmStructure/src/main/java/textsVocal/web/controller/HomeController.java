package textsVocal.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import textsVocal.config.CommonConstants;
import textsVocal.web.uploadingfiles.FileSystemStorageService;
import textsVocal.web.uploadingfiles.StorageProperties;
import textsVocal.web.utilsWeb.Mappings;
import textsVocal.web.utilsWeb.ViewNames;

import javax.servlet.http.HttpServletRequest;

/**
 * class for homepage control
 */
@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @GetMapping(Mappings.HOME)
    public String goHome(Model model, HttpServletRequest request) {
        log.info("===================Entrance parameters / ===================");
        log.info("Entrance from remote address: {}", request.getRemoteAddr());
        log.info("Remote port {}", request.getRemotePort());
        log.info("===================End entrance parameters /HOME ===================");

        //deleting old files
        ApplicationContext context = CommonConstants.getApplicationContext();
        FileSystemStorageService storageService = context.getBean(FileSystemStorageService.class);
        storageService.deleteAll();

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
