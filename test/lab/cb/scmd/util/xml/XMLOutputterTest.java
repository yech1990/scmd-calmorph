//--------------------------------------
// SCMD Project
// 
// XMLOutputterTest.java 
// Since:  2004/05/04
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

import java.io.*;

import junit.framework.TestCase;


/**
 * @author leo
 *
 */
public class XMLOutputterTest extends TestCase
{
    public void testReplaceToPredefinedEntities()
    {
        TextContentFilter filter = new HTMLFilter();
        String input = "'aj&lkjasdfjk< asdaf'<>\"";
        String transformed = filter.filter(input);
        assertEquals("&apos;aj&amp;lkjasdfjk&lt; asdaf&apos;&lt;&gt;&quot;", transformed);
        
    }
    
	public void testXMLOutputter()
	{
		XMLOutputter xmlout = new XMLOutputter(); // System.outに出力
		
		try
		{
		    xmlout.startTag("sample");
		    xmlout.startTag("item", new XMLAttribute("id", "0").add("name", "sampleitem"));		    
		    xmlout.textContent("element content");		    
		    xmlout.closeTag();
		    xmlout.closeTag();
		    xmlout.endOutput();  // 閉じ忘れのタグがあれば、全て閉じる
		    
		}
		catch(InvalidXMLException e)
		{
		    fail(e.getMessage());
		}
	}
	
	public void testXMLFileOut() throws IOException
	{
	    File xmlfile = new File("__samplefile.xml");	    
	    XMLOutputter xmlout = new XMLOutputter(new FileOutputStream(xmlfile));
	    try
	    {
	        xmlout.startTag("roottag");
	        xmlout.startTag("item", new XMLAttribute("id", "10"));
	        xmlout.closeTag();
	        xmlout.endOutput();
	        
	        BufferedReader fin = new BufferedReader(new FileReader(xmlfile));
	        String line;
	        while((line = fin.readLine()) != null)
	        {
	            System.out.println(line);	
	        }
	        
	    }
	    catch(InvalidXMLException e)
	    {
	        fail(e.getMessage());
	    }
	    finally
	    {
	        xmlout.closeStream();   // fileを閉じる
	    }
	}
	
	public void testDTDOutputTest() throws IOException, InvalidXMLException
	{
	    XMLOutputter xmlout = new XMLOutputter();
	    xmlout.setDTDDeclaration(new DTDDeclaration("lab.cb.scmd", "lab.cb.scmd.dtd"));
	    xmlout.startTag("lab.cb.scmd").
	    	startTag("photo", new XMLAttribute("id", "104")).
	    	selfCloseTag("cell", new XMLAttribute("id", "1234").add("max", "110")).
	    	closeTag().
	    closeTag();
	    xmlout.endOutput();
	}
	
	public void testFilter()  throws InvalidXMLException
	{
	    XMLOutputter xmlout = new XMLOutputter();
	    xmlout.startTag("lab.cb.scmd", new XMLAttribute("cdata", "<![CDATA[ 'a&a']]>"));
	    xmlout.textContent("<![CDATA[ 'asdf'&dafa'\"'asfa']]>");
	    xmlout.closeTag();
	    xmlout.endOutput();
	}
	
}


//--------------------------------------
// $Log: XMLOutputterTest.java,v $
// Revision 1.11  2004/08/26 08:47:53  leo
// *** empty log message ***
//
// Revision 1.10  2004/08/07 12:30:11  leo
// Filterを切り替えられるようにしました
//
// Revision 1.9  2004/07/22 14:16:38  leo
// DTD宣言は一つに
//
// Revision 1.8  2004/07/22 13:23:47  leo
// DTD宣言を複数呼べるように変更
//
// Revision 1.7  2004/07/21 02:51:56  leo
// AllTestsにlab.cb.scmd.util.xmlを追加
//
// Revision 1.6  2004/07/13 08:04:31  leo
// 若干修正
//
// Revision 1.5  2004/07/12 08:00:25  leo
// &, <, >, ", 'をentity参照に置換するように改良
//
// Revision 1.4  2004/07/12 07:26:52  leo
// XMLOutputterの修正
//
// Revision 1.3  2004/07/08 08:20:02  leo
// XMLOutputterのテストコードを書きなおし
//
// Revision 1.2  2004/05/05 15:56:48  leo
// A1B, C_data.xls計算部分を追加
//
// Revision 1.1  2004/05/03 17:02:35  leo
// XMLOutputterを書き始めました。テストコードは未製作
//
//--------------------------------------