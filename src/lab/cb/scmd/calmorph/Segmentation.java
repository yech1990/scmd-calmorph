package lab.cb.scmd.calmorph;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;
import java.util.Vector;

import javax.imageio.ImageIO;

import lab.cb.scmd.calmorph2.CalmorphCommon;
import lab.cb.scmd.calmorph2.Labeling;
import lab.cb.scmd.calmorph2.MorphologicalOperation;
import lab.cb.scmd.calmorph2.Thresholding;

public class Segmentation {
	
	private final static boolean _black = true;
	private final static boolean _white = false;
	
	private final static int _size_threshold_1 = 10;
	private final static int _size_threshold_2 = 200;
	
	public static void main(String[] args) throws IOException {
		System.out.println("SCMD START");
		Segmentation sm = new Segmentation();
		
		YeastImage yi = new YeastImage("temp", "cell_wall", ImageIO.read(new File(args[0])), 0);
		sm.segment(yi);
		System.out.println("SCMD END");
	}
	
	// TODO
	public void segment(YeastImage image) {
		boolean err = false;
		String err_kind = "";
		
		image.setPoints(Segmentation.medianFilter(image.getOriginalPoints(), image.getWidth()));
		int[] ci = image.getPoints();
		int[] ci2 = (int[])ci.clone();
        ci  = Segmentation.segmentRoughly(ci, 3, image.getWidth());
        ci2 = Segmentation.segmentRoughly(ci2, 7, image.getWidth());
        
        boolean[] boundary_TRUE_points = Segmentation.makeDifferentPointsBetweenArg1AndArg2TRUE(ci,ci2);
        ci = Segmentation.division(ci, ci2, boundary_TRUE_points, image.getWidth());
        
		ci = Segmentation.scaling(ci, image.getWidth()); // not yet read
		
		Thresholding thr = new Thresholding(ci);
		if ( thr.executeThresholding() ) {
            err = true;
            err_kind = "incorrect colerspace of image";
            return;
        }
		ci = thr.getPoints();
		
		MorphologicalOperation mo = new MorphologicalOperation(image.getWidth(), ci);
        mo.beforecover();
		mo.dilation();
		mo.cover();
		mo.erosion();
        mo.erosion();
        mo.dilation2();
        mo.dilation2();
        mo.dilation2();
        ci = mo.getPoints();
        
        EdgeDetection ed = new EdgeDetection(image.getWidth(), image.getSize(), ci, image.getOriginalPoints());
        ed.edge(0);
        
        //NeckDetection nd = new NeckDetection();
        //nd.searchNeck(image.getWidth(), image.getSize(), ed.getCell(), ed.getPixelToCell(), ed.getPixelToCell2());
        
        //image.ploEdgePoints(ed.getCell());
        //image.plotNeckPoints(ed.getCell());
		//image.drawImage("yeast_test.jpg");
	}
	
	private void drawImageAndExit(YeastImage image, int[] points) {
		YeastImage yi = new YeastImage(image.getWidth(), image.getHeight(), points);
		yi.drawImage("temp.jpg");
        System.out.println("drawImageTmp() END");
		System.exit(0);
	}
	
	/**
	 * Median Filter (3×3)
	 * @param points
	 * @param width
	 * @return
	 */
    public static int[] medianFilter(final int[] points, final int width) {
    	int[] result = new int[points.length];
    	int height = points.length / width;
    	
		for ( int i = 0; i < height; i++ ) {
			for ( int j = 0; j < width; j++ ) {
				if ( i == 0 || i == height - 1 || j == 0 || j == width - 1 ) { result[i * width + j] = points[i * width + j]; }
				else { result[i * width + j] = pickOutMiddleValueInSquare(points, j, i, width); }
			}
		}
		return result;
    }
    
    /**
     * 3×3正方形内の中間値を返す
     * @param points
     * @param x
     * @param y
     * @param width
     * @return
     */
    protected static int pickOutMiddleValueInSquare(int[] points, int x, int y, int width) {
    	int[] filter = new int[9];
		filter[0] = points[(y - 1) * width + x - 1];
		filter[1] = points[(y - 1) * width + x];
		filter[2] = points[(y - 1) * width + x + 1];
		filter[3] = points[y * width + x - 1];
		filter[4] = points[y * width + x];
		filter[5] = points[y * width + x + 1];
		filter[6] = points[(y + 1) * width + x - 1];
		filter[7] = points[(y + 1) * width + x];
		filter[8] = points[(y + 1) * width + x + 1];
		Arrays.sort(filter);
		return filter[4];
    }
    
    /**
     * Backgroundの黒色の一様性によりSegmentation （旧 vivid() ）
     * @param points
     * @param threshold
     * @param width
     * @return
     */
    public static int[] segmentRoughly(final int[] points, final int threshold, final int width) {
		int[] result = new int[points.length];
		for ( int i = 0; i < result.length; i++ ) { result[i] = points[i]; }
		
		int most_black_point = pickOutMostBlackPoint(points, width);
		result = setIntenseBlackByBlackHomogeneityFromArg1Pixel(most_black_point, threshold, width, points, result);
		
		return result;
    }
    
    /**
     * 画像の縁以外で最も黒い点の位置を返す
     * @param points
     * @param width
     * @return
     */
    protected static int pickOutMostBlackPoint(int[] points, int width) {
		int result = -1;
		int min_value = 255;
		int height = points.length / width;
		
		for ( int i = 0; i < height; i++ ) {
			for ( int j = 0; j < width; j++ ) {
				if ( !( i == 0 || i == height - 1 || j == 0 || j == width - 1 ) && points[i * width + j] < min_value ) {
					min_value = points[i * width + j];
					result = i * width + j;
				}
			}
		}
		return result;
    }
    
    /**
     * 最も黒い点から、黒さの一様性が保たれる範囲（急に白くなる点で終了）を、真っ黒（value==0）にする。
     * Backgroundを真っ黒にする。
     * @param most_black_point
     * @param threshold
     * @param width
     * @param points
     * @param result
     * @return
     */
    protected static int[] setIntenseBlackByBlackHomogeneityFromArg1Pixel(final int most_black_point, final int threshold, 
    		final int width, final int[] points, int[] result) {
    	Stack<Integer> stk = new Stack<Integer>();
		stk.push(most_black_point);
		
		boolean[] check = new boolean[points.length];
		check[most_black_point] = true;
		
		while( !stk.empty() ) {
			int p = ((Integer)stk.pop()).intValue();
			
			if ( oneOfFourNeighboringPixelsIsThresholdBlacker(p, width, points, threshold) ) { continue; }
			result[p] = 0;
			
			if ( upperPixelIsNotThresholdWhiter(p, width, points, check, threshold) ) { stk.push(new Integer(p - width)); }
			if ( p - width >= 0 ) { check[p - width] = true; }
			
			if ( lowerPixelIsNotThresholdWhiter(p, width, points, check, threshold) ) { stk.push(new Integer(p + width)); }
			if ( p + width < points.length) { check[p + width] = true; }
			
			if ( leftPixelIsNotThresholdWhiter(p, width, points, check, threshold) )  { stk.push(new Integer(p - 1)); }
			if ( p % width != 0 ) { check[p-1] = true; }
			
			if ( rightPixelIsNotThresholdWhiter(p, width, points, check, threshold) ) { stk.push(new Integer(p + 1)); }
			if ( p % width != width - 1 ) { check[p + 1] = true; }
		}
		return result;
    }
    
    /**
     * 4近傍点のどれか1つが、閾値以上黒い場合TRUEを返す。
     * @param p
     * @param width
     * @param points
     * @param threshold
     * @return
     */
    protected static boolean oneOfFourNeighboringPixelsIsThresholdBlacker(int p, int width, int[] points, int threshold) {
    	if ( ( p - width < 0              || points[p] - points[p - width] < threshold ) && 
			 ( p + width >= points.length || points[p] - points[p + width] < threshold ) && 
			 ( p % width <= 0             || points[p] - points[p - 1]     < threshold ) && 
			 ( p % width >= width - 1     || points[p] - points[p + 1]     < threshold ) ) { return false; }
    	return true;
    }
    
    /**
     * 上隣接点が、閾値以上白くない場合TRUEを返す。 （上隣接点の白さはせいぜい閾値程度）
     * @param p
     * @param width
     * @param points
     * @param check
     * @param threshold
     * @return
     */
    protected static boolean upperPixelIsNotThresholdWhiter(int p, int width, int[] points, boolean[] check, int threshold) {
    	if ( p - width >= 0 && !check[p - width] && points[p - width] - points[p] < threshold ) { return true; }
    	return false;
    }
    
    /**
     * 下隣接点が、閾値以上白くない場合TRUEを返す。 （下隣接点の白さはせいぜい閾値程度）
     * @param p
     * @param width
     * @param points
     * @param check
     * @param threshold
     * @return
     */
    protected static boolean lowerPixelIsNotThresholdWhiter(int p, int width, int[] points, boolean[] check, int threshold) {
    	if ( p + width < points.length && !check[p + width] && points[p + width] - points[p] < threshold ) { return true; }
    	return false;
    }
    
    /**
     * 左隣接点が、閾値以上白くない場合TRUEを返す。 （左隣接点の白さはせいぜい閾値程度）
     * @param p
     * @param width
     * @param points
     * @param check
     * @param threshold
     * @return
     */
    protected static boolean leftPixelIsNotThresholdWhiter(int p, int width, int[] points, boolean[] check, int threshold) {
    	if ( p % width != 0 && !check[p - 1] && points[p - 1] - points[p] < threshold ) { return true; }
    	return false;
    }
    
    /**
     * 右隣接点が、閾値以上白くない場合TRUEを返す。 （右隣接点の白さはせいぜい閾値程度）
     * @param p
     * @param width
     * @param points
     * @param check
     * @param threshold
     * @return
     */
    protected static boolean rightPixelIsNotThresholdWhiter(int p, int width, int[] points, boolean[] check, int threshold) {
    	if ( p % width != width - 1 && !check[p + 1] && points[p + 1] - points[p] < threshold ) { return true; }
    	return false;
    }    
    
    /**
     * 2つの配列の各要素を比較し、同じなら白（FALSE）、異なるなら黒（TRUE）とする。 （旧 dif() ）
     * 異なる閾値でsegmentRoughly()を実行した結果を比較する。 == yeast と Background の境界を黒（TRUE）とする配列に変換する。
     * Background に偽境界はないが、yeast　側には偽境界があるのでは？
     * @param points_1
     * @param points_2
     * @return
     */
    public static boolean[] makeDifferentPointsBetweenArg1AndArg2TRUE(final int[] points_1, final int[] points_2) {
    	if ( points_1.length != points_2.length ) {
    		CalmorphCommon.errorExit("Segmentation.differentiate()", "points_1.length != points_2.length");
    	}
    	boolean[] result = new boolean[points_1.length];
    	for ( int i = 0 ; i < points_1.length; i++ ) {
    		if   ( points_1[i] == points_2[i] ) { result[i] = _white; }
    		else { result[i] = _black; }
    	}
    	return result;
    }
    
    /**
     * difで求めた差分のうち背景であると確定する部分とそうでない部分を分ける
     * @param points_1 : Backgroundを真っ黒（=0）にしたint[]
     * @param points_2 : Backgroundを真っ黒（=0）にしたint[] （points_1よりBackgroundは大きい）
     * @param boundary_TRUE_points : yeast と Background の境界を黒（TRUE）としたBoolean配列
     * @param width
     */
    public static int[] division(int[] points_1, int[] points_2, boolean[] boundary_TRUE_points, int width){
    	int size = points_1.length;
		
    	Labeling label_b = new Labeling(width, size, _size_threshold_1, true);
    	Vector<Integer>[] labeled_boundary = label_b.label(boundary_TRUE_points, _black);
    	
		Labeling label_2 = new Labeling(width, size, _size_threshold_2, false);
		Vector<Integer>[] labeled_2_cell = label_2.label(reverseBlackAndWhite(points_2), _black);
		
		int[] label_of_each_pixel_in_2_cell_black_image = setLabelInEachPixel(size, labeled_2_cell);
		points_1 = eliminateDistinctLabelsWhichNeighbor(points_1, width, labeled_boundary, label_of_each_pixel_in_2_cell_black_image);
		
		return points_1;
    }
    
    /**
     * Backgroundを白（FALSE）、Cellを黒（TRUE）に逆転二値化
     * @param points : segmentRoughly() でBackgroundを真っ黒（=0）にしたint[]
     * @return
     */
    protected static boolean[] reverseBlackAndWhite(int[] points) {
    	boolean[] result = new boolean[points.length];
		for ( int i = 0; i < result.length; i++ ) {
			if ( points[i] == 0 ) { result[i] = _white; }
			else { result[i] = _black; }
		}
		return result;
    }
    
    /**
     * 全ピクセルのlabelを格納した配列を返す
     * @param size
     * @param labeled
     * @return
     */
    protected static int[] setLabelInEachPixel(int size, Vector<Integer>[] labeled) {
    	int[] label_of_each_pixel = new int[size];
		for ( int i = 0; i < size; i++ ) { label_of_each_pixel[i] = -1; }
		for ( int i = 0; i < labeled.length; i++ ) {
			for ( int p : labeled[i] ) { label_of_each_pixel[p] = i; }
		}
		return label_of_each_pixel;
    }
    
    /**
     * labeledの連結成分間の距離が短い（4-近傍同士が接する）連結成分のpoints[]を、真っ黒（=0）にする。
     * @param points : Background小さめのsegmentRoughly()の結果。
     * @param width
     * @param labeled : 境界を黒とした配列を基に、連結成分をラベリングした結果。
     * @param label_of_each_pixel : Background大きめのsegmentRoughly()の結果を基に、Cellを黒として連結成分をラベリングした結果のint[]。
     * @return
     */
    protected static int[] eliminateDistinctLabelsWhichNeighbor(int[] points, int width, 
    		Vector<Integer>[] labeled, int[] label_of_each_pixel) {
    	for ( int i = 0; i < labeled.length; i++ ) {
			int neighbor_label = -1;
			boolean check = false;
			for ( int p : labeled[i] ) {
				int[] stk = new int[4];
				stk[0] = p - width;
				stk[1] = p - 1;
				stk[2] = p + 1;
				stk[3] = p + width;
				for ( int k = 0; k < 4; k++ ) {
					if ( neighbor_label != -1 && label_of_each_pixel[stk[k]] != -1 && label_of_each_pixel[stk[k]] != neighbor_label ) {
						check = true;
						break;
					}
					else if ( label_of_each_pixel[stk[k]] != -1 ) { neighbor_label = label_of_each_pixel[stk[k]]; }
				}
				if ( check ) { break; }
			}
			if ( check ) {
				for ( int p : labeled[i] ){ points[p] = 0; }
			}
		}
    	return points;
    }
    
    /**
     * 重みつき輝度勾配を求めて0～255にスケーリングする （旧gradim()）
     * @param points
     * @param width
     * @return
     */
	public static int[] scaling(int[] points, int width){
		int size = points.length;
		int height = size / width;
		
		int Cimage[] = (int[])points.clone();
		for ( int i = 0; i < size; i++ ) {
			if ( Cimage[i] < 60 && Cimage[i] >= 10 ) { Cimage[i] -= (60 - Cimage[i]) * (60 - Cimage[i]) / 20; }
			else if ( Cimage[i] < 10 ) { Cimage[i] -= 125; }
			else { Cimage[i] = 60; }
		}
		
		double gradmag[] = new double[size];
		double maxmag = 0;
		for ( int i = 1; i < height - 1; i++ ) {
			for ( int j = 1; j < width - 1; j++ ) {
				if ( allNeighboringNinePixelsOfArg1and2InArg3AreNotEqualArg4(i, j, Cimage, -125, width) ) {
					gradmag[i * width + j] = Math.sqrt( 
							( 2 * (Cimage[i * width + j - 1] - Cimage[i * width + j + 1] ) + 
									(Cimage[(i+1) * width + j - 1] - Cimage[(i+1) * width + j + 1]) + 
									(Cimage[(i-1) * width + j - 1] - Cimage[(i-1) * width + j + 1]) ) * 
									( 2 * (Cimage[i * width + j - 1] - Cimage[i * width + j + 1]) + 
											(Cimage[(i+1) * width + j - 1] - Cimage[(i+1) * width + j + 1]) + 
											(Cimage[(i-1) * width + j - 1] - Cimage[(i-1) * width + j + 1]) ) + 
											( 2 * (Cimage[(i-1) * width + j] - Cimage[(i+1) * width + j]) + 
													(Cimage[(i-1) * width + j + 1] - Cimage[(i+1) * width + j + 1]) + 
													(Cimage[(i-1) * width + j - 1] - Cimage[(i+1) * width + j - 1]) ) * 
													( 2 * (Cimage[(i-1) * width + j] - Cimage[(i+1) * width + j]) + 
															(Cimage[(i-1) * width + j + 1] - Cimage[(i+1) * width + j + 1]) + 
															(Cimage[(i-1) * width + j - 1] - Cimage[(i+1) * width + j - 1]) ) );
					if ( gradmag[i * width + j] > maxmag ) { maxmag = gradmag[i * width + j]; }
				}
			}
		}
		
		for ( int i = 1; i < height - 1; i++ ) {
			for ( int j = 1; j < width - 1; j++ ) {
				if ( oneOrNeighboringEightPixelsOfArg1and2InArg3AreEqualArg4(i, j, Cimage, -125, width) ) {
					gradmag[i*width+j] = maxmag / 2;
				}
			}
		}
		
		for ( int i = 0; i < height; i++ ) {
			for ( int j = 0; j < width; j++ ) {
				if ( i == 0 || i == height - 1 || j == 0 || j == width - 1 ) {
					gradmag[i * width + j] = 1;
					continue;
				}
				gradmag[i * width + j] = 1 - gradmag[i * width + j] / maxmag;
			}
		}
		
		int[] result = new int[size];
		for ( int i = 0; i < size; i++ ) { result[i] = 255 - Math.round((float)(gradmag[i]*255)); }
		return result;
	}
	
	/**
	 * 
	 * @param i
	 * @param j
	 * @param points
	 * @param compare
	 * @param width
	 * @return
	 */
	protected static boolean allNeighboringNinePixelsOfArg1and2InArg3AreNotEqualArg4(int i, int j, int[] points, int compare, int width){
		if ( points[i * width + j - 1] != compare && points[i * width + j] != compare && 
				points[i * width + j + 1] != compare && points[(i-1) * width + j - 1] != compare && 
				points[(i-1) * width + j] != compare && points[(i-1) * width + j + 1] != compare && 
				points[(i+1) * width + j - 1] != compare && points[(i+1) * width + j] != compare && 
				points[(i+1) * width + j + 1] != compare ) { return true; }
		else { return false; }
	}
	
	/**
	 * 
	 * @param i
	 * @param j
	 * @param points
	 * @param compare
	 * @param width
	 * @return
	 */
	protected static boolean oneOrNeighboringEightPixelsOfArg1and2InArg3AreEqualArg4(int i, int j, int[] points, int compare, int width){
		if ( points[i * width + j] != compare && ( points[i * width + j - 1] == compare || 
				points[i * width + j + 1] == compare || points[(i-1) * width + j - 1] == compare || 
				points[(i-1) * width + j] == compare || points[(i-1) * width + j + 1] == compare || 
				points[(i+1) * width + j - 1] == compare || points[(i+1) * width + j] == compare || 
				points[(i+1) * width + j + 1] == compare ) ) { return true; }
		else { return false; }
	}
	
	/**
	 * 
	 * @param binary_int_points
	 * @return
	 */
    public static boolean[] convertBinaryIntPointsToBinaryBoolean(int[] binary_int_points) {
    	boolean[] result = new boolean[binary_int_points.length];
    	for ( int i = 0; i < binary_int_points.length; i++ ) {
    		if      ( binary_int_points[i] == 0 )   { result[i] = _black; }
    		else if ( binary_int_points[i] == 255 ) { result[i] = _white; }
    		else { CalmorphCommon.errorExit("SCMD beforecover", "binary"); }
    	}
    	return result;
    }
    
}
