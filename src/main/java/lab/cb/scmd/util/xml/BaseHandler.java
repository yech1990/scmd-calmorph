//--------------------------------------
// SCMDProject
// 
// BaseHandler.java 
// Since: 2004/07/22
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

/**
 * charactersイベントは、bufferの切れ目にあたった際に、 必ずしも連続したstringを与えるわけではないので、
 * charactersイベントのsequenceをつなげた結果を、textContent()として返すクラス。
 * <p>
 * [[NOTICE]]: このクラスを使う場合には、必ずconstructor, startDobument, startElement, endElement,
 * characters eventsで、super(..)を呼ぶこと
 *
 * @author leo
 */
public class BaseHandler extends DefaultHandler {

    /**
     *
     */
    public BaseHandler() {
        super();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String currentContent = (String) _contentStack.pop();
        currentContent += new String(ch, start, length);
        _contentStack.push(currentContent);
    }

    public void textContent(String content) throws SAXException {
        // implement this (deafult: do nothing)
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        String content = ((String) _contentStack.pop()).trim();
        if (content.length() > 0)
            textContent(content);
    }

    public void startDocument() throws SAXException {
        _contentStack.clear();
        _contentStack.push("");
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        _contentStack.push("");
    }

    Stack _contentStack = new Stack();
}

//--------------------------------------
// $Log: BaseHandler.java,v $
// Revision 1.1  2004/07/22 07:10:09  leo
// first ship
//
//--------------------------------------
