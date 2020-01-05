//--------------------------------------
// SCMD Project
// 
// StatisticsWithMissingValueSupport.java 
// Since:  2004/04/23
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.stat;

import lab.cb.scmd.util.table.Cell;

import java.util.Collections;
import java.util.HashSet;

/**
 * 欠損値のStringを無視できるように拡張したStatisticsクラス
 * StatisticsWithMissingValueSuport({"-1", "."}) など
 * 欠損値は、Double(-1).toString != "-1" なので注意
 *
 * @author leo
 */
public class StatisticsWithMissingValueSupport extends Statistics {
    /**
     * @param missingValueList 欠損値として扱う文字列のリスト
     */
    public StatisticsWithMissingValueSupport(String[] missingValueList) {
        super();
        setMissingValues(missingValueList);
    }

    /**
     * ユーザーが定義したfilterがサンプルにかかるようになる
     *
     * @param missingValueList  欠損値として扱う文字列のリスト
     * @param filteringStrategy filterの種類
     */
    public StatisticsWithMissingValueSupport(String[] missingValueList, SampleFilteringStrategy filteringStrategy) {
        super(filteringStrategy);
        setMissingValues(missingValueList);
    }

    protected void setMissingValues(String[] missingValueList) {
        Collections.addAll(_missingValueSet, missingValueList);
    }

    protected boolean isValidAsString(Cell cell) {
        boolean isValid = super.isValidAsString(cell);
        return isValid && !(isMissingValue(cell));
    }

    protected boolean isValidAsDouble(Cell cell) {
        boolean isValid = super.isValidAsDouble(cell);
        return isValid && !(isMissingValue(cell));
    }

    protected boolean isMissingValue(Cell cell) {
        String stringValue = cell.toString();
        return _missingValueSet.contains(stringValue);
    }

    HashSet<String> _missingValueSet = new HashSet<>();
}


//--------------------------------------
// $Log: StatisticsWithMissingValueSupport.java,v $
// Revision 1.10  2004/06/12 13:34:09  leo
// 欠損値の比較の処理を１．７のものに戻しました。
//
// Revision 1.9  2004/06/11 05:29:14  leo
// Cellクラスの
//
// Revision 1.8  2004/05/28 19:45:46  nakatani
// assert(Double(-1) is a missing value) when missingValue=="-1"
//
// Revision 1.7  2004/05/07 03:06:20  leo
// Statisticsクラスを、データのフィルタリングの戦略を切り替えられるように変更
//
// Revision 1.6  2004/05/06 06:10:34  leo
// メッセージ出力用のNullPrintStreamを追加。
// 統計値計算部分は完了。出力部分はこれから
//
// Revision 1.5  2004/04/30 02:25:52  leo
// OptionParserに引数の有無をチェックできる機能を追加
// setRequirementForNonOptionArgument()
//
// Revision 1.4  2004/04/27 06:40:51  leo
// util.stat package test complete
//
// Revision 1.3  2004/04/26 06:57:50  leo
// modify supports for validating double values
//
// Revision 1.2  2004/04/23 06:08:41  leo
// *** empty log message ***
//
// Revision 1.1  2004/04/23 05:56:56  leo
// temporary commit
//
//--------------------------------------