/*
 * Created on 2004/09/16
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package lab.cb.scmd.util.svg;

/**
 * @author sesejun
 * <p>
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SVGColor {
    static public final int[] WHITE = new int[]{255, 255, 255};
    static public final int[] BLACK = new int[]{0, 0, 0};
    static public final int[] BLUE = new int[]{0, 0, 255};
    static public final int[] GREEN = new int[]{0, 255, 0};
    static public final int[] RED = new int[]{255, 0, 0};
    static public final int[] NONE = new int[]{-1, -1, -1};
    private int[] color = {0, 0, 0}; // red, green, blue

    SVGColor() {

    }

    SVGColor(int[] color) {
        set(color);
    }

    public SVGColor(int r, int g, int b) {
        set(r, g, b);
    }

    public void set(int[] rgb) {
        color = rgb;
    }

    public void set(int r, int g, int b) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
    }

    public String toString() {
        String str = "";
        if (color[0] < 0) {
            str = "none";
        } else {
            str = "rgb(" + color[0] + "," + color[1] + "," + color[2] + ")";
        }
        return str;
    }
}
