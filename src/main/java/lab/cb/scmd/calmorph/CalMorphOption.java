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
 * e.g. output d
 * public void setInputDirectory(String inputDirectory) {
 * this.inputDirectory = inputDirectory;
 * }
 * <p>
 * public String getOutputDirectory() {
 * return outputDirectory;
 * }
 * <p>
 * public void setOutputDirectory(String outputDirectory) {
 * this.outputDirectory = outputDirectory;
 * }
 * <p>
 * public String getXmlOutputDirectory() {
 * return xmlOutputDirectory;
 * }
 * <p>
 * public void setXmlOutputDirectory(String xmlOutputDirectory) {
 * this.xmlOutputDirectory = xmlOutputDirectory;
 * }
 * irectory, input photo directory, etc.
 *
 * @author leo
 */
class CalMorphOption {
    private String strainName;
    private String outputDirectory;
    private String inputDirectory;
    private String xmlOutputDirectory;
    private int maxImageNumber = 0;
    private boolean calD = true;
    private boolean calA = true;

    CalMorphOption() {

    }

    String getInputDirectory() {
        return inputDirectory;
    }

    void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    String getOutputDirectory() {
        return outputDirectory;
    }

    void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    String getXmlOutputDirectory() {
        return xmlOutputDirectory;
    }

    void setXmlOutputDirectory(String xmlOutputDirectory) {
        this.xmlOutputDirectory = xmlOutputDirectory;
    }

    int getMaxImageNumber() {
        return maxImageNumber;
    }

    String getStrainName() {
        return strainName;
    }

    void setStrainName(String strainName) {
        this.strainName = strainName;
    }

    void setMaxImageNumber(int maxImageNumber) {
        this.maxImageNumber = maxImageNumber;
    }

    boolean isCalD() {
        return calD;
    }

    void setCalD(boolean calD) {
        this.calD = calD;
    }

    boolean isCalA() {
        return calA;
    }

    void setCalA(boolean calA) {
        this.calA = calA;
    }
}




