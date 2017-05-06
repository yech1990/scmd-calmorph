//--------------------------------------
// SCMD Project
// 
// OutOfRangeException.java 
// Since:  2004/04/27
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.exception;


/**
 * @author leo
 *
 */
public class OutOfRangeException extends SCMDException
{

	/**
	 * 
	 */
	public OutOfRangeException()
	{
		super();
	}

	/**
	 * @param arg0
	 */
	public OutOfRangeException(String arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public OutOfRangeException(Throwable arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public OutOfRangeException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

}


//--------------------------------------
// $Log: OutOfRangeException.java,v $
// Revision 1.1  2004/07/13 08:03:07  leo
// SCMD用に例外を追加
//
// Revision 1.2  2004/05/03 15:21:35  leo
// *** empty log message ***
//
// Revision 1.1  2004/04/27 06:40:51  leo
// util.stat package test complete
//
//--------------------------------------