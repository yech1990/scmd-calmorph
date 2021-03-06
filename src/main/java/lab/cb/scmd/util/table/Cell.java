//--------------------------------------
// SCMD Project
// 
// TableCell.java 
// Since:  2004/04/23
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;


/**
 * @author leo
 */
public class Cell {
    private String _value = "";
    private double _dvalue = Double.NaN;
    private boolean _haveTriedToTransformToDouble = false;


    public Cell() {
        setValue("");
    }

    public Cell(String value) {
        setValue(value);
    }

    public Cell(Double dvalue) {
        setValue(dvalue);
    }

    public boolean equals(Cell other) {
        return this.toString().equals(other.toString());
    }

    public void setValue(String value) {
        _value = value;
        _dvalue = Double.NaN;
        _haveTriedToTransformToDouble = false;
    }

    public void setValue(Double value) {
        _dvalue = value;
        _value = value.toString();
        _haveTriedToTransformToDouble = true;
    }

    public double doubleValue() {
        if (_haveTriedToTransformToDouble)
            return _dvalue;
        else
            return transformToDouble();
    }

    public boolean isValidAsDouble() {
        if (_haveTriedToTransformToDouble)
            return (!Double.isNaN(_dvalue))
                    && (_dvalue != Double.NEGATIVE_INFINITY)
                    && (_dvalue != Double.POSITIVE_INFINITY);
        else {
            transformToDouble();
            return isValidAsDouble();
        }
    }

    public String toString() {
        return _value;
    }

    private double transformToDouble() {
        try {
            if (!_haveTriedToTransformToDouble) {
                _haveTriedToTransformToDouble = true;
                _dvalue = Double.parseDouble(_value);
            }
        } catch (NumberFormatException e) {
            _dvalue = Double.NaN;
        }
        return _dvalue;
    }

}

//--------------------------------------
// $Log: Cell.java,v $
// Revision 1.9  2004/09/02 02:48:04  nakatani
// NaNの判定をDouble.isNaN(_dvalue)に修正。
//
// Revision 1.8  2004/06/11 05:29:14  leo
// Cellクラスの
//
// Revision 1.7  2004/06/10 04:44:53  sesejun
// Add methods for handling row indexes
//
// Revision 1.6  2004/05/28 19:15:31  nakatani
// debug
// assert( Cell(new Double(-1))==Cell("-1") );
//
// Revision 1.5  2004/04/27 07:09:19  leo
// rename grouping.Table to grouping.CalMorphTable
//
// Revision 1.4  2004/04/27 06:40:51  leo
// util.stat package test complete
//
// Revision 1.3  2004/04/26 06:56:56  leo
// temp commit
//
// Revision 1.2  2004/04/23 05:56:56  leo
// temporary commit
//
// Revision 1.1  2004/04/23 04:44:38  leo
// add stat/table utilities
//
//--------------------------------------