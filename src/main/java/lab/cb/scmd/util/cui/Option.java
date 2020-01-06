//--------------------------------------
// SCMD Project
// 
// Option.java 
// Since:  2004/04/22
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.cui;

import lab.cb.scmd.exception.SCMDException;

/**
 * @author leo
 */
public class Option extends OptionComposite {
    String _shortOptionName;
    String _longOptionName;
    private int _optionID;
    private boolean _isSet = false;
    private String _description;

    /**
     * @param optionID        オプションにアクセスするためにユーザが与えるID
     * @param shortOptionName -h など、1文字オプション. 使わない場合は ""にする
     * @param longOptionName  --help など 長い名前のオプション.  使わない場合は ""にする
     * @param description     そのオプションの説明
     * @throws SCMDException
     */
    public Option(int optionID, String shortOptionName, String longOptionName, String description) throws SCMDException {
        _optionID = optionID;
        setOptionName(shortOptionName, longOptionName);
        _description = description;
    }

    public void collectOptionDescriptions(OptionDescriptionContainer container) {
        container.addDescription(getShortName(), getLongName(), getDescription());
    }

    /**
     * オプションをセットする
     */
    public void set() {
        _isSet = true;
    }

    public boolean isSet() {
        return _isSet;
    }

    public boolean takeArgument() {
        return false;
    }

    protected String getArgumentValue() {
        return "";
    }

    public boolean hasArgumentValue() {
        return false;
    }

    public String getShortName() {
        if (_shortOptionName.equals(""))
            return "";
        else
            return "-" + _shortOptionName + (_longOptionName.equals("") ? " " : ", ");
    }

    public String getLongName() {
        if (_longOptionName.equals(""))
            return "";
        else
            return "--" + _longOptionName;
    }

    private String getDescription() {
        return _description;
    }

    int getOptionID() {
        return _optionID;
    }

    private void setOptionName(String shortOptionName, String longOptionName) throws SCMDException {
        if (shortOptionName.length() > 1)
            throw new SCMDException("short option name must be a single character: -" + shortOptionName);
        _shortOptionName = shortOptionName;
        _longOptionName = longOptionName;
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.cui.OptionComposite#findByLongOptionName(java.lang.String)
     */
    public Option findByLongOptionName(String longOption) {
        if (longOption.equals(_longOptionName))
            return this;
        else
            return null;
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.cui.OptionComposite#findByShortOptionName(java.lang.String)
     */
    public Option findByShortOptionName(String shortOption) {
        if (shortOption.equals(_shortOptionName))
            return this;
        else
            return null;
    }

    /* (non-Javadoc)
     * @see lab.cb.scmd.util.cui.OptionComposite#isGroup()
     */
    public boolean isGroup() {
        return false;
    }
}


//--------------------------------------
// $Log: Option.java,v $
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
