//--------------------------------------
// SCMD Project
// 
// AllTests.java 
// Since:  2004/06/11
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;

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
        TestSuite suite = new TestSuite("Test for lab.cb.scmd.util.table");
        //$JUnit-BEGIN$
        suite.addTestSuite(FlatTableTest.class);
        //$JUnit-END$
        return suite;
    }
}


//--------------------------------------
// $Log: AllTests.java,v $
// Revision 1.1  2004/06/11 06:23:45  leo
// TestSuiteÇçÏê¨
//
//--------------------------------------