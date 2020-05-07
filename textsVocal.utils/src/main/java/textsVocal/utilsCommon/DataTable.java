package textsVocal.utilsCommon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * class to manipulate with different characteristics as with table
 * @param <T> type of the column: String or Integer
 */
public class DataTable<T> {

    //== fields ============================================
    private List<String> namesOfColumn = new ArrayList<>();//list of column's names
    private Map<String, List<?>> DataColumns = new HashMap<>();//map with column's name and column's data

    private static final Logger log = LoggerFactory.getLogger("DynamicTableRythm");//logger

    //== constructor =========================================
    /**
     * Constructor
     * @param pNamesOfColumn - list with column's names
     * @param pSupplier - supplier for column's data
     */
    public DataTable(List<String> pNamesOfColumn, List<Supplier<List<T>>> pSupplier) {

        if (!checkDimensionOfListsAndNames(pNamesOfColumn, pSupplier)) {
            log.error("Non-equel dimension of columns names and columns lists in dynamic table");
            throw new IllegalArgumentException("Non-equel dimension of columns names and columns lists in dynamic table");
        }

        if (new HashSet<>(pNamesOfColumn).size() != pNamesOfColumn.size()) {
            log.error("There are duplicate names in namesOfColumn");
            throw new IllegalArgumentException("There are duplicate names in namesOfColumn");
        }

        //Always column N1 = number of line in the table (beginning from 1)
        namesOfColumn.add(0, "$NumberOfLine");
        Supplier<List<Integer>> I = ArrayList::new;
        DataColumns.put("$NumberOfLine", I.get());

        //creature map
        for (int i = 0; i < pNamesOfColumn.size(); i++) {
            addColumn(pNamesOfColumn.get(i), pSupplier.get(i));
        }

        log.debug("Create DynamicTable succesfully");
    }

    //=== getters and setters =======================================================

    public Collection<String> getNamesOfColumn() {
        return Collections.unmodifiableCollection(namesOfColumn);
    }

    //== public instance methods ====================================================
    /**
     * setting data in new row
     *
     * @param pData list of String or Integer to add in the table
     * @return true if adding was OK
     */
    public boolean setRow(List<?> pData) {
        String name;

        if (pData.size() != namesOfColumn.size() - 1) {
            log.error("Non-equel dimension adding data and columns lists in dynamic table");
            throw new IllegalArgumentException("Non-equel dimensions adding data and columns lists in the table");
        }
        try {
            List numlist = DataColumns.get("$NumberOfLine");
            int numRecords = numlist.size();
            numlist.add(++numRecords);

            for (int i = 0; i < pData.size(); i++) {
                name = namesOfColumn.get(i + 1);
                List list = DataColumns.get(name);
                list.add(pData.get(i));
            }
            return true;
        } catch (Exception ex) {
            log.debug("Impossible to add data to table: may be, incompatible data type");
            throw new IllegalArgumentException("Impossible to add data to table: may be, incompatible data type");
        }
    }

    /**
     *getting the size of lists
     * @return size of lists
     */
    public int getSize() {
        return DataColumns.get("$NumberOfLine").size();
    }


    /**
     *return map with data in row
     * @param nRow number of row in the table
     * @return map with name of columns as keys and values of row as values
     */
    public Map<String, Object> getRow(int nRow) {
        String name;
        Map<String, Object> Data = new HashMap<>();
        for (String s : namesOfColumn) {
            name = s;
            Data.put(name, DataColumns.get(name).get(nRow));
        }
        return Data;
    }

    /**
     * get list (column) by its name
     * @param nameColumn name of column
     * @return list (column) by its name
     */
    public List getColumnFromTable(String nameColumn) {
        return DataColumns.get(nameColumn);
    }

    /**
     *find value in column by number of row
     * @param pNameColumn name of column
     * @param nRow number of row
     * @return value in row and column
     */
    public Object getValueFromColumnAndRow(String pNameColumn, int nRow) {
        return DataColumns.get(pNameColumn).get(nRow);
    }

    /**
     *find value in column by condition in another column
     * @param pNameColumn in which column search for value
     * @param pNameCondition in which column there is condition
     * @param pValueCondition value of condition
     * @return list with serching values
     */
    public List getValueFromColumnAndRowByCondition(String pNameColumn, String pNameCondition, T pValueCondition) {
        int indexName = namesOfColumn.indexOf(pNameColumn);
        int indexNameCondition = namesOfColumn.indexOf(pNameCondition);
        if ((indexName == -1) || (indexNameCondition == -1)) {
            log.error("Incorrect names of columns "+pNameColumn + " or " + pNameCondition);
            throw new IllegalArgumentException("Incorrect names of columns");
        }

        List listCondition = DataColumns.get(pNameCondition);
        List values = DataColumns.get(pNameColumn);
        List foundValues = new ArrayList<>();

        int indexFirst = listCondition.indexOf(pValueCondition);
        if (indexFirst != -1) {
            int indexLast = listCondition.lastIndexOf(pValueCondition);
            for (int i = indexFirst; i <= indexLast; i++) {
                if (listCondition.get(i).equals(pValueCondition)) {
                    foundValues.add(values.get(i));
                }
            }
        }
        return foundValues;
    }

    /**
     * by columns name (with Integer values!) search minimum value
     * @param pNameColumn name of column
     * @return min Integer in column
     */
    public Integer getMinimumValue(String pNameColumn)
    {
        List<Integer> numberOfFragment = getColumnFromTable(pNameColumn);
        OptionalInt optMinNumber = numberOfFragment.stream().mapToInt(x -> x).min();
        if (optMinNumber.isPresent())
        {
            return optMinNumber.getAsInt();
        } else
        {
            return -1;
        }
    }

    /**
     * by columns name (with Integer values!) search maximum value
     * @param pNameColumn name of column
     * @return max Integer in column
     */
    public Integer getMaximumValue(String pNameColumn)
    {
        List<Integer> numberOfFragment = getColumnFromTable(pNameColumn);
        OptionalInt optMaxNumber = numberOfFragment.stream().mapToInt(x -> x).max();
        if (optMaxNumber.isPresent())
        {
            return optMaxNumber.getAsInt();
        } else
        {
            return -1;
        }
    }

    /**
     * clear table
     */
    public void clearDynamicTable(){
        for(String name: namesOfColumn){
            List columnData = DataColumns.get(name);
            columnData.clear();
        }
    }
    //== private methods ===================================
    /**
     * add column to the table
     * @param name name for new list (column)
     * @param sup supplier for new list (column)
     */
    private void addColumn(String name, Supplier<List<T>> sup) {
        namesOfColumn.add(name);
        DataColumns.put(name, sup.get());
    }

    /**
     * check equality sizes of lists
     * @param pNamesOfColumn names of columns
     * @param pSupplier supplier for columns
     * @return true if everything is OK
     */
    private boolean checkDimensionOfListsAndNames(List<String> pNamesOfColumn, List<Supplier<List<T>>> pSupplier) {
        return (pNamesOfColumn.size() == pSupplier.size());
    }

}
