//--------------------------------------
// SCMD Project
// 
// AllTests.java 
// Since:  2004/04/20
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.grouping;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author leo
 *
 */
public class AllTests {

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(AllTests.class);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for lab.cb.scmd.autoanalysis");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(TableSchemaTest.class));
		suite.addTest(new TestSuite(TableTypeServerTest.class));
		suite.addTest(new TestSuite(CalcGroupStatTest.class));
		//$JUnit-END$
		return suite;
	}
}


//--------------------------------------
// $Log: AllTests.java,v $
// Revision 1.2  2004/06/11 06:23:45  leo
// TestSuiteÇçÏê¨
//
// Revision 1.1  2004/04/23 02:33:15  leo
// move test codes to autoanalysis.grouping
//
// Revision 1.1  2004/04/22 04:08:46  leo
// first ship for /home/lab.cb.scmd/CVS
//
// Revision 1.1  2004/04/22 02:53:31  leo
// first ship of SCMDProject
//
// Revision 1.1  2004/04/20 08:17:32  leo
// grouping completed
//
//--------------------------------------