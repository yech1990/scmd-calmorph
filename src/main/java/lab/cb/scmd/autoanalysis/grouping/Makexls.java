//--------------------------------------
// SCMD Project
// 
// Makexls.java 
// Since:  2004/06/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.grouping;

/*
 * Created on 2003/07/14
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author sawai
 * <p>
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

import java.io.*;
import java.util.Arrays;
import java.util.Vector;

public class Makexls {
    String path;
    File dir;
    String[] mutants;
    Mutantprocess mp;

    public static void main(String[] s) {
        Makexls mx = new Makexls();
        mx.path = s[0];
        if (s.length == 1) {
            mx.dir = new File(s[0]);
        } else {
            System.err.println("invalid option");
            System.exit(1);
        }
        if (!mx.dir.exists()) {
            System.err.println(s[0] + " not exists");
            System.exit(1);
        }
        mx.mutants = mx.dir.list();
        mx.process();
    }

    public Makexls() {
    }

    public void process() {
        setPrintFile();
        Arrays.sort(mutants);
        for (int i = 0; i < mutants.length; i++) {
            File mutantxls = new File(path + "/" + mutants[i] + "/" + mutants[i] + ".xls");
            if (!mutantxls.exists()) continue;
            System.out.println(mutants[i]);
            System.out.flush();
            mp = new Mutantprocess(mutants[i]);
            mp.getcellData(mutants[i]);
            mp.printDataXLS();
            mp.printSDDataXLS();
        }
    }

    public void setPrintFile() {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/versatile.xls")));
            pw.print("name\tcell_without_complex\t");
            pw.print("mother_cell_size\tbud_cell_size\tbud_ratio\taxis_ratio_in_mother\taxis_ratio_in_bud\tbud_direction\t");
            pw.print("neck_position\tneck_width\tno_bud_ratio\tsmall_bud_ratio\tmedium_bud_ratio\tlarge_bud_ratio\t");
            pw.print("actin_region_ratio\tbud_actin_ratio\tmother_actin_gravity_point\tbud_actin_gravity_point\tactin_A_ratio\tactin_B_ratio\tactin_api_ratio\tactin_iso_ratio\tactin_E_ratio\tactin_F_ratio\tactin_N_ratio\t");
            pw.print("nuclear_number\tnuclear_size_in_mother\tnuclear_size_in_bud\tnuclear_size_in_cell\tnuclear_axis_ratio_in_mother\tnuclear_axis_ratio_in_bud\tnuclear_axis_ratio_in_cell\t");
            pw.print("hip_nuclear\tmother_cell_center_nuclear\tneck_nuclear_in_mother\tbud_top_nuclear\tbud_cell_center_nuclear\tneck_nuclear_in_bud\tlength_between_nucleus\t");
            pw.println("nuclear_A_ratio\tnuclear_A1_ratio\tnuclear_B_ratio\tnuclear_C_ratio\tnuclear_D_ratio\tnuclear_E_ratio\tnuclear_F_ratio\t");
            pw.flush();
            pw.close();

            pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/versatile_SD.xls")));
            pw.print("name\tmother_cell_size\t#\tbud_cell_size\t#\tbud_ratio\t#\taxis_ratio_in_mother\t#\taxis_ratio_in_bud\t#\tbud_direction\t#\t");
            pw.print("neck_position\t#\tneck_width\t#\t");
            pw.print("actin_region_ratio\t#\tbud_actin_ratio\t#\tmother_actin_gravity_point\t#\tbud_actin_gravity_point\t#\t");
            pw.print("nuclear_number\t#\tnuclear_size_in_mother\t#\tnuclear_size_in_bud\t#\tnuclear_size_in_cell\t#\tnuclear_axis_ratio_in_mother\t#\tnuclear_axis_ratio_in_bud\t#\tnuclear_axis_ratio_in_cell\t#\t");
            pw.println("hip_nuclear\t#\tmother_cell_center_nuclear\t#\tneck_nuclear_in_mother\t#\tbud_top_nuclear\t#\tbud_cell_center_nuclear\t#\tneck_nuclear_in_bud\t#\tlength_between_nucleus\t#\t");
            pw.flush();
            pw.close();

            pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/conA_data.xls")));
            pw.print("name\tC11-1\tC11-2\tC12-1\tC12-2\tC13\t");
            for (int i = 101; i <= 128; i++) pw.print("C" + i + "\t");
            pw.println();
            pw.flush();
            pw.close();

            pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/conA_SD.xls")));
            pw.print("name\tC11-1\t#\tC11-2\t#\tC12-1\t#\tC12-2\t#\tC13\t#\t");
            for (int i = 101; i <= 118; i++) pw.print("C" + i + "\t#\t");
            pw.print("C126\t#\tC127\t#\tC128\t#\t");
            pw.println();
            pw.flush();
            pw.close();

            pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/actin_data.xls")));
            pw.print("name\tA7-1\tA7-2\tA8-1\tA8-2\tA9\t");
            for (int i = 101; i <= 123; i++) pw.print("A" + i + "\t");
            pw.println();
            pw.flush();
            pw.close();

            pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/actin_SD.xls")));
            pw.print("name\tA7-1\t#\tA7-2\t#\tA8-1\t#\tA8-2\t#\tA9\t#\t");
            pw.print("A101\t#\tA102\t#\tA103\t#\tA104\t#\tA120\t#\tA121\t#\tA122\t#\tA123\t#\t");
            pw.println();
            pw.flush();
            pw.close();

            pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/dapi_data.xls")));
            pw.print("name\tD14-1\tD14-2\tD14-3\tD15-1\tD15-2\tD15-3\tD16-1\tD16-2\tD16-3\tD17-1\tD17-2\tD17-3\t");
            for (int i = 101; i <= 216; i++) pw.print("D" + i + "\t");
            pw.println();
            pw.flush();
            pw.close();

            pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/dapi_SD.xls")));
            pw.print("name\tD14-1\t#\tD14-2\t#\tD14-3\t#\tD15-1\t#\tD15-2\t#\tD15-3\t#\tD16-1\t#\tD16-2\t#\tD16-3\t#\tD17-1\t#\tD17-2\t#\tD17-3\t#\t");
            for (int i = 101; i <= 198; i++) pw.print("D" + i + "\t#\t");
            pw.println();
            pw.flush();
            pw.close();
        } catch (Exception e) {
            System.err.println("AllProcess.setPrintFile():" + e);
        }
    }

    public class Mutantprocess {
        Vector Cgroup, Agroup, Dgroup;
        Vector[] versparam;
        double[] versparammean;
        Vector[] Cparam;
        double[] Cparammean;
        Vector[] Aparam;
        double[] Aparammean;
        Vector[] Dparam;
        double[] Dparammean;
        int[] countCgroup, countDgroup, countAgroup;
        String mutant;

        public Mutantprocess(String mutant) {
            this.mutant = mutant;
            Cgroup = new Vector();
            Dgroup = new Vector();
            Agroup = new Vector();
            versparam = new Vector[26];
            for (int i = 0; i < versparam.length; i++) {
                versparam[i] = new Vector();
            }
            Cparam = new Vector[26];
            for (int i = 0; i < Cparam.length; i++) {
                Cparam[i] = new Vector();
            }
            Aparam = new Vector[13];
            for (int i = 0; i < Aparam.length; i++) {
                Aparam[i] = new Vector();
            }
            Dparam = new Vector[110];
            for (int i = 0; i < Dparam.length; i++) {
                Dparam[i] = new Vector();
            }
        }

        public void getcellData(String mutant) {
            int ch;
            String data;
            try {
                BufferedReader fr = new BufferedReader(new FileReader(path + "/" + mutant + "/" + mutant + ".xls"));
                while ((char) (ch = fr.read()) != '\n') ;
                while ((ch = fr.read()) != -1) {
                    for (int j = 0; j < 2; j++) {
                        while ((char) (ch = fr.read()) != '\t') ;
                    }
                    data = "";
                    while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                    Cgroup.add(data);
                    for (int j = 0; j < 8; j++) {
                        data = "";
                        while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                        versparam[j].add(new Double(data));
                    }
                    data = "";
                    while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                    Agroup.add(data);
                    for (int j = 8; j < 12; j++) {
                        data = "";
                        while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                        versparam[j].add(new Double(data));
                    }
                    data = "";
                    while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                    Dgroup.add(data);
                    for (int j = 12; j < 26; j++) {
                        data = "";
                        while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                        versparam[j].add(new Double(data));
                    }
                    while ((char) (ch = fr.read()) != '\n') ;
                }
                fr.close();

                fr = new BufferedReader(new FileReader(path + "/" + mutant + "/" + mutant + "_conA_basic.xls"));
                while ((char) (ch = fr.read()) != '\n') ;
                while ((ch = fr.read()) != -1) {
                    for (int j = 0; j < 18; j++) {
                        while ((char) (ch = fr.read()) != '\t') ;
                    }
                    for (int j = 0; j < 5; j++) {
                        data = "";
                        while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                        Cparam[j].add(new Double(data));
                    }
                    while ((char) (ch = fr.read()) != '\n') ;
                }
                fr.close();

                fr = new BufferedReader(new FileReader(path + "/" + mutant + "/" + mutant + "_conA_biological.xls"));
                while ((char) (ch = fr.read()) != '\n') ;
                while ((ch = fr.read()) != -1) {
                    for (int j = 0; j < 3; j++) {
                        while ((char) (ch = fr.read()) != '\t') ;
                    }
                    for (int j = 0; j < 21; j++) {
                        data = "";
                        while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                        Cparam[j + 5].add(new Double(data));
                    }
                    while ((char) (ch = fr.read()) != '\n') ;
                }
                fr.close();

                fr = new BufferedReader(new FileReader(path + "/" + mutant + "/" + mutant + "_actin_basic.xls"));
                while ((char) (ch = fr.read()) != '\n') ;
                while ((ch = fr.read()) != -1) {
                    for (int j = 0; j < 19; j++) {
                        while ((char) (ch = fr.read()) != '\t') ;
                    }
                    for (int j = 0; j < 5; j++) {
                        data = "";
                        while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                        Aparam[j].add(new Double(data));
                    }
                    while ((char) (ch = fr.read()) != '\n') ;
                }
                fr.close();

                fr = new BufferedReader(new FileReader(path + "/" + mutant + "/" + mutant + "_actin_biological.xls"));
                while ((char) (ch = fr.read()) != '\n') ;
                while ((ch = fr.read()) != -1) {
                    for (int j = 0; j < 4; j++) {
                        while ((char) (ch = fr.read()) != '\t') ;
                    }
                    for (int j = 0; j < 8; j++) {
                        data = "";
                        while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                        Aparam[j + 5].add(new Double(data));
                    }
                    while ((char) (ch = fr.read()) != '\n') ;
                }
                fr.close();

                fr = new BufferedReader(new FileReader(path + "/" + mutant + "/" + mutant + "_dapi_basic.xls"));
                while ((char) (ch = fr.read()) != '\n') ;
                while ((ch = fr.read()) != -1) {
                    for (int j = 0; j < 35; j++) {
                        while ((char) (ch = fr.read()) != '\t') ;
                    }
                    for (int j = 0; j < 12; j++) {
                        data = "";
                        while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                        Dparam[j].add(new Double(data));
                    }
                    while ((char) (ch = fr.read()) != '\n') ;
                }
                fr.close();

                fr = new BufferedReader(new FileReader(path + "/" + mutant + "/" + mutant + "_dapi_biological.xls"));
                while ((char) (ch = fr.read()) != '\n') ;
                while ((ch = fr.read()) != -1) {
                    for (int j = 0; j < 4; j++) {
                        while ((char) (ch = fr.read()) != '\t') ;
                    }
                    for (int j = 0; j < 98; j++) {
                        data = "";
                        while ((char) (ch = fr.read()) != '\t') data += (char) ch;
                        Dparam[j + 12].add(new Double(data));
                    }
                    while ((char) (ch = fr.read()) != '\n') ;
                }
                fr.close();
            } catch (Exception e) {
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //data.xlsを出力
        //////////////////////////////////////////////////////////////////////////////////////////
        public void printDataXLS() {
            try {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/versatile.xls", true)));
                calParamMean();
                setCountGroup();
                pw.print(mutant + "\t");
                int Ctotal = (countCgroup[1] + countCgroup[2] + countCgroup[3] + countCgroup[4]);
                int Ctotal_budded = (countCgroup[2] + countCgroup[3] + countCgroup[4]);
                int Atotal_budded = (countAgroup[3] + countAgroup[4] + countAgroup[5] + countAgroup[6]);
                int Atotal_unbudded = (countAgroup[1] + countAgroup[2]);
                int Dtotal = (countDgroup[1] + countDgroup[2] + countDgroup[3] + countDgroup[4]);
                int Dtotal_budded = (countDgroup[2] + countDgroup[3] + countDgroup[4]);
                pw.print(Ctotal + "\t");
                for (int i = 0; i < 8; i++) {
                    pw.print(versparammean[i] + "\t");
                }
                pw.print((double) countCgroup[1] / Ctotal + "\t");
                pw.print((double) countCgroup[2] / Ctotal + "\t");
                pw.print((double) countCgroup[3] / Ctotal + "\t");
                pw.print((double) countCgroup[4] / Ctotal + "\t");
                for (int i = 8; i < 12; i++) {
                    pw.print(versparammean[i] + "\t");
                }
                pw.print((double) countAgroup[1] / Ctotal + "\t");
                pw.print((double) countAgroup[2] / Ctotal + "\t");
                pw.print((double) countAgroup[3] / Ctotal + "\t");
                pw.print((double) countAgroup[4] / Ctotal + "\t");
                pw.print((double) countAgroup[5] / Ctotal + "\t");
                pw.print((double) countAgroup[6] / Ctotal + "\t");
                pw.print((double) countAgroup[7] / Ctotal + "\t");
                for (int i = 12; i < 26; i++) {
                    pw.print(versparammean[i] + "\t");
                }
                pw.print((double) countDgroup[1] / Ctotal + "\t");
                pw.print((double) countDgroup[2] / Ctotal + "\t");
                pw.print((double) countDgroup[3] / Ctotal + "\t");
                pw.print((double) countDgroup[4] / Ctotal + "\t");
                pw.print((double) countDgroup[5] / Ctotal + "\t");
                pw.print((double) countDgroup[6] / Ctotal + "\t");
                pw.println((double) countDgroup[7] / Ctotal + "\t");
                pw.flush();
                pw.close();

                pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/conA_data.xls", true)));
                pw.print(mutant + "\t");
                for (int i = 0; i < 23; i++) pw.print(Cparammean[i] + "\t");
                pw.print((double) countCgroup[1] / Ctotal + "\t");
                pw.print((double) countCgroup[2] / Ctotal + "\t");
                pw.print((double) countCgroup[3] / Ctotal + "\t");
                pw.print((double) countCgroup[4] / Ctotal + "\t");
                pw.print((double) countCgroup[2] / Ctotal_budded + "\t");
                pw.print((double) countCgroup[3] / Ctotal_budded + "\t");
                pw.print((double) countCgroup[4] / Ctotal_budded + "\t");
                for (int i = 23; i < 26; i++) pw.print(Cparammean[i] + "\t");
                pw.println();
                pw.flush();
                pw.close();

                pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/actin_data.xls", true)));
                pw.print(mutant + "\t");
                for (int i = 0; i < 9; i++) pw.print(Aparammean[i] + "\t");
                pw.print((double) countAgroup[1] / Ctotal + "\t");
                pw.print((double) countAgroup[2] / Ctotal + "\t");
                pw.print((double) countAgroup[3] / Ctotal + "\t");
                pw.print((double) countAgroup[4] / Ctotal + "\t");
                pw.print((double) countAgroup[5] / Ctotal + "\t");
                pw.print((double) countAgroup[6] / Ctotal + "\t");
                pw.print((double) (countAgroup[1] + countAgroup[5]) / Ctotal + "\t");
                pw.print((double) (countAgroup[2] + countAgroup[3] + countAgroup[4]) / Ctotal + "\t");
                pw.print((double) countAgroup[7] / Ctotal + "\t");
                pw.print((double) countAgroup[1] / Atotal_unbudded + "\t");
                pw.print((double) countAgroup[2] / Atotal_unbudded + "\t");
                pw.print((double) countAgroup[3] / Atotal_budded + "\t");
                pw.print((double) countAgroup[4] / Atotal_budded + "\t");
                pw.print((double) countAgroup[5] / Atotal_budded + "\t");
                pw.print((double) countAgroup[6] / Atotal_budded + "\t");
                for (int i = 9; i < 13; i++) pw.print(Aparammean[i] + "\t");
                pw.println();
                pw.flush();
                pw.close();

                pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/dapi_data.xls", true)));
                pw.print(mutant + "\t");
                for (int i = 0; i < 110; i++) pw.print(Dparammean[i] + "\t");
                pw.print((double) countDgroup[1] / Ctotal + "\t");
                pw.print((double) countDgroup[2] / Ctotal + "\t");
                pw.print((double) countDgroup[3] / Ctotal + "\t");
                pw.print((double) countDgroup[4] / Ctotal + "\t");
                pw.print((double) countDgroup[5] / Ctotal + "\t");
                pw.print((double) countDgroup[6] / Ctotal + "\t");
                pw.print((double) countDgroup[7] / Ctotal + "\t");
                pw.print((double) countDgroup[1] / (double) countCgroup[1] + "\t");
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
            } catch (Exception e) {
                System.err.println("AverageData.printDataXLS():" + e);
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //SDdata.xlsを出力
        //////////////////////////////////////////////////////////////////////////////////////////
        public void printSDDataXLS() {
            try {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/versatile_SD.xls", true)));
                pw.print(mutant + "\t");
                for (int i = 0; i < versparam.length - 1; i++) {
                    pw.print(SD(versparam[i], versparammean[i]) + "\t" + count(versparam[i]) + "\t");
                }
                pw.println(SD(versparam[versparam.length - 1], versparammean[versparam.length - 1]) + "\t" + count(versparam[versparam.length - 1]));
                pw.flush();
                pw.close();

                pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/conA_SD.xls", true)));
                pw.print(mutant + "\t");
                for (int i = 0; i < Cparam.length - 1; i++) {
                    pw.print(SD(Cparam[i], Cparammean[i]) + "\t" + count(Cparam[i]) + "\t");
                }
                pw.println(SD(Cparam[Cparam.length - 1], Cparammean[Cparam.length - 1]) + "\t" + count(Cparam[Cparam.length - 1]));
                pw.flush();
                pw.close();

                pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/actin_SD.xls", true)));
                pw.print(mutant + "\t");
                for (int i = 0; i < Aparam.length - 1; i++) {
                    pw.print(SD(Aparam[i], Aparammean[i]) + "\t" + count(Aparam[i]) + "\t");
                }
                pw.println(SD(Aparam[Aparam.length - 1], Aparammean[Aparam.length - 1]) + "\t" + count(Aparam[Aparam.length - 1]));
                pw.flush();
                pw.close();
                pw = new PrintWriter(new BufferedWriter(new FileWriter(path + "/dapi_SD.xls", true)));
                pw.print(mutant + "\t");
                for (int i = 0; i < Dparam.length - 1; i++) {
                    pw.print(SD(Dparam[i], Dparammean[i]) + "\t" + count(Dparam[i]) + "\t");
                }
                pw.println(SD(Dparam[Dparam.length - 1], Dparammean[Dparam.length - 1]) + "\t" + count(Dparam[Dparam.length - 1]));
                pw.flush();
                pw.close();
            } catch (Exception e) {
                System.err.println("AverageData.printSDDataXLS():" + e);
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //平均値をセット
        //////////////////////////////////////////////////////////////////////////////////////////
        public void calParamMean() {
            versparammean = new double[versparam.length];
            for (int i = 0; i < versparam.length; i++) {
                versparammean[i] = mean(versparam[i]);
            }
            Cparammean = new double[Cparam.length];
            for (int i = 0; i < Cparam.length; i++) {
                Cparammean[i] = mean(Cparam[i]);
            }
            Aparammean = new double[Aparam.length];
            for (int i = 0; i < Aparam.length; i++) {
                Aparammean[i] = mean(Aparam[i]);
            }
            Dparammean = new double[Dparam.length];
            for (int i = 0; i < Dparam.length; i++) {
                Dparammean[i] = mean(Dparam[i]);
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //グループ分けの数をセット
        //////////////////////////////////////////////////////////////////////////////////////////
        public void setCountGroup() {
            int err = 0;
            countCgroup = new int[6];
            for (int i = 0; i < Cgroup.size(); i++) {
                String g = (String) Cgroup.get(i);
                if (g.equals("complex")) countCgroup[0]++;
                else if (g.equals("no")) countCgroup[1]++;
                else if (g.equals("small")) countCgroup[2]++;
                else if (g.equals("medium")) countCgroup[3]++;
                else if (g.equals("large")) countCgroup[4]++;
                else err++;
            }
            countDgroup = new int[8];
            for (int i = 0; i < Dgroup.size(); i++) {
                String g = (String) Dgroup.get(i);
                if (g.equals("-")) countDgroup[0]++;
                else if (g.equals("A")) countDgroup[1]++;
                else if (g.equals("A1")) countDgroup[2]++;
                else if (g.equals("B")) countDgroup[3]++;
                else if (g.equals("C")) countDgroup[4]++;
                else if (g.equals("D")) countDgroup[5]++;
                else if (g.equals("E")) countDgroup[6]++;
                else if (g.equals("F")) countDgroup[7]++;
                else err++;
            }
            countAgroup = new int[8];
            for (int i = 0; i < Agroup.size(); i++) {
                String g = (String) Agroup.get(i);
                if (g.equals("-")) countAgroup[0]++;
                else if (g.equals("A")) countAgroup[1]++;
                else if (g.equals("B")) countAgroup[2]++;
                else if (g.equals("api")) countAgroup[3]++;
                else if (g.equals("iso")) countAgroup[4]++;
                else if (g.equals("E")) countAgroup[5]++;
                else if (g.equals("F")) countAgroup[6]++;
                else if (g.equals("N")) countAgroup[7]++;
                else err++;
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //データの個数
        //////////////////////////////////////////////////////////////////////////////////////////
        public int count(Vector v) {
            int count = 0;
            for (int i = 0; i < v.size(); i++) {
                double d = ((Double) v.get(i)).doubleValue();
                if (d != -1) {
                    count++;
                }
            }
            return count;
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //-1以外のデータの平均値
        //////////////////////////////////////////////////////////////////////////////////////////
        public double mean(Vector v) {
            double r = 0;
            int count = 0;
            for (int i = 0; i < v.size(); i++) {
                double d = ((Double) v.get(i)).doubleValue();
                if (d != -1) {
                    r += d;
                    count++;
                }
            }
            return r / count;
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //-1以外のデータの標準偏差(=sqrt(分散))
        //////////////////////////////////////////////////////////////////////////////////////////
        public double SD(Vector v, double mean) {
            double r = 0;
            int count = 0;
            for (int i = 0; i < v.size(); i++) {
                double d = ((Double) v.get(i)).doubleValue();
                if (d != -1) {
                    r += (d - mean) * (d - mean);
                    count++;
                }
            }
            return Math.sqrt(r / count);
        }
    }
}


//--------------------------------------
// $Log: Makexls.java,v $
// Revision 1.4  2004/10/15 01:14:02  sesejun
// Add Wilcoxon Rank Sum Test
//
// Revision 1.3  2004/08/27 15:36:25  sesejun
// *** empty log message ***
//
// Revision 1.2  2004/08/27 15:25:29  sesejun
// Calmorphのファイル名変更にしたがった変更
//
// Revision 1.1  2004/06/01 08:59:24  leo
// 澤井君のコードをそのままプロジェクトに入れました
//
//--------------------------------------