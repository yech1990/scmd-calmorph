//--------------------------------------
// SCMD Project
// 
// NotEqual.java 
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
public class NotEqual extends ComparisonPredicate
{

    /**f
     * @param comparisonTarget
     */
    public NotEqual(Comparable comparisonTarget)
    {
        super(comparisonTarget);
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.algorithm.UnaryPredicate#isTrue(java.lang.Object)
     */
    public boolean isTrue(Object input)
    {
        return getComparisonTarget().compareTo(input) != 0;
    }

}


//--------------------------------------
// $Log: NotEqual.java,v $
// Revision 1.1  2004/06/24 02:09:14  leo
// ComparisonPredicateの完備
//
//--------------------------------------