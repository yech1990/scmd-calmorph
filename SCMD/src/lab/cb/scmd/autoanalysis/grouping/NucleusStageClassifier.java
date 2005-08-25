// ------------------------------------
// SCMD Project
//  
// TableTypeSchema.java
// Since: 2004/04/15
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------
package lab.cb.scmd.autoanalysis.grouping;

import java.io.*;
import java.util.*;
import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.cui.*;
import lab.cb.scmd.util.io.NullPrintStream;

/**
 * orf.xlsのDgroupの状態(A, A1, B, C)によって, A, {A1, B}, Cの３つのグループに
 * CalMorphで出力されるデータを振り分けるためのクラス
 * 
 * @author leo
 *  
 */
public class NucleusStageClassifier implements TableFileName
{

	OptionParser		_optParser			= new OptionParser();
	ClassifierSetting	_settings			= new ClassifierSetting();
	PrintStream			_log				= new NullPrintStream();
	boolean				_autoSeach			= false;
	String				_baseDir			= ".";

	final int			OPT_HELP			= 0;
	final int			OPT_VERBOSE			= 1;
	final int			OPT_SETTING_FILE	= 2;
	final int			OPT_OUTPUTDIR		= 3;
	final int			OPT_AUTOSEARCH		= 4;
	final int			OPT_BASEDIR			= 5;

	public NucleusStageClassifier()
	{
		TableTypeServer.Initialize();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// initialize
		NucleusStageClassifier classifier = new NucleusStageClassifier();
		LinkedList inputFileList = classifier.setupByArguments(args);
		classifier.classify(inputFileList);
	}

	/**
	 * @param args
	 *            コマンドライン引数
	 * @return 入力ファイルのStringリスト
	 */
	public LinkedList setupByArguments(String[] args)
	{
		LinkedList inputFileList = new LinkedList();
		try
		{
			_optParser.setOption(new Option(OPT_HELP, "h", "help", "print help message"));
			_optParser.setOption(new Option(OPT_VERBOSE, "v", "verbose", "print some verbose messages"));
			_optParser
					.setOption(new OptionWithArgument(OPT_SETTING_FILE, "s", "", "SETTINGFILE", "read a setting file"));
			_optParser
					.setOption(new OptionWithArgument(OPT_OUTPUTDIR, "o", "outdir", "DIR", "specify output directroy"));
			//_optParser.setRequirementForNonOptionArgument();
			_optParser.setOption(new Option(OPT_AUTOSEARCH, "a", "auto", "auto search orf directories"));
			_optParser.setOption(new OptionWithArgument(OPT_BASEDIR, "b", "basedir", "DIR", "set input directory base"));
			if(args.length < 1)
				printUsage(1);

			_optParser.getContext(args);

			if(_optParser.isSet(OPT_HELP))
				printUsage(0);

			// setup
			if(_optParser.isSet(OPT_VERBOSE))
				_log = System.out;
			if(_optParser.isSet(OPT_SETTING_FILE))
			{
				String settingFile = _optParser.getValue(OPT_SETTING_FILE);
				_settings.loadSettings(settingFile);
			}
			if(_optParser.isSet(OPT_OUTPUTDIR))
				_settings.setProperty("OUTPUT_DIR", _optParser.getValue(OPT_OUTPUTDIR));
			if(_optParser.isSet(OPT_BASEDIR))
				_baseDir = _optParser.getValue(OPT_BASEDIR);

			if(_optParser.isSet(OPT_AUTOSEARCH))
			{
				// search orf directories
				File baseDir = new File(_baseDir);
				if(!baseDir.isDirectory())
					throw new SCMDException("base directory " + _baseDir + " doesn't exist");
				File[] file = baseDir.listFiles();
				for (int i = 0; i < file.length; i++)
				{
					if(!file[i].isDirectory())
						continue; // skip non directory files

					String orfName = file[i].getName();
					inputFileList.add(orfName);
				}
			}
			else
			{
				inputFileList = _optParser.getArgumentList();
				if(inputFileList.size() < 1)
					printUsage(1);
			}
		}
		catch (SCMDException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return inputFileList;
	}

	public void classify(LinkedList inputFileList)
	{
		try
		{
			final String SEP = File.separator;
			String outDirBase = _settings.getProperty("OUTPUT_DIR");
			outDirBase = outDirBase != null ? outDirBase + SEP : "";
			String outputDir[] = new String[3];
			final int TABLE_A = 0;
			final int TABLE_A1B = 1;
			final int TABLE_C = 2;
			TableSchema schema_A = new TableSchema("struct_A.txt");
			TableSchema schema_A1 = new TableSchema("struct_A1.txt");
			TableSchema schema_B = new TableSchema("struct_B.txt");
			TableSchema schema_C = new TableSchema("struct_C.txt");
			for (Iterator fi = inputFileList.iterator(); fi.hasNext();)
			{
				String inputDir = _baseDir + File.separator + (String) fi.next();
				String orf = (new File(inputDir)).getName();
				_log.println("input directory: " + inputDir);
				_log.println("orf: " + orf);

				TableMap tableMap = new TableMap();
				String tableFileName = ""; 
				try
				{
					// load tables (yor202w.xls, yor202w_conA_basic.xls, etc.)
					// and
					// add them to the tableMap
					for (int i = 0; i < TableTypeServer.getTypeMax(); i++)
					{
						TableElement telm = TableTypeServer.getTableElement(i);
						tableFileName = inputDir + SEP + orf + telm.getFileSuffix();
						CalMorphTable table = new CalMorphTable(tableFileName);
						tableMap.addTable(i, table);
					}
				}
				catch (SCMDException e)
				{
					// cannot read some table file correctly
					_log.println("directory " + inputDir + " is skipped because the file " + tableFileName + " doesn't exist");
					continue;
				}

				// create output directory
				for (int i = 0; i < outputDir.length; i++)
				{
					outputDir[i] = outDirBase + GROUP_NAME[i] + SEP + orf + "_" + GROUP_NAME[i];
					File dir = new File(outputDir[i]);
					if(!dir.exists())
						dir.mkdirs();
				}
				// open output files
				PrintWriter outFile[] = new PrintWriter[3];
				for (int i = 0; i < GROUP_FILE_SUFFIX.length; i++)
				{
					String outputFileName = outputDir[i] + SEP + orf + GROUP_FILE_SUFFIX[i];
					outFile[i] = new PrintWriter(new FileWriter(outputFileName));
					_log.println(" output: " + outputFileName);
				}
				// output labels
				schema_A.outputLabel(outFile[TABLE_A]);
				schema_A1.outputLabel(outFile[TABLE_A1B]);
				schema_C.outputLabel(outFile[TABLE_C]);
				// output contents
				CalMorphTable orfTable = tableMap.getTable(0);
				for (int row = 0; row < orfTable.getRowSize(); row++)
				{
					TableSchema schema;
					String nucleusGroup = orfTable.getCellData(row, "Dgroup");
					// group by Dgroup value
					if(nucleusGroup.equals("A"))
						outputRow(row, tableMap, schema_A, outFile[TABLE_A]);
					else if(nucleusGroup.equals("A1"))
						outputRow(row, tableMap, schema_A1, outFile[TABLE_A1B]);
					else if(nucleusGroup.equals("B"))
						outputRow(row, tableMap, schema_B, outFile[TABLE_A1B]);
					else if(nucleusGroup.equals("C"))
						outputRow(row, tableMap, schema_C, outFile[TABLE_C]);
					else
						continue; // unknown group (skip this cell data)
				}
				for (int i = 0; i < outFile.length; i++)
					outFile[i].close();
			}
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
		catch (SCMDException e)
		{
			System.err.println(e.getMessage());
		}
	}

	private void outputRow(int row, TableMap sourceTableMap, TableSchema schema, PrintWriter outputTable)
			throws SCMDException
	{
		int colMax = schema.numRule();
		for (int col = 0; col < colMax - 1; col++)
		{
			outputSingleAttribute(row, col, sourceTableMap, schema, outputTable);
			outputTable.print("\t");
		}
		outputSingleAttribute(row, colMax - 1, sourceTableMap, schema, outputTable);
		outputTable.println();
	}

	private void outputSingleAttribute(int row, int col, TableMap sourceTableMap, TableSchema schema,
			PrintWriter outputTable) throws SCMDException
	{
		AttributePosition attribPos = new AttributePosition(-1, "not defined");
		try
		{
			attribPos = schema.getAttributePosition(col);
			CalMorphTable sourceTable = sourceTableMap.getTable(attribPos.getTableType());
			outputTable.print(sourceTable.getCellData(row, attribPos.getAttributeName()));
		}
		catch (SCMDException e)
		{
			System.err.println("error occured while reading "
					+ TableTypeServer.getTableTypeName(attribPos.getTableType()));
			System.err.println("attrib=" + attribPos.getAttributeName() + " row=" + row + " col=" + col);
			throw e;
		}
	}

	void printUsage(int exitCode)
	{
		System.out.println("Usage: > java -jar NucleusStageClassifier.jar [options] orf_directory");
		System.out.println(_optParser.createHelpMessage());
		System.exit(exitCode);
	}
}

class ClassifierSetting extends Properties
{

	public ClassifierSetting()
	{
		super();
	}

	public void loadSettings(String settingFileName)
	{
		try
		{
			FileInputStream fin = new FileInputStream(settingFileName);
			this.load(fin);
			fin.close();
		}
		catch (IOException e)
		{
			System.err.println("error occured while loading " + settingFileName);
			System.err.println(e.getMessage());
		}
	}

}

class TableMap
{

	public TableMap()
	{}

	public void addTable(int tableType, CalMorphTable table)
	{
		_tableMap.put(new Integer(tableType), table);
	}

	public CalMorphTable getTable(int tableType) throws SCMDException
	{
		CalMorphTable table = (CalMorphTable) _tableMap.get(new Integer(tableType));
		if(table == null)
			throw new SCMDException("cannot find table corresponding to tableType = " + tableType);
		return table;
	}

	HashMap	_tableMap	= new HashMap();
}
//--------------------------------------
// $Log: NucleusStageClassifier.java,v $
// Revision 1.9  2004/07/20 07:51:00  sesejun
// SCMDServerから呼び出せるように、一部メソッドをpublicへ変更。
// AttributePositionを、TableSchemaから独立。
//
// Revision 1.8  2004/05/08 05:17:35  leo
// 出力先で作成するディレクトリを、
// {A,A1B,C}/ORFNAME_{A,A1B,C}/ORFNAME_{A,A1B,C}.xls
// の形式に変更しました。
//
// Revision 1.7  2004/05/07 05:50:46  leo
// baseDirのオプションの間違いを修正
//
// Revision 1.6  2004/05/07 05:47:45  leo
// 自動でORFのディレクトリをサーチできるようにしました
//
// Revision 1.5 2004/04/30 02:25:52 leo
// OptionParserに引数の有無をチェックできる機能を追加
// setRequirementForNonOptionArgument()
//
// Revision 1.4 2004/04/27 16:01:08 leo
// グループ毎のファイル名を、TableFileName classに抽出
//
// Revision 1.3 2004/04/27 09:02:03 leo
// *** empty log message ***
//
// Revision 1.2 2004/04/27 07:09:19 leo
// rename grouping.Table to grouping.CalMorphTable
//
// Revision 1.1 2004/04/23 02:24:25 leo
// move NucleusStageClassifier to lab.cb.scmd.autoanalysys.grouping
//
// Revision 1.3 2004/04/22 15:19:51 leo
// format src files
//
// Revision 1.2 2004/04/22 09:46:11 leo
// modify some comments
//
// Revision 1.1 2004/04/22 04:08:46 leo
// first ship for /home/lab.cb.scmd/CVS
//
// Revision 1.1 2004/04/22 02:53:31 leo
// first ship of SCMDProject
//
// Revision 1.4 2004/04/22 02:30:15 leo
// grouping complete
//
//--------------------------------------
