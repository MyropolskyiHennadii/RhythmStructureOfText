package textsVocal.web.utilsWeb;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * class stores users definitions of stress in unknown words
 */
@Component
public class StressSchemaUnknownWordsInHTMLTable {
    private List<String> newValuesItems = new ArrayList<>();

    public List<String> getNewValuesItems() {
        return newValuesItems;
    }

    public void setNewValuesItems(List<String> newValuesItems) {
        this.newValuesItems = newValuesItems;
    }

    public StressSchemaUnknownWordsInHTMLTable() {
    }
}
