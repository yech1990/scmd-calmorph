//--------------------------------------
// SCMD Project
// 
// CalMorphtable.java 
// Since:  2004/04/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.grouping;


import java.io.*;
import java.util.*;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.table.Cell;

/**
 * TAB区切りのテーブルを読み込むクラス
 * @author leo
 *
 */
public class CalMorphTable extends lab.cb.scmd.util.table.FlatTable {
	/**
	 * @param fileName
	 * @throws SCMDException
	 */
	public CalMorphTable(String fileName) throws SCMDException {
		super(fileName);
	}


	/**
	 * @param out
	 */
	public void outputTableLabels(PrintStream out) {
		Vector labelNameList = getColLabelList();
		for (int x = 0; x < getColSize() - 1; ++x) {
			out.print(labelNameList.get(x) + TAB);
		}
		out.print(labelNameList.get(getColSize() - 1) + NEWLINE);
	}

	public String getCellData(int row, String labelName) throws SCMDException {
		if(row < 0 || row >= getRowSize())
			throw new SCMDException("row " + row + " is out of range (max = " + getRowSize() + ")");
		int col = getColIndex(labelName);
		if(col == -1)
			throw new SCMDException("col " + labelName + " does not exist");
		return ((Cell) getCell(row, labelName)).toString();
	}
	

	static final private String NEWLINE = System.getProperty("line.separator");
	static final private String TAB = "\t";

}


//--------------------------------------
// $Log: CalMorphTable.java,v $
// Revision 1.3  2004/08/01 08:19:36  leo
// BasicTableにhasRowLabelを追加
// XMLOutputterで、java.io.writerを使えるように変更
// （JSPのwriterがjava.io.Writerの派生クラスのため)
//
// Revision 1.2  2004/07/20 07:51:00  sesejun
// SCMDServerから呼び出せるように、一部メソッドをpublicへ変更。
// AttributePositionを、TableSchemaから独立。
//
// Revision 1.1  2004/04/27 07:09:19  leo
// rename grouping.Table to grouping.CalMorphTable
//
// Revision 1.1  2004/04/23 02:24:25  leo
// move NucleusStageClassifier to lab.cb.scmd.autoanalysys.grouping
//
// Revision 1.1  2004/04/22 04:08:46  leo
// first ship for /home/lab.cb.scmd/CVS
//
// Revision 1.1  2004/04/22 02:53:31  leo
// first ship of SCMDProject
//
// Revision 1.3  2004/04/22 02:30:15  leo
// grouping complete
//
//--------------------------------------

