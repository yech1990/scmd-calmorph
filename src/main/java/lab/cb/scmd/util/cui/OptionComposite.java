//--------------------------------------
// SCMD Project
// 
// OptionComposite.java 
// Since:  2004/04/22
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.cui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Composite Patternのベースクラス
 * グループ化されたオプション(OptionGroup)や、単一のオプション(Option)を、
 * それぞれファイルシステムのディレクトリ、フォルダのように扱い、再帰的に
 * 中身をたどるようにする。
 *
 * @author leo
 */
public abstract class OptionComposite {
    /**
     * helpメッセージ用の出力を集める
     *
     * @param container
     */
    abstract public void collectOptionDescriptions(OptionDescriptionContainer container);

    abstract public Option findByLongOptionName(String longOption);

    abstract public Option findByShortOptionName(String shortOption);

    abstract public boolean isGroup();

    /**
     * ヘルプメッセージを格納し、フォーマットして出力するクラス
     *
     * @author leo
     */
    class OptionDescriptionContainer {

        public void addDescription(String shortOptionColumn, String longOptionColumn, String descriptionColumn) {
            String[] column = new String[3];
            column[0] = shortOptionColumn;
            column[1] = longOptionColumn;
            column[2] = descriptionColumn;
            _columnList.add(column);
        }

        public void addDescription(String groupName) {
            String[] singleColumn = new String[1];
            singleColumn[0] = groupName;
            _columnList.add(singleColumn);
        }

        public String toString() {
            // calculate necessary width for each column
            int[] width = {0, 0, 0};
            for (Iterator ci = _columnList.iterator(); ci.hasNext(); ) {
                String[] line = (String[]) ci.next();
                if (line.length != 3)
                    continue; // single line
                for (int i = 0; i < line.length; i++)
                    width[i] = width[i] < line[i].length() ? line[i].length() : width[i];
            }
            for (int i = 0; i < width.length; i++)
                width[i]++;
            // print each options
            StringWriter strWriter = new StringWriter();
            PrintWriter out = new PrintWriter(strWriter);
            for (Iterator ci = _columnList.iterator(); ci.hasNext(); ) {
                String[] line = (String[]) ci.next();
                if (line.length == 1)
                    out.print(line[0]);  // group name
                else {
                    out.print(" "); /// left margin
                    for (int i = 0; i < line.length; i++) {
                        out.print(line[i]);
                        int numSpace = width[i] - line[i].length();
                        for (int j = 0; j < numSpace; j++)
                            out.print(" ");
                    }
                }
                out.println();
            }
            return strWriter.toString();
        }

        LinkedList _columnList = new LinkedList();
    }

}


//--------------------------------------
// $Log: OptionComposite.java,v $
// Revision 1.2  2004/06/11 08:51:27  leo
// option でexclusive な異なるgroupに属するものを、
// 同時にセットしたときに例外を出せるようにした
//
// Revision 1.1  2004/04/22 04:08:46  leo
// first ship for /home/lab.cb.scmd/CVS
//
// Revision 1.1  2004/04/22 02:53:31  leo
// first ship of SCMDProject
//
// Revision 1.1  2004/04/22 02:30:15  leo
// grouping complete
//
//--------------------------------------