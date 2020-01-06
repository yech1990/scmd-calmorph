//--------------------------------------
//SCMD Project
//
//Sample.java 
//Since:  2004/09/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.morphologicalarray;

import java.io.PrintWriter;


/**
 * @author nakatani
 */
public class BoxCox {
    //0.0000118,0.000118 <-Grubbs test
    public static final double[] pValueList = {0.0000118, 0.000118, 0.01, 0.005, 0.001, 0.0005, 0.0002, 0.0001, 0.00005, 0.00001, 0.000005, 0.000001, 1E-10};
    //public static final double[] sigmaValueList={0.5,1,1.5,2,2.5,3};
    public static final double[] sigmaValueList = {2.3263, 3.0902, 3.7190, 4.2649, 4.7534, 5.1993, 5.6120, 5.9978};
    private double TINY = 1.0E-10;
    private Double[] originalData;
    private double bestP;
    private double bestA;
    private double maxLogLikelihood;
    private double transformedMean;
    private double transformedSD;

    public BoxCox(Double[] d) {
        originalData = d;
        getParamWhichMaximizeLogLikelihood();
        Double[] ESD = StatisticalTests.getEandSD(transformAll(bestP, bestA, originalData));
        transformedMean = ESD[0];
        transformedSD = ESD[1];
    }

    static public void printHeader(PrintWriter pw) {
        String t = "\t";
        pw.print("LogLikelihood" + t + "p" + t + "a" + t + "transformedMean" + t + "transformedSD" + t);
        printPValueListHeader(pw);
    }

    static public void printPValueListHeader(PrintWriter pw) {
        for (int i = 0; i < pValueList.length; ++i) {
            pw.print("(lower)" + pValueList[pValueList.length - 1 - i] + "\t");
        }
        for (double v : pValueList) {
            pw.print("(upper)" + v + "\t");
        }
    }

    static public void printHeaderSigma(PrintWriter pw) {
        String t = "\t";
        pw.print("LogLikelihood" + t + "p" + t + "a" + t + "transformedMean" + t + "transformedSD" + t);
        printPValueListHeaderSigma(pw);
    }

    static public void printPValueListHeaderSigma(PrintWriter pw) {
        for (int i = 0; i < sigmaValueList.length; ++i) {
            pw.print("(lower)" + sigmaValueList[sigmaValueList.length - 1 - i] + "\t");
        }
        for (double v : sigmaValueList) {
            pw.print("(upper)" + v + "\t");
        }
    }

    public Double[] getSTDTransformedData(Double[] data) {
        Double[] transformed = transformAll(bestP, bestA, data);
        for (int i = 0; i < transformed.length; ++i) {
            transformed[i] = (transformed[i] - transformedMean) / transformedSD;
        }
        return transformed;
    }

    public Double[] getTransformedData() {
        return transformAll(bestP, bestA, originalData);
    }
    //public static final double[] pValueList={0.01,0.001,0.0001,0.00001,0.000001};

    private void getParamWhichMaximizeLogLikelihood() {
        int partitionP = 1000;
        double minP = -5.0;
        double maxP = 5.0;
        int partitionA = 1000;
        double minA = 0;
        for (Double originalDatum : originalData) {
            if (minA > originalDatum) minA = originalDatum;
        }
        minA = -minA + TINY;
        double maxA = 100 + TINY;
        bestP = Double.MAX_VALUE;
        bestA = Double.MAX_VALUE;
        maxLogLikelihood = -Double.MAX_VALUE;

        //double maxLogLikelihood=-Double.MAX_VALUE;
        int direction = 1;
        try {
            if (logLikelihood(1 + (maxP - minP) / (double) partitionP, minA) > logLikelihood(1, minA)) {
            } else {
                direction = -1;
            }
        } catch (Exception e1) {
            System.err.println("ありえなーい！");
            System.exit(-1);
        }
        for (int j = 0; j <= partitionA; ++j) {
            //double maxLogLikelihood=-Double.MAX_VALUE;
            double thisA = minA + j * (maxA - minA) / (double) partitionA;
            for (int i = 0; i <= partitionP; ++i) {
                double thisP = 1 + direction * i * (maxP - minP) / (double) partitionP;
                if (thisP < minP || maxP < thisP) break;
                double thisLL = -Double.MAX_VALUE;
                try {
                    thisLL = logLikelihood(thisP, thisA);
                    //thisLL= -Math.abs(StatisticalTests.getSampleSkewness(transformAll(thisP,thisA,originalData)));
                } catch (Exception e) {//if thisLL==Infinity
                    continue;
                }
                if (maxLogLikelihood < thisLL) {
                    maxLogLikelihood = thisLL;
                    bestP = thisP;
                    bestA = thisA;
                }  //break;//LogLikelihood must? be increasing along the direction.

                //System.out.print(thisLL+",");break;
            }
            //System.out.println("},{");
//			if(bestP<=minP || bestP>=maxP){//if(! LogLikelihood maximized in [minP,maxP] )
//				continue;
//			}else return;
//			bestA = minA;
            return;
        }
        //System.exit(-1);//to draw sample graph
    }

    private double logLikelihood(double p, double a) throws Exception {
        Double[] transformedData = transformAll(p, a, originalData);
        Double[] ESD = StatisticalTests.getEandSD(transformedData);
        double value = -originalData.length / 2.0 * Math.log(2 * Math.PI * Math.pow(ESD[1], 2)) - originalData.length * (originalData.length - 1) / 2.0;
        for (Double originalDatum : originalData) {
            value += (p - 1) * Math.log(originalDatum + a);
        }
        if (Double.isInfinite(value)) {
            //System.err.println("Error. BoxCox.logLikelihood() Infnity.");
            //System.exit(-1);
            throw new Exception("Infinity");
        }
        return value;
    }

    private Double[] transformAll(double p, double a, Double[] d) {
        Double[] transformedData = new Double[d.length];
        for (int i = 0; i < d.length; ++i) {
            transformedData[i] = transform(p, a, d[i]);
        }
        return transformedData;
    }

    private Double transform(double p, double a, Double d) {
        //double a=TINY;
        double x = d + a;
        if (x <= 0) {
            System.err.println("Error: BoxCox.transform() x<=0");
            System.exit(-1);
        }
        double y = Double.MAX_VALUE;
        if (p == 0) {
            y = Math.log(x);
        } else {
            y = (Math.pow(x, p) - 1) / p;
        }
        if (Double.isNaN(y) || Double.isInfinite(y) || y <= -Double.MAX_VALUE || Double.MAX_VALUE <= y) {
            //throw new SCMDException("BoxCox.transform() p="+p+" y="+y+" x-a="+(x-a));
            System.err.println("BoxCox.transform() p=" + p + " y=" + y + " x-a=" + (x - a));
            System.exit(-1);
        }
        return y;
    }

    private Double reverseTransform(double p, double a, Double d) {
        //double a=TINY;
        double y = d;
        double x = Double.MAX_VALUE;
        if (y * p + 1 <= 0) {
            //System.out.println("Warning: BoxCox.reverseTransform() out of range. return -1.");
            if (p >= 0) x = -Double.MAX_VALUE / 2.0;
            else x = Double.MAX_VALUE / 2.0;
        } else if (p == 0) {
            x = Math.pow(Math.E, y) - a;
        } else {

            x = Math.pow(y * p + 1, 1 / p) - a;
        }
        if (Double.isInfinite(x) || x <= -Double.MAX_VALUE || Double.MAX_VALUE <= x || 1.7976931348623157E308 == x) {
            //throw new SCMDException("BoxCox.transform() p="+p+" y="+y+" x-a="+(x-a));
            System.out.println("BoxCox.reverseTransform() p=" + p + " y=" + y + " x-a=" + (x - a));
            System.exit(-1);
        }
        if (Double.isNaN(x)) {
            x = Double.MAX_VALUE;
        }
        return x;
    }

    public Double getOneSidedCriticalValueForP(double pvalue) {
        double y = StatisticalTests.HastingsApproximationForNormalDF_Reverse(transformedMean, transformedSD, pvalue);
        return reverseTransform(bestP, bestA, y);
    }

    public void print(PrintWriter pw) {
        String t = "\t";
        pw.print(maxLogLikelihood + t + bestP + t + bestA + t + transformedMean + t + transformedSD + t);

        for (int i = 0; i < pValueList.length; ++i) {
            pw.print(getOneSidedCriticalValueForP(pValueList[pValueList.length - 1 - i]) + t);
        }
        for (double v : pValueList) {
            pw.print(getOneSidedCriticalValueForP(1 - v) + t);
        }
    }

    public Double getOneSidedCriticalValueForMeanPlusDSigma(double dsigma) {
        return reverseTransform(bestP, bestA, transformedMean + dsigma * transformedSD);
    }

    public Double getOneSidedCriticalValueForMaxPlusSigma(double dsigma) {
        Double[] transformed = transformAll(bestP, bestA, originalData);
        double maxValue = -Double.MAX_VALUE;
        for (int i = 0; i < originalData.length; ++i) {
            if (maxValue < transformed[i]) {
                maxValue = transformed[i];
            }
        }
        return reverseTransform(bestP, bestA, maxValue + dsigma * transformedSD);
    }

    public Double getOneSidedCriticalValueForMinMinusSigma(double dsigma) {
        Double[] transformed = transformAll(bestP, bestA, originalData);
        double minValue = Double.MAX_VALUE;
        for (int i = 0; i < originalData.length; ++i) {
            if (minValue > transformed[i]) {
                minValue = transformed[i];
            }
        }
        return reverseTransform(bestP, bestA, minValue - dsigma * transformedSD);
    }

    public void printSigma(PrintWriter pw) {
        String t = "\t";
        pw.print(maxLogLikelihood + t + bestP + t + bestA + t + transformedMean + t + transformedSD + t);

        for (int i = 0; i < sigmaValueList.length; ++i) {
            pw.print(getOneSidedCriticalValueForMeanPlusDSigma(-sigmaValueList[sigmaValueList.length - 1 - i]) + t);
            //pw.print(getOneSidedCriticalValueForMinMinusSigma(sigmaValueList[sigmaValueList.length-1-i]).doubleValue()+t);
        }
        for (double v : sigmaValueList) {
            pw.print(getOneSidedCriticalValueForMeanPlusDSigma(v) + t);
            //pw.print(getOneSidedCriticalValueForMaxPlusSigma(sigmaValueList[i]).doubleValue()+t);
        }
    }

}


//--------------------------------------
//$Log: BoxCox.java,v $
//Revision 1.2  2004/12/01 16:20:15  nakamu
//データ中にマイナスがあっても良いように、minAを改良
//
//Revision 1.1  2004/12/01 08:14:34  nakatani
//BoxCoxPowerTransformation, -->normal p-MLE, a-0
//
//--------------------------------------
