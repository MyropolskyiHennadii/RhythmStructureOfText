package textsVocal.web.utilsWeb;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * class stores users changes in stresses table
 */
@Component
public class ChangedValuesInHTMLTable {
    private List<String> checkedItems = new ArrayList<>();
    private List<String> newValuesItems = new ArrayList<>();

    public List<String> getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(List<String> checkedItems) {
        this.checkedItems = checkedItems;
    }

    public List<String> getNewValuesItems() {
        return newValuesItems;
    }

    public void setNewValuesItems(List<String> newValuesItems) {
        this.newValuesItems = newValuesItems;
    }

    public ChangedValuesInHTMLTable() {
    }
}
