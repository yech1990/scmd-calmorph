//--------------------------------------
// SCMD Project
// 
// RegexPredicate.java 
// Since:  2004/06/24
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

import java.util.regex.*;

/** 正規表現にマッチするものなら、trueと判定する
 * 同じパターンを繰り返し使用するとき、このクラスを使うと、
 * 何度もパターンのコンパイルをしなくてすむ
 * また、正規表現のmatchの結果(getMatcher()で取得)を使ってなんらかの出力を得たいなら、transformをoverrideすると良い
 * @author leo
 *
 */
public class RegexPredicate implements SelectiveTransformer
{	
	public RegexPredicate(String regularExpression)
	{
		_regexPattern = Pattern.compile(regularExpression);
	}	
	
	public boolean isTrue(Object input)
	{
		_patternMatcher = _regexPattern.matcher((String) input);
		return _patternMatcher.matches();
	}
	
	public Object transform(Object input)
	{
	    return input;
	}
	
	public Matcher getMatcher() 
	{
		return _patternMatcher;
	}
		
	protected Pattern _regexPattern;
	protected Matcher _patternMatcher = null;
}


//--------------------------------------
// $Log: RegexPredicate.java,v $
// Revision 1.2  2004/06/24 01:16:01  leo
// UnaryPredicateの実装から、SelectiveTransformerの実装に変更
//
// Revision 1.1  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
//--------------------------------------