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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


/**
 * Collectionに加工を加えるためのアルゴリズム群 (C++のSTL風)
 *
 * @author leo
 */
public class Algorithm {
    /**
     * inputCollectionのうち、predicate.isTrue() が満たされるもののみを outputCollectionに追加する
     *
     * @param inputCollection
     * @param outputCollection
     * @param predicate
     * @return
     */
    static public <E> Collection<E> select(Collection<E> inputCollection, Collection<E> outputCollection, UnaryPredicate<E> predicate) {
        for (E element : inputCollection) {
            if (predicate.isTrue(element))
                outputCollection.add(element);
        }
        return outputCollection;
    }

    /**
     * inputCollectionの要素それぞれに、transformer.transform() を適用して、結果をoutputCollectionに追加する
     *
     * @param inputCollection
     * @param outputCollection
     * @param transformer
     * @return
     */
    static public <E, F> Collection<F> transform(Collection<E> inputCollection, Collection<F> outputCollection, Transformer<E, F> transformer) {
        for (E element : inputCollection) {
            outputCollection.add(transformer.transform(element));
        }
        return outputCollection;
    }

    /**
     * inputCollectionの要素のうち、selectiveTransformer.isTrue() を満たすものに、selectiveTransformer.transform()を適用して、
     * 結果をoutputCollectionに追加する
     *
     * @param inputCollection
     * @param outputCollection
     * @param selectiveTransformer
     * @return
     */
    static public <E, F> Collection<F> selectiveTransform(Collection<E> inputCollection, Collection<F> outputCollection, SelectiveTransformer<E, F> selectiveTransformer) {
        for (E element : inputCollection) {
            if (selectiveTransformer.isTrue(element))
                outputCollection.add(selectiveTransformer.transform(element));
        }
        return outputCollection;
    }


    /**
     * inputCollection内で、predicateを満たすものの個数をカウントする
     *
     * @param inputCollection
     * @param predicate
     * @return predicateを満たす要素の数
     */
    static public <E> int count(Collection<E> inputCollection, UnaryPredicate<E> predicate) {
        int count = 0;
        for (E element : inputCollection) {
            if (predicate.isTrue(element))
                count++;
        }
        return count;
    }

    /**
     * inputCollection内の全ての要素がpredicateを満たすかどうかを調べる
     *
     * @param inputCollection
     * @param predicate
     * @return
     */
    static public <E> boolean satisfy(Collection<E> inputCollection, UnaryPredicate<E> predicate) {
        for (E element : inputCollection) {
            if (!predicate.isTrue(element))
                return false;
        }
        return true;
    }


    /**
     * inputCollectionの中で、最初にpredicateを満たすものを返す
     *
     * @param <E>
     * @param inputCollection 入力
     * @param predicate       条件
     * @return inputCollectionの中で、最初にpredicateを満たすものを返す
     */
    static public <E> E find(Collection<E> inputCollection, UnaryPredicate<E> predicate) {
        for (E element : inputCollection) {
            if (!predicate.isTrue(element))
                return element;
        }
        return null;
    }


    /**
     * 2つのCollectionが一致するかどうかを判定する
     *
     * @param input1
     * @param input2
     * @param binaryPredicate ２項が一致するかどうかを判定するBinaryPredicate
     * @return 真偽値
     */
    static public <E> boolean equal(Collection<E> input1, Collection<E> input2, BinaryPredicate<E, E> binaryPredicate) {
        Iterator<E> it1 = input1.iterator();
        Iterator<E> it2 = input2.iterator();
        for (; it1.hasNext(); ) {
            if (!it2.hasNext())
                return false;

            if (!binaryPredicate.isTrue(it1.next(), it2.next()))
                return false;
        }
        return !it2.hasNext();
    }

    static public boolean equal(Collection<Object> input, Object[] comparisonTarget) {
        if (input.size() != comparisonTarget.length)
            return false;
        Iterator<Object> it = input.iterator();
        for (Object o : comparisonTarget) {
            if (!it.hasNext())
                return false;
            if (!o.equals(it.next()))
                return false;
        }
        return true;
    }


    static public boolean equal(Collection<Integer> input, int[] comparisonTarget) {
        if (input.size() != comparisonTarget.length)
            return false;

        Iterator<Integer> it = input.iterator();
        for (int value : comparisonTarget) {
            if (!it.hasNext())
                return false;
            int intVal = it.next();

            if (intVal != value)
                return false;
        }
        return true;
    }

    static public boolean equal(Collection<Double> input, double[] comparisonTarget) {
        if (input.size() != comparisonTarget.length)
            return false;

        Iterator<Double> it = input.iterator();
        for (double v : comparisonTarget) {
            if (!it.hasNext())
                return false;
            double doubleVal = it.next();

            if (doubleVal != v)
                return false;
        }
        return true;
    }


    /**
     * inputの各要素に対し、 functor.applyを適用する
     *
     * @param input
     * @param functor
     */
    static public <E> void foreach(Collection<E> input, Functor<E> functor) {
        for (E element : input) {
            functor.apply(element);
        }
    }


    /**
     * 与えられたCollectionに、add メソッドで、配列から初期値を入力していく
     *
     * @param targetCollection 初期値を入力するcollection
     * @param initialValue     初期値の配列
     * @return targetCollection
     */
    static public Collection<Integer> initializeCollection(Collection<Integer> targetCollection, int[] initialValue) {
        for (int value : initialValue) {
            targetCollection.add(value);
        }
        return targetCollection;
    }

    static public Collection<Double> initializeCollection(Collection<Double> targetCollection, double[] initialValue) {
        for (double v : initialValue) {
            targetCollection.add(v);
        }
        return targetCollection;
    }

    /**
     * @param targetCollection
     * @param initialValue
     * @return
     */
    static public Collection<String> initializeCollection(Collection<String> targetCollection, String[] initialValue) {
        Collections.addAll(targetCollection, initialValue);
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