//--------------------------------------
// SCMD Project
// 
// VerticalTableRangeIterator.java 
// Since:  2004/04/23
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;

/**
 * @author leo
 */
public class VerticalTableRangeIterator implements TableIterator {
    private Table _table;
    private int _rowCursor;
    private int _colIndex;
    private int _rowIndexBegin;
    private int _rowIndexEnd;

    VerticalTableRangeIterator(Table table, int colIndex) {
        _table = table;
        _colIndex = colIndex;
        _rowCursor = -1;
        _rowIndexBegin = 0;
        _rowIndexEnd = table.getRowSize() - 1;
    }

    VerticalTableRangeIterator(Table table, int colIndex, int rowIndexBegin, int rowIndexEnd) {
        _table = table;
        _colIndex = colIndex;
        _rowCursor = rowIndexBegin - 1;
        _rowIndexBegin = rowIndexBegin;
        _rowIndexEnd = rowIndexEnd - 1;
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.table.TableIterator#hasNext()
     */
    public boolean hasNext() {
        return _rowCursor < _rowIndexEnd;
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.table.TableIterator#next()
     */
    public Cell nextCell() {
        return _table.getCell(++_rowCursor, _colIndex);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        VerticalTableRangeIterator vi = new VerticalTableRangeIterator(_table, _colIndex);
        vi._rowCursor = this._rowCursor;
        vi._rowIndexBegin = this._rowIndexBegin;
        vi._rowIndexEnd = this._rowIndexEnd;
        return vi;
    }

    public int row() {
        return _rowCursor;
    }

    public int col() {
        return _colIndex;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next() {
        return nextCell();
    }
}


//--------------------------------------
// $Log: VerticalTableRangeIterator.java,v $
// Revision 1.5  2004/08/10 10:44:31  leo
// CellShapeStatの追加
//
// Revision 1.4  2004/08/02 09:55:42  leo
// *** empty log message ***
//
// Revision 1.3  2004/07/27 05:18:12  leo
// TableIteratorの微調整
//
// Revision 1.2  2004/04/27 06:40:51  leo
// util.stat package test complete
//
// Revision 1.1  2004/04/23 06:53:46  leo
// add vertical/horisontal table iterator
//
//--------------------------------------