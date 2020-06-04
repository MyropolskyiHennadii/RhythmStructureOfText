package textsVocal.console.dbRuMaintenance;

import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * class for quick adding records to Russian dictionary from pattern list
 */
public class DB_PatternDataRu {

    // == fields ==
    private static int idValue = 0;

    private final List<DB_PatternItemRu> items = new ArrayList<>();

    // == constructors ==
    public DB_PatternDataRu() {

        addItem(new DB_PatternItemRu(0, 0, "EXIT (ВЫЙТИ)", "FROM (ИЗ)", "MENU (МЕНЮ)", ".", 0, true));
        addItem(new DB_PatternItemRu(1, 3872610, "табурет", "001", "Существительное", "табурет", 0, true));
        addItem(new DB_PatternItemRu(2, 2263334, "лодырь", "10", "Существительное", "лодырь", 1, true));
        addItem(new DB_PatternItemRu(3, 899530, "картина", "010", "Существительное", "картина", 1, true));
        addItem(new DB_PatternItemRu(4, 2349055, "миля", "10", "Существительное", "миля", 1, true));
        addItem(new DB_PatternItemRu(5, 23, "звездный", "10", "Прилагательное", "звездный", 2, true));
        addItem(new DB_PatternItemRu(6, 2536066, "немецкий", "010", "Прилагательное", "немецкий", 2, true));
        addItem(new DB_PatternItemRu(7, 3748844, "собравшийся", "0100", "Причастие", "собраться", 4, true));
        addItem(new DB_PatternItemRu(8, 2277163, "любящий", "100", "Прилагательное", "любящий", 2, true));
        addItem(new DB_PatternItemRu(9, 3464832, "разгневанный", "0100", "Причастие", "разгневать", 2, true));
        addItem(new DB_PatternItemRu(10, 788027, "играющий", "0100", "Причастие", "играть", 2, true));
        addItem(new DB_PatternItemRu(11, 788027, "ликвидировать", "00100", "Глагол в личной форме", "ликвидировать", 2, true));
        addItem(new DB_PatternItemRu(12, 174398, "бросаться", "010", "Глагол в личной форме", "бросаться", 4, true));
        addItem(new DB_PatternItemRu(13, 3468938, "раздевшись", "010", "Деепричастие", "раздеться", 0, false));

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
