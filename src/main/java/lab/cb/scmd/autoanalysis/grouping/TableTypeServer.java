//--------------------------------------
// SCMD Project
// 
// TableTypeServer.java 
// Since:  2004/04/19
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.grouping;

import java.util.HashMap;
import java.util.Objects;

/**
 * TableTypeServer内で使用
 *
 * @author leo
 */
class TableElement {
    private String _tableTypeName;
    private String _fileSuffix;

    TableElement(String tableTypeName, String fileSuffix) {
        _tableTypeName = tableTypeName;
        _fileSuffix = fileSuffix;
    }

    /**
     * @return
     */
    String getFileSuffix() {
        return _fileSuffix;
    }

    /**
     * @return
     */
    String getTableTypeName() {
        return _tableTypeName;
    }

}

/**
 * Tableのタイプ名(T_ORF, T_CONA_BASIC, etc.) と、実際のファイル名のsuffixとの
 * 対応を与えるクラス。グローバルからアクセスできるようにSingletonパターンを使用 使用前には必ずInitialize()しておくこと
 *
 * @author leo
 */

class TableTypeServer implements TableFileName {

    private static final TableElement[] tableElement = {new TableElement("T_ORF", ".xls"),
            new TableElement("T_CONA_BASIC", "_conA_basic.xls"),
            new TableElement("T_CONA_BIOLOGICAL", "_conA_biological.xls"),
            new TableElement("T_ACTIN_BASIC", "_actin_basic.xls"),
            new TableElement("T_ACTIN_BIOLOGICAL", "_actin_biological.xls"),
            new TableElement("T_DAPI_BASIC", "_dapi_basic.xls"),
            new TableElement("T_DAPI_BIOLOGICAL", "_dapi_biological.xls"), new TableElement("T_UNDEFINED", ""),};
    private static HashMap<String, Integer> _tableNameToTableType = new HashMap<>();
    static private TableTypeServer _instance = null;

    private TableTypeServer() {
        for (int i = 0; i < tableElement.length; i++) {
            _tableNameToTableType.put(tableElement[i].getTableTypeName(), i);
        }
    }

    static void Initialize() {
        _instance = new TableTypeServer();
    }

    static int getTableType(String tableName) {
        Integer tableTypeID = _tableNameToTableType.get(tableName);
        return Objects.requireNonNullElse(tableTypeID, -1);
    }

    static int getTypeMax() {
        return tableElement.length - 1;
    }

    static String getTableTypeName(int tableType) {
        if (tableType >= tableElement.length || tableType < 0)
            return tableElement[tableElement.length - 1].getTableTypeName();
        else
            return tableElement[tableType].getTableTypeName();
    }

    static TableElement getTableElement(int tableType) {
        return tableElement[tableType];
    }
}

//--------------------------------------
// $Log: TableTypeServer.java,v $
// Revision 1.4  2004/08/10 10:44:31  leo
// CellShapeStatの追加
//
// Revision 1.3 2004/07/20 07:51:00 sesejun
// SCMDServerから呼び出せるように、一部メソッドをpublicへ変更。
// AttributePositionを、TableSchemaから独立。
//
// Revision 1.2 2004/05/03 14:39:22 leo
// コメントを追加
//
// Revision 1.1 2004/04/23 02:24:25 leo
// move NucleusStageClassifier to lab.cb.scmd.autoanalysys.grouping
//
// Revision 1.1 2004/04/22 04:08:46 leo
// first ship for /home/lab.cb.scmd/CVS
//
// Revision 1.1 2004/04/22 02:53:31 leo
// first ship of SCMDProject
//
// Revision 1.4 2004/04/22 02:30:15 leo
// grouping complete
//
//--------------------------------------
