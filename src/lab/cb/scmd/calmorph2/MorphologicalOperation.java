package lab.cb.scmd.calmorph2;

import java.util.Vector;

import lab.cb.scmd.calmorph.Segmentation;

public class MorphologicalOperation {
	
	private int _width, _height;
	private int[] _points;  // ìÒílâÊëú Åiçï==0, îí==255Åj
	private final static boolean _black = true;
	private final static boolean _white = false;
	
	public MorphologicalOperation(int width, int[] points) {
		_width = width;
		_height = points.length / width;
		_points = new int[points.length];
		for ( int i = 0; i < _points.length; i++ ) { _points[i] = points[i]; }
	}
	
	public int[] getPoints() {
		return _points;
	}
	
    public void beforecover() {
    	Labeling lab = new Labeling(_width, _points.length, 0, true);
    	Vector<Integer>[] labeled = lab.label(Segmentation.convertBinaryIntPointsToBinaryBoolean(_points), _white);
		
    	for ( int i = 0; i < _points.length; i++ ) { _points[i] = 255; }
		
    	for ( int i = 0; i < labeled.length; i++ ) {
			for ( int p : labeled[i] ) { _points[p] = 0; }
		}
	}
    
    public void cover() {
    	Labeling lab = new Labeling(_width, _points.length, 0, false);
        Vector<Integer>[] labeled = lab.label(Segmentation.convertBinaryIntPointsToBinaryBoolean(_points), _white);
        
        int max_size = 0;
        int max_index = 0;
        for ( int i = 0; i < labeled.length; i++ ) {
            if ( max_size < labeled[i].size() ) {
                 max_size = labeled[i].size();
                 max_index = i;
             }
        }
        
        for ( int i = 0; i < labeled.length; i++ ) {
            if ( i != max_index ) {
                for ( int p : labeled[i] ) { _points[p] = 0; }
            }
        }
    }
    
    /**
     * Dilation Operation
     */
    public void dilation() {
		int[] temp = new int[_points.length];
		for ( int i = 0; i < temp.length; i++ ) { temp[i] = _points[i]; }
		
		for ( int i = 0; i < temp.length; i++ ) {
			if ( i % _width > 0 && i % _width + 1 < _width && i / _width > 0 && i / _width + 1 < _height )  {
				temp[i] &= _points[i - 1];
				temp[i] &= _points[i + 1];
				temp[i] &= _points[i - _width];
				temp[i] &= _points[i + _width];
			}
		}
		for ( int i = 0; i < _points.length; i++ ) { _points[i] = temp[i]; }
	}
    
	public void dilation2() {
		Labeling lab = new Labeling(_width, _points.length, 0, false);
        Vector[] vec = lab.label(Segmentation.convertBinaryIntPointsToBinaryBoolean(_points), _black);
		
        int[] group = new int[_points.length];
		for ( int i = 0; i < group.length; i++ ) { group[i] = -1; }
		for ( int i = 0; i < vec.length; i++ ) {
			for ( int j = 0; j < vec[i].size(); j++ ) { group[((Integer)vec[i].get(j)).intValue()] = i; }
		}
		int[] group2 = new int[_width*_height];
		for(int i=0;i<_width*_height;i++) group2[i] = -1;
		for(int i=0;i<_width*_height;i++) {
			if(i%_width>0 && i%_width<_width-1 && i/_width>0 && i/_width<_height-1){
				int gr=-1;
				boolean check = true;
				if(group[i-1]!=-1){gr = group[i-1];}
				if(group[i+1]!=-1){check &= (gr==-1||group[i+1]==gr); gr = group[i+1];}
				if(group[i-_width]!=-1){check &= (gr==-1||group[i-_width]==gr);gr = group[i-_width];}
				if(group[i+_width]!=-1){check &= (gr==-1||group[i+_width]==gr);gr = group[i+_width];}
				if(check) group2[i] = gr;
			}
		}
		for(int i=0;i<_width*_height;i++){
			if(group2[i] != -1 && (group2[i-1]==-1 || group2[i-1]==group2[i]) && (group2[i+1]==-1 || group2[i+1]==group2[i]) && 
					(group2[i-_width]==-1 || group2[i-_width]==group2[i]) && (group2[i+_width]==-1 || group2[i+_width]==group2[i])){
				_points[i] = 0;
			}
		}
	}
	
	/**
	 * Erosion Operation
	 */
    public void erosion() {
        int[] temp = new int[_points.length];
        for ( int i = 0; i < temp.length; i++ ) { temp[i] = _points[i]; }
        
        for ( int i = 0; i < temp.length; i++ ) {
            if ( i % _width > 0 )           { temp[i] |= _points[i - 1]; }
            if ( i % _width + 1 < _width )  { temp[i] |= _points[i + 1]; }
            if ( i / _width > 0 )           { temp[i] |= _points[i - _width]; }
            if ( i / _width + 1 < _height ) { temp[i] |= _points[i + _width]; }
        }
        for ( int i = 0; i < temp.length; i++ ) { _points[i] = temp[i]; }
    }
    
}
