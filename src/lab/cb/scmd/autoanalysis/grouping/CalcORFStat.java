// ------------------------------------
// SCMD Project
//  
// CalcORFStat.java
// Since: 2005/12/05
//
// $URL:  $ 
// $LastChangedBy: $ 
//--------------------------------------
package lab.cb.scmd.autoanalysis.grouping;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.cui.OptionParser;

/**
 * CalMorphをかけた後、ORFのパラメータを生成するまでの、
 * 一連の処理を行う。
 * @author sesejun
 */
public class CalcORFStat {
	OptionParser		_optParser			= new OptionParser();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CalcORFStat calcOrfStat = new CalcORFStat();
		calcOrfStat.process(args);
	}

	private void process(String[] args) {
		String[] dummyargs = new String [0];

		try {
			File file = new File("tmp");			
			file.mkdir();
		} catch (Exception e) {
			System.out.println("Temporary directory can't be made. You need write permmision on current directory.");
			System.exit(0);
		}
		//  java -Xmx512m -Xms512m -jar ../../bin/NucleusStageClassifier.jar -av -b ../result
		NucleusStageClassifier classifier = new NucleusStageClassifier();
		classifier.setNotRunOnCommandLine();
		classifier.setAutoSearch();
		classifier.setVerbose();
		classifier.setBaseDir(".");
		classifier.setOutDir("tmp");
		LinkedList inputFileList = classifier.setupByArguments(dummyargs);
		classifier.classify(inputFileList);

		// java -Xmx512m -Xms512m -jar ../../bin/CalcGroupStat.jar -v -e
		CalcGroupStat c = new CalcGroupStat();
		try
		{
			c.setBaseDir("tmp");
			c.setVerbose();
			c.setupByArguments(dummyargs);
			c.loopForEachDirectory();
		}
		catch (SCMDException e)
		{
			e.what();
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
	}

}
