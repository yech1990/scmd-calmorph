//--------------------------------------
// SCMD Project
// 
// AllTests.java 
// Since:  2004/06/24
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author leo
 *
 */
public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for lab.cb.scmd.algorithm");
		//$JUnit-BEGIN$
		suite.addTestSuite(AlgorithmTest.class);
		suite.addTestSuite(ComparisonPredicateTest.class);
		suite.addTestSuite(RegexPredicateTest.class);
		//$JUnit-END$
		return suite;
	}
}


//--------------------------------------
// $Log: AllTests.java,v $
// Revision 1.3  2004/06/24 02:28:41  leo
// 正規表現用のPredicateのテストを追加
//
// Revision 1.2  2004/06/24 02:09:14  leo
// ComparisonPredicateの完備
//
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------