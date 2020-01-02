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

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.cui.Option;
import lab.cb.scmd.util.cui.OptionParser;
import lab.cb.scmd.util.cui.OptionWithArgument;
import lab.cb.scmd.util.io.NullPrintStream;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Depending on the Dgroup status (A, A1, B, C) in orf.xls, the three groups A, {A1, B}, C
 * Class for sorting data output by CalMorph
 *
 * @author leo
 */
public class NucleusStageClassifier implements TableFileName {

    OptionParser _optParser = new OptionParser();
    ClassifierSetting _settings = new ClassifierSetting();
    PrintStream _log = new NullPrintStream();
    boolean _autoSeach = false;
    String _baseDir = ".";

    final int OPT_HELP = 0;
    final int OPT_VERBOSE = 1;
    final int OPT_SETTING_FILE = 2;
    final int OPT_OUTPUTDIR = 3;
    final int OPT_AUTOSEARCH = 4;
    final int OPT_BASEDIR = 5;

    // コマンドライン以外からの起動時に設定するオプション
    private boolean SET_RUN_ON_COMMANDLINE = true;
    private boolean SET_OPT_AUTOSEARCH = false;
    private boolean SET_OPT_BASEDIR = false;
    private boolean SET_OPT_VERBOSE = false;
    private boolean SET_OPT_OUTDIR = false;

    public void setNotRunOnCommandLine() {
        SET_RUN_ON_COMMANDLINE = false;
    }

    public void setAutoSearch() {
        SET_OPT_AUTOSEARCH = true;
    }

    public void setBaseDir(String dirname) {
        SET_OPT_BASEDIR = true;
        _baseDir = dirname;
    }

    public void setVerbose() {
        SET_OPT_VERBOSE = true;
    }

    public void setOutDir(String dirname) {
        SET_OPT_OUTDIR = true;
        _settings.setProperty("OUTPUT_DIR", dirname);
    }


    public NucleusStageClassifier() {
        TableTypeServer.Initialize();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // initialize
        NucleusStageClassifier classifier = new NucleusStageClassifier();
        LinkedList inputFileList = classifier.setupByArguments(args);
        classifier.classify(inputFileList);
    }

    /**
     * @param args コマンドライン引数
     * @return 入力ファイルのStringリスト
     */
    public LinkedList setupByArguments(String[] args) {
        LinkedList inputFileList = new LinkedList();
        try {
            _optParser.setOption(new Option(OPT_HELP, "h", "help", "print help message"));
            _optParser.setOption(new Option(OPT_VERBOSE, "v", "verbose", "print some verbose messages"));
            _optParser
                    .setOption(new OptionWithArgument(OPT_SETTING_FILE, "s", "", "SETTINGFILE", "read a setting file"));
            _optParser
                    .setOption(new OptionWithArgument(OPT_OUTPUTDIR, "o", "outdir", "DIR", "specify output directroy"));
            //_optParser.setRequirementForNonOptionArgument();
            _optParser.setOption(new Option(OPT_AUTOSEARCH, "a", "auto", "auto search orf directories"));
            _optParser.setOption(new OptionWithArgument(OPT_BASEDIR, "b", "basedir", "DIR", "set input directory base"));
            if (args.length < 1 && SET_RUN_ON_COMMANDLINE)
                printUsage(1);

            _optParser.getContext(args);

            if (_optParser.isSet(OPT_HELP))
                printUsage(0);

            // setup
            if (_optParser.isSet(OPT_VERBOSE) || SET_OPT_VERBOSE)
                _log = System.out;
            if (_optParser.isSet(OPT_SETTING_FILE)) {
                String settingFile = _optParser.getValue(OPT_SETTING_FILE);
                _settings.loadSettings(settingFile);
            }
            if (_optParser.isSet(OPT_OUTPUTDIR))
                _settings.setProperty("OUTPUT_DIR", _optParser.getValue(OPT_OUTPUTDIR));
            if (_optParser.isSet(OPT_BASEDIR))
                _baseDir = _optParser.getValue(OPT_BASEDIR);

            if (_optParser.isSet(OPT_AUTOSEARCH) || SET_OPT_AUTOSEARCH) {
                // search orf directories
                File baseDir = new File(_baseDir);
                if (!baseDir.isDirectory())
                    throw new SCMDException("base directory " + _baseDir + " doesn't exist");
                File[] file = baseDir.listFiles();
                assert file != null;
                for (File value : file) {
                    if (!value.isDirectory())
                        continue; // skip non directory files
                    if (value.getName().charAt(0) == '.') {
                        _log.println("directory " + value.getName() + " skipped because of invalid directory name");
                        continue;
                    }
                    String orfName = value.getName();
                    inputFileList.add(orfName);
                }
            } else {
                inputFileList = _optParser.getArgumentList();
                if (inputFileList.size() < 1)
                    printUsage(1);
            }
        } catch (SCMDException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return inputFileList;
    }

    public void classify(LinkedList inputFileList) {
        try {
            final String SEP = File.separator;
            String outDirBase = _settings.getProperty("OUTPUT_DIR");
            outDirBase = outDirBase != null ? outDirBase + SEP : "";
            String[] outputDir = new String[3];
            final int TABLE_A = 0;
            final int TABLE_A1B = 1;
            final int TABLE_C = 2;
//			TableSchema schema_A = new TableSchema("struct_A.txt");
//			TableSchema schema_A1 = new TableSchema("struct_A1.txt");
//			TableSchema schema_B = new TableSchema("struct_B.txt");
//			TableSchema schema_C = new TableSchema("struct_C.txt");
            TableSchema schema_A = new TableSchema(structA, false);
            TableSchema schema_A1 = new TableSchema(structA1, false);
            TableSchema schema_B = new TableSchema(structB, false);
            TableSchema schema_C = new TableSchema(structC, false);
            for (Object o : inputFileList) {
                String inputDir = _baseDir + File.separator + o;
                String orf = (new File(inputDir)).getName();
                _log.println("input directory: " + inputDir);
                _log.println("orf: " + orf);

                TableMap tableMap = new TableMap();
                String tableFileName = "";
                try {
                    // load tables (yor202w.xls, yor202w_conA_basic.xls, etc.)
                    // and
                    // add them to the tableMap
                    for (int i = 0; i < TableTypeServer.getTypeMax(); i++) {
                        TableElement telm = TableTypeServer.getTableElement(i);
                        tableFileName = inputDir + SEP + orf + telm.getFileSuffix();
                        CalMorphTable table = new CalMorphTable(tableFileName);
                        tableMap.addTable(i, table);
                    }
                } catch (SCMDException e) {
                    // cannot read some table file correctly
                    _log.println("directory " + inputDir + " is skipped because the file " + tableFileName + " doesn't exist");
                    continue;
                }

                // create output directory
                for (int i = 0; i < outputDir.length; i++) {
                    outputDir[i] = outDirBase + GROUP_NAME[i] + SEP + orf + "_" + GROUP_NAME[i];
                    File dir = new File(outputDir[i]);
                    if (!dir.exists())
                        dir.mkdirs();
                }
                // open output files
                PrintWriter[] outFile = new PrintWriter[3];
                for (int i = 0; i < GROUP_FILE_SUFFIX.length; i++) {
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
                for (int row = 0; row < orfTable.getRowSize(); row++) {
                    TableSchema schema;
                    String nucleusGroup = orfTable.getCellData(row, "Dgroup");
                    // group by Dgroup value
                    switch (nucleusGroup) {
                        case "A":
                            outputRow(row, tableMap, schema_A, outFile[TABLE_A]);
                            break;
                        case "A1":
                            outputRow(row, tableMap, schema_A1, outFile[TABLE_A1B]);
                            break;
                        case "B":
                            outputRow(row, tableMap, schema_B, outFile[TABLE_A1B]);
                            break;
                        case "C":
                            outputRow(row, tableMap, schema_C, outFile[TABLE_C]);
                            break;
                        default:
                            // unknown group (skip this cell data)
                    }
                }
                for (PrintWriter printWriter : outFile) printWriter.close();
            }
        } catch (IOException | SCMDException e) {
            System.err.println(e.getMessage());
        }
    }

    private void outputRow(int row, TableMap sourceTableMap, TableSchema schema, PrintWriter outputTable)
            throws SCMDException {
        int colMax = schema.numRule();
        for (int col = 0; col < colMax - 1; col++) {
            outputSingleAttribute(row, col, sourceTableMap, schema, outputTable);
            outputTable.print("\t");
        }
        outputSingleAttribute(row, colMax - 1, sourceTableMap, schema, outputTable);
        outputTable.println();
    }

    private void outputSingleAttribute(int row, int col, TableMap sourceTableMap, TableSchema schema,
                                       PrintWriter outputTable) throws SCMDException {
        AttributePosition attribPos = new AttributePosition(-1, "not defined");
        try {
            attribPos = schema.getAttributePosition(col);
            CalMorphTable sourceTable = sourceTableMap.getTable(attribPos.getTableType());
            outputTable.print(sourceTable.getCellData(row, attribPos.getAttributeName()));
        } catch (SCMDException e) {
            System.err.println("error occured while reading "
                    + TableTypeServer.getTableTypeName(attribPos.getTableType()));
            System.err.println("attrib=" + attribPos.getAttributeName() + " row=" + row + " col=" + col);
            throw e;
        }
    }

    void printUsage(int exitCode) {
        System.out.println("Usage: > java -jar NucleusStageClassifier.jar [options] orf_directory");
        System.out.println(_optParser.createHelpMessage());
        System.exit(exitCode);
    }

    private String structA =
            "# schema of	table A	\n" +
                    "# (attribute name, original	table, original attributename)\n" +
                    "image_number	T_ORF	image_number\n" +
                    "cell_id	T_ORF	cell_id\n" +
                    "Agroup	T_ORF	Agroup\n" +
                    "C11-1_A	T_CONA_BASIC	C11-1\n" +
                    "C12-1_A	T_CONA_BASIC	C12-1\n" +
                    "C13_A	T_CONA_BASIC	C13\n" +
                    "C103_A	T_CONA_BIOLOGICAL	C103\n" +
                    "C104_A	T_CONA_BIOLOGICAL	C104\n" +
                    "C115_A	T_CONA_BIOLOGICAL	C115\n" +
                    "C126_A	T_CONA_BIOLOGICAL	C126\n" +
                    "C127_A	T_CONA_BIOLOGICAL	C127\n" +
                    "A7-1_A	T_ACTIN_BASIC	A7-1\n" +
                    "A8-1_A	T_ACTIN_BASIC	A8-1\n" +
                    "A101_A	T_ACTIN_BIOLOGICAL	A101\n" +
                    "A120_A	T_ACTIN_BIOLOGICAL	A120\n" +
                    "A121_A	T_ACTIN_BIOLOGICAL	A121\n" +
                    "A122_A	T_ACTIN_BIOLOGICAL	A122\n" +
                    "A123_A	T_ACTIN_BIOLOGICAL	A123\n" +
                    "D14-1_A	T_DAPI_BASIC	D14-1\n" +
                    "D15-1_A	T_DAPI_BASIC	D15-1\n" +
                    "D16-1_A	T_DAPI_BASIC	D16-1\n" +
                    "D17-1_A	T_DAPI_BASIC	D17-1\n" +
                    "D102_A	T_DAPI_BIOLOGICAL	D102\n" +
                    "D105_A	T_DAPI_BIOLOGICAL	D105\n" +
                    "D117_A	T_DAPI_BIOLOGICAL	D117\n" +
                    "D127_A	T_DAPI_BIOLOGICAL	D127\n" +
                    "D135_A	T_DAPI_BIOLOGICAL	D135\n" +
                    "D147_A	T_DAPI_BIOLOGICAL	D147\n" +
                    "D148_A	T_DAPI_BIOLOGICAL	D148\n" +
                    "D154_A	T_DAPI_BIOLOGICAL	D154\n" +
                    "D155_A	T_DAPI_BIOLOGICAL	D155\n" +
                    "D173_A	T_DAPI_BIOLOGICAL	D173\n" +
                    "D176_A	T_DAPI_BIOLOGICAL	D176\n" +
                    "D179_A	T_DAPI_BIOLOGICAL	D179\n" +
                    "D182_A	T_DAPI_BIOLOGICAL	D182\n" +
                    "D188_A	T_DAPI_BIOLOGICAL	D188\n" +
                    "D191_A	T_DAPI_BIOLOGICAL	D191\n" +
                    "D194_A	T_DAPI_BIOLOGICAL	D194\n";

    private String structA1 =
            "image_number	T_ORF image_number\n" +
                    "cell_id	T_ORF	cell_id\n" +
                    "Cgroup	T_ORF	Cgroup\n" +
                    "Agroup	T_ORF	Agroup\n" +
                    "Dgroup	T_ORF	Dgroup\n" +
                    "C11-1_A1B	T_CONA_BASIC	C11-1\n" +
                    "C11-2_A1B	T_CONA_BASIC	C11-2\n" +
                    "C12-1_A1B	T_CONA_BASIC	C12-1\n" +
                    "C12-2_A1B	T_CONA_BASIC	C12-2\n" +
                    "C13_A1B	T_CONA_BASIC	C13\n" +
                    "C101_A1B	T_CONA_BIOLOGICAL	C101\n" +
                    "C102_A1B	T_CONA_BIOLOGICAL	C102\n" +
                    "C103_A1B	T_CONA_BIOLOGICAL	C103\n" +
                    "C104_A1B	T_CONA_BIOLOGICAL	C104\n" +
                    "C105_A1B	T_CONA_BIOLOGICAL	C105\n" +
                    "C106_A1B	T_CONA_BIOLOGICAL	C106\n" +
                    "C107_A1B	T_CONA_BIOLOGICAL	C107\n" +
                    "C108_A1B	T_CONA_BIOLOGICAL	C108\n" +
                    "C109_A1B	T_CONA_BIOLOGICAL	C109\n" +
                    "C110_A1B	T_CONA_BIOLOGICAL	C110\n" +
                    "C111_A1B	T_CONA_BIOLOGICAL	C111\n" +
                    "C112_A1B	T_CONA_BIOLOGICAL	C112\n" +
                    "C113_A1B	T_CONA_BIOLOGICAL	C113\n" +
                    "C114_A1B	T_CONA_BIOLOGICAL	C114\n" +
                    "C115_A1B	T_CONA_BIOLOGICAL	C115\n" +
                    "C116_A1B	T_CONA_BIOLOGICAL	C116\n" +
                    "C117_A1B	T_CONA_BIOLOGICAL	C117\n" +
                    "C118_A1B	T_CONA_BIOLOGICAL	C118\n" +
                    "C126_A1B	T_CONA_BIOLOGICAL	C126\n" +
                    "C127_A1B	T_CONA_BIOLOGICAL	C127\n" +
                    "C128_A1B	T_CONA_BIOLOGICAL	C128\n" +
                    "A7-1_A1B	T_ACTIN_BASIC	A7-1\n" +
                    "A7-2_A1B	T_ACTIN_BASIC	A7-2\n" +
                    "A8-1_A1B	T_ACTIN_BASIC	A8-1\n" +
                    "A8-2_A1B	T_ACTIN_BASIC	A8-2\n" +
                    "A9_A1B	T_ACTIN_BASIC	A9\n" +
                    "A101_A1B	T_ACTIN_BIOLOGICAL	A101\n" +
                    "A102_A1B	T_ACTIN_BIOLOGICAL	A102\n" +
                    "A103_A1B	T_ACTIN_BIOLOGICAL	A103\n" +
                    "A104_A1B	T_ACTIN_BIOLOGICAL	A104\n" +
                    "A120_A1B	T_ACTIN_BIOLOGICAL	A120\n" +
                    "A121_A1B	T_ACTIN_BIOLOGICAL	A121\n" +
                    "A122_A1B	T_ACTIN_BIOLOGICAL	A122\n" +
                    "A123_A1B	T_ACTIN_BIOLOGICAL	A123\n" +
                    "D14-3_A1B	T_DAPI_BASIC	D14-3\n" +
                    "D15-3_A1B	T_DAPI_BASIC	D15-3\n" +
                    "D16-3_A1B	T_DAPI_BASIC	D16-3\n" +
                    "D17-3_A1B	T_DAPI_BASIC	D17-1\n" +
                    "D104_A1B	T_DAPI_BIOLOGICAL	D104\n" +
                    "D107_A1B	T_DAPI_BIOLOGICAL	D107\n" +
                    "D110_A1B	T_DAPI_BIOLOGICAL	D110\n" +
                    "D114_A1B	T_DAPI_BIOLOGICAL	D114\n" +
                    "D118_A1B	T_DAPI_BIOLOGICAL	D118\n" +
                    "D126_A1B	T_DAPI_BIOLOGICAL	D126\n" +
                    "D129_A1B	T_DAPI_BIOLOGICAL	D129\n" +
                    "D132_A1B	T_DAPI_BIOLOGICAL	D132\n" +
                    "D136_A1B	T_DAPI_BIOLOGICAL	D136\n" +
                    "D142_A1B	T_DAPI_BIOLOGICAL	D142\n" +
                    "D143_A1B	T_DAPI_BIOLOGICAL	D143\n" +
                    "D145_A1B	T_DAPI_BIOLOGICAL	D145\n" +
                    "D147_A1B	T_DAPI_BIOLOGICAL	D147\n" +
                    "D148_A1B	T_DAPI_BIOLOGICAL	D148\n" +
                    "D152_A1B	T_DAPI_BIOLOGICAL	D152\n" +
                    "D154_A1B	T_DAPI_BIOLOGICAL	D154\n" +
                    "D155_A1B	T_DAPI_BIOLOGICAL	D155\n" +
                    "D161_A1B	T_DAPI_BIOLOGICAL	D160\n" +
                    "D165_A1B	T_DAPI_BIOLOGICAL	D164\n" +
                    "D169_A1B	T_DAPI_BIOLOGICAL	D169\n" +
                    "D170_A1B	T_DAPI_BIOLOGICAL	D170\n" +
                    "D172_A1B	T_DAPI_BIOLOGICAL	D171\n" +
                    "D175_A1B	T_DAPI_BIOLOGICAL	D175\n" +
                    "D178_A1B	T_DAPI_BIOLOGICAL	D178\n" +
                    "D181_A1B	T_DAPI_BIOLOGICAL	D181\n" +
                    "D184_A1B	T_DAPI_BIOLOGICAL	D184\n" +
                    "D190_A1B	T_DAPI_BIOLOGICAL	D188\n" +
                    "D193_A1B	T_DAPI_BIOLOGICAL	D193\n" +
                    "D196_A1B	T_DAPI_BIOLOGICAL	D196\n";

    private String structB =
            "image_number	T_ORF image_number\n" +
                    "cell_id	T_ORF	cell_id\n" +
                    "Cgroup	T_ORF	Cgroup\n" +
                    "Agroup	T_ORF	Agroup\n" +
                    "Dgroup	T_ORF	Dgroup\n" +
                    "C11-1_A1B	T_CONA_BASIC	C11-1\n" +
                    "C11-2_A1B	T_CONA_BASIC	C11-2\n" +
                    "C12-1_A1B	T_CONA_BASIC	C12-1\n" +
                    "C12-2_A1B	T_CONA_BASIC	C12-2\n" +
                    "C13_A1B	T_CONA_BASIC	C13\n" +
                    "C101_A1B	T_CONA_BIOLOGICAL	C101\n" +
                    "C102_A1B	T_CONA_BIOLOGICAL	C102\n" +
                    "C103_A1B	T_CONA_BIOLOGICAL	C103\n" +
                    "C104_A1B	T_CONA_BIOLOGICAL	C104\n" +
                    "C105_A1B	T_CONA_BIOLOGICAL	C105\n" +
                    "C106_A1B	T_CONA_BIOLOGICAL	C106\n" +
                    "C107_A1B	T_CONA_BIOLOGICAL	C107\n" +
                    "C108_A1B	T_CONA_BIOLOGICAL	C108\n" +
                    "C109_A1B	T_CONA_BIOLOGICAL	C109\n" +
                    "C110_A1B	T_CONA_BIOLOGICAL	C110\n" +
                    "C111_A1B	T_CONA_BIOLOGICAL	C111\n" +
                    "C112_A1B	T_CONA_BIOLOGICAL	C112\n" +
                    "C113_A1B	T_CONA_BIOLOGICAL	C113\n" +
                    "C114_A1B	T_CONA_BIOLOGICAL	C114\n" +
                    "C115_A1B	T_CONA_BIOLOGICAL	C115\n" +
                    "C116_A1B	T_CONA_BIOLOGICAL	C116\n" +
                    "C117_A1B	T_CONA_BIOLOGICAL	C117\n" +
                    "C118_A1B	T_CONA_BIOLOGICAL	C118\n" +
                    "C126_A1B	T_CONA_BIOLOGICAL	C126\n" +
                    "C127_A1B	T_CONA_BIOLOGICAL	C127\n" +
                    "C128_A1B	T_CONA_BIOLOGICAL	C128\n" +
                    "A7-1_A1B	T_ACTIN_BASIC	A7-1\n" +
                    "A7-2_A1B	T_ACTIN_BASIC	A7-2\n" +
                    "A8-1_A1B	T_ACTIN_BASIC	A8-1\n" +
                    "A8-2_A1B	T_ACTIN_BASIC	A8-2\n" +
                    "A9_A1B	T_ACTIN_BASIC	A9\n" +
                    "A101_A1B	T_ACTIN_BIOLOGICAL	A101\n" +
                    "A102_A1B	T_ACTIN_BIOLOGICAL	A102\n" +
                    "A103_A1B	T_ACTIN_BIOLOGICAL	A103\n" +
                    "A104_A1B	T_ACTIN_BIOLOGICAL	A104\n" +
                    "A120_A1B	T_ACTIN_BIOLOGICAL	A120\n" +
                    "A121_A1B	T_ACTIN_BIOLOGICAL	A121\n" +
                    "A122_A1B	T_ACTIN_BIOLOGICAL	A122\n" +
                    "A123_A1B	T_ACTIN_BIOLOGICAL	A123\n" +
                    "D14-3_A1B	T_DAPI_BASIC	D14-3\n" +
                    "D15-3_A1B	T_DAPI_BASIC	D15-3\n" +
                    "D16-3_A1B	T_DAPI_BASIC	D16-3\n" +
                    "D17-3_A1B	T_DAPI_BASIC	D17-3\n" +
                    "D104_A1B	T_DAPI_BIOLOGICAL	D104\n" +
                    "D107_A1B	T_DAPI_BIOLOGICAL	D107\n" +
                    "D110_A1B	T_DAPI_BIOLOGICAL	D110\n" +
                    "D114_A1B	T_DAPI_BIOLOGICAL	D114\n" +
                    "D118_A1B	T_DAPI_BIOLOGICAL	D118\n" +
                    "D126_A1B	T_DAPI_BIOLOGICAL	D126\n" +
                    "D129_A1B	T_DAPI_BIOLOGICAL	D129\n" +
                    "D132_A1B	T_DAPI_BIOLOGICAL	D132\n" +
                    "D136_A1B	T_DAPI_BIOLOGICAL	D136\n" +
                    "D142_A1B	T_DAPI_BIOLOGICAL	D142\n" +
                    "D143_A1B	T_DAPI_BIOLOGICAL	D143\n" +
                    "D145_A1B	T_DAPI_BIOLOGICAL	D145\n" +
                    "D147_A1B	T_DAPI_BIOLOGICAL	D147\n" +
                    "D148_A1B	T_DAPI_BIOLOGICAL	D148\n" +
                    "D152_A1B	T_DAPI_BIOLOGICAL	D152\n" +
                    "D154_A1B	T_DAPI_BIOLOGICAL	D154\n" +
                    "D155_A1B	T_DAPI_BIOLOGICAL	D155\n" +
                    "D161_A1B	T_DAPI_BIOLOGICAL	D161\n" +
                    "D165_A1B	T_DAPI_BIOLOGICAL	D165\n" +
                    "D169_A1B	T_DAPI_BIOLOGICAL	D169\n" +
                    "D170_A1B	T_DAPI_BIOLOGICAL	D170\n" +
                    "D172_A1B	T_DAPI_BIOLOGICAL	D172\n" +
                    "D175_A1B	T_DAPI_BIOLOGICAL	D175\n" +
                    "D178_A1B	T_DAPI_BIOLOGICAL	D178\n" +
                    "D181_A1B	T_DAPI_BIOLOGICAL	D181\n" +
                    "D184_A1B	T_DAPI_BIOLOGICAL	D184\n" +
                    "D190_A1B	T_DAPI_BIOLOGICAL	D190\n" +
                    "D193_A1B	T_DAPI_BIOLOGICAL	D193\n" +
                    "D196_A1B	T_DAPI_BIOLOGICAL	D196\n";

    private String structC =
            "image_number	 T_ORF	 image_number	\n" +
                    "cell_id	 T_ORF	 cell_id	\n" +
                    "Cgroup	 T_ORF	 Cgroup	\n" +
                    "Agroup	 T_ORF	 Agroup	\n" +
                    "C11-1_C	 T_CONA_BASIC	 C11-1\n" +
                    "C11-2_C	 T_CONA_BASIC	 C11-2\n" +
                    "C12-1_C	 T_CONA_BASIC	 C12-1\n" +
                    "C12-2_C	 T_CONA_BASIC	 C12-2\n" +
                    "C13_C	 T_CONA_BASIC	 C13\n" +
                    "C101_C	 T_CONA_BIOLOGICAL	 C101\n" +
                    "C102_C	 T_CONA_BIOLOGICAL	 C102\n" +
                    "C103_C	 T_CONA_BIOLOGICAL	 C103\n" +
                    "C104_C	 T_CONA_BIOLOGICAL	 C104\n" +
                    "C105_C	 T_CONA_BIOLOGICAL	 C105\n" +
                    "C106_C	 T_CONA_BIOLOGICAL	 C106\n" +
                    "C107_C	 T_CONA_BIOLOGICAL	 C107\n" +
                    "C108_C	 T_CONA_BIOLOGICAL	 C108\n" +
                    "C109_C	 T_CONA_BIOLOGICAL	 C109\n" +
                    "C110_C	 T_CONA_BIOLOGICAL	 C110\n" +
                    "C111_C	 T_CONA_BIOLOGICAL	 C111\n" +
                    "C112_C	 T_CONA_BIOLOGICAL	 C112\n" +
                    "C113_C	 T_CONA_BIOLOGICAL	 C113\n" +
                    "C114_C	 T_CONA_BIOLOGICAL	 C114\n" +
                    "C115_C	 T_CONA_BIOLOGICAL	 C115\n" +
                    "C116_C	 T_CONA_BIOLOGICAL	 C116\n" +
                    "C117_C	 T_CONA_BIOLOGICAL	 C117\n" +
                    "C118_C	 T_CONA_BIOLOGICAL	 C118\n" +
                    "C126_C	 T_CONA_BIOLOGICAL	 C126\n" +
                    "C127_C	 T_CONA_BIOLOGICAL	 C127\n" +
                    "C128_C	 T_CONA_BIOLOGICAL	 C128\n" +
                    "A7-1_C	 T_ACTIN_BASIC	 A7-1\n" +
                    "A7-2_C	 T_ACTIN_BASIC	 A7-2\n" +
                    "A8-1_C	 T_ACTIN_BASIC	 A8-1\n" +
                    "A8-2_C	 T_ACTIN_BASIC	 A8-2\n" +
                    "A9_C	 T_ACTIN_BASIC	 A9\n" +
                    "A101_C	 T_ACTIN_BIOLOGICAL	 A101\n" +
                    "A102_C	 T_ACTIN_BIOLOGICAL	 A102\n" +
                    "A103_C	 T_ACTIN_BIOLOGICAL	 A103\n" +
                    "A104_C	 T_ACTIN_BIOLOGICAL	 A104\n" +
                    "A120_C	 T_ACTIN_BIOLOGICAL	 A120\n" +
                    "A121_C	 T_ACTIN_BIOLOGICAL	 A121\n" +
                    "A122_C	 T_ACTIN_BIOLOGICAL	 A122\n" +
                    "A123_C	 T_ACTIN_BIOLOGICAL	 A123\n" +
                    "D14-1_C	 T_DAPI_BASIC	 D14-1\n" +
                    "D14-2_C	 T_DAPI_BASIC	 D14-2\n" +
                    "D14-3_C	 T_DAPI_BASIC	 D14-3\n" +
                    "D15-1_C	 T_DAPI_BASIC	 D15-1\n" +
                    "D15-2_C	 T_DAPI_BASIC	 D15-2\n" +
                    "D15-3_C	 T_DAPI_BASIC	 D15-3\n" +
                    "D16-1_C	 T_DAPI_BASIC	 D16-1\n" +
                    "D16-2_C	 T_DAPI_BASIC	 D16-2\n" +
                    "D16-3_C	 T_DAPI_BASIC	 D16-3\n" +
                    "D17-1_C	 T_DAPI_BASIC	 D17-1\n" +
                    "D17-2_C	 T_DAPI_BASIC	 D17-2\n" +
                    "D103_C	 T_DAPI_BIOLOGICAL	 D103\n" +
                    "D106_C	 T_DAPI_BIOLOGICAL	 D106\n" +
                    "D108_C	 T_DAPI_BIOLOGICAL	 D108\n" +
                    "D109_C	 T_DAPI_BIOLOGICAL	 D109\n" +
                    "D112_C	 T_DAPI_BIOLOGICAL	 D112\n" +
                    "D113_C	 T_DAPI_BIOLOGICAL	 D113\n" +
                    "D116_C	 T_DAPI_BIOLOGICAL	 D116\n" +
                    "D117_C	 T_DAPI_BIOLOGICAL	 D117\n" +
                    "D119_C	 T_DAPI_BIOLOGICAL	 D119\n" +
                    "D121_C	 T_DAPI_BIOLOGICAL	 D121\n" +
                    "D123_C	 T_DAPI_BIOLOGICAL	 D123\n" +
                    "D125_C	 T_DAPI_BIOLOGICAL	 D125\n" +
                    "D128_C	 T_DAPI_BIOLOGICAL	 D128\n" +
                    "D130_C	 T_DAPI_BIOLOGICAL	 D130\n" +
                    "D131_C	 T_DAPI_BIOLOGICAL	 D131\n" +
                    "D134_C	 T_DAPI_BIOLOGICAL	 D134\n" +
                    "D135_C	 T_DAPI_BIOLOGICAL	 D135\n" +
                    "D137_C	 T_DAPI_BIOLOGICAL	 D137\n" +
                    "D139_C	 T_DAPI_BIOLOGICAL	 D139\n" +
                    "D141_C	 T_DAPI_BIOLOGICAL	 D141\n" +
                    "D143_C	 T_DAPI_BIOLOGICAL	 D143\n" +
                    "D144_C	 T_DAPI_BIOLOGICAL	 D144\n" +
                    "D145_C	 T_DAPI_BIOLOGICAL	 D145\n" +
                    "D146_C	 T_DAPI_BIOLOGICAL	 D146\n" +
                    "D147_C	 T_DAPI_BIOLOGICAL	 D147\n" +
                    "D148_C	 T_DAPI_BIOLOGICAL	 D148\n" +
                    "D149_C	 T_DAPI_BIOLOGICAL	 D149\n" +
                    "D150_C	 T_DAPI_BIOLOGICAL	 D150\n" +
                    "D151_C	 T_DAPI_BIOLOGICAL	 D151\n" +
                    "D152_C	 T_DAPI_BIOLOGICAL	 D152\n" +
                    "D153_C	 T_DAPI_BIOLOGICAL	 D153\n" +
                    "D154_C	 T_DAPI_BIOLOGICAL	 D154\n" +
                    "D155_C	 T_DAPI_BIOLOGICAL	 D155\n" +
                    "D156_C	 T_DAPI_BIOLOGICAL	 D156\n" +
                    "D157_C	 T_DAPI_BIOLOGICAL	 D157\n" +
                    "D158_C	 T_DAPI_BIOLOGICAL	 D158\n" +
                    "D159_C	 T_DAPI_BIOLOGICAL	 D159\n" +
                    "D162_C	 T_DAPI_BIOLOGICAL	 D162\n" +
                    "D163_C	 T_DAPI_BIOLOGICAL	 D163\n" +
                    "D166_C	 T_DAPI_BIOLOGICAL	 D166\n" +
                    "D167_C	 T_DAPI_BIOLOGICAL	 D167\n" +
                    "D169_C	 T_DAPI_BIOLOGICAL	 D169\n" +
                    "D170_C	 T_DAPI_BIOLOGICAL	 D170\n" +
                    "D173_C	 T_DAPI_BIOLOGICAL	 D173\n" +
                    "D174_C	 T_DAPI_BIOLOGICAL	 D174\n" +
                    "D176_C	 T_DAPI_BIOLOGICAL	 D176\n" +
                    "D177_C	 T_DAPI_BIOLOGICAL	 D177\n" +
                    "D179_C	 T_DAPI_BIOLOGICAL	 D179\n" +
                    "D180_C	 T_DAPI_BIOLOGICAL	 D180\n" +
                    "D182_C	 T_DAPI_BIOLOGICAL	 D182\n" +
                    "D183_C	 T_DAPI_BIOLOGICAL	 D183\n" +
                    "D185_C	 T_DAPI_BIOLOGICAL	 D185\n" +
                    "D186_C	 T_DAPI_BIOLOGICAL	 D186\n" +
                    "D188_C	 T_DAPI_BIOLOGICAL	 D188\n" +
                    "D189_C	 T_DAPI_BIOLOGICAL	 D189\n" +
                    "D191_C	 T_DAPI_BIOLOGICAL	 D191\n" +
                    "D192_C	 T_DAPI_BIOLOGICAL	 D192\n" +
                    "D193_C	 T_DAPI_BIOLOGICAL	 D193\n" +
                    "D194_C	 T_DAPI_BIOLOGICAL	 D194\n" +
                    "D195_C	 T_DAPI_BIOLOGICAL	 D195\n" +
                    "D196_C	 T_DAPI_BIOLOGICAL	 D196\n" +
                    "D197_C	 T_DAPI_BIOLOGICAL	 D197\n" +
                    "D198_C	 T_DAPI_BIOLOGICAL	 D198\n";
}

class ClassifierSetting extends Properties {

    public ClassifierSetting() {
        super();
    }

    public void loadSettings(String settingFileName) {
        try {
            FileInputStream fin = new FileInputStream(settingFileName);
            this.load(fin);
            fin.close();
        } catch (IOException e) {
            System.err.println("error occured while loading " + settingFileName);
            System.err.println(e.getMessage());
        }
    }

}

class TableMap {

    public TableMap() {
    }

    public void addTable(int tableType, CalMorphTable table) {
        _tableMap.put(tableType, table);
    }

    public CalMorphTable getTable(int tableType) throws SCMDException {
        CalMorphTable table = _tableMap.get(tableType);
        if (table == null)
            throw new SCMDException("cannot find table corresponding to tableType = " + tableType);
        return table;
    }

    HashMap<Integer, CalMorphTable> _tableMap = new HashMap<>();
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
