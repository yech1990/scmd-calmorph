package lab.cb.scmd.calmorph;

import java.util.Vector;

public class Line {
	
	// start　と end　を通る直線に垂直で、start と end の中点middle　を通る直線を求める。
	private double _gradient, _intercept;
	private final static double _vertical = Double.MAX_VALUE;
	
	public Line() { }
	
	public Line(double gradient, double intercept) {
		_gradient = gradient;
		_intercept = intercept;
	}
	
	public double getGradient() {
		return _gradient;
	}
	
	public double getIntercept() {
		return _intercept;
	}
	
	public Line calculateLine(int start, int middle, int end, int width) {
		int[] xy = calculateXY(start, end, width);
		double gradient = calculateGradient(xy[0], xy[1], xy[2], xy[3]);
		double intercept = calculateIntercept(gradient, middle, width);
		return new Line(gradient, intercept);
	}
	
	protected static int[] calculateXY(int start, int end, int width) {
		int[] result = new int[4];
		
		int x1 = start % width;
		int y1 = start / width;
		int x2 = end % width;
		int y2 = end / width;
		
		if ( x1 > x2 ) {
			result[0] = x2;
			result[1] = y2;
			result[2] = x1;
			result[3] = y1;
		} else {
			result[0] = x1;
			result[1] = y1;
			result[2] = x2;
			result[3] = y2;
		}
		return result;
	}
	
	protected static double calculateGradient(double x1, double y1, double x2, double y2) {
		if ( y1 == y2 ) { return _vertical; }
		return ( (x1 - x2) / (y2 - y1) );
	}
	
	protected static double calculateIntercept(double gradient, int middle, int width) {
		if ( gradient == _vertical ) { return (double)(middle % width); }  // 縦線の時は 切片 = middle_x
		return (double)( (middle / width) - gradient * (middle % width) );
	}
	
	protected static double calculateLineDistance(Line line, int p, int width) {
		double x = p % width;
		double y = p / width;
		double grad = line.getGradient();
		double cept = line.getIntercept();
		
		if ( grad == Double.MAX_VALUE ) { return cept * cept; }
		
		return ( (y - grad * x - cept) * (y - grad * x - cept) / (1 + grad * grad) );
	}
	
	public static int findTheNearestPoint(Line line, Vector edge, int width) {
		int result = ((Integer)edge.get(0)).intValue();
		double min = calculateLineDistance(line, ((Integer)edge.get(0)).intValue(), width);
		
		for ( int i = 1; i < edge.size(); i++ ) {
			double distance = calculateLineDistance(line, ((Integer)edge.get(i)).intValue(), width);
			if ( min > distance ) {
				min = distance;
				result = ((Integer)edge.get(i)).intValue();
			}
		}
		return result;
	}
}
