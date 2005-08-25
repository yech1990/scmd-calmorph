package lab.cb.scmd.util.analysis.clique;
import java.util.Arrays;

public class ArrayStats {
	
	public static void main(String[] args) {
		ArrayStats stat = new ArrayStats();
		double [] ary = {1.0, 2.0, 5.0, 100.0, 500.0,1000.0};
		System.out.println( "MAX:" + stat.max(ary) );
		System.out.println( "MIN:" + stat.min(ary) );
		System.out.println( "AVG:" + stat.avg(ary) );
		System.out.println( "VAR:" + stat.variance(ary) );
		System.out.println( "MEDIAN:" + stat.median(ary) );
	}

	public double avg(double [] ary) {
		double center = 0.0;
		int size = ary.length;
		int count = 0;
		for( int i = 0; i < size; i++ ) {
		    if( !Double.isNaN(ary[i]) ) {
		        count++;
			    center += ary[i];
		    }
		}
		return center / count;
	}

	public double variance(double [] ary) {
		int size = ary.length;
		int count = 0;
		double c = avg(ary);
		double var = 0.0;
		for( int i = 0; i < size; i++ ) {
		    if( !Double.isNaN(ary[i]) ) {
		        count++;
				var += (ary[i] - c) * (ary[i] - c);
		    }
		}
		if( count < 2 ) {
			return 0.0; 
			// unable to compute variance because the number of samples is small 
		}
		var = Math.sqrt(var / (count - 1));
				
		return var;
	}

	
	public double max(double [] ary) {
		int size = ary.length;
		double maxvalue = Double.MIN_VALUE;
		for( int i = 0; i < size; i++ ) {
			if( maxvalue < ary[i] ) {
				maxvalue = ary[i];
			}
		}
		return maxvalue;
	}

	
	public double min(double [] ary) {
		int size = ary.length;
		double minvalue = Double.MAX_VALUE;

		for( int i = 0; i < size; i++ ) {
			if( minvalue > ary[i] ) {
				minvalue = ary[i];
			}
		}
		return minvalue;
	}
	
	public double median(double [] ary) {
		int size = ary.length;
		if( size <= 2 ) {
			return avg(ary);
		}
		double [] tmpary = new double [size];
		for( int i = 0; i < size; i++ ) {
			tmpary[i] = ary[i];
		}
		
		Arrays.sort(tmpary); // sorry!
		if( size % 2 == 1 ) {
			return tmpary[size/2 ];  
		} else {
			return ( tmpary[size/2 - 1] + tmpary[size/2] ) / 2; 
		}
	}
}
