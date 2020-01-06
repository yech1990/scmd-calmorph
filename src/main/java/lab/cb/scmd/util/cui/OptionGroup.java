//--------------------------------------
// SCMD Project
// 
// OptionGroup.java 
// Since:  2004/04/22
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.cui;

import lab.cb.scmd.exception.SCMDException;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * @author leo
 */
public class OptionGroup extends OptionComposite {
    private String _groupName;
    private boolean _isExclusive = false;
    private LinkedList _optionList = new LinkedList();

    /**
     * @param groupName オプショングループの名前
     */
    OptionGroup(String groupName) {
        super();
        _groupName = groupName;
    }


    /**
     * @param groupName   オプショングループの名前
     * @param isExclusive trueにセットされた場合、このグループ内のオプションは排他的になる（他のグループのオプションと同時に使用できなくなる)
     */
    public OptionGroup(String groupName, boolean isExclusive) {
        super();
        _groupName = groupName;
        _isExclusive = isExclusive;
    }

    public boolean isGroup() {
        return true;
    }

    String getGroupName() {
        return _groupName;
    }

    boolean isExclusive() {
        return _isExclusive;
    }

    public String createHelpMessage() {
        OptionDescriptionContainer container = new OptionDescriptionContainer();
        collectOptionDescriptions(container);
        return container.toString();
    }

    public void collectOptionDescriptions(OptionDescriptionContainer container) {
        if (!_groupName.equals(""))
            container.addDescription("[" + _groupName + "]");
        for (Object o : _optionList) {
            OptionComposite component = (OptionComposite) o;
            component.collectOptionDescriptions(container);
        }
    }

    /**
     * グループ内にオプションを追加
     *
     * @param option 追加するオプション
     */
    public OptionGroup add(OptionComposite option) {
        _optionList.add(option);
        return this;
    }

    void putOptionsInTheGroup(TreeMap optionMap, TreeMap optionID2GroupMap) throws SCMDException {
        for (Object o : _optionList) {
            Option element = (Option) o;
            int optionID = element.getOptionID();
            if (optionMap.get(optionID) != null)
                throw new SCMDException("duplilcate option id: " + optionID);
            optionMap.put(element.getOptionID(), element);
            optionID2GroupMap.put(element.getOptionID(), _groupName);
        }
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.cui.OptionComposite#findByLongOptionName(java.lang.String)
     */
    public Option findByLongOptionName(String longOption) {
        Option opt = null;
        for (Object o : _optionList) {
            OptionComposite component = (OptionComposite) o;
            opt = component.findByLongOptionName(longOption);
            if (opt != null)
                break;
        }
        return opt;
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.cui.OptionComposite#findByShortOptionName(java.lang.String)
     */
    public Option findByShortOptionName(String shortOption) {
        Option opt = null;
        for (Object o : _optionList) {
            OptionComposite component = (OptionComposite) o;
            opt = component.findByShortOptionName(shortOption);
            if (opt != null)
                break;
        }
        return opt;
    }
}


//--------------------------------------
// $Log: OptionGroup.java,v $
// Revision 1.3  2004/07/07 15:04:22  leo
// Antで自動コンパイル、テスト実行を記述
//
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