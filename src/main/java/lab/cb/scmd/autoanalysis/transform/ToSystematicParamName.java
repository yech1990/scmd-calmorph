/*
 * Created on 2005/02/25
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package lab.cb.scmd.autoanalysis.transform;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.table.FlatTable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

/**
 * @author sesejun
 * <p>
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ToSystematicParamName {

    private String DELIMITER = "\t";
    private PrintStream out = System.out;
    private int OLDNAMECOL = 1;
    private int SYSNAMECOL = 10;

    // args[0] : original
    // args[1] : db hash
    public static void main(String[] args) {
        ToSystematicParamName syspara = new ToSystematicParamName();
        syspara.process(args);
    }

    /**
     * @param args
     */
    private void process(String[] args) {
        String origfile = args[0];
        String hashfile = args[1];
        HashMap<String, String> toSysnameMap = new HashMap<>();

        try {
            FlatTable pt = new FlatTable(hashfile, false, false);
            for (int row = 0; row < pt.getRowSize(); row++) {
                String oldName = pt.getCell(row, OLDNAMECOL).toString();
                String sysName = pt.getCell(row, SYSNAMECOL).toString();
                toSysnameMap.put(oldName, sysName);
            }
            BufferedReader fileReader = new BufferedReader(new FileReader(origfile));
            String line;
            line = fileReader.readLine();
            String[] header = line.split(DELIMITER);
            for (int i = 0; i < header.length; i++) {
                if (i != 0)
                    out.print("\t");
                if (toSysnameMap.containsKey(header[i]))
                    out.print(toSysnameMap.get(header[i]));
                else
                    out.print(header[i]);
            }
            out.println();

            while ((line = fileReader.readLine()) != null) {
                out.print(line);
                out.println();
            }
        } catch (IOException | SCMDException e) {
            e.printStackTrace();
        }
    }
}
