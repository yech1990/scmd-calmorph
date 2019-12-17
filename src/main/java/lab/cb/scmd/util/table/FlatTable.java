// --------------------------------------
// SCMD Project
// 
// FlatTable.java
// Since: 2004/04/26
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;

import lab.cb.scmd.exception.SCMDException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.LinkedList;
import java.util.Vector;

/**
 * @author leo
 */
public class FlatTable extends BasicTable {
    public FlatTable(String fileName) throws SCMDException {
        this(fileName, false, true);
    }

    public FlatTable(String fileName, boolean rowindex, boolean colindex) throws SCMDException {
        loadFromFile(fileName, rowindex, colindex);
    }


    public FlatTable(File file) throws SCMDException {
        this(file, false, true);
    }

    public FlatTable(File file, boolean rowindex, boolean colindex) throws SCMDException {
        loadFromFile(file.getPath(), rowindex, colindex);
    }

    public void appendFromFile(String fileName, boolean rowindex, boolean colindex) throws SCMDException {
        // load from tab-delimited file
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(fileName));

            String line = "";
            int curcolumn = 0;
            // read labels
            boolean columnSizeFlag = false;
            if (colindex == true) {
                line = fileReader.readLine();
            } else {
            }

            // read data
            LinkedList rowLabelList = new LinkedList();
            while ((line = fileReader.readLine()) != null) {
                if (columnSizeFlag == false) {
                    _colSize = line.split(DELIMITER).length;
                }
                readAndSetOneRow(rowindex, line, rowLabelList);
            }
            if (rowindex == true) {
                setRowLabel(rowLabelList);
            }
            fileReader.close();
        } catch (IOException e) {
            throw new SCMDException("error occured while reading " + fileName
                    + "\n" + e.getMessage());
        }
    }

    protected void loadFromFile(String fileName, boolean rowindex, boolean colindex) throws SCMDException {
        // load from tab-delimited file
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(
                    fileName));

            String line = "";
            int curcolumn = 0;
            // read labels
            boolean columnSizeFlag = false;
            if (colindex == true) {
                line = fileReader.readLine();
                loadAndSetColumnIndex(rowindex, line, curcolumn);
                columnSizeFlag = true;
            } else {
                columnSizeFlag = false;
            }

            // read data
            LinkedList rowLabelList = new LinkedList();
            while ((line = fileReader.readLine()) != null) {
                if (columnSizeFlag == false) {
                    _colSize = line.split(DELIMITER).length;
                    if (rowindex == true)
                        _colSize--;
                }
                readAndSetOneRow(rowindex, line, rowLabelList);
            }
            if (rowindex == true) {
                setRowLabel(rowLabelList);
            }
            fileReader.close();
        } catch (IOException e) {
            throw new SCMDException("error occured while reading " + fileName
                    + "\n" + e.getMessage());
        }
    }

    /**
     * @param rowindex
     * @param line
     * @param rowLabelList
     * @throws SCMDException
     */
    private void readAndSetOneRow(boolean rowindex, String line, LinkedList rowLabelList) throws SCMDException {
        Vector row = new Vector(_colSize);

        String[] tokens = line.split(DELIMITER);

        int columnno = 0;
        if (rowindex == true) {
            if (tokens.length > 0) {
                String token = tokens[columnno++];
                rowLabelList.add(trimDoubleQuotation(token));
            } else
                throw new SCMDException("invalid row labels");
        }
        for (int i = columnno; i < tokens.length; i++) {
            String token = tokens[i];
            row.add(new Cell(token));
        }
        while (row.size() < _colSize) {
            row.add(new Cell());
        }
        _rows.add(row);
    }

    /**
     * @param rowindex
     * @param line
     * @param curcolumn
     * @throws SCMDException
     */
    private void loadAndSetColumnIndex(boolean rowindex, String line, int curcolumn) throws SCMDException {
        if (line == null) throw new SCMDException("invalid column labels");
        String[] tokens = line.split(DELIMITER);
        if (rowindex == true) {
            if (tokens.length <= 0)
                throw new SCMDException("invalid table name");
            setTableName(tokens[curcolumn++]);
        }
        LinkedList colLabelList = new LinkedList();
        for (int i = curcolumn; i < tokens.length; i++) {
            String token = tokens[i];
            colLabelList.add(trimDoubleQuotation(token));
        }
        setColLabel(colLabelList);
    }

    /**
     * @param labelList StringのCollection (Vector, LinkedList, etc.)
     */
    public FlatTable(AbstractCollection labelList) {
        setColLabel(labelList);
    }

    String trimDoubleQuotation(String str) {
        if (str.startsWith("\"")) {
            if (str.endsWith("\""))
                return str.substring(1, str.length() - 1);
            else
                return str;
        } else
            return str;
    }

}

//--------------------------------------
// $Log: FlatTable.java,v $
// Revision 1.12  2004/09/09 04:49:10  sesejun
// add appendFromFile
//
// Revision 1.11  2004/07/21 07:25:30  sesejun
// columnのlabelが無いときに列数を０にしてしまう
// バグを修正
//
// Revision 1.10  2004/07/15 04:12:24  sesejun
// FlatTable を、BasicTable class を継承するように変更。
// BasicTable classを継承するクラスとして、AppendableTable classを作成。
// Appendable Table は、ファイルからではなく、自分で1行ずつ
// 追加していく Table 形式。
//
// Revision 1.9  2004/07/14 00:30:22  sesejun
// StringTokenizerを、splitに置き換えた。
// StringTokenizerが、DELIMITER2つが連続する場合、
// 真ん中のcellを無視してしまう仕様を避けるため。
// 避けるため。
//
// Revision 1.8  2004/06/15 12:43:01  sesejun
// Add get{Row,Col}Label method
//
// Revision 1.7  2004/06/10 04:44:53  sesejun
// Add methods for handling row indexes
//
// Revision 1.6  2004/05/07 03:06:20  leo
// Statisticsクラスを、データのフィルタリングの戦略を切り替えられるように変更
//
// Revision 1.5  2004/05/06 09:27:32  leo
// FlatTableでラベルを読み込んだとき、double quotationを取り除くようにした。
// Excelマクロから生成されたファイルは、たまにdouble quoationつきでパラメータ名が出力されるため
//
// Revision 1.4 2004/05/06 06:10:34 leo
// メッセージ出力用のNullPrintStreamを追加。
// 統計値計算部分は完了。出力部分はこれから
//
// Revision 1.3 2004/04/27 07:09:19 leo
// rename grouping.Table to grouping.CalMorphTable
//
// Revision 1.2 2004/04/27 06:40:51 leo
// util.stat package test complete
//
// Revision 1.1 2004/04/26 06:56:56 leo
// temp commit
//
//--------------------------------------
