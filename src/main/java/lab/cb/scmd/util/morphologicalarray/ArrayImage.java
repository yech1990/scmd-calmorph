//--------------------------------------
// SCMD Project
// 
// ArrayImage.java 
// Since:  2004/08/30
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.morphologicalarray;

import lab.cb.scmd.autoanalysis.grouping.CalMorphTable;
import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.table.TableIterator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

//import java.io.PrintStream;
//import com.sun.image.codec.jpeg.ImageFormatException;
//import lab.cb.scmd.util.table.Cell;

/**
 * @author nakatani
 */
public class ArrayImage {
    CalMorphTable tableW;
    int orfSizeW;
    CalMorphTable tableM;
    int orfSizeM;
    int parameterSize;

    BufferedImage image;
    int[][] colorTable;


    ArrayImage(String CalmorphTableFilename_Wildtype, String CalmorphTableFilename_Mutant, String transformLogFilename) throws SCMDException, IOException {
        tableW = new CalMorphTable(CalmorphTableFilename_Wildtype);
        orfSizeW = tableW.getRowSize();
        parameterSize = tableW.getColSize();
        tableM = new CalMorphTable(CalmorphTableFilename_Mutant);
        orfSizeM = tableM.getRowSize();

        image = new BufferedImage(parameterSize - 1, orfSizeM, BufferedImage.TYPE_INT_RGB);
        colorTable = new int[parameterSize - 1][orfSizeM];

        makeArrayImage(transformLogFilename);
//		makeWildtypeArray();
    }

    private void toTransformedWildtypeTable(String filename) throws SCMDException, IOException {
        CalMorphTable table = tableW;
        Double[][] transformedTable = new Double[parameterSize][orfSizeW];
        for (int parameter = 1; parameter < parameterSize; ++parameter) {
            Double[] data = getDataByParameter(table, parameter);
            PowerTransformation pt = new PowerTransformation(data);
            transformedTable[parameter] = pt.getTransformedData();
//			for (int j=0;j<transformed.length;++j){
//				Cell c=new Cell(transformed[j]);
//				table.setCell(c,j,parameter);
//			}
        }
        PrintWriter transformed = new PrintWriter(new FileWriter(filename));
        //transformed.print("orf"+"\t");
        for (int p = 0; p < parameterSize; ++p) {
            transformed.print(table.getColLabel(p) + "\t");
        }
        transformed.println();
        for (int orf = 0; orf < orfSizeW; ++orf) {
            transformed.print(table.getCell(orf, 0) + "\t");
            for (int p = 1; p < parameterSize; ++p) {
                transformed.print(transformedTable[p][orf] + "\t");
                //transformed.print(table.getCell(orf,p)+"\t");
            }
            transformed.println();
        }
        transformed.close();
    }

    static final int RED = toRGB(255, 20, 20);
    static final int Red = toRGB(255, 80, 80);
    static final int red = toRGB(255, 150, 150);
    static final int redwhite = toRGB(255, 200, 200);
    static final int GREEN = toRGB(0, 255, 0);
    static final int Green = toRGB(60, 255, 60);
    static final int green = toRGB(120, 255, 120);
    static final int greenwhite = toRGB(200, 255, 200);
    static final int white = toRGB(255, 255, 255);
    static final int backgroundColor = toRGB(255, 255, 255);

    static private int toRGB(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }


    /**
     * 指定されたパラメーターのデータと、指定されたORFに対して
     * LeaveOneOut式の検定方法で色を決める。
     *
     * @throws SCMDException
     * @throws IOException
     * @author nakatani
     */
    private int testWildtypeLeaveOneOut(int parameter, int testOrf) throws SCMDException, IOException {
        lab.cb.scmd.util.table.Cell test = null;
        Vector<Double> dataW = new Vector<>();
        TableIterator i = tableW.getVerticalIterator(parameter);
        for (int j = 0; i.hasNext(); ++j) {
            lab.cb.scmd.util.table.Cell c = (lab.cb.scmd.util.table.Cell) i.next();
            if (j == testOrf) {
                test = c;
                if (!test.isValidAsDouble() || test.doubleValue() < 0) return backgroundColor;
                continue;
            }
            if (!c.isValidAsDouble() || c.doubleValue() < 0) continue;
            dataW.add(c.doubleValue());
        }
        Double[] data = new Double[dataW.size()];
        for (int k = 0; k < dataW.size(); ++k) {
            data[k] = dataW.get(k);
        }

        PowerTransformation pt = new PowerTransformation(data);
        PrintWriter pw = new PrintWriter(System.out, true);
        //pt.println(pw);
        assert test != null;
        return getSignificance(pt, test.doubleValue());
    }

    /**
     * 野生株をLeaveOneOutで検定して色づけしていく。
     *
     * @throws SCMDException
     * @throws IOException
     * @author nakatani
     */
    private void makeWildtypeArray() throws SCMDException, IOException {
        for (int parameter = 1; parameter < parameterSize; ++parameter) {
            System.out.println(tableW.getColLabel(parameter));
            for (int orf = 0; orf < orfSizeW; ++orf) {
                int signif = testWildtypeLeaveOneOut(parameter, orf);
                int color = significanceToColor(signif);
                image.setRGB(parameter - 1, orf, color);
                colorTable[parameter - 1][orf] = signif;
            }
        }
    }

    /**
     * CalMorphTableから指定されたパラメータのデータを返す。NaNや-1は無視する。
     *
     * @author nakatani
     */
    private Double[] getDataByParameter(CalMorphTable cmt, int parameter) {
        Vector<Double> dataW = new Vector<Double>();
        TableIterator i = cmt.getVerticalIterator(parameter);
        for (int j = 0; i.hasNext(); ++j) {
            lab.cb.scmd.util.table.Cell c = (lab.cb.scmd.util.table.Cell) i.next();
            if (c.isValidAsDouble()) {
                double d = c.doubleValue();
                if (d < 0) continue;
                dataW.add(d);
            }
        }
        Double[] validData = new Double[dataW.size()];
        for (int j = 0; j < dataW.size(); ++j) {
            validData[j] = dataW.get(j);
        }
        return validData;
    }

    /**
     * criticalValueとデータの値からsiginificantな度合いを返す。
     *
     * @throws SCMDException
     * @author nakatani
     */
    private int getSignificance(PowerTransformation pt, Double d) throws SCMDException {
        double x = d;
        Double[] crt = pt.getOneSidedCriticalValues();
        for (int i = 0; i < 7; ++i) {
            if (crt[i] <= x) {
                return 8 - i;
            }
            if (crt[13 - i] >= x) {
                return -(8 - i);
            }
        }
        return 0;
    }

    private int significanceToColor(int signif) {
        switch (signif) {
            case 7:
            case 6:
            case 5:
            case 4:
            case 3:
                return RED;
            case -7:
            case -6:
            case -5:
            case -4:
            case -3:
                return GREEN;
            default:
                return white;
        }
    }

    /**
     * 変異株データに対する色づけ。
     *
     * @throws IOException
     * @throws SCMDException
     * @author nakatani
     */
    private void makeArrayImage(String transformLogFilename) throws IOException, SCMDException {

        PrintWriter transformLog = new PrintWriter(new FileWriter(transformLogFilename));
        transformLog.print("parameter\t");
        PowerTransformation.printHeader(transformLog);
        transformLog.print("greenORFs\t" + "redORFs");
        transformLog.println();

        for (int parameter = 1; parameter < parameterSize; ++parameter) {
            Double[] data = getDataByParameter(tableW, parameter);
            PowerTransformation pt = new PowerTransformation(data);

            //output transform parameter info
            System.out.println(tableW.getColLabel(parameter));
            transformLog.print(tableW.getColLabel(parameter) + "\t");
            pt.print(transformLog);

            //significance test
            int countRed = 0;
            int countGreen = 0;
            TableIterator i = tableM.getVerticalIterator(parameter);
            for (int orf = 0; i.hasNext(); ++orf) {
                //int color=white;
                int signif = 0;
                lab.cb.scmd.util.table.Cell c = (lab.cb.scmd.util.table.Cell) i.next();
                if (c.isValidAsDouble()) {
                    double d = c.doubleValue();
                    if (d >= 0) {
                        signif = getSignificance(pt, d);
                    }
                }
                int color = significanceToColor(signif);
                image.setRGB(parameter - 1, orf, color);
                colorTable[parameter - 1][orf] = signif;
                if (color == RED) {
                    ++countRed;
                    //excel.print("3"+"\t");
                } else if (color == GREEN) {
                    ++countGreen;
                    //excel.print("-3"+"\t");
                } else {
                    //excel.print("0"+"\t");
                }
            }
            transformLog.print(countGreen + "\t" + countRed);
            transformLog.println();
            //excel.println();
        }
        transformLog.close();
    }

    /**
     * 変異株のアレイをファイルに書き込む。
     *
     * @throws IOException
     * @author nakatani
     */
    public void writeImageArray(String filenameM) throws IOException {
        ImageIO.write(image, "png", new File(filenameM));
    }

    public void writeExcelArray(String excelFilename) throws IOException {
        PrintWriter excel = new PrintWriter(new FileWriter(excelFilename));
        for (int i = 0; i < tableM.getColSize(); ++i) {
            excel.print(tableM.getColLabel(i) + "\t");
        }
        excel.println();
        for (int orf = 0; orf < tableM.getRowSize(); ++orf) {
            excel.print(tableM.getCell(orf, 0) + "\t");
            for (int param = 1; param < tableM.getColSize(); ++param) {
                int signif = colorTable[param - 1][orf];
                excel.print(signif + "\t");
            }
            excel.println();
        }
        excel.close();
    }

    /**
     * 引数は、野生株valid.xls, 変異株valid.xls, 変異株.jpeg
     *
     * @author nakatani
     */
    public static void main(String[] args) {
        ArrayImage ai;
        try {
            ai = new ArrayImage(args[0], args[1], args[3]);
            ai.writeExcelArray(args[4]);
            ai.writeImageArray(args[2]);
            ai.toTransformedWildtypeTable(args[5]);

        } catch (SCMDException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
            //} catch (ImageFormatException e1) {
            //    e1.printStackTrace();
            //    System.exit(-1);
        }

    }
}


//--------------------------------------
// $Log: ArrayImage.java,v $
// Revision 1.4  2004/12/02 09:55:43  nakatani
// delete?
//
// Revision 1.3  2004/09/18 23:10:18  nakatani
// *** empty log message ***
//
// Revision 1.2  2004/09/03 06:02:09  nakatani
// *** empty log message ***
//
// Revision 1.1  2004/08/29 20:22:38  nakatani
// MorphologicalArrayのための、検定クラスと画像出力クラス。
//
//--------------------------------------
