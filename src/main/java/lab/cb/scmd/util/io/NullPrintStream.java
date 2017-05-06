//--------------------------------------
// SCMD Project
// 
// NullPrintStream.java 
// Since:  2004/05/06
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.io;

import java.io.PrintStream;

/**  printしても何も実行しないクラス (verboseメッセージの出力切り替えに便利)
 * @author leo
 *
 */
public class NullPrintStream extends PrintStream
{

    /**
     * @param arg0
     */
    public NullPrintStream()
    {
        super(new NullOutputStream());
    }

   
}


//--------------------------------------
// $Log: NullPrintStream.java,v $
// Revision 1.1  2004/05/06 06:10:34  leo
// メッセージ出力用のNullPrintStreamを追加。
// 統計値計算部分は完了。出力部分はこれから
//
//--------------------------------------