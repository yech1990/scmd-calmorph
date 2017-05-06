// --------------------------------------
// SCMD Project
// 
// TableFileName.java
// Since: 2004/04/27
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------
package lab.cb.scmd.autoanalysis.grouping;

/**
 * グループの名前とファイル名のsuffixの定義
 * 
 * @author leo
 *  
 */
public interface TableFileName
{
	int		GROUP_A				= 0;
	int		GROUP_A1B			= 1;
	int		GROUP_C				= 2;
	String	GROUP_NAME[]		= {"A", "A1B", "C"};
	String	GROUP_FILE_SUFFIX[]	= {"_A.xls", "_A1B.xls", "_C.xls"};
	
    public static final int T_ORF = 0;
    public static final int T_CONA_BASIC = 1;
    public static final int T_CONA_BIOLOGICAL = 2;
    public static final int T_ACTIN_BASIC = 3;
    public static final int T_ACTIN_BIOLOGICAL = 4;
    public static final int T_DAPI_BASIC  = 5;
    public static final int T_DAPI_BIOLOGICAL = 6;
    public static final int T_MAX = 7;
    public static final String[] TABLE_SUFFIX = new String[] { ".xls", "_conA_basic.xls", "_conA_biological.xls",
            "_actin_basic.xls", "_actin_biological.xls", "_dapi_basic.xls", "_dapi_biological.xls"};

}
//--------------------------------------
// $Log: TableFileName.java,v $
// Revision 1.3  2004/08/10 10:44:31  leo
// CellShapeStatの追加
//
// Revision 1.2  2004/04/30 06:00:55  leo
// temporary commit
//
// Revision 1.1 2004/04/27 16:01:08 leo
// グループ毎のファイル名を、TableFileName classに抽出
//
//--------------------------------------
