package lab.cb.scmd.util.analysis.table;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Created on 2003/10/28
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
public class PlainTable {
    String FIELD_SEPARATOR = "\t";
    ArrayList<ArrayList<String>> dataArray = new ArrayList<ArrayList<String>>();
    int rowsize = 0;
    int colsize = 0;
    HashMap<String, Integer> colnameMap = new HashMap<String, Integer>();
    ArrayList<String> colnameList = new ArrayList<String>();
    HashMap<String, Integer> rownameMap = new HashMap<String, Integer>();
    ArrayList<String> rownameList = new ArrayList<String>();

    public static void main(String[] args) {
        PlainTable t = new PlainTable();
        t.load(args[0], "\t", false, false);
    }

    public PlainTable() {
    }

    public void load(String filename, String separator) {
        load(filename, FIELD_SEPARATOR, false, false);
    }

    public Integer getRowNumber(String str) {
        return rownameMap.get(str);
    }

    public Integer getColNumber(String str) {
        return colnameMap.get(str);
    }

    public ArrayList getOneRow(int n) {
        return dataArray.get(n);
    }

    public ArrayList getOneRow(String str) {
        return getOneRow(getRowNumber(str).intValue());
    }

    public ArrayList getOneCol(int n) {
        ArrayList<Object> al = new ArrayList<Object>();
        Object tmp;
        for (int i = 0; i < rowsize; i++) {
            tmp = dataArray.get(i);
            if (tmp != null) {
                al.add(((ArrayList) tmp).get(n));
            } else {
                al.add(null);
            }
        }
        return al;
    }

    public Object get(int n, int m) {
        return ((ArrayList) dataArray.get(n)).get(m);
    }


    /**
     *
     */
    public ArrayList load(String filename, String separator, boolean rowname, boolean colname) {
        String str = "";
        int startcol = 0;
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));

            if (rowname == true) {
                startcol = 1;
            }
            String[] strvector;
            if (colname == true) {
                str = in.readLine();
                strvector = str.split(separator);
                for (int i = startcol; i < strvector.length; i++) {
                    colnameMap.put(strvector[i], new Integer(i - startcol));
                    colnameList.add(strvector[i]);
                }
            }
            int n = 0;
            int maxrowsize = 0;
            while ((str = in.readLine()) != null) {
                strvector = str.split(separator);
                ArrayList<String> v = new ArrayList<String>();
                if (rowname == true) {
                    rownameMap.put(strvector[0], new Integer(n));
                    rownameList.add(strvector[0]);
                }
                if (maxrowsize == 0)
                    maxrowsize = strvector.length;
                for (int i = startcol; i < strvector.length; i++) {
                    if (strvector[i].equals("")) {
                        v.add("NaN");
                    } else {
                        v.add(strvector[i]);
                    }
                }
                for (int i = strvector.length; i < maxrowsize; i++)
                    v.add("NaN");
                dataArray.add(v);
                n++;
            }
            colsize = dataArray.get(0).size();
            rowsize = dataArray.size();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found: " + e);
        } catch (IOException e) {
            System.out.println("File I/O Error: " + e);
        }
        return dataArray;
    }

    public int getRowSize() {
        return rowsize;
    }

    public int getColumnSize() {
        return colsize;
    }

    /**
     * @param i
     * @return
     */
    public String getColumnName(int i) {
        return colnameList.get(i);
    }

    /**
     * @param i
     * @return
     */
    public String getRowName(int i) {
        return rownameList.get(i);
    }

}
