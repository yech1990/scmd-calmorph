//--------------------------------------
// SCMD Project
// 
// NullOutputStream.java 
// Since:  2004/05/06
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.io;

import java.io.IOException;
import java.io.OutputStream;

/** NullPrintStreamで使用。 何も出力しないクラス
 * @author leo
 *
 */
public class NullOutputStream extends OutputStream
{

    /**
     * 
     */
    public NullOutputStream()
    {
        super();
        // do nothing
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    public void write(int arg0) throws IOException
    {
        // do nothing
    }

}


//--------------------------------------
// $Log: NullOutputStream.java,v $
// Revision 1.1  2004/05/06 06:10:34  leo
// メッセージ出力用のNullPrintStreamを追加。
// 統計値計算部分は完了。出力部分はこれから
//
//--------------------------------------