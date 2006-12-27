package lab.cb.scmd.calmorph;

import junit.framework.TestCase;

public class SegmentationTest extends TestCase {

	public void testMedianFilter() {
		int[] points = {0,  1,  2,  3,  4, 
         				15, 16, 17, 18, 19,
		        		10, 11, 12, 13, 14,
				        5,  6,  7,  8,  9, 
		                20, 21, 22, 23, 24};
		int[] filtered = Segmentation.medianFilter(points, 5);
		assertEquals(0, filtered[0]);
		assertEquals(4, filtered[4]);
		assertEquals(15, filtered[5]);
		assertEquals(12, filtered[7]);
		assertEquals(19, filtered[9]);
		assertEquals(11, filtered[16]);
		assertEquals(13, filtered[18]);
		assertEquals(20, filtered[20]);
		assertEquals(24, filtered[24]);
	}

	public void testPickOutMiddleValueInSquare() {
		int[] points = {0,  1,  2,  3,  4, 
				        5,  6,  7,  8,  9, 
				        10, 11, 12, 13, 14, 
				        15, 16, 17, 18, 19, 
				        20, 21, 22, 23, 24};
		assertEquals(6, Segmentation.pickOutMiddleValueInSquare(points, 1, 1, 5));
		assertEquals(13, Segmentation.pickOutMiddleValueInSquare(points, 3, 2, 5));
	}
	
	//public void testSegmentRoughly() {
		
	//}
	
	public void testPickOutMostBlackPoint() {
		int[] points = {10, 11, 12, 13, 14, 
 				        15, 16, 17, 18, 19,
        		        0,  1,  2,  3,  4,
		                5,  6,  7,  8,  9, 
                        20, 21, 22, 23, 24};
		assertEquals(11, Segmentation.pickOutMostBlackPoint(points, 5));
		
		int[] points_2 = {10, 11, 12, 13, 14, 
			              15, 16, 17, 18, 19,
		                  1,  2,  0,  3,  4,
                          5,  6,  7,  8,  9, 
                          20, 21, 22, 23, 24};
		assertEquals(12, Segmentation.pickOutMostBlackPoint(points_2, 5));
	}

}
