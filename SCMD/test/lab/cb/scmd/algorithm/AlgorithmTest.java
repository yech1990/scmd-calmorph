//--------------------------------------
// SCMD Project
// 
// AlgorithmTest.java 
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
public class AlgorithmTest extends TestCase {
	
	Vector<Integer> _integerSet;
	
	void validateAnswer(int[] answer, Collection result)
	{
	    Iterator it = result.iterator();
	    for(int i=0; i<answer.length; i++)
	    {
	        assertTrue(it.hasNext());
	        assertTrue(new Integer(answer[i]).equals(it.next()));
	    }
	}
	void validateAnswer(double[] answer, Collection result)
	{
	    Iterator it = result.iterator();
	    for(int i=0; i<answer.length; i++)
	    {
	        assertTrue(it.hasNext());
	        assertTrue(new Double(answer[i]).equals(it.next()));
	    }
	}
	void validateAnswer(String[] answer, Collection result)
	{
	    Iterator it = result.iterator();
	    for(int i=0; i<answer.length; i++)
	    {
	        assertTrue(it.hasNext());
	        assertTrue(answer[i].equals(it.next()));
	    }
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		_integerSet = new Vector<Integer>();
		int[] intArray = { 1, 2, 5, 10, -1, 4, 8 };
		for(int i=0; i<intArray.length; i++)
			_integerSet.add(intArray[i]);
	}
	
	public void testSelect() {
		Vector result = new Vector();
		Algorithm.select(_integerSet, result, new LessThan(new Integer(5)));
		int[] answerArray = { 1, 2, -1, 4 };
		validateAnswer(answerArray, result);
	}

	public void testTransform() {
		class Incrementer implements Transformer<Integer, Integer>{
			public Integer transform(Integer i)
			{
				return new Integer(i + 1);
			}
		}
		Vector result = new Vector();
		Algorithm.transform(_integerSet, result, new Incrementer());
		int[] answerArray = { 2, 3, 6, 11, 0, 5, 9 };
		validateAnswer(answerArray, result);

		// use of anonymous class
		LinkedList result2 = new LinkedList();
		Algorithm.transform(_integerSet, result2, 
		        new Transformer() 
		        { 
		    		public Object transform(Object input) {
		    		    return new Integer(((Integer) input).intValue() - 1);
		    		}
		        });
		int [] answerArray2 = { 0, 1, 4, 9, -2, 3, 7 };
		validateAnswer(answerArray2, result2);
	}
	
	public void testSelectiveTransform() {
	    Vector result = new Vector();
	    Algorithm.selectiveTransform(_integerSet, result, 
	            new SelectiveTransformer()
	            {
	        		int power = 0;
	        		public boolean isTrue(Object input){
	        		    int intVal = ((Integer) input).intValue();
	        		    power = intVal * intVal;
	        		    return power > 25;
	        		}
	        		public Object transform(Object input){
	        		    // reuse previous calculation result in isTrue
	        		    return new Integer(power);
	        		}	        	
	            });
	    int [] answerArray = { 100, 64 };
	    validateAnswer(answerArray, result);
	}
	
	public void testInitializeCollection_int()
	{
	    int[] initValue = { 0, 1, 3, 4, 10 };
	    Vector v = new Vector();
	    Algorithm.initializeCollection(v, initValue);
	    validateAnswer(initValue, v);
	}
	
	public void testInitializeCollection_double()
	{
	    double[] initValue = { 0.3, 1.1, 3.4, 4.123, 10.11 };
	    Vector v = new Vector();
	    Algorithm.initializeCollection(v, initValue);
	    validateAnswer(initValue, v);
	}
	
	public void testInitializeCollection_String()
	{
	    String[] initValue = { "april", "paris", "dog", "cat" };
	    Vector v = new Vector();
	    Algorithm.initializeCollection(v, initValue);
	    validateAnswer(initValue, v);
	}
	
	public void testEqual_int()
	{
	    int[] intValue = { 1, 43, 2, 34};
	    Vector v = new Vector();
	    Algorithm.initializeCollection(v, intValue);
	    
	    assertTrue(Algorithm.equal(v, intValue));	    
	}

	public void testEqual_double()
	{
	    double[] initValue = { 0.3, 1.1, 3.4, 4.123, 10.11 };
	    Vector v = new Vector();
	    Algorithm.initializeCollection(v, initValue);
	    assertTrue(Algorithm.equal(v, initValue));
	}

	public void testEqual_String()
	{
	    String[] initValue = { "april", "paris", "dog", "cat" };
	    Vector v = new Vector();
	    Algorithm.initializeCollection(v, initValue);
	    
	    assertTrue(Algorithm.equal(v, initValue));	    
	}
	
}


//--------------------------------------
// $Log: AlgorithmTest.java,v $
// Revision 1.4  2004/06/24 03:45:22  leo
// Algorithmに、count, equal,を追加
//
// Revision 1.3  2004/06/24 03:00:17  leo
// *** empty log message ***
//
// Revision 1.2  2004/06/24 02:08:18  leo
// 無名クラスでalgorithmを利用する方法を記載
// initializerのテストを追加
//
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------