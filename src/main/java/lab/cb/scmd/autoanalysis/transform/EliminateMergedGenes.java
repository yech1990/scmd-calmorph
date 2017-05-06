//--------------------------------------
//SCMD Project
//
//AttributePosition.java 
//Since:  2005/02/28
//
//$URL: http://scmd.gi.k.u-tokyo.ac.jp/devel/svn/phenome/trunk/SCMD/src/lab/cb/scmd/autoanalysis/grouping/AttributePosition.java $ 
//$LastChangedBy: leo $ 
//--------------------------------------
package lab.cb.scmd.autoanalysis.transform;

import java.io.PrintStream;
import java.util.HashMap;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.io.NullPrintStream;
import lab.cb.scmd.util.table.BasicTable;
import lab.cb.scmd.util.table.FlatTable;

/**
 * @author sesejun
 *
 * Merge されたORFを削除するスクリプト
 * arg[0]: 形態データ
 * arg[1]: annotation_change.tab
 * ftp://ftp.yeastgenome.org/yeast/data_download/chromosomal_feature/annotation_change.tab
 * http://scmd-staff.gi.k.u-tokyo.ac.jp/staffpage/index.php?%B6%A6%CD%AD%2F%B2%F2%C0%CF%CD%D1%A5%D5%A5%A1%A5%A4%A5%EB
 *
 * Standard Outputに、ORFを削除したデータを、
 * Standard Error に、削除したORFのリストを出力
 */
public class EliminateMergedGenes {
    private BasicTable morphTable = null;
    private HashMap<String, String> changeList = new HashMap<String, String> ();
    private HashMap<String, String> changeReason = new HashMap<String, String> ();
    int ORFCOL = 0;
    int RESCOL = 1;
    int MEGCOL = 8;
    private PrintStream out = System.out; //new NullPrintStream();
    private PrintStream err = System.err;

    public static void main(String[] args) {
        EliminateMergedGenes emg = new EliminateMergedGenes();
        emg.load(args[0], args[1]);
        emg.process();
    }

    /**
     *
     */
    private void process() {
        out.print(morphTable.getTableName());
        for( int i = 0; i < morphTable.getColSize(); i++ ) {
            out.print("\t" + morphTable.getColLabel(i));
        }
        out.println();

        int count = 0;
        for( int i = 0; i < morphTable.getRowSize(); i++ ) {
            String label = morphTable.getRowLabel(i);
            if( changeList.containsKey(label) ) {
                err.println(morphTable.getRowLabel(i) + "\t" + changeList.get(morphTable.getRowLabel(i)) + "\tMerged");
                count++;
                continue;
            }
            if( changeReason.containsKey(label) &&
                    changeReason.get(label).equals("Deleted")) {
                err.println(morphTable.getRowLabel(i) + "\t" + "-" + "\tDeleted");
                count++;
                continue;
            }
//            if( changeReason.containsKey(label)) {
//                err.println(morphTable.getRowLabel(i) + "\t" + "null" + "\t" + changeReason.get(label));
//                count++;
//                continue;
//            }
            out.print(morphTable.getRowLabel(i));
            for( int j = 0; j < morphTable.getColSize(); j++ ) {
                out.print("\t" + morphTable.getCell(i, j).toString());
            }
            out.println();
        }
        err.print("# of eliminated genes: " + count);
    }

    /**
     * @param string
     * @param string2
     */
    private void load(String morphdata, String changelist) {
        try {
            morphTable = new FlatTable(morphdata, true, true);
            BasicTable changeTable = new FlatTable(changelist, false, false);

            for(int i = 0; i < changeTable.getRowSize(); i++ ) {
                String aliasOrf = changeTable.getCell(i, ORFCOL).toString();
                String toOrf = changeTable.getCell(i, MEGCOL).toString();
                if( toOrf.length() != 0 ) {
                    changeList.put(aliasOrf, toOrf);
                    changeList.put(toOrf,toOrf);
                }
                String[] res = changeTable.getCell(i, RESCOL).toString().split("\\|");
                changeReason.put(aliasOrf, res[1]);
            }
        } catch (SCMDException e) {
            e.printStackTrace();
        }

    }
}
