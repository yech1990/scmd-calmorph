//--------------------------------------
// SCMD Project
// 
// LessThan.java 
// Since:  2004/06/24
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

/** input < comparisonTarget なら isTrue() = true となる
 * @author leo
 *
 */
public class LessThan extends ComparisonPredicate {
	
	public LessThan(Comparable comparisonTarget)
	{
		super(comparisonTarget);
	}
	/* (non-Javadoc)
	 * @see lab.cb.scmd.algorithm.UnaryPredicate#isTrue(java.lang.Object)
	 */
	public boolean isTrue(Object input) {
		return getComparisonTarget().compareTo(input) > 0;	
	}
}


//--------------------------------------
// $Log: LessThan.java,v $
// Revision 1.2  2004/06/24 02:09:14  leo
// ComparisonPredicateの完備
//
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------