//--------------------------------------
// SCMD Project
// 
// CalcGroupStatTest.java 
// Since:  2004/05/05
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.grouping;

import lab.cb.scmd.exception.SCMDException;
import junit.framework.TestCase;


/**
 * @author leo
 *
 */
public class CalcGroupStatTest extends TestCase
{
	CalcGroupStat c;
	protected void setUp() throws Exception
	{
		super.setUp();
		c = new CalcGroupStat();
	}

	public void testGetCVParameterName()
	{
		try
		{
			String cvName = c.getCVParameterName("A101_A");
			assertEquals("ACV101_A", c.getCVParameterName("A101_A"));
			assertEquals("CCV11-1_A", c.getCVParameterName("C11-1_A"));
		}
		catch(SCMDException e)
		{
			System.out.println(e.getMessage());
			fail("cannot reach here");
		}
	}
}


//--------------------------------------
// $Log: CalcGroupStatTest.java,v $
// Revision 1.1  2004/05/05 15:56:48  leo
// A1B, C_data.xlsŒvŽZ•”•ª‚ð’Ç‰Á
//
//--------------------------------------