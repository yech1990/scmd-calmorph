//--------------------------------------
//SCMD Project
//
//RankSumTest.java 
//Since:  2004/09/08
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.stat.tests;

import java.util.Arrays;
import java.util.Collection;

import lab.cb.scmd.util.stat.DoNotFilterStrategy;
import lab.cb.scmd.util.stat.SampleFilteringStrategy;
import lab.cb.scmd.util.table.Cell;
import lab.cb.scmd.util.table.TableIterator;


public class RankSum {
    private Object[] _g1, _g2;
    private double g1ranksum = 0, g2ranksum = 0;
	protected SampleFilteringStrategy	_sampleFilteringStrategy	= new DoNotFilterStrategy();

	public RankSum(TableIterator g1, TableIterator g2) {
		this._g1 = (getFilteringStrategy().filter(g1)).toArray();
		this._g2 = (getFilteringStrategy().filter(g2)).toArray();
	}

	// Double ‚Ì Collection
    public RankSum(Collection g1, Collection g2) {
        this._g1 = g1.toArray();
        this._g2 = g2.toArray();
    }

	public SampleFilteringStrategy getFilteringStrategy() {
		return _sampleFilteringStrategy;
	}
    /**
     * @return
     */
    public double willcoxconR() {
        int g1size = _g1.length;
        int g2size = _g2.length;
        if( g1size < 10 || g2size < 10 ) {
            //TODO cannot approx. normal distribution
        }
        Arrays.sort(_g1, 
                new java.util.Comparator(){
            		public int compare(Object a, Object b){
            		    double avalue =((Double)a).doubleValue();
            		    double bvalue =((Double)b).doubleValue();
            		    if(avalue < bvalue ){
            		        return -1;
            		    }else if(avalue == bvalue){
            		        return 0;
            		    }
            		    return 1;
            		}
			});
        Arrays.sort(_g2,
                new java.util.Comparator(){
    				public int compare(Object a, Object b){
    				    double avalue =((Double)a).doubleValue();
    				    double bvalue =((Double)b).doubleValue();
    				    if(avalue < bvalue ){
    				        return -1;
    				    }else if(avalue == bvalue){
    				        return 0;
    				    }
    				    return 1;
    				}
			});

        computeRankSum(_g1, _g2);
        double rvalue = computeZvalue();
        return rvalue;
    }

    /**
     * @return
     */
    private double computeZvalue() {
        int g1size = _g1.length;
        int g2size = _g2.length;
        double mu = g1size * g2size / 2.0;
        double delta = Math.sqrt(g1size) * Math.sqrt(g2size) * Math.sqrt((g1size + g2size + 1) / 12.0);
        double uvalue = g1ranksum - g1size * ( g1size + 1) / 2.0 ;
        double u2value = g2ranksum - g2size * ( g2size + 1) / 2.0 ;
        double zvalue = ( uvalue - mu ) / delta;
        return zvalue;
    }

    private void computeRankSum(Object[] g1, Object[] g2) {
        int g1size = _g1.length;
        int g2size = _g2.length;
        int g1cursor = 0;
        int g2cursor = 0;
        double g1sum = 0.0;
        double g2sum = 0.0;
        double rank = 1;

        double g1value = ((Double)g1[g1cursor]).doubleValue(); 
        double g2value = ((Double)g2[g2cursor]).doubleValue(); 

        for( ; g1cursor < g1.length || g2cursor < g2.length; ) {
            if ( g2cursor == g2size || g1value < g2value ) {
                g1sum += rank;
                g1cursor++;
                if( g1cursor < g1.length )
                    g1value = ((Double)g1[g1cursor]).doubleValue();
                else
                    g1value = Double.MAX_VALUE;
                rank++;
            } else if ( g1cursor == g1size || g1value > g2value ){
                g2sum += rank;
                g2cursor++;
                if( g2cursor < g2.length )
                    g2value = ((Double)g2[g2cursor]).doubleValue();
                else
                    g2value = Double.MAX_VALUE;
                rank++;
            } else {
                int g1endcursor = g1cursor + 1;
                int g2endcursor = g2cursor + 1;
                while( g1endcursor < g1size && g1value == ((Double)g1[g1endcursor]).doubleValue())
                    g1endcursor++;
                while( g2endcursor < g2size && g2value == ((Double)g2[g2endcursor]).doubleValue())
                    g2endcursor++;
                int g1diff = g1endcursor - g1cursor;
                int g2diff = g2endcursor - g2cursor;
                double mediumRank = (g1diff + g2diff - 1)/2.0 + rank;
                g1sum += mediumRank * g1diff;
                g2sum += mediumRank * g2diff;
                g1cursor = g1endcursor;
                g2cursor = g2endcursor;
                if( g1cursor < g1.length )
                    g1value = ((Double)g1[g1cursor]).doubleValue();
                else
                    g1value = Double.MAX_VALUE;
                if( g2cursor < g2.length )
                    g2value = ((Double)g2[g2cursor]).doubleValue();
                else
                    g2value = Double.MAX_VALUE;
                rank += g1diff + g2diff; 
            }
        }
        this.g1ranksum = g1sum;
        this.g2ranksum = g2sum;
    }
    
}
