package textsVocal.web.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import textsVocal.config.CommonConstants;
import textsVocal.structure.AnalyserPortionOfText;
import textsVocal.structure.BuildingPortion;
import textsVocal.structure.TextPortionForRhythm;
import textsVocal.web.uploadingfiles.FileSystemStorageService;
import textsVocal.web.uploadingfiles.StorageFileNotFoundException;
import textsVocal.web.uploadingfiles.StorageService;
import textsVocal.web.utilsWeb.Mappings;
import textsVocal.web.utilsWeb.ViewNames;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * class for "setAnalysisAttributes" page control
 */
@Controller
public class SetAnalysisAttributesController {

    private final StorageService storageService;
    private static final Logger log = LoggerFactory.getLogger(SetAnalysisAttributesController.class);

    @Autowired
    public SetAnalysisAttributesController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping(Mappings.SET_ANALYSIS_ATTRIBUTES)
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(SetAnalysisAttributesController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));
        return ViewNames.SET_ANALYSIS_ATTRIBUTES;
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = null;
        if (filename.substring(0, 3).equals("out")) {
            file = storageService.loadAsResourceOutput(filename);
        } else {
            file = storageService.loadAsResource(filename);
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }


    @PostMapping(Mappings.SET_ANALYSIS_ATTRIBUTES)
    public String processingSetTextAttributes(@RequestParam("file") MultipartFile file,
                                              RedirectAttributes redirectAttributes,
                                              boolean checkBoxThisIsProse,
                                              String textFromForm,
                                              String portionsSepataror,
                                              HttpServletRequest request,
                                              Model model) throws IOException {

        ApplicationContext context = CommonConstants.getApplicationContext();
        CommonConstants constants = context.getBean(CommonConstants.class);
        constants.setPortionSeparator(portionsSepataror);
        constants.setThisIsVerse(!checkBoxThisIsProse);
        constants.setThisIsWebApp(true);
        constants.setTextFromWebForm("" + textFromForm.trim());

        Locale locale = context.getBean(LocaleResolver.class).resolveLocale(request);
        CommonConstants.setWebLocale(locale);

        ResourceBundle messages = CommonConstants.getResourceBundle();
        if (textFromForm.trim().isEmpty() && file.isEmpty()) {
            model.addAttribute("mistakeMessage", messages.getString("web.mistakeBySettingsTextAttributes"));
            return ViewNames.SET_ANALYSIS_ATTRIBUTES;
        } else {
            model.addAttribute("mistakeMessage", "");
        }

        if (!textFromForm.trim().isEmpty()) {
            //setting paths, constants
            FileSystemStorageService service = context.getBean(FileSystemStorageService.class);
            service.setFileAttributesToCommonConstants("fromWebForm", false);
            constants.setReadingFromFile(false);
        } else {
            if (!file.isEmpty()) {
                handleFileUpload(file, redirectAttributes, constants);
            }
        }

        BuildingPortion buildingPortion = context.getBean(BuildingPortion.class);
        buildingPortion.startPortionBuilding("" + textFromForm.trim(), constants);

        for (TextPortionForRhythm instance : AnalyserPortionOfText.getListOfInstance()) {
            AnalyserPortionOfText.prepareSetOfWordsForFurtherDefineMeterSchema(instance.getNumberOfPortion());
        }

        AnalyserPortionOfText.prepareUnknownAndKnownWords();

        //if there are no unknown words
        if (CommonConstants.getUnKnownWords().size() == 0) {
            AnalyserPortionOfText.portionAnalysys(constants);
            if (constants.isThisIsVerse()) {
                return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS_VERSE;
            }
            return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS_PROSE;
        }

        //if there are unknown words
        return "redirect:/" + ViewNames.DEFINE_STRESSES_UNKNOWN_WORDS;
    }

    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes, CommonConstants commonConstants) {

        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/" + ViewNames.SET_ANALYSIS_ATTRIBUTES;
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
