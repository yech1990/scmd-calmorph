//--------------------------------------
// SCMDProject
// 
// XMLAttributeMap.java 
// Since: 2004/09/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author leo
 */
public class XMLAttributeMap extends TreeMap {

    /**
     *
     */
    public XMLAttributeMap() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public XMLAttributeMap(Comparator arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public XMLAttributeMap(Map arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public XMLAttributeMap(SortedMap arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }


    public Object put(String key, int value) {
        return super.put(key, value);
    }

    public Object put(String key, double value) {
        return super.put(key, value);
    }

}


//--------------------------------------
// $Log: XMLAttributeMap.java,v $
// Revision 1.1  2004/09/16 10:22:17  leo
// XMLAttribute用のmap
//
//--------------------------------------