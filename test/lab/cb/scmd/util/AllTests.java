//--------------------------------------
// SCMDProject
// 
// AllTests.java 
// Since: 2004/07/08
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util;

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
        TestSuite suite = new TestSuite("Test for lab.cb.scmd.util");
        //$JUnit-BEGIN$
        suite.addTest(lab.cb.scmd.util.cui.AllTests.suite());
        suite.addTest(lab.cb.scmd.util.stat.AllTests.suite());
        suite.addTest(lab.cb.scmd.util.table.AllTests.suite());
        suite.addTest(lab.cb.scmd.util.xml.AllTests.suite());
        //$JUnit-END$
        return suite;
    }
}


//--------------------------------------
// $Log: AllTests.java,v $
// Revision 1.2  2004/07/21 02:51:56  leo
// AllTestsÇ…lab.cb.scmd.util.xmlÇí«â¡
//
// Revision 1.1  2004/07/08 08:24:30  leo
// TestSuiteÇÃç\ê¨Çå©íºÇµ
//
//--------------------------------------