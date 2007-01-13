package lab.cb.scmd.calmorph;

import java.util.Vector;

public class NeckCorrection {
	
	public NeckCorrection() {
		
	}
	
	public static void executeNeckCorrection(final Vector edge, final int size, final int width) {
		boolean[] thinned = thinning(mapEdgeToBoolean(edge, size, width), width);
		int corner = findOneCornerPoint(thinned, ((Integer)edge.get(0)).intValue(), width);
	}
	
	protected static boolean[] mapEdgeToBoolean(final Vector edge, final int size, final int width) {
		boolean[] result = new boolean[size];
		for ( int i = 0; i < edge.size(); i++ ) { result[((Integer)edge.get(i)).intValue()] = true; }
		return result;
	}
	
	protected static boolean[] thinning(boolean[] edge_map, final int width) {
		for ( int i = 0; i < edge_map.length; i++ ) {
			if ( edge_map[i] ) {
				
			}
		}
		return edge_map;
	}
	
	protected static int findOneCornerPoint(final boolean[] edge_map, final int start, final int width) {
		Vector corners = findCornerPoints(edge_map, width);
		if ( corners.size() != 2 ) { System.err.println("NeckCorrection.findCornerPoints() returns not 2 corners.");  System.exit(1); }
		return ((Integer)corners.get(0)).intValue();
	}
	
	protected static Vector findCornerPoints(final boolean[] edge_map, final int width) {
		Vector result = new Vector();
		for ( int i = 0; i < edge_map.length; i++ ) {
			if ( edge_map[i] ) {
				if ( cornerPoint(edge_map, i, width) ) { result.add((Integer)i); }
			}
		}
		return result;
	}
	
	protected static boolean cornerPoint(final boolean[] edge_map, final int i, final int width) {
		int counter = 0;
		
		if ( edge_map[i - width - 1] ) { counter++; }
		if ( edge_map[i - width] )     { counter++; }
		if ( edge_map[i - width + 1] ) { counter++; }
		if ( edge_map[i - 1] )         { counter++; }
		if ( edge_map[i + 1] )         { counter++; }
		if ( edge_map[i + width - 1] ) { counter++; }
		if ( edge_map[i + width] )     { counter++; }
		if ( edge_map[i + width + 1] ) { counter++; }
		
		if ( counter == 1 ) { return true; }
		else { return false; }
	}
	
	protected static Vector resetEdge(final boolean[] edge_map, final int width, final int corner) {
		Vector result = new Vector();
		return result;
	}
	
	protected static double calculateOuterProduct() {
		double result = 0.0;
		
		return result;
	}
}
