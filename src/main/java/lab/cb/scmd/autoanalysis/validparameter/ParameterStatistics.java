//--------------------------------------
//SCMD Project
//
//ParamterStatistics.java 
//Since:  2004/09/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------


package lab.cb.scmd.autoanalysis.validparameter;

import lab.cb.scmd.autoanalysis.grouping.CalMorphTable;
import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.cui.Option;
import lab.cb.scmd.util.cui.OptionParser;
import lab.cb.scmd.util.cui.OptionWithArgument;
import lab.cb.scmd.util.morphologicalarray.BoxCox;
import lab.cb.scmd.util.morphologicalarray.ChiSquareGoodnessOfFitTest;
import lab.cb.scmd.util.morphologicalarray.StatisticalTests;
import lab.cb.scmd.util.table.TableIterator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author nakatani
 */
public class ParameterStatistics {
    OptionParser parser = new OptionParser();
    boolean verbose = true;
    boolean SIGMA = false;

    CalMorphTable wildtypeTable;
    CalMorphTable mutantTable;
    int parameterSize;
    int orfSizeWildtype;
    int orfSizeMutant;

    BoxCox[] transformedWildtypeList;

    String outputFile = null;
    String transformedWildtypeFile = null;
    String transformedMutantFile = null;
    String orfStatFile = null;
    String excelMutantFile = null;

    // option IDs
    final static int OPT_HELP = 0;
    final static int OPT_VERBOSE = 1;
    final static int OPT_WILD = 2;
    final static int OPT_MUTANT = 3;
    //final static int    OPT_PARAMETER       = 4;
    final static int OPT_OUTPUT_PARAMETER_STAT = 5;
    final static int OPT_OUTPUT_TRANSFORMED_WILDTYPE = 6;
    final static int OPT_OUTPUT_TRANSFORMED_MUTANT = 7;
    final static int OPT_OUTPUT_ORF_STAT = 8;
    final static int OPT_SIGMA = 9;
    final static int OPT_EXCEL_MUTANT_FILE = 10;


    ParameterStatistics() {
    }

    /**
     * @param args
     * @throws IOException
     * @throws SCMDException
     */
    public void setupByArguments(String[] args) throws IOException, SCMDException {
        setupOptionParser();
        parser.getContext(args);

        if (parser.isSet(OPT_HELP)) printUsage(0);
        if (parser.isSet(OPT_VERBOSE)) verbose = true;
        if (parser.isSet(OPT_SIGMA)) SIGMA = true;

        if (parser.isSet(OPT_WILD)) {
            String wildtype_file = parser.getValue(OPT_WILD);
            wildtypeTable = new CalMorphTable(wildtype_file);
            orfSizeWildtype = wildtypeTable.getRowSize();
            parameterSize = wildtypeTable.getColSize();
        } else {
            printUsage(-1);
        }
        if (parser.isSet(OPT_MUTANT)) {
            String mutant_file = parser.getValue(OPT_MUTANT);
            mutantTable = new CalMorphTable(mutant_file);
            orfSizeMutant = mutantTable.getRowSize();
            if (mutantTable.getColSize() != parameterSize) {
                System.err.println("assertion failed. mutant_parameter_size!=wildtype_parameter_size.");
                System.exit(-1);
            }
        } else {
            printUsage(-1);
        }
        if (parser.isSet(OPT_OUTPUT_PARAMETER_STAT)) {
            outputFile = parser.getValue(OPT_OUTPUT_PARAMETER_STAT);
            FileWriter fw = new FileWriter(outputFile);
            fw.close();
        } else {
            printUsage(-1);
        }
        if (parser.isSet(OPT_OUTPUT_TRANSFORMED_WILDTYPE)) {
            transformedWildtypeFile = parser.getValue(OPT_OUTPUT_TRANSFORMED_WILDTYPE);
            FileWriter fw = new FileWriter(transformedWildtypeFile);
            fw.close();
        }
        if (parser.isSet(OPT_OUTPUT_TRANSFORMED_MUTANT)) {
            transformedMutantFile = parser.getValue(OPT_OUTPUT_TRANSFORMED_MUTANT);
            FileWriter fw = new FileWriter(transformedMutantFile);
            fw.close();
        }
        if (parser.isSet(OPT_OUTPUT_ORF_STAT)) {
            orfStatFile = parser.getValue(OPT_OUTPUT_ORF_STAT);
            FileWriter fw = new FileWriter(orfStatFile);
            fw.close();
        }
        if (parser.isSet(OPT_EXCEL_MUTANT_FILE)) {
            excelMutantFile = parser.getValue(OPT_EXCEL_MUTANT_FILE);
            FileWriter fw = new FileWriter(excelMutantFile);
            fw.close();
        }
    }

    void setupOptionParser() throws SCMDException {
        parser.setOption(new Option(OPT_HELP, "h", "help", "diaplay help message"));
        parser.setOption(new Option(OPT_VERBOSE, "v", "verbose", "display verbose messages"));
        parser.setOption(new OptionWithArgument(OPT_WILD, "W", "wildtype_file", "FILE", "set wildtype input file"));
        parser.setOption(new OptionWithArgument(OPT_MUTANT, "M", "mutant_file", "FILE", "set mutant input file"));
        //parser.setOption(new OptionWithArgument(OPT_PARAMETER,"p","parameter_file","FILE","set parameter file"));
        parser.setOption(new OptionWithArgument(OPT_OUTPUT_PARAMETER_STAT, "p", "parameter_stat_output_file", "FILE", "set parameter stat output file"));
        parser.setOption(new OptionWithArgument(OPT_OUTPUT_TRANSFORMED_WILDTYPE, "w", "transformed_wildtype_output_file", "FILE", "set transformed wildtype output file"));
        parser.setOption(new OptionWithArgument(OPT_OUTPUT_TRANSFORMED_MUTANT, "m", "transformed_mutant_output_file", "FILE", "set transformed mutant output file"));
        parser.setOption(new OptionWithArgument(OPT_OUTPUT_ORF_STAT, "o", "orf_stat_output_file", "FILE", "set orf stat output file"));
        parser.setOption(new Option(OPT_SIGMA, "s", "sigma_threshold", "thresholding by sigma"));
        parser.setOption(new OptionWithArgument(OPT_EXCEL_MUTANT_FILE, "e", "excel_mutant_output_file", "FILE", "set excel mutant output file"));

    }

    public void printUsage(int exitCode) {
        System.out.println("Usage: ParameterStatistics [option]");
        System.out.println(parser.createHelpMessage());
        System.exit(exitCode);
    }


    public static void main(String[] args) {
        ParameterStatistics ps = new ParameterStatistics();
        try {
            ps.setupByArguments(args);
            ps.transformAllParameters();
            ps.outputStatistics();
            ps.outputTransformedTable();
            ps.outputOrfStat();
            ps.outputExcelTable();
        } catch (SCMDException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void transformAllParameters() {
        transformedWildtypeList = new BoxCox[parameterSize];
        for (int i = 1; i < parameterSize; ++i) {//i=0 --> "name"
            System.out.println(wildtypeTable.getColLabel(i) + "\t");
            Double[] w = getCleanDataByParameter(wildtypeTable, i);
            transformedWildtypeList[i] = new BoxCox(w);
        }
    }

    public void outputStatistics() throws SCMDException, IOException {
        PrintWriter outfile = new PrintWriter(new FileWriter(outputFile));

        //Vector parameterList=wildtypeData.getParameterNames();
        //HashMap wildtypeOrfIgnore=wildtypeData.getOrfIgnore(sampleNumberFilter_threshold);
        //HashMap mutantOrfIgnore=mutantData.getOrfIgnore(sampleNumberFilter_threshold);

        outfile.print("parameter" + "\t" + "valid_wildtype_data" + "\t" + "valid_mutant_data" + "\t" + "SDratio" + "\t" + "PFQ" + "\t");
        outfile.print("mutant_mode" + "\t");
        outfile.print("wildtype_normality_chisquare(p=0.01)" + "\t");
        outfile.print("wildtype_normality_skewness(p=0.01)" + "\t");
        outfile.print("transformed_wildtype_normality_chisquare(p=0.01)" + "\t");
        if (SIGMA) {
            BoxCox.printHeaderSigma(outfile);
            BoxCox.printPValueListHeaderSigma(outfile);
            BoxCox.printPValueListHeaderSigma(outfile);
        } else {
            BoxCox.printHeader(outfile);
            BoxCox.printPValueListHeader(outfile);
            BoxCox.printPValueListHeader(outfile);
        }
        outfile.println();
        for (int i = 1; i < parameterSize; ++i) {//i=0 --> "name"
            //Double[] w=wildtypeData.getValidDataForIthParameter(i,wildtypeOrfIgnore);
            //Double[] m=mutantData.getValidDataForIthParameter(i,mutantOrfIgnore);
            //System.err.println((String)parameterList.get(i)+"\t"+w.length+"\t"+m.length+"\t");
            Double[] w = getCleanDataByParameter(wildtypeTable, i);
            Double[] m = getCleanDataByParameter(mutantTable, i);

            outfile.print(wildtypeTable.getColLabel(i) + "\t" + w.length + "\t" + m.length + "\t");
            outfile.print(getSDRatio(w, m) + "\t");
            outfile.print(getPercentileRankDifferenceBetweenMutantModeAndWildtypeMean(w, m) + "\t");
            outfile.print(StatisticalTests.getMode(m) + "\t");

            ChiSquareGoodnessOfFitTest gof = new ChiSquareGoodnessOfFitTest(w);
            if (gof.isSignificant(0.01)) {
                outfile.print("not_normal" + "\t");
            } else {
                outfile.print("normal" + "\t");
            }

            if (StatisticalTests.getSampleSkewness(w) < 0.57) {
                outfile.print("normal" + "\t");
            } else {
                outfile.print("not_normal" + "\t");
            }

            //System.out.println(wildtypeTable.getColLabel(i));
            //PowerTransformation pt=new PowerTransformation(w);
            //Double[] transformedData=pt.getTransformedData();
            Double[] transformedData = transformedWildtypeList[i].getTransformedData();
            ChiSquareGoodnessOfFitTest gof2 = new ChiSquareGoodnessOfFitTest(transformedData);
            if (gof2.isSignificant(0.01)) {
                outfile.print("not_normal" + "\t");
            } else {
                outfile.print("normal" + "\t");
            }

            if (SIGMA) {
                transformedWildtypeList[i].printSigma(outfile);
                int[] counterW = getSignificantDataNumberSigma(transformedWildtypeList[i], w);
                for (int k = 0; k < counterW.length; ++k) {
                    outfile.print(counterW[k] + "\t");
                }
                int[] counterM = getSignificantDataNumberSigma(transformedWildtypeList[i], m);
                for (int k = 0; k < counterM.length; ++k) {
                    outfile.print(counterM[k] + "\t");
                }
            } else {
                transformedWildtypeList[i].print(outfile);
                int[] counterW = getSignificantDataNumber(transformedWildtypeList[i], w);
                for (int k = 0; k < counterW.length; ++k) {
                    outfile.print(counterW[k] + "\t");
                }
                int[] counterM = getSignificantDataNumber(transformedWildtypeList[i], m);
                for (int k = 0; k < counterM.length; ++k) {
                    outfile.print(counterM[k] + "\t");
                }
            }
            outfile.println();
        }
        outfile.close();
    }

    private int[] getSignificantDataNumber(BoxCox bc, Double[] data) {
        int[] counter = new int[2 * BoxCox.pValueList.length];
        for (int k = 0; k < BoxCox.pValueList.length; ++k) {
            double cvalue = bc.getOneSidedCriticalValueForP(BoxCox.pValueList[BoxCox.pValueList.length - 1 - k]).doubleValue();
            for (int n = 0; n < data.length; ++n) {
                if (data[n].doubleValue() <= cvalue) ++counter[k];
            }
        }
        for (int k = 0; k < BoxCox.pValueList.length; ++k) {
            double cvalue = bc.getOneSidedCriticalValueForP(1 - BoxCox.pValueList[k]).doubleValue();
            for (int n = 0; n < data.length; ++n) {
                if (data[n].doubleValue() >= cvalue) ++counter[BoxCox.pValueList.length + k];
            }
        }
        return counter;
    }

    private int[] getSignificantDataNumberSigma(BoxCox bc, Double[] data) {
        int[] counter = new int[2 * BoxCox.sigmaValueList.length];
        for (int k = 0; k < BoxCox.sigmaValueList.length; ++k) {
            double cvalue = bc.getOneSidedCriticalValueForMeanPlusDSigma(-BoxCox.sigmaValueList[BoxCox.sigmaValueList.length - 1 - k]).doubleValue();
            //double cvalue=bc.getOneSidedCriticalValueForMinMinusSigma(BoxCox.sigmaValueList[BoxCox.sigmaValueList.length-1-k]).doubleValue();
            for (int n = 0; n < data.length; ++n) {
                if (data[n].doubleValue() <= cvalue) ++counter[k];
            }
        }
        for (int k = 0; k < BoxCox.sigmaValueList.length; ++k) {
            double cvalue = bc.getOneSidedCriticalValueForMeanPlusDSigma(BoxCox.sigmaValueList[k]).doubleValue();
            //double cvalue=bc.getOneSidedCriticalValueForMaxPlusSigma(BoxCox.sigmaValueList[k]).doubleValue();
            for (int n = 0; n < data.length; ++n) {
                if (data[n].doubleValue() >= cvalue) ++counter[BoxCox.sigmaValueList.length + k];
            }
        }
        return counter;
    }

    private int getSignificantDataLabelSigma(BoxCox bc, double data) {
        for (int k = 0; k < BoxCox.sigmaValueList.length; ++k) {
            double cvalue = bc.getOneSidedCriticalValueForMeanPlusDSigma(-BoxCox.sigmaValueList[BoxCox.sigmaValueList.length - 1 - k]).doubleValue();
            if (data <= cvalue) return -1 * (BoxCox.sigmaValueList.length - k);
        }
        for (int k = 0; k < BoxCox.sigmaValueList.length; ++k) {
            double cvalue = bc.getOneSidedCriticalValueForMeanPlusDSigma(BoxCox.sigmaValueList[BoxCox.sigmaValueList.length - 1 - k]).doubleValue();
            if (data >= cvalue) return (BoxCox.sigmaValueList.length - k);
        }
        return 0;//not significant
    }

    private double getSDRatio(Double[] wild, Double[] mutant) {
        Double[] ESD = StatisticalTests.getEandSD(wild);
        double wsd = ESD[1].doubleValue();
        ESD = StatisticalTests.getEandSD(mutant);
        double msd = ESD[1].doubleValue();
        return wsd / msd;
    }

    private double getPercentileRankDifferenceBetweenMutantModeAndWildtypeMean(Double[] wild, Double[] mutant) {
        double mutantMode = StatisticalTests.getMode(mutant);
        double percentileRankOfMutantMode = StatisticalTests.percentileRankFromLower(mutant, mutantMode);
        Double[] ESD = StatisticalTests.getEandSD(wild);
        double wildtypeMean = ESD[0].doubleValue();
        double percentileRankOfWildtypeMean = StatisticalTests.percentileRankFromLower(wild, wildtypeMean);
        return Math.abs(percentileRankOfMutantMode - percentileRankOfWildtypeMean);
    }

    /**
     * CalMorphTableから指定されたパラメータのデータを返す。NaNや-1は無視する。
     *
     * @author nakatani
     */
    private Double[] getCleanDataByParameter(CalMorphTable cmt, int parameter) {
        Vector dataW = new Vector();
        TableIterator i = cmt.getVerticalIterator(parameter);
        for (int j = 0; i.hasNext(); ++j) {
            lab.cb.scmd.util.table.Cell c = (lab.cb.scmd.util.table.Cell) i.next();
            if (c.isValidAsDouble()) {
                double d = c.doubleValue();
                if (d < 0) continue;
                dataW.add(new Double(d));
            }
        }
        Double[] validData = new Double[dataW.size()];
        for (int j = 0; j < dataW.size(); ++j) {
            validData[j] = (Double) dataW.get(j);
        }
        return validData;
    }

    private Double[] getAllDataByParameter(CalMorphTable cmt, int parameter) {
        Vector dataW = new Vector();
        TableIterator i = cmt.getVerticalIterator(parameter);
        for (int j = 0; i.hasNext(); ++j) {
            lab.cb.scmd.util.table.Cell c = (lab.cb.scmd.util.table.Cell) i.next();
            double d = c.doubleValue();
            dataW.add(new Double(d));
        }
        Double[] validData = new Double[dataW.size()];
        for (int j = 0; j < dataW.size(); ++j) {
            validData[j] = (Double) dataW.get(j);
        }
        return validData;
    }

    public void outputTransformedTable() throws IOException {
        if (transformedWildtypeFile != null) {
            toTransformedTable(transformedWildtypeFile, wildtypeTable);
        }
        if (transformedMutantFile != null) {
            //toTransformedTable(transformedMutantFile,mutantTable);
        }
    }

    private void toTransformedTable(String filename, CalMorphTable table) throws IOException {
        int paramSize = table.getColSize();
        int orfSize = table.getRowSize();
        //Double[][] transformedTable=new Double[paramSize][orfSize];
        //for(int parameter=1;parameter<parameterSize;++parameter){
        //	Double[] dataToTransform=getCleanDataByParameter(table,parameter);
        //	transformedTable[parameter]=transformedWildtypeList[parameter].getSTDTransformedData(dataToTransform);
        //}
        PrintWriter transformed = new PrintWriter(new FileWriter(filename));
        for (int p = 0; p < paramSize; ++p) {
            transformed.print(table.getColLabel(p) + "\t");
        }
        transformed.println();
        for (int orf = 0; orf < orfSize; ++orf) {
            transformed.print(table.getCell(orf, 0) + "\t");
            for (int p = 1; p < paramSize; ++p) {
                //transformed.print(transformedTable[p][orf]+"\t");
                lab.cb.scmd.util.table.Cell c = table.getCell(orf, p);
                if (c.isValidAsDouble() && c.doubleValue() >= 0) {
                    Double[] data = new Double[1];
                    data[0] = new Double(c.doubleValue());
                    transformed.print(transformedWildtypeList[p].getSTDTransformedData(data)[0].doubleValue() + "\t");
                } else {
                    transformed.print("NaN\t");
                }
            }
            transformed.println();
        }
        transformed.close();
    }

    public void outputExcelTable() throws IOException {
//		if(excelWildtypeFile!=null){
//			toExcelTable(excelWildtypeFile,wildtypeTable);
//		}
        if (excelMutantFile != null) {
            toExcelTable(excelMutantFile, mutantTable);
        }
    }

    private void toExcelTable(String filename, CalMorphTable table) throws IOException {
        int paramSize = table.getColSize();
        int orfSize = table.getRowSize();
        int[][] labeledTable = new int[paramSize][orfSize];
        for (int parameter = 1; parameter < parameterSize; ++parameter) {
            Double[] dataToTransform = new Double[orfSize];
            for (int orf = 0; orf < orfSize; ++orf) {
                dataToTransform[orf] = new Double(table.getCell(orf, parameter).doubleValue());
            }
            Double[] lowerSigmaValue = new Double[BoxCox.sigmaValueList.length];
            Double[] upperSigmaValue = new Double[BoxCox.sigmaValueList.length];
            for (int i = 0; i < BoxCox.sigmaValueList.length; ++i) {
                lowerSigmaValue[i] = transformedWildtypeList[parameter].getOneSidedCriticalValueForMeanPlusDSigma(-BoxCox.sigmaValueList[i]);
                upperSigmaValue[i] = transformedWildtypeList[parameter].getOneSidedCriticalValueForMeanPlusDSigma(BoxCox.sigmaValueList[i]);
            }
            for (int orf = 0; orf < orfSize; ++orf) {
                labeledTable[parameter][orf] = 0;
                for (int k = BoxCox.sigmaValueList.length - 1; k >= 0; --k) {
                    if (dataToTransform[orf].doubleValue() <= lowerSigmaValue[k].doubleValue()) {
                        labeledTable[parameter][orf] = -1 * (k + 1);
                        break;
                    } else if (dataToTransform[orf].doubleValue() >= upperSigmaValue[k].doubleValue()) {
                        labeledTable[parameter][orf] = (k + 1);
                        break;
                    }
                }
            }
        }
        PrintWriter transformed = new PrintWriter(new FileWriter(filename));
        for (int p = 0; p < paramSize; ++p) {
            transformed.print(table.getColLabel(p) + "\t");
        }
        transformed.println();
        for (int orf = 0; orf < orfSize; ++orf) {
            transformed.print(table.getCell(orf, 0) + "\t");
            for (int p = 1; p < paramSize; ++p) {
                transformed.print(labeledTable[p][orf] + "\t");
            }
            transformed.println();
        }
        transformed.close();
    }

    /***
     public void toTransformedWildtypeTable() throws IOException{
     Double[][] transformedTable=new Double[parameterSize][orfSizeWildtype];
     for(int parameter=1;parameter<parameterSize;++parameter){
     transformedTable[parameter]=transformedWildtypeList[parameter].getSTDTransformedData();
     }
     PrintWriter transformed = new PrintWriter(new FileWriter(transformedWildtypeFile));
     //transformed.print("orf"+"\t");
     for(int p=0;p<parameterSize;++p){
     transformed.print(wildtypeTable.getColLabel(p)+"\t");
     }
     transformed.println();
     for(int orf=0;orf<orfSizeWildtype;++orf){
     transformed.print(wildtypeTable.getCell(orf,0)+"\t");
     for(int p=1;p<parameterSize;++p){
     transformed.print(transformedTable[p][orf]+"\t");
     //transformed.print(table.getCell(orf,p)+"\t");
     }
     transformed.println();
     }
     transformed.close();
     }***/

    public void outputOrfStat() throws IOException {
        if (orfStatFile != null) {
            PrintWriter pw = new PrintWriter(new FileWriter(orfStatFile));
            outputOrfStat(pw, wildtypeTable);
            pw.println();
            outputOrfStat(pw, mutantTable);
            pw.println();
            pw.close();
        }
    }

    private void outputOrfStat(PrintWriter pw, CalMorphTable table) throws IOException {
        int orfSize = table.getRowSize();
        int pvalueSize;
        if (SIGMA) {
            pvalueSize = BoxCox.sigmaValueList.length;
        } else {
            pvalueSize = BoxCox.pValueList.length;
        }
        double[] upperCriticalValues = new double[pvalueSize];
        double[] lowerCriticalValues = new double[pvalueSize];
        int[][][] count = new int[orfSize][2][pvalueSize];
        for (int x = 0; x < orfSize; ++x) {
            for (int y = 0; y < pvalueSize; ++y) {
                count[x][0][y] = 0;
                count[x][1][y] = 0;
            }
        }
        for (int parameter = 1; parameter < parameterSize; ++parameter) {
            for (int i = 0; i < pvalueSize; ++i) {
                if (SIGMA) {
                    lowerCriticalValues[i] = transformedWildtypeList[parameter].getOneSidedCriticalValueForMeanPlusDSigma(-BoxCox.sigmaValueList[i]).doubleValue();
                    //lowerCriticalValues[i]=transformedWildtypeList[parameter].getOneSidedCriticalValueForMinMinusSigma(BoxCox.sigmaValueList[i]).doubleValue();
                } else {
                    lowerCriticalValues[i] = transformedWildtypeList[parameter].getOneSidedCriticalValueForP(BoxCox.pValueList[i]).doubleValue();
                }
            }
            for (int i = 0; i < pvalueSize; ++i) {
                if (SIGMA) {
                    upperCriticalValues[i] = transformedWildtypeList[parameter].getOneSidedCriticalValueForMeanPlusDSigma(BoxCox.sigmaValueList[i]).doubleValue();
                    //upperCriticalValues[i]=transformedWildtypeList[parameter].getOneSidedCriticalValueForMaxPlusSigma(BoxCox.sigmaValueList[i]).doubleValue();
                } else {
                    upperCriticalValues[i] = transformedWildtypeList[parameter].getOneSidedCriticalValueForP(1 - BoxCox.pValueList[i]).doubleValue();
                }
            }
            for (int orf = 0; orf < orfSize; ++orf) {
                double value = table.getCell(orf, parameter).doubleValue();
                for (int i = 0; i < pvalueSize; ++i) {
                    if (value <= lowerCriticalValues[i]) ++count[orf][0][i];
                }
                for (int i = 0; i < pvalueSize; ++i) {
                    if (upperCriticalValues[i] <= value) ++count[orf][1][i];
                }
            }
        }

        pw.print("orf" + "\t");
        for (int i = 0; i < pvalueSize; ++i) {
            if (SIGMA) {
                pw.print(BoxCox.sigmaValueList[i] + "\t");
            } else {
                pw.print(BoxCox.pValueList[i] + "\t");
            }
        }
        pw.println();
        for (int orf = 0; orf < orfSize; ++orf) {
            pw.print(table.getCell(orf, 0).toString() + "\t");
            for (int i = 0; i < pvalueSize; ++i) {
                pw.print((count[orf][0][i] + count[orf][1][i]) + "\t");
            }
            pw.println();
        }
        pw.println();
        pw.flush();

        //make hist
        pw.print("count" + "\t");
        int[][] hist = new int[pvalueSize][parameterSize + 1];
        for (int i = 0; i < pvalueSize; ++i) {
            if (SIGMA) {
                pw.print(BoxCox.sigmaValueList[i] + "\t");
            } else {
                pw.print(BoxCox.pValueList[i] + "\t");
            }
            for (int j = 0; j < parameterSize + 1; ++j) {
                hist[i][j] = 0;
            }
        }
        pw.println();

        for (int i = 0; i < pvalueSize; ++i) {
            for (int orf = 0; orf < orfSize; ++orf) {
                ++hist[i][count[orf][0][i] + count[orf][1][i]];
            }
        }

        for (int i = 0; i < parameterSize + 1; ++i) {
            pw.print(i + "\t");
            for (int p = 0; p < pvalueSize; ++p) {
                pw.print(hist[p][i] + "\t");
            }
            pw.println();
        }
        pw.println();
    }

}

//--------------------------------------
//$Log: ParameterStatistics.java,v $
//Revision 1.7  2004/12/06 07:05:28  nakatani
//transform後のデータ出力で、変換できないデータはNaNを出すように変更。
//
//Revision 1.6  2004/12/05 10:15:49  nakamu
//366,367行目を勝手に変えました。あとで対策しなおしてください。
//
//Revision 1.5  2004/12/01 08:15:44  nakatani
//BoxCox power transformationのデータを追加。
//
//Revision 1.4  2004/09/22 16:22:59  nakamu
//StringとCharを合わせていた箇所を訂正しました
//
//Revision 1.3  2004/09/18 23:10:30  nakatani
//*** empty log message ***
//
//Revision 1.2  2004/09/03 06:02:00  nakatani
//*** empty log message ***
//
//Revision 1.1  2004/09/02 08:31:44  nakatani
//　
//
//--------------------------------------
