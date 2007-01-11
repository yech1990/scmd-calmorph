package lab.cb.scmd.calmorph;

import java.util.Vector;

public class BudValidation {
	
	private static final double _bud_length_threshold = 1.5;
	private static final double _mother_length_threshold = 0.2;
	private static final double _perpendicular_length_threshold = 0.5;
	private static final double _circle_threshold = 6.0;
	
	public static Cell[] validation(Cell[] cells, final int width) {
		for ( int i = 0; i < cells.length; i++ ) {
			if ( cells[i].neck == null || cells[i].neck[0] == cells[i].neck[cells[i].neck.length - 1] || 
					cells[i].bud_edge.size() <= 0 ) { continue; }
			
			int bud_edge_length = cells[i].bud_edge.size();
			int mother_edge_length = cells[i].mother_edge.size();
			
			double neck_length = Math.sqrt( getDistance(cells[i].neck[0], cells[i].neck[cells[i].neck.length - 1], width) );
			int neck_middle = getMiddlePoint(cells[i].neck[0], cells[i].neck[cells[i].neck.length - 1], width);
			
			Line line = new Line();
			Line perpendicular = line.calculateLine(cells[i].neck[0], neck_middle, cells[i].neck[cells[i].neck.length - 1], width);
			int bud_middle = Line.findTheNearestPoint(perpendicular, cells[i].bud_edge, width);
			double bud_perpendicular_length = Math.sqrt( getDistance(neck_middle, bud_middle, width) );
			
			int bud_center = getMiddlePoint(neck_middle, bud_middle, width);
			//double[] max_min = calculateMaxAndMinDistances(bud_center, width, cells[i].bud_edge);
			setNeckAndBudMiddle(neck_middle, bud_middle, bud_center, cells, i);
			
			if ( motherLengthValidation(mother_edge_length, bud_edge_length) && 
					( !budPerpendicularLengthValidation(neck_length, bud_perpendicular_length) || 
					!budLengthValidation(neck_length, bud_edge_length) ) ) { deleteBudEdge(cells, i); }
		}
		return cells;
	}
	
	protected static void deleteBudEdge(Cell[] cells, int i) {
		cells[i].mother_edge = budEdgeConcatinatesMotherEdge(cells[i].mother_edge, cells[i].bud_edge);
		cells[i].bud_edge = new Vector();
		//cells[i].neck = null;
	}
	
	protected static void setNeckAndBudMiddle(int neck_middle, int bud_middle, int bud_center, Cell[] cells, int i) {
		cells[i].neck_and_bud_middle = new int[3];
		cells[i].neck_and_bud_middle[0] = neck_middle;
		cells[i].neck_and_bud_middle[1] = bud_middle;
		cells[i].neck_and_bud_middle[2] = bud_center;
	}
	
	protected static void setGradCeptMiddle(double gradient, double intercept, double middle, Cell[] cells, int i) {
		cells[i].grad_cept_middle = new double[3];
		cells[i].grad_cept_middle[0] = gradient;
		cells[i].grad_cept_middle[1] = intercept;
		cells[i].grad_cept_middle[2] = middle;
	}
	
	protected static double getDistance(int start, int end, int width) {
		int x1 = start % width;
		int y1 = start / width;
		int x2 = end % width;
		int y2 = end / width;
		return ( (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) );
	}
	
	protected static int getMiddlePoint(int start, int end, int width) {
		int x1 = start % width;
		int y1 = start / width;
		int x2 = end % width;
		int y2 = end / width;
		return (int)( (x1 + x2) / 2 + (y1 + y2) / 2 * width );
	}
	
	/**
	 * 妥当なbudか判定 1 （budの外周の長さ）
	 * @param neck_length
	 * @param bud_edge_length
	 * @return : 妥当なbud = true,  budではない = false
	 */
	protected static boolean budLengthValidation(double neck_length, double bud_edge_length) {
		if ( neck_length * _bud_length_threshold > bud_edge_length ) { return false; }
		return true;
	}
	
	/**
	 * 妥当なbudか判定 2 （motherの外周の長さ）
	 * @param mother_edge_length
	 * @param bud_edge_length
	 * @return
	 */
	protected static boolean motherLengthValidation(double mother_edge_length, double bud_edge_length) {
		if ( bud_edge_length > mother_edge_length * _mother_length_threshold ) { return true; }
		return false;
	}
	
	/**
	 * 妥当なbudか判定 3 （budの外周の中間点と budのnecksの中間点 間の長さ、が budのnecks間の長さ の閾値倍以下なら budでない。）
	 * @param neck_length
	 * @param bud_perpendicular_length
	 * @return
	 */
	protected static boolean budPerpendicularLengthValidation(double neck_length, double bud_perpendicular_length) {
		if ( neck_length * _perpendicular_length_threshold > bud_perpendicular_length ) { return false; }
		return true;
	}
	
	/**
	 * 妥当なbudか判定 4 （budの円形）
	 * @param max_min
	 * @return
	 */
	protected static boolean budCircleValidation(double[] max_min) {
		double max = max_min[0];
		double min = max_min[1];
		if ( max > _circle_threshold * min ) { return false; }
		return true;
	}
	
	protected static Vector budEdgeConcatinatesMotherEdge(Vector mother_edge, Vector bud_edge) {
		for ( int i = 0; i < bud_edge.size(); i++ ) { mother_edge.add((Integer)bud_edge.get(i)); }
		return mother_edge;
	}
	
	protected static double[] calculateMaxAndMinDistances(int center, int width, Vector edge) {
		double[] result = new double[2];
		
		double max = getDistance(center, ((Integer)edge.get(0)).intValue(), width);
		double min = max;
		
		for ( int i = 1; i < edge.size(); i++ ) {
			double distance = getDistance(center, ((Integer)edge.get(i)).intValue(), width);
			if      ( min > distance ) { min = distance; }
			else if ( max < distance ) { max = distance; }
		}
		
		result[0] = max;
		result[1] = min;
		return result;
	}
}
