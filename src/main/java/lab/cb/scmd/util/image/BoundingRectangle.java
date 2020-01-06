//--------------------------------------
// SCMDProject
// 
// BoundingRectangle.java 
// Since: 2004/07/13
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.image;

import lab.cb.scmd.exception.InvalidParameterException;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author leo
 */
public class BoundingRectangle {
    private int x1 = 0;
    private int x2 = 0;
    private int y1 = 0;
    private int y2 = 0;

    /**
     *
     */
    public BoundingRectangle() {

    }

    public BoundingRectangle(int x1, int x2, int y1, int y2) throws InvalidParameterException {
        super();

        if (x1 > x2 || y1 > y2)
            throw new InvalidParameterException("invalid range: " + "[" + x1 + "-" + x2 + "] - [" + y1 + "-" + y2
                    + "]");

        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public String getGeometry() {
        int xRange = x2 - x1;
        int yRange = y2 - y1;
        return xRange + "x" + yRange + "+" + x1 + "+" + y1;
    }

    String getGeometry(int borderSize) throws InvalidParameterException {
        if (borderSize < 0) throw new InvalidParameterException("invalid border size: " + borderSize);
        int xRange = x2 - x1 + borderSize * 2;
        int yRange = y2 - y1 + borderSize * 2;
        int xBegin = x1 < borderSize ? 0 : x1 - borderSize;
        int yBegin = y1 < borderSize ? 0 : y1 - borderSize;
        return xRange + "x" + yRange + "+" + xBegin + "+" + yBegin;
    }

    public int[] getBox(int borderSize) throws InvalidParameterException {
        if (borderSize < 0) throw new InvalidParameterException("invalid border size: " + borderSize);
        int xRange = x2 - x1 + borderSize * 2;
        int yRange = y2 - y1 + borderSize * 2;
        int xBegin = x1 < borderSize ? 0 : x1 - borderSize;
        int yBegin = y1 < borderSize ? 0 : y1 - borderSize;
        return new int[]{xBegin, yBegin, xRange, yRange};
    }

    public String getAreaCoordinates(int magnification) {
        int a_x1 = x1 * magnification / 100;
        int a_x2 = x2 * magnification / 100;
        int a_y1 = y1 * magnification / 100;
        int a_y2 = y2 * magnification / 100;
        return a_x1 + ", " + a_y1 + ", " + a_x2 + ", " + a_y2;
    }

    public String getCgiArgument() {
        return "x1=" + getX1() +
                "&x2=" + getX2() +
                "&y1=" + getY1() +
                "&y2=" + getY2();
    }

    public Map<String, String> getQueryMap() {
        TreeMap<String, String> map = new TreeMap<>();
        map.put("x1", Integer.toString(getX1()));
        map.put("x2", Integer.toString(getX2()));
        map.put("y1", Integer.toString(getY1()));
        map.put("y2", Integer.toString(getY2()));
        return map;
    }

    private int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    private int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    private int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    private int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }
}

//--------------------------------------
// $Log: BoundingRectangle.java,v $
// Revision 1.6  2004/09/09 02:13:27  leo
// *** empty log message ***
//
// Revision 1.5  2004/08/09 09:20:35  leo
// メソッド追加
//
// Revision 1.4  2004/08/06 14:42:36  leo
// add default constructor
//
// Revision 1.3  2004/07/26 19:32:58  leo
// boudingRectangleにgetCgiArgumentを追加
//
// Revision 1.2  2004/07/25 11:25:37  leo
// 座標の表示形式を追加
//
// Revision 1.1 2004/07/13 08:02:47 leo
// fist ship
//
//--------------------------------------
