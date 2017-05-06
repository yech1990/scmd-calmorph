//--------------------------------------
// SCMD Project
// 
// SampleFilteringStrategy.java 
// Since:  2004/05/07
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.stat;

import java.util.Collection;
import lab.cb.scmd.util.table.TableIterator;


/** テーブルを辿って得られるSampleの集合に、どのようにフィルターをかけるか
 * 定義するためのstrategy (StrategyPattern)
 * setStatClassで、個々の戦略を使うStatisticsクラスを定義しておく必要がある
 * @author leo
 *
 */
abstract public class SampleFilteringStrategy
{
	/** 
	 * @param statClass この戦略をつかうStatisticsクラス
	 */
	public void setStatClass(Statistics statClass)
	{
		_statClass = statClass;
	}
	
	abstract public Collection filter(TableIterator ti);
	
	/**このstrategyを使っているStatisticsクラス本体の機能を使うためのクラス
	 * isValidCellの判定など。
	 * @return
	 */
	protected Statistics getStatClass() { return _statClass; }	
	
	Statistics _statClass;
}


//--------------------------------------
// $Log: SampleFilteringStrategy.java,v $
// Revision 1.1  2004/05/07 03:06:20  leo
// Statisticsクラスを、データのフィルタリングの戦略を切り替えられるように変更
//
//--------------------------------------