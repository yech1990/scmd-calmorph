package lab.cb.scmd.calmorph;

import java.util.Vector;

public class NeckCorrection {
	
	private static final int _edge_corner_flag = Integer.MIN_VALUE;
	private static final int _unreached_flag = Integer.MAX_VALUE;
	private static final int _neck_check_size = 20;
	
	private boolean _corrected;
	private Vector _bud_edge;
	
	private double gradient;
	private double intercept;
	private double middle;
	
	public NeckCorrection() { }
	
	public NeckCorrection(boolean corrected, Vector bud_edge) {
		_corrected = corrected;
		_bud_edge = bud_edge;
	}
	
	public NeckCorrection(boolean corrected, Vector bud_edge, double gra, double inter, double mid) {
		_corrected = corrected;
		_bud_edge = bud_edge;
		gradient = gra;
		intercept = inter;
		middle = mid;
	}
	
	public boolean getCorrected() {
		return _corrected;
	}
	
	public Vector getBudEdge() {
		return _bud_edge;
	}
	
	public double getGradient() { return gradient; }
	public double getInterceps() { return intercept; }
	public double getMiddle() { return middle; }
	
	public static NeckCorrection executeNeckCorrection(final Cell cell, final int size, final int width) {
		Vector edge = cell.bud_edge;
		boolean[] thinned = mapEdgeToBoolean(edge, size, width);
		//boolean[] thinned = thinning(width, mapEdgeToBoolean(edge, size, width));
		// thinned = 全てのtrue点（黒点）は、隣接点数==1 or （隣接点数==2 and 連結成分数==2） であることを確認済み。
		int corner = findOneCornerPoint(thinned, ((Integer)edge.get(0)).intValue(), width);
		
		Vector before = resetEdge(thinned, width, corner);
		//Vector after = correctNecks(before, width);
		NeckCorrection af = correctNecks(before, width);
		//System.out.println(af.getGradient());
		//boolean change = false;
		//if ( before.size() != after.size() ) { change = true; }
		//return new NeckCorrection(false, after);
		return af;
		
		//return resetEdge(thinned, width, corner);
		/*
		for ( int p = 0; p < thinned.length; p++ ) {
			if ( thinned[p] ) {
				boolean[] pre = new boolean[9];
				pre[0] = thinned[ p + 1 ]; pre[1] = thinned[ p - width + 1 ];
				pre[2] = thinned[ p - width ]; pre[3] = thinned[ p - width - 1 ];
				pre[4] = thinned[ p - 1 ]; pre[5] = thinned[ p + width - 1 ];
				pre[6] = thinned[ p + width ]; pre[7] = thinned[ p + width + 1 ];
				int sum = 0;
				for ( int k = 0; k < 8; k++ ) {
					if ( pre[k] ) { sum++; }
				}
				if ( sum == 1 ) { continue; }
				if ( sum == 2 && connect(pre) == 2 ) { continue; }
				System.out.print(sum + "\t");
			}
		}
		System.out.println();*/
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
		
		int now = corner;
		int previous = -1;
		int next;
		
		while ( true ) {
			next = findNextEdge(edge_map, width, now, previous);
			if ( next == _edge_corner_flag ) { break; }
			if ( next == _unreached_flag ) { System.err.println("NeckCorrection.resetEdge() : NEXT not found.");  System.exit(1); }
			
			result.add((Integer)next);
			previous = now;
			now = next;
		}
		
		return result;
	}
	
	protected static int findNextEdge(final boolean[] edge_map, final int width, final int now, final int previous) {
		Vector nexts = new Vector();
		//0 1 2
		//3 * 4
		//5 6 7
		
		if ( isNextEdge(edge_map, previous, now - width - 1) ) { nexts.add((Integer)(now - width - 1)); }
		if ( isNextEdge(edge_map, previous, now - width) )     { nexts.add((Integer)(now - width)); }
		if ( isNextEdge(edge_map, previous, now - width + 1) ) { nexts.add((Integer)(now - width + 1)); }
		if ( isNextEdge(edge_map, previous, now - 1) )         { nexts.add((Integer)(now - 1)); }
		if ( isNextEdge(edge_map, previous, now + 1) )         { nexts.add((Integer)(now + 1)); }
		if ( isNextEdge(edge_map, previous, now + width - 1) ) { nexts.add((Integer)(now + width - 1)); }
		if ( isNextEdge(edge_map, previous, now + width) )     { nexts.add((Integer)(now + width)); }
		if ( isNextEdge(edge_map, previous, now + width + 1) ) { nexts.add((Integer)(now + width + 1)); }
		
		if ( nexts.size() == 0 ) { return _edge_corner_flag; }
		if ( nexts.size() != 1 ) { System.err.println("NeckCorrection.findNextEdge() : not 8-connectivity");  System.exit(1); }
		
		return ((Integer)nexts.get(0)).intValue();
	}
	
	protected static boolean isNextEdge(final boolean[] edge_map, final int previous, final int next) {
		//if ( next < 0 || edge_map.length <= next ) { System.out.println("NeckCorrection.isNextEdge() : " + next); return false; }
		if ( previous != next && edge_map[next] ) { return true; }
		return false;
	}
	
	protected static NeckCorrection correctNecks(final Vector edge, final int width) {
		Line ln = new Line();
		Line line = ln.calculateLine(((Integer)edge.get(0)).intValue(), ((Integer)edge.get(edge.size() - 1)).intValue(), width);
		int p = ((Integer)edge.get(edge.size() - 1)).intValue();
		
		//System.out.println("G = " + line.getGradient() + "  I = "+ line.getIntercept() + "  M = " + ((Integer)edge.get(0)).intValue());
		
		double distance = 0.0;
		int forward = 0;
		int backward = edge.size() - 1;
		
		for ( int i = 0; i < edge.size(); i++ ) {
			distance = Line.calculateLineDistance(line, ((Integer)edge.get(i)).intValue(), width);
			forward = i;
			if ( distance > 1.0 ) { break; }
		}
		
		for ( int i = edge.size() - 1; i >= 0; i-- ) {
			distance = Line.calculateLineDistance(line, ((Integer)edge.get(i)).intValue(), width);
			backward = i;
			if ( distance > 1.0 ) { break; }
		}
		
		Vector result = new Vector();
		if ( forward >= backward ) { System.err.println("NeckCorrection.correctNecks() : distance ERROR"); }
		for ( int i = forward; i <= backward; i++ ) { result.add((Integer)edge.get(i)); }
		
		return new NeckCorrection(false, edge, line.getGradient(), line.getIntercept(), p);
		
		/*
		int middle = edge.size() / 2;
		int last = edge.size() - 1;
		
		int for_all = calculateOuterProduct((Integer)edge.get(0), (Integer)edge.get(middle), (Integer)edge.get(last), width);
		int for_half = calculateOuterProduct((Integer)edge.get(0), (Integer)edge.get((int)(middle / 2)), (Integer)edge.get(middle), width);
		if ( for_all * for_half <= 0 ) { System.out.print("for" + "\t"); }
		
		int back_all = calculateOuterProduct((Integer)edge.get(last), (Integer)edge.get(middle), (Integer)edge.get(0), width);
		int back_half = calculateOuterProduct((Integer)edge.get(last), (Integer)edge.get(last - (int)(middle / 2)), (Integer)edge.get(middle), width);
		if ( back_all * back_half <= 0 ) { System.out.print("back" + "\t"); }
		
		System.out.println();
		
		
		int[] head = new int[_neck_check_size];
		for ( int i = 0; i < head.length; i++ ) { head[i] = ((Integer)edge.get(i)).intValue(); }
		int neck_of_head = correctNeck(head, width);
		
		int[] tail = new int[_neck_check_size];
		for ( int i = 0; i < tail.length; i++ ) { tail[i] = ((Integer)edge.get(edge.size() - i - 1)).intValue(); }
		int neck_of_tail = correctNeck(tail, width);
		*/
		//Vector result = new Vector();
		//for ( int i = neck_of_head; i < edge.size() - neck_of_tail; i++ ) { result.add((Integer)edge.get(i)); }
		//if ( result.size() != edge.size() ) { System.out.println("neck corrected"); }
		//return result;
	}
	// edge necks 間の直線との距離が 1 以下の点を bud edgeから除く
	protected static int correctNeck(final int[] edge, final int width) {
		// - * -    - - -
		// * - *    * - *
		// * - -    - * -    etc を見付ける
		boolean flag = false;
		boolean previous = false;    // plus == true,  minus == false
		int product;
		
		Vector curves = new Vector();
		
		for ( int i = 1; i < edge.length - 1; i++ ) {
			product = calculateOuterProduct(edge[i - 1], edge[i], edge[i + 1], width);
			if ( product >= 2 || product <= -2 ) { curves.add((Integer)i); }
			/*
			if ( !start_flag && product != 0 ) {
				if ( product > 0 ) { previous = true; }
				else               { previous = false;}
				start_flag = true;
			}
			if ( start_flag ) {
				if ( ( previous && product <= -2 )|| ( !previous && product >= 2 ) ) { return i; }
			}*/
		}
		int curve;
		if ( curves.size() == 0 ) { return 0; }
		else { curve = ((Integer)curves.get((int)(curves.size() / 2))).intValue(); }
		
		product = calculateOuterProduct(edge[curve - 1], edge[curve], edge[curve + 1], width);
		if ( product >= 2 ) { flag = true; }
		else if ( product <= -2 ) { flag = false; }
		else { System.err.println("NeckCorrection.correctNeck() : product ERROR");  System.exit(1); }
		
		if ( flag ) {
			
		} else {
			
		}
		
		return 0;
	}
	
	protected static int calculateOuterProduct(final int one, final int two, final int three, final int width) {
		int x1 = (two % width) - (one % width);
		int y1 = (two / width) - (one / width);
		int x2 = (three % width) - (one % width);
		int y2 = (three / width) - (one / width);
		
		return x1 * y2 - x2 * y1;
	}
	
	protected static boolean[] thinning(final int width, final boolean[] points) {
		int size = points.length;
		int height = size / width;
		
		boolean[] pre_image = new boolean[size];
		for ( int i = 0; i < size; i++ ) { pre_image[i] = points[i]; }
		
		boolean[] post_image = new boolean[size];
		for ( int i = 0; i < size; i++ ) { post_image[i] = points[i]; }
		
		int toggle = 1;
		int count = 0;
		
		while ( toggle != 0 ) {
			toggle = 0;
			count++;
			
			for ( int j = 1; j < height - 1; j++ ) {
				for ( int i = 1; i < width - 1; i++ ) {
				    int p = j * width + i;
					if ( !pre_image[p] ) { continue; }
					
					boolean[] pre = new boolean[9];
					boolean[] pos = new boolean[9];
					pre[0] = pre_image[ p + 1 ]; pre[1] = pre_image[ p - width + 1 ];
					pre[2] = pre_image[ p - width ]; pre[3] = pre_image[ p - width - 1 ];
					pre[4] = pre_image[ p - 1 ]; pre[5] = pre_image[ p + width - 1 ];
					pre[6] = pre_image[ p + width ]; pre[7] = pre_image[ p + width + 1 ];
					pos[0] = post_image[ p + 1 ]; pos[1] = post_image[ p - width + 1 ];
					pos[2] = post_image[ p - width ]; pos[3] = post_image[ p - width - 1 ];
					pos[4] = post_image[ p - 1 ]; pos[5] = post_image[ p + width - 1 ];
					pos[6] = post_image[ p + width ]; pos[7] = post_image[ p + width + 1 ];
					
					int sum = 0;
					for ( int k = 0; k < 8; k++ ) { if ( pre[k] ) { sum++; } }
					
					if ( sum == 0 ) { post_image[p] = false; }
					if ( sum >= 3 && sum <= 6 ) {
						if ( connect(pre) == 1 && connect(pos) == 1 ) {
							for ( int k = 1; k < 5; k++ ) { 
								if ( !pos[k] ) {
									boolean c = pre[k]; 
									pre[k] = false;
									if ( connect(pre) != 1 ) { pre[k] = c;  break; }
									pre[k] = c;
								}
								post_image[p] = false;
							}
						}
					}
					if ( post_image[p] == false ) { toggle++; }
				}
			}
			for ( int i = 0; i < size; i++ ) { pre_image[i] = post_image[i]; }
		}
		return post_image;
    }
    
    protected static int connect(boolean[] surroundingPoints) {
    	if ( surroundingPoints.length != 9 ) { return 9; }
		surroundingPoints[8] = surroundingPoints[0];
		int count = 0;
		for ( int i = 1; i < 9; i++ ) { if ( surroundingPoints[i] && !surroundingPoints[i - 1] ) count++; }
		return count;
    }
}
