package lab.cb.scmd.calmorph;

import java.util.Vector;

import junit.framework.TestCase;

public class LineTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testCalculateXY() {
		int[] xy = Line.calculateXY(2, 46, 10);
		assertEquals(2, xy[0]);
		assertEquals(0, xy[1]);
		assertEquals(6, xy[2]);
		assertEquals(4, xy[3]);
		
		xy = Line.calculateXY(42, 6, 10);
		assertEquals(2, xy[0]);
		assertEquals(4, xy[1]);
		assertEquals(6, xy[2]);
		assertEquals(0, xy[3]);
		
		xy = Line.calculateXY(6, 42, 10);
		assertEquals(2, xy[0]);
		assertEquals(4, xy[1]);
		assertEquals(6, xy[2]);
		assertEquals(0, xy[3]);
		
		xy = Line.calculateXY(46, 2, 10);
		assertEquals(2, xy[0]);
		assertEquals(0, xy[1]);
		assertEquals(6, xy[2]);
		assertEquals(4, xy[3]);
	}

	public void testCalculateGradient() {
		int[] xy = Line.calculateXY(2, 46, 10);
		assertEquals(-1.0, Line.calculatePerpendicularGradient(xy[0], xy[1], xy[2], xy[3]));
		
		xy = Line.calculateXY(42, 6, 10);
		assertEquals(1.0, Line.calculatePerpendicularGradient(xy[0], xy[1], xy[2], xy[3]));
		
		xy = Line.calculateXY(6, 42, 10);
		assertEquals(1.0, Line.calculatePerpendicularGradient(xy[0], xy[1], xy[2], xy[3]));
		
		xy = Line.calculateXY(46, 2, 10);
		assertEquals(-1.0, Line.calculatePerpendicularGradient(xy[0], xy[1], xy[2], xy[3]));
		
		xy = Line.calculateXY(5, 35, 10);
		assertEquals(0.0, Line.calculatePerpendicularGradient(xy[0], xy[1], xy[2], xy[3]));
		
		xy = Line.calculateXY(32, 35, 10);
		assertEquals(Double.MAX_VALUE, Line.calculatePerpendicularGradient(xy[0], xy[1], xy[2], xy[3]));
	}

	public void testCalculateIntercept() {
		assertEquals(4.0, Line.calculateIntercept(Double.MAX_VALUE, 24, 10));
		assertEquals(2.0, Line.calculateIntercept(0.0, 24, 10));
		assertEquals(6.0, Line.calculateIntercept(-1.0, 24, 10));
		assertEquals(-2.0, Line.calculateIntercept(1.0, 24, 10));
	}

	public void testCalculateLineDistance() {
		double distance = (2.0 - 4.0) * (2.0 - 4.0);
		double calculate = Line.calculateLineDistance(new Line(Double.MAX_VALUE, 4.0), 2, 10);
		System.out.println(distance + " == " + calculate);
		assertEquals(distance, calculate);
		
		distance = 2.0 * 2.0;
		calculate = Line.calculateLineDistance(new Line(0.0, 2.0), 0, 10);
		System.out.println(distance + " == " + calculate);
		assertEquals(distance, calculate);
		
		distance = (6.0 / Math.sqrt(2.0)) * (6.0 / Math.sqrt(2.0));
		calculate = Line.calculateLineDistance(new Line(-1.0, 6.0), 0, 10);
		System.out.println(distance + " == " + calculate);
		assertEquals((int)(distance + 0.5), (int)(calculate + 0.5));
		
		distance = (2.0 / Math.sqrt(2.0)) * (2.0 / Math.sqrt(2.0));
		calculate = Line.calculateLineDistance(new Line(1.0, -2.0), 0, 10);
		System.out.println(distance + " == " + calculate);
		assertEquals((int)(distance + 0.5), (int)(calculate + 0.5));
	}

	public void testFindTheNearestPoint() {
		Vector edge = new Vector();
		edge.add(Integer.valueOf(0));
		edge.add(Integer.valueOf(1));
		edge.add(Integer.valueOf(2));
		assertEquals(2, Line.findTheNearestPoint((new Line(-1.0, 6.0)), edge, 10));
	}

}
