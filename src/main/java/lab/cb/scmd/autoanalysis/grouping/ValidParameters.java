//--------------------------------------
// SCMD Project
// 
// ValidParameters.java 
// Since: 2004/05/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------
package lab.cb.scmd.autoanalysis.grouping;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.cui.Option;
import lab.cb.scmd.util.cui.OptionParser;
import lab.cb.scmd.util.cui.OptionWithArgument;
import lab.cb.scmd.util.table.Cell;
import lab.cb.scmd.util.table.TableIterator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author nakatani
 */


public class ValidParameters implements DataFileName {
    // option IDs
    private final static int OPT_HELP = 0;
    private final static int OPT_WILDDIR = 1;
    private final static int OPT_MUTANTDIR = 2;
    private OptionParser _parser = new OptionParser();
    private String wildtype_dir = ".";
    private String mutant_dir = ".";
    private String _dir_name;
    private HashMap<String, Vector<Cell>> _parameterToDataTable;
    private Vector<String> _yukoParameterList;
    private Vector<Vector<Cell>> _yukoDataTable;
    private String[] missingValueList = {"NaN", "Infinity", "-1", "-1.0"};
    private int number_of_partitions = 20;
    private ValidParameters() {
    }

    public static void main(String[] args) {
        try {
            ValidParameters c = new ValidParameters();
            c.setupByArguments(args);
            c.MakeVPListAll();
            c.MakeValidParamTable();
        } catch (SCMDException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * @param args
     * @return 解析するORFのディレクトリ
     * @throws SCMDException
     */
    private void setupByArguments(String[] args) throws SCMDException {
        setupOptionParser();

        _parser.getContext(args);

        if (_parser.isSet(OPT_HELP))
            printUsage();
        if (_parser.isSet(OPT_MUTANTDIR))
            mutant_dir = _parser.getValue(OPT_MUTANTDIR);
        if (_parser.isSet(OPT_WILDDIR))
            wildtype_dir = _parser.getValue(OPT_WILDDIR);

    }

    private void setupOptionParser() throws SCMDException {
        _parser.setOption(new Option(OPT_HELP, "h", "help", "diaplay help message"));
        //_parser.setOption(new Option(OPT_VERBOSE, "v", "verbose", "display verbose messages"));
        _parser.setOption(new OptionWithArgument(OPT_WILDDIR, "w", "wildtype_dir", "DIR",
                "set wildtype input directory base (default = current directory)"));
        _parser.setOption(new OptionWithArgument(OPT_MUTANTDIR, "m", "mutant_dir", "DIR",
                "set mutant input directory base (default = current directory)"));
    }

    private void printUsage() {
        System.out.println("Usage: CalcGroupStat [option]");
        System.out.println(_parser.createHelpMessage());
        System.exit(0);
    }

    //	有効パラメーター抽出プログラム
//	引数はデータのあるディレクトリのパス。
//	1.（パラメーター、そのデータ）のペアを全パラメーターについてHashMapしておく。
//	2.有効パラメーターのリストを読み込む。
//	3.有効パラメーターを横軸、変異株ごとのデータを縦軸にとったテーブルを作る。
//	4.出力する。
    private void init() {
        _parameterToDataTable = new HashMap<>();
        _yukoParameterList = new Vector<>();
        _yukoDataTable = new Vector<>();
    }

    private void ProcessDir(String dir) {
        try {
            init();
            _dir_name = dir;
            readAllData();
            readParameterList();
            makeYukoDataTable();
            printYukoDataTable();
        } catch (SCMDException e) {
            e.what();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void MakeValidParamTable() {
        ProcessDir(wildtype_dir);
        ProcessDir(mutant_dir);
    }

    private void readAllData() throws SCMDException {
        for (String s : GROUP_FILE_NAME) {
            readFile(_dir_name + SEP + s);
        }
    }

    private void readFile(String filename) throws SCMDException {
        CalMorphTable cmt = new CalMorphTable(filename);
        int colSize = cmt.getColSize();
        int rowSize = cmt.getRowSize();
        System.out.println("reading " + filename + "\t" + colSize + " parameters" + "\t" + rowSize + " mutants");
        Vector<String> parameters = cmt.getColLabelList();
        for (Object parameter : parameters) {
            String parameter_name = (String) parameter;

            TableIterator ti = cmt.getVerticalIterator(parameter_name);
            Vector<Cell> verticalData = new Vector<Cell>(rowSize);
            while (ti.hasNext()) {
                Cell c = ti.nextCell();
                verticalData.add(c);
            }
            _parameterToDataTable.put(parameter_name, verticalData);
        }
    }

    private void readParameterList() throws IOException {
        _yukoParameterList.add("name");
        java.io.BufferedReader parameterListReader = new java.io.BufferedReader(new java.io.FileReader(_dir_name + SEP + PARAMETER_LIST));
        String line = "";
        while ((line = parameterListReader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line, "\t");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                _yukoParameterList.add(token);
            }
        }
    }

    private void makeYukoDataTable() {
        for (String parameter : _yukoParameterList) {
            Vector<Cell> verticalData = _parameterToDataTable.get(parameter);
            if (verticalData == null) {
                System.err.println("Error(no data): " + parameter);//*****
                System.exit(-1);
            } else {
                //System.err.println("add parameter "+parameter);//*****
                _yukoDataTable.add(verticalData);
            }
        }
        System.err.println("yukoDataTableSize= " + _yukoDataTable.size());//*****
    }

    private void printYukoDataTable() throws IOException {
        char t = '\t';
        java.io.PrintWriter outfile = new java.io.PrintWriter(new java.io.FileWriter(_dir_name + SEP + OUTPUT_FILE_NAME));
        for (String s : _yukoParameterList) {
            outfile.print(s + t);
            //System.out.print((String)_yukoParameterList.get(i)+t);//*****
        }
        outfile.println();
        //System.out.println();//*****

        int tmp = 0;
        for (int row = 0; row < (_yukoDataTable.get(0)).size(); ++row) {
            //if(((Cell)((Vector)_parameterToDataTable.get("cell_without_complex")).get(row)).doubleValue() < 200.0)continue;
            for (Vector<Cell> verticalData : _yukoDataTable) {
                //if(row>1700)System.out.println(row+" "+i);
                Cell c = (verticalData.get(row));
                outfile.print(c.toString() + t);
                //System.out.print(c.toString()+t);//*******
            }
            outfile.println();
            //System.out.println();//*****
        }
        outfile.close();
    }

    //以下は有効パラメーター決定プログラム
    //細胞数が２００以下の株は無視する。
    private void filter_by_sample_number(Vector data, String dir) {
        Vector Anum = ReadFile(dir + NUM_FILE_NAME[0]);
        Vector A1Bnum = ReadFile(dir + NUM_FILE_NAME[1]);
        Vector Cnum = ReadFile(dir + NUM_FILE_NAME[2]);

        Vector new_data = new Vector();
        for (int i = 1; i < data.size(); ++i) {
            Vector orf_data = (Vector) data.get(i);

            double Asize = Double.parseDouble((String) ((Vector) Anum.get(i)).get(1));
            double A1Bsize = Double.parseDouble((String) ((Vector) A1Bnum.get(i)).get(1));
            double Csize = Double.parseDouble((String) ((Vector) Cnum.get(i)).get(1));

            if (Asize + A1Bsize + Csize < 200) continue;
            new_data.add(data.get(i));
        }
        data = new_data;
    }

    //縦横を逆にする＋String->Double＋missing_valueを消す。
    private Vector transpose_ToDouble_IgnoreNaN(Vector v) {
        java.util.HashSet<String> missingValueSet = new java.util.HashSet<String>();
        Collections.addAll(missingValueSet, missingValueList);

        int x = ((Vector) v.get(0)).size();
        int y = v.size();
        Vector t = new Vector();
        for (int i = 1; i < x; ++i) {
            Vector vertical = new Vector();
            for (int j = 1; j < y; ++j) {
                String s = (String) ((Vector) v.get(j)).get(i);
                if (missingValueSet.contains(s)) continue;
                vertical.add(Double.valueOf(s));
            }
            t.add(vertical);
        }
        return t;
    }

    private Vector ReadFile(String filepath) {
        //System.err.println("reading "+filepath);
        java.io.BufferedReader br = null;
        try {
            br = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(filepath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        String input;
        Vector data = new Vector();
        try {
            while ((input = br.readLine()) != null) {
                String[] line = input.split("\t");
                Vector linevec = new Vector();
                Collections.addAll(linevec, line);
                data.add(linevec);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        //System.err.println(filepath+" : size= "+data.size());
        return data;
    }

    private String getPSDrank(double d) {
        if (d > 1) return "A";
        if (d > 0.875) return "B";
        if (d > 0.75) return "C";
        if (d > 0.5) return "D";
        return "E";
    }

    private String getPFQrank(double d) {
        if (d > 40) return "A";
        if (d > 30) return "B";
        if (d > 20) return "C";
        if (d > 10) return "D";
        return "E";
    }

    private Vector<Integer> getHistogram(Vector data, double min, double max, int n_box) {
        double intervalSize = (max - min) / (double) n_box;
        Vector<Integer> histogram = new Vector<Integer>();
        for (int i = 0; i < n_box + 2; ++i) {
            histogram.add(0);
        }

        for (Object datum : data) {
            double d = (Double) datum;
            int boxNum = (int) Math.floor((d - min) / intervalSize);
            if (d < min) boxNum = 0;
            else if (d >= max) boxNum = n_box + 1;
            else boxNum++;
            int count = histogram.get(boxNum);
            histogram.set(boxNum, count + 1);
        }
        return histogram;
    }

    private double getMode(Vector data) {
        double[] ESD = getEandSD(data);
        double E = ESD[0];
        double SD = ESD[1];
        double min = E - 3 * SD;
        double max = E + 3 * SD;
        double partitionSize = (max - min) / (double) number_of_partitions;
        Vector<Integer> histogram = getHistogram(data, min, max, number_of_partitions);
        int max_data = 0;
        double mode = 0;
        for (int i = 1; i < number_of_partitions; ++i) {
            int num_of_data = histogram.get(i);
            if (num_of_data > max_data) {
                max_data = num_of_data;
                mode = min + ((i - 1) + 0.5) * partitionSize;
            }
        }
        return mode;
    }

    private double getMode(Vector data, double min, double max) {
        double partitionSize = (max - min) / (double) number_of_partitions;
        Vector<Integer> histogram = getHistogram(data, min, max, number_of_partitions);
        int max_data = 0;
        double mode = 0;
        for (int i = 1; i < number_of_partitions; ++i) {
            int num_of_data = histogram.get(i);
            if (num_of_data > max_data) {
                max_data = num_of_data;
                mode = min + ((i - 1) + 0.5) * partitionSize;
            }
        }
        return mode;
    }

    private double[] getEandSD(Vector data) {
        double tmpExp = 0;
        double sumOfSquares = 0;
        int n = 0;
        for (int i = 0; i < data.size(); ++i) {
            double x = (Double) data.get(i);
            x -= tmpExp;
            tmpExp += x / (i + 1);
            sumOfSquares += (i * x * x) / (i + 1);
        }
        sumOfSquares = Math.sqrt(sumOfSquares / (data.size() - 1));
        double[] result = new double[2];
        result[0] = tmpExp;//expectation
        result[1] = (sumOfSquares == 0) ? 1 : sumOfSquares;//standard deviation
        return result;
    }

    private double percentileRank(Vector data, double point) {
        int countBigger = 0;
        int countSame = 0;
        for (Object datum : data) {
            double d = (Double) datum;
            if (d > point) ++countBigger;
            if (d == point) ++countSame;
        }
        return 100 * (countBigger + countSame / 2.0) / (double) data.size();
    }

    private void MakeValidParamFile(String filename, Vector param_names, Vector wildtype_, Vector mutants_) {
        char t = '\t', n = '\n';
        try {
            java.io.PrintWriter wpw = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(wildtype_dir + SEP + PARAMETER_LIST, true)));
            java.io.PrintWriter mpw = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(mutant_dir + SEP + PARAMETER_LIST, true)));
            for (int i = 0; i < param_names.size(); ++i) {
                String parameter_name = (String) param_names.get(i);
                if (parameter_name.equals("#"))
                    continue;

                if (filename.endsWith("conA_data.xls")
                        || filename.endsWith("actin_data.xls")
                        || filename.endsWith("dapi_data.xls")) {
                    if (parameter_name.equals("name")
                            || parameter_name.equals("C119")
                            || parameter_name.equals("C120")
                            || parameter_name.equals("C121")
                            || parameter_name.equals("C122")
                            || parameter_name.equals("C123")
                            || parameter_name.equals("C124")
                            || parameter_name.equals("C125")
                            || parameter_name.equals("A105")
                            || parameter_name.equals("A106")
                            || parameter_name.equals("A107")
                            || parameter_name.equals("A108")
                            || parameter_name.equals("A109")
                            || parameter_name.equals("A110")
                            || parameter_name.equals("A111")
                            || parameter_name.equals("A112")
                            || parameter_name.equals("A113")
                            || parameter_name.equals("A114")
                            || parameter_name.equals("A115")
                            || parameter_name.equals("A116")
                            || parameter_name.equals("A117")
                            || parameter_name.equals("A118")
                            || parameter_name.equals("A119")
                            || parameter_name.equals("D201")
                            || parameter_name.equals("D202")
                            || parameter_name.equals("D203")
                            || parameter_name.equals("D204")
                            || parameter_name.equals("D205")
                            || parameter_name.equals("D206")
                            || parameter_name.equals("D207")
                            || parameter_name.equals("D208")
                            || parameter_name.equals("D209")
                            || parameter_name.equals("D210")
                            || parameter_name.equals("D211")
                            || parameter_name.equals("D212")
                            || parameter_name.equals("D213")
                            || parameter_name.equals("D214")
                            || parameter_name.equals("D215")
                            || parameter_name.equals("D216")) {
                    } else {
                        continue;
                    }
                } else if (
                        filename.endsWith("conA_SDdata.xls")
                                || filename.endsWith("actin_SDdata.xls")
                                || filename.endsWith("dapi_SDdata.xls")) {
                    if (parameter_name.equals("CCV119")
                            || parameter_name.equals("CCV120")
                            || parameter_name.equals("CCV121")
                            || parameter_name.equals("CCV122")
                            || parameter_name.equals("CCV123")
                            || parameter_name.equals("CCV124")
                            || parameter_name.equals("CCV125")
                            || parameter_name.equals("ACV105")
                            || parameter_name.equals("ACV106")
                            || parameter_name.equals("ACV107")
                            || parameter_name.equals("ACV108")
                            || parameter_name.equals("ACV109")
                            || parameter_name.equals("ACV110")
                            || parameter_name.equals("ACV111")
                            || parameter_name.equals("ACV112")
                            || parameter_name.equals("ACV113")
                            || parameter_name.equals("ACV114")
                            || parameter_name.equals("ACV115")
                            || parameter_name.equals("ACV116")
                            || parameter_name.equals("ACV117")
                            || parameter_name.equals("ACV118")
                            || parameter_name.equals("ACV119")
                            || parameter_name.equals("DCV201")
                            || parameter_name.equals("DCV202")
                            || parameter_name.equals("DCV203")
                            || parameter_name.equals("DCV204")
                            || parameter_name.equals("DCV205")
                            || parameter_name.equals("DCV206")
                            || parameter_name.equals("DCV207")
                            || parameter_name.equals("DCV208")
                            || parameter_name.equals("DCV209")
                            || parameter_name.equals("DCV210")
                            || parameter_name.equals("DCV211")
                            || parameter_name.equals("DCV212")
                            || parameter_name.equals("DCV213")
                            || parameter_name.equals("DCV214")
                            || parameter_name.equals("DCV215")
                            || parameter_name.equals("DCV216")) {
                    } else {
                        continue;
                    }
                }

                Vector w = (Vector) wildtype_.get(i);
                Vector m = (Vector) mutants_.get(i);
                if (w.size() == 0 || m.size() == 0)
                    System.err.println(
                            "Error: datasize=0 parameter="
                                    + param_names.get(i));
                double[] wildtypeESD = getEandSD(w);
                double[] mutantsESD = getEandSD(m);
                double mutantsMode = getMode(m);
                double pRankWildtypeMean = percentileRank(m, wildtypeESD[0]);
                double pRankMutantsMode = percentileRank(m, mutantsMode);
                if (getPSDrank(wildtypeESD[1] / mutantsESD[1]).equals("A") || getPFQrank(Math.abs(pRankMutantsMode - pRankWildtypeMean)).equals("A")) {
                    continue;
                }
                System.out.print((String)param_names.get(i)+t);
                wpw.print((String)param_names.get(i)+n);
                mpw.print((String)param_names.get(i)+n);
                //System.out.print(wildtypeESD[1]/mutantsESD[1]);
                //System.out.print(t);
                //System.out.println(Math.abs(pRankMutantsMode-pRankWildtypeMean));
                //System.out.println(getPSDrank( wildtypeESD[1]/mutantsESD[1])+t+getPFQrank(Math.abs(pRankMutantsMode-pRankWildtypeMean)));
            }
            wpw.close();
            mpw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void MakeVPListAll() {
        try {//clear PARAMETER_LIS file
            java.io.PrintWriter wpw =
                    new java.io.PrintWriter(
                            new java.io.BufferedWriter(
                                    new java.io.FileWriter(
                                            wildtype_dir + SEP + PARAMETER_LIST)));
            wpw.close();
            java.io.PrintWriter mpw =
                    new java.io.PrintWriter(
                            new java.io.BufferedWriter(
                                    new java.io.FileWriter(
                                            mutant_dir + SEP + PARAMETER_LIST)));
            mpw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        for (String s : GROUP_FILE_NAME) {
            Vector wildtype = ReadFile(wildtype_dir + SEP + s);
            Vector mutants = ReadFile(mutant_dir + SEP + s);
            Vector parameter = ((Vector) wildtype.get(0));
            filter_by_sample_number(wildtype, wildtype_dir);
            filter_by_sample_number(mutants, mutant_dir);
            wildtype = transpose_ToDouble_IgnoreNaN(wildtype);
            mutants = transpose_ToDouble_IgnoreNaN(mutants);
            parameter.removeElementAt(0);

            MakeValidParamFile(s, parameter, wildtype, mutants);
        }
    }
}


//--------------------------------------
// $Log: ValidParameters.java,v $
// Revision 1.10  2004/08/27 14:30:57  nakatani
// *** empty log message ***
//
// Revision 1.9  2004/08/27 12:08:56  nakatani
// Total stage のパラメーターのＣＶを追加。
//
// Revision 1.8  2004/08/27 12:04:42  nakatani
// Total stage のパラメーターを追加。
//
// Revision 1.7  2004/08/04 10:26:17  nakatani
// *** empty log message ***
//
// Revision 1.6  2004/08/04 04:53:10  nakatani
// OptionParserを追加
//
// Revision 1.5  2004/08/04 03:17:06  nakatani
// 有効なパラメーターを選ぶ関数を追加。
//
// Revision 1.4  2004/08/01 08:19:36  leo
// BasicTableにhasRowLabelを追加
// XMLOutputterで、java.io.writerを使えるように変更
// （JSPのwriterがjava.io.Writerの派生クラスのため)
//
// Revision 1.3  2004/07/27 05:18:12  leo
// TableIteratorの微調整
//
// Revision 1.2  2004/06/29 01:31:37  leo
// データfile名を定義するinterfaceを表に出しました
//
// Revision 1.1  2004/06/24 04:02:39  nakatani
// サンプル数が２００以下の株を除く処理を追加。
//
// Revision 1.9  2004/05/20 01:47:11  nakatani
// *** empty log message ***
//
// Revision 1.8  2004/05/11 04:55:32  nakatani
// 読み込むファイル名と書き込むファイル名を変更。
//
// Revision 1.7  2004/05/10 05:18:51  nakatani
// output部分をデバッグ。PrintWriterから出力されないバグがまだ残る。
//
// Revision 1.6  2004/05/08 03:16:05  leo
// char SEP='/'と定義しているところを取り除きました。
//
// Revision 1.5  2004/05/08 03:14:02  leo
// ファイル名のパスの区切りに File.separator を使うようにしました。
// （Windows(\), Linux(/)互換のため)
// mainメソッドでの例外を、catchしてエラーメッセージを出力するようにしました。
// - mainが例外をthrowすると、ユーザーに直にエラーメッセージが表示されて気持ち悪いので(^-^;
//
// Revision 1.4  2004/05/07 21:10:52  nakatani
// *** empty log message ***
//
// Revision 1.3  2004/05/06 00:48:34  nakatani
// test
//
// Revision 1.2  2004/05/02 19:38:41  leo
// CVS用のコメントを足しておきました
//
//--------------------------------------
