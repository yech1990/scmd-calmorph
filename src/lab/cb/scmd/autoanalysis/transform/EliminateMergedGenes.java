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
    BasicTable morphTable = null;
    HashMap<String, String> mergeList = new HashMap<String, String> ();
    int ORFCOL = 0;
    int MEGCOL = 8;
    PrintStream out = System.out;
    PrintStream err = System.err;

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
        
        for( int i = 0; i < morphTable.getRowSize(); i++ ) {
            if( mergeList.containsKey(morphTable.getRowLabel(i)) ) {
                err.println("Merged: " +  morphTable.getRowLabel(i) + " to " + mergeList.get(morphTable.getRowLabel(i)));
                continue;
            }
            out.print(morphTable.getRowLabel(i));
            for( int j = 0; j < morphTable.getColSize(); j++ ) {
                out.print("\t" + morphTable.getCell(i, j).toString());
            }
            out.println();
        }
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
                    mergeList.put(aliasOrf, toOrf);
                }
            }
        } catch (SCMDException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
}
