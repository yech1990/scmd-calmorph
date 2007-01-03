package lab.cb.scmd.calmorph2;

public class Thresholding {
	
	private int _size;
	private int[] _points, _gray_scale_histogram;
	
	public Thresholding(int[] points) {
		_size = points.length;
		
		_points = new int[points.length];
		for ( int i = 0; i < points.length; i ++ ) { _points[i] = points[i]; }
		
		_gray_scale_histogram = new int[256];
        for ( int i = 0; i < 256; i++ ) { _gray_scale_histogram[i] = 0; }
	}
	
	public int[] getPoints() {
		return _points;
	}
	
	/**
	 * 二値化を実行
	 * @return
	 */
	public boolean executeThresholding() {
		if ( checkOutsideOfTheScopeAndMakeGrayScaleHistogram() ) { return true; }
		int threshold = determineThreshold();
        execute(threshold);
        return false;
	}
	
	/**
	 * 画像のgray valueが256以上ならTRUEを返す。
	 * 同時にgray valueのヒストグラムを作成する。
	 * @return
	 */
	protected boolean checkOutsideOfTheScopeAndMakeGrayScaleHistogram() {
        for ( int i = 0; i < _size; i++ ) {
            if ( _points[i] > 255 ) { return true; }
            _gray_scale_histogram[_points[i]]++;
        }
        return false;
	}
	
	/**
	 * thresholdを決める
	 * @return : threshold
	 */
	protected int determineThreshold() {
		int result = 0;
		
		double gray_mean = 0;
        for ( int i = 0; i < 256; i++ ) { gray_mean += (double)(i) * (double)(_gray_scale_histogram[i]) / (double)(_size); }
        
        double cumulative_ratio = (double)(_gray_scale_histogram[0]) / (double)(_size);
        double temp_gray_mean = 0;
        double maxv = 0;
        
        for ( int i = 1; i < 256; i++ ) {
            if ( 0 < cumulative_ratio && cumulative_ratio < 1 ) {
            	double sk = (gray_mean * cumulative_ratio - temp_gray_mean) 
            	         * (gray_mean * cumulative_ratio - temp_gray_mean) / (cumulative_ratio * (1 - cumulative_ratio));
            	if ( maxv < sk ) {
            		maxv = sk;
            		result = i - 1;
            	}
            }
            temp_gray_mean += (double)(i) * (double)(_gray_scale_histogram[i]) / (double)(_size);
            cumulative_ratio += (double)(_gray_scale_histogram[i]) / (double)(_size);
        }
        return result;
	}
	
	/**
	 * 二値化　（Background==黒 --> Cell==黒）
	 * @param threshold
	 */
	protected void execute(int threshold) {
		for ( int i = 0; i < _size; i++ ) {
            if ( _points[i] >= threshold ) { _points[i] = 0; }
            else { _points[i] = 255; }
        }
	}
	
}
