// ------------------------------------
// SCMD Project
//
// CellImage.java
// Since: 2004/04/16
//
// $URL$
// $LastChangedBy$
//--------------------------------------

package lab.cb.scmd.calmorph;

//import com.sun.image.codec.jpeg.*;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.util.Arrays;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class CellImage {

    private Logger _logger = Logger.getLogger(this.getClass());

    private int _width, _height, _size;                          //Image width, height, number of pixels
    private int number, Ddiff, Adiff, startid;                   //Image number, DAPI image shift, actin image shift, start cell number
    private String name, suffix, path, outdir;                           //knockout strain name, image directory, output directory
    private int[] _cell_points, _nucleus_points, _actin_points;  //3 types of metric points
    private int[] ci, ci2, di, ai;                               //Brightness of 3 types of images during processing
    private int[] pixeltocell, pixeltocell2;                     //which cell is inside a certain pixel
    Cell[] cell;                                                 //cells in the image
    boolean err, calD, calA;                                     //Whether err has occurred, whether to process DAPI images or actin images
    private boolean objectsave, outimage, outsheet;              //Whether to save in the middle of an object, output an image, or output basic and biological sheets
    private int objectload;                                      //How to retrieve the saved object
    private int maxdiff;
    private boolean flag_tmp;
    String err_kind;
    //int[] actindiv;//テスト

    //private static final String _image_suffix = ".jpg";
    private static final String _cell_wall = "cell_wall";
    private static final String _nucleus = "nucleus";
    private static final String _actin = "actin";

    CellImage(String name, String path, String suffix, int number, String outdir, int startid, boolean calD, boolean calA) {
        _width = 2040;            //Fixed for the time being
        _height = 2040;           //Fixed for the time being
        _size = _width * _height; //Fixed for the time being
        maxdiff = 5;              //Fixed for the time being
        this.name = name;
        this.path = path;
        this.suffix = suffix;
        this.number = number;
        this.outdir = outdir;
        this.startid = startid;
        this.calD = calD;
        this.calA = calA;
        err = false;
        err_kind = "";
        BufferedImage bi;
        DataBuffer db = null;

        _logger.debug("process a photo: " + number);

        String prefix = path + File.separator + new File(path).getName();
        //String prefix = path;

        if (new File(prefix + "-C" + number + "." + suffix).exists() && (bi = getBufferedImage(prefix + "-C" + number + "." + suffix)) != null) {
            db = bi.getRaster().getDataBuffer();
        } else {
            err = true;
            _logger.warn("Cell wall image of " + name + " was not loaded.");
        }

        if (!err) {
            assert db != null;
            if (_size == db.getSize()) {
                _cell_points = new int[_size];
                for (int i = 0; i < _size; i++) {
                    _cell_points[i] = db.getElem(i);
                }
            } else {
                err = true;
                err_kind = "incorrect size of image";
                _logger.warn("Size of " + name + "'s cell wall image is incorrect.");
            }
        }

        if (calD) {//DAPI画像の処理も行う
            if (new File(prefix + "-D" + number + "." + suffix).exists()) {
                if ((bi = getBufferedImage(prefix + "-D" + number + "." + suffix)) != null) {
                    db = bi.getRaster().getDataBuffer();
                } else {
                    err = true;
                    _logger.warn("Nucleus image of " + name + " was not loaded.");
                }
            } else {
                calD = false;
                err = true;
                _logger.warn("Nucleus image of " + name + " was not loaded although Flag is true.");
            }
            if (calD) {
                assert db != null;
                if (_size == db.getSize()) {
                    _nucleus_points = new int[_size];
                    for (int i = 0; i < _size; i++) {
                        _nucleus_points[i] = db.getElem(i);
                    }
                } else {
                    err = true;
                    err_kind = "incorrect size of image";
                    _logger.warn("Size of " + name + "'s nucleus image is incorrect.");
                }
            }
        }

        if (calA) { //actin画像の処理も行う
            if (new File(prefix + "-A" + number + "." + suffix).exists()) {
                if ((bi = getBufferedImage(prefix + "-A" + number + "." + suffix)) != null) {
                    db = bi.getRaster().getDataBuffer();
                } else {
                    err = true;
                    _logger.warn("Actin image of " + name + " was not loaded.");
                }
            } else {
                err = true;
                calA = false;
                _logger.warn("Actin image of " + name + " was not loaded although Flag is true.");
            }
            if (calA) {
                assert db != null;
                if (_size == db.getSize()) {
                    _actin_points = new int[_size];
                    for (int i = 0; i < _size; i++) {
                        _actin_points[i] = db.getElem(i);
                    }
                } else {
                    err = true;
                    err_kind = "incorrect size of image";
                    _logger.warn("Size of " + name + "'s actin image is incorrect.");
                }
            }
        }
    }

    /**
     * Option settings
     *
     * @param objectsave
     * @param objectload
     * @param outimage
     * @param outsheet
     */
    void setOptions(boolean objectsave, int objectload, boolean outimage, boolean outsheet) {
        this.objectsave = objectsave;
        this.objectload = objectload;
        this.outimage = outimage;
        this.outsheet = outsheet;
    }

    /**
     * Perform all processing
     *
     * @param pwbaseC
     * @param pwexpandC
     * @param pwbaseD
     * @param pwexpandD
     * @param pwbaseA
     * @param pwexpandA
     * @param pwpatchA
     * @param pwvers
     * @param pwxml
     * @return
     */
    public int process(PrintWriter pwbaseC, PrintWriter pwexpandC, PrintWriter pwbaseD, PrintWriter pwexpandD, PrintWriter pwbaseA, PrintWriter pwexpandA, PrintWriter pwpatchA, PrintWriter pwvers, PrintWriter pwxml) {

        err = false;
        if (objectload < 0) {
            segmentCells();
        } else if (objectload == 0) load(0);
        if (objectsave) save(0);

        if (calD) {
            if (objectload < 1 && !err) {
                procDImage();
            } else if (objectload == 1) load(1);
            if (objectsave) save(1);
        }
        if (calA) {
            if (objectload < 2 && !err) {
                procAImage();
            } else if (objectload >= 2) load(2);
            if (objectsave) save(2);
        }
        if (!err) {//ずれが大きくなければデータを出して個数を返す
            if (outsheet) {
                writeXLSBaseC(pwbaseC);
                writeXLSExpandC(pwexpandC);
                if (calD) {
                    writeXLSBaseD(pwbaseD);
                    writeXLSExpandD(pwexpandD);
                }
                if (calA) {
                    writeXLSBaseA(pwbaseA);
                    writeXLSExpandA(pwexpandA);
                    writeXLSPatchA(pwpatchA);
                }
            }
            writeXLSVers(pwvers);
            if (outimage) {
                outCImage();
                if (calD) outDImage();
                if (calA) outAImage();
            }
            if (pwxml != null) writeImageDataXML(pwxml);
            return cell.length;
        } else return 0;
    }

    /**
     * his3_control_*-C*.jpg の処理
     * morphological segmentation
     */
    private void segmentCells() {
        ci = _cell_points.clone(); //Keep the original image

        medianim(ci);
        ci2 = ci.clone();
        vivid(ci, 3);
        vivid(ci2, 7);
        int[] difci = dif(ci, ci2);
        division(ci, ci2, difci);
        ci = gradim(ci);
        if (!threshold(ci)) {//画像の色が255を超えたら
            err = true;
            err_kind = "incorrect colerspace of image";
            return;
        }
        beforecover(ci);
        dilation(ci);
        cover(ci);
        erosion(ci);
        erosion(ci);
        dilation2(ci);
        dilation2(ci);
        dilation2(ci);
        edge(ci, _cell_points);
        searchNeck();

        cell = BudValidation.validation(cell, _cell_points.length, _width);

        serchbrightpoint(_cell_points);
        serchwidepoint(_cell_points);

        setEllipse();
        setCellData();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //DAPI画像の処理
    ////////////////////////////////////////////////////////////////////////////////
    private void procDImage() {
        di = _nucleus_points.clone();//元の画像はとっておく
        if (isDifferentDImage()) {//DAPI画像が大きくずれていたら
            err = true;
            if (err_kind.equals("")) err_kind = "gap of DAPI image to cell image";
            return;
        }

        rethresh(di, _nucleus_points, Ddiff);
        depth(di);
        setDState();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //actin image processing
    ////////////////////////////////////////////////////////////////////////////////
    private void procAImage() {
        ai = _actin_points.clone();//元の画像はとっておく
        if (isDifferentAImage()) {//actin画像が大きくずれていたら
            err = true;
            if (err_kind.equals("")) err_kind = "gap of actin image to cell image";
            return;
        }
        rethresh(ai, _actin_points, Adiff);
        searchActinRegion();
        searchActinPatch();
        setAState();


    }

    // calculate percentiles of array
    public static short[] percentiles(short[] latencies, double... percentiles) {
        short[] latenciesSorted = latencies.clone();
        Arrays.sort(latenciesSorted, 0, latenciesSorted.length);
        short[] values = new short[percentiles.length];
        for (int i = 0; i < percentiles.length; i++) {
            int index = (int) (percentiles[i] * latenciesSorted.length);
            values[i] = latenciesSorted[index];
        }
        return values;
    }

    ///////////////////////////////////////////////////////////////////////////
    //Read image file
    ///////////////////////////////////////////////////////////////////////////
    private BufferedImage getBufferedImage(String filename) {
        try {
            //return JPEGCodec.createJPEGDecoder(new FileInputStream(file)).decodeAsBufferedImage();
            BufferedImage input = ImageIO.read(new File(filename));
            short[] pixels = ((DataBufferUShort) input.getRaster().getDataBuffer()).getData();
            short[] per = percentiles(pixels, 0.8, 0.999);
            short min = per[0];
            short max = per[1];
            double norm = 255 / ((max - min) * 1.0);
            short[] pixelsNorm = new short[pixels.length];
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] < min) {
                    pixelsNorm[i] = 0;
                } else if (pixels[i] > max) {
                    pixelsNorm[i] = 255;
                } else {
                    pixelsNorm[i] = (short) ((pixels[i] - min) * norm);
                }
            }
            //System.out.println(Arrays.toString(pixelsNorm));
            BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
            final short[] a = ((DataBufferUShort) output.getRaster().getDataBuffer()).getData();
            System.arraycopy(pixelsNorm, 0, a, 0, pixelsNorm.length);
            return output;
        } catch (IOException ioe) {
            _logger.error(ioe);
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //Median filter removes image noise
    ///////////////////////////////////////////////////////////////////////////
    private void medianim(int[] Cim) {
        int[] newci = new int[_size];
        for (int i = 0; i < _height; i++) {
            for (int j = 0; j < _width; j++) {
                if (i == 0 || i == _height - 1 || j == 0 || j == _width - 1) {
                    newci[i * _width + j] = Cim[i * _width + j];
                } else {
                    int[] brightness = new int[9];
                    brightness[0] = Cim[i * _width + j - 1 - _width];
                    brightness[1] = Cim[i * _width + j - 1];
                    brightness[2] = Cim[i * _width + j - 1 + _width];
                    brightness[3] = Cim[i * _width + j - _width];
                    brightness[4] = Cim[i * _width + j];
                    brightness[5] = Cim[i * _width + j + _width];
                    brightness[6] = Cim[i * _width + j + 1 - _width];
                    brightness[7] = Cim[i * _width + j + 1];
                    brightness[8] = Cim[i * _width + j + 1 + _width];
                    Arrays.sort(brightness);
                    newci[i * _width + j] = brightness[4];
                }
            }
        }
        if (_size >= 0) System.arraycopy(newci, 0, Cim, 0, _size);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Roughly separate the cell part from the background part
    ///////////////////////////////////////////////////////////////////////////
    private void vivid(int[] Cim, int gradthresh) {
        int[] newci = new int[_size];
        System.arraycopy(Cim, 0, newci, 0, _size);
        int minbr = 255;
        int minpo = -1;
        for (int i = 0; i < _height; i++) {
            for (int j = 0; j < _width; j++) {
                if (!(i == 0 || i == _height - 1 || j == 0 || j == _width - 1) && Cim[i * _width + j] < minbr) {
                    minbr = Cim[i * _width + j];
                    minpo = i * _width + j;
                }
            }
        }
        boolean[] check = new boolean[_size];
        Stack<Integer> stk = new Stack<>();
        stk.push(minpo);
        check[minpo] = true;
        while (!stk.empty()) {
            int p = stk.pop();
            if (!((p - _width < 0 || Cim[p] - Cim[p - _width] < gradthresh) && (p + _width >= _size || Cim[p] - Cim[p + _width] < gradthresh) && (p % _width == 0 || Cim[p] - Cim[p - 1] < gradthresh) && (p % _width == _width - 1 || Cim[p] - Cim[p + 1] < gradthresh)))
                continue;
            newci[p] = 0;
            if (p - _width >= 0 && !check[p - _width] && Cim[p - _width] - Cim[p] < gradthresh) {
                stk.push(p - _width);
            }
            if (p - _width >= 0) check[p - _width] = true;
            if (p + _width < _size && !check[p + _width] && Cim[p + _width] - Cim[p] < gradthresh) {
                stk.push(p + _width);
            }
            if (p + _width < _size) check[p + _width] = true;
            if (p % _width != 0 && !check[p - 1] && Cim[p - 1] - Cim[p] < gradthresh) {
                stk.push(p - 1);
            }
            if (p % _width != 0) check[p - 1] = true;
            if (p % _width != _width - 1 && !check[p + 1] && Cim[p + 1] - Cim[p] < gradthresh) {
                stk.push(p + 1);
            }
            if (p % _width != _width - 1) check[p + 1] = true;
        }
        if (_size >= 0) System.arraycopy(newci, 0, Cim, 0, _size);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Take the difference between two results multiplied by vivid with different thresholds
    ///////////////////////////////////////////////////////////////////////////
    private int[] dif(int[] ci, int[] ci2) {
        int[] difci = new int[_size];
        for (int i = 0; i < _size; i++) {
            if (ci[i] == ci2[i]) difci[i] = 255;
            else difci[i] = 0;
        }
        return difci;
    }

    ///////////////////////////////////////////////////////////////////////////
    //Divide the difference determined by dif into the part that is
    // determined to be the background and the part that is not
    ///////////////////////////////////////////////////////////////////////////
    private void division(int[] ci, int[] ci2, int[] difci) {
        Vector[] vec = label(difci, 0, 10, true);
        int[] thci = new int[_size];
        for (int i = 0; i < _size; i++) {
            if (ci2[i] == 0) thci[i] = 255;
            else thci[i] = 0;
        }
        Vector[] vec2 = label(thci, 0, 3000, false);
        //(edited by yechang 200 -> 3000)
        int[] pixelToArea = new int[_size];
        for (int i = 0; i < _size; i++) pixelToArea[i] = -1;
        for (int i = 0; i < vec2.length; i++) {
            for (int j = 0; j < vec2[i].size(); j++) {
                pixelToArea[(Integer) vec2[i].get(j)] = i;
            }
        }
        for (Vector vector : vec) {
            int neighbor = -1;
            boolean check = false;
            for (Object o : vector) {
                int p = (Integer) o;
                int[] stk = new int[4];
                stk[0] = p - _width;
                stk[1] = p - 1;
                stk[2] = p + 1;
                stk[3] = p + _width;
                for (int k = 0; k < 4; k++) {
                    if (neighbor != -1 && pixelToArea[stk[k]] != -1 && pixelToArea[stk[k]] != neighbor) {
                        check = true;
                        break;
                    } else if (pixelToArea[stk[k]] != -1) neighbor = pixelToArea[stk[k]];
                }
                if (check) break;
            }
            if (check) {
                for (Object o : vector) ci[(Integer) o] = 0;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //Find weighted brightness gradient and scale to 0-255
    ///////////////////////////////////////////////////////////////////////////
    private int[] gradim(int[] Cim) {
        int[] Cimage = Cim.clone();
        for (int i = 0; i < _size; i++) {
            if (Cimage[i] < 60 && Cimage[i] >= 10) Cimage[i] -= (60 - Cimage[i]) * (60 - Cimage[i]) / 20;
            else if (Cimage[i] < 10) Cimage[i] -= 125;
            else Cimage[i] = 60;
        }
        double[] gradmag = new double[_size];
        double maxmag = 0;
        for (int i = 1; i < _height - 1; i++) {
            for (int j = 1; j < _width - 1; j++) {
                if (Cimage[i * _width + j] != -125 && Cimage[i * _width + j - 1] != -125 && Cimage[i * _width + j + 1] != -125 && Cimage[i * _width + j - _width] != -125 && Cimage[i * _width + j + _width] != -125 && Cimage[i * _width + j - _width - 1] != -125 && Cimage[i * _width + j + _width + 1] != -125 && Cimage[i * _width + j + _width - 1] != -125 && Cimage[i * _width + j - _width + 1] != -125) {
                    gradmag[i * _width + j] = Math.sqrt((2 * (Cimage[i * _width + j - 1] - Cimage[i * _width + j + 1]) + (Cimage[i * _width + j - 1 + _width] - Cimage[i * _width + j + 1 + _width]) + (Cimage[i * _width + j - 1 - _width] - Cimage[i * _width + j + 1 - _width])) * (2 * (Cimage[i * _width + j - 1] - Cimage[i * _width + j + 1]) + (Cimage[i * _width + j - 1 + _width] - Cimage[i * _width + j + 1 + _width]) + (Cimage[i * _width + j - 1 - _width] - Cimage[i * _width + j + 1 - _width])) + (2 * (Cimage[i * _width + j - _width] - Cimage[i * _width + j + _width]) + (Cimage[i * _width + j - _width + 1] - Cimage[i * _width + j + _width + 1]) + (Cimage[i * _width + j - _width - 1] - Cimage[i * _width + j + _width - 1])) * (2 * (Cimage[i * _width + j - _width] - Cimage[i * _width + j + _width]) + (Cimage[i * _width + j - _width + 1] - Cimage[i * _width + j + _width + 1]) + (Cimage[i * _width + j - _width - 1] - Cimage[i * _width + j + _width - 1])));
                    if (gradmag[i * _width + j] > maxmag) maxmag = gradmag[i * _width + j];
                }
            }
        }
        for (int i = 1; i < _height - 1; i++) {
            for (int j = 1; j < _width - 1; j++) {
                if (Cimage[i * _width + j] != -125 && (Cimage[i * _width + j - 1] == -125 || Cimage[i * _width + j + 1] == -125 || Cimage[i * _width + j - _width] == -125 || Cimage[i * _width + j + _width] == -125 || Cimage[i * _width + j - _width - 1] == -125 || Cimage[i * _width + j + _width + 1] == -125 || Cimage[i * _width + j + _width - 1] == -125 || Cimage[i * _width + j - _width + 1] == -125)) {
                    gradmag[i * _width + j] = maxmag / 2;
                }
            }
        }
        for (int i = 0; i < _height; i++) {
            for (int j = 0; j < _width; j++) {
                if (i == 0 || i == _height - 1 || j == 0 || j == _width - 1) {
                    gradmag[i * _width + j] = 1;
                    continue;
                }
                gradmag[i * _width + j] = 1 - gradmag[i * _width + j] / maxmag;
            }
        }
        int[] test = new int[_size];
        for (int i = 0; i < _size; i++) test[i] = 255 - Math.round((float) (gradmag[i] * 255));
        return test;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //Binary array of arguments and put into argument array
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private boolean threshold(int[] image) {
        int[] hg = new int[256];
        for (int i = 0; i < 256; i++) {
            hg[i] = 0;
        }
        double ut = 0;
        for (int i = 0; i < _size; i++) {
            if (image[i] > 255) {
                return false;
            }
            hg[image[i]]++;
        }
        for (int i = 0; i < 256; i++) {
            ut += (double) (i) * (double) (hg[i]) / (double) (_size);
        }
        double maxv = 0;
        double wk = (double) (hg[0]) / (double) (_size);
        double uk = 0;
        double sk = 0;
        int maxk = 0;
        for (int k = 1; k < 255; k++) {
            if (wk > 0 && wk < 1) {
                sk = (ut * wk - uk) * (ut * wk - uk) / (wk * (1 - wk));
                if (maxv < sk) {
                    maxv = sk;
                    maxk = k - 1;
                }
            }
            uk += (double) (hg[k]) * (double) (k) / (double) (_size);
            wk += (double) (hg[k]) / (double) (_size);
        }
        for (int i = 0; i < _size; i++) {
            if (image[i] >= maxk) image[i] = 0;
            else image[i] = 255;
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //Fill in the binarized array for the argument and put it in the argument array
    /////////////////////////////////////////////////////////////////////////////////////////////////
    private void cover(int[] biimage) {
        Vector[] vec = label(biimage, 255, 0, false);
        int maxsize = 0, maxi = 0;
        for (int i = 0; i < vec.length; i++) {
            if (maxsize < vec[i].size()) {
                maxsize = vec[i].size();
                maxi = i;
            }
        }
        for (int i = 0; i < vec.length; i++) {
            if (i != maxi) {
                for (int j = 0; j < vec[i].size(); j++) {
                    int p = (Integer) vec[i].get(j);
                    biimage[p] = 0;
                }
            }
        }
    }

    private void beforecover(int[] biimage) {
        Vector[] vec = label(biimage, 255, 0, true);
        for (int i = 0; i < _size; i++) biimage[i] = 255;
        for (Vector vector : vec) {
            for (Object o : vector) {
                int p = (Integer) o;
                biimage[p] = 0;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // take binary image and label color color
    // minco--use anything smaller than this value
    // cornercut--true means everything on the edge of the image
    /////////////////////////////////////////////////////////////////////////////
    public Vector[] label(int[] biimage, int color, int minco, boolean cornercut) {
        Vector[] vec;
        Vector<Integer> same;
        int[] lab = new int[_size];

        int nlbl = 0;
        same = new Vector<>();
        for (int i = 0; i < _size; i++) {
            lab[i] = -1;
        }
        if (biimage[0] == color) {
            lab[0] = nlbl;
            same.add(nlbl++);
        }
        for (int j = 1; j < _width; j++) {
            if (biimage[j] == color) {
                if (lab[j - 1] >= 0) {
                    lab[j] = lab[j - 1];
                } else {
                    lab[j] = nlbl;
                    same.add(nlbl++);
                }
            }
        }
        for (int i = 1; i < _height; i++) {
            if (biimage[i * _width] == color) {
                if (lab[(i - 1) * _width] >= 0) {
                    lab[i * _width] = lab[(i - 1) * _width];
                } else {
                    lab[i * _width] = nlbl;
                    same.add(nlbl++);
                }
            }
            for (int j = 1; j < _width; j++) {
                if (biimage[i * _width + j] == color) {
                    int a1, a2;
                    if (lab[i * _width + j - 1] >= 0) a1 = smallestlabel(same, lab[i * _width + j - 1]);
                    else a1 = -1;
                    if (lab[(i - 1) * _width + j] >= 0) a2 = smallestlabel(same, lab[(i - 1) * _width + j]);
                    else a2 = -1;
                    if (a1 == -1 && a2 == -1) {
                        lab[i * _width + j] = nlbl;
                        same.add(nlbl++);
                    } else if (a1 == -1) {
                        lab[i * _width + j] = a2;
                    } else if (a2 == -1) {
                        lab[i * _width + j] = a1;
                    } else if (a1 < a2) {
                        lab[i * _width + j] = a1;
                        same.set(a2, a1);
                    } else {
                        lab[i * _width + j] = a2;
                        same.set(a1, a2);
                    }
                }
            }
        }
        int maxl = -1;
        for (int i = 0; i < same.size(); i++) {
            int s = smallestlabel(same, i);
            if (maxl < s) maxl = s;
            same.set(i, s);
        }
        nlbl = maxl;
        Vector[] vec2 = new Vector[nlbl + 1];
        for (int i = 0; i < nlbl + 1; i++) {
            vec2[i] = new Vector();
        }
        for (int i = 0; i < _size; i++) {
            if (lab[i] >= 0) {
                vec2[same.get(lab[i])].add(i);
            }
        }
        int num = 0;
        boolean[] flag = new boolean[nlbl + 1];//塊とみなすかどうか
        for (int i = 0; i < nlbl + 1; i++) {
            if (vec2[i].size() > minco) {//サイズ以上の塊について
                if (!cornercut) {//cornercutが指定されていなければ
                    flag[i] = true;
                    num++;
                } else {//cornercutが指定されていれば
                    flag[i] = true;
                    for (int j = 0; j < vec2[i].size(); j++) {
                        int p = (Integer) vec2[i].get(j);
                        if (p < _width || p > _width * (_height - 1) || p % _width == 0 || p % _width == _width - 1) {//壁に接するpixelが存在
                            flag[i] = false;
                            break;
                        }
                    }
                    if (flag[i]) num++;
                }
            } else {
                flag[i] = false;
            }
        }
        vec = new Vector[num];
        int index = 0;
        for (int i = 0; i < vec.length; i++) {
            vec[i] = new Vector();
            while (index < nlbl + 1) {
                if (flag[index]) break;
                index++;
            }
            if (index < nlbl + 1) {
                for (int k = 0; k < vec2[index].size(); k++) {
                    vec[i].add(vec2[index].get(k));
                }
                index++;
            } else {
                break;
            }
        }

        return vec;
    }

    public Vector[] label(int[] grey, int color, int minco, boolean cornercut, int wid, int hei) {
        Vector[] vec;
        Vector<Integer> same;
        int[] lab = new int[wid * hei];

        int nlbl = 0;
        same = new Vector<>();
        for (int i = 0; i < wid * hei; i++) {
            lab[i] = -1;
        }
        if (grey[0] == color) {
            lab[0] = nlbl;
            same.add(nlbl++);
        }
        for (int j = 1; j < wid; j++) {
            if (grey[j] == color) {
                if (lab[j - 1] >= 0) {
                    lab[j] = smallestlabel(same, lab[j - 1]);
                } else {
                    lab[j] = nlbl;
                    same.add(nlbl++);
                }
            }
        }
        for (int i = 1; i < hei; i++) {
            if (grey[i * wid] == color) {
                if (lab[(i - 1) * wid] >= 0) {
                    lab[i * wid] = smallestlabel(same, lab[(i - 1) * wid]);
                } else {
                    lab[i * wid] = nlbl;
                    same.add(nlbl++);
                }
            }
            for (int j = 1; j < wid; j++) {
                if (grey[i * wid + j] == color) {
                    int a1, a2;
                    if (lab[i * wid + j - 1] >= 0) a1 = smallestlabel(same, lab[i * wid + j - 1]);
                    else a1 = -1;
                    if (lab[(i - 1) * wid + j] >= 0) a2 = smallestlabel(same, lab[(i - 1) * wid + j]);
                    else a2 = -1;
                    if (a1 == -1 && a2 == -1) {
                        lab[i * wid + j] = nlbl;
                        same.add(nlbl++);
                    } else if (a1 == -1) {
                        lab[i * wid + j] = a2;
                    } else if (a2 == -1) {
                        lab[i * wid + j] = a1;
                    } else if (a1 < a2) {
                        lab[i * wid + j] = a1;
                        same.set(a2, a1);
                    } else {
                        lab[i * wid + j] = a2;
                        same.set(a1, a2);
                    }
                }
            }
        }
        int maxl = -1;
        for (int i = 0; i < same.size(); i++) {
            int s = smallestlabel(same, i);
            if (maxl < s) maxl = s;
            same.set(i, s);
        }
        nlbl = maxl;
        Vector[] vec2 = new Vector[nlbl + 1];
        for (int i = 0; i < nlbl + 1; i++) {
            vec2[i] = new Vector();
        }
        for (int i = 0; i < wid * hei; i++) {
            if (lab[i] >= 0) {
                vec2[same.get(lab[i])].add(i);
            }
        }
        int num = 0;
        boolean[] flag = new boolean[nlbl + 1];//塊とみなすかどうか
        for (int i = 0; i < nlbl + 1; i++) {
            if (vec2[i].size() > minco) {//サイズ以上の塊について
                if (!cornercut) {//cornercutが指定されていなければ
                    flag[i] = true;
                    num++;
                } else {//cornercutが指定されていれば
                    flag[i] = true;
                    for (int j = 0; j < vec2[i].size(); j++) {
                        int p = (Integer) vec2[i].get(j);
                        if (p < wid || p > wid * (hei - 1) || p % wid == 0 || p % wid == wid - 1) {//壁に接するpixelが存在
                            flag[i] = false;
                            break;
                        }
                    }
                    if (flag[i]) num++;
                }
            } else {
                flag[i] = false;
            }
        }
        vec = new Vector[num];
        int index = 0;
        for (int i = 0; i < vec.length; i++) {
            vec[i] = new Vector();
            while (index < nlbl + 1) {
                if (flag[index]) break;
                index++;
            }
            if (index < nlbl + 1) {
                for (int k = 0; k < vec2[index].size(); k++) {
                    vec[i].add(vec2[index].get(k));
                }
                index++;
            } else {
                break;
            }
        }
        return vec;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //ラベル付けに使う、木をつぶす
    ////////////////////////////////////////////////////////////////////////////////
    private int smallestlabel(Vector<Integer> same, int label) {
        int now = label;
        Vector<Integer> temp = new Vector<>();
        while (true) {
            if (same.get(now) == now) {
                for (int i = 0; i < temp.size(); i++) {
                    same.set(temp.elementAt(i), now);
                }
                return now;
            } else {
                temp.add(now);
                now = same.get(now);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //binary imageの黒い領域を一枚けずる
    /////////////////////////////////////////////////////////////////////////////////////
    private void erosion(int[] biimage) {
        int[] imagetemp = new int[_width * _height];
        if (_size >= 0) System.arraycopy(biimage, 0, imagetemp, 0, _size);
        for (int i = 0; i < _size; i++) {
            if (i % _width - 1 > 0) imagetemp[i] |= biimage[i - 1];
            if (i % _width + 1 < _width) imagetemp[i] |= biimage[i + 1];
            if (i / _width - 1 > 0) imagetemp[i] |= biimage[i - _width];
            if (i / _width + 1 < _height) imagetemp[i] |= biimage[i + _width];
        }
        if (_size >= 0) System.arraycopy(imagetemp, 0, biimage, 0, _size);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //黒い領域を一枚ふやす
    /////////////////////////////////////////////////////////////////////////////////////
    private void dilation(int[] biimage) {
        int[] imagetemp = new int[_width * _height];
        if (_width * _height >= 0) System.arraycopy(biimage, 0, imagetemp, 0, _width * _height);
        for (int i = 0; i < _width * _height; i++) {
            if (i % _width - 1 > 0 && i % _width + 1 < _width) imagetemp[i] &= biimage[i - 1];
            if (i % _width + 1 < _width && i % _width - 1 > 0) imagetemp[i] &= biimage[i + 1];
            if (i / _width - 1 > 0 && i / _width + 1 < _height) imagetemp[i] &= biimage[i - _width];
            if (i / _width + 1 < _height && i / _width - 1 > 0) imagetemp[i] &= biimage[i + _width];
        }
        if (_width * _height >= 0) System.arraycopy(imagetemp, 0, biimage, 0, _width * _height);
    }

    private void dilation2(int[] biimage) {
        Vector[] vec = label(biimage, 0, 0, false);
        int[] group = new int[_width * _height];
        for (int i = 0; i < _width * _height; i++) group[i] = -1;
        for (int i = 0; i < vec.length; i++) {
            for (int j = 0; j < vec[i].size(); j++) {
                group[(Integer) vec[i].get(j)] = i;
            }
        }
        int[] group2 = new int[_width * _height];
        for (int i = 0; i < _width * _height; i++) group2[i] = -1;
        for (int i = 0; i < _width * _height; i++) {
            if (i % _width > 0 && i % _width < _width - 1 && i / _width > 0 && i / _width < _height - 1) {
                int gr = -1;
                boolean check = true;
                if (group[i - 1] != -1) {
                    gr = group[i - 1];
                }
                if (group[i + 1] != -1) {
                    check = (gr == -1 || group[i + 1] == gr);
                    gr = group[i + 1];
                }
                if (group[i - _width] != -1) {
                    check &= (gr == -1 || group[i - _width] == gr);
                    gr = group[i - _width];
                }
                if (group[i + _width] != -1) {
                    check &= (gr == -1 || group[i + _width] == gr);
                    gr = group[i + _width];
                }
                if (check) group2[i] = gr;
            }
        }
        for (int i = 0; i < _width * _height; i++) {
            if (group2[i] != -1 && (group2[i - 1] == -1 || group2[i - 1] == group2[i]) && (group2[i + 1] == -1 || group2[i + 1] == group2[i]) && (group2[i - _width] == -1 || group2[i - _width] == group2[i]) && (group2[i + _width] == -1 || group2[i + _width] == group2[i])) {
                biimage[i] = 0;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //edgeを探す
    /////////////////////////////////////////////////////////////////////////////////////
    private void edge(int[] image, int[] oriimage) {
        int[] image2 = new int[_size];
        Vector[] vec = label(image, 0, 3000, true);
        //Consider more than 200 clumps as cells (edited by yechang 200 -> 3000)
        Vector[] vec2 = new Vector[vec.length];
        cell = new Cell[vec.length];
        pixeltocell = new int[_size];
        pixeltocell2 = new int[_size];
        for (int i = 0; i < _size; i++) {
            pixeltocell[i] = -1;
            pixeltocell2[i] = -1;
        }
        for (int i = 0; i < vec.length; i++) {
            cell[i] = new Cell(_width, _height, startid + i);
            cell[i].setGroup(1);
            for (int j = 0; j < vec[i].size(); j++) {
                int p = (Integer) vec[i].get(j);
                pixeltocell[p] = i;
                if (image[p - _width] == 0 && image[p - 1] == 0 && image[p + 1] == 0 && image[p + _width] == 0) { //vecには細胞の部分のうちふちだけを記憶
                    vec[i].remove(j);
                    j--;
                }
            }
        }
        for (int i = 0; i < _size; i++) {
            image[i] = 255;
        }
        for (Vector vector : vec) { //imageを細胞のふち部分だけ黒で残りが白となるようにセット
            for (Object o : vector) {
                image[(Integer) o] = 0;
            }
        }
        for (int i = 0; i < vec.length; i++) {//細胞内部（輪郭より内側）に接していないものはすてる
            for (int j = 0; j < vec[i].size(); j++) {
                int p = (Integer) vec[i].get(j);
                if ((image[p - _width] == 0 || pixeltocell[p - _width] != i) && (image[p - 1] == 0 || pixeltocell[p - 1] != i) &&
                        (image[p + 1] == 0 || pixeltocell[p + 1] != i) && (image[p + _width] == 0 || pixeltocell[p + _width] != i)) {
                    vec[i].remove(j);
                    j--;
                    image[p] = 255;
                    pixeltocell[p] = -1;
                }
            }
        }
        for (int j = 0; j < _size; j++) {
            pixeltocell2[j] = pixeltocell[j];
            image2[j] = image[j];
        }
        for (int i = 0; i < vec.length; i++) {
            vec2[i] = new Vector();
            for (int j = 0; j < vec[i].size(); j++) {
                int p = (Integer) vec[i].get(j);
                vec2[i].add(p);
            }
        }
        edgecorrect1(vec, image, oriimage);
        for (int i = 0; i < _size; i++) {
            if (pixeltocell[i] != -1) {
                cell[pixeltocell[i]].cover.add(i);
            }
        }
        boolean[] check = new boolean[_size];
        for (int i = 0; i < _size; i++) {
            check[i] = true;
        }
        for (int i = 0; i < cell.length; i++) {
            int p = (Integer) vec[i].get(0);
            if (nextpoint(image, i, p, check, p) && vec[i].size() == cell[i].edge.size()) {
            } else {
                cell[i].budcrush = 2;
            }
        }
        for (Cell value : cell) {//One point of extremely bright cells in the complex Sawai additional part
            if (value.edge.size() > 0) {
                int[] br = new int[256];
                for (int j = 0; j < 256; j++) br[j] = 0;
                for (int j = 0; j < value.edge.size(); j++) {
                    int p = (Integer) value.edge.get(j);
                    br[oriimage[p]]++;
                }
                int r = 0;
                int s = 255;
                while (r <= value.edge.size() / 20) {
                    r += br[s];
                    s--;
                }
                int q = 0;
                r = 0;
                int t = 0;
                for (int j = 0; j < value.edge.size(); j++) {
                    int p = (Integer) value.edge.get(j);
                    if (oriimage[p] <= s) {
                        q += oriimage[p];
                        t++;
                    } else {
                        r += oriimage[p];
                    }
                }
                if (t > 0) q = q / t;
                if (r > 0 && (value.edge.size() - t > 0)) r = r / (value.edge.size() - t);
                if (q < 50 && r > 100 && r - q > 70) value.setGroup(0);
            }
        }
        edgecorrect2(vec2, image2, oriimage);
        for (int i = 0; i < _size; i++) {
            if (pixeltocell2[i] != -1) {
                cell[pixeltocell2[i]].cover_2.add(i);
            }
        }
        for (int i = 0; i < _size; i++) {
            check[i] = true;
        }
        for (int i = 0; i < cell.length; i++) {
            int p = (Integer) vec2[i].get(0);
            if (nextpoint2(image2, i, p, check, p) && vec2[i].size() == cell[i].edge_2.size()) {
            } else {
                cell[i].setGroup(0);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //Follow edge
    ///////////////////////////////////////////////////////////////////////////
    private boolean nextpoint(int[] grey, int i, int p, boolean[] check, int start) {
        cell[i].edge.add(p);
        //System.out.println("set:"+p);
        check[p] = false;
        if (grey[p - 1] == 0 && pixeltocell[p - 1] == i && check[p - 1]) {
            if (nextpoint(grey, i, p - 1, check, start)) {
                return true;
            }
        }
        if (grey[p + 1] == 0 && pixeltocell[p + 1] == i && check[p + 1]) {
            if (nextpoint(grey, i, p + 1, check, start)) {
                return true;
            }
        }
        if (grey[p - _width] == 0 && pixeltocell[p - _width] == i && check[p - _width]) {
            if (nextpoint(grey, i, p - _width, check, start)) {
                return true;
            }
        }
        if (grey[p + _width] == 0 && pixeltocell[p + _width] == i && check[p + _width]) {
            if (nextpoint(grey, i, p + _width, check, start)) {
                return true;
            }
        }
        if (grey[p - _width - 1] == 0 && pixeltocell[p - _width - 1] == i && check[p - _width - 1]) {
            if (nextpoint(grey, i, p - _width - 1, check, start)) {
                return true;
            }
        }
        if (grey[p - _width + 1] == 0 && pixeltocell[p - _width + 1] == i && check[p - _width + 1]) {
            if (nextpoint(grey, i, p - _width + 1, check, start)) {
                return true;
            }
        }
        if (grey[p + _width - 1] == 0 && pixeltocell[p + _width - 1] == i && check[p + _width - 1]) {
            if (nextpoint(grey, i, p + _width - 1, check, start)) {
                return true;
            }
        }
        if (grey[p + _width + 1] == 0 && pixeltocell[p + _width + 1] == i && check[p + _width + 1]) {
            if (nextpoint(grey, i, p + _width + 1, check, start)) {
                return true;
            }
        }
        if (nextTo(p, start) && cell[i].edge.size() > 10) return true;
        else {
            //System.out.println(((Integer)cell[i].edge.get(cell[i].edge.size()-1)).intValue());
            cell[i].edge.remove(cell[i].edge.size() - 1);
            check[p] = true;
            return false;
        }
    }

    private boolean nextpoint2(int[] grey, int i, int p, boolean[] check, int start) {
        cell[i].edge_2.add(p);
        check[p] = false;
        if (grey[p - 1] == 0 && pixeltocell2[p - 1] == i && check[p - 1]) {
            if (nextpoint2(grey, i, p - 1, check, start)) {
                return true;
            }
        }
        if (grey[p + 1] == 0 && pixeltocell2[p + 1] == i && check[p + 1]) {
            if (nextpoint2(grey, i, p + 1, check, start)) {
                return true;
            }
        }
        if (grey[p - _width] == 0 && pixeltocell2[p - _width] == i && check[p - _width]) {
            if (nextpoint2(grey, i, p - _width, check, start)) {
                return true;
            }
        }
        if (grey[p + _width] == 0 && pixeltocell2[p + _width] == i && check[p + _width]) {
            if (nextpoint2(grey, i, p + _width, check, start)) {
                return true;
            }
        }
        if (grey[p - _width - 1] == 0 && pixeltocell2[p - _width - 1] == i && check[p - _width - 1]) {
            if (nextpoint2(grey, i, p - _width - 1, check, start)) {
                return true;
            }
        }
        if (grey[p - _width + 1] == 0 && pixeltocell2[p - _width + 1] == i && check[p - _width + 1]) {
            if (nextpoint2(grey, i, p - _width + 1, check, start)) {
                return true;
            }
        }
        if (grey[p + _width - 1] == 0 && pixeltocell2[p + _width - 1] == i && check[p + _width - 1]) {
            if (nextpoint2(grey, i, p + _width - 1, check, start)) {
                return true;
            }
        }
        if (grey[p + _width + 1] == 0 && pixeltocell2[p + _width + 1] == i && check[p + _width + 1]) {
            if (nextpoint2(grey, i, p + _width + 1, check, start)) {
                return true;
            }
        }
        if (nextTo(p, start) && cell[i].edge_2.size() > 10) return true;
        else {
            cell[i].edge_2.remove(cell[i].edge_2.size() - 1);
            check[p] = true;
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    //True if adjacent points
    ///////////////////////////////////////////////////////////////////////////////
    private boolean nextTo(int p, int q) {
        return p == q - _width - 1 || p == q - _width || p == q - _width + 1 || p == q - 1 || p == q + 1 || p == q + _width - 1 || p == q + _width || p == q + _width + 1;
    }

    ///////////////////////////////////////////////////////////////////////////
    //Edge correction
    ///////////////////////////////////////////////////////////////////////////
    private void edgecorrect1(Vector[] vec, int[] image, int[] oriimage) {
        int counter;
        int brightness;
        int x, n, m, k, flag, flag2, ori, mopoint;
        boolean[] move = new boolean[_size];
        for (int i = 0; i < vec.length; i++) {
            int j = 0;
            ori = vec[i].size();
            flag = 0;
            flag2 = 0;
            while (j < vec[i].size()) {
                if ((vec[i].size() - j) * 4 < ori) flag++;
                n = vec[i].size();
                m = j;
                mopoint = 0;
                for (int a = 0; a < _size; a++) move[a] = false;
                while (j < n) {
                    int p = (Integer) vec[i].get(j);
                    brightness = 0;
                    x = 0;
                    if (pixeltocell[p - 1] == i && image[p - 1] == 255) x -= 1;
                    if (pixeltocell[p + 1] == i && image[p + 1] == 255) x += 1;
                    if (pixeltocell[p - _width] == i && image[p - _width] == 255) x -= _width;
                    if (pixeltocell[p + _width] == i && image[p + _width] == 255) x += _width;
                    if (pixeltocell[p + x] == i && image[p + x] == 255) {
                        brightness = oriimage[p + x];
                    }
                    if (p + x * 2 >= 0 && p + x * 2 < _size && pixeltocell[p + x * 2] == i && image[p + x * 2] == 255 && brightness < oriimage[p + x * 2]) {
                        brightness = oriimage[p + x * 2];
                    }
                    if (p + x * 3 >= 0 && p + x * 3 < _size && pixeltocell[p + x * 3] == i && image[p + x * 3] == 255 && brightness < oriimage[p + x * 3]) {
                        brightness = oriimage[p + x * 3];
                    }
                    if (oriimage[p] < brightness) {
                        move[p] = true;
                    }
                    j++;
                }
                while (m < n) {
                    int p = (Integer) vec[i].get(m);
                    if (move[p] && (move[p - _width - 1] || move[p - _width] || move[p - _width + 1] || move[p - 1] || move[p + 1] || move[p + _width - 1] || move[p + _width] || move[p + _width + 1])) {
                        pixeltocell[p] = -1;
                        image[p] = 255;
                        vec[i].remove(m);
                        m--;
                        j--;
                        n--;
                        mopoint++;
                        if (pixeltocell[p - 1] == i && image[p - 1] == 255) {
                            image[p - 1] = 0;
                            vec[i].add(p - 1);
                        }
                        if (pixeltocell[p + 1] == i && image[p + 1] == 255) {
                            image[p + 1] = 0;
                            vec[i].add(p + 1);
                        }
                        if (pixeltocell[p - _width] == i && image[p - _width] == 255) {
                            image[p - _width] = 0;
                            vec[i].add(p - _width);
                        }
                        if (pixeltocell[p + _width] == i && image[p + _width] == 255) {
                            image[p + _width] = 0;
                            vec[i].add(p + _width);
                        }
                    }
                    m++;
                }
                boolean change = true;
                while (change) {
                    change = false;
                    for (k = 0; k < vec[i].size(); k++) {
                        int p = (Integer) vec[i].get(k);
                        if (!((pixeltocell[p - _width] == i && image[p - _width] == 255) || (pixeltocell[p - 1] == i && image[p - 1] == 255) || (pixeltocell[p + 1] == i && image[p + 1] == 255) || (pixeltocell[p + _width] == i && image[p + _width] == 255)) && (vec[i].size() > 1)) {
                            image[p] = 255;
                            vec[i].remove(k);
                            pixeltocell[p] = -1;
                            if (k < j) j--;
                            k--;
                        }
                    }
                    for (k = 0; k < vec[i].size(); k++) {
                        int p = (Integer) vec[i].get(k);
                        int c = 0;
                        x = 0;
                        if (pixeltocell[p - _width] == -1) c++;
                        else x = -_width;
                        if (pixeltocell[p + _width] == -1) c++;
                        else x = _width;
                        if (pixeltocell[p - 1] == -1) c++;
                        else x = -1;
                        if (pixeltocell[p + 1] == -1) c++;
                        else x = 1;
                        if (c == 3) {
                            image[p] = 255;
                            vec[i].remove(k);
                            pixeltocell[p] = -1;
                            if (k < j) j--;
                            k--;
                            image[p + x] = 0;
                            vec[i].add(p + x);
                            change = true;
                        }
                    }
                }
                if (mopoint < 4) flag2++;
            }
            if (flag2 > 3) cell[i].budcrush = 2;
            else if (flag > 2) cell[i].budcrush = 1;
            n = vec[i].size();
            for (k = 0; k < n; k++) {
                int p = (Integer) vec[i].get(k);
                if (p % _width > 1 && pixeltocell[p - 1] == -1 && image[p - 1] == 255) {
                    pixeltocell[p - 1] = i;
                    image[p - 1] = 0;
                    vec[i].add(p - 1);
                }
                if (p % _width < _width - 1 && pixeltocell[p + 1] == -1 && image[p + 1] == 255) {
                    pixeltocell[p + 1] = i;
                    image[p + 1] = 0;
                    vec[i].add(p + 1);
                }
                if (p - _width >= _width && pixeltocell[p - _width] == -1 && image[p - _width] == 255) {
                    pixeltocell[p - _width] = i;
                    image[p - _width] = 0;
                    vec[i].add(p - _width);
                }
                if (p + _width < _size - _width && pixeltocell[p + _width] == -1 && image[p + _width] == 255) {
                    pixeltocell[p + _width] = i;
                    image[p + _width] = 0;
                    vec[i].add(p + _width);
                }
            }
            for (k = 0; k < vec[i].size(); k++) {
                int p = (Integer) vec[i].get(k);
                if (p - _width >= 0 && p + _width < _size) {
                    if ((pixeltocell[p - _width] == i) && (pixeltocell[p - 1] == i) && (pixeltocell[p + 1] == i) && (pixeltocell[p + _width] == i) && (vec[i].size() > 1)) {
                        image[p] = 255;
                        vec[i].remove(k);
                        k--;
                    }
                }
            }
            for (k = 0; k < vec[i].size(); k++) {
                int p = (Integer) vec[i].get(k);
                if (p - _width >= 0 && p + _width < _size) {
                    if (!((pixeltocell[p - _width] == i && image[p - _width] == 255) || (pixeltocell[p - 1] == i && image[p - 1] == 255) || (pixeltocell[p + 1] == i && image[p + 1] == 255) || (pixeltocell[p + _width] == i && image[p + _width] == 255)) && (vec[i].size() > 1)) {
                        image[p] = 255;
                        vec[i].remove(k);
                        pixeltocell[p] = -1;
                        k--;
                    }
                }
            }
        }
    }


    private void edgecorrect2(Vector[] vec, int[] image, int[] oriimage) {
        int brightness1, brightness2;
        int x, n, m, k, flag, ori;
        boolean[] move = new boolean[_size];
        double counter2 = 0;//ぼやけ画像除去のためのカウンタ　澤井追加部分
        for (int i = 0; i < vec.length; i++) {
            int j = 0;
            while (j < vec[i].size()) {
                n = vec[i].size();
                m = j;
                for (int a = 0; a < _size; a++) move[a] = false;
                while (j < n) {
                    int p = (Integer) vec[i].get(j);
                    brightness1 = -1;
                    brightness2 = 0;
                    x = 0;
                    int xx = 1;
                    if (pixeltocell2[p - 1] == i && image[p - 1] == 255) x -= 1;
                    if (pixeltocell2[p + 1] == i && image[p + 1] == 255) x += 1;
                    if (pixeltocell2[p - _width] == i && image[p - _width] == 255) x -= _width;
                    if (pixeltocell2[p + _width] == i && image[p + _width] == 255) x += _width;
                    if (p + x * 2 >= 0 && p + x * 2 < _size && pixeltocell2[p + x * 2] == i && image[p + x * 2] == 255
                            && p - x * 2 >= 0 && p - x * 2 < _size && pixeltocell2[p - x * 2] == -1 && image[p + x * 2] == 255) {
                        brightness1 = oriimage[p + x * 2] - oriimage[p];
                        brightness2 = oriimage[p] - oriimage[p - x * 2];
                        xx = 2;
                    } else if (pixeltocell2[p + x] == i && image[p + x] == 255 && pixeltocell2[p + x] == -1 && image[p + x] == 255) {
                        brightness1 = oriimage[p + x] - oriimage[p];
                        brightness2 = oriimage[p] - oriimage[p - x];
                        xx = 1;
                    }
                    if (brightness1 >= brightness2 || oriimage[p - x * (xx - 1)] - oriimage[p - x * xx] < 5) { // 追加
                        move[p] = true;
                    }
                    j++;
                }
                while (m < n) {
                    int p = (Integer) vec[i].get(m);
                    if (move[p] && (move[p - _width - 1] || move[p - _width] || move[p - _width + 1] || move[p - 1] || move[p + 1] || move[p + _width - 1] || move[p + _width] || move[p + _width + 1])) {
                        pixeltocell2[p] = -1;
                        image[p] = 255;
                        vec[i].remove(m);
                        m--;
                        j--;
                        n--;
                        if (pixeltocell2[p - 1] == i && image[p - 1] == 255) {
                            image[p - 1] = 0;
                            vec[i].add(p - 1);
                        }
                        if (pixeltocell2[p + 1] == i && image[p + 1] == 255) {
                            image[p + 1] = 0;
                            vec[i].add(p + 1);
                        }
                        if (pixeltocell2[p - _width] == i && image[p - _width] == 255) {
                            image[p - _width] = 0;
                            vec[i].add(p - _width);
                        }
                        if (pixeltocell2[p + _width] == i && image[p + _width] == 255) {
                            image[p + _width] = 0;
                            vec[i].add(p + _width);
                        }
                    }
                    m++;
                }
                boolean change = true;
                while (change) {
                    change = false;
                    for (k = 0; k < vec[i].size(); k++) {
                        int p = (Integer) vec[i].get(k);
                        if (!((pixeltocell2[p - _width] == i && image[p - _width] == 255) || (pixeltocell2[p - 1] == i && image[p - 1] == 255) || (pixeltocell2[p + 1] == i && image[p + 1] == 255) || (pixeltocell2[p + _width] == i && image[p + _width] == 255)) && (vec[i].size() > 1)) {
                            image[p] = 255;
                            vec[i].remove(k);
                            pixeltocell2[p] = -1;
                            if (k < j) j--;
                            k--;
                        }
                    }
                    for (k = 0; k < vec[i].size(); k++) {
                        int p = (Integer) vec[i].get(k);
                        int c = 0;
                        x = 0;
                        if (pixeltocell2[p - _width] == -1) c++;
                        else x = -_width;
                        if (pixeltocell2[p + _width] == -1) c++;
                        else x = _width;
                        if (pixeltocell2[p - 1] == -1) c++;
                        else x = -1;
                        if (pixeltocell2[p + 1] == -1) c++;
                        else x = 1;
                        if (c == 3) {
                            image[p] = 255;
                            vec[i].remove(k);
                            pixeltocell2[p] = -1;
                            if (k < j) j--;
                            k--;
                            image[p + x] = 0;
                            vec[i].add(p + x);
                            change = true;
                        }
                    }
                }
            }
            double counter = 0;//この先、ぼやけ画像除去の操作　澤井追加部分
            for (k = 0; k < vec[i].size(); k++) {
                int p = (Integer) vec[i].get(k);
                x = 0;
                if (pixeltocell2[p - 1] != i && image[p - 1] == 255) x -= 1;
                if (pixeltocell2[p + 1] != i && image[p + 1] == 255) x += 1;
                if (pixeltocell2[p - _width] != i && image[p - _width] == 255) x -= _width;
                if (pixeltocell2[p + _width] != i && image[p + _width] == 255) x += _width;
                if (p + x * 2 >= 0 && p + x * 2 < _size && p - x >= 0 && p - x < _size && oriimage[p] - oriimage[p + x * 2] > oriimage[p - x] - oriimage[p + x])
                    counter += (double) (oriimage[p] - oriimage[p + x * 2]) / (double) oriimage[p];
                else counter += (double) (oriimage[p - x] - oriimage[p + x]) / (double) oriimage[p - x];
            }
            counter /= vec[i].size();
            counter2 += counter;
        }
        if (counter2 / (double) vec.length < 0.046) {
            err = true;
            err_kind = "blur of cell image";
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    //Find a neck point
    //cell[i].mother_edge のセット
    //cell[i].bud_edge    のセット
    //cell[i].neck        のセット
    //////////////////////////////////////////////////////////////////////////////
    private void searchNeck() {
        int scorerad = 10;  //10
        int scoremeanrad = 2; // 2
        int scorethr = 920;  //920
        Vector<Integer> tmp = new Vector<>();
        for (int i = 0; i < cell.length; i++) {
            int es;
            boolean neck1 = false;
            int[] score, scoretmp;
            if (cell[i].getGroup() > 0) {
                Vector<Integer> neck = new Vector<>();
                int jj;
                int start;
                while (true) {
                    if (cell[i].budcrush == 2 || cell[i].edge.size() < 10) {
                        cell[i].budcrush = 0;
                        for (int j = 0; j < _size; j++) {
                            if (pixeltocell2[j] == i) pixeltocell[j] = i;
                            if (pixeltocell[j] == i && pixeltocell2[j] == -1) pixeltocell[j] = -1;
                        }
                        cell[i].edge = cell[i].edge_2;
                        cell[i].cover = cell[i].cover_2;
                    }
                    es = cell[i].edge.size();
                    scoretmp = new int[es];
                    score = new int[es];
                    for (int j = 0; j < es; j++) {
                        int p = (Integer) cell[i].edge.get(j);
                        for (int x = -scorerad; x <= scorerad; x++) {//Count of intracellular pixels within radius scorerad
                            for (int y = -scorerad; y <= scorerad; y++) {
                                if (Math.sqrt(x * x + y * y) <= scorerad && p + y * _width + x >= 0 && p + y * _width + x < _size && pixeltocell[p + y * _width + x] == i)
                                    scoretmp[j]++;
                            }
                        }
                    }
                    for (int j = 0; j < es; j++) {
                        int s = 0;
                        for (int k = -scoremeanrad; k <= scoremeanrad; k++) {//scoremeanradの範囲を合計
                            s += scoretmp[(j + es + k) % es];
                        }
                        score[j] = s;
                    }
                    jj = 0;
                    while (true) {
                        if (jj < -es) {
                            break;
                        }
                        if (score[(jj + es) % es] < scorethr) break;
                        jj--;
                    }
                    start = -1;
                    for (int j = 0; j < es; j++) {
                        if (score[(j + jj + es) % es] >= scorethr && start < 0) {
                            start = j;
                        }
                        if (score[(j + jj + es) % es] < scorethr && start >= 0) {
                            neck.add((start + j - 1) / 2);
                            start = -1;
                        }
                    }
                    if (neck.size() < 2 && cell[i].budcrush == 1) {
                        cell[i].budcrush = 0;
                        for (int j = 0; j < _size; j++) {
                            if (pixeltocell2[j] == i) pixeltocell[j] = i;
                            if (pixeltocell[j] == i && pixeltocell2[j] == -1) pixeltocell[j] = -1;
                        }
                        cell[i].edge = cell[i].edge_2;
                        cell[i].cover = cell[i].cover_2;
                        neck.clear();
                    } else break;
                }
                if (neck.size() > 2 || cell[i].edge.size() < 10) {
                    cell[i].bud_ratio = 0;
                    cell[i].setGroup(0);
                    continue;
                }
                if (neck.size() == 2) {//一回目ネック２個
                    int n1 = neck.get(0);
                    int n2 = neck.get(1);
                    if ((n2 - n1) * 2 < es) {
                        for (int j = 0; j < n1; j++) {
                            cell[i].mother_edge.add(cell[i].edge.get((j + jj + es) % es));
                        }
                        for (int j = n1; j < n2; j++) {
                            cell[i].bud_edge.add(cell[i].edge.get((j + jj + es) % es));
                        }
                        for (int j = n2; j < es; j++) {
                            cell[i].mother_edge.add(cell[i].edge.get((j + jj + es) % es));
                        }
                    } else {
                        int n = n1 + es - n2;
                        for (int j = 0; j < n1; j++) {
                            cell[i].bud_edge.add(cell[i].edge.get((j + jj + es) % es));
                        }
                        for (int j = n1; j < n2; j++) {
                            cell[i].mother_edge.add(cell[i].edge.get((j + jj + es) % es));
                        }
                        for (int j = n2; j < es; j++) {
                            cell[i].bud_edge.add(cell[i].edge.get((j + jj + es) % es));
                        }
                    }
                    cell[i].bud_cover = getLinePixel((Integer) cell[i].edge.get((jj + neck.get(0) + es) % es), (Integer) cell[i].edge.get((jj + neck.get(1) + es) % es));
                    flag_tmp = false;
                    cell[i].bud_cover = getAreainBud(i, cell[i].bud_cover, cell[i].mother_edge, cell[i].bud_edge);
                    if (flag_tmp) {
                        Vector tmp_vec = cell[i].bud_edge;
                        cell[i].bud_edge = cell[i].mother_edge;
                        cell[i].mother_edge = tmp_vec;
                    }
                    if (cell[i].bud_cover.size() == cell[i].bud_edge.size()) {//芽のcover領域が小さすぎる場合complexに分類しなおし
                        for (int j = 0; j < cell[i].bud_edge.size(); j++) {
                            int p = (Integer) cell[i].bud_edge.get(j);
                            cell[i].mother_edge.add(p);
                        }
                        cell[i].bud_edge = new Vector();
                        cell[i].bud_ratio = 0;
                        cell[i].setGroup(0);
                    } else {
                        cell[i].bud_ratio = Math.sqrt((double) cell[i].bud_cover.size() / (cell[i].cover.size() - cell[i].bud_cover.size()));
                        if (cell[i].bud_ratio == 0) cell[i].setGroup(1);
                        else if (cell[i].bud_ratio < 0.5) cell[i].setGroup(2);
                        else if (cell[i].bud_ratio < 0.7) cell[i].setGroup(3);
                        else cell[i].setGroup(4);
                    }
                    cell[i].neck = new int[2];
                    for (int j = 0; j < 2; j++) {
                        int k = (jj + neck.get(j) + es) % es;
                        cell[i].neck[j] = (Integer) cell[i].edge.get(k);
                    }
                } else if (neck.size() == 1) {//１回目ネック１個
                    for (int j = 0; j < es; j++) {
                        cell[i].mother_edge.add(cell[i].edge.get((j + jj + es) % es));
                    }
                    cell[i].bud_ratio = 0;
                    cell[i].setGroup(1);
                    neck1 = true;//一回目でネックをひとつ見つけている
                    tmp = neck;
                } else {
                    for (int j = 0; j < es; j++) {
                        cell[i].mother_edge.add(cell[i].edge.get((j + jj + es) % es));
                    }
                    cell[i].bud_ratio = 0;
                    cell[i].setGroup(1);
                }
                //noに分類されたものについてくびれの認識をゆるくしてみる。
                //small以外には変えない
                for (int th = scorethr - 20; th >= 820; th -= 20) {
                    if (cell[i].getGroup() == 1) {
                        jj = 0;
                        while (true) {
                            if (jj < -es) {
                                break;
                            }
                            if (score[(jj + es) % es] < th) break;
                            jj--;
                        }
                        start = -1;
                        neck = new Vector<>();
                        for (int j = 0; j < es; j++) {
                            var i1 = score[(j + jj + es) % es];
                            if (i1 >= th && start < 0) {
                                start = j;
                            }
                            if (i1 < th && start >= 0) {
                                neck.add((start + j - 1) / 2);
                                start = -1;
                            }
                        }
                        if (neck.size() == 2) {
                            Vector<Object> medge = new Vector<>();
                            Vector<Object> bedge = new Vector<>();
                            int n1 = neck.get(0);
                            int n2 = neck.get(1);
                            if ((n2 - n1) * 2 < es) {
                                for (int j = 0; j < n1; j++) {
                                    medge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                                for (int j = n1; j < n2; j++) {
                                    bedge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                                for (int j = n2; j < es; j++) {
                                    medge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                            } else {
                                int n = n1 + es - n2;
                                for (int j = 0; j < n1; j++) {
                                    bedge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                                for (int j = n1; j < n2; j++) {
                                    medge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                                for (int j = n2; j < es; j++) {
                                    bedge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                            }
                            Vector<Integer> bud_cover = getLinePixel((Integer) cell[i].edge.get((jj + neck.get(0) + es) % es), (Integer) cell[i].edge.get((jj + neck.get(1) + es) % es));
                            flag_tmp = false;
                            bud_cover = getAreainBud(i, bud_cover, medge, bedge);
                            if (flag_tmp) {
                                Vector<Object> tmp_vec = bedge;
                                bedge = medge;
                                medge = tmp_vec;
                            }
                            if (bud_cover.size() < bedge.size() + 5) {//芽のcover領域が小さすぎる場合noに分類しなおし
                            } else {
                                double bud_ratio = Math.sqrt((double) bud_cover.size() / (cell[i].cover.size() - bud_cover.size()));
                                if (bud_ratio < 0.5 && bud_ratio > 0) {
                                    cell[i].setGroup(2);//smallに変える
                                } else if (bud_ratio < 0.7 && bud_ratio > 0 && neck1) {//最初に一つネックがみつかってるときのみ
                                    cell[i].setGroup(3);//mediumに変える
                                } else if (bud_ratio <= 1.0 && bud_ratio > 0 && neck1) {//最初に一つネックがみつかってるときのみ
                                    cell[i].setGroup(4);//largeに変える
                                }
                                if (cell[i].getGroup() > 1) {
                                    cell[i].mother_edge = medge;
                                    cell[i].bud_edge = bedge;
                                    cell[i].bud_ratio = bud_ratio;
                                    cell[i].bud_cover = bud_cover;
                                    cell[i].neck = new int[2];
                                    for (int j = 0; j < 2; j++) {
                                        int k = (jj + neck.get(j) + es) % es;
                                        cell[i].neck[j] = (Integer) cell[i].edge.get(k);
                                    }
                                }
                            }
                        } else if (th == 820 && neck1) {//最初にneck一つをみつけ最後までいってもneckが二つ見つからない
                            neck = tmp;
                            int n = neck.get(0);
                            int np = (Integer) cell[i].edge.get((n + jj + es) % es);
                            double mind = _width * _height;
                            int minj = 0;
                            //int nigrad = 10;
                            flag_tmp = false;
                            double prev_d = 0;
                            double d = 0;
                            for (int j = 0; j < cell[i].edge.size(); j++) {
                                int p = (Integer) cell[i].edge.get((n + j + jj + es) % es);
                                prev_d = d;
                                d = Point2D.distance(np % _width, np / _width, p % _width, p / _width);
                                if (flag_tmp) {
                                    if (prev_d < d) {
                                        if (mind > d) {
                                            mind = d;
                                            minj = j;
                                        }
                                    }
                                } else {
                                    if (prev_d > d) {
                                        if (mind > d) {
                                            mind = d;
                                            minj = j;
                                        }
                                        flag_tmp = true;
                                    }
                                }
                            }
                            if ((minj + n + es) % es < n) {
                                neck.add(0, (minj + n + es) % es);
                            } else {
                                neck.add((minj + n + es) % es);
                            }
                            int n1 = neck.get(0);
                            int n2 = neck.get(1);
                            cell[i].mother_edge.clear();
                            if ((n2 - n1) * 2 < es) {
                                for (int j = 0; j < n1; j++) {
                                    cell[i].mother_edge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                                for (int j = n1; j < n2; j++) {
                                    cell[i].bud_edge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                                for (int j = n2; j < es; j++) {
                                    cell[i].mother_edge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                            } else {
                                for (int j = 0; j < n1; j++) {
                                    cell[i].bud_edge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                                for (int j = n1; j < n2; j++) {
                                    cell[i].mother_edge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                                for (int j = n2; j < es; j++) {
                                    cell[i].bud_edge.add(cell[i].edge.get((j + jj + es) % es));
                                }
                            }
                            cell[i].bud_cover = getLinePixel((Integer) cell[i].edge.get((jj + neck.get(0) + es) % es), (Integer) cell[i].edge.get((jj + neck.get(1) + es) % es));
                            flag_tmp = false;
                            cell[i].bud_cover = getAreainBud(i, cell[i].bud_cover, cell[i].mother_edge, cell[i].bud_edge);
                            if (flag_tmp) {
                                Vector tmp_vec = cell[i].bud_edge;
                                cell[i].bud_edge = cell[i].mother_edge;
                                cell[i].mother_edge = tmp_vec;
                            }
                            if (cell[i].bud_cover.size() == cell[i].bud_edge.size()) {//芽のcover領域が小さすぎる場合noに分類しなおし
                                for (int j = 0; j < cell[i].bud_edge.size(); j++) {
                                    int p = (Integer) cell[i].bud_edge.get(j);
                                    cell[i].mother_edge.add(p);
                                }
                                cell[i].bud_edge = new Vector();
                                cell[i].bud_ratio = 0;
                                cell[i].setGroup(1);
                            } else {
                                cell[i].bud_ratio = Math.sqrt((double) cell[i].bud_cover.size() / (cell[i].cover.size() - cell[i].bud_cover.size()));
                                if (cell[i].bud_ratio == 0) cell[i].setGroup(1);
                                else if (cell[i].bud_ratio < 0.5) cell[i].setGroup(2);
                                else if (cell[i].bud_ratio < 0.7) cell[i].setGroup(3);
                                else cell[i].setGroup(4);
                            }
                            cell[i].neck = new int[2];
                            for (int j = 0; j < 2; j++) {
                                int k = (jj + neck.get(j) + es) % es;
                                cell[i].neck[j] = (Integer) cell[i].edge.get(k);
                            }
                        }
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    //Returns the pixel to be painted when drawing a straight line between two points in a vector
    //////////////////////////////////////////////////////////////////////////////
    private Vector<Integer> getLinePixel(int s, int g) {
        int dx = g % _width - s % _width;
        int dy = g / _width - s / _width;
        int x, x_, y, y_, plusx, plusy, c1, c2, d;
        Vector<Integer> line = new Vector<>();

        if (Math.abs(dx) >= Math.abs(dy)) {
            if (dx >= 0) {//始点と方向を決める
                x = s % _width;
                x_ = g % _width;
                y = s / _width;
                if (dy >= 0) {//
                    plusy = 1;
                } else {
                    plusy = -1;
                    dy = -dy;
                }
            } else {
                x = g % _width;
                x_ = s % _width;
                y = g / _width;
                dx = -dx;
                if (dy >= 0) {//
                    plusy = -1;
                } else {
                    plusy = 1;
                    dy = -dy;
                }
            }
            d = 2 * dy - dx;
            c1 = 2 * (dy - dx);
            c2 = 2 * dy;
            line.add(y * _width + x);
            for (int i = x + 1; i <= x_; i++) {
                if (d > 0) {
                    y += plusy;
                    d += c1;
                } else {
                    d += c2;
                }
                line.add(y * _width + i);
            }
        } else {
            if (dy >= 0) {//始点と方向を決める
                y = s / _width;
                y_ = g / _width;
                x = s % _width;
                if (dx >= 0) {
                    plusx = 1;
                } else {
                    plusx = -1;
                    dx = -dx;
                }
            } else {//goal、start入れ替え
                y = g / _width;
                y_ = s / _width;
                x = g % _width;
                dy = -dy;
                if (dx >= 0) {
                    plusx = -1;
                } else {
                    plusx = 1;
                    dx = -dx;
                }
            }
            d = 2 * dx - dy;
            c1 = 2 * (dx - dy);
            c2 = 2 * dx;
            line.add(y * _width + x);
            for (int i = y + 1; i <= y_; i++) {
                if (d > 0) {
                    x += plusx;
                    d += c1;
                } else {
                    d += c2;
                }
                line.add(i * _width + x);
            }
        }
        return line;
    }

    //////////////////////////////////////////////////////////////////////////////
    //Returns a Vector containing the bud area pixels
    //////////////////////////////////////////////////////////////////////////////
    private Vector<Integer> getAreainBud(int c, Vector n, Vector m, Vector b) {
        int top = _height, bottom = 0, left = _width, right = 0;//coverする長方形
        for (Object o1 : m) {
            int p = (Integer) o1;
            if (top > p / _width) top = p / _width;
            if (bottom < p / _width) bottom = p / _width;
            if (left > p % _width) left = p % _width;
            if (right < p % _width) right = p % _width;
        }
        for (Object o : b) {
            int p = (Integer) o;
            if (top > p / _width) top = p / _width;
            if (bottom < p / _width) bottom = p / _width;
            if (left > p % _width) left = p % _width;
            if (right < p % _width) right = p % _width;
        }
        for (Object element : n) {
            int p = (Integer) element;
            if (top > p / _width) top = p / _width;
            if (bottom < p / _width) bottom = p / _width;
            if (left > p % _width) left = p % _width;
            if (right < p % _width) right = p % _width;
        }
        int wid = right - left + 3;
        int hei = bottom - top + 3;
        int s = wid * hei;
        int[] greytemp = new int[s];
        Vector<Integer> ba = new Vector<>();//返すvector

        for (int i = 0; i < s; i++) {
            greytemp[i] = 255;
        }
        //neck、bud_edgeで囲んだ領域を作る
        for (Object item : b) {
            int p = (Integer) item;
            int x = p % _width - left;
            int y = p / _width - top;
            greytemp[y * wid + x + 1 + wid] = 0;//小さいほうの座標
        }
        for (Object value : n) {
            int p = (Integer) value;
            int x = p % _width - left;
            int y = p / _width - top;
            greytemp[y * wid + x + 1 + wid] = 0;//小さいほうの座標
        }
        Vector[] vec = label(greytemp, 255, 0, false, wid, hei);//小さいほうでラベル付け

        if (vec.length == 2) {
            boolean flag_out = false;
            for (int i = 0; i < vec[0].size(); i++) {
                if ((Integer) vec[0].get(i) == 0) {
                    flag_out = true;
                    break;
                }
            }
            if (flag_out) {//vec[0]が外部
                if (cell[c].cover.size() / 2 >= vec[1].size() + b.size()) {//芽の領域確定
                    for (int i = 0; i < cell[c].cover.size(); i++) {
                        int p = (Integer) cell[c].cover.get(i);
                        int x = p % _width - left;
                        int y = p / _width - top;
                        if (y * wid + x + 1 + wid >= 0 && y * wid + x + 1 + wid < wid * hei)
                            greytemp[y * wid + x + 1 + wid] = 0;//あとで修正・・・
                    }
                    for (int i = 0; i < vec[1].size(); i++) {
                        int p = (Integer) vec[1].get(i);//座標を戻す
                        int x = p % wid - 1 + left;
                        int y = p / wid - 1 + top;
                        if (greytemp[p] == 0) ba.add(y * _width + x);
                    }
                    for (Object o : b) {//芽の輪郭部分も加える
                        int p = (Integer) o;
                        ba.add(p);
                    }
                } else {//bud_edgeとmother_edgeを入れ替える
                    for (int i = 0; i < vec[1].size(); i++) {//greytempを埋める
                        int p = (Integer) vec[1].get(i);
                        greytemp[p] = 0;
                    }
                    for (int i = 0; i < cell[c].cover.size(); i++) {//cover領域でbudにされなかったものをいれる
                        int p = (Integer) cell[c].cover.get(i);
                        int x = p % _width - left;
                        int y = p / _width - top;
                        if (greytemp[y * wid + x + 1 + wid] == 255) ba.add(p);
                    }
                    flag_tmp = true;
                }
            } else {//vec[1]が外部
                if (cell[c].cover.size() / 2 >= vec[0].size() + b.size()) {//芽の領域確定
                    for (int i = 0; i < cell[c].cover.size(); i++) {
                        int p = (Integer) cell[c].cover.get(i);
                        int x = p % _width - left;
                        int y = p / _width - top;
                        if (y * wid + x + 1 + wid > 0 && y * wid + x + 1 + wid < wid * hei)
                            greytemp[y * wid + x + 1 + wid] = 0;
                    }
                    for (int i = 0; i < vec[0].size(); i++) {
                        int p = (Integer) vec[0].get(i);//座標を戻す
                        int x = p % wid - 1 + left;
                        int y = p % hei - 1 + top;
                        if (greytemp[p] == 0) ba.add(y * _width + x);
                    }
                    for (Object o : b) {//芽の輪郭部分も加える
                        int p = (Integer) o;
                        ba.add(p);
                    }
                } else {//bud_edgeとmother_edgeを入れ替える
                    for (int i = 0; i < vec[0].size(); i++) {//greytempを埋める
                        int p = (Integer) vec[0].get(i);
                        greytemp[p] = 0;
                    }
                    for (int i = 0; i < cell[c].cover.size(); i++) {//cover領域でbudにされなかったものをいれる
                        int p = (Integer) cell[c].cover.get(i);
                        int x = p % _width - left;
                        int y = p / _width - top;
                        if (greytemp[y * wid + x + 1 + wid] == 255) ba.add(p);
                    }
                    flag_tmp = true;
                }
            }
        } else if (vec.length == 1) {//内部が見つからない
        } else {//たまたま二つ以上に分割されたばあい・・・
            int v = 0;
            for (int i = 1; i < vec.length; i++) {
                v += vec[i].size();
            }
            if (cell[c].cover.size() / 2 >= v + b.size()) {//芽の領域確定
                for (int i = 0; i < cell[c].cover.size(); i++) {
                    int p = (Integer) cell[c].cover.get(i);
                    int x = p % _width - left;
                    int y = p / _width - top;
                    if (y * wid + x + 1 + wid > 0 && y * wid + x + 1 + wid < wid * hei)
                        greytemp[y * wid + x + 1 + wid] = 0;//あとで修正いるかも・・・
                }
                for (int j = 1; j < vec.length; j++) {
                    for (int i = 0; i < vec[j].size(); i++) {
                        int p = (Integer) vec[j].get(i);//座標を戻す
                        int x = p % wid - 1 + left;
                        int y = p / wid - 1 + top;
                        if (greytemp[p] == 0) ba.add(y * _width + x);
                    }
                }
                for (Object o : b) {//芽の輪郭部分も加える
                    int p = (Integer) o;
                    ba.add(p);
                }
            } else {//bud_edgeとmother_edgeを入れ替える
                for (int j = 1; j < vec.length; j++) {
                    for (int i = 0; i < vec[j].size(); i++) {//greytempを埋める
                        int p = (Integer) vec[j].get(i);
                        greytemp[p] = 0;
                    }
                }
                for (int i = 0; i < cell[c].cover.size(); i++) {//cover領域でbudにされなかったものをいれる
                    int p = (Integer) cell[c].cover.get(i);
                    int x = p % _width - left;
                    int y = p / _width - top;
                    if (greytemp[y * wid + x + 1 + wid] == 255) ba.add(p);
                }
                flag_tmp = true;
            }
        }

        return ba;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Find the highest and lowest cell wall brightness
    //////////////////////////////////////////////////////////////////////////////
    private void serchbrightpoint(int[] Cim) {
        for (Cell value : cell) {
            int max = -1;
            int min = 256;
            for (int j = 0; j < value.edge.size(); j++) {
                int br = Cim[(Integer) value.edge.get(j)];
                if (br == max) value.brightestCpoint.add(value.edge.get(j));
                if (br > max) {
                    value.brightestCpoint = new Vector();
                    max = br;
                    value.brightestCpoint.add(value.edge.get(j));
                }
                if (br == min) value.darkestCpoint.add(value.edge.get(j));
                if (br < min) {
                    value.darkestCpoint = new Vector();
                    min = br;
                    value.darkestCpoint.add(value.edge.get(j));
                }
            }
            value.Cmaxbright = max;
            value.Cminbright = min;
        }
    }


    //////////////////////////////////////////////////////////////////////////////
    //Find the highest and lowest cell wall thickness
    //////////////////////////////////////////////////////////////////////////////
    private void serchwidepoint(int[] CIm) {
        int[] image = new int[_size];
        for (int i = 0; i < _size; i++) image[i] = 255;
        for (Cell value : cell) {
            for (int j = 0; j < value.edge.size(); j++) {
                int p = (Integer) value.edge.get(j);
                image[p] = 0;
            }
        }
        for (int i = 0; i < cell.length; i++) {
            double maxw = -1;
            double minw = 20;
            double[] width = new double[cell[i].edge.size()];
            for (int j = 0; j < cell[i].edge.size(); j++) {
                int p = (Integer) cell[i].edge.get(j);
                if (cell[i].getGroup() > 1) {
                    int q1 = cell[i].neck[0];
                    int q2 = cell[i].neck[1];
                    if ((Math.abs(p - q1) % _width <= 2 && Math.abs(p - q1) / _width <= 2) || (Math.abs(p - q2) % _width <= 2 && Math.abs(p - q2) / _width <= 2)) {
                        width[j] = -1;
                        continue;
                    }
                }
                int brightness1 = -1;
                int brightness2 = 0;
                int x = 0;
                if (pixeltocell[p - 1] == i && image[p - 1] == 255) x -= 1;
                if (pixeltocell[p + 1] == i && image[p + 1] == 255) x += 1;
                if (pixeltocell[p - _width] == i && image[p - _width] == 255) x -= _width;
                if (pixeltocell[p + _width] == i && image[p + _width] == 255) x += _width;
                double count = 0;
                while (p + x * 2 >= 0 && p + x * 2 < _size && p % _width < (p + x * 2) % _width + 2 && p % _width > (p + x * 2) % _width - 2) {
                    brightness1 = CIm[p + x * 2] - CIm[p + x];
                    brightness2 = CIm[p + x] - CIm[p];
                    if (CIm[p] < CIm[p + x] || brightness1 < brightness2) {
                        count++;
                    } else break;
                    p += x;
                }
                if (!(x == 1 || x == -1 || x == _width || x == -_width)) count *= 1.4;
                width[j] = count;
            }
            for (int j = 0; j < cell[i].edge.size(); j++) {
                double[] points = new double[5];
                if (width[j] == -1) continue;
                points[0] = width[(j - 2 + cell[i].edge.size()) % cell[i].edge.size()];
                points[1] = width[(j - 1 + cell[i].edge.size()) % cell[i].edge.size()];
                points[2] = width[j];
                points[3] = (j + 1 + cell[i].edge.size()) % cell[i].edge.size();
                points[4] = (j + 2 + cell[i].edge.size()) % cell[i].edge.size();
                Arrays.sort(points);
                if (points[2] == maxw) cell[i].widestCpoint.add(cell[i].edge.get(j));
                if (points[2] > maxw) {
                    cell[i].widestCpoint = new Vector();
                    maxw = points[2];
                    cell[i].widestCpoint.add(cell[i].edge.get(j));
                }
                if (points[2] == minw) cell[i].narrowestCpoint.add(cell[i].edge.get(j));
                if (points[2] < minw) {
                    cell[i].narrowestCpoint = new Vector();
                    minw = points[2];
                    cell[i].narrowestCpoint.add(cell[i].edge.get(j));
                }
            }
            cell[i].Cmaxwidth = (int) maxw;
            cell[i].Cminwidth = (int) minw;
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    //楕円をあてる
    //////////////////////////////////////////////////////////////////////////////
    private void setEllipse() {
        for (Cell value : cell) {
            value.setEllipse();
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    //出力用のデータをセット
    /////////////////////////////////////////////////////////////////////////////
    private void setCellData() {
        for (Cell value : cell) {
            value.setCellData();
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    //画像の出力
    /////////////////////////////////////////////////////////////////////////////
    private void outCImage() {
        int[] pixel = new int[_size];
        for (int i = 0; i < _size; i++) {
            pixel[i] = 0xff000000 | (_cell_points[i] << 16) | (_cell_points[i] << 8) | _cell_points[i];
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image im = toolkit.createImage(new MemoryImageSource(_width, _height, pixel, 0, _width));
        BufferedImage bi = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(Color.red);
        while (!g.drawImage(im, 0, 0, null)) {
        }
        for (Cell value : cell) {
            value.outCImage(g);
        }
        //System.out.println(outdir+"/"+name+"/"+name+"-conA"+number+".jpg");
        writeJPEG(bi, outdir + "/" + name + "/" + name + "-conA" + number + ".jpg");
    }

    /////////////////////////////////////////////////////////////////////////////
    // True if the image is significantly out of alignment
    // If it is slightly off, fix it and false
    /////////////////////////////////////////////////////////////////////////////
    private boolean isDifferentDImage() {
        int[] ci_tmp = _cell_points.clone();
        threshold(ci_tmp);
        int countcarea = 0;
        for (int i = 0; i < _size; i++) {
            if (ci_tmp[i] == 0) countcarea++;
        }
        int[] hg = new int[256];
        for (int i = 0; i < 256; i++) {
            hg[i] = 0;
        }
        for (int i = 0; i < _size; i++) {
            if (di[i] < 256 && di[i] >= 0) hg[di[i]]++;
            else {
                _logger.warn(di[i]);
                err_kind = "incorrect colorspace of dapi image";
                return true;
            } //画像の色がおかしい
        }
        int countdarea = 0;
        for (int i = 255; i >= 0; i--) {
            countdarea += hg[i];
            if (countdarea > countcarea) {
                countdarea -= hg[i];
                for (int j = 0; j < _size; j++) {
                    if (di[j] >= i + 1) di[j] = 0;
                    else di[j] = 255;
                }
                break;
            }
        }
        erosion(di);
        dilation(di);
        int k = 7;
        cover(ci_tmp);
        for (Cell element : cell) {
            for (int j = 0; j < element.cover.size(); j++) {
                int p = (Integer) element.cover.get(j);
                ci_tmp[p] = 0;
            }
        }
        int count = 0;
        for (int i = 0; i < _size; i++) {
            if (di[i] == 0) {
                // Whether there is something outside the cell area
                if (ci_tmp[i] == 255) count++;
            }
        }
        // area is too far out of cell area
        if (count > (countdarea / 2)) {
            return true;
        }
        int countdiff = 0;
        for (int i = 0; i < _size; i++) {
            if (di[i] == 0) {//アクチン領域で
                if (i % _width < k || ci_tmp[i - k] == 255) {//k個左にずらしてセル領域の外のものがあれば
                    countdiff++;
                }
            }
        }
        //System.out.println(count1);
        if (countdiff < count * 0.9) {//動かしたほうがずれが少ない
            return true;
        }
        countdiff = 0;
        for (int i = 0; i < _size; i++) {
            if (di[i] == 0) {
                if (i % _width >= _width - k || ci_tmp[i + k] == 255) {//k個右にずらしてセル領域の外のものがあれば
                    countdiff++;
                }
            }
        }
        //System.out.println(count1);
        if (countdiff < count * 0.9) {//動かしたほうがずれが少ない
            return true;
        }
        countdiff = 0;
        for (int i = 0; i < _size; i++) {
            if (di[i] == 0) {
                if (i / _width < k || ci_tmp[i - k * _width] == 255) {//k個上にずらしてセル領域の外のものがあれば
                    countdiff++;
                }
            }
        }
        //System.out.println(count1);
        if (countdiff < count * 0.9) {//動かしたほうがずれが少ない
            return true;
        }
        countdiff = 0;
        for (int i = 0; i < _size; i++) {
            if (di[i] == 0) {
                if (i / _width >= _height - k || ci_tmp[i + k * _width] == 255) {//k個下にずらしてセル領域の外のものがあれば
                    countdiff++;
                }
            }
        }
        //System.out.println(count1);
        if (countdiff < count * 0.9) {//Less shift when moved
            return true;
        }
        //以下大きくずれてない場合少し修正
        int[] dep = new int[_size];
        cover(di);
        Vector[] vec = label(di, 0, 200, true);
        // (edited by yechang 20 -> 200)
        k = 0;
        int mink = 0;
        Vector[] partedge = new Vector[4];
        boolean[] pixelofactin = new boolean[_size];
        for (int i = 0; i < 4; i++) partedge[i] = new Vector();
        for (Cell item : cell) {
            for (int j = 0; j < item.cover.size(); j++) {
                int p = (Integer) item.cover.elementAt(j);
                if (pixeltocell[p - 1] < 0) partedge[0].add(p);
                if (pixeltocell[p + 1] < 0) partedge[1].add(p);
                if (pixeltocell[p - _width] < 0) partedge[2].add(p);
                if (pixeltocell[p + _width] < 0) partedge[3].add(p);
            }
        }
        for (int i = 0; i < _size; i++) pixelofactin[i] = false;
        for (Vector vector : vec) {
            for (int j = 0; j < vector.size(); j++) {
                pixelofactin[(Integer) vector.elementAt(j)] = true;
            }
        }
        int min = countOutActin(vec, k);
        int value = min;
        for (int i = 0; i < maxdiff; i += 2) {
            for (int j = 1; j < i; j++) {
                value = countOutActin2(partedge[0], partedge[1], pixelofactin, value, k, k - 1);
                k--;
                if (min > value) {
                    min = value;
                    mink = k;
                }
            }
            for (int j = 1; j < i; j++) {
                value = countOutActin2(partedge[2], partedge[3], pixelofactin, value, k, k - _width);
                k -= _width;
                if (min > value) {
                    min = value;
                    mink = k;
                }
            }
            for (int j = 1; j < i + 1; j++) {
                value = countOutActin2(partedge[1], partedge[0], pixelofactin, value, k, k + 1);
                k++;
                if (min > value) {
                    min = value;
                    mink = k;
                }
            }
            for (int j = 1; j < i + 1; j++) {
                value = countOutActin2(partedge[3], partedge[2], pixelofactin, value, k, k + _width);
                k += _width;
                if (min > value) {
                    min = value;
                    mink = k;
                }
            }
        }
        Ddiff = mink;
        return false;
    }

    /////////////////////////////////////////////////////////////////////////////////
    //外に出てるあくちんをかぞえる
    /////////////////////////////////////////////////////////////////////////////////
    private int countOutActin(Vector[] v, int diff) {
        int count = 0;

        for (Vector vector : v) {
            for (int j = 0; j < vector.size(); j++) {
                int p = (Integer) vector.elementAt(j) + diff;
                if (p < 0 || p >= _size || pixeltocell[p] < 0) count++;
            }
        }
        //System.out.println(count);
        return count;
    }

    private int countOutActin2(Vector partedge1, Vector partedge2, boolean[] pixelofactin, int previous, int prediff, int diff) {
        for (int i = 0; i < partedge1.size(); i++) {
            int p = (Integer) partedge1.elementAt(i) - prediff;
            if (p >= 0 && p < _size && pixelofactin[p]) previous++;
        }
        for (int i = 0; i < partedge2.size(); i++) {
            int p = (Integer) partedge2.elementAt(i) - diff;
            if (p >= 0 && p < _size && pixelofactin[p]) previous--;
        }
        return previous;
    }

    ///////////////////////////////////////////////////////////////////////////////
    //imageをcell.coverの範囲で２値化,diffのずれがある
    ///////////////////////////////////////////////////////////////////////////////
    private void rethresh(int[] image, int[] oriimage, int diff) {
        for (int i = 0; i < _size; i++) {
            image[i] = 255;
        }
        int diffx = diff % _width;
        if (diffx >= _width / 2) diffx -= _width;
        else if (diffx <= -_width / 2) diffx += _width;
        for (Cell value : cell) {
            int[] cellarea = new int[value.cover.size()];
            for (int j = 0; j < value.cover.size(); j++) {
                int p = (Integer) value.cover.get(j) - diff;
                if (p < _size && p >= 0 && p / _width == (p + diffx) / _width) cellarea[j] = oriimage[p];
                else cellarea[j] = 0;
            }
            blockthreshold(cellarea);
            for (int j = 0; j < value.cover.size(); j++) {
                int p = (Integer) value.cover.get(j) - diff;
                if (p < _size && p >= 0 && p / _width == (p + diffx) / _width) image[p] = cellarea[j];
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    //cellareaの中だけ２値化
    ///////////////////////////////////////////////////////////////////////////////
    private void blockthreshold(int[] cellarea) {
        int[] hg = new int[256];
        for (int i = 0; i < 256; i++) {
            hg[i] = 0;
        }
        double ut = 0;
        for (int value : cellarea) {
            //System.out.println(cellarea[i]);
            hg[value]++;
        }
        for (int i = 0; i < 256; i++) {
            ut += (double) (i) * (double) (hg[i]) / (double) (cellarea.length);
        }
        double maxv = 0;
        double wk = (double) (hg[0]) / (double) (cellarea.length);
        double uk = 0;
        double sk = 0;
        int maxk = 0;
        for (int k = 1; k < 255; k++) {
            if (wk > 0 && wk < 1) {
                sk = (ut * wk - uk) * (ut * wk - uk) / (wk * (1 - wk));
                if (maxv < sk) {
                    maxv = sk;
                    maxk = k - 1;
                }
            }
            uk += (double) (hg[k]) * (double) (k) / (double) (cellarea.length);
            wk += (double) (hg[k]) / (double) (cellarea.length);
        }
        //System.out.println(maxk);
        if (maxk > 10) {
            for (int i = 0; i < cellarea.length; i++) {
                if (cellarea[i] >= maxk) cellarea[i] = 0;//細胞内部が全部暗い場合は核なし
                else cellarea[i] = 255;
            }
        } else {
            for (int i = 0; i < cellarea.length; i++) {
                cellarea[i] = 255;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Find the center of the nucleus
    //cell[i].Dpoint set
    /////////////////////////////////////////////////////////////////////////////////
    private void depth(int[] biimage) {
        Vector[] vec = label(biimage, 0, 20, true);

        //ここから核の重心を求める処理
        int[] mpoint = new int[vec.length];//重心
        int[][] mpointB = new int[vec.length][2];//Center of gravity of mother cell side and bud side when cut at the neckline in B cells
        Vector<Integer> MafterB = new Vector<>();
        for (Cell cell11 : cell) {
            cell11.setFlagUD(false);
        }
        for (int i = 0; i < vec.length; i++) {
            int r = 1;
            int d = 0;
            int s = 0;
            int pointx = 0;
            int pointy = 0;
            int maxbr = 0;
            for (int j = 0; j < vec[i].size(); j++) {
                int pconA = (Integer) vec[i].get(j) + Ddiff;//細胞画像の位置にする
                int pdapi = (Integer) vec[i].get(j);//DAPI画像の位置にする
                if (pixeltocell[pconA] >= 0) {
                    if (cell[pixeltocell[pconA]].getGroup() >= 2) {
                        int k;
                        if (cell[pixeltocell[pconA]].inmother(pconA)) k = 1;
                        else k = 0;
                        r *= k;
                        d += k;
                    }
                    pointx += pconA % _width;
                    pointy += pconA / _width;
                    s++;
                    if (_nucleus_points[pdapi] > maxbr) maxbr = _nucleus_points[pdapi];
                }
            }
            pointx /= s;
            pointy /= s;
            if (maxbr > 30) mpoint[i] = pointy * _width + pointx;//暗すぎる核は除く
            else mpoint[i] = 0;
            if (pixeltocell[mpoint[i]] >= 0 && r == 0 && d > 0) {//核が母細胞と芽の両方にかかっている……分裂中の核
                cell[pixeltocell[mpoint[i]]].setFlagUD(true);
                int s1 = 0;
                int pointx1 = 0;
                int pointy1 = 0;
                int s2 = 0;
                int pointx2 = 0;
                int pointy2 = 0;
                for (int j = 0; j < vec[i].size(); j++) {
                    int pconA = (Integer) vec[i].get(j) + Ddiff;//細胞画像の位置にする
                    int pdapi = (Integer) vec[i].get(j);//DAPI画像の位置にする
                    if (pixeltocell[pconA] >= 0) {
                        if (cell[pixeltocell[pconA]].inmother(pconA)) {
                            pointx1 += pconA % _width;
                            pointy1 += pconA / _width;
                            s1++;
                        } else {
                            pointx2 += pconA % _width;
                            pointy2 += pconA / _width;
                            s2++;
                        }
                    }
                }
                pointx1 /= s1;
                pointy1 /= s1;
                pointx2 /= s2;
                pointy2 /= s2;
                mpointB[i][0] = pointy1 * _width + pointx1;
                mpointB[i][1] = pointy2 * _width + pointx2;
            }
        }
        for (Cell cell10 : cell) {
            cell10.Dpoint = new Vector();
        }
        for (Cell cell9 : cell) {
            cell9.DpointB = new Vector();
        }
        for (int i = 0; i < vec.length; i++) {//母細胞中の核から先に記録
            if (pixeltocell[mpoint[i]] >= 0 && cell[pixeltocell[mpoint[i]]].inmother(mpoint[i])) {
                MafterB.add(i);
                cell[pixeltocell[mpoint[i]]].Dpoint.add(new Point(mpoint[i] % _width, mpoint[i] / _width));
            }
        }
        for (int i = 0; i < vec.length; i++) {//芽中の核は母細胞中の核の後に記録
            if (pixeltocell[mpoint[i]] >= 0 && !cell[pixeltocell[mpoint[i]]].inmother(mpoint[i])) {
                MafterB.add(i);
                cell[pixeltocell[mpoint[i]]].Dpoint.add(new Point(mpoint[i] % _width, mpoint[i] / _width));
            }
        }
        for (int i = 0; i < vec.length; i++) {
            if (pixeltocell[mpoint[i]] >= 0 && cell[pixeltocell[mpoint[i]]].getFlagUD() && cell[pixeltocell[mpoint[i]]].Dpoint.size() == 1) {
                cell[pixeltocell[mpoint[i]]].DpointB.add(new Point(mpointB[i][0] % _width, mpointB[i][0] / _width));
                cell[pixeltocell[mpoint[i]]].DpointB.add(new Point(mpointB[i][1] % _width, mpointB[i][1] / _width));
            }
        }

        //ここから核の最大輝点と最大輝度を求める処理
        int[] brightpoint = new int[vec.length];
        int[][] brightpointB = new int[vec.length][2];
        for (int i = 0; i < vec.length; i++) {
            int brightness = 0;
            Vector<Integer> bpoint = new Vector<>();
            for (int j = 0; j < vec[i].size(); j++) {
                int p = (Integer) vec[i].get(j);
                if (pixeltocell[p + Ddiff] >= 0) {
                    if (_nucleus_points[p] > brightness) {
                        brightness = _nucleus_points[p];
                        bpoint = new Vector<>();
                        bpoint.add(p);
                    } else if (_nucleus_points[p] == brightness) bpoint.add(p);
                }
            }
            if (bpoint.size() > 1) {//最大輝点が複数点あった場合、それらの重心を使う
                int s = 0;
                int pointx = 0;
                int pointy = 0;
                for (Object o : bpoint) {
                    int pconA = (Integer) o + Ddiff;
                    pointx += pconA % _width;
                    pointy += pconA / _width;
                    s++;
                }
                pointx /= s;
                pointy /= s;
                brightpoint[i] = pointy * _width + pointx;
            } else {
                int pconA = bpoint.get(0) + Ddiff;
                brightpoint[i] = pconA;
            }

            if (pixeltocell[mpoint[i]] >= 0 && cell[pixeltocell[mpoint[i]]].getFlagUD() && cell[pixeltocell[mpoint[i]]].Dpoint.size() == 1) {
                int brightness1 = 0;
                int brightness2 = 0;
                Vector<Integer> bpoint1 = new Vector<>();
                Vector<Integer> bpoint2 = new Vector<>();
                for (int j = 0; j < vec[i].size(); j++) {
                    int p = (Integer) vec[i].get(j);
                    if (pixeltocell[p + Ddiff] >= 0) {
                        if (cell[pixeltocell[p + Ddiff]].inmother(p + Ddiff)) {
                            if (_nucleus_points[p] > brightness1) {
                                brightness1 = _nucleus_points[p];
                                bpoint1 = new Vector<>();
                                bpoint1.add(p);
                            } else if (_nucleus_points[p] == brightness1) bpoint1.add(p);
                        } else {
                            if (_nucleus_points[p] > brightness2) {
                                brightness2 = _nucleus_points[p];
                                bpoint2 = new Vector<>();
                                bpoint2.add(p);
                            } else if (_nucleus_points[p] == brightness2) bpoint2.add(p);
                        }
                    }
                }
                if (bpoint1.size() > 1) {
                    int r = 1;
                    int d = 0;
                    int s = 0;
                    int pointx = 0;
                    int pointy = 0;
                    for (Object o : bpoint1) {
                        int pconA = (Integer) o + Ddiff;
                        pointx += pconA % _width;
                        pointy += pconA / _width;
                        s++;
                    }
                    pointx /= s;
                    pointy /= s;
                    brightpointB[i][0] = pointy * _width + pointx;
                } else {
                    int pconA = bpoint1.get(0) + Ddiff;
                    brightpointB[i][0] = pconA;
                }
                if (bpoint2.size() > 1) {
                    int r = 1;
                    int d = 0;
                    int s = 0;
                    int pointx = 0;
                    int pointy = 0;
                    for (Object o : bpoint2) {
                        int pconA = (Integer) o + Ddiff;
                        pointx += pconA % _width;
                        pointy += pconA / _width;
                        s++;
                    }
                    pointx /= s;
                    pointy /= s;
                    brightpointB[i][1] = pointy * _width + pointx;
                } else {
                    int pconA = bpoint2.get(0) + Ddiff;
                    brightpointB[i][1] = pconA;
                }
            }
        }

        for (Cell cell8 : cell) cell8.Dbrightpoint = new Vector();
        for (Cell cell7 : cell) cell7.DbrightpointB = new Vector();
        for (Cell cell6 : cell) cell6.Dmaxbright = new Vector();
        for (Cell cell5 : cell) cell5.DmaxbrightB = new Vector();

        for (Object o2 : MafterB) {
            int i = (Integer) o2;
            if (pixeltocell[mpoint[i]] >= 0) {
                cell[pixeltocell[mpoint[i]]].Dbrightpoint.add(new Point(brightpoint[i] % _width, brightpoint[i] / _width));
                cell[pixeltocell[mpoint[i]]].Dmaxbright.add(_nucleus_points[brightpoint[i]]);
            }
        }
        for (int i = 0; i < vec.length; i++) {
            if (pixeltocell[mpoint[i]] >= 0 && cell[pixeltocell[mpoint[i]]].getFlagUD() && cell[pixeltocell[mpoint[i]]].Dpoint.size() == 1) {
                cell[pixeltocell[mpoint[i]]].DbrightpointB.add(new Point(brightpointB[i][0] % _width, brightpointB[i][0] / _width));
                cell[pixeltocell[mpoint[i]]].DmaxbrightB.add(_nucleus_points[brightpointB[i][0]]);
                cell[pixeltocell[mpoint[i]]].DbrightpointB.add(new Point(brightpointB[i][1] % _width, brightpointB[i][1] / _width));
                cell[pixeltocell[mpoint[i]]].DmaxbrightB.add(_nucleus_points[brightpointB[i][1]]);
            }
        }

        //ここから核領域と核の輝度合計を求める処理
        boolean[] Dcov = new boolean[_size];//核の外縁を求める際に使用
        for (Cell cell4 : cell) cell4.Dcover = new Vector();
        for (Cell cell3 : cell) cell3.Dtotalbright = new Vector();
        for (Cell cell2 : cell) cell2.DcoverB = new Vector();
        for (Cell cell1 : cell) cell1.DtotalbrightB = new Vector();
        for (Object o1 : MafterB) {
            int i = (Integer) o1;
            if (pixeltocell[mpoint[i]] >= 0) {
                Vector<Integer> Dc = new Vector<>();
                int totalbr = 0;
                for (int j = 0; j < vec[i].size(); j++) {
                    int pconA = (Integer) vec[i].get(j) + Ddiff;
                    Dc.add(pconA);
                    Dcov[pconA] = true;
                    totalbr += _nucleus_points[pconA - Ddiff];
                }
                cell[pixeltocell[mpoint[i]]].Dcover.add(Dc);
                cell[pixeltocell[mpoint[i]]].Dtotalbright.add(totalbr);
                if (cell[pixeltocell[mpoint[i]]].getFlagUD() && cell[pixeltocell[mpoint[i]]].Dpoint.size() == 1) {
                    Vector<Integer> Dc1 = new Vector<>();
                    Vector<Integer> Dc2 = new Vector<>();
                    int totalbr1 = 0;
                    int totalbr2 = 0;
                    for (int j = 0; j < vec[i].size(); j++) {
                        int pconA = (Integer) vec[i].get(j) + Ddiff;
                        if (cell[pixeltocell[mpoint[i]]].inmother(pconA)) {
                            Dc1.add(pconA);
                            totalbr1 += _nucleus_points[pconA - Ddiff];
                        } else {
                            Dc2.add(pconA);
                            totalbr2 += _nucleus_points[pconA - Ddiff];
                        }
                    }
                    cell[pixeltocell[mpoint[i]]].DcoverB.add(Dc1);
                    cell[pixeltocell[mpoint[i]]].DtotalbrightB.add(totalbr1);
                    cell[pixeltocell[mpoint[i]]].DcoverB.add(Dc2);
                    cell[pixeltocell[mpoint[i]]].DtotalbrightB.add(totalbr2);
                }
            }
        }

        //ここから核の外縁を求めるための処理
        for (Cell element : cell) element.Dedge = new Vector();
        Vector[] De = new Vector[vec.length];
        for (Object item : MafterB) {
            int i = (Integer) item;
            if (pixeltocell[mpoint[i]] >= 0) {
                De[i] = new Vector();
                for (int j = 0; j < vec[i].size(); j++) {
                    int pconA = (Integer) vec[i].get(j) + Ddiff;
                    if (!Dcov[pconA - _width] || !Dcov[pconA - 1] || !Dcov[pconA + 1] || !Dcov[pconA + _width])
                        De[i].add(pconA);
                }
                cell[pixeltocell[mpoint[i]]].Dedge.add(De[i]);
            }
        }

        //ここから核の、重心から一番遠い点（D3）、D3から一番遠い点（D4）、D3D4と垂直で重心を通る直線と核の外縁の交点のうち重心から遠い方（D5）を求めるための処理
        for (Cell value : cell) {
            value.D345point = new Vector[3];
            for (int j = 0; j < 3; j++) value.D345point[j] = new Vector();
        }
        for (Object o : MafterB) {
            int i = (Integer) o;
            if (mpoint[i] >= 0) {
                double maxdist = -1;
                int maxpoint = -1;
                for (int j = 0; j < vec[i].size(); j++) {
                    int pconA = (Integer) vec[i].get(j) + Ddiff;
                    if (distance(pconA, mpoint[i]) > maxdist) {
                        maxdist = distance(pconA, mpoint[i]);
                        maxpoint = pconA;
                    }
                }
                if (pixeltocell[maxpoint] >= 0)
                    cell[pixeltocell[maxpoint]].D345point[0].add(new Point((maxpoint) % _width, (maxpoint) / _width));//D3の記録
                int maxpoint2 = -1;
                maxdist = -1;
                for (int j = 0; j < vec[i].size(); j++) {
                    int pconA = (Integer) vec[i].get(j) + Ddiff;
                    if (distance(pconA, maxpoint) > maxdist) {
                        maxdist = distance(pconA, maxpoint);
                        maxpoint2 = pconA;
                    }
                }
                if (pixeltocell[maxpoint2] >= 0)
                    cell[pixeltocell[maxpoint2]].D345point[1].add(new Point((maxpoint2) % _width, (maxpoint2) / _width));//D4の記録
                if (maxpoint2 / _width != maxpoint / _width) {
                    int maxpoint3 = -1;
                    double mindist = 1000;
                    for (int j = 0; j < De[i].size(); j++) {//一つ目の交点を求める
                        int pconA = (Integer) De[i].get(j);
                        if (Line2D.ptLineDist(mpoint[i] % _width, mpoint[i] / _width, mpoint[i] % _width + maxpoint2 / _width - maxpoint / _width, mpoint[i] / _width - maxpoint2 % _width + maxpoint % _width, pconA % _width, pconA / _width) < mindist) {
                            mindist = Line2D.ptLineDist(mpoint[i] % _width, mpoint[i] / _width, mpoint[i] % _width + maxpoint2 / _width - maxpoint / _width, mpoint[i] / _width - maxpoint2 % _width + maxpoint % _width, pconA % _width, pconA / _width);
                            maxpoint3 = pconA;
                        }
                    }
                    mindist = 1000;
                    int maxpoint3_2 = -1;
                    for (int j = 0; j < De[i].size(); j++) {//二つ目の交点を求める
                        int pconA = (Integer) De[i].get(j);
                        if (distance(pconA, maxpoint3) > 1 && Line2D.ptLineDist(mpoint[i] % _width, mpoint[i] / _width, mpoint[i] % _width + maxpoint2 / _width - maxpoint / _width, mpoint[i] / _width - maxpoint2 % _width + maxpoint % _width, pconA % _width, pconA / _width) < mindist) {
                            mindist = Line2D.ptLineDist(mpoint[i] % _width, mpoint[i] / _width, mpoint[i] % _width + maxpoint2 / _width - maxpoint / _width, mpoint[i] / _width - maxpoint2 % _width + maxpoint % _width, pconA % _width, pconA / _width);
                            maxpoint3_2 = pconA;
                        }
                    }
                    if (mindist < 1 && distance(maxpoint3, mpoint[i]) < distance(maxpoint3_2, mpoint[i]))
                        maxpoint3 = maxpoint3_2;//より重心から遠い交点を採用
                    if (pixeltocell[maxpoint3] >= 0)
                        cell[pixeltocell[maxpoint3]].D345point[2].add(new Point((maxpoint3) % _width, (maxpoint3) / _width));//D5の記録
                } else {
                    int maxpoint3 = -1;
                    maxdist = -1;
                    for (int j = 0; j < vec[i].size(); j++) {
                        int pconA = (Integer) vec[i].get(j) + Ddiff;
                        if ((pconA - mpoint[i]) % _width == 0 && (pconA - mpoint[i]) / _width > maxdist) {
                            maxdist = (pconA - mpoint[i]) / _width;
                            maxpoint3 = pconA;
                        }
                    }
                    if (pixeltocell[maxpoint3] >= 0)
                        cell[pixeltocell[maxpoint3]].D345point[2].add(new Point((maxpoint3) % _width, (maxpoint3) / _width));//D5の記録
                }
            }
        }
    }

    private double distance(int a, int b) {
        return Math.sqrt((a / _width - b / _width) * (a / _width - b / _width) + (a % _width - b % _width) * (a % _width - b % _width));
    }

    /////////////////////////////////////////////////////////////////////////////////
    //核に関するデータをセット
    /////////////////////////////////////////////////////////////////////////////////
    private void setDState() {
        for (Cell value : cell) {
            value.setDState();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //DAPI画像出力
    /////////////////////////////////////////////////////////////////////////////////
    private void outDImage() {
        int[] pixel = new int[_size];
        for (int i = 0; i < _size; i++) {
            pixel[i] = 0xff000000 | (_nucleus_points[i] << 16) | (_nucleus_points[i] << 8) | _nucleus_points[i];
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image im = toolkit.createImage(new MemoryImageSource(_width, _height, pixel, 0, _width));
        BufferedImage bi = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        Point p = calDiffPoint(Ddiff);
        if (p.x >= 0 && p.y >= 0) while (!g.drawImage(im, p.x, p.y, null)) {
        }
        if (p.x >= 0 && p.y < 0)
            while (!g.drawImage(im, p.x, 0, _width, _height + p.y, 0, -p.y, _width, _height, null)) {
            }
        if (p.x < 0 && p.y >= 0)
            while (!g.drawImage(im, 0, p.y, _width + p.x, _height, -p.x, 0, _width, _height, null)) {
            }
        if (p.x < 0 && p.y < 0)
            while (!g.drawImage(im, p.x, p.y, _width + p.x, _height + p.y, -p.x, -p.y, _width, _height, null)) {
            }
        //while(!g.drawImage(im,0,0,null)){}
        g.setFont(new Font("Courier", Font.PLAIN, 20));
        for (Cell value : cell) {
            value.outDImage(g);
        }
        writeJPEG(bi, outdir + "/" + name + "/" + name + "-dapi" + number + ".jpg");
    }

    /////////////////////////////////////////////////////////////////////////////
    //画像が大きくずれていたらtrue
    //少しずれていたら直してfalse
    /////////////////////////////////////////////////////////////////////////////
    private boolean isDifferentAImage() {
        int[] ci_tmp = _cell_points.clone();
        threshold(ci_tmp);
        int countcarea = 0;
        for (int i = 0; i < _size; i++) {
            if (ci_tmp[i] == 0) countcarea++;
        }
        int[] hg = new int[256];
        for (int i = 0; i < 256; i++) {
            hg[i] = 0;
        }
        for (int i = 0; i < _size; i++) {
            if (ai[i] < 256 && ai[i] >= 0) hg[ai[i]]++;
            else {
                err_kind = "incorrect colorspace of actin image";
                return true;
            } //画像の色がおかしい
        }
        int countdarea = 0;
        for (int i = 255; i >= 0; i--) {
            countdarea += hg[i];
            if (countdarea > countcarea) {
                countdarea -= hg[i];
                for (int j = 0; j < _size; j++) {
                    if (ai[j] >= i + 1) ai[j] = 0;
                    else ai[j] = 255;
                }
                break;
            }
        }
        erosion(ai);
        dilation(ai);
        int k = 7;
        cover(ci_tmp);
        for (Cell element : cell) {
            for (int j = 0; j < element.cover.size(); j++) {
                int p = (Integer) element.cover.get(j);
                ci_tmp[p] = 0;
            }
        }
        int count = 0;
        for (int i = 0; i < _size; i++) {
            if (ai[i] == 0) {
                if (ci_tmp[i] == 255) {//セル領域の外のものがあれば
                    count++;
                }
            }
        }
        if (count > countdarea / 2) {//領域がセル領域から出すぎ
            return true;
        }
        int countdiff = 0;
        for (int i = 0; i < _size; i++) {
            if (ai[i] == 0) {//アクチン領域で
                if (i % _width < k || ci_tmp[i - k] == 255) {//k個左にずらしてセル領域の外のものがあれば
                    countdiff++;
                }
            }
        }
        //System.out.println(count1);
        if (countdiff < count * 0.9) {//動かしたほうがずれが少ない
            return true;
        }
        countdiff = 0;
        for (int i = 0; i < _size; i++) {
            if (ai[i] == 0) {
                if (i % _width >= _width - k || ci_tmp[i + k] == 255) {//k個右にずらしてセル領域の外のものがあれば
                    countdiff++;
                }
            }
        }
        //System.out.println(count1);
        if (countdiff < count * 0.9) {//動かしたほうがずれが少ない
            return true;
        }
        countdiff = 0;
        for (int i = 0; i < _size; i++) {
            if (ai[i] == 0) {
                if (i / _width < k || ci_tmp[i - k * _width] == 255) {//k個上にずらしてセル領域の外のものがあれば
                    countdiff++;
                }
            }
        }
        //System.out.println(count1);
        if (countdiff < count * 0.9) {//動かしたほうがずれが少ない
            return true;
        }
        countdiff = 0;
        for (int i = 0; i < _size; i++) {
            if (ai[i] == 0) {
                if (i / _width >= _height - k || ci_tmp[i + k * _width] == 255) {//k個下にずらしてセル領域の外のものがあれば
                    countdiff++;
                }
            }
        }
        //System.out.println(count1);
        if (countdiff < count * 0.9) {//動かしたほうがずれが少ない
            return true;
        }
        //Small correction if not significantly below
        int[] dep = new int[_size];
        cover(ai);
        Vector[] vec = label(ai, 0, 200, true);
        // (edited by yechang 20 -> 200)
        k = 0;
        int mink = 0;
        Vector[] partedge = new Vector[4];
        boolean[] pixelofactin = new boolean[_size];
        for (int i = 0; i < 4; i++) partedge[i] = new Vector();
        for (Cell item : cell) {
            for (int j = 0; j < item.cover.size(); j++) {
                int p = (Integer) item.cover.elementAt(j);
                if (pixeltocell[p - 1] < 0) partedge[0].add(p);
                if (pixeltocell[p + 1] < 0) partedge[1].add(p);
                if (pixeltocell[p - _width] < 0) partedge[2].add(p);
                if (pixeltocell[p + _width] < 0) partedge[3].add(p);
            }
        }
        for (int i = 0; i < _size; i++) pixelofactin[i] = false;
        for (Vector vector : vec) {
            for (int j = 0; j < vector.size(); j++) {
                pixelofactin[(Integer) vector.elementAt(j)] = true;
            }
        }
        int min = countOutActin(vec, k);
        int value = min;
        for (int i = 0; i < maxdiff; i += 2) {
            for (int j = 1; j < i; j++) {
                value = countOutActin2(partedge[0], partedge[1], pixelofactin, value, k, k - 1);
                k--;
                if (min > value) {
                    min = value;
                    mink = k;
                }
            }
            for (int j = 1; j < i; j++) {
                value = countOutActin2(partedge[2], partedge[3], pixelofactin, value, k, k - _width);
                k -= _width;
                if (min > value) {
                    min = value;
                    mink = k;
                }
            }
            for (int j = 1; j < i + 1; j++) {
                value = countOutActin2(partedge[1], partedge[0], pixelofactin, value, k, k + 1);
                k++;
                if (min > value) {
                    min = value;
                    mink = k;
                }
            }
            for (int j = 1; j < i + 1; j++) {
                value = countOutActin2(partedge[3], partedge[2], pixelofactin, value, k, k + _width);
                k += _width;
                if (min > value) {
                    min = value;
                    mink = k;
                }
            }
        }
        Adiff = mink;
        return false;
    }

    /////////////////////////////////////////////////////////////////////////////////
    //アクチン領域で分類
    /////////////////////////////////////////////////////////////////////////////////
    private void searchActinRegion() {
        //アクチンの消えたセルを探す
        int diffx = Adiff % _width;
        if (diffx >= _width / 2) diffx -= _width;
        else if (diffx <= -_width / 2) diffx += _width;
        for (Cell value : cell) {
            boolean flag_tmp = true;
            value.Aregionsize = new int[2];
            value.Atotalbright = new int[2];
            value.Acenterpoint = new Point[3][5];
            value.Acover = new Vector();
            int regionsize = 0;
            int totalbright = 0;
            int maxbright = 0;
            int x = 0;
            int y = 0;
            int brx = 0;
            int bry = 0;
            long brxx = 0;
            long bryy = 0;
            long brx3 = 0;
            long bry3 = 0;
            long brx4 = 0;
            long bry4 = 0;
            long total2 = 0;
            long total3 = 0;
            long total4 = 0;
            for (int j = 0; j < value.cover.size(); j++) {
                int p = (Integer) value.cover.get(j);
                if (p - Adiff >= 0 && p - Adiff < _size && p / _width == (p - diffx) / _width && ai[p - Adiff] == 0) {
                    value.Acover.add(p);
                    flag_tmp = false;
                    regionsize++;
                    long br = _actin_points[p - Adiff];
                    x += p % _width;
                    y += p / _width;
                    brx += p % _width * br;
                    bry += p / _width * br;
                    brxx += p % _width * br * br;
                    bryy += p / _width * br * br;
                    brx3 += p % _width * br * br * br;
                    bry3 += p / _width * br * br * br;
                    brx4 += p % _width * br * br * br * br;
                    bry4 += p / _width * br * br * br * br;
                    totalbright += br;
                    total2 += br * br;
                    total3 += br * br * br;
                    total4 += br * br * br * br;
                    if (maxbright < _actin_points[p - Adiff]) maxbright = _actin_points[p - Adiff];
                }
            }
            if (regionsize != 0) {
                x /= regionsize;
                y /= regionsize;
                brx /= totalbright;
                bry /= totalbright;
                brxx /= total2;
                bryy /= total2;
                brx3 /= total3;
                bry3 /= total3;
                brx4 /= total4;
                bry4 /= total4;
            } else {
                x = -1;
                y = -1;
                brx = -1;
                bry = -1;
                brxx = -1;
                bryy = -1;
                brx3 = -1;
                bry3 = -1;
                brx4 = -1;
                bry4 = -1;
            }
            if ((flag_tmp || maxbright < 10) && value.getGroup() > 0) value.setAgroup("N");
            else {
                value.Aregionsize[0] = regionsize;
                value.Atotalbright[0] = totalbright;
                value.Acenterpoint[2][0] = new Point(x, y);
                value.Acenterpoint[2][1] = new Point(brx, bry);
                value.Acenterpoint[2][2] = new Point((int) brxx, (int) bryy);
                value.Acenterpoint[2][3] = new Point((int) brx3, (int) bry3);
                value.Acenterpoint[2][4] = new Point((int) brx4, (int) bry4);
                if (value.getGroup() == 1) {
                    if (regionsize > value.cover.size() / 4) value.setAgroup("A");
                    else value.setAgroup("B");
                    value.Acenterpoint[0][0] = new Point(x, y);
                    value.Acenterpoint[0][1] = new Point(brx, bry);
                    value.Acenterpoint[0][2] = new Point((int) brxx, (int) bryy);
                    value.Acenterpoint[0][3] = new Point((int) brx3, (int) bry3);
                    value.Acenterpoint[0][4] = new Point((int) brx4, (int) bry4);
                } else if (value.getGroup() > 1) {
                    int mregionsize = 0;
                    int mtotalbright = 0;
                    int bregionsize = 0;
                    int btotalbright = 0;
                    int mx = 0;
                    int my = 0;
                    int mbrx = 0;
                    int mbry = 0;
                    long mbrxx = 0;
                    long mbryy = 0;
                    long mbrx3 = 0;
                    long mbry3 = 0;
                    long mbrx4 = 0;
                    long mbry4 = 0;
                    long mtotal2 = 0;
                    long mtotal3 = 0;
                    long mtotal4 = 0;
                    int bx = 0;
                    int by = 0;
                    int bbrx = 0;
                    int bbry = 0;
                    long bbrxx = 0;
                    long bbryy = 0;
                    long bbrx3 = 0;
                    long bbry3 = 0;
                    long bbrx4 = 0;
                    long bbry4 = 0;
                    long btotal2 = 0;
                    long btotal3 = 0;
                    long btotal4 = 0;
                    for (int j = 0; j < value.cover.size(); j++) {
                        int p = (Integer) value.cover.get(j);
                        if (p - Adiff >= 0 && p - Adiff < _size && p / _width == (p - diffx) / _width && ai[p - Adiff] == 0) {
                            long br = _actin_points[p - Adiff];
                            if (value.inmother(p)) {
                                mregionsize++;
                                mtotalbright += _actin_points[p - Adiff];
                                mx += p % _width;
                                my += p / _width;
                                mbrx += p % _width * br;
                                mbry += p / _width * br;
                                mbrxx += p % _width * br * br;
                                mbryy += p / _width * br * br;
                                mbrx3 += p % _width * br * br * br;
                                mbry3 += p / _width * br * br * br;
                                mbrx4 += p % _width * br * br * br * br;
                                mbry4 += p / _width * br * br * br * br;
                                mtotal2 += br * br;
                                mtotal3 += br * br * br;
                                mtotal4 += br * br * br * br;
                            } else {
                                bregionsize++;
                                btotalbright += br;
                                bx += p % _width;
                                by += p / _width;
                                bbrx += p % _width * br;
                                bbry += p / _width * br;
                                bbrxx += p % _width * br * br;
                                bbryy += p / _width * br * br;
                                bbrx3 += p % _width * br * br * br;
                                bbry3 += p / _width * br * br * br;
                                bbrx4 += p % _width * br * br * br * br;
                                bbry4 += p / _width * br * br * br * br;
                                btotal2 += br * br;
                                btotal3 += br * br * br;
                                btotal4 += br * br * br * br;
                            }
                        }
                    }
                    if (mregionsize != 0) {
                        mx /= mregionsize;
                        my /= mregionsize;
                        mbrx /= mtotalbright;
                        mbry /= mtotalbright;
                        mbrxx /= mtotal2;
                        mbryy /= mtotal2;
                        mbrx3 /= mtotal3;
                        mbry3 /= mtotal3;
                        mbrx4 /= mtotal4;
                        mbry4 /= mtotal4;
                    } else {
                        mx = -1;
                        my = -1;
                        mbrx = -1;
                        mbry = -1;
                        mbrxx = -1;
                        mbryy = -1;
                        mbrx3 = -1;
                        mbry3 = -1;
                        mbrx4 = -1;
                        mbry4 = -1;
                    }
                    if (bregionsize != 0) {
                        bx /= bregionsize;
                        by /= bregionsize;
                        bbrx /= btotalbright;
                        bbry /= btotalbright;
                        bbrxx /= btotal2;
                        bbryy /= btotal2;
                        bbrx3 /= btotal3;
                        bbry3 /= btotal3;
                        bbrx4 /= btotal4;
                        bbry4 /= btotal4;
                    } else {
                        bx = -1;
                        by = -1;
                        bbrx = -1;
                        bbry = -1;
                        bbrxx = -1;
                        bbryy = -1;
                        bbrx3 = -1;
                        bbry3 = -1;
                        bbrx4 = -1;
                        bbry4 = -1;
                    }
                    value.Acenterpoint[0][0] = new Point(mx, my);
                    value.Acenterpoint[0][1] = new Point(mbrx, mbry);
                    value.Acenterpoint[0][2] = new Point((int) mbrxx, (int) mbryy);
                    value.Acenterpoint[0][3] = new Point((int) mbrx3, (int) mbry3);
                    value.Acenterpoint[0][4] = new Point((int) mbrx4, (int) mbry4);
                    value.Acenterpoint[1][0] = new Point(bx, by);
                    value.Acenterpoint[1][1] = new Point(bbrx, bbry);
                    value.Acenterpoint[1][2] = new Point((int) bbrxx, (int) bbryy);
                    value.Acenterpoint[1][3] = new Point((int) bbrx3, (int) bbry3);
                    value.Acenterpoint[1][4] = new Point((int) bbrx4, (int) bbry4);
                    value.Aregionsize[0] = mregionsize;
                    value.Aregionsize[1] = bregionsize;
                    value.Atotalbright[0] = mtotalbright;
                    value.Atotalbright[1] = btotalbright;
                }
            }
            if (value.getGroup() >= 2) {//ネックライン上に乗っているアクチン領域のネックライン全体に対する割合を求める
                if (Math.abs(value.neck[0] % _width - value.neck[1] % _width) > Math.abs(value.neck[0] / _width - value.neck[1] / _width)) {
                    int min = 0;
                    if (value.neck[0] % _width > value.neck[1] % _width) min = 1;
                    int minx = value.neck[min] % _width;
                    int maxx = value.neck[(1 + min) % 2] % _width;
                    int miny = value.neck[min] / _width;
                    int maxy = value.neck[(1 + min) % 2] / _width;
                    int count = 0;
                    for (int j = 0; j <= maxx - minx; j++) {
                        int p = minx + j + (miny + ((maxy - miny) * j) / (maxx - minx)) * _width;
                        if (ai[p - Adiff] == 0) count++;
                    }
                    value.actinonneckline = (double) count / (double) (maxx - minx + 1);
                } else {
                    int min = 0;
                    if (value.neck[0] / _width > value.neck[1] / _width) min = 1;
                    int minx = value.neck[min] % _width;
                    int maxx = value.neck[(1 + min) % 2] % _width;
                    int miny = value.neck[min] / _width;
                    int maxy = value.neck[(1 + min) % 2] / _width;
                    int count = 0;
                    for (int j = 0; j <= maxy - miny; j++) {
                        int p = (miny + j) * _width + minx + ((maxx - minx) * j) / (maxy - miny);
                        if (ai[p - Adiff] == 0) count++;
                    }
                    value.actinonneckline = (double) count / (double) (maxy - miny + 1);
                }

            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //アクチンパッチを探す
    /////////////////////////////////////////////////////////////////////////////////
    private void searchActinPatch() {
        Vector<ActinLabel> same;
        int[] lab = new int[_size];

        same = new Vector<>();
        ai = _actin_points.clone();
        for (int i = 0; i < _size; i++) {//全てラベルがついてない状態に
            lab[i] = -1;
            if (ai[i] < 60) ai[i] = 0;
            else {
                ai[i] = (ai[i] - 60) / 8 + 1;
            }
        }
        for (int i = 0; i < _size; i++) {//上で振られた値が極大になっている領域を求める
            //if(i%w-1 >= 0 && i/w-1 >= 0) {//画像の左端or上端でなければ
            if (pixeltocell[i] >= 0) {//細胞内部の点について
                if (bright(ai, i - 1, i) && bright(ai, i - _width, i)) {//左、上の両方より暗いとき
                    //何もしない
                } else if (bright(ai, i, i - 1) && bright(ai, i, i - _width)) {//左、上の両方より明るいとき
                    removeActinLabel(same, lab, i - 1);//左のラベルを消す
                    removeActinLabel(same, lab, i - _width);//上のラベルを消す
                    lab[i] = same.size();//新しいラベルをつけて
                    same.add(new ActinLabel(same.size(), true));//自分を指すラベルを登録
                } else if ((bright(ai, i, i - 1) || equal(ai, i, i - 1)) && bright(ai, i - _width, i)) {//左と同じか明るいが上より暗い
                    removeActinLabel(same, lab, i - 1);//左のラベルを消す
                } else if (bright(ai, i - 1, i) && (bright(ai, i, i - _width) || equal(ai, i, i - _width))) {//左より暗いが上と同じか明るい
                    removeActinLabel(same, lab, i - _width);//上のラベルを消す
                } else if (equal(ai, i, i - 1) && equal(ai, i, i - _width)) {//左とも上とも同じ明るさ
                    int a1 = smallestActinLabel(same, lab, i - 1);
                    int a2 = smallestActinLabel(same, lab, i - _width);
                    if (a1 == -1) {//左のラベルがなかったら
                        removeActinLabel(same, lab, i - _width);//上のラベルを消す
                    } else if (a2 == -1) {//上のラベルがなかったら
                        removeActinLabel(same, lab, i - 1);//左のラベルを消す
                    } else {//両方のラベルがあれば
                        if (a1 <= a2) {//小さい方にポインタを付け替える
                            (same.get(a2)).setPointer(a1);
                            lab[i] = a1;
                        } else {
                            (same.get(a1)).setPointer(a2);
                            lab[i] = a2;
                        }
                    }
                } else if (equal(ai, i, i - 1) && bright(ai, i, i - _width)) {//左とおなじで上が暗い
                    removeActinLabel(same, lab, i - _width);//上のラベルを消して
                    lab[i] = smallestActinLabel(same, lab, i - 1);//左のsmallestラベルにする
                } else if (bright(ai, i, i - 1) && equal(ai, i, i - _width)) {//上とおなじで左が暗い
                    removeActinLabel(same, lab, i - 1);//左のラベルを消して
                    lab[i] = smallestActinLabel(same, lab, i - _width);//上のsmallestラベルにする
                }
            }
        }
        for (int i = 0; i < _size; i++) {
            lab[i] = smallestActinLabel(same, lab, i);//一番小さいラベルをつける
            if (lab[i] < 0) lab[i] = 255;
            else lab[i] = 0;
        }
        int diffx = Adiff % _width;
        if (diffx >= _width / 2) diffx -= _width;
        else if (diffx <= -_width / 2) diffx += _width;
        Vector[] vec = label(lab, 0, 0, true);
        Vector[] brhist = new Vector[256];
        int[] patchp = new int[vec.length];
        for (int i = 0; i < 256; i++) brhist[i] = new Vector();
        for (int i = 0; i < vec.length; i++) {
            int x = 0;
            int y = 0;
            for (int j = 0; j < vec[i].size(); j++) {
                int p = (Integer) vec[i].get(j);
                x += p % _width;
                y += p / _width;
            }
            x /= vec[i].size();
            y /= vec[i].size();
            int p1 = x + y * _width;
            if (p1 + Adiff > 0 && p1 + Adiff < _size && p1 / _width == (p1 + diffx) / _width && pixeltocell[p1 + Adiff] >= 0) {
                brhist[_actin_points[p1]].add(i);
                patchp[i] = p1;
            }
        }

        //int[] alab = new int[size];
        //for(int i=0;i<size;i++) alab[i]=-1;
        //actindiv = new int[size];
        //for(int i=0;i<size;i++) actindiv[i]=-1;

        int[] patchnumofcell = new int[cell.length];
        int[] patchbrightofcell = new int[cell.length];
        for (int p : patchp) {
            if (p == 0) continue;
            patchnumofcell[pixeltocell[p + Adiff]]++;
            patchbrightofcell[pixeltocell[p + Adiff]] += _actin_points[p];
        }
        for (int i = 0; i < cell.length; i++) {
            if (patchnumofcell[i] > 0) patchbrightofcell[i] /= patchnumofcell[i];
        }

        int[] actinbrightofcell = new int[cell.length];
        for (int i = 0; i < cell.length; i++) {
            actinbrightofcell[i] = 255;
            int count = 0;
            for (int j = 0; j < cell[i].cover.size(); j++) {
                int p = (Integer) cell[i].cover.get(j);
                if (p - Adiff >= 0 && p - Adiff < _size && p / _width == (p - diffx) / _width) {
                    actinbrightofcell[i] += _actin_points[p - Adiff];
                    count++;
                }
            }
            actinbrightofcell[i] /= count;
        }
        boolean[] ingroup = new boolean[_size];//あるピクセルがアクチンパッチ領域に含まれたかどうか
        for (int i = 255; i >= 30; i--) {
            for (int j = 0; j < brhist[i].size(); j++) {
                int ii = (Integer) brhist[i].get(j);
                int p = patchp[ii];
                int cellnum = pixeltocell[p + Adiff];
                int bright = _actin_points[p];
                Vector<Integer> group = new Vector<>();
                for (int k = 0; k < vec[ii].size(); k++) {
                    p = (Integer) vec[ii].get(k);
                    ingroup[p] = true;
                    group.add(p);
                }
                for (int k = 0; k < group.size(); k++) {
                    p = group.get(k);
                    if (p + Adiff >= 0 && p + Adiff < _size && p / _width == (p + diffx) / _width) {
                        int maxneighbor = 0;
                        if (p - _width >= 0) maxneighbor = _actin_points[p - _width];
                        if (p % _width != 0 && maxneighbor < _actin_points[p - 1]) maxneighbor = _actin_points[p - 1];
                        if (p % _width != _width - 1 && maxneighbor < _actin_points[p + 1])
                            maxneighbor = _actin_points[p + 1];
                        if (p + _width < _size && maxneighbor < _actin_points[p + _width])
                            maxneighbor = _actin_points[p + _width];

                        if (p - _width >= 0 && p + Adiff - _width >= 0 && pixeltocell[p + Adiff - _width] >= 0 && !ingroup[p - _width] && _actin_points[p - _width] > Math.max(bright / 2, actinbrightofcell[cellnum]) && _actin_points[p - _width] <= _actin_points[p] && (maxneighbor - _actin_points[p] < _actin_points[p] - _actin_points[p - _width] || maxneighbor < _actin_points[p] + 5 || _actin_points[p] > 150)) {
                            ingroup[p - _width] = true;
                            group.add(p - _width);
                        }
                        if (p % _width != 0 && (p + Adiff) % _width != 0 && pixeltocell[p + Adiff - 1] >= 0 && !ingroup[p - 1] && _actin_points[p - 1] > Math.max(bright / 2, actinbrightofcell[cellnum]) && _actin_points[p - 1] <= _actin_points[p] && (maxneighbor - _actin_points[p] < _actin_points[p] - _actin_points[p - 1] || maxneighbor < _actin_points[p] + 5 || _actin_points[p] > 150)) {
                            ingroup[p - 1] = true;
                            group.add(p - 1);
                        }
                        if (p % _width != _width - 1 && (p + Adiff) % _width != _width - 1 && pixeltocell[p + Adiff + 1] >= 0 && !ingroup[p + 1] && _actin_points[p + 1] > Math.max(bright / 2, actinbrightofcell[cellnum]) && _actin_points[p + 1] <= _actin_points[p] && (maxneighbor - _actin_points[p] < _actin_points[p] - _actin_points[p + 1] || maxneighbor < _actin_points[p] + 5 || _actin_points[p] > 150)) {
                            ingroup[p + 1] = true;
                            group.add(p + 1);
                        }
                        if (p + _width < _size && p + Adiff + _width < _size && pixeltocell[p + Adiff + _width] >= 0 && !ingroup[p + _width] && _actin_points[p + _width] > Math.max(bright / 2, actinbrightofcell[cellnum]) && _actin_points[p + _width] <= _actin_points[p] && (maxneighbor - _actin_points[p] < _actin_points[p] - _actin_points[p + _width] || maxneighbor < _actin_points[p] + 5 || _actin_points[p] > 150)) {
                            ingroup[p + _width] = true;
                            group.add(p + _width);
                        }
                    }
                }
				/*for(int k=0;k<group.size();k++){//alabには各ピクセルがアクチンパッチ領域に含まれているかを記録、細胞内の別のアクチンパッチ領域には別の番号が振られる
					p = ((Integer)group.get(k)).intValue();
					alab[p] = cell[cellnum].actinpatchpoint.size();
				}*/
                if (_actin_points[patchp[ii]] < patchbrightofcell[cellnum] && group.size() > 30) {//暗くて領域が広がりすぎたアクチンパッチは除く
					/*for(int k=0;k<group.size();k++){
						p = ((Integer)group.get(k)).intValue();
						alab[p] = -1;
					}*/
                } else {
                    cell[cellnum].actinpatchpoint.add(patchp[ii] + Adiff);
                    cell[cellnum].actinpatchbright.add(_actin_points[patchp[ii]]);
                    cell[cellnum].actinpatchsize.add(group.size());
                    cell[cellnum].totalpatchsize += group.size();
                }
            }
        }//ここまででアクチンパッチを求める

        /*for(int i=0;i<cell.length;i++){
			for(int j=0;j<cell[i].cover.size();j++) {
				int p = ((Integer)cell[i].cover.get(j)).intValue();
				if(alab[p] > -1 && (alab[p-w] != alab[p] || alab[p-1] != alab[p] || alab[p+1] != alab[p] || alab[p+w] != alab[p])){
					actindiv[p] = alab[p];
				}
			}
        }*/

        for (Cell item : cell) {//アクチンパッチの重心（重み輝度0〜4乗）を求める
            item.Apatchcenterpoint = new Point[3][5];
            int x = 0;
            int y = 0;
            int brx = 0;
            int bry = 0;
            long brxx = 0;
            long bryy = 0;
            long brx3 = 0;
            long bry3 = 0;
            long brx4 = 0;
            long bry4 = 0;
            int total = 0;
            long total2 = 0;
            long total3 = 0;
            long total4 = 0;
            int mpatchsize = 0;
            int bpatchsize = 0;
            int mx = 0;
            int my = 0;
            int mbrx = 0;
            int mbry = 0;
            long mbrxx = 0;
            long mbryy = 0;
            long mbrx3 = 0;
            long mbry3 = 0;
            long mbrx4 = 0;
            long mbry4 = 0;
            int mtotal = 0;
            long mtotal2 = 0;
            long mtotal3 = 0;
            long mtotal4 = 0;
            int bx = 0;
            int by = 0;
            int bbrx = 0;
            int bbry = 0;
            long bbrxx = 0;
            long bbryy = 0;
            long bbrx3 = 0;
            long bbry3 = 0;
            long bbrx4 = 0;
            long bbry4 = 0;
            int btotal = 0;
            long btotal2 = 0;
            long btotal3 = 0;
            long btotal4 = 0;
            for (int j = 0; j < item.actinpatchpoint.size(); j++) {
                int p = (Integer) item.actinpatchpoint.get(j);
                long br = _actin_points[p - Adiff];
                x += p % _width;
                y += p / _width;
                brx += p % _width * br;
                bry += p / _width * br;
                brxx += p % _width * br * br;
                bryy += p / _width * br * br;
                brx3 += p % _width * br * br * br;
                bry3 += p / _width * br * br * br;
                brx4 += p % _width * br * br * br * br;
                bry4 += p / _width * br * br * br * br;
                total += br;
                total2 += br * br;
                total3 += br * br * br;
                total4 += br * br * br * br;
                if (item.getGroup() > 1) {
                    if (item.inmother(p)) {
                        mpatchsize++;
                        mtotal += br;
                        mx += p % _width;
                        my += p / _width;
                        mbrx += p % _width * br;
                        mbry += p / _width * br;
                        mbrxx += p % _width * br * br;
                        mbryy += p / _width * br * br;
                        mbrx3 += p % _width * br * br * br;
                        mbry3 += p / _width * br * br * br;
                        mbrx4 += p % _width * br * br * br * br;
                        mbry4 += p / _width * br * br * br * br;
                        mtotal2 += br * br;
                        mtotal3 += br * br * br;
                        mtotal4 += br * br * br * br;
                    } else {
                        bpatchsize++;
                        btotal += br;
                        bx += p % _width;
                        by += p / _width;
                        bbrx += p % _width * br;
                        bbry += p / _width * br;
                        bbrxx += p % _width * br * br;
                        bbryy += p / _width * br * br;
                        bbrx3 += p % _width * br * br * br;
                        bbry3 += p / _width * br * br * br;
                        bbrx4 += p % _width * br * br * br * br;
                        bbry4 += p / _width * br * br * br * br;
                        btotal2 += br * br;
                        btotal3 += br * br * br;
                        btotal4 += br * br * br * br;
                    }
                }
            }
            if (item.actinpatchpoint.size() != 0) {
                x /= item.actinpatchpoint.size();
                y /= item.actinpatchpoint.size();
                brx /= total;
                bry /= total;
                brxx /= total2;
                bryy /= total2;
                brx3 /= total3;
                bry3 /= total3;
                brx4 /= total4;
                bry4 /= total4;
            } else {
                x = -1;
                y = -1;
                brx = -1;
                bry = -1;
                brxx = -1;
                bryy = -1;
                brx3 = -1;
                bry3 = -1;
                brx4 = -1;
                bry4 = -1;
            }
            item.Apatchcenterpoint[2][0] = new Point(x, y);
            item.Apatchcenterpoint[2][1] = new Point(brx, bry);
            item.Apatchcenterpoint[2][2] = new Point((int) brxx, (int) bryy);
            item.Apatchcenterpoint[2][3] = new Point((int) brx3, (int) bry3);
            item.Apatchcenterpoint[2][4] = new Point((int) brx4, (int) bry4);
            item.Apatchcenterpoint[0][0] = new Point(x, y);
            item.Apatchcenterpoint[0][1] = new Point(brx, bry);
            item.Apatchcenterpoint[0][2] = new Point((int) brxx, (int) bryy);
            item.Apatchcenterpoint[0][3] = new Point((int) brx3, (int) bry3);
            item.Apatchcenterpoint[0][4] = new Point((int) brx4, (int) bry4);
            if (item.getGroup() > 1) {
                if (mpatchsize != 0) {
                    mx /= mpatchsize;
                    my /= mpatchsize;
                    mbrx /= mtotal;
                    mbry /= mtotal;
                    mbrxx /= mtotal2;
                    mbryy /= mtotal2;
                    mbrx3 /= mtotal3;
                    mbry3 /= mtotal3;
                    mbrx4 /= mtotal4;
                    mbry4 /= mtotal4;
                } else {
                    mx = -1;
                    my = -1;
                    mbrx = -1;
                    mbry = -1;
                    mbrxx = -1;
                    mbryy = -1;
                    mbrx3 = -1;
                    mbry3 = -1;
                    mbrx4 = -1;
                    mbry4 = -1;
                }
                if (bpatchsize != 0) {
                    bx /= bpatchsize;
                    by /= bpatchsize;
                    bbrx /= btotal;
                    bbry /= btotal;
                    bbrxx /= btotal2;
                    bbryy /= btotal2;
                    bbrx3 /= btotal3;
                    bbry3 /= btotal3;
                    bbrx4 /= btotal4;
                    bbry4 /= btotal4;
                } else {
                    bx = -1;
                    by = -1;
                    bbrx = -1;
                    bbry = -1;
                    bbrxx = -1;
                    bbryy = -1;
                    bbrx3 = -1;
                    bbry3 = -1;
                    bbrx4 = -1;
                    bbry4 = -1;
                }
                item.Apatchcenterpoint[0][0] = new Point(mx, my);
                item.Apatchcenterpoint[0][1] = new Point(mbrx, mbry);
                item.Apatchcenterpoint[0][2] = new Point((int) mbrxx, (int) mbryy);
                item.Apatchcenterpoint[0][3] = new Point((int) mbrx3, (int) mbry3);
                item.Apatchcenterpoint[0][4] = new Point((int) mbrx4, (int) mbry4);
                item.Apatchcenterpoint[1][0] = new Point(bx, by);
                item.Apatchcenterpoint[1][1] = new Point(bbrx, bbry);
                item.Apatchcenterpoint[1][2] = new Point((int) bbrxx, (int) bbryy);
                item.Apatchcenterpoint[1][3] = new Point((int) bbrx3, (int) bbry3);
                item.Apatchcenterpoint[1][4] = new Point((int) bbrx4, (int) bbry4);
            }
        }

        for (Cell value : cell) {//アクチンパッチをノードとしたときの最小TSPパスの近似解を求める
            value.calcActinpathlength();
        }
    }


    private boolean bright(int[] image, int p1, int p2) {
        return image[p1] > image[p2];
    }

    private boolean equal(int[] image, int p1, int p2) {
        return image[p1] >= image[p2] && image[p1] <= image[p2];
    }

    ///////////////////////////////////////////////////////////////////////////
    //画像上でiの位置につけられたラベルのﾙｰﾄを返す
    //木をつぶす
    ///////////////////////////////////////////////////////////////////////////
    private int smallestActinLabel(Vector<ActinLabel> same, int[] lab, int i) {
        if (lab[i] >= 0) {
            int now = lab[i];
            Vector<Integer> temp = new Vector<>();
            while (true) {
                ActinLabel al = same.get(now);
                if (al.isEnabled()) {//ラベルが消えてなければ
                    if (al.getPointer() == now) {//ﾙｰﾄまでたどり着いたら
                        for (Object o : temp) {//それまでのラベルのポインタをﾙｰﾄにして
                            (same.get((Integer) o)).setPointer(now);
                        }
                        return now;//ﾙｰﾄの番号を返す
                    } else {//ﾙｰﾄじゃなければ
                        temp.add(now);//このラベルを通過したことを記録し
                        now = (same.get(now)).getPointer();//先をたどる
                    }
                } else {//ラベルが消えてたら
                    for (int j = 0; j < temp.size(); j++) {//それまでのラベルも消して（全部消えてるかも）
                        (same.get(temp.elementAt(j))).setState(false);
                    }
                    return -1;//ラベルが消えてたことを知らせる。
                }
            }
        } else {//ラベルがついていなければ
            return -1;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //画像上でiの位置につけられたラベルを消す
    //ラベルがつながれているときはその先も消す
    ///////////////////////////////////////////////////////////////////////////
    private void removeActinLabel(Vector<ActinLabel> same, int[] lab, int i) {
        if (lab[i] >= 0) {
            int now = lab[i];
            while (true) {
                ActinLabel al = same.get(now);
                if (al.getPointer() == now || !al.isEnabled()) {//ﾙｰﾄorすでに消されているなら先をたどらない
                    al.setState(false);
                    break;
                } else {
                    now = al.getPointer();
                    al.setState(false);
                }
            }
        }  //もともとラベルが無い

    }


    /////////////////////////////////////////////////////////////////////////////////
    //アクチンに関するデータをセット
    /////////////////////////////////////////////////////////////////////////////////
    private void setAState() {
        for (Cell value : cell) {
            value.setAState(Adiff, _actin_points);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //actin画像出力
    /////////////////////////////////////////////////////////////////////////////////
    private void outAImage() {
        int[] pixel = new int[_size];
        for (int i = 0; i < _size; i++) {
            pixel[i] = 0xff000000 | (_actin_points[i] << 16) | (_actin_points[i] << 8) | _actin_points[i];
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image im = toolkit.createImage(new MemoryImageSource(_width, _height, pixel, 0, _width));
        BufferedImage bi = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        Point p = calDiffPoint(Adiff);
        if (p.x >= 0 && p.y >= 0) while (!g.drawImage(im, p.x, p.y, null)) {
        }
        if (p.x >= 0 && p.y < 0)
            while (!g.drawImage(im, p.x, 0, _width, _height + p.y, 0, -p.y, _width, _height, null)) {
            }
        if (p.x < 0 && p.y >= 0)
            while (!g.drawImage(im, 0, p.y, _width + p.x, _height, -p.x, 0, _width, _height, null)) {
            }
        if (p.x < 0 && p.y < 0)
            while (!g.drawImage(im, p.x, p.y, _width + p.x, _height + p.y, -p.x, -p.y, _width, _height, null)) {
            }
        g.setFont(new Font("Courier", Font.PLAIN, 20));
        g.setColor(new Color(0xff8888ff));
        for (Cell value : cell) {
            value.outAImage(g);
        }

        writeJPEG(bi, outdir + "/" + name + "/" + name + "-actin" + number + ".jpg");
    }

    /////////////////////////////////////////////////////////////////////////////////
    //Do output
    /////////////////////////////////////////////////////////////////////////////////
    private void writeXLSBaseC(PrintWriter pw) {
        for (Cell value : cell) {
            value.writeXLSBaseC(pw, number);
        }
    }

    private void writeXLSExpandC(PrintWriter pw) {
        for (Cell value : cell) {
            value.writeXLSExpandC(pw, number);
        }
    }

    private void writeXLSBaseD(PrintWriter pw) {
        for (Cell value : cell) {
            value.writeXLSBaseD(pw, number);
        }
    }

    private void writeXLSExpandD(PrintWriter pw) {
        for (Cell value : cell) {
            value.writeXLSExpandD(pw, number);
        }
    }

    private void writeXLSBaseA(PrintWriter pw) {
        for (Cell value : cell) {
            value.writeXLSBaseA(pw, number);
        }
    }

    private void writeXLSExpandA(PrintWriter pw) {
        for (Cell value : cell) {
            value.writeXLSExpandA(pw, number);
        }
    }

    private void writeXLSPatchA(PrintWriter pw) {
        for (Cell value : cell) {
            value.writeXLSPatchA(pw, number);
        }
    }

    private void writeXLSVers(PrintWriter pw) {
        for (Cell value : cell) {
            value.writeXLSVers(pw, number, calA, calD);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //画像データの出力を行う
    /////////////////////////////////////////////////////////////////////////////////
    private void writeImageDataXML(PrintWriter pwxml) {
        pwxml.println(" <photo id=\"" + number + "\">");
        for (Cell value : cell) {
            value.writeImageDataXML(pwxml, number);
        }
        pwxml.println(" </photo>");
    }


    ///////////////////////////////////////////////////////////////////////////
    //画像ファイルを書き出す
    ///////////////////////////////////////////////////////////////////////////
    private void writeJPEG(BufferedImage img, String fname) {
        try {
            //File file = new File(fname);
            //BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            // 舊的使用 jpeg classes進行處理的方法
            //JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            //JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
            //param.setQuality(1.0f, false);
            //encoder.setJPEGEncodeParam(param);
            //encoder.encode(img);
            //out.flush();
            //out.close();
            ImageIO.write(img, "jpeg", new File(fname));
        } catch (Exception e) {
            System.err.println("CellImage.writeJPEG(BufferedImage,String):" + e);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //imageの中身を出力
    //////////////////////////////////////////////////////////////////////////
    public void writeimage(int[] image, String filename) {
        BufferedImage bi = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        for (int i = 0; i < _size; i++) {
            g.setColor(new Color(image[i], image[i], image[i]));
            g.drawLine(i % _width, i / _width, i % _width, i / _width);
        }
        writeJPEG(bi, outdir + "/" + this.name + "/" + this.name + "-" + filename + "" + number + ".jpg");
    }

    //////////////////////////////////////////////////////////////////////////
    //Coordinate display of deviation
    //////////////////////////////////////////////////////////////////////////
    private Point calDiffPoint(int diff) {
        Point p = new Point();
        if (diff >= 0) {
            if (diff % _width < _width / 2) {
                p.x = diff % _width;
                p.y = diff / _width;
            } else {
                p.x = diff % _width - _width;
                p.y = diff / _width + 1;
            }
        } else {
            if ((-diff) % _width < _width / 2) {
                p.x = -((-diff) % _width);
                p.y = -((-diff) / _width);
            } else {
                p.x = _width - ((-diff) % _width);
                p.y = -((-diff) / _width) - 1;
            }
        }
        return p;
    }

    //////////////////////////////////////////////////////////////////////////
    //objectをsave
    //////////////////////////////////////////////////////////////////////////
    private void save(int savenum) {
        try {
            File sa = new File("objects");
            if (!sa.exists()) sa.mkdir();
            sa = new File("objects/" + name);
            if (!sa.exists()) sa.mkdir();
            ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream((new FileOutputStream("objects/" + name + "/" + name + "-" + number + "-" + savenum + ".dat"))));
            oos.writeObject(number);
            oos.writeObject(Ddiff);
            oos.writeObject(Adiff);
            oos.writeObject(_cell_points);
            oos.writeObject(_nucleus_points);
            oos.writeObject(_actin_points);
            oos.writeObject(ci);
            oos.writeObject(di);
            oos.writeObject(ai);
            oos.writeObject(pixeltocell);
            oos.writeObject(pixeltocell2);
            oos.writeObject(cell);
            oos.writeObject(err);
            oos.writeObject(calD);
            oos.writeObject(calA);
            oos.writeObject(flag_tmp);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            System.err.println("CellImage.save(int):" + e);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //objectをload
    //////////////////////////////////////////////////////////////////////////
    public void load(int loadnum) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream((new FileInputStream("objects/" + name + "/" + name + "-" + number + "-" + loadnum + ".dat"))));
            number = (Integer) ois.readObject();
            Ddiff = (Integer) ois.readObject();
            Adiff = (Integer) ois.readObject();
            _cell_points = (int[]) ois.readObject();
            _nucleus_points = (int[]) ois.readObject();
            _actin_points = (int[]) ois.readObject();
            ci = (int[]) ois.readObject();
            di = (int[]) ois.readObject();
            ai = (int[]) ois.readObject();
            pixeltocell = (int[]) ois.readObject();
            pixeltocell2 = (int[]) ois.readObject();
            cell = (Cell[]) ois.readObject();
            err = (Boolean) ois.readObject();
            calD = (Boolean) ois.readObject();
            calA = (Boolean) ois.readObject();
            flag_tmp = (Boolean) ois.readObject();
        } catch (Exception e) {
            System.err.println("CellImage.load(int):" + e);
        }
    }
}

class ActinLabel {
    private int pointer;
    private boolean state;

    ActinLabel(int p, boolean b) {
        pointer = p;
        state = b;
    }

    void setState(boolean b) {
        state = b;
    }

    int getPointer() {
        return pointer;
    }

    void setPointer(int p) {
        pointer = p;
    }

    boolean isEnabled() {
        return state;
    }
}


//--------------------------------------
//$Log:
