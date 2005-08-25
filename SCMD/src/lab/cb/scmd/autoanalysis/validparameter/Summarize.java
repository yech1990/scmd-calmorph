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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import lab.cb.scmd.autoanalysis.grouping.CalMorphTable;
import lab.cb.scmd.autoanalysis.grouping.DataFileName;
import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.cui.Option;
import lab.cb.scmd.util.cui.OptionParser;
import lab.cb.scmd.util.cui.OptionWithArgument;
import lab.cb.scmd.util.io.NullPrintStream;
import lab.cb.scmd.util.table.BasicTable;
import lab.cb.scmd.util.table.Cell;
import lab.cb.scmd.util.table.FlatTable;

/**
* @author nakatani
* 
* NucleusStageClassifier, calcgroupstat, makexls で作成したパラメータから、
* 501のパラメータ（param.xlsで指定したパラメータ）を抽出する
* 
*/
public class Summarize {
	OptionParser		parser				= new OptionParser();
	String 				targetDir           = null;
	String				parameterFile       = null;
	String              orfFile             = null;
	String              outputFile          = null;
    int                 ignoreThreshold     = 0;
	static final String SEP = File.separator;
	
	// option IDs
	final static int	OPT_HELP			= 0;
	final static int	OPT_VERBOSE			= 1;
	final static int	OPT_TARGETDIR		= 2;
	final static int    OPT_PARAMETER       = 3;
	final static int    OPT_ORF             = 4;
	final static int    OPT_OUTPUT          = 5;
    final static int    OPT_IGNORETHRESHOLD = 6;
    
    PrintStream    _log = new NullPrintStream();
	
	Summarize(){}
    
	public Summarize(String target_dir, String parameter_file) throws SCMDException, IOException{
		targetDir = target_dir;
		parameterFile = parameter_file;
		readOutputParameterList();
		readAllData();
	}

    protected TreeSet<String> getOrfIgnore(int threshold) throws SCMDException{
		CalMorphTable versatileTable = new CalMorphTable(targetDir+SEP+DataFileName.VERSATILE);
		TreeSet<String> IgnoreList = new TreeSet<String> ();
		int orfSize=versatileTable.getRowSize();
		for(int i=0;i<orfSize;++i){
			Cell c = versatileTable.getCell(i,1);
			if(c.isValidAsDouble()==false){
				System.err.println("Error in Summarize.getOrfIgnore(). format error in "+targetDir+SEP+DataFileName.VERSATILE+'?');
			}		
			int sampleNumber=(int)c.doubleValue();
			if(sampleNumber<threshold){
				String orf=versatileTable.getCell(i,0).toString();
				IgnoreList.add(orf);
				_log.println("ignore "+orf+". sample number="+sampleNumber+" < threshold="+threshold);				
			}
		}
		return IgnoreList;
	}

    /**
	 * @param args
	 * @throws SCMDException
	 * @throws IOException
	 */
	public void setupByArguments(String[] args) throws SCMDException, IOException
	{
		setupOptionParser();
		parser.getContext(args);

		if(parser.isSet(OPT_HELP)) {
            printUsage(0);
            System.exit(0);
        }
		if(parser.isSet(OPT_VERBOSE))
		    _log = System.out;
        
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
        if(parser.isSet(OPT_IGNORETHRESHOLD)) {
            ignoreThreshold = Integer.parseInt(new String(parser.getValue(OPT_IGNORETHRESHOLD)));
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
            s.outputSummary();
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
	
	
    Vector<String> outputParams = new Vector<String>();
    Vector<String> outputOrfList = new Vector<String>();
    OutputResult result = new OutputResult();
    
    //取り出した値を格納するコンテナ
    class OutputResult {
        Map<String, Map<String, String>> table = new TreeMap<String, Map<String, String>>();
        TreeSet<String> cols = new TreeSet<String> ();
        String TABLENAME = ""; 
        String NULLVALUE = ".";
        
        public void setTableName(String name) {
            TABLENAME = name;
        }
        
        public String getTableName() {
            return TABLENAME;
        }
        
        public String get(String rowname, String colname) {
            Map<String, String> onerow = table.get(rowname);
            if(onerow == null)
                return NULLVALUE;
            String str = onerow.get(colname);
            if(str == null)
                return NULLVALUE;
            return str;
        }
        
        public void set(String rowname, String colname, String value) {
            if(!table.containsKey(rowname))
                table.put(rowname, new TreeMap<String, String>());
            Map<String, String> onerow = table.get(rowname);
            onerow.put(colname, value);
            if(!cols.contains(colname)) 
                cols.add(colname);
        }
        
        public boolean colExists(String colname) {
            return cols.contains(colname);
        }

        /**
         * @return
         */
        public Vector<String> getRowList() {
            int size = table.keySet().size();
            String[] liststr = new String [size];
            Vector<String> list = new Vector<String> ();
            int n = 0;
            for(String r: table.keySet()) {
                liststr[n++] = r;
            }
            Arrays.sort(liststr);
            for(String r: liststr) {
                list.add(r);
            }
            return list;
        }
    }
	
	private void readOutputParameterList() throws IOException {		
		int parameterIndex=0;
        result.setTableName("name");
        
		BufferedReader parameterListReader = null;
		try {
			parameterListReader = new BufferedReader(new FileReader(parameterFile));
		} catch (FileNotFoundException e) {
			System.err.println("Error in Summarize.readOutputParameterList(). (file not found) filename="+parameterFile);
			e.printStackTrace();
		}
		String line;
		while( (line=parameterListReader.readLine())!=null){
            String[] cols = line.split("\t+");
            for(String c: cols) 
                outputParams.add(c);
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
		while( (line=orfListReader.readLine()) != null){
            String[] cols = line.split("\t+");
            for(String c: cols) {
                outputOrfList.add(c);
            }
		}
	}
	
	public void readAllData() throws SCMDException{
		for(int i=0 ; i < DataFileName.GROUP_FILE_NAME.length ; ++i){
			readFile(targetDir + SEP + DataFileName.GROUP_FILE_NAME[i] );
		}
		//in case specified parameter does not exist
		boolean no_such_param = false;
        for(String s: outputParams) {
            if( !result.colExists(s) ) {
                no_such_param = true;
                _log.println("Error. no such parameter. " + s);
            }
        }
		if(no_such_param){
			_log.println("target directory = "+targetDir);
			System.exit(-1);
		}
	}

    private void readFile(String filename) throws SCMDException{
		BasicTable cmt = new FlatTable(filename, true, true);
		int colSize = cmt.getColSize();
		int rowSize = cmt.getRowSize();

        _log.println("reading " + filename + "\t" + colSize+" parameters"+"\t"+rowSize+" ORFs");

        for(int row = 0; row < rowSize; row++ ) {
            String rowname = cmt.getRowLabel(row);
            for(int col = 0; col < colSize; col++ ) {
                String colname = cmt.getColLabel(col);
                result.set(rowname, colname, cmt.getCell(row, col).toString());
            }
        }
        
	}

    public void outputSummary() throws IOException, SCMDException {
        outputSummary(getOrfIgnore(ignoreThreshold));
    }

    public void outputSummary(TreeSet<String> ignoreOrfList) throws IOException {
        PrintStream outfile = new PrintStream(new FileOutputStream(outputFile));
        
        outfile.print(result.getTableName());
        for(String p: outputParams) {
            outfile.print("\t" + p);
        }
        outfile.println();
        if(outputOrfList == null || outputOrfList.size() == 0 )
            outputOrfList = result.getRowList();
        for(String o: outputOrfList) {
            if(ignoreOrfList.contains(o))
                continue;
            outfile.print(o);
            for(String p: outputParams) {
                outfile.print("\t" + result.get(o, p));
            }
            outfile.println();
        }
    }
    
}


//--------------------------------------
//$Log: Summarize.java,v $
//Revision 1.3  2004/09/22 16:25:52  nakamu
//StringとCharの足し合わせ箇所を訂正
//
//Revision 1.2  2004/09/03 06:02:00  nakatani
//*** empty log message ***
//
//Revision 1.1  2004/09/02 08:31:44  nakatani
//　
//
//--------------------------------------
