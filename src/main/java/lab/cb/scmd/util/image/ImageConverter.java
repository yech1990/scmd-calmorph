//--------------------------------------
// SCMDProject
// 
// ImageConverter.java 
// Since: 2004/07/13
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.image;

import lab.cb.scmd.exception.InvalidParameterException;
import lab.cb.scmd.exception.UnfinishedTaskException;
import lab.cb.scmd.util.ProcessRunner;
import lab.cb.scmd.util.io.FileUtil;

import java.io.*;
import java.net.URL;

/**
 * @author leo
 */
public class ImageConverter {
    private File _convertProgram;

    /**
     *
     */
    public ImageConverter(File pathToImageMagickConvert) throws FileNotFoundException {
        // validate path
        _convertProgram = pathToImageMagickConvert;
        FileUtil.testExistence(_convertProgram);
    }

    public void scaleImage(PrintStream out, File imageFile, int scalingPercentage) throws FileNotFoundException,
            InvalidParameterException, UnfinishedTaskException {
        FileUtil.testExistence(imageFile);

        ProcessRunner.run(out, _convertProgram + getScalingOption(scalingPercentage) + imageFile + " -");
    }

    public void scaleImage(OutputStream out, URL imageURL, int scalingPercentage) throws
            InvalidParameterException, UnfinishedTaskException, IOException {
        ProcessRunner.run(imageURL.openStream(), out, _convertProgram + getScalingOption(scalingPercentage) + " - -");
    }

    public void clipImage(PrintStream out, File imageFile, BoundingRectangle clipRegion, int scalingPercentage,
                          int border) throws FileNotFoundException, InvalidParameterException, UnfinishedTaskException {
        FileUtil.testExistence(imageFile);

        ProcessRunner.run(out, _convertProgram + getClippingOption(clipRegion, border)
                + getScalingOption(scalingPercentage) + imageFile + " -");
    }

    public void clipImage(OutputStream out, URL imageURL, BoundingRectangle clipRegion, int scalingPercentage, int border)
            throws InvalidParameterException, UnfinishedTaskException, IOException {
        ProcessRunner.run(imageURL.openStream(), out, _convertProgram + getClippingOption(clipRegion, border) + getScalingOption(scalingPercentage) + " - -");
    }

    private void validateScalingPercentage(int scalingPercentage) throws InvalidParameterException {
        if (scalingPercentage < 0 || scalingPercentage > 200)
            throw new InvalidParameterException("invalid scaling percentage: " + scalingPercentage + "%");
    }

    /**
     * cellinfo用の命令
     *
     * @param out
     * @param imageFile  SCMDの３種類の画像ファイルのpath
     * @param clipRegion
     */
    public void clipAndCombineImages(PrintStream out, File[] imageFile, BoundingRectangle clipRegion,
                                     int scalingPercentage) throws FileNotFoundException, InvalidParameterException, UnfinishedTaskException {
        StringBuilder fileArgument = new StringBuilder();
        // TODO Even if part of the image file is missing, it should be handled so that it can be displayed properly
        for (File file : imageFile) {
            FileUtil.testExistence(file);
            fileArgument.append(" -append ").append(file);
        }

        ProcessRunner.run(out, _convertProgram + getClippingOption(clipRegion, 4) + getScalingOption(scalingPercentage)
                + fileArgument + " -");
    }

    private String getScalingOption(int scalingPercentage) throws InvalidParameterException {
        validateScalingPercentage(scalingPercentage);
        if (scalingPercentage == 100)
            return " ";
        else
            return " -scale " + scalingPercentage + "% ";

    }

    private String getClippingOption(BoundingRectangle clipRegion, int borderSize) throws InvalidParameterException {
        return " -crop " + clipRegion.getGeometry(borderSize) + " ";
    }

    public File getConvertProgram() {
        return _convertProgram;
    }
}

//--------------------------------------
// $Log: ImageConverter.java,v $
// Revision 1.9  2004/08/23 01:04:25  leo
// conflictを修正
//
//
// Revision 1.8  2004/08/14 15:22:42  leo
// *** empty log message ***
//
// Revision 1.7  2004/08/14 11:09:54  leo
// Warningの整理、もう使わなくなったクラスにdeprecatedマークを入れました
//
// Revision 1.6 2004/08/12 14:50:18 leo
// *** empty log message ***
//
// Revision 1.5 2004/08/01 08:19:36 leo
// BasicTableにhasRowLabelを追加
// XMLOutputterで、java.io.writerを使えるように変更
// （JSPのwriterがjava.io.Writerの派生クラスのため)
//
// Revision 1.4 2004/07/26 19:32:58 leo
// boudingRectangleにgetCgiArgumentを追加
//
// Revision 1.3 2004/07/21 08:07:24 leo
// *** empty log message ***
//
// Revision 1.2 2004/07/17 08:07:39 leo
// InvalidPathExceptionを取り除きました
//
// Revision 1.1 2004/07/13 08:02:47 leo
// fist ship
//
//--------------------------------------
