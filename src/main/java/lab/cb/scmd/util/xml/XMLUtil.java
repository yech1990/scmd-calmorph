//--------------------------------------
// SCMDProject
// 
// XMLUtil.java 
// Since: 2004/08/26
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

/**
 * @author leo
 *
 */
public class XMLUtil
{
	
	
	/** contentをCDATAセクションで囲んで返す
	 * @param content
	 * @return
	 */
	public static String createCDATA(String content)
	{
	    return "<![CDATA[" + content + "]]>";
	}

}


//--------------------------------------
// $Log: XMLUtil.java,v $
// Revision 1.1  2004/08/26 04:28:13  leo
// CDATAの処理を追加
//
//--------------------------------------