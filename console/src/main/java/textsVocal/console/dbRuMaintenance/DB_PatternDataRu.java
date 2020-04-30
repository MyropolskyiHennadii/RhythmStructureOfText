package textsVocal.console.dbRuMaintenance;

import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DB_PatternDataRu {

    // == fields ==
    private static int idValue = 0;

    private final List<DB_PatternItemRu> items = new ArrayList<>();

    // == constructors ==
    public DB_PatternDataRu() {

        addItem(new DB_PatternItemRu(0, 0, "EXIT (ВЫЙТИ)", "FROM (ИЗ)", "MENU (МЕНЮ)", ".", 0));
        addItem(new DB_PatternItemRu(1, 3872610, "табурет", "001", "Существительное", "табурет", 0));
        addItem(new DB_PatternItemRu(2, 899530, "картина", "010", "Существительное", "картина", 1));
        addItem(new DB_PatternItemRu(3, 23, "звездный", "10", "Прилагательное", "звездный", 2));
       // addItem(new DB_PatternItemRu(4, 24770, "аккредитовав", "00001", "Деепричастие", "аккредитовать", 0));
    }

    // == public methods ==
    public Collection<HaveID> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(@NonNull DB_PatternItemRu toAdd) {
        toAdd.setId(idValue);
        items.add(toAdd);
        idValue++;
    }

    public DB_PatternItemRu getDB_PatternItemRuById(int id){
        for (DB_PatternItemRu pattern: items) {
            if(pattern.getId() == id) {
                return pattern;
            }
        }
        return null;
    }

}
