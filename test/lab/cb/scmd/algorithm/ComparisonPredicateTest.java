//--------------------------------------
// SCMD Project
// 
// ComparisonPredicateTest.java 
// Since:  2004/06/24
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

import java.util.*;
import junit.framework.TestCase;

/**
 * @author leo
 *
 */
public class ComparisonPredicateTest extends TestCase {
	public void testLess() {
		ComparisonPredicate less = new LessThan(new Integer(5));
		assertTrue(less.isTrue(new Integer(3)));
		assertFalse(less.isTrue(new Integer(10)));
		assertFalse(less.isTrue(new Integer(5)));
	}
	public void testLessThanOrEqual() {
		ComparisonPredicate leq = new LessThanOrEqual(new Integer(5));
		assertTrue(leq.isTrue(new Integer(3)));
		assertFalse(leq.isTrue(new Integer(10)));
		assertTrue(leq.isTrue(new Integer(5)));	
		
		// Stringの大小比較にも使えます		
		ComparisonPredicate sleq = new LessThanOrEqual("hello");
		assertTrue(sleq.isTrue(new String("apple")));
		assertFalse(sleq.isTrue(new String("hello world")));
		assertTrue(sleq.isTrue(new String("hello")));
	}
	
	public void testGreaterThan() {
		ComparisonPredicate gt = new GreaterThan(new Integer(5));
		assertFalse(gt.isTrue(new Integer(3)));
		assertTrue(gt.isTrue(new Integer(10)));
		assertFalse(gt.isTrue(new Integer(5)));				
	}
	
	public void testGreaterThanOrEqual() {
		ComparisonPredicate geq = new GreaterThanOrEqual(new Integer(5));
		assertFalse(geq.isTrue(new Integer(3)));
		assertTrue(geq.isTrue(new Integer(10)));
		assertTrue(geq.isTrue(new Integer(5)));				
	}
	
	public void testEqual()
	{
		ComparisonPredicate eq = new Equal(new Integer(5));
		assertFalse(eq.isTrue(new Integer(3)));
		assertFalse(eq.isTrue(new Integer(10)));
		assertTrue(eq.isTrue(new Integer(5)));					    
	}

	public void testNotEqual()
	{
		ComparisonPredicate neq = new NotEqual(new Integer(5));
		assertTrue(neq.isTrue(new Integer(3)));
		assertTrue(neq.isTrue(new Integer(10)));
		assertFalse(neq.isTrue(new Integer(5)));					    
	}
	
	public void testNumericalComparisonPredicate() {
	    Vector input = new Vector();
	    int[] initValue = { 3, 5, -3, -4, 10 };
	    Algorithm.initializeCollection(input, initValue);
	    Vector result = new Vector();
	    Algorithm.select(input, result, 
	            new ComparisonPredicate(new Integer(3)) {
	        		public boolean isTrue(Object input){
	        		    return  this.getComparisonTarget().compareTo(input) < 0; // 3 < input ならresultに出力
	        		}
	    	});
	    
	    int[] answer = { 5, 10 };
	    Iterator it = result.iterator();
	    for(int i=0; i < answer.length; i++)
	    {
	        assertTrue(it.hasNext());
	        assertTrue(((Integer) it.next()).equals(new Integer(answer[i])));
	    }	    
	}
}


//--------------------------------------
// $Log: ComparisonPredicateTest.java,v $
// Revision 1.2  2004/06/24 03:00:17  leo
// *** empty log message ***
//
// Revision 1.1  2004/06/24 02:09:14  leo
// ComparisonPredicateの完備
//
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------