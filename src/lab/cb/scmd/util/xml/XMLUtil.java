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
	
	
	/** content‚ğCDATAƒZƒNƒVƒ‡ƒ“‚ÅˆÍ‚ñ‚Å•Ô‚·
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
// CDATA‚Ìˆ—‚ğ’Ç‰Á
//
//--------------------------------------