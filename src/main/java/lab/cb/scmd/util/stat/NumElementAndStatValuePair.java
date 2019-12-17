// --------------------------------------
// SCMD Project
// 
// NumElementAndStatValuePair.java
// Since: 2004/05/07
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.stat;

/**
 * 要素の数と、計算結果のペア
 *
 * @author leo
 */
public class NumElementAndStatValuePair {

    public NumElementAndStatValuePair(int numElement_, double value_) {
        numElement = numElement_;
        value = value_;
    }


    public int numElement;
    public double value;

    /**
     * @return Returns the numElement.
     */
    public int getNumElement() {
        return numElement;
    }

    /**
     * @return Returns the value.
     */
    public double getValue() {
        return value;
    }
}

//--------------------------------------
// $Log: NumElementAndStatValuePair.java,v $
// Revision 1.1  2004/05/07 04:30:26  leo
// 時間計測用クラスを追加
//
//--------------------------------------
