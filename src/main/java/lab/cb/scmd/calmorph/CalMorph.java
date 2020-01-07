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

import com.github.tomaslanger.chalk.Chalk;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.xerial.util.cui.OptionParser;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CalMorph {

    public CalMorph() {
    }

    /**
     * Usage > java -jar calmorph.jar [option] [image_number]
     */
    public static void main(String[] args) {
        OptionParser<Opt> parser = new OptionParser<>();
        //BasicConfigurator.configure();
        Logger _logger = LogManager.getLogger(CalMorph.class);
        try {
            parser.addOption(Opt.HELP, "h", "help",
                    "display this help message");
            parser.addOptionWithArgument(Opt.INPUTDIR, "i", "input", "DIR",
                    "input photo directory", ".");
            parser.addOptionWithArgument(Opt.OUTPUTDIR, "o", "output", "DIR",
                    "output directory of the analysis results", "result");
            parser.addOptionWithArgument(Opt.OUTPUT_DIR_IMAGE_XMLDATA, "x", "xout", "DIR",
                    "output directory of XML image data", "xml");
            parser.addOptionWithArgument(Opt.STRAIN_NAME, "n", "strain", "STRAIN_NAME",
                    "specify the strain name");
            parser.addOptionWithArgument(Opt.IMAGE_SUFFIX, "s", "suffix", "IMAGE_SUFFIX",
                    "specify the suffix of input image file, default: tif");
            parser.addOptionWithArgument(Opt.IMAGE_ASPECTRATIO, "r", "ratio", "IMAGE_ASPECTRATIO",
                    "specify the aspect ratio of input image file, opt: [widthxheight], default: 2040x2040");
            parser.addOptionWithArgument(Opt.ACTIN, "a", "actin", "ture/false",
                    "actin mode, opt: [true / false], default false", "false");
            parser.addOptionWithArgument(Opt.DAPI, "d", "dapi", "ture/false",
                    "DAPI mode, opt: [true / false], default true", "true");
            parser.addOptionWithArgument(Opt.DARKEN_BACKGROUND, "b", "background", "PERCENTAGE",
                    "make the background " + Chalk.on("darker").gray() + " by percentage (%), opt: float number in 0~100, default 0", "0");
            parser.addOptionWithArgument(Opt.LOG_CONFIG, "l", "logconfig", "CONFIG_FILE",
                    "logger configuration file");
            parser.addOption(Opt.VERBOSE, "v", "verbose",
                    "display verbose messages");

            parser.parse(args); // read the command line arguments

            if (parser.isSet(Opt.HELP) || args.length == 0) {
                System.out.println(Chalk.on("CalMorph").cyan().bold() + " version " + Chalk.on("2.0.2").cyan().bold());
                System.out.println(Chalk.on("> A forked version of CalMorph in HeLab by YC\n").yellow());
                System.out.println("Usage: java -jar CalMorph.jar [options]");
                System.out.println("> [options]");
                System.out.println(parser.helpMessage());
                return;
            }

            if (parser.isSet(Opt.VERBOSE))
                Configurator.setRootLevel(Level.TRACE);
            else
                Configurator.setRootLevel(Level.INFO);
            if (parser.isSet(Opt.LOG_CONFIG)) {
                LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
                File file = new File(parser.getValue(Opt.LOG_CONFIG));
                logContext.setConfigLocation(file.toURI());
                logContext.reconfigure();
                // configure the logger
                // PropertyConfigurator.configure(parser.getValue(Opt.LOG_CONFIG));
            }


            // run in command line user interface (CUI) mode
            CalMorphOption calMorphOption = new CalMorphOption();

            // input dir
            String inputDirName = parser.getValue(Opt.INPUTDIR);
            calMorphOption.setInputDirectory(inputDirName);

            // output dir
            calMorphOption.setOutputDirectory(parser.getValue(Opt.OUTPUTDIR));
            calMorphOption.setXmlOutputDirectory(parser.getValue(Opt.OUTPUT_DIR_IMAGE_XMLDATA));

            // image aspect ratio
            String imageAspectRatio = "2040x2040";
            if (parser.isSet(Opt.IMAGE_ASPECTRATIO))
                imageAspectRatio = parser.getValue(Opt.IMAGE_ASPECTRATIO);
            calMorphOption.setImageAspectRatio(imageAspectRatio);

            // image suffix
            String imageSuffix = "tif";
            if (parser.isSet(Opt.IMAGE_SUFFIX))
                imageSuffix = parser.getValue(Opt.IMAGE_SUFFIX);
            calMorphOption.setImageSuffix(imageSuffix);

            // String strainName = inputDirName;
            String strainName = new File(inputDirName).getName();
            if (parser.isSet(Opt.STRAIN_NAME))
                strainName = parser.getValue(Opt.STRAIN_NAME);
            calMorphOption.setStrainName(strainName);

            // darken background by percentage (%)
            double darkenBackground = 0.0;
            if (parser.isSet(Opt.DARKEN_BACKGROUND))
                darkenBackground = Double.parseDouble(parser.getValue(Opt.DARKEN_BACKGROUND)) / 100.0;
            if (darkenBackground > 0.99999) {
                darkenBackground = 0.99999;
            }
            calMorphOption.setDarkenBackground(darkenBackground);

            calMorphOption.setCalA(parser.isSet(Opt.ACTIN) && Boolean.parseBoolean(parser.getValue(Opt.ACTIN)));
            calMorphOption.setCalD(!parser.isSet(Opt.DAPI) || Boolean.parseBoolean(parser.getValue(Opt.DAPI)));


            Pattern conAfileNamePattern = Pattern.compile("[\\w-]+-C([0-9]+)\\." + imageSuffix);
            File inputDir = new File(inputDirName);
            File[] ls = inputDir.listFiles();
            int maxImage = 0;
            assert ls != null;
            for (File f : ls) {
                String fileName = f.getName();
                Matcher m = conAfileNamePattern.matcher(fileName);
                if (!m.matches())
                    continue;
                _logger.debug("found a conA image file : " + fileName);

                int photoNum = Integer.parseInt(m.group(1));
                if (photoNum > maxImage)
                    maxImage = photoNum;
            }
            calMorphOption.setMaxImageNumber(maxImage);

            _logger.info("max image# in the folder: " + calMorphOption.getMaxImageNumber());
            _logger.debug("image suffix: " + calMorphOption.getImageSuffix());
            _logger.debug("image aspect ratio: " + calMorphOption.getImageAspectRatio());
            _logger.debug("strain name: " + calMorphOption.getStrainName());
            _logger.debug("input directory: " + calMorphOption.getInputDirectory());
            _logger.debug("output directory: " + calMorphOption.getOutputDirectory());
            _logger.debug("XML output directory: " + calMorphOption.getXmlOutputDirectory());

            DisruptantProcess dp = new DisruptantProcess(calMorphOption);
            dp.process();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            _logger.error(e.getMessage());
        }
    }

    enum Opt {
        HELP, OUTPUT_DIR_IMAGE_XMLDATA, INPUTDIR, OUTPUTDIR, VERBOSE, IMAGE_ASPECTRATIO, IMAGE_SUFFIX, LOG_CONFIG, STRAIN_NAME, ACTIN, DAPI, DARKEN_BACKGROUND
    }

}
