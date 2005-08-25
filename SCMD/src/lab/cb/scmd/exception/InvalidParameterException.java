//--------------------------------------
// SCMDProject
// 
// InvalidParameterException.java 
// Since: 2004/07/13
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.exception;

/**
 * @author leo
 *
 */
public class InvalidParameterException extends SCMDException
{

    /**
     * 
     */
    public InvalidParameterException()
    {
        super();

    }

    /**
     * @param arg0
     */
    public InvalidParameterException(String arg0)
    {
        super(arg0);

    }

    /**
     * @param arg0
     */
    public InvalidParameterException(Throwable arg0)
    {
        super(arg0);

    }

    /**
     * @param arg0
     * @param arg1
     */
    public InvalidParameterException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);

    }

}


//--------------------------------------
// $Log: InvalidParameterException.java,v $
// Revision 1.1  2004/07/13 08:03:07  leo
// SCMD—p‚É—áŠO‚ð’Ç‰Á
//
//--------------------------------------