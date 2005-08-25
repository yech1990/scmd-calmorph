//--------------------------------------
// SCMD Project
// 
// TableTypeSchema.java 
// Since:  2004/04/19
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.grouping;

import java.io.*;
import java.util.*;

import lab.cb.scmd.exception.SCMDException;

/**
 * 
 * schemaファイルは、以下の形式 （空白orTAB区切りで一行ずつ対応を記述する)
 * <pre>
 * # schema of	table A	
 * # (attribute name, original	table, original attributename)
 * image_number	T_ORF	image_number
 * cell_	T_ORF	cell_id
 * Agroup	T_ORF	Agroup
 * C11-1_A	T_CONA_BASIC	C11-1
 * C12-1_A	T_CONA_BASIC	C12-1
 * C13_A	T_CONA_BASIC	C13
 * C103_A	T_CONA_BIOLOGICAL	C103
 * C104_A	T_CONA_BIOLOGICAL	C104
 * C115_A	T_CONA_BIOLOGICAL	C115
 * C126_A	T_CONA_BIOLOGICAL	C126
 * ...
 * </pre>
 * @author leo
 * 
 */
public class TableSchema {
	public TableSchema(String schemaFile) throws SCMDException {
		loadFromFile(schemaFile);
	}

	public int numRule(){
		return _labelList.size();
	}
	public AttributePosition getAttributePosition(int ruleIndex) throws SCMDException {
		if(ruleIndex > _labelList.size())
			throw new SCMDException("invalid rule index: " + ruleIndex + " out of " + _labelList.size());
		return (AttributePosition) _labelToSourcePosition.get(_labelList.get(ruleIndex));
	}

	public void outputContents(PrintStream out) {
		for (Iterator li = _labelList.iterator(); li.hasNext();) {
			String label = (String) li.next();
			AttributePosition pos =
				(AttributePosition) _labelToSourcePosition.get(label);
			out.println(label + "\t" + pos.toString());
		}
	}

	public void outputLabel(PrintWriter out) {
		for (int i = 0; i < _labelList.size() - 1; i++) {
			out.print((String) _labelList.get(i) + "\t");
		}
		out.println((String) _labelList.get(_labelList.size() - 1));
	}

	void loadFromFile(String schemaFile) throws SCMDException {
		try {
			BufferedReader fileReader =
				new BufferedReader(new FileReader(schemaFile));
			// read labels
			String line;
			int lineCount = 0;
			while ((line = fileReader.readLine()) != null) {
				lineCount++;
				if (line.startsWith("#"))
					continue; // comment line
				StringTokenizer tokenizer = new StringTokenizer(line, "\t ");
				try {
					String newAttributeName = tokenizer.nextToken();
					String originalTableName = tokenizer.nextToken();
					String originalAttributeName = tokenizer.nextToken();
					_labelList.add(newAttributeName);
					int tableType = TableTypeServer.getTableType(originalTableName);
					if(tableType == -1)
						throw new SCMDException(originalTableName + " is invalid data file type in " + schemaFile + " line: " + lineCount);
					_labelToSourcePosition.put(
						newAttributeName,
						new AttributePosition(tableType, originalAttributeName));
				} catch (NoSuchElementException ne) {
					// invalid row
				}
			}
			fileReader.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			throw new SCMDException(
				"error occured while loading " + schemaFile);
		}
	}
	Vector _labelList = new Vector();
	HashMap _labelToSourcePosition = new HashMap();
}



//--------------------------------------
// $Log: TableSchema.java,v $
// Revision 1.2  2004/07/20 07:51:00  sesejun
// SCMDServerから呼び出せるように、一部メソッドをpublicへ変更。
// AttributePositionを、TableSchemaから独立。
//
// Revision 1.1  2004/04/23 02:24:25  leo
// move NucleusStageClassifier to lab.cb.scmd.autoanalysys.grouping
//
// Revision 1.1  2004/04/22 04:08:46  leo
// first ship for /home/lab.cb.scmd/CVS
//
// Revision 1.1  2004/04/22 02:53:31  leo
// first ship of SCMDProject
//
// Revision 1.4  2004/04/22 02:30:15  leo
// grouping complete
//
//--------------------------------------

