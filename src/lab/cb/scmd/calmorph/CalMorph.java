//------------------------------------
// SCMD Project
//  
// CalMorph.java 
// Since:  2004/04/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.calmorph;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xerial.util.cui.OptionParser;
import org.xerial.util.cui.OptionParserException;

class CalMorph {
	String	name, path, outdir, xmldir;

	public CalMorph() {
	}

	enum Opt {
		HELP, GUI, OUTPUT_DIR_IMAGE_XMLDATA, INPUTDIR, OUTPUTDIR, VERBOSE, LOG_CONFIG, ORF_NAME
	}

	/**
	 * Usage > java -jar calmorph.jar [option] [image_number]
	 */
	public static void main(String[] args) {
		OptionParser<Opt> parser = new OptionParser<Opt>();
		BasicConfigurator.configure();
		Logger _logger = Logger.getLogger(CalMorph.class);
		try {
			parser.addOption(Opt.HELP, "h", "help", "display hyelp message");
			parser.addOption(Opt.GUI, "g", "gui", "run in GUI mode");
			parser.addOptionWithArgument(Opt.OUTPUT_DIR_IMAGE_XMLDATA, "x", "xout", "DIR", "output directory of XML image data", "xml");
			parser.addOptionWithArgument(Opt.INPUTDIR, "i", "input", "DIR", "input photo directory", ".");
			parser.addOptionWithArgument(Opt.OUTPUTDIR, "o", "output", "DIR", "output directory of the analysis results", "result");
			parser.addOption(Opt.VERBOSE, "v", "verbose", "display verbose messages");
			parser.addOptionWithArgument(Opt.ORF_NAME, "n", "orf", "ORF_NAME", "specify the orf name");
			parser.addOptionWithArgument(Opt.LOG_CONFIG, "l", "logconfig", "CONFIG_FILE", "logger configuration file");

			parser.parse(args); // read the command line arguments

			if (parser.isSet(Opt.HELP)) {
				System.out.println("> calmorph [option]");
				System.out.println(parser.helpMessage());
				return;
			}

			if (parser.isSet(Opt.GUI)) {
				GUIFrame gui = new GUIFrame();
				gui.setVisible(true);
				return;
			}

            if (parser.isSet(Opt.VERBOSE))
                Logger.getRootLogger().setLevel(Level.TRACE);
            else
                Logger.getRootLogger().setLevel(Level.INFO);

            if (parser.isSet(Opt.LOG_CONFIG)) {
				// configure the logger
				PropertyConfigurator.configure(parser.getValue(Opt.LOG_CONFIG));
			}


						
			// run in command line user interface (CUI) mode
			String inputDirName = parser.getValue(Opt.INPUTDIR);
			
			CalMorphOption calMorphOption = new CalMorphOption();
			calMorphOption.setInputDirectory(inputDirName);
			calMorphOption.setOutputDirectory(parser.getValue(Opt.OUTPUTDIR));
			calMorphOption.setXmlOutputDirectory(parser.getValue(Opt.OUTPUT_DIR_IMAGE_XMLDATA));
			
			String orfName = inputDirName;
			if(parser.isSet(Opt.ORF_NAME))
				orfName = parser.getValue(Opt.ORF_NAME);
						
			calMorphOption.setOrfName(orfName);
			
            
			Pattern conAfileNamePattern = Pattern.compile("[\\w-]+-C([0-9]+)\\.jpg");
			File inputDir = new File(inputDirName);
			File ls[] = inputDir.listFiles();
			int maxImage = 0;
			for (File f : ls)
			{
                String fileName = f.getName();
                Matcher m = conAfileNamePattern.matcher(fileName);
                if(!m.matches())
                    continue;
                _logger.debug("found a conA image file : " + fileName);
                
                int photoNum = Integer.parseInt(m.group(1));
                if(photoNum > maxImage)
                    maxImage = photoNum;
			}
            calMorphOption.setMaxImageNumber(maxImage);
            
            _logger.info("max image# in the folder: " + calMorphOption.getMaxImageNumber());
			_logger.debug("orf name: " + calMorphOption.getOrfName());
			_logger.debug("input directory: " + calMorphOption.getInputDirectory());
			_logger.debug("output directory: " + calMorphOption.getOutputDirectory());
			_logger.debug("XML output directory: " + calMorphOption.getXmlOutputDirectory());

			DisruptantProcess dp = new DisruptantProcess(calMorphOption);
			dp.process();
		}

		catch (OptionParserException e) {
			System.err.println(e.getMessage());
			_logger.error(e.getMessage());
			return;
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			_logger.error(e.getMessage());
			return;
		}
	}

}
