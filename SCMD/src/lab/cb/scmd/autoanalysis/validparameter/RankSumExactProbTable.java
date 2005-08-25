//--------------------------------------
//SCMD Project
//
//RankSumExactProbTable.java 
//Since:  2004/09/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.validparameter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class RankSumExactProbTable {
	HashMap tableList=new HashMap();
	
	public RankSumExactProbTable(){}
	public ArrayList getTable(int controlSize,int mutantSize) throws IOException{
		if(tableList.containsKey(new Integer(controlSize))==false){
			tableList.put(new Integer(controlSize),new HashMap());
		}
		HashMap table=(HashMap)tableList.get(new Integer(controlSize));
		if(table.containsKey(new Integer(mutantSize))==false){
			table.put(new Integer(mutantSize),readExactProbTable(controlSize,mutantSize));
		}
		return (ArrayList)table.get(new Integer(mutantSize));
	}
	private ArrayList readExactProbTable(int controlSize,int mutantSize) throws IOException{
		String filename=new String("./table/table"+controlSize+"_"+mutantSize+".xls");
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buf=br.readLine();
		if(buf==null){
			System.err.println("no input");
			System.exit(-1);
		}
		String[] line=buf.split("\t");
		if(controlSize!=Integer.parseInt(line[0]) || mutantSize!= Integer.parseInt(line[1])){
			System.err.println("wrong exact probability table.");
			System.exit(-1);
		}
		ArrayList table=new ArrayList();
		while((buf=br.readLine())!=null ){
			line=buf.split("\t");
			if(line.length<2)break;
			table.add(new Double(Double.parseDouble(line[1])));
		}
		return table;
	}
	
}

//--------------------------------------
//$Log: RankSumExactProbTable.java,v $
//Revision 1.3  2004/12/09 10:06:27  nakatani
//*** empty log message ***
//
//Revision 1.2  2004/12/09 03:49:25  nakatani
//*** empty log message ***
//
//Revision 1.1  2004/12/09 03:26:07  nakatani
//*** empty log message ***
//
//--------------------------------------
