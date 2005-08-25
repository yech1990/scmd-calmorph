//--------------------------------------
// SCMD Project
// 
// UnaryPredicate.java 
// Since:  2004/06/23
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

/**
 * @author leo
 *
 */
public interface UnaryPredicate<E> 
{
	public boolean isTrue(E input);
}


//--------------------------------------
// $Log: UnaryPredicate.java,v $
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------