//--------------------------------------
// SCMD Project
// 
// RegexPredicateTest.java 
// Since:  2004/06/24
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.algorithm;

import junit.framework.TestCase;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @author leo
 *
 */
public class RegexPredicateTest extends TestCase
{
    public void testRegexPredicate()
    {
        RegexPredicate regex = new RegexPredicate("A[0-9]{3}");
        assertTrue(regex.isTrue(new String("A004")));
        assertFalse(regex.isTrue(new String("A0051")));
    }
    
    public void testRegexSelectiveTransformer()
    {
        Vector input = new Vector();
        String[] initValue = {"asdf234asdf", "abc113", "xsdf11345"};
        Algorithm.initializeCollection(input, initValue);
        
        Vector result = new Vector();
        // 正規表現にマッチするもののみに変換を施す
        Algorithm.selectiveTransform(input, result, 
                new RegexPredicate("([a-z]+)([0-9]+)") {
            		public Object transform(Object input){
            		    Matcher matcher = getMatcher();
            		    return matcher.group(2) + matcher.group(1);  // alphabet部分と数字部分を入れ替える
            		}
        		});
        
        String[] answer = { "113abc", "11345xsdf" };
        Iterator it = result.iterator();
        for(int i=0; i<answer.length; i++)
        {
            assertTrue(it.hasNext());
            assertTrue(answer[i].equals(it.next()));
        }
    }
}


//--------------------------------------
// $Log: RegexPredicateTest.java,v $
// Revision 1.1  2004/06/24 02:28:41  leo
// 正規表現用のPredicateのテストを追加
//
//--------------------------------------