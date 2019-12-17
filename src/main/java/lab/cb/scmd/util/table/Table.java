//--------------------------------------
// SCMD Project
// 
// TableBase.java 
// Since:  2004/04/23
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;

import lab.cb.scmd.exception.OutOfRangeException;

/**
 * @author leo
 */
public interface Table {
    String getTableName();

    Cell getCell(int row, int col);

    void setCell(Cell cell, int row, int col) throws OutOfRangeException;

    /**
     * 行数の上限を返す
     *
     * @return 行数の上限
     */
    int getRowSize();

    /**
     * 列数の上限を返す
     *
     * @return 列数の上限
     */
    int getColSize();
}


//--------------------------------------
// $Log: Table.java,v $
// Revision 1.6  2004/07/13 08:07:20  leo
// プロセスを起動してコマンドラインを実行するツールを導入
//
// Revision 1.5  2004/06/10 04:44:53  sesejun
// Add methods for handling row indexes
//
// Revision 1.4  2004/04/27 06:40:51  leo
// util.stat package test complete
//
// Revision 1.3  2004/04/26 06:56:56  leo
// temp commit
//
// Revision 1.2  2004/04/23 06:53:46  leo
// add vertical/horisontal table iterator
//
// Revision 1.1  2004/04/23 04:44:38  leo
// add stat/table utilities
//
//--------------------------------------