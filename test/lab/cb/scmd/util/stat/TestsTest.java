/*
 * Created on 2004/09/08
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package lab.cb.scmd.util.stat;

import junit.framework.TestCase;

/**
 * @author sesejun
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestsTest extends TestCase {

    public void testRankSum() {
    }

    public void testCdfOfNormalDistribution() {
        TestsOfStatistics tests = new TestsOfStatistics();
        // 1sigma
        assertTrue( tests.cdfOfNormalDistribution(1, 0, 1) < 0.84135 );
        assertTrue( tests.cdfOfNormalDistribution(1, 0, 1) > 0.84134 );
        // 2sigma
        assertTrue( tests.cdfOfNormalDistribution(2, 0, 1) < 0.9773 );
        assertTrue( tests.cdfOfNormalDistribution(2, 0, 1) > 0.9772 );
    }

}
