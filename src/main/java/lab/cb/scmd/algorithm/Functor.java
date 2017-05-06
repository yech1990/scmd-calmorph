//--------------------------------------
// SCMD Project
// 
// Functor.java 
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
public interface Functor<E>
{
    public void apply(E input);
}


//--------------------------------------
// $Log: Functor.java,v $
// Revision 1.1  2004/06/24 03:45:22  leo
// Algorithmに、count, equal,を追加
//
//--------------------------------------