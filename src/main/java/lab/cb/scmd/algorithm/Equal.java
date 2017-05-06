//--------------------------------------
// SCMD Project
// 
// Equal.java 
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
public class Equal extends ComparisonPredicate
{

    /**
     * @param comparisonTarget
     */
    public Equal(Comparable comparisonTarget)
    {
        super(comparisonTarget);
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.algorithm.UnaryPredicate#isTrue(java.lang.Object)
     */
    public boolean isTrue(Object input)
    {
        return getComparisonTarget().compareTo(input) == 0;
    }

}


//--------------------------------------
// $Log: Equal.java,v $
// Revision 1.1  2004/06/24 02:09:14  leo
// ComparisonPredicateの完備
//
//--------------------------------------