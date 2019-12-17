//--------------------------------------
//SCMD Project
//
//Tests.java 
//Since:  2004/09/07
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------
package lab.cb.scmd.util.stat;

import lab.cb.scmd.util.stat.tests.RankSum;

import java.util.Collection;

public class TestsOfStatistics {
    Statistics _stat = new Statistics();

    public TestsOfStatistics() {
    }

    public TestsOfStatistics(StatisticsWithMissingValueSupport stat) {
        _stat = stat;
    }

    // 2群のrank sum test を行う
    public double rankSum(Collection g1, Collection g2) {
        RankSum rs = new RankSum(g1, g2);
        double x = rs.willcoxconR();
        return cdfOfNormalDistributionUsingHastingsApproximation(x);
    }

    // 正規分布に対する、累積密度を求める
    // mean: 正規分布の平均
    // sd:	 分散
    // x:	 累積する値
    // modified by sesejun based on nakatani's implementation
    public double cdfOfNormalDistribution(double x, double mean, double sd) {
        double cdf = cdfOfNormalDistributionUsingHastingsApproximation((x - mean) / sd);
        return cdf;
    }

    // 標準正規分布に対する累積密度をHastingsの近似を利用して求める
    private double cdfOfNormalDistributionUsingHastingsApproximation(double x) {
        boolean isMinus = false;
        if (x < 0) {
            x = -x;
            isMinus = true;
        }
        Double[] coefficients = new Double[]{
                new Double(0.0498673470),
                new Double(0.0211410061),
                new Double(0.0032776263),
                new Double(0.0000380036),
                new Double(0.0000488906),
                new Double(0.0000053830)
        };

        double sum = 1;
        double xpow = 1;
        for (int i = 0; i < 6; ++i) {
            xpow *= x;
            sum += coefficients[i].doubleValue() * xpow;
        }
        double returnValue = 1 - 0.5 * Math.pow(sum, -16);
        if (isMinus) {
            return 1 - returnValue;
        } else {
            return returnValue;
        }
    }

}
