//--------------------------------------
// SCMD Project
//
// CalMorphOption.java
// Since: 2007/01/09
//
// $URL$ 
// $Author$
//--------------------------------------
package lab.cb.scmd.calmorph;

/**
 * This class holds the information required to run CalMorph,
 * e.g. output directory, input photo directory, etc.
 *
 * @author leo
 */
public class CalMorphOption {
    String orfName;
    String outputDirectory;
    String inputDirectory;
    String xmlOutputDirectory;
    int maxImageNumber = 0;
    private boolean calD = true;
    private boolean calA = false;
    private boolean outimage = true;

    public CalMorphOption() {

    }

    // inputDirectory
    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    // outputDirectory
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    // xmlOutputDirectory
    public void setXmlOutputDirectory(String xmlOutputDirectory) {
        this.xmlOutputDirectory = xmlOutputDirectory;
    }

    public String getXmlOutputDirectory() {
        return xmlOutputDirectory;
    }

    // maxImageNumber
    public void setMaxImageNumber(int maxImageNumber) {
        this.maxImageNumber = maxImageNumber;
    }

    public int getMaxImageNumber() {
        return maxImageNumber;
    }


    // orfName
    public void setOrfName(String orfName) {
        this.orfName = orfName;
    }

    public String getOrfName() {
        return orfName;
    }

    // calD
    public void setCalD(boolean calD) {
        this.calD = calD;
    }

    public boolean getCalD() {
        return calD;
    }

    // calA
    public void setCalA(boolean calA) {
        this.calA = calA;
    }

    public boolean getCalA() {
        return calA;
    }

    // outimage
    public void setOutimage(boolean outimage) {
        this.outimage = outimage;
    }

    public boolean getOutimage() {
        return outimage;
    }
}




