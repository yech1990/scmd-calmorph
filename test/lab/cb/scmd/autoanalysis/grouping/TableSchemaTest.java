/*
 * Created on 2004/04/19
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package lab.cb.scmd.autoanalysis.grouping;
import java.io.FileOutputStream;
import java.io.PrintStream;

import lab.cb.scmd.exception.SCMDException;

import junit.framework.TestCase;

/**
 * @author leo
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TableSchemaTest extends TestCase
{

	/**
	 * Constructor for TableSchemaTest.
	 * @param arg0
	 */
	public TableSchemaTest(String arg0)
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
		
		String [] schema = {
		"# schema of\ttable A\t",
		"# (attribute name, original\ttable, original attributename)",
		"image_number\tT_ORF\timage_number",
		"cell_\tT_ORF\tcell_id",
		"Agroup\tT_ORF\tAgroup",
		"C11-1_A\tT_CONA_BASIC\tC11-1",
		"C12-1_A\tT_CONA_BASIC\tC12-1",
		"C13_A\tT_CONA_BASIC\tC13",
		"C103_A\tT_CONA_BIOLOGICAL\tC103",
		"C104_A\tT_CONA_BIOLOGICAL\tC104",
		"C115_A\tT_CONA_BIOLOGICAL\tC115",
		"C126_A\tT_CONA_BIOLOGICAL\tC126",
		"C127_A\tT_CONA_BIOLOGICAL\tC127",
		"A7-1_A\tT_ACTIN_BASIC\tA7-1",
		"A8-1_A\tT_ACTIN_BASIC\tA8-1",
		"A101_A\tT_ACTIN_BIOLOGICAL\tA101",
		"A120_A\tT_ACTIN_BIOLOGICAL\tA120",
		"A121_A\tT_ACTIN_BIOLOGICAL\tA121",
		"A122_A\tT_ACTIN_BIOLOGICAL\tA122",
		"A123_A\tT_ACTIN_BIOLOGICAL\tA123",
		"D14-1_A\tT_DAPI_BASIC\tD14-1",
		"D15-1_A\tT_DAPI_BASIC\tD15-1",
		"D16-1_A\tT_DAPI_BASIC\tD16-1",
		"D17-1_A\tT_DAPI_BASIC\tD17-1",
		"D102_A\tT_DAPI_BIOLOGICAL\tD102",
		"D105_A\tT_DAPI_BIOLOGICAL\tD105",
		"D117_A\tT_DAPI_BIOLOGICAL\tD117",
		"D127_A\tT_DAPI_BIOLOGICAL\tD127",
		"D135_A\tT_DAPI_BIOLOGICAL\tD135",
		"D147_A\tT_DAPI_BIOLOGICAL\tD147",
		"D148_A\tT_DAPI_BIOLOGICAL\tD148",
		"D154_A\tT_DAPI_BIOLOGICAL\tD154",
		"D155_A\tT_DAPI_BIOLOGICAL\tD155",
		"D173_A\tT_DAPI_BIOLOGICAL\tD173",
		"D176_A\tT_DAPI_BIOLOGICAL\tD176",
		"D179_A\tT_DAPI_BIOLOGICAL\tD179",
		"D182_A\tT_DAPI_BIOLOGICAL\tD182",
		"D188_A\tT_DAPI_BIOLOGICAL\tD188",
		"D191_A\tT_DAPI_BIOLOGICAL\tD191",
		"D194_A\tT_DAPI_BIOLOGICAL\tD194"};
		
		PrintStream out =  new PrintStream(new FileOutputStream("_structA.txt"));
		for(int i=0; i<schema.length; i++)
		    out.println(schema[i]);
		out.close();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testLoadFromFile()
	{
		try
		{
			TableSchema schema = new TableSchema("_structA.txt");
			//schema.outputContents(System.out);
		}
		catch (SCMDException e)
		{
			fail(e.getMessage());
		}
	}

}
