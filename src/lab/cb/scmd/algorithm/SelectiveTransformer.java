//--------------------------------------
// SCMD Project
// 
// SelectiveTransformer.java 
// Since:  2004/06/24
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

/** isTrue = trueとなる入力に対してのみ、transformを行いたい時に使う
 * また、その際、isTrueで計算した結果を再利用してtransformを実行できるようにするためのクラス
 * @author leo
 *
 */
public interface SelectiveTransformer extends UnaryPredicate, Transformer 
{
	/* (non-Javadoc)
	 * @see lab.cb.scmd.algorithm.UnaryPredicate#isTrue(java.lang.Object)
	 */
	public boolean isTrue(Object target);
	/* (non-Javadoc)
	 * @see lab.cb.scmd.algorithm.Transformer#transform(java.lang.Object)
	 */
	public Object transform(Object object);
}


//--------------------------------------
// $Log: SelectiveTransformer.java,v $
// Revision 1.2  2004/06/24 01:15:25  leo
// abstract classから、interfaceに変更
//
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------