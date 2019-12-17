//--------------------------------------
//SCMD Project
//
//AttributePosition.java 
//Since:  2004/04/19
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.grouping;

import lab.cb.scmd.exception.SCMDException;

/**
 * （テーブル、パラメータ名）という情報を持つ
 *
 * @author leo
 */
public class AttributePosition {

    public AttributePosition(int tableType, String attributeName) throws SCMDException {
        if (_tableType >= TableTypeServer.getTypeMax())
            throw new SCMDException("invalid table type: " + tableType);
        _tableType = tableType;
        _attributeName = attributeName;
    }

    public int getTableType() {
        return _tableType;
    }

    public String getAttributeName() {
        return _attributeName;
    }

    public String toString() {
        return "(" + TableTypeServer.getTableTypeName(_tableType) + ", "
                + _attributeName + ")";
    }

    private int _tableType;

    private String _attributeName;
}

