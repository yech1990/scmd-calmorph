//--------------------------------------
// SCMDProject
// 
// CellShapeStat.java 
// Since: 2004/08/10
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
import lab.cb.scmd.util.stat.EliminateOnePercentOfBothSidesStrategy;
import lab.cb.scmd.util.stat.Statistics;
import lab.cb.scmd.util.stat.StatisticsWithMissingValueSupport;
import lab.cb.scmd.util.table.*;
import lab.cb.scmd.util.time.StopWatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author leo
 */
public class CellShapeStat implements TableFileName {
    private OptionParser _optionParser = new OptionParser();

    private PrintStream _log = new NullPrintStream();

    private static final int OPT_HELP = 0;
    private static final int OPT_INPUTDIR = 1;
    private static final int OPT_OUTPUTDIR = 2;
    private static final int OPT_VERBOSE = 3;

    private File _inputDir = null;
    private File _outputDir = null;

    private Statistics _stat = new StatisticsWithMissingValueSupport(new String[]{".", "-1", "-1.0"},
            new EliminateOnePercentOfBothSidesStrategy());

    /**
     * C-num-no, D-num-C などのグループの組み合わせの値を求めるクラス
     *
     * @leo
     */
    private CellShapeStat() {
        super();
    }

    public static void main(String[] arg) {
        CellShapeStat instance = new CellShapeStat();
        instance.execute(arg);
    }

    private void setupOptions() throws SCMDException {
        _optionParser.setOption(new Option(OPT_HELP, "h", "help", "print help message"));
        _optionParser.setOption(new OptionWithArgument(OPT_INPUTDIR, "i", "input", "DIR",
                "specify input data directory"));
        _optionParser
                .setOption(new OptionWithArgument(OPT_OUTPUTDIR, "o", "output", "DIR", "specify output directory"));
        _optionParser.setOption(new Option(OPT_VERBOSE, "v", "verbose", "display verbose message"));
    }

    private void printHelpMessage() {
        System.out.println("[usage] CellShapeStat");
        System.out.println(_optionParser.createHelpMessage());
    }

    private String[] _groupParameter = new String[]{"Cgroup", "Dgroup", "Agroup"};          // T_ORF
    private String[] _prefix = new String[]{"C", "D", "A"};
    // テーブルに載っているパラメータ
    private String[][] _groupNamePattern = new String[][]{{"no", "small", "medium", "large"},
            {"A", "A1", "A1|B", "B", "C", "D", "E", "F"}, {"A", "B", "api", "iso", "E", "F"}};

    private String[] _shapeParameter = new String[]{"longAxis", "roundness", "budNeckPosition",
            "budGrowthDirection", "areaRatio"};
    private String[] _correspondingParameter = new String[]{"C103", "C115", "C105", "C106", "C118"};

    private String getParamName(int CDAgroup, int shapeType, int patternType) {
        return _prefix[CDAgroup] + "-" + _shapeParameter[shapeType] + "_" + _groupNamePattern[CDAgroup][patternType];
    }

    private String getNumParamName(int CDAgroup, int patternType) {
        return _prefix[CDAgroup] + "-num-" + _groupNamePattern[CDAgroup][patternType];
    }

    private void execute(String[] arg) {
        try {
            StopWatch timer = new StopWatch();

            setupOptions();
            _optionParser.getContext(arg);

            if (_optionParser.isSet(OPT_HELP)) {
                printHelpMessage();
                return;
            }

            if (_optionParser.isSet(OPT_VERBOSE)) {
                _log = System.out;
            }

            _inputDir = _optionParser.isSet(OPT_INPUTDIR) ? new File(_optionParser.getValue(OPT_INPUTDIR)) : new File(
                    "./");
            _outputDir = _optionParser.isSet(OPT_OUTPUTDIR) ? new File(_optionParser.getValue(OPT_OUTPUTDIR))
                    : new File("./");

            _log.println("input directory = " + _inputDir);
            _log.println("output directoyr = " + _outputDir);

            // load CalMorph tables

            // 最終的な結果を入れるテーブル の準備
            Vector<String> resultParamList = new Vector<>();
            for (int i = 0; i < _groupNamePattern.length; i++) {
                for (int j = 0; j < _groupNamePattern[i].length; j++) {
                    resultParamList.add(getNumParamName(i, j));
                }
            }

            for (int i = 0; i < _prefix.length; i++) {
                for (int j = 0; j < _shapeParameter.length; j++) {
                    for (int k = 0; k < _groupNamePattern[i].length; k++) {
                        String paramName = getParamName(i, j, k);
                        resultParamList.add(paramName);
                    }
                }
            }
            AppendableTable statTable = new AppendableTable("shape_stat", resultParamList, true);

            // for each orf
            File[] targetFile = _inputDir.listFiles();
            assert targetFile != null;
            for (File file : targetFile) {
                if (!file.isDirectory()) continue; // skip single files
                String orf = file.getName();
                File orfTableFile = new File(file, "/" + orf + TABLE_SUFFIX[T_ORF]);
                File cellParamTableFile = new File(file, "/" + orf + TABLE_SUFFIX[T_CONA_BIOLOGICAL]);

                _log.println("entering the directory: " + orf);
                if (!orfTableFile.exists()) {
                    _log.println("[skip] since " + orfTableFile + " doesn't exist");
                    continue;
                }
                if (!cellParamTableFile.exists()) {
                    _log.println("[skip] since " + cellParamTableFile + " doesn't exist");
                    continue;
                }

                BasicTable orfTable = new FlatTable(orfTableFile, false, true);
                BasicTable cellParamTable = new FlatTable(cellParamTableFile, false, true);

                HashMap<String, Number> result = new HashMap<>();

                // for each group
                for (int group = 0; group < _groupParameter.length; group++) {
                    String groupCol = _groupParameter[group];
                    // for each group pattern
                    for (int p = 0; p < _groupNamePattern[group].length; p++) {
                        Pattern pattern = Pattern.compile(_groupNamePattern[group][p]);
                        // group pattern に対応するcell idを集める
                        LinkedList<String> targetCellIDList = new LinkedList<>();
                        for (TableIterator ti = orfTable.getVerticalIterator(groupCol); ti.hasNext(); ) {
                            Cell cell = ti.nextCell();
                            Matcher matcher = pattern.matcher(cell.toString());
                            if (matcher.matches()) {
                                targetCellIDList.add(orfTable.getCell(ti.row(), "cell_id").toString());
                            }
                        }
                        //_log.println("group: " + _groupNamePattern[group][p]
                        // + "\t # of cells: "
                        //                                + targetCellIDList.size());
                        result.put(getNumParamName(group, p), targetCellIDList.size());

                        // longAxis, などのparameter をcell ID毎に集めたtableを作る
                        AppendableTable dataOfCurrentGroupPattern = new AppendableTable("table", _shapeParameter);
                        TableIterator ti = cellParamTable.getVerticalIterator("cell_id");
                        for (Object o : targetCellIDList) {
                            String cellID = (String) o;
                            // cellIDに対応する行をiteratorを見つける
                            while (ti.hasNext()) {
                                if (ti.nextCell().toString().equals(cellID)) {
                                    Vector<String> dataRow = new Vector<>();
                                    for (String value : _correspondingParameter) {
                                        Cell cell = cellParamTable.getCell(ti.row(), value);
                                        dataRow.add(cell.toString());
                                    }
                                    dataOfCurrentGroupPattern.append(dataRow);
                                }
                            }
                        }

                        for (int s = 0; s < _shapeParameter.length; s++) {
                            result.put(getParamName(group, s, p), _stat
                                    .calcAverage(dataOfCurrentGroupPattern.getVerticalIterator(_shapeParameter[s])));
                        }
                    }
                }
                Vector<String> resultRow = new Vector<>();
                resultRow.add(orf);
                for (Object o : resultParamList) {
                    String param = (String) o;
                    resultRow.add(result.get(param).toString());
                }
                statTable.append(resultRow);
            }

            if (statTable.getRowSize() >= 1) {
                File outputFile = new File(_outputDir, "/average_shape.xls");
                _log.println("output file = " + outputFile);
                PrintStream dataOut = new PrintStream(new FileOutputStream(outputFile));

                dataOut.print(statTable.getTableName());
                Vector<String> labels = statTable.getColLabelList();
                for (Object label : labels) {
                    dataOut.print("\t" + label);
                }
                dataOut.println();
                for (int row = 0; row < statTable.getRowSize(); row++) {
                    dataOut.print(statTable.getRowLabel(row));
                    //for(Iterator li=labels.iterator(); li.hasNext())
                    for (int col = 0; col < statTable.getColSize(); col++) {
                        dataOut.print("\t" + statTable.getCell(row, col).toString());
                    }
                    dataOut.println();
                }
                dataOut.close();
            }


            timer.showElapsedTime(_log);
            _log.println("done.");
        } catch (SCMDException e) {
            e.what();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

//--------------------------------------
// $Log: CellShapeStat.java,v $
// Revision 1.5  2004/08/14 11:09:54  leo
// Warningの整理、もう使わなくなったクラスにdeprecatedマークを入れました
//
// Revision 1.4  2004/08/11 06:45:32  leo
// バグを修正
//
// Revision 1.3  2004/08/11 05:36:47  leo
// ファイルが出力されないエラーを修正
//
// Revision 1.2 2004/08/10 11:46:35 leo
// 細胞数のデータも出力
//
// Revision 1.1 2004/08/10 10:44:02 leo
// 追加
//
//--------------------------------------
