//--------------------------------------
// SCMDServer
//
// XMLException.java
// Since: 2004/12/15
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------
package lab.cb.scmd.util.xml;

import lab.cb.scmd.exception.SCMDException;

/**
 * @author leo
 */
class XMLException extends SCMDException {

    /**
     *
     */
    XMLException() {
        super();
    }

    /**
     * @param arg0
     */
    XMLException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    XMLException(Throwable arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    XMLException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}


//--------------------------------------
// $Log$
//--------------------------------------

