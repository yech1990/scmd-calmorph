//--------------------------------------
//SCMD Project
//
//RankSumExactProbTable.java 
//Since:  2004/09/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.validparameter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


class RankSumExactProbTable {
    private HashMap<Integer, HashMap<Object, ArrayList<Double>>> tableList = new HashMap<Integer, HashMap<Object, ArrayList<Double>>>();

    RankSumExactProbTable() {
    }

    ArrayList<Double> getTable(int controlSize, int mutantSize) throws IOException {
        if (!tableList.containsKey(controlSize)) {
            tableList.put(controlSize, new HashMap<>());
        }
        HashMap<Object, ArrayList<Double>> table = tableList.get(controlSize);
        if (!table.containsKey(mutantSize)) {
            table.put(mutantSize, readExactProbTable(controlSize, mutantSize));
        }
        return table.get(mutantSize);
    }

    private ArrayList<Double> readExactProbTable(int controlSize, int mutantSize) throws IOException {
        String filename = "./table/table" + controlSize + "_" + mutantSize + ".xls";
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String buf = br.readLine();
        if (buf == null) {
            System.err.println("no input");
            System.exit(-1);
        }
        String[] line = buf.split("\t");
        if (controlSize != Integer.parseInt(line[0]) || mutantSize != Integer.parseInt(line[1])) {
            System.err.println("wrong exact probability table.");
            System.exit(-1);
        }
        ArrayList<Double> table = new ArrayList<>();
        while ((buf = br.readLine()) != null) {
            line = buf.split("\t");
            if (line.length < 2) break;
            table.add(Double.parseDouble(line[1]));
        }
        return table;
    }

}

//--------------------------------------
//$Log: RankSumExactProbTable.java,v $
//Revision 1.3  2004/12/09 10:06:27  nakatani
//*** empty log message ***
//
//Revision 1.2  2004/12/09 03:49:25  nakatani
//*** empty log message ***
//
//Revision 1.1  2004/12/09 03:26:07  nakatani
//*** empty log message ***
//
//--------------------------------------
