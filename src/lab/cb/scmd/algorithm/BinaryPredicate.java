//--------------------------------------
// SCMD Project
// 
// BinaryPredicate.java 
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
public interface BinaryPredicate<E, F> 
{
	public boolean isTrue(E o1, F o2);
}


//--------------------------------------
// $Log: BinaryPredicate.java,v $
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------