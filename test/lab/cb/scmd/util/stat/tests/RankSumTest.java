//--------------------------------------
//SCMD Project
//
//RankSumTest.java 
//Since:  2004/09/08
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------
package lab.cb.scmd.util.stat.tests;

import java.io.IOException;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.table.FlatTable;
import lab.cb.scmd.util.table.TabFormatter;
import lab.cb.scmd.util.table.TableIterator;
import junit.framework.TestCase;

public class RankSumTest extends TestCase {
    
    public RankSumTest (String arg0) {
        super(arg0);
    }

    public void testRankSum() {
		try
		{
		   TabFormatter formatter = new TabFormatter("__sample.txt");
	        String[][] matrix = { 
	                { "a", "182", "169", "173", "143", "158", "156", "176", "165"},
//	        		{ "a", "163", "142", "174", "137", "151", "143", "180", "162"},
	        		{ "b", "163", "142", "174", "137", "151", "143", "180", "162"}
//	                { "a", "1", "2", "3"},
//	                { "b", "2", "3", "4"}
	                };
		   formatter.outputMatrix(matrix);
		   formatter.close();
		}
		catch(IOException e)
		{
		    fail(e.getMessage());
		}
		try {
            FlatTable t = new FlatTable("__sample.txt", true, false);
//            RankSum rankSum = new RankSum(t.getVerticalIterator(0), t.getVerticalIterator(1));
            RankSum rankSum = new RankSum(t.getRow("a"), t.getRow("b"));
            double wr = rankSum.willcoxconR();
            assertEquals((int)(wr * 100000), 120774);
            System.out.println(wr);
        } catch (SCMDException e) {
            e.printStackTrace();
        }

    }
    
    public void testChiSquare() {
        
    }

}
