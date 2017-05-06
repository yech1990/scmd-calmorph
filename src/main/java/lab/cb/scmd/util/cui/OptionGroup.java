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

import java.util.*;

import lab.cb.scmd.exception.SCMDException;

/**
 * @author leo
 *
 */
public class OptionGroup extends OptionComposite
{
	/**
	 * @param groupName オプショングループの名前
	 */
	public OptionGroup(String groupName)
	{
		super();
		_groupName = groupName;
	}
	/**
	 * @param groupName オプショングループの名前
	 * @param isExclusive trueにセットされた場合、このグループ内のオプションは排他的になる（他のグループのオプションと同時に使用できなくなる)
	 */
	public OptionGroup(String groupName, boolean isExclusive)
	{
		super();
		_groupName = groupName;
		_isExclusive = isExclusive;
	}
	
	public boolean isGroup() { return true; }
	public String getGroupName() { return _groupName; }

	
	public boolean isExclusive()
	{ return _isExclusive; }

	public String createHelpMessage()
	{
		OptionDescriptionContainer container = new OptionDescriptionContainer();
		collectOptionDescriptions(container);
		return container.toString();
	}
	public void collectOptionDescriptions(OptionDescriptionContainer container)
	{
		if(!_groupName.equals(""))	
			container.addDescription("[" + _groupName + "]");
		for (Iterator li = _optionList.iterator(); li.hasNext();)
		{
			OptionComposite component = (OptionComposite) li.next();
			component.collectOptionDescriptions(container);
		}
	}
	/**
	 * グループ内にオプションを追加
	 * @param option 	追加するオプション
	 */
	public OptionGroup add(OptionComposite option)
	{
		_optionList.add(option);
		return this;
	}
	
	public void putOptionsInTheGroup(TreeMap optionMap, TreeMap optionID2GroupMap) throws SCMDException
	{ 
		for (Iterator oi = _optionList.iterator(); oi.hasNext();)
		{
			Option element = (Option) oi.next();
			int optionID = element.getOptionID();
			if(optionMap.get(new Integer(optionID)) != null)
				throw new SCMDException("duplilcate option id: " + optionID);
			optionMap.put(new Integer(element.getOptionID()), element);
			optionID2GroupMap.put(new Integer(element.getOptionID()), _groupName);
		}		
	}

	String _groupName;
	boolean _isExclusive = false;
	LinkedList _optionList = new LinkedList();
	/* (non-Javadoc)
	 * @see lab.cb.scmd.util.cui.OptionComposite#findByLongOptionName(java.lang.String)
	 */
	public Option findByLongOptionName(String longOption)
	{
		Option opt = null;
		for(Iterator li = _optionList.iterator(); li.hasNext();)
		{
			OptionComposite component = (OptionComposite) li.next();
			opt = component.findByLongOptionName(longOption);
			if(opt != null)
				break;
		}
		return opt;
	}
	/* (non-Javadoc)
	 * @see lab.cb.scmd.util.cui.OptionComposite#findByShortOptionName(java.lang.String)
	 */
	public Option findByShortOptionName(String shortOption)
	{
		Option opt = null;
		for(Iterator li = _optionList.iterator(); li.hasNext();)
		{
			OptionComposite component = (OptionComposite) li.next();
			opt = component.findByShortOptionName(shortOption);
			if(opt != null)
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