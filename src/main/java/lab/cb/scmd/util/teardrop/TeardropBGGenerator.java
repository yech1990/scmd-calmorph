//-----------------------------------
// SCMD Project
// 
// TeardropBGGenerator.java 
// Since: 2005/02/02
//
// $Date$ 
// $Author$
//--------------------------------------
package lab.cb.scmd.util.teardrop;

import java.io.PrintStream;

import lab.cb.scmd.exception.UnfinishedTaskException;
import lab.cb.scmd.util.ProcessRunner;

/**
 * @author leo
 *
 */
public class TeardropBGGenerator
{
    // color table
    private String BASE_COLOR               = "#8888FF";
    //private String STANDARD_POSISION_COLOR    = "#8888FF";
    private int IMAGEWIDTH                  = 70;
    private int IMAGEHEIGHT                 = 300;
    private int BARHEIGHT                   = 4;
    private int MARGIN                      = 4;
    private String _converter = "/usr/local/bin/convert";

    /*
    public void drawTeardrop(String columnLabel, PrintStream out) {
        TeardropStatistics tds = (TeardropStatistics)_statisticsMap.get(columnLabel);
        int size = tds.getHistgramSize();
        int centerindex = tds.getIndex(tds.getAvg());
        String drawCommand = "-fill " + BASE_COLOR;
        for( int i = 0; i < size; i++ ) {
            int hpos = ( centerindex - i ) * BARHEIGHT + IMAGEHEIGHT /2 ;
            double barwidth = Math.log(tds.getHistgram(i) + 1) * (IMAGEWIDTH - MARGIN) / Math.log(tds.getMaxCount() + 1);
            double middle = IMAGEWIDTH/2.0;
            String stpos = (int)(middle - barwidth/2.0) + "," + hpos; 
            String edpos = (int)(middle + barwidth/2.0) + "," + (hpos + BARHEIGHT);
            drawCommand += " -draw \"rectangle " + stpos + " " + edpos + "\"";
        }
        //drawCommand += " -font helvetica -draw \'color black text 5,5 \"" + columnLabel + "\"\'";
        String cmd = _converter + " " + BGFILE + " " + drawCommand + " -"; 
        try {
            //ProcessRunner.run(out, _converter + " " + BGFILE + " " + drawCommand);
            ProcessRunner.run(out, cmd);
        } catch (UnfinishedTaskException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    */
}
