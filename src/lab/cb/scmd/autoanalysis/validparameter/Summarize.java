//--------------------------------------
//SCMD Project
//
//Summarize.java 
//Since:  2004/09/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.validparameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import lab.cb.scmd.autoanalysis.grouping.CalMorphTable;
import lab.cb.scmd.autoanalysis.grouping.DataFileName;
import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.cui.Option;
import lab.cb.scmd.util.cui.OptionParser;
import lab.cb.scmd.util.cui.OptionWithArgument;
import lab.cb.scmd.util.table.Cell;
import lab.cb.scmd.util.table.TableIterator;

/**
* @author nakatani
*
*/
public class Summarize {
	OptionParser		parser				= new OptionParser();
	boolean				verbose             = true;
	String 				targetDir           = null;
	String				parameterFile       = null;
	String              orfFile             = null;
	String              outputFile          = null;
	static final String SEP=File.separator;
	
	// option IDs
	final static int	OPT_HELP			= 0;
	final static int	OPT_VERBOSE			= 1;
	final static int	OPT_TARGETDIR		= 2;
	final static int    OPT_PARAMETER       = 3;
	final static int    OPT_ORF             = 4;
	final static int    OPT_OUTPUT          = 5;
	
	Summarize(){}
	protected Summarize(String target_dir, String parameter_file) throws SCMDException, IOException{
		targetDir=target_dir;
		parameterFile=parameter_file;
		readOutputParameterList();
		readAllData();
	}
	protected Vector getParameterNames(){
		return outputHeader;
	}
	protected Double[] getValidDataForIthParameter(int Ith,HashMap ignoreOrfList){
		TableIterator ite=(TableIterator)outputDataTable.get(Ith);
		ite=(TableIterator)ite.clone();
		TableIterator orf=(TableIterator)outputDataTable.get(0);
		orf=(TableIterator)orf.clone();
		Vector v=new Vector();
		for(;orf.hasNext();){
			Cell c=(Cell)ite.next();
			if(ignoreOrfList!=null){
				Cell orfCell=(Cell)orf.next();
				if(ignoreOrfList.containsKey(orfCell.toString()))continue;
			}
			if(c.isValidAsDouble()){
				double d=c.doubleValue();
				if(d<0)continue;
				v.add(new Double(d));
			}
		}
		Double[] returnValue=new Double[v.size()];
		for(int i=0;i<v.size();++i){
			returnValue[i]=(Double)v.get(i);
		}
		return returnValue;
		//return (Double[])v.toArray();
	}
	protected HashMap getOrfIgnore(int threshold) throws SCMDException{
		CalMorphTable versatileTable = new CalMorphTable(targetDir+SEP+DataFileName.VERSATILE);
		HashMap IgnoreList = new HashMap();
		int orfSize=versatileTable.getRowSize();
		for(int i=0;i<orfSize;++i){
			Cell c = versatileTable.getCell(i,1);
			if(c.isValidAsDouble()==false){
				System.err.println("Error in Summarize.getOrfIgnore(). format error in "+targetDir+SEP+DataFileName.VERSATILE+'?');
			}		
			int sampleNumber=(int)c.doubleValue();
			if(sampleNumber<threshold){
				String orf=versatileTable.getCell(i,0).toString();
				IgnoreList.put(orf,orf);
				if(verbose){
					System.out.println("ignore "+orf+". sample number="+sampleNumber+" < threshold="+threshold);				
				}
			}
		}
		return IgnoreList;
	
	}
	/*
	protected HashMap getOrfIgnore(int threshold) throws SCMDException{
		CalMorphTable[] dataNumTables = new CalMorphTable[DataFileName.NUM_FILE_NAME.length]; 
		for(int i=0;i<dataNumTables.length;++i){
			String filename=targetDir+SEP+DataFileName.NUM_FILE_NAME[i];
			if(verbose)System.out.println("reading "+filename);
			dataNumTables[i]=new CalMorphTable(filename);
		}
		HashMap IgnoreList=new HashMap();
		int rowSize=dataNumTables[0].getRowSize();
		for(int i=0;i<rowSize;++i){
			int sampleNumber=0;
			for(int j=0;j<dataNumTables.length;++j){
				Cell c = dataNumTables[j].getCell(i,1);
				if(c.isValidAsDouble()==false){
					System.err.println("Error in Summarize.getOrfIgnore(). format error in "+targetDir+SEP+DataFileName.NUM_FILE_NAME[j]+'?');
				}
				sampleNumber+=c.doubleValue();
			}
			if(sampleNumber<threshold){
				String orf=dataNumTables[0].getCell(i,0).toString();
				IgnoreList.put(orf,orf);
				if(verbose){
					System.out.println("ignore "+orf+". sample number="+sampleNumber+" < threshold="+threshold);
				}
			}
		}
		return IgnoreList;
	}*/
	/**
	 * @param args
	 * @throws SCMDException
	 * @throws IOException
	 */
	public void setupByArguments(String[] args) throws SCMDException, IOException
	{
		setupOptionParser();
		parser.getContext(args);

		if(parser.isSet(OPT_HELP))printUsage(0);
		if(parser.isSet(OPT_VERBOSE))verbose=true;
		if(parser.isSet(OPT_TARGETDIR)){
			targetDir = new String(parser.getValue(OPT_TARGETDIR));
		}else{
			printUsage(-1);
		}
		if(parser.isSet(OPT_PARAMETER)){
			parameterFile = new String(parser.getValue(OPT_PARAMETER));
			readOutputParameterList();
		}else{
			printUsage(-1);
		}
		if(parser.isSet(OPT_ORF)){
			orfFile = new String(parser.getValue(OPT_ORF));
			readOutputOrfList();
		}
		if(parser.isSet(OPT_OUTPUT)){
			outputFile = new String(parser.getValue(OPT_OUTPUT));
		}else{
			printUsage(-1);
		}
	}

	private void setupOptionParser() throws SCMDException
	{
		parser.setOption(new Option(OPT_HELP, "h", "help", "diaplay help message"));
		parser.setOption(new Option(OPT_VERBOSE, "v", "verbose", "display verbose messages"));
		parser.setOption(new OptionWithArgument(OPT_TARGETDIR, "d", "target_dir", "DIR","set input directory base"));		
		parser.setOption(new OptionWithArgument(OPT_PARAMETER, "p", "parameter_file", "FILE", "set parameter file"));
		parser.setOption(new OptionWithArgument(OPT_ORF,"o","orf_file","FILE","set orf file"));
		parser.setOption(new OptionWithArgument(OPT_OUTPUT,"f","output_file","FILE","set output file"));
	}
	private void printUsage(int exitCode)
	{
		System.out.println("Usage: Summarize [option]");
		System.out.println(parser.createHelpMessage());
		System.exit(exitCode);
	}


	public static void main(String[] args) {
		try
		{
			Summarize s=new Summarize();	
			s.setupByArguments(args);
			s.readAllData();
			s.outputSummary(s.getOrfIgnore(200));
		}
		catch(SCMDException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	
	//
	HashMap outputParameterList	= new HashMap();
	Vector  outputHeader        = new Vector();
	HashMap outputOrfList       = new HashMap();
	Vector  outputDataTable     = new Vector();
	
	private void readOutputParameterList() throws IOException {		
		int parameterIndex=0;
		outputParameterList.put("name",new Integer(parameterIndex++));
		outputHeader.add("name");
		BufferedReader parameterListReader = null;
		try {
			parameterListReader = new BufferedReader(new FileReader(parameterFile));
		} catch (FileNotFoundException e) {
			System.err.println("Error in Summarize.readOutputParameterList(). (file not found) filename="+parameterFile);
			e.printStackTrace();
		}
		String line;
		while( (line=parameterListReader.readLine())!=null){
			java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(line, "\t");
			while (tokenizer.hasMoreTokens())
			{
				String token = tokenizer.nextToken();
				outputParameterList.put(token,new Integer(parameterIndex++));
				outputHeader.add(token);
			}
		}
	}
	private void readOutputOrfList() throws IOException {
		BufferedReader orfListReader = null;
		try {
			orfListReader = new BufferedReader(new FileReader(orfFile));
		} catch (FileNotFoundException e) {
			System.err.println("Error in Summarize.readOutputOrfList(). (file not found) filename="+orfFile);
			e.printStackTrace();
		}
		String line;
		while( (line=orfListReader.readLine())!=null){
			java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(line, "\t");
			while (tokenizer.hasMoreTokens())
			{
				String token = tokenizer.nextToken();
				outputOrfList.put(token,token);
			}
		}
	}
	
	public void readAllData()throws SCMDException{
		outputDataTable.setSize(outputParameterList.size());
		for(int i=0;i<DataFileName.GROUP_FILE_NAME.length;++i){
			readFile(targetDir+SEP+DataFileName.GROUP_FILE_NAME[i]);
		}
		//in case specified parameter does not exist
		boolean no_such_param=false;
		for(int i=1;i<outputDataTable.size();++i){
			if(outputDataTable.get(i)==null){
				no_such_param=true;
				System.out.println("Error. no such parameter. "+outputHeader.get(i));
			}
		}
		if(no_such_param){
			System.out.println("target directory = "+targetDir);
			System.exit(-1);
		}
	}
	private void readFile(String filename)throws SCMDException{
		CalMorphTable cmt=new CalMorphTable(filename);
		int colSize=cmt.getColSize();
		int rowSize=cmt.getRowSize();
		if(verbose){
			System.out.println("reading "+filename+"\t"+colSize+" parameters"+"\t"+rowSize+" ORFs");
		}
		Vector parameters=cmt.getColLabelList();
		Iterator i=parameters.iterator();
		while(i.hasNext()){
			String parameterName=(String)i.next();
			if(outputParameterList.containsKey(parameterName)==false)continue;
			int parameterIndex=((Integer)outputParameterList.get(parameterName)).intValue();
			outputDataTable.set(parameterIndex,cmt.getVerticalIterator(parameterName));
		}
	}
	public void outputSummary(HashMap ignoreOrfList) throws IOException{
		PrintWriter outfile = new PrintWriter(new FileWriter(outputFile));
		Iterator i=outputHeader.iterator();
		for(;i.hasNext();){
			String s=(String)i.next();
			outfile.print(s+"\t");
		}
		outfile.println();
		
		while(true){
			Iterator j=outputDataTable.iterator();
			TableIterator ti=(TableIterator)j.next();
			if(ti.hasNext()==false)break;
			Cell c=(Cell)ti.next();
			String orfName=c.toString();
			boolean outputThisOrf=false;
			if(orfFile==null || outputOrfList.containsKey(orfName)){
				if(orfFile==null && ignoreOrfList!=null && ignoreOrfList.containsKey(orfName) ){	
				}else{
					outputThisOrf=true;
					outfile.print(orfName+"\t");
				}
			}
			for(;j.hasNext();){
				TableIterator ite=(TableIterator)j.next();
				Cell cell=(Cell)ite.next();
				if(outputThisOrf)outfile.print(cell.toString()+"\t");
			}
			if(outputThisOrf)outfile.println();
		}
		outfile.close();
	}
}


//--------------------------------------
//$Log: Summarize.java,v $
//Revision 1.3  2004/09/22 16:25:52  nakamu
//String‚ÆChar‚Ì‘«‚µ‡‚í‚¹‰ÓŠ‚ð’ù³
//
//Revision 1.2  2004/09/03 06:02:00  nakatani
//*** empty log message ***
//
//Revision 1.1  2004/09/02 08:31:44  nakatani
//@
//
//--------------------------------------
