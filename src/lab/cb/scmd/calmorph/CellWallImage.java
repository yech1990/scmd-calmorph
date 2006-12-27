package lab.cb.scmd.calmorph;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import lab.cb.scmd.calmorph2.CalmorphCommon;

public class CellWallImage {
	
	int _width, _height, _size, _satrt_id;
	int[] _points, _original_points;
	Cell[] _cell;
	
	public CellWallImage(String name, BufferedImage bi, int start_id) {
		_width = bi.getWidth();
		_height = bi.getHeight();
		DataBuffer db = bi.getRaster().getDataBuffer();
		_size = db.getSize();
		
		if (_size != _width * _height) { CalmorphCommon.errorExit("CellWallImage.CellWallImage()", "size != width * height"); }
		_satrt_id = start_id;
		
		_points = new int[_size];
        for ( int i = 0; i < _points.length; i++ ) { _original_points[i] = db.getElem(i); }
	}
	
	public int getWidth() {
		return _width;
	}
	
	public int getHeight() {
		return _height;
	}
	
	public int getSize() {
		return _size;
	}
	
	public int[] getOriginalPoints() {
		return _original_points;
	}
	
	public void setPoints(final int[] points) {
		_points = new int[points.length];
		for ( int i = 0; i < _points.length; i++ ) { _points[i] = points[i]; }
	}
	
	public int[] getPoints() {
		return _points;
	}
	
}
