//--------------------------------------
// SCMD Project
// 
// OptionParser.java 
// Since:  2004/04/21
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.cui;

import java.util.*;

import lab.cb.scmd.exception.SCMDException;

//import lab.cb.scmd.exception.*;


/**
 * コマンドライン引数を解析するクラス
 * @author leo
 *
 */
public class OptionParser
{
	public OptionParser()
	{}
	public void setRequirementForNonOptionArgument()
	{
		_requireNonOptionArgument = true;
	}
	public void setOption(Option option) throws SCMDException
	{
		int optionID = option.getOptionID();
		if (_optionMap.get(new Integer(optionID)) != null)
			throw new SCMDException("duplicate option ID: " + optionID);
		_optionMap.put(new Integer(optionID), option);
		_globalOption.add(option);
	}
	public void addOptionGroup(OptionGroup optionGroup) throws SCMDException
	{
		_globalOption.add(optionGroup);
		if(optionGroup.isExclusive())
			_incompatibleGroup.add(optionGroup.getGroupName());
		optionGroup.putOptionsInTheGroup(_optionMap, _optionIDToGroupMap);
	}

	public String createHelpMessage()
	{
		return _globalOption.createHelpMessage();
	}
	
	void setGroupHash(Option opt)
	{
		int id = opt.getOptionID();
		String groupName = (String) _optionIDToGroupMap.get(new Integer(id));
		if(groupName == null)
			return;
		
		_setGroupHash.add(groupName);
	}

	/**
	 * コマンドライン引数をparsingする
	 * - あらかじめsetOptionでoptionの種類を設定しておく
	 * @param args コマンドライン引数を渡す
	 * @throws SCMDException
	 */
	public void getContext(String[] args) throws SCMDException
	{
		_args = args;
		int index = 0;
		while(index < _args.length)
		{
			String arg = _args[index];
			if(arg.startsWith("--"))
			{
				// long name option
				int splitPos = arg.indexOf('=');
				String longOptionName, value;
				if(splitPos == -1)
				{
					// no value is found
					longOptionName = arg.substring(2);
					Option opt = _globalOption.findByLongOptionName(longOptionName);
					if(opt == null)
						throw new SCMDException("unknown option --" + longOptionName);
					if(opt.takeArgument())
						throw new SCMDException("parameter value is required for --" + longOptionName);
					opt.set();
					setGroupHash(opt);
				}
				else
				{
					// an argumen value is found
					longOptionName = arg.substring(2, splitPos);
					value = arg.substring(splitPos+1);
					Option opt = _globalOption.findByLongOptionName(longOptionName);
					if(opt == null)
						throw new SCMDException("unknown option --" + longOptionName);
					if(opt instanceof OptionWithArgument)
					{
						OptionWithArgument optWithArg = (OptionWithArgument) opt;
						optWithArg.set();
						setGroupHash(optWithArg);
						optWithArg.setArgument(value);						
					}
					else
						throw new SCMDException("syntax error --" + longOptionName);
				}
			}
			else if(arg.startsWith("-"))
			{
				// short name option
				String shortOptionList = arg.substring(1);
				for(int i=0; i<shortOptionList.length(); i++)
				{
					String shortOption = shortOptionList.substring(i, i+1);
					Option opt = _globalOption.findByShortOptionName(shortOption);
					if(opt == null)
						throw new SCMDException("unknown option -" + shortOption);
					if(opt.takeArgument())
					{
						if(shortOptionList.length() != 1)
							throw new SCMDException("options with argument must be isolated: -" + shortOption);
						if(opt instanceof OptionWithArgument)
						{
							OptionWithArgument optWithArg = (OptionWithArgument) opt;
							if(++index < _args.length)
								optWithArg.setArgument(_args[index]);
							else
								throw new SCMDException("parameter value is required for -" + shortOption);								
						}
					}
					opt.set();
					setGroupHash(opt);
				}
			}
			else 
			{
				// plain argument 
				_argumentList.add(arg);
			}
			index++;
		}
		
		// validate incompatible group options
		int incompatibleGroupCount = 0;
		LinkedList incompatibleGroupNameList = new LinkedList();
		for(Iterator hi = _setGroupHash.iterator(); hi.hasNext();)
		{
			String groupName = (String) hi.next();
			if(_incompatibleGroup.contains(groupName))
			{
				incompatibleGroupCount++;
				incompatibleGroupNameList.add(groupName);				
			}
		}
		if(incompatibleGroupCount >= 2)
			throw new SCMDException("options in incompatible groups " + incompatibleGroupNameList + " are set simultaneously");
		
		
		// check non option arguments 
		if(_requireNonOptionArgument && _argumentList.size() < 1)
			throw new LackOfArgumentException();
		
		
	}
	public LinkedList getArgumentList()
	{
		return _argumentList;
	}
	
	/** index番目の引数を返す （optionやoption付属の引数は除いてカウントする）
	 * @param index 
	 * @return
	 */
	public String getArgument(int index)
	{
	    LinkedList argList = getArgumentList();
	    if(index >= argList.size())
	    {
	        return "";
	    }
	    else
	        return (String) argList.get(index);
	}
	
	public boolean isSet(int optionID)
	{
		Option opt = findOption(optionID);
		return (opt == null) ? false : opt.isSet();
	}
	public String getValue(int optionID)
	{
		Option opt = findOption(optionID);
		return opt == null ? "" : opt.getArgumentValue();
	}
	
	protected Option findOption(int optionID)
	{
		OptionComposite opt = (OptionComposite) _optionMap.get(new Integer(optionID));
		if(opt == null) 
			return null;
		return (opt instanceof Option) ? (Option) opt : null;
	}
	
	TreeMap _optionMap = new TreeMap();
	TreeMap _optionIDToGroupMap = new TreeMap();
	LinkedList _incompatibleGroup = new LinkedList();
	HashSet _setGroupHash = new HashSet();

	private OptionGroup _globalOption = new OptionGroup("");
	private String[] _args = { "" };
	private LinkedList _argumentList = new LinkedList();
	private boolean _requireNonOptionArgument = false;
}

//--------------------------------------
// $Log: OptionParser.java,v $
// Revision 1.5  2004/09/01 07:01:36  leo
// getArgument
//
// Revision 1.4  2004/06/11 08:51:27  leo
// option でexclusive な異なるgroupに属するものを、
// 同時にセットしたときに例外を出せるようにした
//
// Revision 1.3  2004/05/05 16:27:50  leo
// コマンドラインのparse時のエラー(LackOfArgumentException)を追加。
// SCMDExceptionに、エラー表示の簡便化のためwhat()メソッドを追加。
//
// Revision 1.2  2004/04/30 02:25:52  leo
// OptionParserに引数の有無をチェックできる機能を追加
// setRequirementForNonOptionArgument()
//
// Revision 1.1  2004/04/22 04:08:46  leo
// first ship for /home/lab.cb.scmd/CVS
//
// Revision 1.1  2004/04/22 02:53:31  leo
// first ship of SCMDProject
//
// Revision 1.2  2004/04/22 02:30:15  leo
// grouping complete
//
// Revision 1.1  2004/04/21 05:35:05  leo
// add cui option reader (one test should be passed)
//
//--------------------------------------