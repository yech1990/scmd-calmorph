//--------------------------------------
// SCMD Project
// 
// StatisticsTest.java 
// Since:  2004/04/23
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.stat;

import java.io.IOException;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.table.*;
import junit.framework.TestCase;

/**
 * @author leo
 *
 */
public class StatisticsTest extends TestCase
{

	/**
	 * Constructor for StatisticsTest.
	 * @param arg0
	 */
	public StatisticsTest(String arg0)
	{
		super(arg0);
	}
	
	protected void setUp() throws Exception
	{
	    super.setUp();
		try
		{
		   TabFormatter formatter = new TabFormatter("__sample.txt");
	        String[][] matrix = { 
	        		{ "a", "b", "c"}, 
	        		{ "1", "2", "4"},
	                { "1.0", "3.3", "."}, 
	                { "-1", "NaN", "1.5"}};
		   formatter.outputMatrix(matrix);
		   formatter.close();
		}
		catch(IOException e)
		{
		    fail(e.getMessage());
		}
	}
	
	public void testStat()
	{
		try
		{
			FlatTable t = new FlatTable("__sample.txt");
			TableIterator ti = t.getHorisontalIterator(0);
			Statistics s = new Statistics();
			double ave = s.calcAverage(ti);
			assertEquals((double)(1+2+4)/3, ave, 0.01);
			double diffsquare = (double) ((1-ave) * (1-ave) + (2-ave) * (2-ave) + (4-ave)*(4-ave));
			double unbiasedvar = diffsquare / (3-1); 
			assertEquals(unbiasedvar, s.calcVariance(t.getHorisontalIterator(0)), 0.01);
		}
		catch(SCMDException e)
		{
			fail(e.getMessage() + "\n" + 
				"please check working directory is correctly set to workfolder/test/stat");
		}

	}

}


//--------------------------------------
// $Log: StatisticsTest.java,v $
// Revision 1.3  2004/06/11 06:23:45  leo
// TestSuiteÇçÏê¨
//
// Revision 1.2  2004/04/27 06:40:51  leo
// util.stat package test complete
//
// Revision 1.1  2004/04/23 04:44:38  leo
// add stat/table utilities
//
//--------------------------------------