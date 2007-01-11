package lab.cb.scmd.calmorph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class TemporaryIO {
	
	private static final String _parameter = "len_05_moth_02_bud_15_";
	
	/**
	 * temporary SCMD main method
     * 細胞壁の写真を含むディレクトリ名を引数にして実行。
     * ネック検出部分のみRefactoring＋コード追加（二倍体レモン型偽ネック回避）。
     * ネック検出後、検出結果を画像に出力。
	 * @param args
	 */
	public static void main(String[] args) {
		TemporaryIO io = new TemporaryIO();
		io.execute(args[0]);
		System.out.println("SCMD END");
	}
	
	private void execute(final String dir_name) { 
        String[] image_names = directoryList(dir_name);
        for ( String image : image_names ) {
            String[] splited = image.split("-");
            String[] number = splited[splited.length - 1].split("\\.");
            CellImage ci = new CellImage(image, dir_name + "/" + splited[0], 
            		Integer.valueOf( number[0].substring(1, number[0].length()) ), "result", 0, false, false);
        	ci.segmentCells();
        }
    }
	
	private String[] directoryList(final String dir_name) {
        File directory = new File(dir_name);
        
        assert directory.exists() : "Source directory " + directory.getName() + " does not exist.";
        assert directory.isDirectory() : "Source directory " + directory.getName() + " is not a directory.";
        
        String[] result = directory.list();
        Arrays.sort(result);
        
        return result;
    }
	
	public static void drawImage(int[] points, final int width, final Cell[] cells, final String filename, final String out_dir) {
    	for ( int i = 0; i < cells.length; i++ ) {
    		if ( cells[i].neck == null ) { continue; }
    		if ( cells[i].neck.length == 2 ) {
    			for ( int neck : cells[i].neck ) { drawPoint(neck, points, width, 0x00ffff); }
    		}/*
    		for ( int j = 0; j < cells[i].mother_edge.size(); j++ ) {
    			points[( (Integer)cells[i].mother_edge.get(j) ).intValue()] = 0xffffff;
    		}*/
    		for ( int j = 0; j < cells[i].bud_edge.size(); j++ ) {
    			points[( (Integer)cells[i].bud_edge.get(j) ).intValue()] = 0xff0000;
    		}/*
    		if ( cells[i].neck_and_bud_middle != null ) {
    			drawPoint(cells[i].neck_and_bud_middle[0], points, width, 0x00ff00);
    			drawPoint(cells[i].neck_and_bud_middle[2], points, width, 0x00ff00);
    		}
    		int x = (int)(cells[i].grad_cept_middle[2]) % width;
    		for ( int j = x - 10; j < x + 11; j++ ) {
    			int y = (int)( cells[i].grad_cept_middle[0] * j + cells[i].grad_cept_middle[1] );
    			points[y * width + j] = 0xff0000;
    		}*/
    	}
        BufferedImage bi = makeBufferedImage(points, width);
        Iterator writers = ImageIO.getImageWritersBySuffix(getSuffix(filename));
        if ( writers.hasNext() ) {
            ImageWriter writer = (ImageWriter) writers.next();
            try {
                ImageOutputStream stream = ImageIO.createImageOutputStream(new File(out_dir + "/" + _parameter + filename));
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
	
    private static void drawPoint(int p, int[] points, int width, int color) {
    	points[p - width - 1] = color;
		points[p - width]     = color;
		points[p - width + 1] = color;
		points[p - 1]         = color;
		points[p]             = color;
		points[p + 1]         = color;
		points[p + width - 1] = color;
		points[p + width]     = color;
		points[p + width + 1] = color;
    }
    
    private static BufferedImage makeBufferedImage(final int[] points, final int width) {
		BufferedImage bi = new BufferedImage(width, (points.length / width), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		for( int i = 0; i < points.length; i++ ) {
			g.setColor(new Color((points[i] << 16) | (points[i] << 8) | points[i]));
			g.drawLine(i % width, i / width, i % width, i / width);
		}
		return bi;
    }
    
    private static String getSuffix(final String filename) {
        if ( filename.length() < 3 ) {
            System.err.println("getSuffix -- Error");
            System.exit(1);
        } else { return filename.substring(filename.length() - 3); }
        return null;
    }
    
}
