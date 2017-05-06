//--------------------------------------
// SCMD Project
// 
// LessThanOrEqual.java 
// Since:  2004/06/24
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

/**
 * @author leo
 *
 */
public class LessThanOrEqual extends ComparisonPredicate {
	
	public LessThanOrEqual(Comparable compareTarget)
	{
		super(compareTarget);
	}
	/* (non-Javadoc)
	 * @see lab.cb.scmd.algorithm.UnaryPredicate#isTrue(java.lang.Object)
	 */
	public boolean isTrue(Object input) {
		return getComparisonTarget().compareTo(input) >= 0;	
	}
}


//--------------------------------------
// $Log: LessThanOrEqual.java,v $
// Revision 1.2  2004/06/24 02:09:14  leo
// ComparisonPredicateの完備
//
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------