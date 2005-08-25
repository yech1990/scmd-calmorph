// --------------------------------------
// SCMD Project
// 
// FlatTableTest.java
// Since: 2004/04/27
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;

import lab.cb.scmd.exception.SCMDException;
import junit.framework.TestCase;
import java.io.*;

/**
 * @author leo
 *  
 */
public class FlatTableTest extends TestCase
{

    /**
     * Constructor for FlatTableTest.
     * 
     * @param arg0
     */
    public FlatTableTest(String arg0)
    {
        super(arg0);
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    void createDataFile(String fileName, String[][] matrix)
    {
        try
        {
        	TabFormatter formatter = new TabFormatter(fileName);
        	formatter.outputMatrix(matrix);
        	formatter.close();
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    /*
     * Test for void FlatTable(String)
     */
    public void testFlatTableString()
    {
        String[][] array = { 
        		{ "a", "a1", "a2", "a3"}, 
        		{ "0", "1", "2", "3"},
                { "4.5", "NaN", "-1", "3"}, 
                { "5", "5", "3", "4"}};
        createDataFile("__samplefile1.txt", array);

        try
        {
            FlatTable f = new FlatTable("__samplefile1.txt"); // workfolder/test/FlatTabelTest/sample_file.txt
            /*
             * sample_file の中身 a a1 a2 a3 0 1 2 3 4.5 NaN -1 3 5 5 3 4
             */
            assertEquals(f.getRowSize(), 3);
            assertEquals(f.getColSize(), 4);
            assertTrue(f.getCell(0, "a").equals(new Cell("0")));
            assertTrue(f.getCell(1, "a").equals(new Cell("4.5")));
            assertTrue(f.getCell(2, "a").equals(new Cell("5")));
            assertTrue(f.getCell(0, "a1").equals(new Cell("1")));
            assertTrue(f.getCell(2, "a1").equals(new Cell("5")));
            assertTrue(f.getCell(0, "a2").equals(new Cell("2")));
            assertTrue(f.getCell(1, "a2").equals(new Cell("-1")));
            assertTrue(f.getCell(2, "a2").equals(new Cell("3")));
            assertTrue(f.getCell(0, "a3").equals(new Cell("3")));
            assertTrue(f.getCell(1, "a3").equals(new Cell("3")));
            assertTrue(f.getCell(2, "a3").equals(new Cell("4")));
            assertTrue(f.getCell(1, "a1").equals(new Cell("NaN")));

            TableIterator ti = f.getHorisontalIterator(0);
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("0")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("1")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("2")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("3")));
            assertFalse(ti.hasNext());

            ti = f.getHorisontalIterator(1);
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("4.5")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("NaN")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("-1")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("3")));
            assertFalse(ti.hasNext());

            ti = f.getVerticalIterator("a3");
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("3")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("3")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("4")));
            assertFalse(ti.hasNext());
            
        }
        catch (SCMDException e)
        {
            fail(e.getMessage());
        }
    }

    /*
     * Test for void FlatTableTrueTrue(String) File has both row index and column index
     */
    public void testFlatTableStringTrueTrue()
    {
        String[][] array = {         
        		{ "table", "a1", "a2", "a3"}, 
        		{ "r1", "1", "2", "3"},
                { "r2", "NaN", "-1", "3"}, 
                { "r3", "5", "3", "4"},
                { "r4", "2", "-3", "NaN"}
                };
        createDataFile("__samplefile2.txt", array);
    
        try
        {
            FlatTable f = new FlatTable("__samplefile2.txt", true, true); 
            assertEquals(f.getRowSize(), 4);
            assertEquals(f.getColSize(), 3);
            assertTrue(f.getCell("r1", "a1").equals(new Cell("1")));
            assertTrue(f.getCell("r2", "a1").equals(new Cell("NaN")));
            assertTrue(f.getCell("r3", "a1").equals(new Cell("5")));
            assertTrue(f.getCell("r4", "a1").equals(new Cell("2")));
            assertTrue(f.getCell("r1", "a2").equals(new Cell("2")));
            assertTrue(f.getCell("r2", "a2").equals(new Cell("-1")));
            assertTrue(f.getCell("r3", "a2").equals(new Cell("3")));
            assertTrue(f.getCell("r4", "a2").equals(new Cell("-3")));
            assertTrue(f.getCell("r1", "a3").equals(new Cell("3")));
            assertTrue(f.getCell("r2", "a3").equals(new Cell("3")));
            assertTrue(f.getCell("r3", "a3").equals(new Cell("4")));
            assertTrue(f.getCell("r4", "a3").equals(new Cell("NaN")));

            TableIterator ti = f.getHorisontalIterator("r1");
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("1")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("2")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("3")));
            assertFalse(ti.hasNext());

            ti = f.getHorisontalIterator("r4");
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("2")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("-3")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("NaN")));
            assertFalse(ti.hasNext());

            ti = f.getVerticalIterator("a3");
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("3")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("3")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("4")));
            assertTrue(ti.hasNext());
            assertTrue(((Cell) ti.nextCell()).equals(new Cell("NaN")));
            assertFalse(ti.hasNext());

            assertTrue( f.getColIndex("a1") == 0);
            assertTrue( f.getColIndex("a2") == 1);
            assertTrue( f.getColIndex("a3") == 2);
            assertTrue( f.getRowIndex("r1") == 0);
            assertTrue( f.getRowIndex("r2") == 1);
            assertTrue( f.getRowIndex("r3") == 2);
            assertTrue( f.getRowIndex("r4") == 3);
        }
        catch (SCMDException e)
        {
            fail(e.getMessage());
        }
    }

    public void testFlatTableStringTrueFalse()
    {}

    public void testFlatTableStringFalseTrue()
    {}
    
    public void testReadingTabSequence()
    {
        String[][] array = {         
        		{ "table", "a1", "a2", "a3"}, 
        		{ "r1",    "",   "",   "3"},
                { "r2",    "",   "-1", ""}, 
                { "r3",    "5",  "",   ""},
                };
        createDataFile("__samplefile3.txt", array);
    
        try
        {
            FlatTable f = new FlatTable("__samplefile3.txt", true, true); 
            assertEquals(f.getRowSize(), 3);
            assertEquals(f.getColSize(), 3);
            assertTrue(f.getCell("r1", "a1").equals(new Cell("")));
            assertTrue(f.getCell("r2", "a1").equals(new Cell("")));
            assertTrue(f.getCell("r3", "a1").equals(new Cell("5")));
            assertTrue(f.getCell("r1", "a2").equals(new Cell("")));
            assertTrue(f.getCell("r2", "a2").equals(new Cell("-1")));
            assertTrue(f.getCell("r3", "a2").equals(new Cell("")));
            assertTrue(f.getCell("r1", "a3").equals(new Cell("3")));
            assertTrue(f.getCell("r2", "a3").equals(new Cell("")));
            assertTrue(f.getCell("r3", "a3").equals(new Cell("")));
        }
        catch(SCMDException e)
        {
            fail(e.getMessage());
        }
    }
}

//--------------------------------------
// $Log: FlatTableTest.java,v $
// Revision 1.9  2004/07/27 05:18:12  leo
// TableIteratorの微調整
//
// Revision 1.8  2004/07/15 04:12:24  sesejun
// FlatTable を、BasicTable class を継承するように変更。
// BasicTable classを継承するクラスとして、AppendableTable classを作成。
// Appendable Table は、ファイルからではなく、自分で1行ずつ
// 追加していく Table 形式。
//
// Revision 1.7  2004/07/13 08:32:40  leo
// 読み込むファイル名を修正
//
// Revision 1.6  2004/07/13 08:31:17  leo
// テストのエラーを修正
//
// Revision 1.5  2004/07/13 08:30:26  leo
// 連続したtabを持つテーブル用のtestを追加
//
// Revision 1.4  2004/06/11 06:23:45  leo
// TestSuiteを作成
//
// Revision 1.3  2004/06/11 05:29:14  leo
// Cellクラスの
//
// Revision 1.2 2004/06/10 04:45:31 sesejun
// To handle row indexes
//
// Revision 1.1 2004/04/27 06:40:51 leo
// util.stat package test complete
//
//--------------------------------------
