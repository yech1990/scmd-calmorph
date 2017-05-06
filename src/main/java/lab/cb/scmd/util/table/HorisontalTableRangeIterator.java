//--------------------------------------
// SCMD Project
// 
// HorisontalTableRange.java 
// Since:  2004/04/23
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;

/**
 * @author leo
 *
 */
public class HorisontalTableRangeIterator implements TableIterator
{
	/**一行をフルにトレースするItratorを作成
	 * @param table
	 * @param rowIndex iteratorがたどる行
	 */
	HorisontalTableRangeIterator(Table table, int rowIndex)
	{
		_table = table;
		_rowIndex = rowIndex;
		_colCursor = -1;
		_colIndexBegin = 0;
		_colIndexEnd = table.getColSize() - 1;
	}
	
	/** 一行の特定の範囲内を動くIterator
	 * @param table
	 * @param rowIndex iteratorがたどる行
	 * @param colIndexBegin 開始列番号
	 * @param colIndexEnd 終了列番号
	 */
	HorisontalTableRangeIterator(Table table, int rowIndex, int colIndexBegin, int colIndexEnd)
	{
		_table = table;
		_rowIndex = rowIndex;
		_colCursor = colIndexBegin - 1;
		_colIndexBegin = colIndexBegin;
		_colIndexEnd = colIndexEnd - 1;		
	}

	/* (non-Javadoc)
	 * @see lab.cb.scmd.util.table.TableIterator#hasNext()
	 */
	public boolean hasNext()
	{
		return _colCursor < _colIndexEnd;
	}

	/* (non-Javadoc)
	 * @see lab.cb.scmd.util.table.TableIterator#next()
	 */
	public Cell nextCell()
	{
		return _table.getCell(_rowIndex, ++_colCursor);
	}

    
	public Object clone()
	{
		HorisontalTableRangeIterator it = 
			new HorisontalTableRangeIterator(_table, _rowIndex);
		it._colCursor = this._colCursor;
		it._colIndexBegin  = this._colIndexBegin;
		it._colIndexEnd = this._colIndexEnd;
		return it;
	}
	
	int _colCursor;
	Table _table;
	int _rowIndex;
	int _colIndexBegin;
	int _colIndexEnd;
    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next() {
        return nextCell();
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.table.TableIterator#row()
     */
    public int row() {
        return _rowIndex;
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.table.TableIterator#col()
     */
    public int col() {
        return _colCursor;
    }	
}


//--------------------------------------
// $Log: HorisontalTableRangeIterator.java,v $
// Revision 1.7  2004/08/10 10:44:31  leo
// CellShapeStatの追加
//
// Revision 1.6  2004/08/02 09:55:42  leo
// *** empty log message ***
//
// Revision 1.5  2004/07/27 05:18:12  leo
// TableIteratorの微調整
//
// Revision 1.4  2004/04/27 06:40:51  leo
// util.stat package test complete
//
// Revision 1.3  2004/04/23 06:53:46  leo
// add vertical/horisontal table iterator
//
// Revision 1.2  2004/04/23 05:56:56  leo
// temporary commit
//
// Revision 1.1  2004/04/23 04:44:38  leo
// add stat/table utilities
//
//--------------------------------------