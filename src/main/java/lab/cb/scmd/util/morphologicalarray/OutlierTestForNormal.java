//--------------------------------------
// SCMD Project
// 
// OutlierTest.java 
// Since:  2004/08/30
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.morphologicalarray;

/**
 * @author nakatani
 */
public class OutlierTestForNormal {
    private Double[] data;
    private double E;
    //Double[] criticalValue;
    private double SD;
    private double MAX;
    private double MIN;
    public OutlierTestForNormal(Double[] givenData) {
        data = givenData;
        getEandSD(data);
    }

    /**
     * critical value のテーブルをセットする。
     * 将来nサンプルの場合に拡張する時のため。
     *
     * @author nakatani
     */
    private void setCriticalValue() {
    }

    /**
     * 与えられたサンプル数に対するcritical value を返す。
     * とりあえずサンプル数１２６か１２７の場合だけに対応。
     * （実際にはcriticalValueはサンプル数１２０の時の値。本当は補間して3.68ぐらいか？）
     *
     * @author nakatani
     */
    private double getCriticalValue(int nSample) {
		/*if(nSample<126||127<nSample){
			System.err.println("error. サンプル数"+nSample+"に対するcritical valueを知りません。");
			System.exit(-1);
		}*/
        //return 3.66;
        return 3.0902;//upper 0.001
        //return 3.7190;//upper 0.0001
    }

    /**
     * 平均値と標準偏差、max,minも計算する。
     *
     * @author nakatani
     */
    private void getEandSD(Double[] data) {
        if (data.length < 2) {
            System.err.println("Error in OutlierTestForNormal.getEandSD() data.length=" + data.length);
            System.exit(-1);
        }
        double tmp_exp = 0;
        double sum_of_squares = 0;
        int n = 0;
        MAX = data[0];
        MIN = MAX;
        for (int i = 0; i < data.length; ++i) {
            double x = data[i];
            if (MAX < x) MAX = x;
            if (x < MIN) MIN = x;
            x -= tmp_exp;
            tmp_exp += x / (double) (i + 1);
            sum_of_squares += i * x * x / (i + 1);
        }
        sum_of_squares = Math.sqrt(sum_of_squares / (data.length - 1));

        E = tmp_exp;//expectation
        SD = (sum_of_squares == 0.0) ? -1 : sum_of_squares;//standard deviation
    }


    private double N1_testOfUpperOutlier(double x) {
        //if(x<MAX)return 0.0;
        return (x - E) / SD;
    }

    private double N1_testOfLowerOutlier(double x) {
        //if(MIN<x)return 0.0;
        return (E - x) / SD;
    }

    public boolean isUpperOutlier(double x) {
        return N1_testOfUpperOutlier(x) >= getCriticalValue(data.length);
    }

    public boolean isLowerOutlier(double x) {
        return N1_testOfLowerOutlier(x) >= getCriticalValue(data.length);
    }
}


//--------------------------------------
// $Log: OutlierTestForNormal.java,v $
// Revision 1.3  2004/09/18 23:10:18  nakatani
// *** empty log message ***
//
// Revision 1.2  2004/09/03 06:02:09  nakatani
// *** empty log message ***
//
// Revision 1.1  2004/08/29 20:22:38  nakatani
// MorphologicalArrayのための、検定クラスと画像出力クラス。
//
//--------------------------------------