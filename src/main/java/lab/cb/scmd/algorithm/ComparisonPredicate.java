//--------------------------------------
// SCMD Project
// 
// ComparisonPredicate.java 
// Since:  2004/06/24
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

/**
 * Comparableの実装(Integer, Double, etc.) を与えて、
 * たとえば、 Integer と Integer同士の比較を行うための functor
 *
 * @author leo
 */
public abstract class ComparisonPredicate implements UnaryPredicate {
    public ComparisonPredicate(Comparable comparisonTarget) {
        _comparisonTarget = comparisonTarget;
    }

    protected Comparable getComparisonTarget() {
        return _comparisonTarget;
    }

    Comparable _comparisonTarget;
}


//--------------------------------------
// $Log: ComparisonPredicate.java,v $
// Revision 1.1  2004/06/24 02:09:14  leo
// ComparisonPredicateの完備
//
// Revision 1.2  2004/06/24 01:47:06  leo
// コメントを追加
//
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------