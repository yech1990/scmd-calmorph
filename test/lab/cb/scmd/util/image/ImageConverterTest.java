//--------------------------------------
// SCMDProject
// 
// ImageConverterTest.java 
// Since: 2004/07/13
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.image;

//import junit.framework.Test;
import java.io.*;
import java.util.Properties;

import lab.cb.scmd.exception.*;

import junit.framework.TestCase;
//import junit.framework.TestSuite;
//import junit.extensions.TestSetup;

/**
 * @author leo
 *
 */
public class ImageConverterTest extends TestCase
{
//    public static Test suite()
//    {
//        TestSetup setup = new TestSetup(new TestSuite(ImageConverterTest.class))
//        {
//            protected void setUp() throws 
//        };
//        return setup;
//    }

    
    String _imageMagickConvert = null;
    ImageConverter _converter = null;
    File _imageFile = new File("yor202w-C1.jpg");
    
    protected void setUp() throws Exception 
    {
        File propertyFile = new File("test.properties");
        if(!propertyFile.exists())
            fail(propertyFile + " doesn't exist. set workfolder to SCMDProject/workfolder/test" ); 
        
        // check image
        assertTrue("yor202w-A1.jpg doesn't exist. check workfolder is set to SCMDProject/workfolder/test", _imageFile.exists());

		// load a setting file
        FileInputStream input = new FileInputStream("test.properties");
        Properties property = new Properties();
        property.load(input);
        
        _imageMagickConvert = property.getProperty("IMAGEMAGICK_CONVERT");
        input.close();
        
        if(_imageMagickConvert == null)
            fail("property of IMAGEMAGICK_CONVERT doesn't exist in " + propertyFile);
		
        _converter = new ImageConverter(new File(_imageMagickConvert));
    }
    

    public void testScaleImage() throws SCMDException, IOException
    {
        PrintStream out = new PrintStream(new FileOutputStream("__scale50p1.jpg"));
        _converter.scaleImage(out, _imageFile, 50);
        out.close();
    }

    public void testClipImage() throws SCMDException, IOException
    {
        PrintStream out = new PrintStream(new FileOutputStream("__clip.jpg"));
        _converter.clipImage(out, _imageFile, new BoundingRectangle(48, 90, 34, 72), 100, 4);
        out.close();
    }

    public void testValidateScalingPercentage()
    {}

    public void testClipAndCombineImages()
    {}

    public void testGetScalingOption()
    {}

    public void testGetClippingOption()
    {}

}


//--------------------------------------
// $Log: ImageConverterTest.java,v $
// Revision 1.2  2004/07/26 19:32:58  leo
// boudingRectangleÇ…getCgiArgumentÇí«â¡
//
// Revision 1.1  2004/07/13 08:04:31  leo
// é·ä±èCê≥
//
//--------------------------------------