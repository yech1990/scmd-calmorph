//--------------------------------------
//SCMD Project
//
//RankSumTest.java 
//Since:  2004/09/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.autoanalysis.validparameter;


import java.io.IOException;
import java.util.ArrayList;


public class RankSumTest {

    private int data_size1;
    private int data_size2;
    private java.util.Vector all = null;
    private double rank_sum1 = 0;
    private double rank_sum2 = 0;

    private double test_statistic_z;

    private ArrayList probTable;
    private int minRankSum;
    private static RankSumExactProbTable exact = new RankSumExactProbTable();

    //	public void readExactProbTable(String filename) throws IOException{
//		BufferedReader br=new BufferedReader(new FileReader(filename));
//		String buf=br.readLine();
//		if(buf==null){
//			System.err.println("no input");
//			System.exit(-1);
//		}
//		String[] line=buf.split("\t");
//		if(data_size1!=Integer.parseInt(line[0]) || data_size2!= Integer.parseInt(line[1])){
//			System.err.println("wrong exact probability table.");
//			System.exit(-1);
//		}
//		ArrayList table=new ArrayList();
//		buf=br.readLine();
//		minRankSum=data_size2*(data_size2+1)/2;
//		while((buf=br.readLine())!=null ){
//			line=buf.split("\t");
//			if(line.length<2)break;
//			table.add(new Double(Double.parseDouble(line[1])));
//		}
//		probTable=table;
//	}
    private double getExactProb(double ranksum) {
        if (ranksum < minRankSum) {
            //System.out.println("wrong ranksum. ranksum="+ranksum+" minRankSum="+minRankSum);
            return 0;
        }
        if (ranksum >= minRankSum + probTable.size()) {
            //System.out.println("wrong ranksum. ranksum="+ranksum+" maxRankSum="+(minRankSum+probTable.size()));
            return 1;
        }
        int r = (int) Math.floor(ranksum);
        return (double) (Double) probTable.get(r - minRankSum);
    }

    double getProb() {
        //String filename=new String("./table/table"+data_size1+"_"+data_size2+".xls");
        //readExactProbTable(filename);
        if (Math.floor(rank_sum2) == rank_sum2) {
            double a = getExactProb(rank_sum2);
            double b = 1 - getExactProb(rank_sum2 - 1);
            if (a < b) {
                return a;
            } else {
                return 1 - b;
            }
        } else {
            return getExactProb(rank_sum2);
        }
        //return StatisticalTests.HastingsApproximationForStandardNormalDF(get_value());
    }

    RankSumTest(java.util.Vector f, java.util.Vector g) throws IOException {
        data_size1 = f.size();
        data_size2 = g.size();
        if (data_size1 < 10 || data_size2 < 10) {
            //System.out.println("error. can not approx by normal.");
        }

        all = new java.util.Vector();//LabeledData[data_size1+data_size2];
        for (int i = 0; i < data_size1; ++i) {
            all.add(new LabeledData((Double) f.get(i), -1));
        }
        for (int i = 0; i < data_size2; ++i) {
            all.add(new LabeledData((Double) g.get(i), 1));
        }

        get_test_statistics();

        probTable = exact.getTable(data_size1, data_size2);
        minRankSum = data_size2 * (data_size2 + 1) / 2;
    }

    public double get_value() {
        return test_statistic_z;
    }

    private void get_test_statistics() {
        java.util.List list = new java.util.LinkedList(all);
        java.util.Collections.sort(list, new java.util.Comparator() {
            public int compare(Object a, Object b) {
                LabeledData A = (LabeledData) a;
                LabeledData B = (LabeledData) b;
                if (A.get_value() < B.get_value()) {
                    return -1;
                } else if (A.get_value() == B.get_value()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        all = new java.util.Vector(list);
        rank_sum1 = get_rank_sum(-1);
        rank_sum2 = get_rank_sum(1);
        if (rank_sum1 + rank_sum2 != (data_size1 + data_size2) * (data_size1 + data_size2 + 1) / 2) {
            System.out.println("Error. rank sum");
        }
        //System.out.println(rank_sum1+rank_sum2+"\t"+(data_size1+data_size2)*(data_size1+data_size2+1)/2);

        double test_statistic_U = rank_sum1 - data_size1 * (data_size1 + 1) / 2;
        test_statistic_z = get_normal_approx(test_statistic_U);
    }

    private double get_rank_sum(int label) {
        double tmp_rank_sum = 0;
        int partial_rank_sum = 0;
        int data_same_rank = 0;
        int num_of_target_data = 0;
        double previous_value = ((LabeledData) all.get(0)).get_value();
        for (int i = 0; i < all.size(); ++i) {
            if (((LabeledData) all.get(i)).get_value() != previous_value) {
                tmp_rank_sum += partial_rank_sum / (double) data_same_rank * num_of_target_data;
                partial_rank_sum = 0;
                data_same_rank = 0;
                num_of_target_data = 0;
            }
            partial_rank_sum += i + 1;
            data_same_rank++;
            if (((LabeledData) all.get(i)).get_label() == label) num_of_target_data++;
            previous_value = ((LabeledData) all.get(i)).get_value();
        }
        tmp_rank_sum += partial_rank_sum / (double) data_same_rank * num_of_target_data;
        return tmp_rank_sum;
    }

    private double get_normal_approx(double U) {
        double exp = data_size1 * data_size2 / 2.0;
        double stddev = Math.sqrt(data_size1 * data_size2 * (data_size1 + data_size2 + 1) / 12.0);
        return (U - exp) / stddev;
    }

    public double get_W() {
        return rank_sum1 - data_size1 * (data_size1 + 1) / 2;
    }
}
//--------------------------------------
//$Log: RankSumTest.java,v $
//Revision 1.15  2004/12/09 09:34:05  nakatani
//*** empty log message ***
//
//Revision 1.14  2004/12/09 06:49:58  nakatani
//*** empty log message ***
//
//Revision 1.13  2004/12/09 06:44:17  nakatani
//*** empty log message ***
//
//Revision 1.12  2004/12/09 05:49:58  nakatani
//*** empty log message ***
//
//Revision 1.11  2004/12/09 03:26:13  nakatani
//*** empty log message ***
//
//Revision 1.10  2004/12/09 00:32:31  nakatani
//*** empty log message ***
//
//Revision 1.9  2004/12/08 15:34:37  nakatani
//*** empty log message ***
//
//Revision 1.8  2004/12/08 13:41:10  nakatani
//*** empty log message ***
//
//Revision 1.7  2004/12/08 13:30:24  nakatani
//*** empty log message ***
//
//Revision 1.6  2004/12/08 07:47:01  nakatani
//*** empty log message ***
//
//Revision 1.5  2004/12/08 07:26:33  nakatani
//*** empty log message ***
//
//Revision 1.4  2004/12/08 07:21:24  nakatani
//*** empty log message ***
//
//Revision 1.3  2004/12/08 07:19:29  nakatani
//*** empty log message ***
//
//Revision 1.2  2004/12/08 06:58:34  nakatani
//exact prob
//
//--------------------------------------

