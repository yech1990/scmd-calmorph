// --------------------------------------
// SCMD Project
// 
// Statistics.java
// Since: 2004/04/23
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.stat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import lab.cb.scmd.util.table.*;

/**
 * TableIteratorを入力するメソッドは全て、SampleFilteringStrategyで、サンプルにfilter（１％除去など）が掛けられ、
 * Collectionを入力とするメソッドで統計値が計算される。
 * Collectionを入力とするメソッドではfilterはかからない
 * @author leo
 *  
 */
public class Statistics
{

	protected SampleFilteringStrategy	_sampleFilteringStrategy	= new DoNotFilterStrategy();

	/**
	 * デフォルト （サンプルにフィルターはかからない）
	 */
	public Statistics()
	{
		getFilteringStrategy().setStatClass(this);
	}

	/**
	 * ユーザーが定義したfilterがサンプルにかかるようになる
	 * 
	 * @param filteringStrategy
	 */
	public Statistics(SampleFilteringStrategy filteringStrategy)
	{
		_sampleFilteringStrategy = filteringStrategy;
		getFilteringStrategy().setStatClass(this);
	}

	public SampleFilteringStrategy getFilteringStrategy()
	{
		return _sampleFilteringStrategy;
	}

	/**
	 * そのセルでdouble値が有効かどうかをチェックする
	 * 
	 * @param cell
	 *            チェックするCell
	 * @return
	 */
	protected boolean isValidAsDouble(Cell cell)
	{
		return cell.isValidAsDouble();
	}

	protected boolean isValidAsString(Cell cell)
	{
		return (cell.toString() != null);
	}

	
	
	public double calcAverage(TableIterator ti)
	{
		return calcMean(ti);
	}

	public double calcMean(TableIterator ti)
	{
		Collection sampleCollection = getFilteringStrategy().filter(ti);
		return calcMean(sampleCollection);
		//		NumElementAndValuePair numElementAndMean = calcMean_internal(ti);
		//		return numElementAndMean.value;
	}

	public NumElementAndStatValuePair calcMeanAndNumSample(TableIterator ti)
	{
		Collection sampleCollection = getFilteringStrategy().filter(ti);
		return new NumElementAndStatValuePair(sampleCollection.size(), calcMean(sampleCollection));
	}
	
    public Collection filter(TableIterator ti)
    {
        return getFilteringStrategy().filter(ti);
    }

	/**
	 * 不偏分散を返す
	 * 
	 * @param ti
	 * @return
	 */
	public double calcVariance(TableIterator ti)
	{
		Collection sampleCollection = getFilteringStrategy().filter(ti);
		return calcVariance(sampleCollection);

		//		NumElementAndValuePair numElementAndMean =
		// calcMean_internal((TableIterator) ti.clone());
		//		if(numElementAndMean.numElement < 2)
		//			return -1; // unable to compute the variance
		//		double diffSquare = calcDiffSquare((TableIterator) ti.clone(),
		// numElementAndMean.value);
		//		return diffSquare / (numElementAndMean.numElement - 1);

	}
	public double calcSD(TableIterator ti)
	{
		Collection sampleCollection = getFilteringStrategy().filter(ti);
		return calcSD(sampleCollection);
	}

	/**
	 * CV（変動係数）＝ 標準偏差 / 平均 を返す
	 * 
	 * @param ti
	 * @return
	 */
	public double calcCV(TableIterator ti)
	{
		Collection sampleCollection = getFilteringStrategy().filter(ti);
		return calcCV(sampleCollection);
		//		NumElementAndValuePair numElementAndMean =
		// calcMean_internal((TableIterator) ti.clone());
		//		if(numElementAndMean.numElement < 2)
		//			return -1; // unable to compute the variance
		//		double diffSquare = calcDiffSquare((TableIterator) ti.clone(),
		// numElementAndMean.value);
		//		double SD = Math.sqrt(diffSquare / numElementAndMean.numElement);
		//		return SD / numElementAndMean.value;
	}

	public double getMaxValue(TableIterator ti) 
	{
		Collection sampleCollection = getFilteringStrategy().filter(ti);
		return getMaxValue(sampleCollection);
	}

	public double getMinValue(TableIterator ti) 
	{
		Collection sampleCollection = getFilteringStrategy().filter(ti);
		return getMinValue(sampleCollection);
	}

	public HashMap calcStats(TableIterator ti)
	{
		// calc mean, SD, CV, num_sample
		// TODO implementbb
		return null;
	}

	public int countValidDoubleCell(TableIterator ti)
	{
		Collection sampleCollection = getFilteringStrategy().filter(ti);
		return sampleCollection.size();
		//		int count = 0;
		//		for(; ti.hasNext(); )
		//		{
		//			Cell c = ti.next();
		//			if(isValidAsDouble(c))
		//				count++;
		//		}
		//		return count;
	}

	public int countValidStringCell(TableIterator ti)
	{
		int count = 0;
		for (; ti.hasNext();)
		{
			Cell c = ti.nextCell();
			if(isValidAsString(c))
				count++;
		}
		return count;
	}


//	/**
//	 * 平均からの差の二乗の和を計算
//	 * 
//	 * @param ti
//	 * @param mean
//	 * @return
//	 */
//	protected double calcDiffSquare(TableIterator ti, double mean)
//	{
//		int numElement = 0;
//		double diffSquare = 0;
//		for (; ti.hasNext();)
//		{
//			Cell c = ti.next();
//			if(!isValidAsDouble(c))
//				continue;
//			double v = c.doubleValue();
//			diffSquare += (v - mean) * (v - mean);
//			numElement++;
//		}
//		return diffSquare;
//	}
//
//	protected NumElementAndValuePair calcMean_internal(TableIterator ti)
//	{
//		double sum = 0;
//		int numElement = 0;
//		for (; ti.hasNext();)
//		{
//			Cell c = ti.next();
//			if(!isValidAsDouble(c))
//				continue; // skip invalid value
//			sum += c.doubleValue();
//			numElement++;
//		}
//		if(numElement == 0)
//			return new NumElementAndValuePair(0, 0);
//		else
//			return new NumElementAndValuePair(numElement, sum / numElement);
//	}

	/**
	 * cの平均を返す
	 * 
	 * @param c
	 *            Doubleのコレクション （nullを含まないことが前提)
	 * @return 平均値
	 */
	static public double calcMean(Collection c)
	{
		int numElement = c.size();
		if(numElement == 0)
			return 0;
		double sum = 0;
		for (Iterator it = c.iterator(); it.hasNext();)
		{
			sum += ((Double) it.next()).doubleValue();
		}
		return sum / numElement;
	}

	static public double calcVariance(Collection c)
	{
		int numElement = c.size();
		if(numElement < 2)
			return -1; // unable to compute the variance

		double squareSum = 0;
		for (Iterator it = c.iterator(); it.hasNext();)
		{
			double v = ((Double) it.next()).doubleValue();
			squareSum += v * v;
		}
		double sum = 0;
		for (Iterator it = c.iterator(); it.hasNext();)
			sum += ((Double) it.next()).doubleValue();

		double variance = (numElement * squareSum - (sum * sum)) / (numElement * (numElement - 1));
		return variance;
	}

	static public double calcSD(Collection c)
	{
		double variance = calcVariance(c);
		if(variance < 0)
			return -1; // unable to compute the standard deviation

		return Math.sqrt(variance);
	}

	static public double calcCV(Collection c)
	{
		double mean = calcMean(c);
		if(mean == 0)
			return -1; // unable to compute the CV

		double SD = calcSD(c);
		if(SD < 0)
			return -1; // unable to compute SD

		return SD / mean;
	}
	
	static public double getMinValue(Collection c)
	{
		double min = Double.MAX_VALUE;
		for (Iterator it = c.iterator(); it.hasNext();)
		{
			double v = ((Double) it.next()).doubleValue();
			if( v < min )
				min = v;
		}
		return min;
	}

	static public double getMaxValue(Collection c)
	{
		double max = - Double.MAX_VALUE;
		for (Iterator it = c.iterator(); it.hasNext();)
		{
			double v = ((Double) it.next()).doubleValue();
			if( v > max )
				max = v;
		}
		return max;
	}

}

//--------------------------------------
// $Log: Statistics.java,v $
// Revision 1.15  2004/08/27 03:19:38  leo
// Statisticsにpublic constructorを追加
//
// Revision 1.14  2004/08/23 04:30:16  sesejun
// To make teardrop view
//
// Revision 1.13  2004/08/10 10:44:31  leo
// CellShapeStatの追加
//
// Revision 1.12  2004/07/27 05:18:12  leo
// TableIteratorの微調整
//
// Revision 1.11  2004/05/07 08:17:00  leo
// Collectionから計算するメソッドをstaticに変更
//
// Revision 1.10  2004/05/07 04:30:26  leo
// 時間計測用クラスを追加
//
// Revision 1.9  2004/05/07 03:06:20  leo
// Statisticsクラスを、データのフィルタリングの戦略を切り替えられるように変更
//
// Revision 1.8 2004/05/06 08:05:41 leo
// CV値の計算時のバグ修正
//
// Revision 1.7 2004/05/06 06:10:34 leo
// メッセージ出力用のNullPrintStreamを追加。
// 統計値計算部分は完了。出力部分はこれから
//
// Revision 1.6 2004/05/04 15:11:32 leo
// A_data.xlsの計算部分を追加 （要テスト）
//
// Revision 1.5 2004/04/27 06:40:51 leo
// util.stat package test complete
//
// Revision 1.4 2004/04/26 06:57:50 leo
// modify supports for validating double values
//
// Revision 1.3 2004/04/23 06:08:41 leo
// *** empty log message ***
//
// Revision 1.2 2004/04/23 05:56:56 leo
// temporary commit
//
// Revision 1.1 2004/04/23 04:44:38 leo
// add stat/table utilities
//
//--------------------------------------
