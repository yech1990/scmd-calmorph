package lab.cb.scmd.calmorph;

import java.util.Vector;

import lab.cb.scmd.calmorph2.Labeling;

public class EdgeDetection {
	
	private int _width, _height, _size;
	private int[] _points, _points_2, _original_points;
	private Vector<Integer>[] _labeled, _labeled_2;
	private final static boolean _black = true;
	private final static boolean _white = false;
	
	Cell[] _cells;
	int[] _labels_of_each_pixel, _labels_of_each_pixel_2;
	
	public EdgeDetection(int width, int size, int[] points, int[] original_points) {
		_width = width;
		_height = size / width;
		_size = size;
		
		_points = new int[points.length];
		for ( int i = 0; i < _points.length; i++ ) { _points[i] = points[i]; }
		
		_points_2 = new int[points.length];
		
		_original_points = new int[original_points.length];
		for ( int i = 0; i < _original_points.length; i++ ) { _original_points[i] = original_points[i]; }
		
		_labels_of_each_pixel = new int[size];
		_labels_of_each_pixel_2 = new int[size];
	}
	
	public Cell[] getCells() {
		return _cells;
	}
	
	public int[] getLabelsOfEachPixel() {
		return _labels_of_each_pixel;
	}
	
	public int[] getLabelsOfEachPixel2() {
		return _labels_of_each_pixel_2;
	}
	
	public void edge(int startid) {
		Labeling lab = new Labeling(_width, _size, 200, true);  //２００以上の塊を細胞とみなす
		_labeled = lab.label(Segmentation.convertBinaryIntPointsToBinaryBoolean(_points), _black);
		
		initializeCellAndLabelsOfEachPixels(startid);
		removeBlackSurroundedPixel();
		resetPointsToBoundaryOnly();
		
		removeBlackSurroundedOrAnotherLabledPixel();
		initializePoints2AndLabelsOfEachPixel2();
		initializeLabeled2();
		
		correctEdge1();
		
		for(int i=0;i<_size;i++) {
			if(_labels_of_each_pixel[i] != -1){
				_cells[_labels_of_each_pixel[i]].cover.add(new Integer(i));
			}
		}
		boolean[] check = new boolean[_size];
		for(int i=0;i<_size;i++) {
			check[i] = true;
		}
		for(int i=0;i<_cells.length;i++) {
			int p=((Integer)_labeled[i].get(0)).intValue();
			if(nextpoint(_points,i,p,check,p, _labels_of_each_pixel, _cells, _width) && _labeled[i].size() == _cells[i].edge.size()) {
			} else {
				_cells[i].budcrush = 2;
			}
		}
		for(int i=0;i<_cells.length;i++) {//一点だけ極端に明るい細胞をcomplexに 澤井追加部分
			if(_cells[i].edge.size()>0){
				int[] br = new int[256];
				for(int j=0;j<256;j++) br[j]=0;
				for(int j=0;j<_cells[i].edge.size();j++) {
					int p=((Integer)_cells[i].edge.get(j)).intValue();
					br[_original_points[p]]++;
				}
				int r=0;
				int s=255;
				while(r<=_cells[i].edge.size()/20){
					r+=br[s];
					s--;
				}
				int q=0;
				r=0;
				int t=0;
				for(int j=0;j<_cells[i].edge.size();j++) {
					int p=((Integer)_cells[i].edge.get(j)).intValue();
					if(_original_points[p]<=s){
						q+=_original_points[p];
						t++;
					}
					else{
						r+=_original_points[p];
					}
				}
				if(t>0) q=q/t;
				if(r>0 && (_cells[i].edge.size() - t > 0)) r=r/(_cells[i].edge.size() - t);
				if(q<50 && r>100 && r-q>70) _cells[i].setGroup(0);
			}
		}
		edgecorrect2(_labeled_2,_points_2,_original_points, _labels_of_each_pixel_2, _cells, _width);
		for(int i=0;i<_size;i++) {
			if(_labels_of_each_pixel_2[i] != -1){
				_cells[_labels_of_each_pixel_2[i]].cover_2.add(new Integer(i));
			}
		}
		for(int i=0;i<_size;i++) {
			check[i] = true;
		}
		for(int i=0;i<_cells.length;i++) {
			int p=((Integer)_labeled_2[i].get(0)).intValue();
			if ( nextpoint2(_points_2,i,p,check,p, _labels_of_each_pixel_2, _cells, _width) && _labeled_2[i].size() == _cells[i].edge_2.size() ) {
			} else {
				_cells[i].setGroup(0);
			}
		}
	}
	
	/**
	 * _cells[] と _labels_of_each_pixel[] 2種 の初期化
	 * @param startid
	 */
	protected void initializeCellAndLabelsOfEachPixels(int startid) {
		_cells = new Cell[_labeled.length];
		for ( int i = 0; i < _labeled.length; i++ ) {
			_cells[i] = new Cell(_width,_height,startid+i);
			_cells[i].setGroup(1);
		}
		
		for ( int i = 0; i < _size; i++ ) {
			_labels_of_each_pixel[i] = -1;
			_labels_of_each_pixel_2[i] = -1;
		}
	}
	
	/**
	 * _labeled[]から ４近傍全て黒のpixelを除く。
	 * _labeled[]を、Cellの境界を構成するpixelのみを含む様にする。
	 */
	protected void removeBlackSurroundedPixel() {
		for ( int i = 0; i < _labeled.length; i++ ) {
			for ( int j = 0; j < _labeled[i].size(); j++ ) {
				int p = ((Integer)_labeled[i].get(j)).intValue();
				_labels_of_each_pixel[p] = i;
				if ( _points[p - _width] == 0 && _points[p - 1] == 0 && _points[p + 1] == 0 && _points[p + _width] == 0) {
					_labeled[i].remove(j);
					j--;
				}
			}
		}
	}
	
	/**
	 * Cellの境界のみ黒、他は白に _points[]をリセット。
	 *   _labeled : Cellの境界を構成するpixelのみ含むVector
	 */
	protected void resetPointsToBoundaryOnly() {
		for ( int i = 0; i < _points.length; i++ ) { _points[i] = 255; }
		for ( int i = 0; i < _labeled.length; i++ ) {
			for(int p : _labeled[i] ) { _points[p] = 0; }
		}
	}
	
	/**
	 * _labeled[]から、4近傍全て黒or別番号label、のpixelを除く。
	 * _labeled[]を、Cell内部（輪郭より内側）に接しているpixelのみを含む様にする。
	 */
	protected void removeBlackSurroundedOrAnotherLabledPixel() {
		for ( int i = 0; i < _labeled.length; i++ ) {
			for ( int j = 0; j < _labeled[i].size(); j++ ) {
				int p = ((Integer)_labeled[i].get(j)).intValue();
				if ( ( _points[p - _width] == 0 || _labels_of_each_pixel[p - _width] != i ) && 
					 ( _points[p - 1] == 0      || _labels_of_each_pixel[p - 1] != i ) && 
				     ( _points[p + 1] == 0      || _labels_of_each_pixel[p + 1] != i ) && 
				     ( _points[p + _width] == 0 || _labels_of_each_pixel[p + _width] != i ) ) {
					_labeled[i].remove(j);
					j--;
					_points[p] = 255;
					_labels_of_each_pixel[p] = -1;
				}
			}
		}
	}
	
	/**
	 * _points_2[] と _labels_of_each_pixel_2[] の初期化
	 */
	protected void initializePoints2AndLabelsOfEachPixel2() {
		for ( int j = 0; j < _size; j++ ) {
			_points_2[j] = _points[j];
			_labels_of_each_pixel_2[j] = _labels_of_each_pixel[j];
		}
	}
	
	/**
	 * _labeled_2の初期化
	 */
	protected void initializeLabeled2() {
		_labeled_2 = new Vector[_labeled.length];
		for ( int i = 0; i < _labeled.length; i++ ) {
			_labeled_2[i] = new Vector<Integer>();
			for ( int p : _labeled[i] ) { _labeled_2[i].add( new Integer(p) ); }
		}
	}
	
	protected void correctEdge1() {
		int counter;
		int brightness;
		int x,n,m,k,flag,flag2,ori,mopoint;
		boolean[] move = new boolean[_size];
		for(int i=0;i<_labeled.length;i++) {
			int j=0;
			ori=_labeled[i].size();
			flag = 0;
			flag2 = 0;
			while(j<_labeled[i].size()) {
				if((_labeled[i].size()-j)*4<ori) flag++;
				n = _labeled[i].size();
				m = j;
				mopoint = 0;
				for(int a=0;a<_size;a++) move[a] = false;
				while(j<n){
					int p=((Integer)_labeled[i].get(j)).intValue();
					counter=0;
					brightness=0;
					x=0;
					if(_labels_of_each_pixel[p-1] == i && _points[p-1] == 255) x -= 1;
					if(_labels_of_each_pixel[p+1] == i && _points[p+1] == 255) x += 1;
					if(_labels_of_each_pixel[p-_width] == i && _points[p-_width] == 255) x -= _width;
					if(_labels_of_each_pixel[p+_width] == i && _points[p+_width] == 255) x += _width;
					if(_labels_of_each_pixel[p+x] == i && _points[p+x] == 255) {
						brightness = _original_points[p+x];
					}
					if(p+x*2 >= 0 && p+x*2 < _size && _labels_of_each_pixel[p+x*2] == i && _points[p+x*2] == 255 && brightness < _original_points[p+x*2]) {
						brightness = _original_points[p+x*2];
					}
					if(p+x*3 >= 0 && p+x*3 < _size && _labels_of_each_pixel[p+x*3] == i && _points[p+x*3] == 255 && brightness < _original_points[p+x*3]) {
						brightness = _original_points[p+x*3];
					}
					if(_original_points[p] < brightness){
					move[p] = true;
					}
					j++;
				}
				while(m<n){
					int p=((Integer)_labeled[i].get(m)).intValue();
					if(move[p] && (move[p-_width-1] || move[p-_width] || move[p-_width+1] || move[p-1] || move[p+1] || move[p+_width-1] || move[p+_width] || move[p+_width+1])){
						_labels_of_each_pixel[p] = -1;
						_points[p] = 255;
						_labeled[i].remove(m);
						m--;
						j--;
						n--;
						mopoint++;
						if(_labels_of_each_pixel[p-1] == i && _points[p-1] == 255){
							_points[p-1] = 0;
							_labeled[i].add(new Integer(p-1));
						}
						if(_labels_of_each_pixel[p+1] == i && _points[p+1] == 255){
							_points[p+1] = 0;
							_labeled[i].add(new Integer(p+1));
						}
						if(_labels_of_each_pixel[p-_width] == i && _points[p-_width] == 255){
							_points[p-_width] = 0;
							_labeled[i].add(new Integer(p-_width));
						}
						if(_labels_of_each_pixel[p+_width] == i && _points[p+_width] == 255){
							_points[p+_width] = 0;
							_labeled[i].add(new Integer(p+_width));
						}
					}
					m++;
				}
				boolean change = true;
				while(change) {
					change = false;
					for(k=0;k<_labeled[i].size();k++){
						int p=((Integer)_labeled[i].get(k)).intValue();
						if(!((_labels_of_each_pixel[p-_width] == i && _points[p-_width] == 255) || (_labels_of_each_pixel[p-1] == i && _points[p-1] == 255) || (_labels_of_each_pixel[p+1] == i && _points[p+1] == 255) || (_labels_of_each_pixel[p+_width] == i && _points[p+_width] == 255)) && (_labeled[i].size() > 1)){
							_points[p] = 255;
							_labeled[i].remove(k);
							_labels_of_each_pixel[p] = -1;
							if(k<j) j--;
							k--;
						}
					}
					for(k=0;k<_labeled[i].size();k++){
						int p=((Integer)_labeled[i].get(k)).intValue();
						int c=0;
						x=0;
						if(_labels_of_each_pixel[p-_width] == -1) c++;
						else x=-_width;
						if(_labels_of_each_pixel[p+_width] == -1) c++;
						else x=_width;
						if(_labels_of_each_pixel[p-1] == -1) c++;
						else x=-1;
						if(_labels_of_each_pixel[p+1] == -1) c++;
						else x=1;
						if(c==3) {
							_points[p] = 255;
							_labeled[i].remove(k);
							_labels_of_each_pixel[p] = -1;
							if(k<j) j--;
							k--;
							_points[p+x] = 0;
							_labeled[i].add(new Integer(p+x));
							change = true;
						}
					}
				}
				if(mopoint<4) flag2++;
			}
			if(flag2>3) _cells[i].budcrush = 2;
			else if(flag>2) _cells[i].budcrush = 1;
			n=_labeled[i].size();
			for(k=0;k<n;k++){
				int p=((Integer)_labeled[i].get(k)).intValue();
					if(p%_width>1 && _labels_of_each_pixel[p-1] == -1 && _points[p-1] == 255){
						_labels_of_each_pixel[p-1] = i;
						_points[p-1] = 0;
						_labeled[i].add(new Integer(p-1));
					}
					if(p%_width < _width-1 && _labels_of_each_pixel[p+1] == -1 && _points[p+1] == 255){
						_labels_of_each_pixel[p+1] = i;
						_points[p+1] = 0;
						_labeled[i].add(new Integer(p+1));
					}
					if(p-_width >= _width && _labels_of_each_pixel[p-_width] == -1 && _points[p-_width] == 255){
						_labels_of_each_pixel[p-_width] = i;
						_points[p-_width] = 0;
						_labeled[i].add(new Integer(p-_width));
					}
					if(p+_width < _size-_width && _labels_of_each_pixel[p+_width] == -1 && _points[p+_width] == 255){
						_labels_of_each_pixel[p+_width] = i;
						_points[p+_width] = 0;
						_labeled[i].add(new Integer(p+_width));
					}
			}
			for(k=0;k<_labeled[i].size();k++){
				int p=((Integer)_labeled[i].get(k)).intValue();
				if(p-_width >= 0 && p+_width < _size){
				if((_labels_of_each_pixel[p-_width] == i) && (_labels_of_each_pixel[p-1] == i) && (_labels_of_each_pixel[p+1] == i) && (_labels_of_each_pixel[p+_width] == i) && (_labeled[i].size() > 1)){
					_points[p] = 255;
					_labeled[i].remove(k);
					k--;
				}
				}
			}
			for(k=0;k<_labeled[i].size();k++){
				int p=((Integer)_labeled[i].get(k)).intValue();
				if(p-_width >= 0 && p+_width < _size){
				if(!((_labels_of_each_pixel[p-_width] == i && _points[p-_width] == 255) || (_labels_of_each_pixel[p-1] == i && _points[p-1] == 255) || (_labels_of_each_pixel[p+1] == i && _points[p+1] == 255) || (_labels_of_each_pixel[p+_width] == i && _points[p+_width] == 255)) && (_labeled[i].size() > 1)){
					_points[p] = 255;
					_labeled[i].remove(k);
					_labels_of_each_pixel[p] = -1;
					k--;
				}
				}
			}
		}
	}
	
    private static boolean nextpoint(int[] grey,int i,int p,boolean[] check,int start, int[] pixeltocell, Cell[] cell, int _width) {
        cell[i].edge.add(new Integer(p));
        //System.out.println("set:"+p);
        check[p] = false;
        if(grey[p-1] == 0 && pixeltocell[p-1] == i && check[p-1]) {
            if(nextpoint(grey,i,p-1,check,start, pixeltocell, cell, _width)) {
                return true;
            }
        }
        if(grey[p+1] == 0 && pixeltocell[p+1] == i && check[p+1]) {
            if(nextpoint(grey,i,p+1,check,start, pixeltocell, cell, _width)) {
                return true;
            }
        }
        if(grey[p-_width] == 0 && pixeltocell[p-_width] == i && check[p-_width]) {
            if(nextpoint(grey,i,p-_width,check,start, pixeltocell, cell, _width)) {
                return true;
            }
        }
        if(grey[p+_width] == 0 && pixeltocell[p+_width] == i && check[p+_width]) {
            if(nextpoint(grey,i,p+_width,check,start, pixeltocell, cell, _width)) {
                return true;
            }
        }
        if(grey[p-_width-1] == 0 && pixeltocell[p-_width-1] == i && check[p-_width-1]) {
            if(nextpoint(grey,i,p-_width-1,check,start, pixeltocell, cell, _width)) {
                return true;
            }
        }
        if(grey[p-_width+1] == 0 && pixeltocell[p-_width+1] == i && check[p-_width+1]) {
            if(nextpoint(grey,i,p-_width+1,check,start, pixeltocell, cell, _width)) {
                return true;
            }
        }
        if(grey[p+_width-1] == 0 && pixeltocell[p+_width-1] == i && check[p+_width-1]) {
            if(nextpoint(grey,i,p+_width-1,check,start, pixeltocell, cell, _width)) {
                return true;
            }
        }
        if(grey[p+_width+1] == 0 && pixeltocell[p+_width+1] == i && check[p+_width+1]) {
            if(nextpoint(grey,i,p+_width+1,check,start, pixeltocell, cell, _width)) {
                return true;
            }
        }
        if ( nextTo(p, start, _width) && cell[i].edge.size() > 10 ) { return true; }
        else {
            //System.out.println(((Integer)cell[i].edge.get(cell[i].edge.size()-1)).intValue());
            cell[i].edge.remove(cell[i].edge.size()-1);
            check[p] = true;
            return false;
        }
    }
    
    private static boolean nextpoint2(int[] grey,int i,int p,boolean[] check,int start, int[] pixeltocell2, Cell[] cell, int _width) {
        cell[i].edge_2.add(new Integer(p));
        check[p] = false;
        if(grey[p-1] == 0 && pixeltocell2[p-1] == i && check[p-1]) {
            if(nextpoint2(grey,i,p-1,check,start, pixeltocell2, cell, _width)) {
                return true;
            }
        }
        if(grey[p+1] == 0 && pixeltocell2[p+1] == i && check[p+1]) {
            if(nextpoint2(grey,i,p+1,check,start, pixeltocell2, cell, _width)) {
                return true;
            }
        }
        if(grey[p-_width] == 0 && pixeltocell2[p-_width] == i && check[p-_width]) {
            if(nextpoint2(grey,i,p-_width,check,start, pixeltocell2, cell, _width)) {
                return true;
            }
        }
        if(grey[p+_width] == 0 && pixeltocell2[p+_width] == i && check[p+_width]) {
            if(nextpoint2(grey,i,p+_width,check,start, pixeltocell2, cell, _width)) {
                return true;
            }
        }
        if(grey[p-_width-1] == 0 && pixeltocell2[p-_width-1] == i && check[p-_width-1]) {
            if(nextpoint2(grey,i,p-_width-1,check,start, pixeltocell2, cell, _width)) {
                return true;
            }
        }
        if(grey[p-_width+1] == 0 && pixeltocell2[p-_width+1] == i && check[p-_width+1]) {
            if(nextpoint2(grey,i,p-_width+1,check,start, pixeltocell2, cell, _width)) {
                return true;
            }
        }
        if(grey[p+_width-1] == 0 && pixeltocell2[p+_width-1] == i && check[p+_width-1]) {
            if(nextpoint2(grey,i,p+_width-1,check,start, pixeltocell2, cell, _width)) {
                return true;
            }
        }
        if(grey[p+_width+1] == 0 && pixeltocell2[p+_width+1] == i && check[p+_width+1]) {
            if(nextpoint2(grey,i,p+_width+1,check,start, pixeltocell2, cell, _width)) {
                return true;
            }
        }
        if(nextTo(p, start, _width) && cell[i].edge_2.size() > 10) return true;
        else {
            cell[i].edge_2.remove(cell[i].edge_2.size()-1);
            check[p] = true;
            return false;
        }
    }
    
    private static boolean nextTo(int p,int q, int _width) {
        if(p == q-_width-1 || p == q-_width || p == q-_width+1 || p == q-1 || p == q+1 || 
        		p == q+_width-1 || p == q+_width || p == q+_width+1) return true;
        else return false;
    }
    
	private static void edgecorrect2(Vector[] vec,int[] image,int[] oriimage, int[] pixeltocell2, Cell[] cell, int _width) {
		int _size = image.length;
	    int brightness1,brightness2;
		int x,n,m,k,flag,ori;
		boolean[] move = new boolean[_size];
		double counter2 = 0;//ぼやけ画像除去のためのカウンタ　澤井追加部分
	    for(int i=0;i<vec.length;i++) {
	    	int j=0;
	        while(j<vec[i].size()) {
	        	n = vec[i].size();
	        	m = j;
	        	for(int a=0;a<_size;a++) move[a] = false;
	        	while(j<n){
	                int p=((Integer)vec[i].get(j)).intValue();
	    	    	brightness1=-1;
	        		brightness2=0;
	        		x=0;
	        		int xx=1;
	                if(pixeltocell2[p-1] == i && image[p-1] == 255) x -= 1;
		            if(pixeltocell2[p+1] == i && image[p+1] == 255) x += 1;
	    	        if(pixeltocell2[p-_width] == i && image[p-_width] == 255) x -= _width;
	        	    if(pixeltocell2[p+_width] == i && image[p+_width] == 255) x += _width;
	        	    if(p+x*2 >= 0 && p+x*2 < _size && pixeltocell2[p+x*2] == i && image[p+x*2] == 255
	        	    && p-x*2 >= 0 && p-x*2 < _size && pixeltocell2[p-x*2] == -1 && image[p+x*2] == 255) {
	        		    brightness1 = oriimage[p+x*2] - oriimage[p];
	        	    	brightness2 = oriimage[p] - oriimage[p-x*2];
	        	    	xx = 2;
	        	    }
	        		else if(pixeltocell2[p+x] == i && image[p+x] == 255 && pixeltocell2[p+x] == -1 && image[p+x] == 255) {
	    				brightness1 = oriimage[p+x] - oriimage[p];
	        			brightness2 = oriimage[p] - oriimage[p-x];
	        			xx = 1;
	        		}
	            	if(brightness1 >= brightness2 || oriimage[p-x*(xx-1)] - oriimage[p-x*xx] < 5){ // 追加
	                move[p] = true;
	            	}
	        		j++;
	        	}
	        	while(m<n){
	        		int p=((Integer)vec[i].get(m)).intValue();
	        		if(move[p] && (move[p-_width-1] || move[p-_width] || move[p-_width+1] || move[p-1] || move[p+1] || move[p+_width-1] || move[p+_width] || move[p+_width+1])){
	        			pixeltocell2[p] = -1;
	        			image[p] = 255;
	                	vec[i].remove(m);
	                	m--;
	        			j--;
	        			n--;
	                	if(pixeltocell2[p-1] == i && image[p-1] == 255){
	                		image[p-1] = 0;
	                		vec[i].add(new Integer(p-1));
	                	}
	                	if(pixeltocell2[p+1] == i && image[p+1] == 255){
	                		image[p+1] = 0;
	                		vec[i].add(new Integer(p+1));
	                	}
	                	if(pixeltocell2[p-_width] == i && image[p-_width] == 255){
	                		image[p-_width] = 0;
	                		vec[i].add(new Integer(p-_width));
	                	}
	                	if(pixeltocell2[p+_width] == i && image[p+_width] == 255){
	                		image[p+_width] = 0;
	                		vec[i].add(new Integer(p+_width));
	                	}
	        		}
	        		m++;
	            }
	        	boolean change = true;
	        	while(change) {
	        		change = false;
	        		for(k=0;k<vec[i].size();k++){
	        			int p=((Integer)vec[i].get(k)).intValue();
	        			if(!((pixeltocell2[p-_width] == i && image[p-_width] == 255) || (pixeltocell2[p-1] == i && image[p-1] == 255) || (pixeltocell2[p+1] == i && image[p+1] == 255) || (pixeltocell2[p+_width] == i && image[p+_width] == 255)) && (vec[i].size() > 1)){
	        				image[p] = 255;
	        				vec[i].remove(k);
	        				pixeltocell2[p] = -1;
	        				if(k<j) j--;
	        				k--;
	        			}
	        		}
	        		for(k=0;k<vec[i].size();k++){
	        			int p=((Integer)vec[i].get(k)).intValue();
	        			int c=0;
	        			x=0;
	        			if(pixeltocell2[p-_width] == -1) c++;
	        			else x=-_width;
	        			if(pixeltocell2[p+_width] == -1) c++;
	        			else x=_width;
	        			if(pixeltocell2[p-1] == -1) c++;
	        			else x=-1;
	        			if(pixeltocell2[p+1] == -1) c++;
	        			else x=1;
	        			if(c==3) {
	        				image[p] = 255;
	        				vec[i].remove(k);
	        				pixeltocell2[p] = -1;
	        				if(k<j) j--;
	        				k--;
	        	        	image[p+x] = 0;
	        	        	vec[i].add(new Integer(p+x));
	        				change = true;
	        			}
	        		}
	        	}
	        }
			double counter = 0;//この先、ぼやけ画像除去の操作　澤井追加部分
			for(k=0;k<vec[i].size();k++){
				int p=((Integer)vec[i].get(k)).intValue();
				x=0;
				if(pixeltocell2[p-1] != i && image[p-1] == 255) x -= 1;
				if(pixeltocell2[p+1] != i && image[p+1] == 255) x += 1;
				if(pixeltocell2[p-_width] != i && image[p-_width] == 255) x -= _width;
				if(pixeltocell2[p+_width] != i && image[p+_width] == 255) x += _width;
				if(p+x*2 >= 0 && p+x*2 < _size && p-x >= 0 && p-x < _size && oriimage[p] - oriimage[p+x*2] > oriimage[p-x] - oriimage[p+x]) counter += (double)(oriimage[p] - oriimage[p+x*2]) / (double)oriimage[p];
				else counter += (double)(oriimage[p-x] - oriimage[p+x]) / (double)oriimage[p-x];
			}
			counter /= (double)vec[i].size();
			counter2 += counter;
	    }
		if(counter2 / (double)vec.length < 0.46){
			 //err = true;
			 //err_kind = "blur of cell image";
		}
	}
}
