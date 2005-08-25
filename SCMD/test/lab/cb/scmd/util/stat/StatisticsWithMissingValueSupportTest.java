//--------------------------------------
// SCMD Project
// 
// StatisticsWithMissingValueSupportTest.java 
// Since:  2004/04/27
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.stat;

import java.io.IOException;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.table.FlatTable;
import lab.cb.scmd.util.table.TabFormatter;
import lab.cb.scmd.util.table.TableIterator;
import junit.framework.TestCase;

/**
 * @author leo
 *
 */
public class StatisticsWithMissingValueSupportTest extends TestCase
{

	/**
	 * Constructor for StatisticsWithMissingValueSupportTest.
	 * @param arg0
	 */
	public StatisticsWithMissingValueSupportTest(String arg0)
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
	
	public void testStatWithMissingValue()
	{
		try
		{
			FlatTable t = new FlatTable("__sample.txt");
			/* file contents
			a	b	c
			1	2	4
			1.0	3.3	.
			-1	NaN	1.5
			*/


			TableIterator ti = t.getHorisontalIterator(0);
			String missingValue[] = {".", "-1"};		
			Statistics s = new StatisticsWithMissingValueSupport(missingValue);



			// parent methods
			double ave = s.calcAverage(ti);
			assertEquals((double)(1+2+4)/3, ave, 0.01);
			double diffsquare = (double) ((1-ave) * (1-ave) + (2-ave) * (2-ave) + (4-ave)*(4-ave));
			double unbiasedvar = diffsquare / (3-1); 
			assertEquals(unbiasedvar, s.calcVariance(t.getHorisontalIterator(0)), 0.01);
			
			// missing value handling
			ave = (4 + 1.5) / 2;
			assertEquals(ave, s.calcAverage(t.getVerticalIterator("c")), 0.001);
			unbiasedvar = ((4-ave)*(4-ave) + (1.5-ave) * (1.5-ave)) / (2-1);
			assertEquals(unbiasedvar, s.calcVariance(t.getVerticalIterator("c")), 0.001);
			
			
//			String[] missing={"-1"};
//			StatisticsWithMissingValueSupport swmvs=new StatisticsWithMissingValueSupport(missing);
//			Cell c=new Cell(new Double(-1));
//			assertTrue(swmvs.isMissingValue(c));
			//if(swmvs.isMissingValue(c)){
			//	System.out.println("Double(-1) is a missing value.");
			//}else{
			//	System.out.println("Double(-1) is not a missing value.");
			//}
		}
		catch(SCMDException e)
		{
			fail(e.getMessage() + "\n" + 
				"please check working directory is correctly set to workfolder/test/stat");
		}

	}

}


//--------------------------------------
// $Log: StatisticsWithMissingValueSupportTest.java,v $
// Revision 1.4  2004/06/11 06:23:45  leo
// TestSuiteÇçÏê¨
//
// Revision 1.3  2004/06/11 05:29:14  leo
// CellÉNÉâÉXÇÃ
//
// Revision 1.2  2004/05/28 19:44:21  nakatani
// assert(Double(-1) is a missing value)  when missingValue=="-1"
//
// Revision 1.1  2004/04/27 06:40:51  leo
// util.stat package test complete
//
//--------------------------------------