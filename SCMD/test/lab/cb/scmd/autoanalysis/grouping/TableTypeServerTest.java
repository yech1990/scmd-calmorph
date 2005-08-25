/*
 * Created on 2004/04/19
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package lab.cb.scmd.autoanalysis.grouping;


import junit.framework.TestCase;

/**
 * @author leo
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TableTypeServerTest extends TestCase
{

	/**
	 * Constructor for TableTypeServerTest.
	 * @param arg0
	 */
	public TableTypeServerTest(String arg0)
	{
		super(arg0);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		TableTypeServer.Initialize();		
	}
	
	public void testTableTypeServer() {
		assertTrue(TableTypeServer.getTypeMax() > 0);
		
		String tableTypeName[] = {"T_ORF", "T_CONA_BASIC", "T_CONA_BIOLOGICAL", 
			"T_ACTIN_BASIC", "T_ACTIN_BIOLOGICAL", "T_DAPI_BASIC", "T_DAPI_BIOLOGICAL" 
		};
		for (int i = 0; i < tableTypeName.length; i++) {
			int type = TableTypeServer.getTableType(tableTypeName[i]);
			assertEquals(tableTypeName[i], TableTypeServer.getTableTypeName(type));
		}
	}


}
