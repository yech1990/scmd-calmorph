package lab.cb.scmd.autoanalysis.validparameter;

import lab.cb.scmd.autoanalysis.grouping.CalMorphTable;
import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.cui.Option;
import lab.cb.scmd.util.cui.OptionParser;
import lab.cb.scmd.util.cui.OptionWithArgument;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;


public class DPSRST {
    // option IDs
    private final static int OPT_HELP = 0;
    private final static int OPT_VERBOSE = 1;
    private final static int OPT_CONTROL = 2;
    private final static int OPT_MUTANT = 3;
    //final static int    OPT_PARAMETER       = 4;
    private final static int OPT_PVALUE_OUTPUT = 5;
    private final static int OPT_DS_STAT_OUTPUT = 6;
    private final static int OPT_PARAM_STAT_OUTPUT = 7;
    private final static double NODATA = -1;
    private final static double[] criticalValue = {0.01, 0.001, 0.0001, 0.00001, 0.000001, 0.0000001, 0.00000001};
    private static final double[] pvalueList = {0.01, 0.001, 0.0001, 0.00001, 0.000001};
    private OptionParser parser = new OptionParser();
    private boolean verbose = false;
    private CalMorphTable controlTable;
    private CalMorphTable mutantTable;
    private int parameterSize;
    private int orfSizeControl;
    private int orfSizeMutant;
    private String pvalueTableOutputFile = null;
    private String dsStatOutputFile = null;
    private String paramStatOutputFile = null;
    private double[][] pvalue = null;
    private int uniqueGsnameSize;
    private Vector<String> uniqueDsnameList = new Vector<>();

    private DPSRST() {
    }

    public static void main(String[] args) {
        DPSRST dps = new DPSRST();
        try {
            dps.setupByArguments(args);
            dps.testAllMutant();
            dps.printPvalueTable();
            dps.printGsnameStat();
            dps.printParameterStat();
        } catch (SCMDException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * @param args
     * @throws IOException
     * @throws SCMDException
     */
    private void setupByArguments(String[] args) throws IOException, SCMDException {
        setupOptionParser();
        parser.getContext(args);

        if (parser.isSet(OPT_HELP)) printUsage(0);
        if (parser.isSet(OPT_VERBOSE)) verbose = true;

        if (parser.isSet(OPT_CONTROL)) {
            String control_file = parser.getValue(OPT_CONTROL);
            controlTable = new CalMorphTable(control_file);
            orfSizeControl = controlTable.getRowSize();
            parameterSize = controlTable.getColSize();
        } else {
            printUsage(-1);
        }
        if (parser.isSet(OPT_MUTANT)) {
            String mutant_file = parser.getValue(OPT_MUTANT);
            mutantTable = new CalMorphTable(mutant_file);
            orfSizeMutant = mutantTable.getRowSize();
            uniqueGsnameSize = getUniqueDsnameSize();
            if (mutantTable.getColSize() != parameterSize) {
                System.err.println("assertion failed. mutant_parameter_size!=wildtype_parameter_size.");
                System.exit(-1);
            }
        } else {
            printUsage(-1);
        }
        if (parser.isSet(OPT_PVALUE_OUTPUT)) {
            pvalueTableOutputFile = parser.getValue(OPT_PVALUE_OUTPUT);
            FileWriter fw = new FileWriter(pvalueTableOutputFile);
            fw.close();
        } else {
            printUsage(-1);
        }
        if (parser.isSet(OPT_DS_STAT_OUTPUT)) {
            dsStatOutputFile = parser.getValue(OPT_DS_STAT_OUTPUT);
            FileWriter fw = new FileWriter(dsStatOutputFile);
            fw.close();
        }
        if (parser.isSet(OPT_PARAM_STAT_OUTPUT)) {
            paramStatOutputFile = parser.getValue(OPT_PARAM_STAT_OUTPUT);
            FileWriter fw = new FileWriter(paramStatOutputFile);
            fw.close();
        }
    }

    private void setupOptionParser() throws SCMDException {
        parser.setOption(new Option(OPT_HELP, "h", "help", "diaplay help message"));
        parser.setOption(new Option(OPT_VERBOSE, "v", "verbose", "display verbose messages"));
        parser.setOption(new OptionWithArgument(OPT_CONTROL, "C", "control_file", "FILE", "set control input file"));
        parser.setOption(new OptionWithArgument(OPT_MUTANT, "M", "mutant_file", "FILE", "set mutant input file"));
        parser.setOption(new OptionWithArgument(OPT_PVALUE_OUTPUT, "p", "pvalue_table_output_file", "FILE", "set pvalue table output file"));
        parser.setOption(new OptionWithArgument(OPT_DS_STAT_OUTPUT, "d", "ds_stat_output_file", "FILE", "set ds stat output file"));
        parser.setOption(new OptionWithArgument(OPT_PARAM_STAT_OUTPUT, "P", "param_stat_output_file", "FILE", "set param stat output file"));

    }

    private void printUsage(int exitCode) {
        System.out.println("Usage: DPSRST [option]");
        System.out.println(parser.createHelpMessage());
        System.exit(exitCode);
    }

    /**
     * CalMorphTableから指定されたパラメータ、
     * 指定されたORF（for(int orf=begin;orf<end;orf++)）のデータを返す。
     * NaNは無視する。
     *
     * @author nakatani
     */
    private Vector<Double> getDataByParameter(CalMorphTable cmt, int parameter, int begin, int end) {
        Vector<Double> data = new Vector<>();
        for (int orf = begin; orf < end; ++orf) {
            lab.cb.scmd.util.table.Cell c = cmt.getCell(orf, parameter);
            //System.out.println(parameter+"\t"+orf+"\t"+c.toString());
            if (c.isValidAsDouble()) {
                double d = c.doubleValue();
                data.add(d);
            }
        }
        return data;
    }

    private Vector<Double> getAllDataByParameter(CalMorphTable cmt, int parameter) {
        return getDataByParameter(cmt, parameter, 0, cmt.getRowSize());
    }

    private double[] foreachMutant(int begin, int end) throws IOException {
        double[] pvalueList = new double[parameterSize];
        for (int parameter = 1; parameter < parameterSize; ++parameter) {
            Vector<Double> controlData = getAllDataByParameter(controlTable, parameter);
            Vector<Double> mutantData = getDataByParameter(mutantTable, parameter, begin, end);
            if (controlData.size() < 10 && mutantData.size() < 10) {
                pvalueList[parameter] = NODATA;
                continue;
            }
            if (controlData.size() == 0 || mutantData.size() == 0) {
                pvalueList[parameter] = NODATA;
                continue;
            }
            RankSumTest rst = new RankSumTest(controlData, mutantData);
            pvalueList[parameter] = rst.getProb();
            //pvalueList[parameter]=rst.get_W();
        }
        return pvalueList;
    }

    private int getEndOfOrf(CalMorphTable table, int begin) {
        String orfName = table.getCell(begin, 0).toString();
        int orf = begin;
        for (; orf < table.getRowSize(); ++orf) {
            if (table.getCell(orf, 0).toString().equals(orfName)) {
            } else break;
        }
        return orf;
    }

    private int getUniqueDsnameSize() {
        int unique = 0;
        int orfEnd;
        for (int orfBegin = 0; orfBegin < mutantTable.getRowSize(); orfBegin = orfEnd) {
            orfEnd = getEndOfOrf(mutantTable, orfBegin);
            unique++;
            uniqueDsnameList.add(mutantTable.getCell(orfBegin, 0).toString());
        }
        return unique;
    }

    private void testAllMutant() throws IOException {
        pvalue = new double[uniqueGsnameSize][parameterSize];
        int orfEnd, i = 0;
        for (int orfBegin = 0; orfBegin < mutantTable.getRowSize(); orfBegin = orfEnd) {
            orfEnd = getEndOfOrf(mutantTable, orfBegin);
            //System.out.println(mutantTable.getCell(orfBegin,0).toString()+"\t"+(orfEnd-orfBegin));
            double[] pvalueList = foreachMutant(orfBegin, orfEnd);
            pvalue[i++] = pvalueList;
        }
    }

    private void printPvalueTable() throws IOException {
        if (pvalueTableOutputFile == null) return;
        PrintWriter pw = new PrintWriter(new FileWriter(pvalueTableOutputFile));
        //print parameter
        Vector<String> paramList = mutantTable.getColLabelList();
        for (Object o : paramList) {
            pw.print(o.toString() + "\t");
        }
        pw.println();

        for (int i = 0; i < uniqueGsnameSize; ++i) {
            pw.print(uniqueDsnameList.get(i) + "\t");
            for (int parameter = 1; parameter < parameterSize; ++parameter) {
                if (pvalue[i][parameter] == NODATA) {
                    pw.print("NaN\t");
                } else {
                    pw.print(pvalue[i][parameter] + "\t");
                }
            }
            pw.println();
        }
        pw.close();
    }

    private void printParameterStat() throws IOException {
        if (paramStatOutputFile == null) return;
        PrintWriter pw = new PrintWriter(new FileWriter(paramStatOutputFile));

        int[][] hist = new int[uniqueGsnameSize + 1][pvalueList.length];
        for (int parameter = 1; parameter < parameterSize; ++parameter) {
            int[] count = new int[pvalueList.length];
            for (int i = 0; i < pvalueList.length; ++i) count[i] = 0;
            for (int orf = 0; orf < uniqueGsnameSize; ++orf) {
                for (int p = 0; p < pvalueList.length; ++p) {
                    if (pvalue[orf][parameter] < pvalueList[p] || 1 - pvalueList[p] < pvalue[orf][parameter]) {
                        count[p]++;
                    }
                }
            }
            for (int p = 0; p < pvalueList.length; ++p) hist[count[p]][p]++;
        }

        pw.print("count" + "\t");
        for (double v : pvalueList) {
            pw.print(v + "\t");
        }
        pw.println();

        for (int i = 0; i < uniqueGsnameSize + 1; ++i) {
            pw.print(i + "\t");
            for (int j = 0; j < pvalueList.length; ++j) {
                pw.print(hist[i][j] + "\t");
            }
            pw.println();
        }
        pw.println();
        pw.close();

    }

    private void printGsnameStat() throws IOException {
        if (dsStatOutputFile == null) return;
        PrintWriter pw = new PrintWriter(new FileWriter(dsStatOutputFile));
        pw.print("gsname\t");
        for (double value : criticalValue) {
            pw.print(value + "\t");
        }
        pw.println();

        int[][] hist = new int[criticalValue.length][parameterSize + 1];
        for (int i = 0; i < criticalValue.length; ++i) {
            for (int j = 0; j < parameterSize; ++j) {
                hist[i][j] = 0;
            }
        }
        for (int ds = 0; ds < uniqueGsnameSize; ++ds) {
            pw.print(uniqueDsnameList.get(ds) + "\t");
            int[] count = new int[criticalValue.length];
            for (int i = 0; i < criticalValue.length; ++i) count[i] = 0;
            for (int p = 1; p < parameterSize; ++p) {
                if (pvalue[ds][p] == NODATA) continue;
                for (int c = 0; c < criticalValue.length; ++c) {
                    if (pvalue[ds][p] <= criticalValue[c] || 1 - criticalValue[c] <= pvalue[ds][p]) {
                        count[c]++;
                    }
                }
            }
            for (int i = 0; i < criticalValue.length; ++i) {
                pw.print(count[i] + "\t");
                hist[i][count[i]]++;
            }
            pw.println();
        }
        pw.println();
        pw.print("count\t");
        for (double v : criticalValue) {
            pw.print(v + "\t");
        }
        pw.println();
        for (int i = 0; i < parameterSize + 1; ++i) {
            for (int j = 0; j < criticalValue.length; ++j) {
                pw.print(i + "\t" + hist[j][i] + "\t");
            }
            pw.println();
        }
        pw.println();

        pw.println("pvalue\tMutant\tFALSE(indep)\tFALSE(not-indep)");
        for (int i = 0; i < criticalValue.length; ++i) {
            double False_indep = (1 - Math.pow((1 - 2 * criticalValue[i]), parameterSize)) * uniqueGsnameSize;
            double False_nonindep = Math.min(1, (2 * criticalValue[i]) * parameterSize) * uniqueGsnameSize;
            pw.println(criticalValue[i] + "\t" + (uniqueGsnameSize - hist[i][0]) + "\t" + False_indep + "\t" + False_nonindep);
        }
        pw.println("total\t" + uniqueGsnameSize);
        pw.println();
        pw.close();
    }
}
