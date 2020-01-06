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

import lab.cb.scmd.util.table.TableIterator;

import java.util.Collection;


/**
 * テーブルを辿って得られるSampleの集合に、どのようにフィルターをかけるか
 * 定義するためのstrategy (StrategyPattern)
 * setStatClassで、個々の戦略を使うStatisticsクラスを定義しておく必要がある
 *
 * @author leo
 */
abstract public class SampleFilteringStrategy {
    private Statistics _statClass;

    abstract public Collection filter(TableIterator ti);

    /**
     * このstrategyを使っているStatisticsクラス本体の機能を使うためのクラス
     * isValidCellの判定など。
     *
     * @return
     */
    Statistics getStatClass() {
        return _statClass;
    }

    /**
     * @param statClass この戦略をつかうStatisticsクラス
     */
    void setStatClass(Statistics statClass) {
        _statClass = statClass;
    }
}


//--------------------------------------
// $Log: SampleFilteringStrategy.java,v $
// Revision 1.1  2004/05/07 03:06:20  leo
// Statisticsクラスを、データのフィルタリングの戦略を切り替えられるように変更
//
//--------------------------------------