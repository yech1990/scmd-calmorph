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
 *
 */
public class CalMorphOption
{
	String orfName;
    String outputDirectory;
    String inputDirectory;
    String xmlOutputDirectory;
    int maxImageNumber = 0;
    boolean calD = true;
    boolean calA = true;

    public CalMorphOption()
    {

    }

    public String getInputDirectory()
    {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory)
    {
        this.inputDirectory = inputDirectory;
    }

    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }

    public String getXmlOutputDirectory()
    {
        return xmlOutputDirectory;
    }

    public void setXmlOutputDirectory(String xmlOutputDirectory)
    {
        this.xmlOutputDirectory = xmlOutputDirectory;
    }

    public int getMaxImageNumber()
    {
        return maxImageNumber;
    }


    public String getOrfName() {
		return orfName;
	}

	public void setOrfName(String orfName) {
		this.orfName = orfName;
	}

	public void setMaxImageNumber(int maxImageNumber)
    {
        this.maxImageNumber = maxImageNumber;
    }

    public boolean isCalD() {
        return calD;
    }

    public void setCalD(boolean calD) {
        this.calD = calD;
    }

    public boolean isCalA() {
        return calA;
    }

    public void setCalA(boolean calA) {
        this.calA = calA;
    }
}




