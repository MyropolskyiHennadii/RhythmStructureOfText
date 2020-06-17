package textsVocal.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import textsVocal.config.CommonConstants;
import textsVocal.config.HeaderAndFooterListsForWebOutput;
import textsVocal.structure.AnalyserPortionOfText;
import textsVocal.structure.VersePortionForRhythm;
import textsVocal.utilsCommon.FileTreatment;
import textsVocal.utilsCore.OutputWebCharacteristics;
import textsVocal.web.uploadingfiles.StorageException;
import textsVocal.web.uploadingfiles.StorageProperties;
import textsVocal.web.uploadingfiles.StorageService;
import textsVocal.web.utilsWeb.ChangedValuesInHTMLTable;
import textsVocal.web.utilsWeb.Mappings;
import textsVocal.web.utilsWeb.ViewNames;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * class for "showAnalysisResultsVerse" page control
 */
@Controller
public class ShowAnalysisResultsVerseController {

    private static List<List<OutputWebCharacteristics>> listStressesTable = new ArrayList<>();
    private static List<Double[]> listStressesProfile;
    private static List<Double[]> listJunctureProfile;

    private final StorageService storageService;
    private static final Logger log = LoggerFactory.getLogger(ShowAnalysisResultsVerseController.class);

    @Autowired
    public ShowAnalysisResultsVerseController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping(ViewNames.SHOW_ANALYSIS_RESULTS_VERSE)
    public String showAnalysisResults(Model model) {

        //headers and footers
        List<List<String>> listListHeaders = HeaderAndFooterListsForWebOutput.getPortionHeaders();
        List<List<String>> listListFooters = HeaderAndFooterListsForWebOutput.getPortionFooters();
        model.addAttribute("listListHeaders", listListHeaders);
        model.addAttribute("listListFooters", listListFooters);

        //main table with characteristics
        listStressesTable = AnalyserPortionOfText.prepareListStressesVerseForWeb();

        model.addAttribute("listTableData", listStressesTable);

        //list with stress profiles
        listStressesProfile = AnalyserPortionOfText.prepareListStressesProfileWeb();
        model.addAttribute("listStressesProfile", listStressesProfile);

        //list with juncture profiles
        listJunctureProfile = AnalyserPortionOfText.prepareListJunctureProfileWeb();
        model.addAttribute("listJunctureProfile", listJunctureProfile);

        model.addAttribute("numberOfCentences", AnalyserPortionOfText.getNumberOfSegments());
        model.addAttribute("maxLength", AnalyserPortionOfText.getMaxLengthSegment());
        model.addAttribute("averageLength", AnalyserPortionOfText.getAverageLengthOfSegments());

        //for catch checkboxes in the tables
        model.addAttribute("changedValuesInHTMLTable", new ChangedValuesInHTMLTable());

        //file with results
        model.addAttribute("files", storageService.loadAllOutput().map(
                path -> MvcUriComponentsBuilder.fromMethodName(SetAnalysisAttributesController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));

        return ViewNames.SHOW_ANALYSIS_RESULTS_VERSE;
    }

    /**
     * by changing value in the table stresses
     *
     * @param changedValuesInHTMLTable instance of ChangedValuesHTMLTable
     * @param errors
     * @param model
     * @return
     */
    @RequestMapping(value = "/tableStressesVerse", method = RequestMethod.POST)
    public String getChangedFlagsVerse(@ModelAttribute ChangedValuesInHTMLTable changedValuesInHTMLTable, BindingResult errors, Model model) {
        VersePortionForRhythm.makeCorrectionInVerseCharacteristicFromWebUser(changedValuesInHTMLTable.getCheckedItems(), changedValuesInHTMLTable.getNewValuesItems());
        AnalyserPortionOfText.calculateSummaryForAllPortions();
        return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS_VERSE;
    }


    @PostMapping(Mappings.SHOW_ANALYSIS_RESULTS_VERSE)
    public String processingSaveResultsToFile(Model model) {
        saveResultsToFileForUsersDownloading();
        return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS_VERSE;
    }

    /**
     * save results to file for further downloading by user
     */
    public void saveResultsToFileForUsersDownloading() {
        ApplicationContext context = CommonConstants.getApplicationContext();
        String outputLocation = context.getBean(StorageProperties.class).getOutputLocation();
        Path saveLocation = Paths.get(outputLocation);
        try {
            Files.createDirectories(saveLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage: {}",e.getMessage());
            throw new StorageException("Could not initialize storage", e);
        }

        ResourceBundle messages = CommonConstants.getResourceBundle();

        StringBuilder accumulation = new StringBuilder();
        List<List<String>> listListHeaders = HeaderAndFooterListsForWebOutput.getPortionHeaders();
        List<List<String>> listListFooters = HeaderAndFooterListsForWebOutput.getPortionFooters();

        for (int i = 0; i < listListHeaders.size(); i++) {

            //header
            List<String> listHeader = listListHeaders.get(i);
            for (String s : listHeader) {
                accumulation.append(s).append("\n");
            }

            //stresses
            List<OutputWebCharacteristics> stressTable = listStressesTable.get(i);
            VersePortionForRhythm.outputLineInResume(accumulation, new String[]{"Line", "Meter representation", "Meter-number of foots", "Shift meter (N syllable)", "Quantity of syllables"});
            for (OutputWebCharacteristics w : stressTable) {
                VersePortionForRhythm.outputLineInResume(accumulation, new String[]{w.getWords(), w.getMeterRepresentation(), "[" + (w.getMeter() == null ? ' ': w.toString())+ "]",
                        "[" + w.getShiftRegularMeterOnSyllable() + "]", "[" + w.getNumberOfSyllable() + "]"});
            }

            //list with stress profiles
            Double[] stressProfile = listStressesProfile.get(i);
            accumulation.append("==========================\n");
            accumulation.append(messages.getString("nameStressProfile") + "\n");
            accumulation.append(messages.getString("nameNumberSyllable") + "\n");
            for (int k = 0; k < stressProfile.length; k++) {
                accumulation.append("\t" + (i + 1));
            }
            accumulation.append("\n");
            accumulation.append(messages.getString("namePercentStress") + "\n");
            for (double v : stressProfile) {
                accumulation.append("\t" + v);
            }
            accumulation.append("\n");
            accumulation.append("==========================\n");

            //list with juncture profiles
            Double[] junctureProfile = listJunctureProfile.get(i);
            accumulation.append(messages.getString("nameJunctureProfile") + "\n");
            accumulation.append(messages.getString("nameNumberSyllable") + "\n");
            for (int k = 0; k < junctureProfile.length; k++) {
                accumulation.append("\t" + (k + 1));
            }
            accumulation.append("\n");
            accumulation.append(messages.getString("namePercentJuncture") + "\n");
            for (double v : junctureProfile) {
                accumulation.append("\t" + v);
            }
            accumulation.append("\n");
            accumulation.append("==========================\n");

            //footers
            List<String> listFooters = listListFooters.get(i);
            for (String line : listFooters) {
                accumulation.append(line);
            }
        }
        accumulation.append("\n");
        accumulation.append("============================\n");
        accumulation.append("\n");

        accumulation.append("Number of lines: ").append(AnalyserPortionOfText.getNumberOfSegments()).append("\n");
        accumulation.append("Maximal length (in syllables): ").append(AnalyserPortionOfText.getMaxLengthSegment()).append("\n");
        accumulation.append("Average length (in syllables): ").append(AnalyserPortionOfText.getAverageLengthOfSegments()).append("\n");
        accumulation.append("----------------------------\n");

        CommonConstants constants = context.getBean(CommonConstants.class);
        String fileOutput = constants.getFileOutputDirectory() + File.separator + constants.getFileOutputName();
        FileTreatment.outputResultToFile(accumulation, fileOutput, false);
    }

}
