//--------------------------------------
//SCMDProject
//
//TeardropBackgroupnd.java 
//Since: 2004/08/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.teardrop;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.exception.UnfinishedTaskException;
import lab.cb.scmd.util.ProcessRunner;
import lab.cb.scmd.util.cui.Option;
import lab.cb.scmd.util.cui.OptionParser;
import lab.cb.scmd.util.cui.OptionWithArgument;
import lab.cb.scmd.util.stat.NumElementAndStatValuePair;
import lab.cb.scmd.util.stat.Statistics;
import lab.cb.scmd.util.stat.StatisticsWithMissingValueSupport;
import lab.cb.scmd.util.table.BasicTable;
import lab.cb.scmd.util.table.FlatTable;
import lab.cb.scmd.util.table.TableIterator;

// Teardropの背景を作成する
public class TeardropBackground {

	OptionParser _parser = new OptionParser();
	final static int OPT_HELP = 0;
	final static int OPT_IMAGEMAGICK = 1;
	// File Name
	private String PREFIX					= "td_";
	private String SUFFIX					= ".png";
	private PrintStream _out				= System.out;
	private String BGFILE					= "TeardropBackground.png";

	// color table
	private String BASE_COLOR 				= "#8888FF";
	//private String STANDARD_POSISION_COLOR 	= "#8888FF";
	private int	IMAGEWIDTH					= 70;
	private int	IMAGEHEIGHT					= 300;
	private int BARHEIGHT					= 4;
	private int MARGIN						= 4;

	// instances
	BasicTable _dataset = null;
	Map _statisticsMap	= new HashMap();

	// program
	String _converter = "C:/Program Files/ImageMagick-6.0.3-Q16/convert.exe";

	public static void main(String[] args) {

		TeardropBackground tb = new TeardropBackground();
		tb.load(args);
		tb.computeStatistics();
		tb.drawAllColumnTeardrop();
	}

	public void load(String[] args) {
		try {
			_parser.setOption(new Option(OPT_HELP, "h", "help", "print help message"));
			_parser.setOption(new OptionWithArgument(OPT_IMAGEMAGICK, "c", "convert", "PATH", "set the path to ImageMagcik convert executable"));
			_parser.getContext(args);

			String filename = _parser.getArgument(0);

			if(_parser.isSet(OPT_HELP))
			{
				System.out.println("java -jar TeardropBackground.jar [option] tablefile");
				System.out.println(_parser.createHelpMessage());
				System.exit(0);
			}

			if(_parser.isSet(OPT_IMAGEMAGICK))
				_converter = _parser.getValue(OPT_IMAGEMAGICK);


			_dataset = new FlatTable(filename, true, true);
		} catch (SCMDException e) {
			e.printStackTrace();
			System.err.println("Invalid File");
			System.exit(0);
		}
	}

	public void setDataset(BasicTable bt) {
		_dataset = bt;
	}

	public void computeStatistics() {
		System.out.println("Paramname" + "\t" + "Average" + "\t" + "SD" + "\t" + "MAX" + "\t" + "MIN");
		int rowSize =  _dataset.getRowSize();
		int colSize =  _dataset.getColSize();
		for( int i = 0; i < colSize; i++ ) {
			computeTeardropStats(_dataset.getColLabel(i));
			//computeTeardropStats(rowSize, i);
		}
	}

	private void computeTeardropStats(String label) {
		TableIterator verticalIterator = _dataset.getVerticalIterator(label);
		String missingValue[] = {".", "0", "0.0"};
		Statistics s = new StatisticsWithMissingValueSupport(missingValue);
		NumElementAndStatValuePair pair = s.calcMeanAndNumSample(verticalIterator);
		verticalIterator = _dataset.getVerticalIterator(label);
		double avg = pair.getValue();
		double var =  s.calcVariance(verticalIterator);
		double sd = Math.sqrt(var);
		verticalIterator = _dataset.getVerticalIterator(label);
		double max = s.getMaxValue(verticalIterator);
		verticalIterator = _dataset.getVerticalIterator(label);
		double min = s.getMinValue(verticalIterator);
		TeardropStatistics tds = new TeardropStatistics(avg, sd, max, min);

		verticalIterator = _dataset.getVerticalIterator(label);
		tds.calcHistgram(s.getFilteringStrategy().filter(verticalIterator));
		_statisticsMap.put(label, tds);

		System.out.println(label + "\t" + avg + "\t" + sd + "\t" + max + "\t" + min);
	}


	public void drawAllColumnTeardrop() {
		List columnLabels = _dataset.getColLabelList();
		int colSize = columnLabels.size();
		try {
			for( int i = 0; i < colSize; i++ ) {
				//System.err.println(i + ":" + columnLabels.get(i));
				String filename = PREFIX + columnLabels.get(i) + SUFFIX;
				_out = new PrintStream( new BufferedOutputStream( new FileOutputStream(filename)));
				drawTeardrop((String)columnLabels.get(i), _out);
				_out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void drawTeardrop(String columnLabel, PrintStream out) {
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

}

