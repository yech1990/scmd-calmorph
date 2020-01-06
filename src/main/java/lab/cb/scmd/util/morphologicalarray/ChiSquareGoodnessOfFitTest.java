//--------------------------------------
// SCMD Project
// 
// ChiSquareGoodnessOfFitTest.java 
// Since:  2004/09/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.morphologicalarray;

/**
 * @author nakatani
 */
public class ChiSquareGoodnessOfFitTest {

    private static final double[] partition_le30 = {-Double.MAX_VALUE, -1.0, 0, 1.0, Double.MAX_VALUE};
    private static final double[] partition_g30 = {-Double.MAX_VALUE, -1.2, -0.4, 0.4, 1.2, Double.MAX_VALUE};
    private static final double[] partition_g50 = {-Double.MAX_VALUE, -1.5, -0.75, 0, 0.75, 1.5, Double.MAX_VALUE};
    private static final double[] partition_g75 = {-Double.MAX_VALUE, -1.8, -1.2, -0.6, 0, 0.6, 1.2, 1.8, Double.MAX_VALUE};
    private static final double[] partition_g120 = {-Double.MAX_VALUE, -2.0, -1.5, -1.0, -0.5, 0, 0.5, 1.0, 1.5, 2.0, Double.MAX_VALUE};
    private static final double[] prob_le30 = {0.159, 0.341, 0.341, 0.159};
    private static final double[] prob_g30 = {0.115, 0.230, 0.311, 0.230, 0.115};
    private static final double[] prob_g50 = {0.067, 0.160, 0.273, 0.273, 0.160, 0.067};
    private static final double[] prob_g75 = {0.036, 0.079, 0.159, 0.226, 0.226, 0.159, 0.079, 0.036};
    private static final double[] prob_g120 = {0.023, 0.044, 0.092, 0.150, 0.191, 0.191, 0.150, 0.092, 0.044, 0.023};
    private Double[] standardizedData;
    private int[] sampleFrequency;
    private double[] expectedFrequency;
    private double[] partition;
    private double[] prob;
    private int degreeOfFreedom;
    public ChiSquareGoodnessOfFitTest(Double[] originalData) {
        makePartition(originalData.length);
        standardizedData = StatisticalTests.getStandardizedData(originalData);
        sampleFrequency = getSampleFrequency(standardizedData);
        expectedFrequency = getExpectedFrequency(standardizedData.length);
    }

    private void printStatus() {
        for (int i = 0; i < sampleFrequency.length; ++i) {
            System.out.print(partition[i - 1] + "--" + partition[i]);
            System.out.print(" sample=" + sampleFrequency[i]);
            System.out.println(" expected=" + expectedFrequency[i]);
        }

        System.out.print(" statistics=" + getStatistics());
        System.out.println(" significance=" + isSignificant(0.01));
    }

    private void makePartition(int data_size) {
        if (data_size > 120) {
            partition = partition_g120;
            prob = prob_g120;
        } else if (data_size > 75) {
            partition = partition_g75;
            prob = prob_g75;
        } else if (data_size > 50) {
            partition = partition_g50;
            prob = prob_g50;
        } else if (data_size > 30) {
            partition = partition_g30;
            prob = prob_g30;
        } else {
            partition = partition_le30;
            prob = prob_le30;
        }
        degreeOfFreedom = partition.length - 3;
    }

    private int inWhichInterval(double d) {
        for (int i = 1; i < partition.length; ++i) {
            //System.out.println(d+"("+partition[i-1]+","+partition[i]+")");
            if (partition[i - 1] < d && d <= partition[i]) {
                return i - 1;
            }
        }
        System.err.println("ChiSquareGOFTest.inWhichInterval() strange value=" + d);
        return Integer.MAX_VALUE;
    }

    private int[] getSampleFrequency(Double[] data) {
        int[] freq = new int[partition.length - 1];
        for (int i = 0; i < freq.length; ++i) {
            freq[i] = 0;
        }
        for (int i = 0; i < data.length; ++i) {
            ++freq[inWhichInterval(data[i].doubleValue())];
        }
        return freq;
    }

    private double[] getExpectedFrequency(int sampleSize) {
        double[] freq = new double[partition.length - 1];
        for (int i = 0; i < freq.length; ++i) {
            freq[i] = sampleSize * prob[i];
        }
        return freq;
    }

    double getStatistics() {
        double statistics = 0;
        for (int i = 0; i < sampleFrequency.length; ++i) {
            statistics += (sampleFrequency[i] - expectedFrequency[i]) * (sampleFrequency[i] - expectedFrequency[i]) / expectedFrequency[i];
        }
        if (Double.MIN_VALUE < statistics && statistics < Double.MAX_VALUE) {
        } else {
            printStatus();
        }
        return statistics;
    }

    public boolean isSignificant(double pvalue) {
        if (pvalue != 0.1 && pvalue != 0.01 && pvalue != 0.001) {
            System.err.println("Error. ChiSquareGoodnessOfFitTest.isSignificant() pvalue!=0.01&&pvalue!=0.001");
            System.exit(-1);
        }
        if (degreeOfFreedom > 0) {
            return getStatistics() >= StatisticalTests.upperCriticalValueOfChiSquareDistribution(degreeOfFreedom, pvalue);
        } else {
            return true;
        }
    }
}


//--------------------------------------
// $Log: ChiSquareGoodnessOfFitTest.java,v $
// Revision 1.3  2004/09/18 23:10:18  nakatani
// *** empty log message ***
//
// Revision 1.2  2004/09/03 06:02:09  nakatani
// *** empty log message ***
//
// Revision 1.1  2004/09/01 06:17:10  nakatani
// *** empty log message ***
//
// Revision 1.1  2004/09/01 03:31:06  nakatani
// *** empty log message ***
//
//--------------------------------------