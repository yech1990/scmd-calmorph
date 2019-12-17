// --------------------------------------
// SCMD Project
// 
// XMLOutputter.java
// Since: 2004/05/04
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;


/**
 * validなXML（タグがきちんと閉じているXML）を出力するのをサポートするクラス
 *
 * @author leo
 */

public class XMLOutputter {

    /**
     * デフォルトでは、standard outに出力
     */
    /**
     *
     */
    public XMLOutputter() {
        setOutputStream(System.out);
    }

    /**
     * @param outputStream XMLの出力先
     */
    public XMLOutputter(OutputStream outputStream) {
        setOutputStream(outputStream);
    }

    public XMLOutputter(Writer writer) {
        _out = new PrintWriter(writer);
    }


    public void omitHeader() {
        _omitHeader = true;
    }


    public void setDTDDeclaration(DTDDeclaration dtdDeclaration) {
        _dtdDeclaration = dtdDeclaration;
    }

    public void setOutputStream(OutputStream outputStream) {
        _out = new PrintWriter(outputStream);
    }

    protected void header() {
        if (_omitHeader)
            return;
        _out.println("<?xml version=\"1.0\" ?>");
    }

    protected void outputDTD(String rootTagName) {
        if (_omitHeader)
            return;

        if (_dtdDeclaration == null) {
            _out.println("<!DOCTYPE " + rootTagName + ">");
        } else {
            _out.println(_dtdDeclaration.toString());
        }
    }

    protected void startTagInit(String startTag) {
        if (_isRootTag) {
            header();
            outputDTD(startTag);
            _isRootTag = false;
        }

        if (_previousIsTag) {
            linefeed();
        }
        if (!_previousIsTextContent)
            indent();
    }

    public XMLOutputter startTag(String tagName) {
        startTagInit(tagName);

        _out.print(L_BRACE + tagName + R_BRACE);
        _tagNameStack.push(tagName);
        _previousIsTag = true;
        _previousIsTextContent = false;
        return this;
    }

    public XMLOutputter element(String tagName, XMLAttribute attributes, String textContent) throws InvalidXMLException {
        startTag(tagName, attributes);
        textContent(textContent);
        closeTag();
        return this;
    }

    public XMLOutputter element(String tagName, String textContent) throws InvalidXMLException {
        startTag(tagName);
        textContent(textContent);
        closeTag();
        return this;
    }


    /**
     * @param tagName
     * @param attributes
     * @return
     */
    public XMLOutputter startTag(String tagName, XMLAttribute attributes) {
        startTagInit(tagName);

        _out.print(L_BRACE + tagName);
        if (attributes != null) {
            if (attributes.length() > 0)
                _out.print(" " + attributes.toString(_contentFilter));
        }
        _out.print(R_BRACE);
        _tagNameStack.push(tagName);
        _previousIsTag = true;
        _previousIsTextContent = false;

        return this;
    }

    public XMLOutputter startTag(String tagName, XMLAttribute attributes, String textContent)
            throws InvalidXMLException {
        startTag(tagName, attributes);
        textContent(textContent);
        return this;
    }

    public XMLOutputter selfCloseTag(String tagName, XMLAttribute attributes) {
        startTagInit(tagName);
        _out.print(L_BRACE + tagName + " " + attributes.toString(_contentFilter) + ENDTAG_MARK + R_BRACE);
        linefeed();
        _previousIsTag = false;
        _previousIsTextContent = false;
        return this;
    }

    public XMLOutputter selfCloseTag(String tagName) {
        startTagInit(tagName);
        _out.print(L_BRACE + tagName + ENDTAG_MARK + R_BRACE);
        linefeed();
        _previousIsTag = true;
        _previousIsTextContent = false;
        return this;
    }

    public XMLOutputter closeTag() throws InvalidXMLException {
        if (_tagNameStack.empty())
            throw new InvalidXMLException("too many closeTag invokation");
        String closedTagName = (String) _tagNameStack.pop();

        if (_previousIsTag) {
            linefeed();
            indent();
        }
        if (!_previousIsTag && !_previousIsTextContent)
            indent();
        _out.print(L_BRACE + ENDTAG_MARK + closedTagName + R_BRACE);
        //linefeed();
        _previousIsTag = true;
        _previousIsTextContent = false;
        return this;
    }

    public XMLOutputter textContent(String elementContent) throws InvalidXMLException {
        boolean isLongContent = elementContent.length() > 40;
        if (_previousIsTag && isLongContent) {
            linefeed();
            indent();
        }
        if (_tagNameStack.empty())
            throw new InvalidXMLException("text content must be enclosed in a tag");
        _out.print(_contentFilter.filter(elementContent));
        _previousIsTag = false;
        _previousIsTextContent = true;
        if (isLongContent) {
            _previousIsTag = true;
            _previousIsTextContent = false;
        }
        return this;
    }

    public XMLOutputter cdata(String cdataContent) {
        _out.print(XMLUtil.createCDATA(cdataContent));
        return this;
    }


    public XMLOutputter PI(String target, String content) {
        _out.print("<?");
        _out.print(target);
        _out.print(" ");
        _out.print(content);
        _out.print("?>");
        return this;
    }

    /**
     * 最後にこのメソッドを呼ぶことで、自分でcloseTag()を呼ばなくても、 自動でスタック内の全てのタグを閉じてくれる
     */
    public void endOutput() throws InvalidXMLException {
        while (!_tagNameStack.empty()) {
            closeTag();
        }
        _out.flush();
    }

    public void closeStream() {
        _out.close();
    }

    void indent() {
        int depth = _tagNameStack.size();
        for (int i = 0; i < depth * 2; i++)
            _out.print(" ");
    }

    void linefeed() {
        _out.println();
    }

    public void setContentFilter(TextContentFilter filter) {
        _contentFilter = filter;
    }

    protected PrintWriter _out;
    Stack _tagNameStack = new Stack();
    boolean _previousIsTag = false;
    boolean _previousIsTextContent = false;
    protected String L_BRACE = "<";
    protected String R_BRACE = ">";
    protected String ENDTAG_MARK = "/";
    String _DTDFile = null;
    protected boolean _isRootTag = true;
    boolean _omitHeader = false;

    DTDDeclaration _dtdDeclaration = null;

    TextContentFilter _contentFilter = new HTMLFilter();
}

//--------------------------------------
// $Log: XMLOutputter.java,v $
// Revision 1.21  2004/11/26 06:18:36  leo
// *** empty log message ***
//
// Revision 1.20  2004/09/21 01:45:54  leo
// 継承用にprotectedに変更
//
// Revision 1.19  2004/09/16 10:14:15  leo
// header, outputDTDをprotectedに
//
// Revision 1.18  2004/09/16 10:08:11  leo
// PI, cdataを追加
//
// Revision 1.17  2004/08/09 09:31:17  leo
// indentを調整
//
// Revision 1.16  2004/08/09 09:20:35  leo
// メソッド追加
//
// Revision 1.15  2004/08/07 12:30:11  leo
// Filterを切り替えられるようにしました
//
// Revision 1.14  2004/08/02 09:55:42  leo
// *** empty log message ***
//
// Revision 1.13  2004/08/01 08:19:36  leo
// BasicTableにhasRowLabelを追加
// XMLOutputterで、java.io.writerを使えるように変更
// （JSPのwriterがjava.io.Writerの派生クラスのため)
//
// Revision 1.12  2004/07/22 14:16:38  leo
// DTD宣言は一つに
//
// Revision 1.11  2004/07/22 13:23:22  leo
// DTD宣言を複数呼べるように変更
//
// Revision 1.10 2004/07/21 14:55:07 leo
// DTDのSetterを追加
//
// Revision 1.9 2004/07/21 08:07:24 leo
// *** empty log message ***
//
// Revision 1.8 2004/07/21 02:49:38 leo
// PrintStreamから、OutputStreamに変更
//
// Revision 1.7 2004/07/13 08:04:07 leo
// selfCloseTagを追加
//
// Revision 1.6 2004/07/12 08:00:25 leo
// &, <, >, ", 'をentity参照に置換するように改良
//
// Revision 1.5 2004/07/12 07:26:52 leo
// XMLOutputterの修正
//
// Revision 1.4 2004/07/07 15:04:22 leo
// Antで自動コンパイル、テスト実行を記述
//
// Revision 1.3 2004/07/07 08:00:27 leo
// タグの閉じすぎのときにInvalidXMLExceptionを投げるようにしました
//
// Revision 1.2 2004/06/26 07:33:19 leo
// formatting XML outputs
//
// Revision 1.1 2004/05/03 17:02:35 leo
// XMLOutputterを書き始めました。テストコードは未製作
//
//--------------------------------------
