//--------------------------------------
// SCMD Project
// 
// Algorithm.java 
// Since:  2004/06/23
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

import java.util.*;


/** Collectionに加工を加えるためのアルゴリズム群 (C++のSTL風)
 * @author leo
 *
 */
public class Algorithm 
{
	/** inputCollectionのうち、predicate.isTrue() が満たされるもののみを outputCollectionに追加する
	 * @param inputCollection
	 * @param outputCollection
	 * @param predicate
	 * @return
	 */
	static public Collection select(Collection inputCollection, Collection outputCollection, UnaryPredicate predicate)
	{
		for(Iterator it = inputCollection.iterator(); it.hasNext(); )
		{
			Object element = it.next();
			if(predicate.isTrue(element))
				outputCollection.add(element);
		}
		return outputCollection;
	}
	
	/** inputCollectionの要素それぞれに、transformer.transform() を適用して、結果をoutputCollectionに追加する
	 * @param inputCollection
	 * @param outputCollection
	 * @param transformer
	 * @return
	 */
	static public Collection transform(Collection inputCollection, Collection outputCollection, Transformer transformer)
	{
		for(Iterator it = inputCollection.iterator(); it.hasNext(); )
		{
			Object element = it.next();
			outputCollection.add(transformer.transform(element));
		}		
		return outputCollection;
	}
	
	/** inputCollectionの要素のうち、selectiveTransformer.isTrue() を満たすものに、selectiveTransformer.transform()を適用して、
	 * 結果をoutputCollectionに追加する
	 * @param inputCollection
	 * @param outputCollection
	 * @param selectiveTransformer
	 * @return
	 */
	static public Collection selectiveTransform(Collection inputCollection, Collection outputCollection, SelectiveTransformer selectiveTransformer)
	{
		for(Iterator it = inputCollection.iterator(); it.hasNext(); )
		{
			Object element = it.next();
			if(selectiveTransformer.isTrue(element))
				outputCollection.add(selectiveTransformer.transform(element));
		}
		return outputCollection;		
	}
	
	
	/** inputCollection内で、predicateを満たすものの個数をカウントする
	 * @param inputCollection
	 * @param predicate
	 * @return predicateを満たす要素の数
	 */
	static public int count(Collection inputCollection, UnaryPredicate predicate)
	{
	    int count = 0;
	    for(Iterator it = inputCollection.iterator(); it.hasNext(); )
	    {
	        if(predicate.isTrue(it.next()))
	              count++;
	    }
	    return count;
	}
	
	/** inputCollection内の全ての要素がpredicateを満たすかどうかを調べる
	 * @param inputColletion 
	 * @param predicate
	 * @return
	 */
	static public boolean satisfy(Collection inputColletion, UnaryPredicate predicate)
	{
	    for(Iterator it = inputColletion.iterator(); it.hasNext(); )
	    {
	        if(!predicate.isTrue(it.next()))
	            return false;    
	    }
	    return true;
	}
	
	
	static public Object find(Collection inputCollection, UnaryPredicate predicate)
	{
	    for(Iterator it = inputCollection.iterator(); it.hasNext(); )
	    {
	        Object elem = it.next();
	        if(!predicate.isTrue(elem))
	            return elem;
	    }
	    return null;
	}
	
	
	
	/** 2つのCollectionが一致するかどうかを判定する
	 * @param input1
	 * @param input2
	 * @param binaryPredicate ２項が一致するかどうかを判定するBinaryPredicate
	 * @return 真偽値
	 */
	static public boolean equal(Collection input1, Collection input2, BinaryPredicate binaryPredicate)
	{
	    Iterator it1 = input1.iterator();
	    Iterator it2 = input2.iterator();
	    for( ; it1.hasNext(); )
	    {
	        if(!it2.hasNext())
	            return false;
	        
	        if(!binaryPredicate.isTrue(it1.next(), it2.next()))
	            return false;
	    }
	    if(it2.hasNext())
	        return false;
	    else
	        return true;
	}
	
	static public boolean equal(Collection input, Object[] comparisonTarget)
	{
	    if(input.size() != comparisonTarget.length)
	        return false;
	    Iterator it = input.iterator();
	    for(int i=0; i<comparisonTarget.length; i++){
	        if(!it.hasNext())
	            return false;
	        if(!comparisonTarget[i].equals(it.next()))
	            return false;
	    }
	    return true;
	}

	
	static public boolean equal(Collection input, int[] comparisonTarget)
	{
	    if(input.size() != comparisonTarget.length)
	        return false;
	    
	    Iterator it = input.iterator();
	    for(int i=0; i<comparisonTarget.length; i++){
	        if(!it.hasNext())
	            return false;
	        int intVal = ((Integer) it.next()).intValue();

	        if(intVal != comparisonTarget[i])
	            return false;
	    }
	    return true;
	}
	
	static public boolean equal(Collection input, double[] comparisonTarget)
	{
	    if(input.size() != comparisonTarget.length)
	        return false;
	    
	    Iterator it = input.iterator();
	    for(int i=0; i<comparisonTarget.length; i++){
	        if(!it.hasNext())
	            return false;
	        double doubleVal = ((Double) it.next()).doubleValue();

	        if(doubleVal != comparisonTarget[i])
	            return false;
	    }
	    return true;
	}


	/** inputの各要素に対し、 functor.applyを適用する
	 * @param input
	 * @param functor
	 */
	static public void foreach(Collection input, Functor functor) 
	{
	   Iterator it = input.iterator();
	   for( ; it.hasNext(); )
	   {
	       functor.apply(it.next());
	   }
	}
	
	
	/** 与えられたCollectionに、add メソッドで、配列から初期値を入力していく
	 * @param targetCollection 初期値を入力するcollection 
	 * @param initialValue 初期値の配列
	 * @return targetCollection
	 */
	static public Collection initializeCollection(Collection targetCollection, int[] initialValue)
	{
	    for(int i=0; i<initialValue.length; i++)
	    {
	        targetCollection.add(new Integer(initialValue[i]));
	    }
	    return targetCollection;
	}

	static public Collection initializeCollection(Collection targetCollection, double[] initialValue)
	{
	    for(int i=0; i<initialValue.length; i++)
	    {
	        targetCollection.add(new Double(initialValue[i]));
	    }
	    return targetCollection;
	}

	/**
	 * @param targetCollection
	 * @param initialValue
	 * @return
	 */
	static public Collection initializeCollection(Collection targetCollection, String[] initialValue)
	{
	    for(int i=0; i<initialValue.length; i++)
	    {
	        targetCollection.add(initialValue[i]);
	    }
	    return targetCollection;
	}
}


//--------------------------------------
// $Log: Algorithm.java,v $
// Revision 1.6  2004/07/22 07:09:38  leo
// satisfy, findを追加
//
// Revision 1.5  2004/06/24 03:45:22  leo
// Algorithmに、count, equal,を追加
//
// Revision 1.4  2004/06/24 02:28:41  leo
// 正規表現用のPredicateのテストを追加
//
// Revision 1.3  2004/06/24 02:09:14  leo
// ComparisonPredicateの完備
//
// Revision 1.2  2004/06/24 01:47:35  leo
// Collectionのinitializerを追加
//
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------