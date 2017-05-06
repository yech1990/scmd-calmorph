//--------------------------------------
// SCMD Project
// 
// Transformer.java 
// Since:  2004/06/24
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

/** E型からF型に変換するtransformerのinterface
 * @author leo
 *
 */
public interface Transformer<E, F> 
{
	public F transform(E object);
}


//--------------------------------------
// $Log: Transformer.java,v $
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------