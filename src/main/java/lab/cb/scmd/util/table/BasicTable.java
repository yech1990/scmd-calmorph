//--------------------------------------
//SCMD Project
//
//FlatTable.java
//Since: 2004/07/14
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;

import lab.cb.scmd.exception.OutOfRangeException;

import java.util.*;

public class BasicTable implements Table {

    protected void setColLabel(AbstractCollection labelList) {
        Object[] labelArray = labelList.toArray();
        setColLabel(labelArray);
    }

    protected void setColLabel(Object[] labelArray) {
        _colSize = labelArray.length;
        for (int i = 0; i < _colSize; i++) {
            String colLabel = (String) labelArray[i];
            _colLabelToColIndexMap.put(colLabel, new Integer(i));
            _colLabel.add(colLabel);
        }
    }

    public void addColLabel(String colLabel) {
        _colLabelToColIndexMap.put(colLabel, new Integer(_colLabel.size()));
        _colLabel.add(colLabel);
    }

    public String getColLabel(int n) {
        return (String) _colLabel.get(n);
    }

    protected void setRowLabel(AbstractCollection labelList) {
        Object[] labelArray = labelList.toArray();
        setRowLabel(labelArray);
    }

    protected void setRowLabel(Object[] labelArray) {
        for (int i = 0; i < _rows.size(); i++) {
            String rowLabel = (String) labelArray[i];
            _rowLabelToRowIndexMap.put(rowLabel, new Integer(i));
            _rowLabel.add(rowLabel);
        }
        _hasRowLabel = true;
    }

    public void addRowLabel(String rowLabel) {
        _rowLabelToRowIndexMap.put(rowLabel, new Integer(_colLabel.size()));
        _rowLabel.add(rowLabel);
    }

    public String getRowLabel(int n) {
        return (String) _rowLabel.get(n);
    }

    /*
     * (non-Javadoc)
     *
     * @see lab.cb.scmd.util.table.Table#getCell(int, int)
     */
    public Cell getCell(int row, int col) {
        return isValidCoordinates(row, col) ? (Cell) ((Vector) _rows.get(row))
                .get(col) : null;
    }

    public Cell getCell(int row, String colLabel) {
        int col = getColIndex(colLabel);
        return col == -1 ? null : getCell(row, col);
    }

    public Cell getCell(String rowLabel, int col) {
        int row = getRowIndex(rowLabel);
        return row == -1 ? null : getCell(row, col);
    }

    public Cell getCell(String rowLabel, String colLabel) {
        int row = getRowIndex(rowLabel);
        int col = getColIndex(colLabel);
        return (col == -1 || row == -1) ? null : getCell(row, col);
    }

    public int getColIndex(String colLabel) {
        Integer col = (Integer) _colLabelToColIndexMap.get(colLabel);
        if (col == null)
            return -1;
        else
            return col.intValue();
    }

    public int getRowIndex(String rowLabel) {
        Integer row = (Integer) _rowLabelToRowIndexMap.get(rowLabel);
        if (row == null)
            return -1;
        else
            return row.intValue();
    }

    public void setCell(Cell cell, int row, int col) throws OutOfRangeException {
        if (isValidCoordinates(row, col)) {
            Vector hrzline = (Vector) _rows.get(row);
            hrzline.set(col, cell);
        } else {
            throw new OutOfRangeException("out of range: (" + row + ", " + col
                    + ")");
        }
    }

    protected boolean isValidCoordinates(int row, int col) {
        return row >= 0 && row < _rows.size() && col >= 0 && col < _colSize;
    }

    public final Vector getColLabelList() {
        return _colLabel;
    }

    public final Vector getRowLabelList() {
        return _rowLabel;
    }


    public TableIterator getHorisontalIterator(int rowIndex) {
        return new HorisontalTableRangeIterator(this, rowIndex);
    }

    public TableIterator getHorisontalIterator(String rowLabel) {
        int row = getRowIndex(rowLabel);
        if (row != -1)
            return new HorisontalTableRangeIterator(this, row);
        else
            return new HorisontalTableRangeIterator(this, 0, 0, 0);
    }

    /**
     * @param colIndex
     * @return
     */
    public TableIterator getVerticalIterator(int colIndex) {
        return new VerticalTableRangeIterator(this, colIndex);
    }

    public TableIterator getVerticalIterator(String colLabel) {
        int col = getColIndex(colLabel);
        if (col != -1)
            return new VerticalTableRangeIterator(this, col);
        else
            return new VerticalTableRangeIterator(this, 0, 0, 0);
    }

    public List getRow(String rowLabel) {
        TableIterator ti = getHorisontalIterator(rowLabel);
        LinkedList list = new LinkedList();
        for (; ti.hasNext(); ) {
            list.add(ti.nextCell());
        }
        return list;
    }

    public List getRow(int n) {
        TableIterator ti = getHorisontalIterator(n);
        LinkedList list = new LinkedList();
        for (; ti.hasNext(); ) {
            list.add(ti.nextCell());
        }
        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see lab.cb.scmd.util.table.Table#getRowLimit(int)
     */
    public int getRowSize() {
        return _rows.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see lab.cb.scmd.util.table.Table#getColLimit(int)
     */
    public int getColSize() {
        return _colSize;
    }

    /*
     * (non-Javadoc)
     *
     */
    public String getTableName() {
        return _tableName;
    }

    /*
     *
     */
    public String setTableName(String tableName) {
        String oldName = _tableName;
        _tableName = tableName;
        return oldName;
    }

    public boolean hasRowLabel() {
        return _hasRowLabel;
    }

    protected void setHasRowLabel(boolean flag) {
        _hasRowLabel = flag;
    }

    String _tableName = "";
    Vector _rows = new Vector();
    int _colSize = 0;
    Vector _colLabel = new Vector();
    Vector _rowLabel = new Vector();
    HashMap _colLabelToColIndexMap = new HashMap();
    HashMap _rowLabelToRowIndexMap = new HashMap();
    boolean _hasRowLabel = false;

    String DELIMITER = "\t";

    /*
     * (non-Javadoc)
     *
     * @see lab.cb.scmd.util.table.Table#setCell(lab.cb.scmd.util.table.Cell, int, int)
     */
}
//--------------------------------------
//$Log: BasicTable.java,v $
//Revision 1.7  2004/10/05 11:04:51  nakamu
//setCellで、中身を書き換えていなかったバグを修正
//
//--------------------------------------