//--------------------------------------
// SCMDProject
// 
// HTMLFilter.java 
// Since: 2004/08/07
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author leo
 */
public class HTMLFilter implements TextContentFilter {
    private Pattern cdataPattern = Pattern.compile("<!\\[CDATA\\[([^\\]]*)\\]\\]>");

    /**
     *
     */
    HTMLFilter() {
    }

    /**
     * &, <, >, ", ' の文字列をentitiy参照に変換する
     *
     * @param content 変換する文字列
     * @return
     */
    public String filter(String content) {

        Matcher m = cdataPattern.matcher(content);
        if (m.matches()) {
            // CDATA sectionの中身を返す
            return m.group(1);
        }

        StringBuilder substituedStringBuffer = new StringBuilder(content.length());
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            switch (c) {
                case '<':
                    substituedStringBuffer.append("&lt;");
                    break;
                case '>':
                    substituedStringBuffer.append("&gt;");
                    break;
                case '"':
                    substituedStringBuffer.append("&quot;");
                    break;
                case '\'':
                    substituedStringBuffer.append("&apos;");
                    break;
                case '&':
                    substituedStringBuffer.append("&amp;");
                    break;
                default:
                    substituedStringBuffer.append(c);
            }
        }
        return substituedStringBuffer.toString();
    }

}

//--------------------------------------
// $Log: HTMLFilter.java,v $
// Revision 1.2  2004/08/26 04:28:13  leo
// CDATAの処理を追加
//
// Revision 1.1  2004/08/07 12:30:11  leo
// Filterを切り替えられるようにしました
//
//--------------------------------------
