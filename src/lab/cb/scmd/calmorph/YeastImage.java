package lab.cb.scmd.calmorph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import lab.cb.scmd.calmorph2.CalmorphCommon;

public class YeastImage {
	
	int _width, _height, _size, _satrt_id;
	int[] _points, _original_points;
	String _image_type;
	Cell[] _cell;
	
	public YeastImage(String name, String image_type, BufferedImage bi, int start_id) {
		_image_type = image_type;
		
		_width = bi.getWidth();
		_height = bi.getHeight();
		DataBuffer db = bi.getRaster().getDataBuffer();
		_size = db.getSize();
		
		if (_size != _width * _height) { CalmorphCommon.errorExit("CellWallImage.CellWallImage()", "size != width * height"); }
		_satrt_id = start_id;
		
		_original_points = new int[_size];
        for ( int i = 0; i < _original_points.length; i++ ) { _original_points[i] = db.getElem(i); }
	}
	
	public YeastImage(int w, int h, int[] points) {
		_width = w;
		_height = h;
		_size = w * h;
		_points = new int[points.length];
		for ( int i = 0; i < _points.length; i++ ) { _points[i] = points[i]; }
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
	
	public void drawImage(final String filename) {
        BufferedImage bi = makeBufferedImage();
        Iterator writers = ImageIO.getImageWritersBySuffix(getSuffix(filename));
        if ( writers.hasNext() ) {
            ImageWriter writer = (ImageWriter) writers.next();
            try {
                ImageOutputStream stream = ImageIO.createImageOutputStream(new File(filename));
                writer.setOutput(stream);
                
                ImageWriteParam param = writer.getDefaultWriteParam();
                if ( param.canWriteCompressed() ) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(1.0f);
                } else { System.out.println("Compression is not supported."); }
                
                writer.write(null, new IIOImage(bi, null, null), param);
                stream.close();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    protected BufferedImage makeBufferedImage() {
		BufferedImage bi = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		for( int i = 0; i < _size; i++ ) {
			g.setColor(new Color((_points[i] << 16) | (_points[i] << 8) | _points[i]));
			g.drawLine(i % _width, i / _width, i % _width, i / _width);
		}
		return bi;
    }
    
    protected String getSuffix(final String filename) {
        if ( filename.length() < 3 ) {
            System.err.println("getSuffix -- Error");
            System.exit(1);
        } else { return filename.substring(filename.length() - 3); }
        return null;
    }
    
    public void ploEdgePoints(Cell[] cell) {
    	int color = 0xff0000;
    	if ( cell == null ) { System.out.println("no cell"); }
    	for ( int i = 0 ; i < cell.length; i++ ) {
    		if ( cell[i] == null ) {System.out.println("no cell["+ i + "]");}
    		if ( cell[i].edge == null ) {System.out.println("no cell["+ i + "].neck");}
    		Vector<Integer> edge = cell[i].edge;
    		for ( int p : edge ) {
    			//_points[p - 1] = color;
    			_points[p] = color;
    			//_points[p + 1] = color;
    			//_points[p - 1 - _width] = color;
    			//_points[p - _width] = color;
    			//_points[p + 1 - _width] = color;
    			//_points[p - 1 + _width] = color;
    			//_points[p + _width] = color;
    			//_points[p + 1 + _width] = color;
    		}
    	}
    }
    
}
