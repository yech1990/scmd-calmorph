//--------------------------------------
// SCMD Project
// 
// ImageResizer.java 
// Since: 2004/06/14
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.image;


//import java.awt.RenderingHints;

import lab.cb.scmd.util.time.StopWatch;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

//
//import javax.media.jai.*;
//import javax.media.jai.operator.*;

/**
 * 画像の変換方式によるperformanceを見るための実験用のクラス
 *
 * @author leo
 */
public class ImageResizer {
    static public void resizeImage(String fileName) {
        StopWatch sw = new StopWatch();
        BufferedImage imageSource = null;
        try {
            imageSource = ImageIO.read(new File(fileName));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }

        int width = imageSource.getWidth();
        int height = imageSource.getHeight();

        BufferedImage scaledImage = new BufferedImage
                (width / 2, height / 2, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D scaledImageGraphics = scaledImage.createGraphics();
        scaledImageGraphics.drawImage(imageSource.getScaledInstance(width / 2, height / 2, Image.SCALE_FAST), 0, 0, null);

        sw.showElapsedTime(System.err);
        try {
            ImageIO.write(scaledImage, "jpeg", System.out);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }
        
        /*
        
        PlanarImage imageSource = JAI.create("fileload", fileName);
        
        Interpolation interpolation = Interpolation.getInstance(Interpolation.INTERP_BILINEAR);
        RenderedOp scaleOperation = ScaleDescriptor.create(imageSource, new Float(1.25), new Float(1.25), 
                new Float(0), new Float(0), interpolation, (RenderingHints) null);
        
        PlanarImage scaledImage = scaleOperation.createInstance();
        BufferedImage bufferedImage = scaledImage.getAsBufferedImage();
        
        try
        {
            ImageIO.write(scaledImage, "jpeg", System.out);
        }
        catch(IOException e)
        {
            System.err.println(e.getMessage());
            return;
        }
        */
    }

    public static void resizeImageByConvert(String fileName) {
        String cmd = "m:/Program Files/ImageMagick-5.5.6-q16/convert.exe -scale 125% " + fileName + " -";

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                //System.err.println(line);

            }
            p.waitFor();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] arg) {
        if (arg.length < 1) {
            System.err.println("input image filename");
            return;
        }
        StopWatch sp = new StopWatch();
        resizeImage(arg[0]);
        sp.showElapsedTime(System.err);
        sp.reset();
        resizeImageByConvert(arg[0]);
        sp.showElapsedTime(System.err);
    }
}


//--------------------------------------
// $Log: ImageResizer.java,v $
// Revision 1.2  2004/07/13 08:02:47  leo
// fist ship
//
// Revision 1.1  2004/06/15 06:20:01  leo
// 画像のサイズ変換用のクラス
//
//--------------------------------------