//--------------------------------------
// SCMD Project
// 
// EliminateOnePercentOfBothSidesStrategy.java 
// Since:  2004/05/07
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.stat;

import lab.cb.scmd.util.table.Cell;
import lab.cb.scmd.util.table.TableIterator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;


/**
 * サンプルをソートして、両側1%を除去（1%が１未満の場合は、１に繰り上げ）したコレクションを
 * 返す戦略
 *
 * @author leo
 */
public class EliminateOnePercentOfBothSidesStrategy extends SampleFilteringStrategy {

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.stat.SampleFilteringStrategy#filter(lab.cb.scmd.util.table.TableIterator)
     */
    public Collection filter(TableIterator ti) {
        LinkedList list = new LinkedList();
        for (; ti.hasNext(); ) {
            Cell c = ti.nextCell();
            if (!getStatClass().isValidAsDouble(c))
                continue;
            list.add(new Double(c.doubleValue()));
        }
        Collections.sort(list);

        double onePercent = list.size() * 0.01;
        int numEliminates = (int) Math.floor(onePercent);
        numEliminates = numEliminates < 1 ? 1 : numEliminates;   // １未満なら１に繰上げ

        // listの両端から除去
        for (int i = 0; i < numEliminates && list.size() > 2; i++) {
            list.removeFirst();
            list.removeLast();
        }

        return list;
    }

}


//--------------------------------------
// $Log: EliminateOnePercentOfBothSidesStrategy.java,v $
// Revision 1.2  2004/07/27 05:18:12  leo
// TableIteratorの微調整
//
// Revision 1.1  2004/05/07 03:06:20  leo
// Statisticsクラスを、データのフィルタリングの戦略を切り替えられるように変更
//
//--------------------------------------