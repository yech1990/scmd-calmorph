//--------------------------------------
// SCMD Project
// 
// DTDDeclaration.java 
// Since:  2004/07/22
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

/**
 * @author leo
 */
public class DTDDeclaration {

    private String _rootElement;
    private String _uri;

    public DTDDeclaration(String rootElement, String uri) {
        this._rootElement = rootElement;
        this._uri = uri;
    }

    public String toString() {
        return "<!DOCTYPE " + getRootElement() + " SYSTEM \"" + getUri() + "\">";
    }

    private String getRootElement() {
        return _rootElement;
    }

    public void setRootElement(String rootElement) {
        this._rootElement = rootElement;
    }

    private String getUri() {
        return _uri;
    }

    public void setUri(String uri) {
        this._uri = uri;
    }
}


//--------------------------------------
// $Log: DTDDeclaration.java,v $
// Revision 1.1  2004/07/22 13:23:22  leo
// DTD宣言を複数呼べるように変更
//
//--------------------------------------
