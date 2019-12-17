//--------------------------------------
// SCMD Project
//
// StyleAttribute.java 
// Since:  2004/09/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------/*
package lab.cb.scmd.util.svg;

import lab.cb.scmd.util.xml.TextContentFilter;
import lab.cb.scmd.util.xml.XMLAttribute;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author sesejun
 * <p>
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StyleAttribute extends XMLAttribute {

    /**
     *
     */
    public StyleAttribute() {
        super();
    }

    /**
     * @param properties
     */
    public StyleAttribute(Map properties) {
        super(properties);
    }

    /**
     * @param properties
     */
    public StyleAttribute(Properties properties) {
        super(properties);
    }

    /**
     * @param attributeName
     * @param attributeValue
     */
    public StyleAttribute(String attributeName, double attributeValue) {
        super(attributeName, attributeValue);
    }

    /**
     * @param attributeName
     * @param attributeValue
     */
    public StyleAttribute(String attributeName, int attributeValue) {
        super(attributeName, attributeValue);
    }

    /**
     * @param attributeName
     * @param attributeValue
     */
    public StyleAttribute(String attributeName, Object attributeValue) {
        super(attributeName, attributeValue);
    }

    /**
     * @param attributeName
     * @param attributeValue
     */
    public StyleAttribute(String attributeName, String attributeValue) {
        super(attributeName, attributeValue);
    }

    public String toString(TextContentFilter filter) {
        String returnString = "";
        Iterator ni = _attributeNameList.iterator();
        for (; ni.hasNext(); ) {
            String attributeName = (String) ni.next();
            String attributeValue = (String) _attributeValue.get(attributeName);
            returnString += attributeName + ":" + filter.filter(attributeValue) + "; ";
        }
        if (!returnString.equals("")) {
            // remove the unnecessary white space
            return returnString.substring(0, returnString.length() - 1);
        } else
            return returnString;
    }

}
