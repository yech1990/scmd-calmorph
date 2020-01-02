//--------------------------------------
// SCMD Project
// 
// StatisticalTests.java 
// Since:  2004/09/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.morphologicalarray;

import lab.cb.scmd.exception.SCMDException;


/**
 * @author nakatani
 */
public class StatisticalTests {

    StatisticalTests() {
    }

    static public double max(Double[] data) {
        double max = data[0];
        for (int i = 1; i < data.length; ++i) {
            double d = data[i];
            if (d > max) max = d;
        }
        return max;
    }

    static public double min(Double[] data) {
        double min = data[0];
        for (int i = 1; i < data.length; ++i) {
            double d = data[i];
            if (d < min) min = d;
        }
        return min;
    }

    /**
     * 平均値と標準偏差を計算する。
     *
     * @author nakatani
     */
    static public Double[] getEandSD(Double[] data) {
        if (data.length < 2) {
            System.err.println("Error in StatisticalTests.getEandSD(). data.length==" + data.length);
            System.exit(-1);
        }
        double tmp_exp = 0;
        double sum_of_squares = 0;
        int n = 0;
        for (int i = 0; i < data.length; ++i) {
            double x = data[i];
            x -= tmp_exp;
            tmp_exp += x / (double) (i + 1);
            sum_of_squares += i * x * x / (double) (i + 1);
        }
        sum_of_squares = Math.sqrt(sum_of_squares / (data.length - 1));

        Double[] EandSD = new Double[2];
        EandSD[0] = tmp_exp;//expectation
        EandSD[1] = sum_of_squares;//standard deviation
        return EandSD;
    }

    /**
     * 幾何平均。
     *
     * @author nakatani
     */
    static public double getGeometricMean(Double[] data) {
        double mean = 1;
        for (Double datum : data) {
            mean *= datum;
            //System.out.println(data[i].doubleValue()+"\t");
        }
        return Math.pow(mean, 1 / (double) data.length);
    }

    /**
     * (x-sample_mean)/sample_sdによって標準化されたデータを返す。
     *
     * @author nakatani
     */
    static public Double[] getStandardizedData(Double[] data) {
        Double[] standard = new Double[data.length];
        Double[] ESD = getEandSD(data);
        if (ESD[1] == 0) return data;
        for (int i = 0; i < data.length; ++i) {
            standard[i] = (data[i] - ESD[0]) / ESD[1];
        }
        return standard;
    }

    static public double getSampleSkewness(Double[] data) throws SCMDException {
        Double[] ESD = getEandSD(data);
        double sampleMean = ESD[0];
        if (ESD[1] == 0) {
            return 0.0;
        }
        double a = 0, b = 0;
        for (double d : data) {
            a += (d - sampleMean) * (d - sampleMean) * (d - sampleMean);
            b += (d - sampleMean) * (d - sampleMean);
        }
        double result = Math.sqrt(data.length) * a / Math.sqrt(b * b * b);
        if (Double.isNaN(result)) {
            throw new SCMDException("Error. StatisticalTests.getSampleSkewness() returned NaN. data.length=" + data.length + " a=" + a + " b=" + b + " sampleMean=" + sampleMean);
            /***
             System.err.println("Error. StatisticalTests.getSampleSkewness() returned NaN. data.length="+data.length+" a="+a+" b="+b+" sampleMean="+sampleMean);
             for(int i=0;i<data.length;++i){
             System.err.println(data[i].doubleValue());
             }
             System.exit(-1);
             ***/
        }
        return result;
    }

    static public double getSampleKurtosis(Double[] data) throws SCMDException {
        Double[] ESD = getEandSD(data);
        double sampleMean = ESD[0];
        if (ESD[1] == 0) {
            return 0.0;
        }
        double a = 0, b = 0;
        for (double d : data) {
            a += (d - sampleMean) * (d - sampleMean) * (d - sampleMean) * (d - sampleMean);
            b += (d - sampleMean) * (d - sampleMean);
        }
        double result = data.length * a / b * b;
        if (Double.isNaN(result)) {
            throw new SCMDException("Error. StatisticalTests.getSampleKurtosis() returned NaN. data.length=" + data.length + " a=" + a + " b=" + b);
            /***
             System.err.println("Error. StatisticalTests.getSampleKurtosis() returned NaN. data.length="+data.length+" a="+a+" b="+b);
             for(int i=0;i<data.length;++i){
             System.err.println(data[i].doubleValue());
             }
             System.exit(-1);
             ***/
        }
        return result;
    }

    static public double percentileRankFromLower(Double[] data, double x) {
        int count_bigger = 0;
        int count_same = 0;
        for (double d : data) {
            if (d > x) ++count_bigger;
            if (d == x) ++count_same;
        }
        return 100 * (count_bigger + count_same / 2.0) / (double) data.length;
    }

    static public Integer[] getHistogram(Double[] data, double min, double max, int n_box) {
        double intervalLength = (max - min) / (double) n_box;
        Integer[] histogram = new Integer[n_box + 2];
        for (int i = 0; i < n_box + 2; ++i) {
            histogram[i] = 0;
        }
        for (double d : data) {
            int boxNum = (int) Math.floor((d - min) / intervalLength);
            if (d < min) boxNum = 0;
            else if (d >= max) boxNum = n_box + 1;
            else boxNum++;
            int count = histogram[boxNum];
            histogram[boxNum] = count + 1;
        }
        return histogram;
    }

    static final int numberOfPartitions = 20;

    static public double getMode(Double[] data) {
        Double[] ESD = getEandSD(data);
        double E = ESD[0];
        double SD = ESD[1];
        double min = E - 3 * SD;
        double max = E + 3 * SD;
        double partitionLength = (max - min) / (double) numberOfPartitions;
        Integer[] histogram = getHistogram(data, min, max, numberOfPartitions);
        int max_data = 0;
        double mode = 0;
        for (int i = 1; i < numberOfPartitions; ++i) {
            int num_of_data = histogram[i];
            if (num_of_data > max_data) {
                max_data = num_of_data;
                mode = min + ((i - 1) + 0.5) * partitionLength;
            }
        }
        if (mode < 0) mode = 0;
        return mode;
    }

    static public double getMode(Double[] data, double min, double max) {
        double partitionLength = (max - min) / (double) numberOfPartitions;
        Integer[] histogram = getHistogram(data, min, max, numberOfPartitions);
        int max_data = 0;
        double mode = 0;
        for (int i = 1; i < numberOfPartitions; ++i) {
            int num_of_data = histogram[i];
            if (num_of_data > max_data) {
                max_data = num_of_data;
                mode = min + ((i - 1) + 0.5) * partitionLength;
            }
        }
        return mode;
    }


    static public double HastingsApproximationForStandardNormalDF(double x) {
        boolean isMinus = false;
        if (x < 0) {
            x = -x;
            isMinus = true;
        }
        Double[] coefficients = new Double[6];
        coefficients[0] = 0.0498673470;
        coefficients[1] = 0.0211410061;
        coefficients[2] = 0.0032776263;
        coefficients[3] = 0.0000380036;
        coefficients[4] = 0.0000488906;
        coefficients[5] = 0.0000053830;

        double sum = 1;
        double xpow = 1;
        for (int i = 0; i < 6; ++i) {
            xpow *= x;
            sum += coefficients[i] * xpow;
        }
        double returnValue = 1 - 0.5 * Math.pow(sum, -16);
        if (isMinus) {
            return 1 - returnValue;
        } else {
            return returnValue;
        }
    }

    static public double HastingsApproximationForNormalDF(double mean, double sd, double x) {
        return HastingsApproximationForStandardNormalDF((x - mean) / sd);
    }

    static public double HastingsApproximationForStandardNormalDF_Reverse(double p) {
        if (p <= 0 || 1 <= p) {
            System.err.println("Error in StatisticalTests.HastingsApproximationForReverseStandardNormalDF(" + p + ") assert(0<x<1)");
            System.exit(-1);
        }
        int sign;
        if (p <= 0.5) {
            sign = -1;
        } else {
            p = 1 - p;
            sign = 1;
        }
        double[] coeff_a = new double[3];
        double[] coeff_b = new double[3];
        coeff_a[0] = 2.515517;
        coeff_a[1] = 0.802853;
        coeff_a[2] = 0.010328;
        coeff_b[0] = 1.432788;
        coeff_b[1] = 0.189269;
        coeff_b[2] = 0.001308;

        double sum_a = 0, sum_b = 1;
        double z = Math.sqrt(-2 * Math.log(1 - p));
        double returnValue = z - (coeff_a[0] + coeff_a[1] * z + coeff_a[2] * z * z) / (1 + coeff_b[0] * z + coeff_b[1] * z * z + coeff_b[2] * z * z * z);
        return -sign * returnValue;
    }

    static public double HastingsApproximationForNormalDF_Reverse(double mean, double sd, double p) {
        double x = HastingsApproximationForStandardNormalDF_Reverse(p);
        return sd * x + mean;
    }

    public static final double standardNormalUpper6 = 4.7534;//upper 0.000001
    public static final double standardNormalUpper5 = 4.2649;//upper 0.00001
    public static final double standardNormalUpper4 = 3.7190;//upper 0.0001
    public static final double standardNormalUpper3 = 3.0902;//upper 0.001
    public static final double standardNormalUpper2 = 2.3263;//upper 0.01

    //reference http://www.itl.nist.gov/div898/handbook/eda/section3/eda3674.htm
    static final double[] ChiSquareTableUpper0_1 = {
            -1,
            2.706,
            4.605,
            6.251,
            7.779,
            9.236,
            10.645,
            12.017,
            13.362,
            14.684,
            15.987,
            17.275,
            18.549,
            19.812,
            21.064,
            22.307,
            23.542,
            24.769,
            25.989,
            27.204,
            28.412
    };
    static final double[] ChiSquareTableUpper0_01 = {
            -1,
            6.635,
            9.210,
            11.345,
            13.277,
            15.086,
            16.812,
            18.475,
            20.090,
            21.666,
            23.209,
            24.725,
            26.217,
            27.688,
            29.141,
            30.578,
            32.000,
            33.409,
            34.805,
            36.191,
            37.565
    };
    static final double[] ChiSquareTableUpper0_001 = {
            -1,
            10.828,
            13.816,
            16.266,
            18.467,
            20.515,
            22.458,
            24.322,
            26.125,
            27.877,
            29.588,
            31.264,
            32.910,
            34.528,
            36.123,
            37.697,
            39.252,
            40.790,
            42.312,
            43.820,
            45.315
    };

    static public double upperCriticalValueOfChiSquareDistribution(int degreeOfFreedom, double prob) {
        if (degreeOfFreedom < 1 || 20 < degreeOfFreedom) {
            System.err.println("Error in Statistics.upperCriticalValueOfChiSquareDistribution(): no critical value table for degree of freedom =" + degreeOfFreedom);
            System.exit(-1);
        }
        double returnValue = -1;
        if (prob == 0.1) {
            returnValue = ChiSquareTableUpper0_1[degreeOfFreedom];
        } else if (prob == 0.01) {
            returnValue = ChiSquareTableUpper0_01[degreeOfFreedom];
        } else if (prob == 0.001) {
            returnValue = ChiSquareTableUpper0_001[degreeOfFreedom];
        } else {
            System.err.println("Error in Statistics.upperCriticalValueOfChiSquareDistribution(): no critical value table for prob=" + prob);
            System.exit(-1);
        }
        return returnValue;
    }

    public static void main(String[] args) {
        for (int i = 1; i < 10; ++i) {
            //double d=HastingsApproximationForStandardNormalDF_Reverse(i/10.0);
            //System.out.println(i/10.0+"\t"+d);
            double d = HastingsApproximationForStandardNormalDF_Reverse(Math.pow(10, -i));
            System.out.println(Math.pow(10, -i) + "\t" + d);
        }
    }
}


//--------------------------------------
// $Log: StatisticalTests.java,v $
// Revision 1.5  2004/12/01 08:16:32  nakatani
// BoxCox 細かい修正。
//
// Revision 1.4  2004/09/18 23:10:18  nakatani
// *** empty log message ***
//
// Revision 1.3  2004/09/03 06:02:09  nakatani
// *** empty log message ***
//
// Revision 1.2  2004/09/01 06:27:56  nakatani
// *** empty log message ***
//
// Revision 1.1  2004/09/01 00:41:09  nakatani
// *** empty log message ***
//
//--------------------------------------