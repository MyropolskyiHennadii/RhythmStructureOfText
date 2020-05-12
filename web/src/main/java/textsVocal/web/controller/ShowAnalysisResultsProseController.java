package textsVocal.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import textsVocal.config.CommonConstants;
import textsVocal.config.HeaderAnFooterListsForWebOutput;
import textsVocal.structure.AnalyserPortionOfText;
import textsVocal.structure.ProsePortionForRythm;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * class for "showAnalysisResultsProse" page control
 */
@Controller
public class ShowAnalysisResultsProseController {

    private static List<List<OutputWebCharacteristics>> listStressesTable = new ArrayList<>();
    private static List<String> listStressesProfile = new ArrayList<>();
    private static List<String> listDistributionByLenght = new ArrayList<>();

    private final StorageService storageService;

    @Autowired
    public ShowAnalysisResultsProseController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping(ViewNames.SHOW_ANALYSIS_RESULTS_PROSE)
    public String showAnalysisResults(Model model) {

        //headers and footers
        List<List<String>> listListHeaders = HeaderAnFooterListsForWebOutput.getPortionHeaders();
        List<List<String>> listListFooters = HeaderAnFooterListsForWebOutput.getPortionFooters();
        model.addAttribute("listListHeaders", listListHeaders);
        model.addAttribute("listListFooters", listListFooters);

        //main table with characteristics
        listStressesTable = AnalyserPortionOfText.prepareListStressesTableProseForWeb();
        model.addAttribute("listTableData", listStressesTable);

        //list with stress profiles
        listStressesProfile = Arrays.stream(AnalyserPortionOfText.getStressProfileOfAllPortions()).collect(Collectors.toList());
        model.addAttribute("listStressesProfile", listStressesProfile);

        model.addAttribute("numberOfCentences", AnalyserPortionOfText.getNumberOfSegments());
        model.addAttribute("maxLength", AnalyserPortionOfText.getMaxLengthSegment());
        model.addAttribute("averageLength", AnalyserPortionOfText.getAverageLengthOfSegments());

        //list with distribution by length
        listDistributionByLenght = Arrays.stream(AnalyserPortionOfText.getDistributionSegmentByLength()).collect(Collectors.toList());
        model.addAttribute("listDistributionByLength", listDistributionByLenght);

        //for catch checkboxes in the tables
        model.addAttribute("changedValuesInHTMLTable", new ChangedValuesInHTMLTable());

        //file with results
        model.addAttribute("files", storageService.loadAllOutput().map(
                path -> MvcUriComponentsBuilder.fromMethodName(SetAnalysisAttributesController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));

        return ViewNames.SHOW_ANALYSIS_RESULTS_PROSE;
    }

    /**
     * by changing value in the table stresses
     *
     * @param changedValuesInHTMLTable instance of ChangedValuesHTMLTable
     * @param errors
     * @param model
     * @return
     */
    @RequestMapping(value = "/tableStressesProse", method = RequestMethod.POST)
    public String getChangedFlagsProse(@ModelAttribute ChangedValuesInHTMLTable changedValuesInHTMLTable, BindingResult errors, Model model) {
        ProsePortionForRythm.makeCorrectionInProseCharacteristicFromWebUser(changedValuesInHTMLTable.getCheckedItems(), changedValuesInHTMLTable.getNewValuesItems());
        AnalyserPortionOfText.calculateSummaryForAllPortions();
        return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS_PROSE;
    }


    @PostMapping(Mappings.SHOW_ANALYSIS_RESULTS_PROSE)
    public String processingSaveResultsToFile(Model model) {
        saveResultsToFileForUsersDownloading();
        return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS_PROSE;
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
            throw new StorageException("Could not initialize storage", e);
        }

        StringBuilder accumulation = new StringBuilder();
        List<List<String>> listListHeaders = HeaderAnFooterListsForWebOutput.getPortionHeaders();
        List<List<String>> listListFooters = HeaderAnFooterListsForWebOutput.getPortionFooters();

        for (int i = 0; i < listListHeaders.size(); i++) {

            //header
            List<String> listHeader = listListHeaders.get(i);
            for (String s : listHeader) {
                accumulation.append(s).append("\n");
            }

            //stresses
            List<OutputWebCharacteristics> stressTable = listStressesTable.get(i);
            for (OutputWebCharacteristics w : stressTable) {
                accumulation.append("[").append(w.getId()).append("] ").append(w.getWords()).append("\n");
                accumulation.append("[").append(w.getMeterRepresentationWithSpaces()).append("]").append("\n");
            }
        }
        accumulation.append("\n");
        accumulation.append("============================\n");
        accumulation.append("\n");
        accumulation.append("Number of sentences: ").append(AnalyserPortionOfText.getNumberOfSegments()).append("\n");
        accumulation.append("Maximal length (in syllables): ").append(AnalyserPortionOfText.getMaxLengthSegment()).append("\n");
        accumulation.append("Average length (in syllables): ").append(AnalyserPortionOfText.getAverageLengthOfSegments()).append("\n");
        accumulation.append("----------------------------\n");

        accumulation.append("Average stress profile\n");
        accumulation.append("Number of syllable\n");
        for (int j = 0; j < listStressesProfile.size(); j++) {
            accumulation.append("\t" + (j + 1));
        }
        accumulation.append("\n");
        accumulation.append("% of stresses\n");
        for (String v : listStressesProfile) {
            accumulation.append("\t" + v);
        }
        accumulation.append("\n");
        accumulation.append("----------------------------\n");
        accumulation.append("Distribution sentences by length\n");
        accumulation.append("Number of syllable\n");
        for (int j = 0; j < listDistributionByLenght.size(); j++) {
            accumulation.append("\t" + (j + 1));
        }
        accumulation.append("\n");
        accumulation.append("% sentences to all\n");
        for (String v : listDistributionByLenght) {
            accumulation.append("\t" + v);
        }
        accumulation.append("\n");
        accumulation.append("----------------------------\n");

        CommonConstants constants = context.getBean(CommonConstants.class);
        String fileOutput = constants.getFileOutputDirectory() + File.separator + constants.getFileOutputName();
        FileTreatment.outputResultToFile(accumulation, fileOutput);
    }

}
