package lab.cb.scmd.calmorph;

import java.util.Vector;

public class BudValidation {
	
	private static final double _threshold = 2.0;
	
	public static Cell[] validation(Cell[] cells, final int width) {
		for ( int i = 0; i < cells.length; i++ ) {
			if ( cells[i].bud_edge.size() == 0 ) { continue; }
			int bud_edge_length = cells[i].bud_edge.size();
			int bud_start = ( (Integer)cells[i].bud_edge.get(0) ).intValue();
			int bud_end = ( (Integer)cells[i].bud_edge.get(bud_edge_length - 1) ).intValue();
			
			double neck_length = getDistance(bud_start, bud_end, width);
			if ( !budValidationCheck(neck_length, (double)bud_edge_length) ) {
				cells[i].mother_edge = budEdgeConcatinatesMotherEdge(cells[i].mother_edge, cells[i].bud_edge);
				cells[i].bud_edge = new Vector();
			}
		}
		return cells;
	}
	
	private static double getDistance(int start, int end, int width) {
		int x1 = start % width;
		int y1 = start / width;
		int x2 = end % width;
		int y2 = end / width;
		
		return Math.sqrt( (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) );
	}
	
	/**
	 * ‘Ã“–‚Èbud‚©”»’è
	 * @param neck_length
	 * @param bud_edge_length
	 * @return : ‘Ã“–‚Èbud = true,  bud‚Å‚Í‚È‚¢ = false
	 */
	private static boolean budValidationCheck(double neck_length, double bud_edge_length) {
		if ( neck_length * _threshold > bud_edge_length ) { return false; }
		return true;
	}
	
	private static Vector budEdgeConcatinatesMotherEdge(Vector mother_edge, Vector bud_edge) {
		for ( int i = 0; i < bud_edge.size(); i++ ) { mother_edge.add((Integer)bud_edge.get(i)); }
		return mother_edge;
	}
}
