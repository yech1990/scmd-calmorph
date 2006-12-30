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
	
	public void testOneOfFourNeighboringPixelsIsThresholdBlacker() {
		int[] points = {10, 11, 12, 13, 14, 
			            15, 16, 7,  3,  19,
		                0,  6,  10, 10, 8,
                        5,  6,  6,  8,  9, 
                        20, 21, 22, 23, 24};
		assertFalse(Segmentation.oneOfFourNeighboringPixelsIsThresholdBlacker(12, 5, points, 6));
		assertTrue(Segmentation.oneOfFourNeighboringPixelsIsThresholdBlacker(13, 5, points, 6));
	}
	
	public void testUpperPixelIsNotThresholdWhiter() {
		int[] points = {10, 11, 12, 13, 14, 
	                    15, 14, 7,  13, 19,
                        0,  6,  10, 10, 8,
                        5,  6,  6,  8,  9, 
                        20, 21, 22, 23, 24};
		boolean[] check = new boolean[points.length];
		assertFalse(Segmentation.upperPixelIsNotThresholdWhiter(11, 5, points, check, 6));
		assertTrue(Segmentation.upperPixelIsNotThresholdWhiter(12, 5, points, check, 6));
		assertTrue(Segmentation.upperPixelIsNotThresholdWhiter(13, 5, points, check, 6));
		check[7] = true;
		assertFalse(Segmentation.upperPixelIsNotThresholdWhiter(12, 5, points, check, 6));
	}
	
	public void testLowerPixelIsNotThresholdWhiter() {
		int[] points = {10, 11, 12, 13, 14, 
                        15, 14, 7,  13, 19,
                        0,  6,  10, 10, 8,
                        5,  12, 6,  12, 9, 
                        20, 21, 22, 23, 24};
		boolean[] check = new boolean[points.length];
		assertFalse(Segmentation.lowerPixelIsNotThresholdWhiter(11, 5, points, check, 6));
		assertTrue(Segmentation.lowerPixelIsNotThresholdWhiter(12, 5, points, check, 6));
		assertTrue(Segmentation.lowerPixelIsNotThresholdWhiter(13, 5, points, check, 6));
		check[17] = true;
		assertFalse(Segmentation.lowerPixelIsNotThresholdWhiter(12, 5, points, check, 6));
	}
	
	public void testLeftPixelIsNotThresholdWhiter() {
		int[] points = {10, 11, 12, 13, 14, 
                        15, 14, 7,  13, 19,
                        12, 6,  11, 6,  8,
                        5,  6,  6,  8,  9, 
                        20, 21, 22, 23, 24};
		boolean[] check = new boolean[points.length];
		assertFalse(Segmentation.leftPixelIsNotThresholdWhiter(11, 5, points, check, 6));
		assertTrue(Segmentation.leftPixelIsNotThresholdWhiter(12, 5, points, check, 6));
		assertTrue(Segmentation.leftPixelIsNotThresholdWhiter(13, 5, points, check, 6));
		check[11] = true;
		assertFalse(Segmentation.leftPixelIsNotThresholdWhiter(12, 5, points, check, 6));
	}
	
	public void testRightPixelIsNotThresholdWhiter() {
		int[] points = {10, 11, 12, 13, 14, 
                        15, 14, 7,  13, 19,
                        0,  0,  10, 15, 8,
                        5,  6,  6,  8,  9, 
                        20, 21, 22, 23, 24};
		boolean[] check = new boolean[points.length];
		assertFalse(Segmentation.rightPixelIsNotThresholdWhiter(11, 5, points, check, 6));
		assertTrue(Segmentation.rightPixelIsNotThresholdWhiter(12, 5, points, check, 6));
		assertTrue(Segmentation.rightPixelIsNotThresholdWhiter(13, 5, points, check, 6));
		check[13] = true;
		assertFalse(Segmentation.rightPixelIsNotThresholdWhiter(12, 5, points, check, 6));
	}
}
