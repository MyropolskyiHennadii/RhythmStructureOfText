package textsVocal.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import textsVocal.config.HeaderAnFooterListsForWebOutput;
import textsVocal.structure.AnalyserPortionOfText;
import textsVocal.structure.ProsePortionForRythm;
import textsVocal.utilsCore.OutputWebCharacteristics;
import textsVocal.web.utilsWeb.ChangedValuesInHTMLTable;
import textsVocal.web.utilsWeb.ViewNames;

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
        List<String> listStressesProfile = Arrays.stream(AnalyserPortionOfText.getStressProfileOfAllPortions()).collect(Collectors.toList());
        model.addAttribute("listStressesProfile", listStressesProfile);

        model.addAttribute("numberOfCentences", AnalyserPortionOfText.getNumberOfSegments());
        model.addAttribute("maxLength", AnalyserPortionOfText.getMaxLengthSegment());
        model.addAttribute("averageLength", AnalyserPortionOfText.getAverageLengthOfSegments());

        //list with distribution by length
        List<String> listDistributionByLenght = Arrays.stream(AnalyserPortionOfText.getDistributionSegmentByLength()).collect(Collectors.toList());
        model.addAttribute("listDistributionByLength", listDistributionByLenght);

        //for catch checkboxes in the tables
        model.addAttribute("changedValuesInHTMLTable", new ChangedValuesInHTMLTable());

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
    public String getChangedFlagsVerse(@ModelAttribute ChangedValuesInHTMLTable changedValuesInHTMLTable, BindingResult errors, Model model) {
        ProsePortionForRythm.makeCorrectionInProseCharacteristicFromWebUser(changedValuesInHTMLTable.getCheckedItems(), changedValuesInHTMLTable.getNewValuesItems());
        AnalyserPortionOfText.calculateSummeryForAllPortions();
        return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS_PROSE;
    }
}
