package lab.cb.scmd.calmorph2;

import java.util.Vector;

public class Labeling {
	
	private int _width, _height, _size, _size_threshold;
	private boolean _corner_cut;
	
	private Vector<Integer>[] _constituent_pixels_of_each_label;
	private boolean[] _label_validity;
	
	public Labeling(int width, int size, int size_threshold, boolean corner_cut) {
		_width = width;
		_height = size / width;
		_size = size;
		_size_threshold = size_threshold;
		_corner_cut = corner_cut;
	}
    
	/**
	 * 連結成分ごとに番号をラベリング
	 * @param binary_image
	 * @param color
	 * @return : label番号ごとに、その全構成pixelを登録したVector、を要素に持つ配列を返す。
	 */
	public Vector<Integer>[] label(boolean[] binary_image, boolean color) {
        NumberLabeling num_label = new NumberLabeling(_width, _size);
        num_label.executeNumberLabeling(binary_image, color);
        
        int max_label = num_label.resetSameLabelsAndGetMaxLabel();
        setConstituentPixelsOfEachLabel(max_label, num_label);
        
        int number_of_valid_labels = eliminateLabelsOnImageCornerAndSmallLabels(max_label);
        return getConstituentPixelsOfValidLabels(number_of_valid_labels, max_label);
    }
	
	/**
	 * label番号ごとに、その全構成pixelを登録。
	 * 登録先 : Vector<Integer>[] _constituent_pixels_of_each_label
	 * @param max_label
	 * @param num_label
	 */
	protected void setConstituentPixelsOfEachLabel(int max_label, NumberLabeling num_label) {
		_constituent_pixels_of_each_label = new Vector[max_label + 1];
        for ( int i = 0; i < max_label + 1; i++ ) { _constituent_pixels_of_each_label[i] = new Vector<Integer>(); }
        for ( int i = 0; i < _size; i++ ) {
            if ( num_label.getLabeled()[i] >= 0 ) {
            	_constituent_pixels_of_each_label[( (Integer)(num_label.getSameLabels().get( num_label.getLabeled()[i] )) ).intValue()].add(new Integer(i));
            }
        }
	}
	
	/**
	 * label番号ごとに、その全構成pixelを登録。（valid labels only version）
	 * @param number_of_valid_labels
	 * @param max_label
	 * @return
	 */
	protected Vector<Integer>[] getConstituentPixelsOfValidLabels(int number_of_valid_labels, int max_label) {
		Vector<Integer>[] result = new Vector[number_of_valid_labels];
        int index = 0;
        
        for ( int i = 0; i < result.length; i++ ) {
            result[i] = new Vector<Integer>();
            while ( index <= max_label ) {
                if ( _label_validity[index] ) { break; }
                index++;
            }
            if ( index <= max_label ) {
                for ( int p : _constituent_pixels_of_each_label[index] ) { result[i].add(p); }
                index++;
            } else { break; }
        }
        
        return result;
	}
	
	/**
	 * ラベリングされた連結成分の内、画像の縁に接しているものと、サイズが閾値以下のものを削除する。
	 * @param max_label
	 * @return : 残った （画像の縁に接しておらず、サイズが閾値以上の） ラベリングされた連結成分の数
	 */
	protected int eliminateLabelsOnImageCornerAndSmallLabels(int max_label) {
		int number_of_valid_labels = 0;
        _label_validity = new boolean[max_label + 1];
        
        for ( int i = 0 ; i < max_label + 1; i++ ) {
            if ( _constituent_pixels_of_each_label[i].size() > _size_threshold ) {
                if ( !_corner_cut ) {
                    _label_validity[i] = true;
                    number_of_valid_labels++;
                } else {
                    if ( !eliminateLabelsOnImageCorner(i) ) { number_of_valid_labels++; }
                }
            } else { _label_validity[i] = false; }
        }
        return number_of_valid_labels;
	}
	
	/**
	 * あるラベリングされた連結成分が画像の縁に接している場合、その連結成分を除外する。
	 * _label_validity[i] = false とする。
	 * @param i
	 * @return : 除外する場合、TRUEを返す。
	 */
	protected boolean eliminateLabelsOnImageCorner(int i) {
		_label_validity[i] = true;
        for ( int p : _constituent_pixels_of_each_label[i] ) {
            if ( onImageCorner(p) ) {
                _label_validity[i] = false;
                return true;
            }
        }
        return false;
	}
	
	/**
	 * あるラベリングされた連結成分が、画像の縁に接している場合TRUEを返す。
	 * @param p
	 * @return
	 */
	protected boolean onImageCorner(int p) {
		if ( p < _width || p >= _width * (_height - 1) || p % _width <= 0 || p % _width >= _width - 1 ) { return true; }
		else { return false; }
	}
}
