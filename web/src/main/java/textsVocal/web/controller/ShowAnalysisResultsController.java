package textsVocal.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import textsVocal.config.CommonConstants;
import textsVocal.config.HeaderAnFooterListsForWebOutput;
import textsVocal.structure.PortionOfTextAnalyser;
import textsVocal.structure.VersePortionForRythm;
import textsVocal.utilsCore.WebVerseCharacteristicsOutput;
import textsVocal.web.utilsWeb.ChangedValuesInHTMLTable;
import textsVocal.web.utilsWeb.ViewNames;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ShowAnalysisResultsController {

    @Autowired
    private ConfigurableApplicationContext context;

    private static List<List<WebVerseCharacteristicsOutput>> listStressesTable = new ArrayList<>();

    @GetMapping(ViewNames.SHOW_ANALYSIS_RESULTS)
    public String showAnalysisResults(Model model) {

        //headers and footers
        List<List<String>> listListHeaders = HeaderAnFooterListsForWebOutput.getPortionHeaders();
        List<List<String>> listListFooters = HeaderAnFooterListsForWebOutput.getPortionFooters();
        model.addAttribute("listListHeaders", listListHeaders);
        model.addAttribute("listListFooters", listListFooters);

        //main table with characteristics
        if (context.getBean(CommonConstants.class).isThisIsVerse()) {//verse
            listStressesTable = PortionOfTextAnalyser.prepareListStressesTableVerseForWeb();
        } else {//prose
            listStressesTable =  PortionOfTextAnalyser.prepareListStressesTableProseForWeb();
        }
        model.addAttribute("listTableData", listStressesTable);

        //list with stress profiles
        List<Double[]> listStressesProfile = PortionOfTextAnalyser.prepareListStressesProfileWeb();
        model.addAttribute("listStressesProfile", listStressesProfile);

        //list with juncture profiles
        List<Double[]> listJunctureProfile = PortionOfTextAnalyser.prepareListJunctureProfileWeb();
        model.addAttribute("listJunctureProfile", listJunctureProfile);

        //for catch checkboxes in the tables
        model.addAttribute("changedValuesInHTMLTable", new ChangedValuesInHTMLTable());

        return ViewNames.SHOW_ANALYSIS_RESULTS;
    }

    /**
     * by changing walue in the table stresses
     *
     * @param changedValuesInHTMLTable
     * @param errors
     * @param model
     * @return
     */
    @RequestMapping(value = "/tableStressesVerse", method = RequestMethod.POST)
    public String getChangedFlagsVerse(@ModelAttribute ChangedValuesInHTMLTable changedValuesInHTMLTable, BindingResult errors, Model model) {
        VersePortionForRythm.makeCorrectionInVerseCharacteristicFromWebUser(changedValuesInHTMLTable.getCheckedItems(), changedValuesInHTMLTable.getNewValuesItems());
        return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS;
    }
}
