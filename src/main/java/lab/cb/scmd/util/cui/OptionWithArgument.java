//--------------------------------------
// SCMD Project
// 
// OptionWithArgument.java 
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

public class OptionWithArgument extends Option {
    private String _argumentName;
    private String _argumentValue = "";

    public OptionWithArgument(int optionID, String shortName, String longName, String argumentName, String description) throws SCMDException {
        super(optionID, shortName, longName, description);
        _argumentName = argumentName;
    }

    public String getShortName() {
        if (_shortOptionName.equals(""))
            return "";
        else
            return "-" + _shortOptionName + (_longOptionName.equals("") ? " " + _argumentName : ", ");
    }

    public String getLongName() {
        if (_longOptionName.equals(""))
            return "";
        else
            return "--" + _longOptionName + "=" + _argumentName;
    }

    public boolean takeArgument() {
        return true;
    }

    void setArgument(String value) {
        _argumentValue = value;
    }

    public String getArgumentValue() {
        return _argumentValue;
    }
}


//--------------------------------------
// $Log: OptionWithArgument.java,v $
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