package textsVocal.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class DynamicTableRythm<T> {

    //== fields ============================================
    private List<String> namesOfColumn = new ArrayList<String>();//list of column's names
    private Map<String, List<?>> DataColumns = new HashMap<>();//map with column's name and column's data

    private static final Logger log = LoggerFactory.getLogger("DynamicTableRythm");//logger

    //== constructor =========================================
    /**
     * Constructor
     * @param pNamesOfColumn - list with column's names
     * @param pSupplier - supplier for column's data
     */
    public DynamicTableRythm(List<String> pNamesOfColumn, List<Supplier<List<T>>> pSupplier) {

        if (!checkDimensionOfListsAndNames(pNamesOfColumn, pSupplier)) {
            throw new IllegalArgumentException("Non-equel dimension of columns names and columns lists in dynamic table");
        }

        if (new HashSet<>(pNamesOfColumn).size() != pNamesOfColumn.size()) {
            throw new IllegalArgumentException("There are duplicate names in namesOfColumn");
        }

        //Always column N1 = number of line in the table (beginning from 1)
        namesOfColumn.add(0, "$NumberOfLine");
        Supplier<List<Integer>> I = ArrayList<Integer>::new;
        DataColumns.put("$NumberOfLine", I.get());

        //creature map
        for (int i = 0; i < pNamesOfColumn.size(); i++) {
            addColumn(pNamesOfColumn.get(i), pSupplier.get(i));
        }

        log.debug("Create DynamicTable succesfully");
    }

    //=== getters and setters =======================================================


    //== public instance methods ====================================================
    /**
     * setting data in new row
     *
     * @param pData
     * @return
     */
    public boolean setRow(List<?> pData) {
        String name;

        if (pData.size() != namesOfColumn.size() - 1) {
            log.debug("Non-equel dimension adding data and columns lists in dynamic table", IllegalArgumentException.class);
            throw new IllegalArgumentException("Non-equel dimension adding data and columns lists in dynamic table");
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
            log.debug("Impossible to add data to table: may be, incompatible data type", IllegalArgumentException.class);
            throw new IllegalArgumentException("Impossible to add data to table: may be, incompatible data type");
        }
    }

    /**
     *getting the size of lists
     * @return
     */
    public int getSize() {
        return DataColumns.get("$NumberOfLine").size();
    }


    /**
     *return map with data in row
     * @param nRow
     * @return
     */
    public Map getRow(int nRow) {
        String name = "";
        Map<String, Object> Data = new HashMap<>();
        for (int i = 0; i < namesOfColumn.size(); i++) {
            name = namesOfColumn.get(i);
            Data.put(name, DataColumns.get(name).get(nRow));
        }
        return Data;
    }

    public List getColumn(String nameColumn) {
        return DataColumns.get(nameColumn);
    }

    /**
     *find value in column by number of row
     * @param pNameColumn
     * @param nRow
     * @return
     */
    public Object getValue(String pNameColumn, int nRow) {
        return DataColumns.get(pNameColumn).get(nRow);
    }

    /**
     *find value in column by condition in another column
     * @param pNameColumn
     * @param pNameCondition
     * @param pValueCondition
     * @return
     */
    public List getValue(String pNameColumn, String pNameCondition, T pValueCondition) {
        int indexName = namesOfColumn.indexOf(pNameColumn);
        int indexNameCondition = namesOfColumn.indexOf(pNameCondition);
        if ((indexName == -1) || (indexNameCondition == -1)) {
            log.debug("Incorrect names of columns", IllegalArgumentException.class);
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
     * by columns name (with Integer values!) find minimum value
     * @param pNameColumn
     * @return
     */
    public Integer getMinimumValue(String pNameColumn)
    {
        List<Integer> numberOfFragment = getColumn("Number of line");
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
     * by columns name (with Integer values!) find maximum value
     * @param pNameColumn
     * @return
     */
    public Integer getMaximumValue(String pNameColumn)
    {
        List<Integer> numberOfFragment = getColumn("Number of line");
        OptionalInt optMaxNumber = numberOfFragment.stream().mapToInt(x -> x).max();
        if (optMaxNumber.isPresent())
        {
            return optMaxNumber.getAsInt();
        } else
        {
            return -1;
        }
    }

    //== private methods ===================================
    /**
     * add column to the table
     * @param name
     * @param sup
     * @param <T>
     */
    private <T> void addColumn(String name, Supplier<List<T>> sup) {
        namesOfColumn.add(name);
        DataColumns.put(name, sup.get());
    }

    /**
     * check equality sizes of lists
     * @param pNamesOfColumn
     * @param pSupplier
     * @param <T>
     * @return
     */
    private <T> boolean checkDimensionOfListsAndNames(List<String> pNamesOfColumn, List<Supplier<List<T>>> pSupplier) {
        return (pNamesOfColumn.size() == pSupplier.size());
    }

}
