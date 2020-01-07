package lab.cb.scmd.util.analysis.clique;

import java.util.Arrays;

public class ArrayStats {

    public static void main(String[] args) {
        ArrayStats stat = new ArrayStats();
        double[] ary = {1.0, 2.0, 5.0, 100.0, 500.0, 1000.0};
        System.out.println("MAX:" + stat.max(ary));
        System.out.println("MIN:" + stat.min(ary));
        System.out.println("AVG:" + stat.avg(ary));
        System.out.println("VAR:" + stat.variance(ary));
        System.out.println("MEDIAN:" + stat.median(ary));
    }

    double avg(double[] ary) {
        double center = 0.0;
        int size = ary.length;
        int count = 0;
        for (double v : ary) {
            if (!Double.isNaN(v)) {
                count++;
                center += v;
            }
        }
        return center / count;
    }

    public double variance(double[] ary) {
        int size = ary.length;
        int count = 0;
        double c = avg(ary);
        double var = 0.0;
        for (double v : ary) {
            if (!Double.isNaN(v)) {
                count++;
                var += (v - c) * (v - c);
            }
        }
        if (count < 2) {
            return 0.0;
            // unable to compute variance because the number of samples is small
        }
        var = Math.sqrt(var / (count - 1));

        return var;
    }


    public double max(double[] ary) {
        int size = ary.length;
        double maxvalue = Double.MIN_VALUE;
        for (double v : ary) {
            if (maxvalue < v) {
                maxvalue = v;
            }
        }
        return maxvalue;
    }


    public double min(double[] ary) {
        int size = ary.length;
        double minvalue = Double.MAX_VALUE;

        for (double v : ary) {
            if (minvalue > v) {
                minvalue = v;
            }
        }
        return minvalue;
    }

    private double median(double[] ary) {
        int size = ary.length;
        if (size <= 2) {
            return avg(ary);
        }
        double[] tmpary = new double[size];
        System.arraycopy(ary, 0, tmpary, 0, size);

        Arrays.sort(tmpary); // sorry!
        if (size % 2 == 1) {
            return tmpary[size / 2];
        } else {
            return (tmpary[size / 2 - 1] + tmpary[size / 2]) / 2;
        }
    }
}
