package lab.cb.scmd.calmorph;

import java.util.Arrays;
import java.util.Stack;
import java.util.Vector;

import lab.cb.scmd.calmorph2.CalmorphCommon;

public class Segmentation {
	
	private final static boolean _black = true;
	private final static boolean _white = false;
	
	private final static int _size_threshold_1 = 10;
	private final static int _size_threshold_2 = 200;
	
	public void segment(YeastImage image) {
		boolean err = false;
		String err_kind = "";
		
		image.setPoints(Segmentation.medianFilter(image.getOriginalPoints(), image.getWidth()));
		int[] ci = image.getPoints();
		int[] ci2 = (int[])ci.clone();
        Segmentation.segmentRoughly(ci, 3, image.getWidth());
        Segmentation.segmentRoughly(ci2, 7, image.getWidth());
        
        // TODO 12/30
        boolean[] difci = Segmentation.differentiate(ci,ci2);
        
        /*
        Segmentation.division(ci,ci2,difci, image.getWidth());
		ci = Segmentation.gradim(ci, image.getWidth());
        if(!Segmentation.threshold(ci)) {//画像の色が255を超えたら
            err = true;
            err_kind = "incorrect colerspace of image";
            return;
        }
        Segmentation.beforecover(ci, image.getWidth());
		Segmentation.dilation(ci, image.getWidth());
		Segmentation.cover(ci, image.getWidth());
		Segmentation.erosion(ci, image.getWidth());
        Segmentation.erosion(ci, image.getWidth());
        Segmentation.dilation2(ci, image.getWidth());
        Segmentation.dilation2(ci, image.getWidth());
        Segmentation.dilation2(ci, image.getWidth());
        
        EdgeDetection ed = new EdgeDetection(image.getSize());
        ed.edge(ci,image.getOriginalPoints(), image.getWidth(), 0);
        
        NeckDetection nd = new NeckDetection();
        nd.searchNeck(image.getWidth(), image.getSize(), ed.getCell(), ed.getPixelToCell(), ed.getPixelToCell2());
        
        //_image.ploEdgePoints(ed.getCell());
        image.plotNeckPoints(ed.getCell());
		image.drawImage("yeast_test.jpg");
		*/
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
     * （旧 dif() ）
     * @param points1
     * @param points2
     * @return
     */
    public static boolean[] differentiate(int[] points1, int[] points2) {
    	assert points1.length == points2.length;
    	boolean[] result = new boolean[points1.length];
    	for ( int i = 0 ; i < points1.length; i++ ) {
    		if   ( points1[i] == points2[i] ) { result[i] = _white; }
    		else { result[i] = _black; }
    	}
    	return result;
    }
    
    public static void division(int[] points_1, int[] points_2, boolean[] dif_binary, int width){
    	int size = points_1.length;
		
    	Vector[] labeled_1 = label(dif_binary, _black, _size_threshold_1, width, size / width, true);
		boolean[] thci = new boolean[size];
		for ( int i = 0; i < size; i++ )	{
			if ( points_2[i] == 0 ) { thci[i] = _white; }
			else { thci[i] = _black; }
		}
		
		Vector[] labeled_2 = label(thci,_black, _size_threshold_2, width, size / width, false);
		int[] pixeltoarea = new int[size];
		for ( int i = 0; i < size; i++ ) { pixeltoarea[i] = -1; }
		for ( int i = 0; i < labeled_2.length; i++ ) {
			for ( int j = 0; j < labeled_2[i].size(); j++ ) {
				pixeltoarea[( (Integer)labeled_2[i].get(j) ).intValue()] = i;
			}
		}
		for ( int i = 0; i < labeled_1.length; i++ ) {
			int neighbor = -1;
			boolean check = false;
			for(int j=0;j<labeled_1[i].size();j++){
				int p = ((Integer)labeled_1[i].get(j)).intValue();
				int[] stk = new int[4];
				stk[0] = p-width;
				stk[1] = p-1;
				stk[2] = p+1;
				stk[3] = p+width;
				for(int k=0;k<4;k++){
					if(neighbor != -1 && pixeltoarea[stk[k]] != -1 && pixeltoarea[stk[k]] != neighbor){
						check = true;
						break;
					}
					else if(pixeltoarea[stk[k]] != -1) neighbor = pixeltoarea[stk[k]];
				}
				if(check) break;
			}
			if(check){
				for(int j=0;j<labeled_1[i].size();j++){
					points_1[((Integer)labeled_1[i].get(j)).intValue()] = 0;
				}
			}
		}
    }
    
    // TODO
    public static Vector[] label(boolean[] binary_image, boolean color,int size_threshold, int width, int height, boolean corner_cut) {
    	int size = binary_image.length;
        Vector<Integer> same = new Vector<Integer>();
        int[] label = new int[size];
        
        int label_number = numberLabeling(label, width, binary_image, color, same);
        
        // sameをセットし直す　（必要？）
        int max_label = -1;
        for ( int i = 0; i < same.size(); i++ ) {
            int s = smallestlabel(same, i);
            if ( max_label < s ) { max_label = s; }
            same.set(i, new Integer(s));
        }
        
        label_number = max_label;
        Vector[] vec2 = new Vector[label_number + 1];
        for ( int i = 0; i < label_number + 1; i++ ) {
            vec2[i] = new Vector();
        }
        for ( int i = 0; i < size; i++ ) {
            if ( label[i] < 0 ) { }
            else { vec2[( (Integer)same.get(label[i]) ).intValue()].add(new Integer(i)); }
        }
        
        int number = 0;
        boolean[] flags = new boolean[label_number + 1];  //塊とみなすかどうか
        for ( int i = 0 ; i < label_number + 1; i++ ) {
            if ( vec2[i].size() > size_threshold ) {      //サイズ以上の塊について
                if ( !corner_cut ) {               //corner_cutが指定されていなければ
                    flags[i] = true;
                    number++;
                } else {                           //cornercutが指定されていれば
                    flags[i]=true;
                    for ( int j = 0; j < vec2[i].size(); j++ ) {
                        int p = ( (Integer)vec2[i].get(j) ).intValue();
                        if ( p < width || p > width * (height-1) || p % width == 0 || p % width == width - 1 ) {  //壁に接するpixelが存在
                            flags[i] = false;
                            break;
                        }
                    }
                    if ( flags[i] ) { number++; }
                }
            } else { flags[i] = false; }
        }
        
        Vector<Integer>[] result = new Vector[number];
        int index = 0;
        for ( int i = 0; i < result.length; i++ ) {
            result[i] = new Vector<Integer>();
            while ( index < label_number + 1 ) {
                if ( flags[index] ) { break; }
                index++;
            }
            if ( index < label_number + 1 ) {
                for ( int k = 0; k < vec2[index].size(); k++ ) {
                    result[i].add( (Integer)vec2[index].get(k) );
                }
                index++;
            } else { break; }
        }
        
        return result;
    }
    
    /**
     * 二値化画像を連結成分ごとにラベリング
     * @param label
     * @param width
     * @param binary_image
     * @param color
     * @param same
     * @return
     */
    private static int numberLabeling(int[] label, int width, boolean[] binary_image, boolean color, Vector<Integer> same) {
    	int label_number = 0;
    	int size = label.length;
    	int height = label.length / width;
        for ( int i = 0; i < size; i++ ) { label[i] = -1; }
        
        if ( binary_image[0] == color ) {
            label[0] = label_number;
            same.add(new Integer(label_number++));
        }
        for ( int j = 1; j < width; j++ ) {
            if ( binary_image[j] == color ) {
                if ( label[j-1] >= 0 ) {
                    label[j] = label[j-1];
                } else {
                    label[j] = label_number;
                    same.add(new Integer(label_number++));
                }
            }
        }
        for (int i = 1; i < height; i++ ) {
            if ( binary_image[i * width] == color ) {
                if ( label[(i-1) * width] >= 0 ) {
                    label[i * width] = label[(i-1) * width];
                } else {
                    label[i * width] = label_number;
                    same.add(new Integer(label_number++));
                }
            }
            for ( int j = 1; j < width; j++ ) {
                if ( binary_image[i * width + j] == color ) {
                    int left_label, upper_label;
                    if ( label[i * width + j - 1] >= 0) { left_label = smallestlabel(same, label[i * width + j - 1]); }
                    else left_label = -1;
                    if ( label[(i-1) * width + j] >= 0) { upper_label = smallestlabel(same, label[(i-1) * width + j]); }
                    else upper_label = -1;
                    
                    if ( left_label == -1 && upper_label == -1 ) {
                        label[i * width + j] = label_number;
                        same.add(new Integer(label_number++));
                    } else if ( left_label == -1 ) {
                        label[i * width + j] = upper_label;
                    } else if ( upper_label == -1 ) {
                        label[i * width + j] = left_label;
                    } else if ( left_label < upper_label ) {
                        label[i * width + j] = left_label;
                        same.set(upper_label, new Integer(left_label));
                    } else {
                        label[i * width + j] = upper_label;
                        same.set(left_label, new Integer(upper_label));
                    }
                }
            }
        }
        return label_number;
    }
    
    
    public static int smallestlabel(Vector<Integer> same, int label_number) {
        int result = label_number;
        Vector<Integer> temp = new Vector<Integer>();
        while (true) {
            if ( ((Integer)same.get(result)).intValue() == result ) {
                for ( int i = 0; i < temp.size(); i++) {
                    same.set( ((Integer)temp.elementAt(i) ).intValue(), new Integer(result) );   // 必要？
                }
                return result;
            } else {
                temp.add( new Integer(result) );
                result = ( (Integer)same.get(result) ).intValue();
            }
        }
    }
    
	public static int[] gradim(int[] points, int width){
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
	
	private static boolean allNeighboringNinePixelsOfArg1and2InArg3AreNotEqualArg4(int i, int j, int[] points, int compare, int width) {
		if ( points[i * width + j - 1] != compare && points[i * width + j] != compare && 
				points[i * width + j + 1] != compare && points[(i-1) * width + j - 1] != compare && 
				points[(i-1) * width + j] != compare && points[(i-1) * width + j + 1] != compare && 
				points[(i+1) * width + j - 1] != compare && points[(i+1) * width + j] != compare && 
				points[(i+1) * width + j + 1] != compare ) { return true; }
		else { return false; }
	}
	
	private static boolean oneOrNeighboringEightPixelsOfArg1and2InArg3AreEqualArg4(int i, int j, int[] points, int compare, int width) {
		if ( points[i * width + j] != compare && ( points[i * width + j - 1] == compare || 
				points[i * width + j + 1] == compare || points[(i-1) * width + j - 1] == compare || 
				points[(i-1) * width + j] == compare || points[(i-1) * width + j + 1] == compare || 
				points[(i+1) * width + j - 1] == compare || points[(i+1) * width + j] == compare || 
				points[(i+1) * width + j + 1] == compare ) ) { return true; }
		else { return false; }
	}
	
    public static boolean threshold(int[] image) {
    	int size = image.length;
    	
    	int[] hg = new int[256];
        for ( int i = 0; i < 256; i++ ) { hg[i] = 0; }
        
        double ut = 0;
        for ( int i = 0; i < size; i++ ) {
            if ( image[i] > 255 ) { return false; }
            hg[image[i]]++;
        }
        
        for ( int i = 0; i < 256; i++ ) { ut += (double)(i) * (double)(hg[i]) / (double)(size); }
        
        double maxv = 0;
        double wk = (double)(hg[0]) / (double)(size);
        double uk = 0;
        double sk = 0;
        int maxk=0;
        for ( int k = 1; k < 255; k++ ) {
            if ( wk > 0 && wk < 1 ) {
            	sk = (ut * wk - uk) * (ut * wk - uk) / (wk * (1-wk));
            	if ( maxv < sk ) {
            		maxv = sk;
            		maxk = k - 1;
            	}
            }
            uk += (double)(hg[k]) * (double)(k) / (double)(size);
            wk += (double)(hg[k]) / (double)(size);
        }
        // thresholding
        for ( int i = 0; i < size; i++ ) {
            if ( image[i] >= maxk ) { image[i] = 0; }
            else { image[i] = 255; }
        }
        return true;
    }
    
    public static void beforecover(int[] binary_int_points, int width) {
    	Vector[] vec = label(
    			convertBinaryIntPointsToBinaryBoolean(binary_int_points), _white, 0, width, binary_int_points.length / width, true);
		
    	for ( int i = 0; i < binary_int_points.length; i++ ) { binary_int_points[i] = 255; }
		for ( int i = 0; i < vec.length; i++ ) {
			for ( int j = 0; j < vec[i].size(); j++ ) {
				int p = ((Integer)vec[i].get(j)).intValue();
				binary_int_points[p] = 0;
			}
		}
	}
    
    /**
     * 穴埋め
     * @param binary_int_points
     * @param width
     */
    public static void cover(int[] binary_int_points, int width) {
        Vector[] vec = label(
        		convertBinaryIntPointsToBinaryBoolean(binary_int_points), _white, 0, width, binary_int_points.length / width, false);
        
        int max_size = 0;
        int max_index = 0;
        for ( int i = 0; i < vec.length; i++ ) {
            if ( max_size < vec[i].size() ) {
                 max_size = vec[i].size();
                 max_index = i;
             }
        }
        
        for ( int i = 0; i < vec.length; i++ ) {
            if ( i != max_index ) {
                for ( int j = 0; j < vec[i].size(); j++ ) {
                    int p = ((Integer)vec[i].get(j)).intValue();
                    binary_int_points[p] = 0;
                }
            }
        }
    }
    
    public static boolean[] convertBinaryIntPointsToBinaryBoolean(int[] binary_int_points) {
    	boolean[] result = new boolean[binary_int_points.length];
    	for ( int i = 0; i < binary_int_points.length; i++ ) {
    		if      ( binary_int_points[i] == 0 )   { result[i] = _black; }
    		else if ( binary_int_points[i] == 255 ) { result[i] = _white; }
    		else { CalmorphCommon.errorExit("SCMD beforecover", "binary"); }
    	}
    	return result;
    }
    
	public static void dilation(int[] binary_int_points, int width) {
		int height = binary_int_points.length / width;
		int[] temp = new int[binary_int_points.length];
		for ( int i = 0; i < temp.length; i++ ) { temp[i] = binary_int_points[i]; }
		
		for ( int i = 0; i < temp.length; i++ ) {
			if ( i % width - 1 > 0 && i % width + 1 < width ) { temp[i] &= binary_int_points[i - 1]; }
			if ( i % width + 1 < width && i % width - 1 > 0 ) { temp[i] &= binary_int_points[i + 1]; }
			if ( i / width - 1 > 0 && i / width + 1 < height ) { temp[i] &= binary_int_points[i - width]; }
			if ( i / width + 1 < height && i / width - 1 > 0 ) { temp[i] &= binary_int_points[i + width]; }
		}
		for ( int i = 0; i < binary_int_points.length; i++ ) { binary_int_points[i] = temp[i]; }
	}
	
	public static void dilation2(int[] binary_int_points, int width) {
		int height = binary_int_points.length / width;
        Vector[] vec = label(convertBinaryIntPointsToBinaryBoolean(binary_int_points), _black, 0, width, height, false);
		
        int[] group = new int[binary_int_points.length];
		for ( int i = 0; i < group.length; i++ ) { group[i] = -1; }
		for ( int i = 0; i < vec.length; i++ ) {
			for ( int j = 0; j < vec[i].size(); j++ ) { group[((Integer)vec[i].get(j)).intValue()] = i; }
		}
		int[] group2 = new int[width*height];
		for(int i=0;i<width*height;i++) group2[i] = -1;
		for(int i=0;i<width*height;i++) {
			if(i%width>0 && i%width<width-1 && i/width>0 && i/width<height-1){
				int gr=-1;
				boolean check = true;
				if(group[i-1]!=-1){gr = group[i-1];}
				if(group[i+1]!=-1){check &= (gr==-1||group[i+1]==gr); gr = group[i+1];}
				if(group[i-width]!=-1){check &= (gr==-1||group[i-width]==gr);gr = group[i-width];}
				if(group[i+width]!=-1){check &= (gr==-1||group[i+width]==gr);gr = group[i+width];}
				if(check) group2[i] = gr;
			}
		}
		for(int i=0;i<width*height;i++){
			if(group2[i] != -1 && (group2[i-1]==-1 || group2[i-1]==group2[i]) && (group2[i+1]==-1 || group2[i+1]==group2[i]) && 
					(group2[i-width]==-1 || group2[i-width]==group2[i]) && (group2[i+width]==-1 || group2[i+width]==group2[i])){
				binary_int_points[i] = 0;
			}
		}
	}
	
    public static void erosion(int[] binary_int_points, int width) {
    	int height = binary_int_points.length / width;
        int[] temp = new int[binary_int_points.length];
        for ( int i = 0; i < temp.length; i++ ) { temp[i] = binary_int_points[i]; }
        
        for ( int i = 0; i < temp.length; i++ ) {
            if ( i % width - 1 > 0 )          { temp[i] |= binary_int_points[i - 1]; }
            if ( i % width + 1 < width - 1 )  { temp[i] |= binary_int_points[i + 1]; }
            if ( i / width - 1 > 0 )          { temp[i] |= binary_int_points[i - width]; }
            if ( i / width + 1 < height - 1 ) { temp[i] |= binary_int_points[i + width]; }
        }
        for ( int i = 0; i < temp.length; i++ ) { binary_int_points[i] = temp[i]; }
    }
    
}
