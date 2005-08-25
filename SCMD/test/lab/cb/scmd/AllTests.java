//--------------------------------------
// SCMD Project
// 
// AllTests.java 
// Since:  2004/06/11
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.cui.*;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author leo
 * SCMD ProjectのTest Suite 
 * 実行時には、workfolderを、workfolder/testに指定すること
 */
public class AllTests
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite("SCMD Test Suites");
        //$JUnit-BEGIN$
        suite.addTest(lab.cb.scmd.algorithm.AllTests.suite());
        suite.addTest(lab.cb.scmd.autoanalysis.grouping.AllTests.suite());
        suite.addTest(lab.cb.scmd.util.AllTests.suite());
        //$JUnit-END$
        return suite;
    }

    public static void main(String[] args)
    {
        final int OPT_HELP = 0;
        final int OPT_SWINGTEST = 1;
        final int OPT_AWTTEST = 2;
        try
        {
            OptionParser parser = new OptionParser();
        
            parser.addOptionGroup((new OptionGroup("swing option", true)).
                    add(new Option(OPT_SWINGTEST, "s", "swing", "run Swing TestRunner")));
            
            parser.addOptionGroup((new OptionGroup("awt option", true)).
                    add(new Option(OPT_AWTTEST, "a", "awt", "run AWT TestRunner")));

            parser.getContext(args);
            
            if(parser.isSet(OPT_SWINGTEST))
            {
                junit.swingui.TestRunner.run(AllTests.class);
                return;
            }
            if(parser.isSet(OPT_AWTTEST))
            {
                junit.awtui.TestRunner.run(AllTests.class);
                return;
            }
            junit.textui.TestRunner.run(AllTests.class);           
        }
        catch(SCMDException e)
        {
            e.what(System.err);            
        }            
    }

}


//--------------------------------------
// $Log: AllTests.java,v $
// Revision 1.4  2004/07/08 08:24:30  leo
// TestSuiteの構成を見直し
//
// Revision 1.3  2004/07/07 15:04:22  leo
// Antで自動コンパイル、テスト実行を記述
//
// Revision 1.2  2004/06/23 16:31:58  leo
// Collection操作のためのlab.cb.scmd.algorithmパッケージを追加
//
// Revision 1.1  2004/06/11 06:23:45  leo
// TestSuiteを作成
//
//--------------------------------------