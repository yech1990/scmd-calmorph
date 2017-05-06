//------------------------------------
// SCMD Project
//  
// SCMDException.java 
// Since:  2004/04/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------
package lab.cb.scmd.exception;

import java.io.PrintStream;

/**
 * SCMD Projectでのコードで使う例外クラスのベース。
 * このクラスをextendsして、好みの例外をlab.cb.scmd.exceptionパッケージ内に作成してください。
 * @author leo
 */
public class SCMDException extends Exception {

	/**
	 * 
	 */
	public SCMDException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public SCMDException(String arg0) {
		super(arg0);
	}

	/** 可変長の引数をとるconstructor. メッセージ間にはスペースが入る
	 * @param message 
	 * @TODO 可変長引数の使い方を調べてから実装
	 */
//	public SCMDException(String message1, String... message)
//	{
//	    super(message1 + )
//	}
//	
//	static protected String concatinateStrings(Object... s)
//	{
//	    StringBuffer buffer = new StringBuffer();
//	    for))
//	    buffer.append(s);
//	    return buffer.toString();
//	}

	
	/**
	 * @param arg0
	 */
	public SCMDException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SCMDException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	
	/** 例外メッセージを出力する
	 * @param outputStream 出力先 (System.errなど)
	 */
	public void what(PrintStream outputStream)
	{
		String m = this.getMessage();
		if(m != null)
		{
			outputStream.println(m);
		}
	}
	
	/** 例外メッセージをSystem.errに出力する
	 */
	public void what()
	{
		what(System.err);
	}

}


//--------------------------------------
// $Log: SCMDException.java,v $
// Revision 1.2  2004/05/05 16:27:50  leo
// コマンドラインのparse時のエラー(LackOfArgumentException)を追加。
// SCMDExceptionに、エラー表示の簡便化のためwhat()メソッドを追加。
//
// Revision 1.1  2004/04/22 04:08:46  leo
// first ship for /home/lab.cb.scmd/CVS
//
// Revision 1.1  2004/04/22 02:53:31  leo
// first ship of SCMDProject
//
// Revision 1.2  2004/04/22 02:30:15  leo
// grouping complete
//
// Revision 1.1  2004/04/19 09:20:44  leo
// first ship
//
// Revision 1.1  2004/04/16 09:28:46  leo
// add exception class & table class
//
//--------------------------------------