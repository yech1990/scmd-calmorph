//--------------------------------------
// SCMDProject
// 
// AllTests.java 
// Since: 2004/07/08
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author leo
 *
 */
public class AllTests
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for lab.cb.scmd.util.xml");
        //$JUnit-BEGIN$
        suite.addTestSuite(XMLOutputterTest.class);
        //$JUnit-END$
        return suite;
    }
}


//--------------------------------------
// $Log: AllTests.java,v $
// Revision 1.1  2004/07/08 08:24:30  leo
// TestSuiteÇÃç\ê¨Çå©íºÇµ
//
//--------------------------------------