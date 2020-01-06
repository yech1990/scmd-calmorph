//--------------------------------------
// SCMD Project
// 
// TabFormatter.java 
// Since:  2004/06/11
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 * Tab区切りのテキスト（ファイル）を出力するためのツール
 *
 * @author leo
 */
public class TabFormatter {
    private PrintStream _out;

    public TabFormatter() {
        _out = System.out;
    }

    public TabFormatter(String outputFile) throws FileNotFoundException {
        _out = new PrintStream(new FileOutputStream(outputFile));
    }

    public void outputMatrix(String[][] matrix) {
        for (String[] strings : matrix) {
            int col = 0;
            for (; col < strings.length - 1; col++) {
                _out.print(strings[col] + "\t");
            }
            _out.println(strings[col]);
        }
    }

    public void close() {
        _out.close();
    }
}


//--------------------------------------
// $Log: TabFormatter.java,v $
// Revision 1.1  2004/06/11 06:23:45  leo
// TestSuiteを作成
//
//--------------------------------------