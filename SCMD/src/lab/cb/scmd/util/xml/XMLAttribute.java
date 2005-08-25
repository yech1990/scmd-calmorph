//--------------------------------------
// SCMD Project
// 
// XMLAttribute.java 
// Since:  2004/05/04
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;



/** XMLのelementのattribute（あるいはそのリスト）を表現するクラス
 * @author leo
 *
 */
public class XMLAttribute
{
    static TextContentFilter _filter = new HTMLFilter();
    
	public XMLAttribute(String attributeName, String attributeValue)
	{
		add(attributeName, attributeValue);
	}
	public XMLAttribute(String attributeName, int attributeValue)
	{
		add(attributeName, attributeValue);
	}
	public XMLAttribute(String attributeName, double attributeValue)
	{
		add(attributeName, attributeValue);
	}
	public XMLAttribute(String attributeName, Object attributeValue)
	{
		add(attributeName, attributeValue);
	}
	
	public XMLAttribute()
	{ 
	}
	
	public XMLAttribute add(String attributeName, String attributeValue)
	{
		_attributeNameList.add(attributeName);
		_attributeValue.put(attributeName, attributeValue);
		return this;
	}
	
	public XMLAttribute add(String attributeName, int attributeValue)
	{
	    return this.add(attributeName, Integer.toString(attributeValue));
	}
	public XMLAttribute add(String attributeName, double attributeValue)
	{
	    return this.add(attributeName, Double.toString(attributeValue));
	}
	public XMLAttribute add(String attributeName, Object attributeValue)
	{
	    return this.add(attributeName, attributeValue.toString());
	}
	
	
	public XMLAttribute(Properties properties)
	{
	    Set keySet = properties.keySet();
	    for(Iterator it = keySet.iterator(); it.hasNext(); )
	    {
	        String attribute = (String) it.next();
	        String value = properties.getProperty(attribute);
	        this.add(attribute, value);
	    }
	}
	public XMLAttribute(Map properties)
	{
	    Set keySet = properties.keySet();
	    for(Iterator it = keySet.iterator(); it.hasNext(); )
	    {
	        String attribute = (String) it.next();
	        String value = properties.get(attribute).toString();
	        this.add(attribute, value);
	    }
	}
	
	public String getValue(String attributeName)
	{
	    return (String) _attributeValue.get(attributeName);
	}
	
	public int length()
	{
	    return _attributeNameList.size();
	}
	
	public String toString()
	{
	    return toString(_filter);
	}
	
	public String toString(TextContentFilter filter)
	{
		String returnString = "";
		Iterator ni = _attributeNameList.iterator();
		for ( ; ni.hasNext();)
		{
			String attributeName = (String) ni.next();
			String attributeValue = (String) _attributeValue.get(attributeName);
			returnString += attributeName + "=\"" + filter.filter(attributeValue) + "\" "; 
		}
		if(!returnString.equals(""))
		{
			// remove the unnecessary white space
			return returnString.substring(0, returnString.length()-1);
		}
		else 
			return returnString;
	}
	
	protected LinkedList _attributeNameList = new LinkedList();
	protected HashMap _attributeValue = new HashMap();
}


//--------------------------------------
// $Log: XMLAttribute.java,v $
// Revision 1.10  2004/09/21 01:45:54  leo
// 継承用にprotectedに変更
//
// Revision 1.9  2004/09/16 09:51:48  leo
// constructorを追加
//
// Revision 1.8  2004/08/31 04:46:40  leo
// constructor(Map)を追加
//
// Revision 1.7  2004/08/07 12:30:11  leo
// Filterを切り替えられるようにしました
//
// Revision 1.6  2004/07/22 07:10:27  leo
// 内部データの保持機構を変更
//
// Revision 1.5  2004/07/21 08:07:24  leo
// *** empty log message ***
//
// Revision 1.4  2004/07/13 08:04:07  leo
// selfCloseTagを追加
//
// Revision 1.3  2004/07/12 08:00:25  leo
// &, <, >, ", 'をentity参照に置換するように改良
//
// Revision 1.2  2004/07/12 07:26:52  leo
// XMLOutputterの修正
//
// Revision 1.1  2004/05/03 17:02:35  leo
// XMLOutputterを書き始めました。テストコードは未製作
//
//--------------------------------------