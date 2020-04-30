package textsVocal.utilsCore;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Controller
public class DefineStressInUnknownWordsWebController implements PropertyChangeListener {

    private String newUnKnownWord = "";

    public String getNewUnKnownWord() {
        return newUnKnownWord;
    }

    public void setNewUnKnownWord(String newUnKnownWord) {
        this.newUnKnownWord = newUnKnownWord;
    }

    @GetMapping("unknownWords")
    public String getUnknownWords(Model model) throws InterruptedException, ExecutionException, IOException {
        /*String word = getNewUnKnownWord().trim();*/
        model.addAttribute("word", "WAIT, PLEASE");
        return "unknownWords";
    }

    @PostMapping("unknownWords")
    public String processingSetTextAttributes(String stressSchema,
                                              Model model) throws InterruptedException, ExecutionException, IOException {

/*        String word = getNewUnKnownWord().trim();*/
/*        ApplicationContext context = CommonConstants.getApplicationContext();
        CommonConstants constants = context.getBean(CommonConstants.class);

        if (!CommonConstants.isAlreadyRunning()) {
            CommonConstants.setAlreadyRunning(true);
            BuildingPortion buildingPortion = context.getBean(BuildingPortion.class);
            buildingPortion.startPortionBuilding(constants.getTextFromWebForm().trim(), constants);
        }*/
        model.addAttribute("word", "&&&&&&&");
        System.out.println("new stress schema " + stressSchema);
/*        Set<String> serviceSet = new HashSet<>();
        serviceSet.add(stressSchema);
        CommonConstants.getTempWordDictionary().put(word, serviceSet);

        model.addAttribute("word", word);
        if (word.equals("###EndBuildPortion###")) {
            return "showAnalysisResults";
        }*/
        return "unknownWords";
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.setNewUnKnownWord((String) evt.getNewValue());
        System.out.println("!!!!!!!" + evt.getNewValue());
    }

}
