// --------------------------------------
// SCMD Project
// 
// AppendableTableTest.java
// Since: 2004/07/14
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.table;

import junit.framework.TestCase;

/**
 * @author leo
 *  
 */
public class AppendableTableTest extends TestCase
{

    /**
     * Constructor for FlatTableTest.
     * 
     * @param arg0
     */
    public AppendableTableTest()
    {
        super();
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

    /*
     * Test for void FlatTable(String)
     */
    public void testFlatTableString()
    {
        String[] columnName = { "a", "a1", "a2", "a3"}; 
        String[][] array = { 
        		{ "0", "1", "2", "3"},
                { "4.5", "NaN", "-1", "3"}, 
                { "5", "5", "3", "4"}};

            AppendableTable f = new AppendableTable("test table", columnName);
            for( int i = 0; i < array.length; i++ ) {
                f.append(array[i]);
            }
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

    /*
     * Test for void FlatTableTrueTrue(String) File has both row index and column index
     */
    public void testFlatTableStringTrueTrue()
    {
        String[]   columnName = { "a1", "a2", "a3"}; 
        String[][] array = {         
        		{ "r1", "1", "2", "3"},
                { "r2", "NaN", "-1", "3"}, 
                { "r3", "5", "3", "4"},
                { "r4", "2", "-3", "NaN"}
                };

            AppendableTable f = new AppendableTable("test table", columnName, true); 
            for( int i = 0; i < array.length; i++ ) {
                f.append(array[i]);
            }

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
    }

    public void testFlatTableStringTrueFalse()
    {}

    public void testFlatTableStringFalseTrue()
    {}
    
}

//--------------------------------------
// $Log: AppendableTableTest.java,v $
// Revision 1.2  2004/07/27 05:18:12  leo
// TableIteratorの微調整
//
// Revision 1.1  2004/07/15 04:12:24  sesejun
// FlatTable を、BasicTable class を継承するように変更。
// BasicTable classを継承するクラスとして、AppendableTable classを作成。
// Appendable Table は、ファイルからではなく、自分で1行ずつ
// 追加していく Table 形式。
//
//--------------------------------------
