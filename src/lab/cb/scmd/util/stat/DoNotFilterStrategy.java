//--------------------------------------
// SCMD Project
// 
// DoNotFilterStrategy.java 
// Since:  2004/05/07
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.stat;

import java.util.Collection;
import java.util.LinkedList;

import lab.cb.scmd.util.table.Cell;
import lab.cb.scmd.util.table.TableIterator;


/** Validなdouble値を持つCellであれば、filterを掛けずにそのままDoubleのコレクションとして
 * 返す戦略
 * @author leo
 *
 */
public class DoNotFilterStrategy extends SampleFilteringStrategy
{

	/* (non-Javadoc)
	 * @see lab.cb.scmd.util.stat.SampleFilteringStrategy#filter(lab.cb.scmd.util.table.TableIterator)
	 */
	public Collection filter(TableIterator ti)
	{
		LinkedList list = new LinkedList();
		for(; ti.hasNext(); )
		{
			Cell c = (Cell) ti.nextCell();
			if(!getStatClass().isValidAsDouble(c))
				continue;			
			list.add(new Double(c.doubleValue()));
		}
		return list;
	}

}


//--------------------------------------
// $Log: DoNotFilterStrategy.java,v $
// Revision 1.2  2004/07/27 05:18:12  leo
// TableIteratorの微調整
//
// Revision 1.1  2004/05/07 03:06:20  leo
// Statisticsクラスを、データのフィルタリングの戦略を切り替えられるように変更
//
//--------------------------------------