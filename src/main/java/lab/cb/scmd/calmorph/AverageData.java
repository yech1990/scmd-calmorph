//------------------------------------
// SCMD Project
//  
// AverageData.java 
// Since:  2004/04/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.calmorph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Vector;

class AverageData {
    private Vector<String> Cgroup;
    private Vector<String> Agroup;
    private Vector<String> Dgroup;
    private Vector[] versparam;
    private double[] versparammean;
    private Vector[] Cparam;
    private double[] Cparammean;
    private Vector[] Aparam;
    private double[] Aparammean;
    private Vector[] Dparam;
    private double[] Dparammean;
    private int[] countCgroup, countDgroup, countAgroup;

    private String outdir, name;

    private double MISSINGVALUE = Double.NaN;
    private String DATAFILESUFFIX = ".xls";
    private String SUMMARYFILE = "_data";
    private String SDFILE = "_SD";
    private String VERSATILEFILE = "versatile";
    private String ACTINFILE = "actin";
    private String CONAFILE = "conA";
    private String DAPIFILE = "dapi";

    AverageData(String name, String outdir) {
        Cgroup = new Vector<>();
        Dgroup = new Vector<>();
        Agroup = new Vector<>();
        versparam = new Vector[26];
        Arrays.fill(versparam, new Vector());
        Cparam = new Vector[26];
        Arrays.fill(Cparam, new Vector());
        Aparam = new Vector[13];
        Arrays.fill(Aparam, new Vector());
        Dparam = new Vector[110];
        Arrays.fill(Dparam, new Vector());
        this.name = name;
        this.outdir = outdir;
    }

    void addCellData(CellImage image) {
        for (int i = 0; i < image.cell.length; i++) {
            Cgroup.add(image.cell[i].getCgroup());
            Dgroup.add(image.cell[i].getDgroup());
            Agroup.add(image.cell[i].getAgroup());
            for (int j = 0; j < versparam.length; j++) {
                versparam[j].add(image.cell[i].versparam[j]);
            }
            for (int j = 0; j < image.cell[i].Cbaseparam.length; j++) {
                Cparam[j].add(image.cell[i].Cbaseparam[j]);
            }
            for (int j = 0; j < image.cell[i].Cexpandparam.length; j++) {
                Cparam[image.cell[i].Cbaseparam.length + j].add(image.cell[i].Cexpandparam[j]);
            }
            if (image.calA) {
                for (int j = 0; j < image.cell[i].Abaseparam.length; j++) {
                    Aparam[j].add(image.cell[i].Abaseparam[j]);
                }
                for (int j = 0; j < image.cell[i].Aexpandparam.length; j++) {
                    Aparam[image.cell[i].Abaseparam.length + j].add(image.cell[i].Aexpandparam[j]);
                }
            }
            if (image.calD) {
                for (int j = 0; j < image.cell[i].Dbaseparam.length; j++) {
                    Dparam[j].add(image.cell[i].Dbaseparam[j]);
                }
                for (int j = 0; j < image.cell[i].Dexpandparam.length; j++) {
                    Dparam[image.cell[i].Dbaseparam.length + j].add(image.cell[i].Dexpandparam[j]);
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //data.xlsを出力
    //data.xls -> versatile.xls
    //////////////////////////////////////////////////////////////////////////////////////////
    void printDataXLS(boolean calA, boolean calD, boolean outsheet) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
                    outdir + "/" + VERSATILEFILE + DATAFILESUFFIX, true)));
            calParamMean(calA, calD);
            setCountGroup();
            pw.print(name + "\t");
            int Ctotal = (countCgroup[1] + countCgroup[2] + countCgroup[3] + countCgroup[4]);
            int Ctotal_budded = (countCgroup[2] + countCgroup[3] + countCgroup[4]);
            int Atotal_budded = (countAgroup[3] + countAgroup[4]
                    + countAgroup[5] + countAgroup[6]);
            int Atotal_unbudded = (countAgroup[1] + countAgroup[2]);
            int Dtotal = (countDgroup[1] + countDgroup[2] + countDgroup[3] + countDgroup[4]);
            int Dtotal_budded = (countDgroup[2] + countDgroup[3] + countDgroup[4]);
            pw.print(Ctotal + "\t");
            for (int i = 0; i < 8; i++) {
                pw.print(versparammean[i] + "\t");
            }
            // 1 2 3 4
            for (int i = 1; i <= 4; i++) {
                pw.print((double) countCgroup[i] / Ctotal + "\t");
            }
            for (int i = 8; i < 12; i++) {
                pw.print(versparammean[i] + "\t");
            }
            // 1 2 3 4 5 6 7
            for (int i = 1; i <= 7; i++) {
                pw.print((double) countAgroup[i] / Ctotal + "\t");
            }
            for (int i = 12; i < 26; i++) {
                pw.print(versparammean[i] + "\t");
            }
            // 1 2 3 4 5 6 7
            for (int i = 1; i <= 7; i++) {
                pw.print((double) countDgroup[i] / Ctotal + "\t");
            }
            pw.println();
            pw.flush();
            pw.close();

            if (outsheet) {
                pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir
                        + "/" + CONAFILE + SUMMARYFILE + DATAFILESUFFIX, true)));
                pw.print(name + "\t");
                for (int i = 0; i < 23; i++) {
                    pw.print(Cparammean[i] + "\t");
                }
                pw.print((double) countCgroup[1] / Ctotal + "\t");
                pw.print((double) countCgroup[2] / Ctotal + "\t");
                pw.print((double) countCgroup[3] / Ctotal + "\t");
                pw.print((double) countCgroup[4] / Ctotal + "\t");
                pw.print((double) countCgroup[2] / Ctotal_budded + "\t");
                pw.print((double) countCgroup[3] / Ctotal_budded + "\t");
                pw.print((double) countCgroup[4] / Ctotal_budded + "\t");
                for (int i = 23; i < 26; i++) {
                    pw.print(Cparammean[i] + "\t");
                }
                pw.println();
                pw.flush();
                pw.close();

                if (calA) {
                    pw = new PrintWriter(new BufferedWriter(new FileWriter(
                            outdir + "/" + ACTINFILE + SUMMARYFILE + DATAFILESUFFIX, true)));
                    pw.print(name + "\t");
                    for (int i = 0; i < 9; i++) {
                        pw.print(Aparammean[i] + "\t");
                    }
                    pw.print((double) countAgroup[1] / Ctotal + "\t");
                    pw.print((double) countAgroup[2] / Ctotal + "\t");
                    pw.print((double) countAgroup[3] / Ctotal + "\t");
                    pw.print((double) countAgroup[4] / Ctotal + "\t");
                    pw.print((double) countAgroup[5] / Ctotal + "\t");
                    pw.print((double) countAgroup[6] / Ctotal + "\t");
                    pw.print((double) (countAgroup[1] + countAgroup[5])
                            / Ctotal + "\t");
                    pw
                            .print((double) (countAgroup[2] + countAgroup[3] + countAgroup[4])
                                    / Ctotal + "\t");
                    pw.print((double) countAgroup[7] / Ctotal + "\t");
                    pw.print((double) countAgroup[1] / Atotal_unbudded + "\t");
                    pw.print((double) countAgroup[2] / Atotal_unbudded + "\t");
                    pw.print((double) countAgroup[3] / Atotal_budded + "\t");
                    pw.print((double) countAgroup[4] / Atotal_budded + "\t");
                    pw.print((double) countAgroup[5] / Atotal_budded + "\t");
                    pw.print((double) countAgroup[6] / Atotal_budded + "\t");
                    for (int i = 9; i < 13; i++) {
                        pw.print(Aparammean[i] + "\t");
                    }
                    pw.println();
                    pw.flush();
                    pw.close();
                }
                if (calD) {
                    pw = new PrintWriter(new BufferedWriter(new FileWriter(
                            outdir + "/" + DAPIFILE + SUMMARYFILE + DATAFILESUFFIX, true)));
                    pw.print(name + "\t");
                    for (int i = 0; i < 110; i++) {
                        pw.print(Dparammean[i] + "\t");
                    }
                    pw.print((double) countDgroup[1] / Ctotal + "\t");
                    pw.print((double) countDgroup[2] / Ctotal + "\t");
                    pw.print((double) countDgroup[3] / Ctotal + "\t");
                    pw.print((double) countDgroup[4] / Ctotal + "\t");
                    pw.print((double) countDgroup[5] / Ctotal + "\t");
                    pw.print((double) countDgroup[6] / Ctotal + "\t");
                    pw.print((double) countDgroup[7] / Ctotal + "\t");
                    pw.print((double) countDgroup[1] / (double) countCgroup[1]
                            + "\t");
                    pw.print((double) countDgroup[2] / Ctotal_budded + "\t");
                    pw.print((double) countDgroup[3] / Ctotal_budded + "\t");
                    pw.print((double) countDgroup[4] / Ctotal_budded + "\t");
                    pw.print((double) countDgroup[1] / Dtotal + "\t");
                    pw.print((double) countDgroup[2] / Dtotal + "\t");
                    pw.print((double) countDgroup[3] / Dtotal + "\t");
                    pw.print((double) countDgroup[4] / Dtotal + "\t");
                    pw.print((double) countDgroup[2] / Dtotal_budded + "\t");
                    pw.print((double) countDgroup[3] / Dtotal_budded + "\t");
                    pw.print((double) countDgroup[4] / Dtotal_budded + "\t");

                    pw.println();
                    pw.flush();
                    pw.close();
                }
            }
        } catch (Exception e) {
            System.err.println("AverageData.printDataXLS():" + e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //SDdata.xlsを出力
    //////////////////////////////////////////////////////////////////////////////////////////
    void printSDDataXLS(boolean calA, boolean calD, boolean outsheet) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
                    outdir + "/" + VERSATILEFILE + SDFILE + DATAFILESUFFIX, true)));
            pw.print(name + "\t");
            for (int i = 0; i < versparam.length - 1; i++) {
                pw.print(SD(versparam[i], versparammean[i]) + "\t"
                        + count(versparam[i]) + "\t");
            }
            pw.println(SD(versparam[versparam.length - 1],
                    versparammean[versparam.length - 1])
                    + "\t" + count(versparam[versparam.length - 1]));
            pw.flush();
            pw.close();

            if (outsheet) {
                pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir
                        + "/" + CONAFILE + SDFILE + DATAFILESUFFIX, true)));
                pw.print(name + "\t");
                for (int i = 0; i < Cparam.length - 1; i++) {
                    pw.print(SD(Cparam[i], Cparammean[i]) + "\t"
                            + count(Cparam[i]) + "\t");
                }
                pw.println(SD(Cparam[Cparam.length - 1],
                        Cparammean[Cparam.length - 1])
                        + "\t" + count(Cparam[Cparam.length - 1]));
                pw.flush();
                pw.close();

                if (calA) {
                    pw = new PrintWriter(new BufferedWriter(new FileWriter(
                            outdir + "/" + ACTINFILE + SDFILE + DATAFILESUFFIX, true)));
                    pw.print(name + "\t");
                    for (int i = 0; i < Aparam.length - 1; i++) {
                        pw.print(SD(Aparam[i], Aparammean[i]) + "\t"
                                + count(Aparam[i]) + "\t");
                    }
                    pw.println(SD(Aparam[Aparam.length - 1],
                            Aparammean[Aparam.length - 1])
                            + "\t" + count(Aparam[Aparam.length - 1]));
                    pw.flush();
                    pw.close();
                }
                if (calD) {
                    pw = new PrintWriter(new BufferedWriter(new FileWriter(
                            outdir + "/" + DAPIFILE + SDFILE + DATAFILESUFFIX, true)));
                    pw.print(name + "\t");
                    for (int i = 0; i < Dparam.length - 1; i++) {
                        pw.print(SD(Dparam[i], Dparammean[i]) + "\t"
                                + count(Dparam[i]) + "\t");
                    }
                    pw.println(SD(Dparam[Dparam.length - 1],
                            Dparammean[Dparam.length - 1])
                            + "\t" + count(Dparam[Dparam.length - 1]));
                    pw.flush();
                    pw.close();
                }
            }
        } catch (Exception e) {
            System.err.println("AverageData.printSDDataXLS():" + e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //平均値をセット
    //////////////////////////////////////////////////////////////////////////////////////////
    private void calParamMean(boolean calA, boolean calD) {
        versparammean = new double[versparam.length];
        for (int i = 0; i < versparam.length; i++) {
            versparammean[i] = mean(versparam[i]);
        }
        Cparammean = new double[Cparam.length];
        for (int i = 0; i < Cparam.length; i++) {
            Cparammean[i] = mean(Cparam[i]);
        }
        if (calA) {
            Aparammean = new double[Aparam.length];
            for (int i = 0; i < Aparam.length; i++) {
                Aparammean[i] = mean(Aparam[i]);
            }
        }
        if (calD) {
            Dparammean = new double[Dparam.length];
            for (int i = 0; i < Dparam.length; i++) {
                Dparammean[i] = mean(Dparam[i]);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //グループ分けの数をセット
    //////////////////////////////////////////////////////////////////////////////////////////
    private void setCountGroup() {
        countCgroup = new int[6];
        for (String item : Cgroup) {
            String g = item;
            switch (g) {
                case "complex":
                    countCgroup[0]++;
                    break;
                case "no":
                    countCgroup[1]++;
                    break;
                case "small":
                    countCgroup[2]++;
                    break;
                case "medium":
                    countCgroup[3]++;
                    break;
                case "large":
                    countCgroup[4]++;
                    break;
                default:
                    break;
            }
        }
        countDgroup = new int[8];
        for (String value : Dgroup) {
            String g = value;
            switch (g) {
                case "-":
                    countDgroup[0]++;
                    break;
                case "A":
                    countDgroup[1]++;
                    break;
                case "A1":
                    countDgroup[2]++;
                    break;
                case "B":
                    countDgroup[3]++;
                    break;
                case "C":
                    countDgroup[4]++;
                    break;
                case "D":
                    countDgroup[5]++;
                    break;
                case "E":
                    countDgroup[6]++;
                    break;
                case "F":
                    countDgroup[7]++;
                    break;
                default:
                    break;
            }
        }
        countAgroup = new int[8];
        for (String o : Agroup) {
            String g = o;
            switch (g) {
                case "-":
                    countAgroup[0]++;
                    break;
                case "A":
                    countAgroup[1]++;
                    break;
                case "B":
                    countAgroup[2]++;
                    break;
                case "api":
                    countAgroup[3]++;
                    break;
                case "iso":
                    countAgroup[4]++;
                    break;
                case "E":
                    countAgroup[5]++;
                    break;
                case "F":
                    countAgroup[6]++;
                    break;
                case "N":
                    countAgroup[7]++;
                    break;
                default:
                    break;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //データの個数
    //////////////////////////////////////////////////////////////////////////////////////////
    private int count(Vector v) {
        int count = 0;
        for (Object o : v) {
            double d = (Double) o;
            if (d != -1) {
                count++;
            }
        }
        return count;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //-1以外のデータの平均値
    //////////////////////////////////////////////////////////////////////////////////////////
    private double mean(Vector v) {
        double r = 0;
        int count = 0;
        for (Object o : v) {
            double d = (Double) o;
            if (d != -1) {
                r += d;
                count++;
            }
        }
        if (count <= 0) {
            return MISSINGVALUE;
        }
        return r / count;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //-1以外のデータの標準偏差(=sqrt(分散))
    //////////////////////////////////////////////////////////////////////////////////////////
    private double SD(Vector v, double mean) {
        double r = 0;
        int count = 0;
        for (Object o : v) {
            double d = (Double) o;
            if (d != -1) {
                r += (d - mean) * (d - mean);
                count++;
            }
        }
        if (count <= 0) {
            return MISSINGVALUE;
        }
        return Math.sqrt(r / count);
    }
}
//--------------------------------------
//$Log: AverageData.java,v $
//Revision 1.10  2004/09/06 14:25:08  sesejun
//*** empty log message ***
//
//Revision 1.9  2004/09/06 13:44:06  sesejun
//CalMorphのおそらく正しい1_0のソース
//
//Revision 1.8  2004/06/30 17:14:21  sesejun
//計算部分と出力部分の分離。
//エラーを吐く際の
//
//Revision 1.7  2004/06/25 01:42:05  sesejun
//CalMorph_version_1.0相当へ変更
//一部、変更が元にもどってしまっている可能性大
//気がついたら、再度変更してください。
//
//Revision 1.6  2004/06/23 08:08:11  nakatani
//SDdata.xls --> versatile_SD.xls に変更
//
//Revision 1.5  2004/06/23 07:58:12  nakatani
//Avoiding Divide by zero on method "SD"'
//
//Revision 1.4  2004/06/09 09:49:30  sesejun
//Avoiding Divide by zero on method 'mean'
//
//Revision 1.3  2004/06/01 12:41:03  nakatani
//ファイル名変更（data.xls-->versatile.xls, SDdata.xls-->versatile_SD.xls）
//