//--------------------------------------
//SCMD Project
//
//SVGOutputter.java 
//Since:  2004/09/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------
package lab.cb.scmd.util.svg;

import java.io.OutputStream;
import java.io.Writer;

import lab.cb.scmd.util.xml.InvalidXMLException;
import lab.cb.scmd.util.xml.XMLAttribute;
import lab.cb.scmd.util.xml.XMLOutputter;

public class SVGOutputter extends XMLOutputter {
    
    SVGColor foregroundColor = new SVGColor();
    SVGColor backgroupndColor = new SVGColor();
    String fontsize = "12pt";
    /**
     * 
     */
    public SVGOutputter() {
        super();
    }
    /**
     * @param outputStream
     */
    public SVGOutputter(OutputStream outputStream) {
        super(outputStream);
    }
    /**
     * @param writer
     */
    public SVGOutputter(Writer writer) {
        super(writer);
    }
    
    public void header() {
        header(300,300);
    }
    
    public void header(int width, int height) {
		if(_isRootTag) {    
		    _out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		    outputDTD("svg PUBLIC '-//W3C//DTD SVG 1.0//EN' 'http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd'");
		    _isRootTag = false;
		}
        
        XMLAttribute att = new XMLAttribute();
        StyleAttribute style = new StyleAttribute();
        style.add("fill-opacity", "1");
        style.add("color-rendering", "auto");
        style.add("color-interpolation", "auto");
        style.add("text-rendering", "auto");
        style.add("stroke", "black");
        style.add("stroke-linecap", "square");
        style.add("stroke-miterlimit", "10");
        style.add("shape-rendering", "auto");
        style.add("stroke-opacity", "1");
        style.add("fill", "black");
        style.add("stroke-dasharray", "none");
        style.add("font-weight", "normal");
        style.add("stroke-width", "1");
        style.add("font-family", "&apos;sansserif&apos;");
        style.add("font-style", "normal");
        style.add("stroke-linejoin", "miter");
        style.add("font-size", "12");
        style.add("stroke-dashoffset", "0");
        style.add("image-rendering", "auto");
        att.add("xmlns", "http://www.w3.org/2000/svg");
        att.add("xmlns:xlink", "http://www.w3.org/1999/xlink");
        att.add("width", width );
        att.add("height", height );
        startTag("svg", att, style);
        startGroup();
    }
    
    private void startTag(String string, XMLAttribute att, StyleAttribute style) {
        att = addStyle(att, style);
        startTag(string, att);
    }

    public void footer() throws InvalidXMLException {
        closeGroup(); // g
        closeTag(); // svg
    }

    public void startGroup() {
        startTag("g");
    }
    
    public void startGroup(XMLAttribute att) {
        startTag("g", att);
    }
    
    public void closeGroup() throws InvalidXMLException {
        closeTag();
    }

    public void setForegroundColor(SVGColor color) {
        foregroundColor = color;
    }

    public void setForegroundColor(int[] color) {
        foregroundColor = new SVGColor(color);
    }

    public void setBackgroundColor(SVGColor color) {
        backgroupndColor = color;
    }

    public void setBackgroundColor(int[] color) {
        backgroupndColor = new SVGColor(color);
    }
    
    public void setFontSize(int size) {
        fontsize = size + "pt";
    }


    public void drawRect(int x, int y, int width, int height) {
        drawRect(x, y, width, height, new XMLAttribute(), new StyleAttribute());
    }
    
    public void drawRect(int x, int y, int width, int height, XMLAttribute att) {
        drawRect(x, y, width, height, att, new StyleAttribute());
    }

    public void drawRect(int x, int y, int width, int height, XMLAttribute att, StyleAttribute style) {
        att.add("x", x);
        att.add("y", y);
        att.add("width", width);
        att.add("height", height);
        style.add("stroke", foregroundColor.toString());
        style.add("fill", backgroupndColor.toString());
        att = addStyle(att, style);
        selfCloseTag("rect", att);
    }

    private XMLAttribute addStyle(XMLAttribute att, StyleAttribute style) {
        att.add("style", style.toString());
        return att;
    }

    public void drawString(String str, int x, int y) throws InvalidXMLException {
        StyleAttribute style = new StyleAttribute();
        style.add("stroke", foregroundColor);
        style.add("font-size", fontsize);
        drawString(str, x, y, style);
    }

    public void drawString(String str, int x, int y, StyleAttribute style) throws InvalidXMLException {
        XMLAttribute att = new XMLAttribute();
        att.add("x", x);
        att.add("y", y);
        element("text", att, str);
    }
}
