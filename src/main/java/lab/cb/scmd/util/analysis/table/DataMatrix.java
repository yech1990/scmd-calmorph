package lab.cb.scmd.util.analysis.table;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * Created on 2003/10/24
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author sesejun
 * <p>
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DataMatrix {
    private PrintStream fOut = System.out;
    private String name = "";
    private ArrayList<String>[] axisNameList = new ArrayList[2];
    private double[][] dataMatrix;
    private HashMap<String, Integer>[] axisNameToNumber = new HashMap[2];
    private int rowsize = 0;
    private int colsize = 0;
    private double MISSINGVALUE = Double.MIN_VALUE;
    private String FIELD_SEPARATOR = "\t";
    private String NULLVALUE = "";

    public DataMatrix() {
        axisNameList[0] = new ArrayList<>();
        axisNameList[1] = new ArrayList<>();
        axisNameToNumber[0] = new HashMap<>();
        axisNameToNumber[1] = new HashMap<>();
    }

    private DataMatrix(int row, int column) {
        dataMatrix = new double[row][column];
        rowsize = row;
        colsize = column;

        axisNameList[0] = new ArrayList<>();
        axisNameList[1] = new ArrayList<>();
        axisNameToNumber[0] = new HashMap<>();
        axisNameToNumber[1] = new HashMap<>();
    }

    public DataMatrix(String str, double[][] data) {
        name = str;

        rowsize = data.length;
        colsize = data[0].length;
        dataMatrix = data;

        axisNameList[0] = new ArrayList<>();
        axisNameList[1] = new ArrayList<>();
        axisNameToNumber[0] = new HashMap<>();
        axisNameToNumber[1] = new HashMap<>();
    }

    public static void main(String[] args) {
        int argc = 0;
        DataMatrix dm = new DataMatrix();
        String columnFile = "";
        String rowFile = "";
        if (args[argc].equals("-c")) {
            argc++;
            columnFile = args[argc++];
        }
        if (args[argc].equals("-r")) {
            argc++;
            rowFile = args[argc++];
        }
        dm.load(args[argc++], columnFile, rowFile);
    }

    /**
     * @return
     */
    public void setMissingValue(double v) {
        MISSINGVALUE = v;
    }

    /*
     *
     */
    private boolean isMissingValueAt(int row, int column) {
        return this.get(row, column) == MISSINGVALUE;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    /**
     * @return
     */
    public String getColumnName(int n) {
        return axisNameList[0].get(n);
    }

    /**
     * @return
     */
    public ArrayList<String> getAllColumnName() {
        return axisNameList[0];
    }

    /**
     * @param str
     */
    private void addColumnName(String str) {
        axisNameToNumber[0].put(str, new Integer(axisNameList[0].size()));
        axisNameList[0].add(str);
    }

    /**
     * @param n
     * @param str
     */
    public void setColumnName(int n, String str) {
        axisNameToNumber[0].put(str, new Integer(axisNameList[0].size()));
        axisNameList[0].set(n, str);
    }

    /**
     * @param strlist
     */
    public void addAllColumnName(ArrayList<String> strlist) {
        axisNameList[0] = strlist;
    }

    /**
     * @return
     */
    private Integer getColumnNumber(String str) {
        return axisNameToNumber[0].get(str);
    }


    public int getColumnSize() {
        return colsize;
    }

    /**
     * @return
     */
    public String getRowName(int n) {
        return axisNameList[1].get(n);
    }

    /**
     * @return
     */
    public ArrayList<String> getAllRowName() {
        return axisNameList[1];
    }

    /**
     * @param str
     */
    private void addRowName(String str) {
        axisNameToNumber[1].put(str, new Integer(axisNameList[1].size()));
        axisNameList[1].add(str);
    }

    /**
     * @param n
     * @param str
     */
    public void setRowName(int n, String str) {
        axisNameToNumber[1].put(str, new Integer(axisNameList[1].size()));
        axisNameList[1].set(n, str);
    }

    /**
     * @param strlist
     */
    public void addAllRowName(ArrayList<String> strlist) {
        axisNameList[1] = strlist;
    }

    /**
     * @return
     */
    private Integer getRowNumber(String str) {
        return axisNameToNumber[1].get(str);
    }

    public int getRowSize() {
        return rowsize;
    }

    public double get(int row, int column) {
        return dataMatrix[row][column];
    }

    private double[] getOneRow(int row) {
        return dataMatrix[row];
    }

    public double[] getOneRow(String str) {
        Integer i = getRowNumber(str);
        if (i == null)
            return new double[0];
        int row = i.intValue();
        return getOneRow(row);
    }

    public double[] getOneColumn(int column) {
        double[] oneColumn = new double[getRowSize()];
        for (int i = 0; i < getRowSize(); i++) {
            oneColumn[i] = get(i, column);
        }
        return oneColumn;
    }

    public double[] getOneColumn(String str) {
        Integer colnum = getColumnNumber(str);
        if (colnum == null)
            return null;
        int column = colnum.intValue();
        return getOneColumn(column);
    }

    public double[][] getMatrix() {
        return dataMatrix;
    }

    public void set(int row, int column, double v) {
        dataMatrix[row][column] = v;
    }

    public void load(String filename) {
        this.load(filename, NULLVALUE, FIELD_SEPARATOR, true, true);
    }

    public void load(String filename, String nullvalue) {
        this.load(filename, nullvalue, FIELD_SEPARATOR, true, true);
    }

    public void load(String filename, String fieldSeparator, boolean rowComment, boolean columnComment) {
        this.load(filename, NULLVALUE, FIELD_SEPARATOR, true, true);
    }

    public void load(String filename, String nullvalue, String fieldSeparator, boolean rowComment, boolean columnComment) {
        try {
            load(new FileReader(filename), nullvalue, fieldSeparator, rowComment, columnComment);
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found: " + e);
        }
    }

    public void load(Reader reader, String fieldSeparator, boolean rowComment, boolean columnComment) {
        this.load(reader, NULLVALUE, fieldSeparator, rowComment, columnComment);

    }

    public void load(Reader reader, String nullvalue, String fieldSeparator, boolean rowComment, boolean columnComment) {

        ArrayList<ArrayList> dataArray = new ArrayList<>();
        String str = "";
        try {

            BufferedReader in = new BufferedReader(reader);

            String[] strvector;
            if (columnComment) {
                str = in.readLine();
                strvector = str.split(FIELD_SEPARATOR);
                setName(strvector[0]);
                for (int i = 1; i < strvector.length; i++) {
                    addColumnName(strvector[i]);
                }
            }

            int dataStartColumn = 0;
            if (rowComment)
                dataStartColumn = 1;
            int maxrowsize = 0;
            while ((str = in.readLine()) != null) {
                strvector = str.split(FIELD_SEPARATOR);
                if (rowComment)
                    addRowName(strvector[0]);
                if (maxrowsize == 0)
                    maxrowsize = strvector.length;
                ArrayList<Double> v = new ArrayList<>();
                for (int i = dataStartColumn; i < strvector.length; i++) {
                    if (strvector[i].equals(nullvalue))
                        v.add(new Double(Double.NaN));
                    else
                        v.add(new Double(Double.parseDouble(strvector[i])));
                }
                for (int i = strvector.length; i < maxrowsize; i++) {
                    v.add(new Double(Double.NaN));
                }
                dataArray.add(v);
            }
            colsize = dataArray.get(0).size();
            rowsize = dataArray.size();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found: " + e);
        } catch (IOException e) {
            System.out.println("File I/O Error: " + e);
        }

        dataMatrix = new double[getRowSize()][getColumnSize()];
        for (int i = 0; i < getRowSize(); i++) {
            ArrayList v = dataArray.get(i);
            for (int j = 0; j < getColumnSize(); j++) {
                dataMatrix[i][j] = ((Double) v.get(j)).doubleValue();
            }
        }
        return;
    }

    private void load(String string, String columnFile, String rowFile) {
        DataMatrix tmpdm = new DataMatrix();
        tmpdm.load(string);

        ArrayList<String> colparams = new ArrayList<>();
        if (columnFile.length() != 0) {
            PlainTable ct = new PlainTable();
            ct.load(columnFile, "\t", false, false);
            for (int i = 0; i < ct.getRowSize(); i++) {
                colparams.add(ct.get(i, 0).toString());
            }
        } else {
            colparams = tmpdm.getAllColumnName();
        }

        ArrayList<String> rowparams = new ArrayList<>();
        if (rowFile.length() != 0) {
            PlainTable rt = new PlainTable();
            rt.load(rowFile, "\t", false, false);
            for (int i = 0; i < rt.getRowSize(); i++) {
                rowparams.add(rt.get(i, 0).toString());
            }
        } else {
            rowparams = tmpdm.getAllRowName();
        }

        System.out.print(tmpdm.getName());
        for (String colparam : colparams) {
            System.out.print("\t" + colparam);
        }
        System.out.println();
        for (String rowname : rowparams) {
            System.out.print(rowname);
            for (String colname : colparams) {
                System.out.print("\t" + tmpdm.get(tmpdm.getRowNumber(rowname), tmpdm.getColumnNumber(colname)));
            }
            System.out.println();
        }
    }


    public double[] avg() {
        return centerOfGravity();
    }

    private double[] avg(boolean col) {
        return centerOfGravity(col);
    }

    /**
     * @return
     */
    private double[] centerOfGravity() {
        return centerOfGravity(true);
    }

    private double[] centerOfGravity(boolean col) {
        double[] center = new double[0];
        int rowsize = getRowSize();
        int colsize = getColumnSize();

        if (col) {
            center = new double[getColumnSize()];
            for (int i = 0; i < colsize; i++) {
                center[i] = 0.0;
            }
            int n = 0;
            for (int i = 0; i < colsize; i++) {
                n = 0;
                for (int j = 0; j < rowsize; j++) {
                    if (this.get(j, i) == MISSINGVALUE)
                        continue;
                    center[i] += this.get(j, i);
                    n++;
                }
                center[i] = center[i] / n;
            }
        } else {
            center = new double[getRowSize()];
            for (int i = 0; i < rowsize; i++) {
                center[i] = 0.0;
            }
            int n = 0;
            for (int i = 0; i < rowsize; i++) {
                n = 0;
                for (int j = 0; j < colsize; j++) {
                    if (this.get(i, j) == MISSINGVALUE)
                        continue;
                    center[i] += this.get(i, j);
                    n++;
                }
                center[i] = center[i] / n;
            }
        }
        return center;
    }

    /**
     * @return
     */
    public double[] var() {
        return var(true);
    }

    public double[] var(boolean col) {
        int colsize = getColumnSize();
        int rowsize = getRowSize();
        double[] var = new double[0];
        if (col) {
            if (rowsize < 2) {
                return new double[0];
                // unable to compute variance because the number of samples is small
            }
            double[] c = centerOfGravity(col);
            var = new double[colsize];
            for (int i = 0; i < colsize; i++) {
                int n = 0;
                for (int j = 0; j < rowsize; j++) {
                    if (isMissingValueAt(j, i))
                        continue;
                    var[i] += (get(j, i) - c[i]) * (get(j, i) - c[i]);
                    n++;
                }
                var[i] = var[i] / (n - 1);
            }
        } else {
            if (colsize < 2) {
                return new double[0];
                // unable to compute variance because the number of samples is small
            }
            double[] c = centerOfGravity(col);
            var = new double[rowsize];
            for (int i = 0; i < rowsize; i++) {
                int n = 0;
                for (int j = 0; j < colsize; j++) {
                    if (isMissingValueAt(i, j))
                        continue;
                    var[i] += (get(i, j) - c[i]) * (get(i, j) - c[i]);
                    n++;
                }
                var[i] = var[i] / (n - 1);
            }

        }
        return var;
    }

    /**
     * @return
     */
    public double[] stddev() {
        return stddev(true);
    }

    private double[] stddev(boolean col) {
        double[] var = this.var(col);
        for (int i = 0; i < var.length; i++) {
            var[i] = Math.sqrt(var[i]);
        }
        return var;
    }

    /**
     * coefficient of variation
     */
    public double[] cv() {
        return cv(true);
    }

    private double[] cv(boolean col) {
        double[] cv = this.stddev(col);
        double[] avg = this.avg(col);
        for (int i = 0; i < cv.length; i++) {
            cv[i] = cv[i] / avg[i];
        }
        return cv;
    }

    public double[] max() {
        return max(true);
    }

    public double[] max(boolean col) {
        double[] maxarray = new double[0];
        if (col) {
            maxarray = new double[getColumnSize()];
            int row = getRowSize();
            int column = getColumnSize();
            Arrays.fill(maxarray, Double.MIN_VALUE);
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    if (maxarray[j] < this.get(i, j) && !isMissingValueAt(i, j)) {
                        maxarray[j] = this.get(i, j);
                    }
                }
            }

        } else {
            maxarray = new double[getRowSize()];
            int row = getRowSize();
            int column = getColumnSize();
            Arrays.fill(maxarray, Double.MIN_VALUE);
            for (int i = 0; i < column; i++) {
                for (int j = 0; j < row; j++) {
                    if (maxarray[j] < this.get(j, i) && !isMissingValueAt(j, i)) {
                        maxarray[j] = this.get(j, i);
                    }
                }
            }
        }
        return maxarray;
    }


    public double[] min() {
        return min(true);
    }

    public double[] min(boolean col) {
        double[] minarray = new double[0];
        if (col) {
            minarray = new double[getColumnSize()];
            int row = getRowSize();
            int column = getColumnSize();
            Arrays.fill(minarray, Double.MAX_VALUE);
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    if (minarray[j] > this.get(i, j) && !isMissingValueAt(i, j)) {
                        minarray[j] = this.get(i, j);
                    }
                }
            }

        } else {
            minarray = new double[getRowSize()];
            int row = getRowSize();
            int column = getColumnSize();
            Arrays.fill(minarray, Double.MAX_VALUE);
            for (int i = 0; i < column; i++) {
                for (int j = 0; j < row; j++) {
                    if (minarray[j] > this.get(j, i) && !isMissingValueAt(j, i)) {
                        minarray[j] = this.get(j, i);
                    }
                }
            }
        }
        return minarray;
    }

    public DataMatrix subMatrix(Object[] rows, Object[] cols) {
        DataMatrix nm = new DataMatrix(rows.length, cols.length);
        int rowNumber, colNumber;
        for (int i = 0; i < rows.length; i++) {
            nm.addRowName((String) rows[i]);
            rowNumber = this.getRowNumber((String) rows[i]).intValue();
            for (int j = 0; j < cols.length; j++) {
                colNumber = this.getColumnNumber((String) cols[j]).intValue();
                nm.set(i, j, this.get(rowNumber, colNumber));
            }
        }
        for (Object col : cols) {
            nm.addColumnName((String) col);
        }
        return nm;
    }

    public void print(PrintStream out) {
        fOut = out;
        print();
    }

    public void print() {
        print(true, true);
    }

    /**
     * @param printRowName
     * @param printColName
     */
    public void print(boolean printRowName, boolean printColName) {
        double data = 0.0;
        if (printColName) {
            fOut.print(name);
            for (int i = 0; i < axisNameList[0].size(); i++) {
                fOut.print(FIELD_SEPARATOR + axisNameList[0].get(i));
            }
            fOut.println();
        }
        for (int i = 0; i < rowsize; i++) {
            if (printRowName) {
                fOut.print(axisNameList[1].get(i) + FIELD_SEPARATOR);
            }
            for (int j = 0; j < colsize - 1; j++) {
                data = dataMatrix[i][j];
                if (data == Math.round(data))
                    fOut.print((int) data + FIELD_SEPARATOR);
                else
                    fOut.print(data + FIELD_SEPARATOR);
            }
            data = dataMatrix[i][colsize - 1];
            if (data == Math.round(data))
                fOut.println((int) data + FIELD_SEPARATOR);
            else
                fOut.println(data + FIELD_SEPARATOR);
        }

    }

    /**
     * @param matrixname
     * @param printRowName
     * @param printColName
     * @param member
     */
    public void print(String matrixname, boolean printRowName, boolean printColName, ArrayList member) {
        if (printColName) {
            fOut.print(matrixname);
            for (int i = 0; i < axisNameList[0].size(); i++) {
                fOut.print(FIELD_SEPARATOR + axisNameList[0].get(i));
            }
            fOut.println();
        }

        for (Object o : member) {
            int n = ((Integer) o).intValue();
            if (printRowName) {
                fOut.print(axisNameList[1].get(n) + FIELD_SEPARATOR);
            }
            for (int j = 0; j < colsize - 1; j++) {
                fOut.print(dataMatrix[n][j] + FIELD_SEPARATOR);
            }
            fOut.println(dataMatrix[n][colsize - 1]);
        }

    }

}
