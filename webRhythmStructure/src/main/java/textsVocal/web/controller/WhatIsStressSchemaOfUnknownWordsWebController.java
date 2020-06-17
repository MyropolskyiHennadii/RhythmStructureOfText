package textsVocal.web.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import textsVocal.config.CommonConstants;
import textsVocal.structure.AnalyserPortionOfText;
import textsVocal.structure.Word;
import textsVocal.web.utilsWeb.Mappings;
import textsVocal.web.utilsWeb.StressSchemaUnknownWordsInHTMLTable;
import textsVocal.web.utilsWeb.ViewNames;

import java.util.stream.Collectors;

/**
 * class for "defineStressUnknownWords" page control
 */
@Controller
public class WhatIsStressSchemaOfUnknownWordsWebController {


    @GetMapping(Mappings.DEFINE_STRESSES_UNKNOWN_WORDS)
    public String getUnknownWords(Model model) {

        model.addAttribute("unknownWords", CommonConstants.getUnKnownWords().stream().map(Word::getTextWord).collect(Collectors.toList()));
        model.addAttribute("stressSchemaUnknownWordsInHTMLTable", new StressSchemaUnknownWordsInHTMLTable());

        return ViewNames.DEFINE_STRESSES_UNKNOWN_WORDS;
    }

    @PostMapping(Mappings.DEFINE_STRESSES_UNKNOWN_WORDS)
    public String processingSetTextAttributes(String stressSchema,
                                              Model model){

        return ViewNames.DEFINE_STRESSES_UNKNOWN_WORDS;
    }

    /**
     * by changing value in the table stresses
     *
     * @param stressSchemaUnknownWordsInHTMLTable instance of StressSchemaUnknownWordsInHTMLTable
     * @param errors
     * @param model
     * @return
     */
    @RequestMapping(value = "/tableStressesWords", method = RequestMethod.POST)
    public String getChangeSchema(@ModelAttribute StressSchemaUnknownWordsInHTMLTable stressSchemaUnknownWordsInHTMLTable, BindingResult errors, Model model) {

        CommonConstants.updateTempWordDictionaryWithUsersDefinition(stressSchemaUnknownWordsInHTMLTable.getNewValuesItems());

        ApplicationContext context = CommonConstants.getApplicationContext();
        CommonConstants commonConstants = (CommonConstants) context.getBean("commonConstants");
        AnalyserPortionOfText.portionAnalysys(commonConstants);

        if (commonConstants.isThisIsVerse()) {
            return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS_VERSE;
        }
        return "redirect:/" + ViewNames.SHOW_ANALYSIS_RESULTS_PROSE;
    }
}
