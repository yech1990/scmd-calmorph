//--------------------------------------
// SCMD Project
// 
// OptionParserTest.java 
// Since:  2004/04/21
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.cui;

import java.util.LinkedList;

import junit.framework.TestCase;
import lab.cb.scmd.exception.SCMDException;

/**
 * @author leo
 *
 */
public class OptionParserTest extends TestCase
{

	/**
	 * Constructor for OptionParserTest.
	 * @param arg0
	 */
	public OptionParserTest(String arg0)
	{
		super(arg0);
	}

	/*
	 * @see TestCase#setUp()
	 */
	private OptionParser parser = new OptionParser();
	final static int OPT_HELP = 0;
	final static int OPT_VERBOSE = 1;
	final static int OPT_OUTPUT_FILE = 2;
	final static int OPT_GUI_MODE = 3;
	final static int OPT_CUI_MODE = 4;
	final static int OPT_MODE = 5;

	protected void setUp() throws Exception
	{
		super.setUp();

		try
		{

			parser.setOption(new Option(OPT_HELP, "h", "help", "help message"));
			parser.setOption(new Option(OPT_VERBOSE, "v", "verbose", "display verbose messages"));
			parser.setOption(new OptionWithArgument(OPT_OUTPUT_FILE, "o", "", "FILE", "specify output file"));

			OptionGroup optGroup1 = new OptionGroup("GUI options", true);
			optGroup1.add(new Option(OPT_GUI_MODE, "g", "gui", "GUI mode"));
			parser.addOptionGroup(optGroup1);

			OptionGroup optGroup2 = new OptionGroup("CUI options", true);
			optGroup2.add(new Option(OPT_CUI_MODE, "c", "cui", "CUI mode"));
			parser.addOptionGroup(optGroup2);

			OptionGroup optGroup3 = new OptionGroup("other options");
			optGroup3.add(new OptionWithArgument(OPT_MODE, "m", "mode", "MODE", "compatible option"));
			parser.addOptionGroup(optGroup3);
		}
		catch (SCMDException e)
		{
			System.err.println(e.getMessage());
		}
	}

//	public void testCreateHelpMessage()
//	{
//		System.out.println(parser.createHelpMessage());
//	}

	public void testGetContext()
	{
		String args[] = { "-m", "3", "-vh", "inputfile.txt" };
		try
		{
			parser.getContext(args);
			assertTrue(parser.isSet(OPT_MODE));
			assertTrue(parser.isSet(OPT_VERBOSE));
			assertTrue(parser.isSet(OPT_HELP));
			assertFalse(parser.isSet(OPT_OUTPUT_FILE));
			assertTrue(parser.getValue(OPT_MODE).equals("3"));
			LinkedList argList = parser.getArgumentList();
			assertTrue(argList.size() == 1);
			assertTrue(((String) argList.get(0)).equals("inputfile.txt"));
		}
		catch (SCMDException e)
		{
			System.err.println(e.getMessage());
			fail("cannot reach here");
		}
	}

	public void testFindOption()
	{
		Option opt = parser.findOption(OPT_HELP);
		assertTrue(opt != null);
		assertEquals(opt.getOptionID(), OPT_HELP);
	}
	
	public void testDupilicateOption()
	{
		try{
		parser.setOption(new Option(OPT_HELP, "h", "help2", "another help"));
		fail("cannot reach here");
		}
		catch(SCMDException e)
		{
			// success
		}
	}
	
	public void testLongOptionArgument()
	{
		String args[] = { "--mode=mixed"};
		try
		{
			parser.getContext(args);
			assertTrue(parser.isSet(OPT_MODE));
			assertTrue(parser.getValue(OPT_MODE).equals("mixed"));
			LinkedList argList = parser.getArgumentList();
			assertTrue(argList.size() == 0);
		}
		catch (SCMDException e)
		{
			System.err.println(e.getMessage());
			fail("cannot reach here");
		}

	}
	
	public void testGroupCompatibility()
	{
		String args[] = { "--gui", "--cui"};
		try
		{
			parser.getContext(args);
			fail("OptionParser must recongnize that imcompatible options are set");
		}
		catch(SCMDException e)
		{
			//System.err.println(e.getMessage());
		}
	}
	
	public void testNonOptionRequirement()
	{
		String args[] = {};
		try
		{
			parser.setRequirementForNonOptionArgument();
			parser.getContext(args);
			fail("OptionParser must check whether a on-option argument exists");
		}
		catch(SCMDException e)
		{
			// success
		}
	}
	
	

}

//--------------------------------------
// $Log: OptionParserTest.java,v $
// Revision 1.3  2004/06/11 08:51:27  leo
// option でexclusive な異なるgroupに属するものを、
// 同時にセットしたときに例外を出せるようにした
//
// Revision 1.2  2004/04/30 02:25:52  leo
// OptionParserに引数の有無をチェックできる機能を追加
// setRequirementForNonOptionArgument()
//
// Revision 1.1  2004/04/22 04:08:46  leo
// first ship for /home/lab.cb.scmd/CVS
//
// Revision 1.1  2004/04/22 02:53:31  leo
// first ship of SCMDProject
//
// Revision 1.1  2004/04/21 05:35:05  leo
// add cui option reader (one test should be passed)
//
//--------------------------------------