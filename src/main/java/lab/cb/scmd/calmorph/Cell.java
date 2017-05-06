// ------------------------------------
// SCMD Project
//  
// Cell.java
// Since: 2004/04/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.calmorph;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;

class Cell implements Serializable {
	int[] neck_and_bud_middle; // temporary data for bud validation
	double[] grad_cept_middle;
	boolean neck_change;

	int w, h, size, id;// 画像サイズ、細胞の大きさ、bud_ratioの大きさわけ、cell番号

	private int group;

	private String Cgroup, Dgroup, Agroup;

	int budcrush;

	Vector cover, edge, mother_edge, bud_edge, bud_cover, edge_2, cover_2;

	int[] neck;

	double bud_ratio;

	double fitness;

	private double[] mother_ellipse, bud_ellipse;// 楕円パラメータ

	boolean budell_flag, bud_short_flag;

	Point point;// 細胞の左上の位置

	Point bottomrightPoint;

	private Point neckpoint, centerpoint, hippoint, budcenterpoint, budtop;

	Point[] longpoint, shortpoint, budlongpoint, budshortpoint;// 楕円の端点

	double long_length;

	double short_length;

	double budlong_length;

	double budshort_length;

	Point farfromneckpoint;

	Vector brightestCpoint, darkestCpoint;

	int Cmaxbright;

	int Cminbright;

	Vector widestCpoint, narrowestCpoint;

	int Cmaxwidth;

	int Cminwidth;

	private boolean flag_ud; // if nuclei is on both modhter and bud cells,
	// true.

	Vector Dpoint;

	Vector Dbrightpoint;

	Vector DpointB;

	Vector DbrightpointB;

	Vector[] D345point;

	Vector Dedge;

	Vector Dcover;

	Vector DcoverB;

	Vector Dtotalbright;

	Vector Dmaxbright;

	Vector DtotalbrightB;

	Vector DmaxbrightB;

	Vector Acover;

	int[] Aregionsize;

	int[] Atotalbright;

	Point[][] Acenterpoint;

	Point[] farfromneckApoint;

	double actinonneckline;

	double budactincenterposition;

	double motheractincenterposition;

	Vector actinpatchpoint, actinpatchbright, actinpatchsize;

	Point[][] Apatchcenterpoint;

	int[] actinpatchorder;

	double maxpatchdistance;

	int totalpatchsize;

	Vector actinpatchpath;

	double actinpathlength;

	int brightpatchnumber;

	Point[] Cpointparam;

	Vector[] Cpointsparam;

	double[] Cbaseparam;

	double[] Cexpandparam;

	//	Point[] Dpointparam;
	HashMap<String, Point> dPointParamMap = new HashMap<String, Point>();

	double[] Dbaseparam;

	double[] Dexpandparam;

	Point[] Apointparam;

	double[] Abaseparam;

	double[] Aexpandparam;

	double[] versparam;

	public Cell(int w, int h, int id) {
		this.w = w;
		this.h = h;
		this.size = w * h;
		this.id = id;
		init();
	}

	public void init() {
		edge = new Vector();
		edge_2 = new Vector();
		cover = new Vector();
		cover_2 = new Vector();
		mother_edge = new Vector();
		bud_edge = new Vector();
		bud_cover = new Vector();
		longpoint = new Point[2];
		shortpoint = new Point[2];
		budlongpoint = new Point[2];
		budshortpoint = new Point[2];
		actinpatchpoint = new Vector();
		actinpatchbright = new Vector();
		actinpatchsize = new Vector();
		fitness = -1;
	}

	public void setGroup(int n) {
		group = n;
	}

	public String getCgroup() {
		return Cgroup;
	}

	public String getDgroup() {
		return Dgroup;
	}

	public String getAgroup() {
		return Agroup;
	}

	public void setAgroup(String ag) {
		Agroup = ag;
	}

	public int getGroup() {
		return group;
	}

	public boolean getFlagUD() {
		return flag_ud;
	}

	public void setFlagUD(boolean ud) {
		flag_ud = ud;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// 楕円を当てる
	// //////////////////////////////////////////////////////////////////////////////
	public void setEllipse() {
		double[][] D;
		double[][] S = new double[6][6];
		double[][] C = { { 0, 0, -2, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0 },
				{ -2, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 } };
		if (group >= 1) {
			int es = mother_edge.size();
			D = new double[es][6];
			for (int i = 0; i < es; i++) {
				int p = ((Integer) mother_edge.get(i)).intValue();
				int x = p % w;
				int y = p / w;
				D[i][0] = x * x;
				D[i][1] = x * y;
				D[i][2] = y * y;
				D[i][3] = x;
				D[i][4] = y;
				D[i][5] = 1;
			}
			S = MatOp.BtB(D, D.length);
			mother_ellipse = MatOp.generalEigenVector(C, S);
			if (mother_ellipse == null) {// 楕円があたらなかったらcomplex
				group = 0;
			} else {
				double k = Math.sqrt(-1
						/ (mother_ellipse[1] * mother_ellipse[1] - 4
						* mother_ellipse[0] * mother_ellipse[2]));
				for (int i = 0; i < 6; i++) {
					mother_ellipse[i] *= k;
				}
			}
		}

		if (group > 2 || (group == 2 && bud_ratio >= 0.3)) {// medium以上の芽に楕円をあてる(smallで0.3以上のときも)
			int es = bud_edge.size();
			D = new double[es][6];
			for (int i = 0; i < es; i++) {
				int p = ((Integer) bud_edge.get(i)).intValue();
				int x = p % w;
				int y = p / w;
				D[i][0] = x * x;
				D[i][1] = x * y;
				D[i][2] = y * y;
				D[i][3] = x;
				D[i][4] = y;
				D[i][5] = 1;
			}
			S = MatOp.BtB(D, D.length);
			bud_ellipse = MatOp.generalEigenVector(C, S);
			if (bud_ellipse == null) {// 芽の楕円当てに失敗sたらcomplexに
				group = 0;
			} else {
				double k = Math.sqrt(-1
						/ (bud_ellipse[1] * bud_ellipse[1] - 4 * bud_ellipse[0]
						* bud_ellipse[2]));
				for (int i = 0; i < 6; i++) {
					bud_ellipse[i] *= k;
				}
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////
	// 出力するデータのセット
	// /////////////////////////////////////////////////////////////////////////////////////////
	public void setCellData() {
		// 位置
		int left = w, right = 0, top = h, bottom = 0;
		for (int i = 0; i < cover.size(); i++) {// pointのセット
			int p = ((Integer) cover.get(i)).intValue();
			if (left > p % w)
				left = p % w;
			if (right < p % w)
				right = p % w;
			if (top > p / w)
				top = p / w;
			if (bottom < p / w)
				bottom = p / w;
		}
		point = new Point(left, top);
		bottomrightPoint = new Point(right, bottom);
		if (group > 1) {// neckpoint,farfromneckpointのセット
			neckpoint = new Point((neck[0] % w + neck[1] % w) / 2,
					(neck[0] / w + neck[1] / w) / 2);
			double max = 0;
			int maxpoint = -1;
			for (int i = 0; i < mother_edge.size(); i++) {
				int p = ((Integer) mother_edge.get(i)).intValue();
				if (distance(p, neckpoint.y * w + neckpoint.x) > max) {
					maxpoint = p;
					max = distance(p, neckpoint.y * w + neckpoint.x);
				}
			}
			farfromneckpoint = new Point(maxpoint % w, maxpoint / w);
		}

		// グループ分け
		if (group == 0)
			Cgroup = "complex";
		else if (group == 1)
			Cgroup = "no";
		else if (group == 2)
			Cgroup = "small";
		else if (group == 3)
			Cgroup = "medium";
		else if (group == -1)
			Cgroup = "miss";
		else
			Cgroup = "large";
		Dgroup = "-";// 他でセット
		Agroup = "-";// 他でセット

		// paramのset 求めることができないパラメータについては値が-1になるようにセットしておく
		Cpointparam = new Point[11];
		for (int i = 0; i < 11; i++)
			Cpointparam[i] = new Point(-1, -1);
		Cpointsparam = new Vector[4];
		for (int i = 0; i < 4; i++)
			Cpointsparam[i] = new Vector();
		Cbaseparam = new double[5];
		for (int i = 0; i < 5; i++)
			Cbaseparam[i] = -1;
		Cexpandparam = new double[21];
		for (int i = 0; i < 21; i++)
			Cexpandparam[i] = -1;
		Apointparam = new Point[15];
		for (int i = 0; i < 15; i++)
			Apointparam[i] = new Point(-1, -1);
		Abaseparam = new double[5];
		for (int i = 0; i < 5; i++)
			Abaseparam[i] = -1;
		Aexpandparam = new double[8];
		for (int i = 0; i < 8; i++)
			Aexpandparam[i] = -1;
//		Dpointparam = new Point[31];
//		for (int i = 0; i < 31; i++)
//			Dpointparam[i] = new Point(-1, -1);
		Dbaseparam = new double[12];
		for (int i = 0; i < 12; i++)
			Dbaseparam[i] = -1;
		Dexpandparam = new double[98];
		for (int i = 0; i < 98; i++)
			Dexpandparam[i] = -1;

		// 楕円関係
		if (group > 0) {
			if (mother_ellipse != null)
				setCellEllData();
		}
		// 芽関係
		if (group > 1) {
			setBudData();
		}
		if (group == -1 || group == 0) {
		} else {
			if (group == 1
					|| distance(longpoint[0], neckpoint) < distance(
					longpoint[1], neckpoint)) {
				// C1-1
				Cpointparam[0] = longpoint[0];
				// C1-2
				Cpointparam[1] = longpoint[1];
			} else {
				// C1-1
				Cpointparam[0] = longpoint[1];
				// C1-2
				Cpointparam[1] = longpoint[0];
			}// 楕円近似の長軸の点

			if (group == 1
					|| distance(shortpoint[0], neckpoint) < distance(
					shortpoint[1], neckpoint)) {
				// C2-1
				Cpointparam[2] = shortpoint[0];
				// C2-2
				Cpointparam[3] = shortpoint[1];
			} else {
				// C2-1
				Cpointparam[2] = shortpoint[1];
				// C2-2
				Cpointparam[3] = shortpoint[0];
			}// 楕円近似の短軸の点

			if (group != 1) {
				if (distance(neck[0], Cpointparam[0]) < distance(neck[1],
						Cpointparam[0])) {
					// C3-1
					Cpointparam[4] = new Point(neck[0] % w, neck[0] / w);
					// C3-2
					Cpointparam[5] = new Point(neck[1] % w, neck[1] / w);
				} else {
					// C3-1
					Cpointparam[4] = new Point(neck[1] % w, neck[1] / w);
					// C3-2
					Cpointparam[5] = new Point(neck[0] % w, neck[0] / w);
				}
			}// ネックの両端の点
			// ネックの中点
			if (group != 1)
				Cpointparam[6] = neckpoint; // C4-1
			// 娘細胞の長軸の点
			if (group != 1)
				Cpointparam[7] = budtop; // C4-2 
			if (group != 1 && bud_short_flag) {
				if (distance(budshortpoint[0], Cpointparam[4]) < distance(
						budshortpoint[1], Cpointparam[4])) {
					// C5-1
					Cpointparam[8] = budshortpoint[0];
					// C5-2
					Cpointparam[9] = budshortpoint[1];
				} else {
					// C5-1
					Cpointparam[8] = budshortpoint[1];
					// C5-2
					Cpointparam[9] = budshortpoint[0];
				}
			}// 娘細胞の短軸の点

			// C6
			for (int i = 0; i < brightestCpoint.size(); i++) {
				int p = ((Integer) brightestCpoint.elementAt(i)).intValue();
				Cpointsparam[0].add(new Point(p % w, p / w));// 細胞壁で最も輝度の高い点
			}
			// C7
			for (int i = 0; i < darkestCpoint.size(); i++) {
				int p = ((Integer) darkestCpoint.elementAt(i)).intValue();
				Cpointsparam[1].add(new Point(p % w, p / w));// //細胞壁で最も輝度の低い点
			}
			// C8
			for (int i = 0; i < widestCpoint.size(); i++) {
				int p = ((Integer) widestCpoint.elementAt(i)).intValue();
				Cpointsparam[2].add(new Point(p % w, p / w));// //細胞壁で最も厚い点
			}
			// C9
			for (int i = 0; i < narrowestCpoint.size(); i++) {
				int p = ((Integer) narrowestCpoint.elementAt(i)).intValue();
				Cpointsparam[3].add(new Point(p % w, p / w));// //細胞壁で最も薄い点
			}

			// C10
			if (group != 1)
				Cpointparam[10] = farfromneckpoint;// ネックの中点から最も遠い外周の点

			// C11-1
			if (group == 1)
				Cbaseparam[0] = cover.size();
			else
				Cbaseparam[0] = cover.size() - bud_cover.size();// 母細胞の大きさ
			// C11-2
			if (group != 1)
				Cbaseparam[1] = bud_cover.size();// 芽の大きさ
			// C12-1
			Cbaseparam[2] = edgeSize(mother_edge);// 母細胞の外周の長さ
			// C12-2
			if (group != 1)
				Cbaseparam[3] = edgeSize(bud_edge);// 芽の外周の長さ
			// C13
			Cbaseparam[4] = fitness;// 楕円近似の適合度

			Cexpandparam[0] = cover.size();// 細胞の大きさ
			Cexpandparam[1] = edgeSize(edge);// 外周の長さ
			Cexpandparam[2] = long_length;// 母細胞の長軸の長さ
			Cexpandparam[3] = short_length;// 母細胞の短軸の長さ
			if (group != 1)
				Cexpandparam[4] = angle(neckpoint, centerpoint, longpoint[0]);// 芽のネックの傾き
			if (group != 1)
				Cexpandparam[5] = angle(budtop, neckpoint, longpoint[0],
						longpoint[1]);// 芽の方向
			if (group != 1)
				Cexpandparam[6] = budlong_length;// 芽の長軸の長さ
			if (group != 1 && bud_short_flag)
				Cexpandparam[7] = budshort_length;// 芽の短軸の長さ
			if (group != 1)
				Cexpandparam[8] = distance(neck[0], neck[1]);// 芽のネックの幅
			if (group != 1)
				Cexpandparam[9] = getPointLineDist(longpoint[0], longpoint[1],
						budtop);// 芽の先端と母細胞の長軸延長線との距離
			if (group != 1)
				Cexpandparam[10] = getPointLineDist(shortpoint[0],
						shortpoint[1], budtop);// 芽の先端と母細胞の短軸延長線との距離
			if (group != 1)
				Cexpandparam[11] = distance(neckpoint, centerpoint);// 芽のネックの中点と母細胞の中心点との距離
			if (group != 1 && Cexpandparam[5] != 0)
				Cexpandparam[12] = Cexpandparam[9]
						/ Math.sin(Cexpandparam[5] * Math.PI / 180.0);// 芽の先端から芽の長軸と母細胞の長軸との交点までの距離
			if (group != 1 && bud_short_flag)
				Cexpandparam[13] = Cexpandparam[6] / Cexpandparam[7];// 芽の長軸短軸比
			Cexpandparam[14] = Cexpandparam[2] / Cexpandparam[3];// 母細胞の長軸短軸比
			if (group != 1 && bud_short_flag)
				Cexpandparam[15] = Cexpandparam[13] / Cexpandparam[14];// 母細胞の長軸短軸比と芽の長軸短軸比の比
			if (group != 1)
				Cexpandparam[16] = Cbaseparam[3] / Cbaseparam[2];// 母細胞の外周と芽の外周の比
			if (group != 1)
				Cexpandparam[17] = Cbaseparam[1] / Cbaseparam[0];// 母細胞の面積と芽の面積の比
			Cexpandparam[18] = Cmaxbright - Cminbright;
			Cexpandparam[19] = Cmaxwidth - Cminwidth;
			if (group != 1)
				Cexpandparam[20] = distance(neckpoint, Cpointparam[10]);

		}
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// 楕円に関係するデータをセット
	// ///////////////////////////////////////////////////////////////////////////////
	public void setCellEllData() {
		double a = mother_ellipse[0] / (-mother_ellipse[5]);
		double b = mother_ellipse[1] / (-mother_ellipse[5]);
		double c = mother_ellipse[2] / (-mother_ellipse[5]);
		double d = mother_ellipse[3] / (-mother_ellipse[5]);
		double e = mother_ellipse[4] / (-mother_ellipse[5]);
		double p = (-2 * c * d + b * e) / (4 * a * c - b * b);
		double q = (b * d - 2 * a * e) / (4 * a * c - b * b);
		centerpoint = new Point((int) p, (int) q);
		double f = -a * p * p - b * p * q - c * q * q - d * p - e * q + 1;
		double sin2theta = b / Math.sqrt((a - c) * (a - c) + b * b);
		double cos2theta = (c - a) / Math.sqrt((a - c) * (a - c) + b * b);
		double sintheta = 0;
		if (b > 0)
			sintheta = Math.sqrt((1 - cos2theta) / 2);
		else
			sintheta = -Math.sqrt((1 - cos2theta) / 2);
		double costheta = Math.sqrt((1 + cos2theta) / 2);
		double aa = Math.sqrt(2 * f
				/ (a + c - Math.sqrt(b * b + (a - c) * (a - c))));
		double bb = Math.sqrt(2 * f
				/ (a + c + Math.sqrt(b * b + (a - c) * (a - c))));
		Point p1 = new Point((int) (p + aa * costheta), (int) (q - aa
				* sintheta));
		Point p2 = new Point((int) (p - aa * costheta), (int) (q + aa
				* sintheta));
		Point p3 = new Point((int) (p - bb * sintheta), (int) (q - bb
				* costheta));
		Point p4 = new Point((int) (p + bb * sintheta), (int) (q + bb
				* costheta));
		if (p1.distance(p2) <= p3.distance(p4)) {
			longpoint[0] = p3;
			longpoint[1] = p4;
			shortpoint[0] = p1;
			shortpoint[1] = p2;
		} else {
			longpoint[0] = p1;
			longpoint[1] = p2;
			shortpoint[0] = p3;
			shortpoint[1] = p4;
		}
		long_length = distance(longpoint[0], longpoint[1]);
		short_length = distance(shortpoint[0], shortpoint[1]);
		fitness = 0;
		for (int i = 0; i < mother_edge.size(); i++) {
			int po = ((Integer) mother_edge.elementAt(i)).intValue();
			int x = po % w;
			int y = po / w;
			fitness += (mother_ellipse[0] * x * x + mother_ellipse[1] * x * y
					+ mother_ellipse[2] * y * y + mother_ellipse[3] * x
					+ mother_ellipse[4] * y + mother_ellipse[5])
					* (mother_ellipse[0] * x * x + mother_ellipse[1] * x * y
					+ mother_ellipse[2] * y * y + mother_ellipse[3] * x
					+ mother_ellipse[4] * y + mother_ellipse[5]);
		}
		fitness /= mother_edge.size();
		fitness /= Math.PI * distance(longpoint[0], longpoint[1])
				* distance(shortpoint[0], shortpoint[1]) / 4.0;
		fitness /= Math.PI * distance(longpoint[0], longpoint[1])
				* distance(shortpoint[0], shortpoint[1]) / 4.0;
		if (fitness > 0.002) {
			group = 0;
			Cgroup = "complex";
			fitness = -1;
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////
	// 芽に関するデータをセット
	// ///////////////////////////////////////////////////////////////////////////////////
	public void setBudData() {
		if (group > 1) {
			if (neckpoint.distance(longpoint[0].x, longpoint[0].y) >= neckpoint
					.distance(longpoint[1].x, longpoint[1].y)) {
				hippoint = longpoint[0];
			} else {
				hippoint = longpoint[1];
			}
			double maxk = 0;
			int maxp = 0;
			for (int i = 0; i < bud_edge.size(); i++) {// 近い方のネックから一番とおい
				int p = ((Integer) bud_edge.get(i)).intValue();
				double d1 = Point2D.distance(neck[0] % w, neck[0] / w, p % w, p
						/ w);
				double d2 = Point2D.distance(neck[1] % w, neck[1] / w, p % w, p
						/ w);
				if (d1 >= d2) {// d2のが近い
					if (maxk < d2) {
						maxk = d2;
						maxp = p;
					}
				} else {// d1のが近い
					if (maxk < d1) {
						maxk = d1;
						maxp = p;
					}
				}
			}
			budtop = new Point(maxp % w, maxp / w);
			budlong_length = neckpoint.distance(budtop.x, budtop.y);
			if (getPointLineDist(new Point(neck[0] % w, neck[0] / w),
					new Point(neck[1] % w, neck[1] / w), budtop) < 1) {
				group = 0;
				Cgroup = "complex";
			}
		}
		if ((group > 2 || (group == 2 && bud_ratio >= 0.3))
				&& bud_ellipse != null) {// 芽の先端とネックを結んだ線を長軸としてマッチする楕円を計算
			budell_flag = true;
			budlongpoint[0] = new Point(budtop.x, budtop.y);
			budlongpoint[1] = new Point(neckpoint.x, neckpoint.y);
			budcenterpoint = new Point((budtop.x + neckpoint.x) / 2,
					(budtop.y + neckpoint.y) / 2);
			double costh = (budtop.x - neckpoint.x) / budlong_length;
			double sinth = (budtop.y - neckpoint.y) / budlong_length;
			double x2y2 = 0, y2 = 0, y4 = 0;
			for (int i = 0; i < bud_edge.size(); i++) {
				int p = ((Integer) bud_edge.get(i)).intValue();
				double x = (p % w - budcenterpoint.x) * costh
						+ (p / w - budcenterpoint.y) * sinth;
				double y = -(p % w - budcenterpoint.x) * sinth
						+ (p / w - budcenterpoint.y) * costh;
				x2y2 += x * x * y * y;
				y2 += y * y;
				y4 += y * y * y * y;
			}
			double s = -2 * (4 * x2y2 / budlong_length / budlong_length - y2)
					/ 2 / y4;
			if (s > 0)
				bud_short_flag = true;
			else
				bud_short_flag = false;
			if (bud_short_flag) {
				budshort_length = 2 / Math.sqrt(s);
				int x = (int) ((budcenterpoint.y - budtop.y) * budshort_length / budlong_length);
				int y = (int) ((budtop.x - budcenterpoint.x) * budshort_length / budlong_length);
				budshortpoint[0] = new Point(budcenterpoint.x + x,
						budcenterpoint.y + y);
				budshortpoint[1] = new Point(budcenterpoint.x - x,
						budcenterpoint.y - y);
			}
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// 点と直線の距離
	// ///////////////////////////////////////////////////////////////////////////////
	public double getPointLineDist(Point p1, Point p2, Point bt) {
		int a = p1.y - p2.y;
		int b = p2.x - p1.x;
		int c = -p1.y * b - p1.x * a;
		return Math.abs(a * (bt.x) + b * (bt.y) + c) / Math.sqrt(a * a + b * b);
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// conA画像の出力
	// ///////////////////////////////////////////////////////////////////////////////
	public void outCImage(Graphics g) {
		g.setColor(Color.blue);
		g.setFont(new Font("Courier", Font.PLAIN, 15));
		g.drawString(id + " " + Cgroup, point.x, point.y);
		g.setColor(Color.green);
		for (int j = 0; j < edge.size(); j++) {
			int p = ((Integer) edge.get(j)).intValue();
			g.fillRect(p % w, p / w, 1, 1);
		}
		if (group > 1) {
			g.setColor(Color.red);
			if (neck != null) {
				for (int j = 0; j < 2; j++) {
					g.fillOval(neck[j] % w - 2, neck[j] / w - 2, 4, 4);
				}
			}
		}
		if (group > 1) {
			g.setColor(Color.green);
			g.fillOval(budtop.x - 2, budtop.y - 2, 4, 4);
		}
		g.setColor(Color.yellow);
		if (group > 0) {
			g.drawLine(longpoint[0].x, longpoint[0].y, longpoint[1].x,
					longpoint[1].y);
			g.drawLine(shortpoint[0].x, shortpoint[0].y, shortpoint[1].x,
					shortpoint[1].y);
		}
		g.setColor(Color.yellow);
		if (budell_flag) {
			g.drawLine(budlongpoint[0].x, budlongpoint[0].y, budlongpoint[1].x,
					budlongpoint[1].y);
			if (bud_short_flag)
				g.drawLine(budshortpoint[0].x, budshortpoint[0].y,
						budshortpoint[1].x, budshortpoint[1].y);
		}
		/*
		 * g.setColor(Color.red); if(group > 0){ for(int i=0;i
		 * <brightestCpoint.size();i++){
		 * g.fillOval(((Integer)brightestCpoint.get(i)).intValue()%w-1,((Integer)brightestCpoint.get(i)).intValue()/w-1,3,3); }
		 * g.setColor(Color.blue); for(int i=0;i <darkestCpoint.size();i++){
		 * g.fillOval(((Integer)darkestCpoint.get(i)).intValue()%w-1,((Integer)darkestCpoint.get(i)).intValue()/w-1,3,3); } }
		 */
		/*
		 * g.setColor(Color.red); if(group > 0){ for(int i=0;i
		 * <widestCpoint.size();i++){
		 * g.fillOval(((Integer)widestCpoint.get(i)).intValue()%w-1,((Integer)widestCpoint.get(i)).intValue()/w-1,3,3); }
		 * g.setColor(Color.blue); for(int i=0;i <narrowestCpoint.size();i++){
		 * g.fillOval(((Integer)narrowestCpoint.get(i)).intValue()%w-1,((Integer)narrowestCpoint.get(i)).intValue()/w-1,3,3); }
		 */
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// 核に関するデータをセット
	// ///////////////////////////////////////////////////////////////////////////////
	public void setDState() {
		if (group > 0) {
			if (Dpoint.size() != 1 && Dpoint.size() != 2)
				Dgroup = "E";
			else if (Dpoint.size() == 2) {// 核が２個
				if (group > 1) {// 芽ありの場合
					if (inmother(((Point) Dpoint.elementAt(0)).y * w
							+ ((Point) Dpoint.elementAt(0)).x)
							^ inmother(((Point) Dpoint.elementAt(1)).y * w
							+ ((Point) Dpoint.elementAt(1)).x)) {
						Dgroup = "C";
					} else
						Dgroup = "D";
				} else
					Dgroup = "D";
			} else {
				if (group == 1) {
					Dgroup = "A";
				} else if (group > 1) {
					if (flag_ud) {
						Dgroup = "B";
					} else if (!inmother(((Point) Dpoint.elementAt(0)).y * w
							+ ((Point) Dpoint.elementAt(0)).x)) {
						Dgroup = "F";
					} else {
						Dgroup = "A1";
					}
				}
			}

			// D101
			Dexpandparam[0] = Dpoint.size();// これのみDEFについても数える

			if (Dgroup.equals("A") || Dgroup.equals("A1") || Dgroup.equals("B")
					|| Dgroup.equals("C")) {
				{
					// c = 0, D1-1
					if (Dgroup.equals("B")) {
						dPointParamMap.put("d000", (Point) DpointB.elementAt(0));
					} else {
						// 核領域の重心（母）
						dPointParamMap.put("d000", (Point) Dpoint.elementAt(0));
					}
					// c = 1, D1-2
					if (Dgroup.equals("B")) {
						dPointParamMap.put("d001", (Point) DpointB.elementAt(1));
					} else if (Dgroup.equals("C")) {
						// 核領域の重心（娘）
						dPointParamMap.put("d001", (Point) Dpoint.elementAt(1));
					} else {
						dPointParamMap.put("d001", ParameterStatus.missingValuePoint);
					}
					// c = 2, D1-3-1
					if (Dgroup.equals("A")
							|| Dgroup.equals("A1")
							|| (Dgroup.equals("B") && inmother((Point) Dpoint
							.elementAt(0)))) {
						// 核領域の重心（全体、母側）
						dPointParamMap.put("d002", (Point) Dpoint.elementAt(0));
					} else {
						dPointParamMap.put("d002", ParameterStatus.missingValuePoint);
					}
					// c = 3, D1-3-2
					if (Dgroup.equals("B")
							&& !inmother((Point) Dpoint.elementAt(0))) {
						// 核領域の重心（全体、娘側）
						dPointParamMap.put("d003", (Point) Dpoint.elementAt(0));
					} else {
						dPointParamMap.put("d003", ParameterStatus.missingValuePoint);
					}
					// c = 4, D2-1
					if (Dgroup.equals("B")) {
						dPointParamMap.put("d004", (Point) DbrightpointB.elementAt(0));
					} else {
						// 核領域の最大輝点（母）
						dPointParamMap.put("d004", (Point) Dbrightpoint.elementAt(0));
					}
					// c = 5, D2-2
					if (Dgroup.equals("B")) {
						dPointParamMap.put("d005", (Point) DbrightpointB.elementAt(1));
					} else if (Dgroup.equals("C")) {
						// 核領域の最大輝点（娘）
						dPointParamMap.put("d005", (Point) Dbrightpoint.elementAt(1));
					} else {
						dPointParamMap.put("d005", ParameterStatus.missingValuePoint);
					}
					// c = 6, D2-3-1
					if (Dgroup.equals("A")
							|| Dgroup.equals("A1")
							|| (Dgroup.equals("B") &&
							inmother(((Point) Dbrightpoint.elementAt(0)).y
									* w + (((Point) Dbrightpoint.elementAt(0)).x)))) {
						// 核領域の最大輝点（全体、母側）
						dPointParamMap.put("d006", (Point)Dbrightpoint.elementAt(0));
					} else {
						dPointParamMap.put("d006", ParameterStatus.missingValuePoint);
					}
					// c = 7, D2-3-2
					if (Dgroup.equals("B")
							&& !inmother((Point) Dbrightpoint.elementAt(0))) {
						// 核領域の最大輝点（全体、娘側）
						dPointParamMap.put("d007", (Point)Dbrightpoint.elementAt(0));
					} else {
						dPointParamMap.put("d007", ParameterStatus.missingValuePoint);
					}
					// c = 8, D3-1
					if (!Dgroup.equals("B")) {
						// 重心から最も遠い核の外周点（母）
						dPointParamMap.put("d008", (Point) D345point[0].elementAt(0));
					} else {
						dPointParamMap.put("d008", ParameterStatus.missingValuePoint);
					}
					// c = 9, D3-2
					if (Dgroup.equals("C")) {
						// 重心から最も遠い核の外周点（娘）
						dPointParamMap.put("d009", (Point) D345point[0].elementAt(1));
					} else {
						dPointParamMap.put("d009", ParameterStatus.missingValuePoint);
					}
					// c = 10, D3-3
					if (!Dgroup.equals("C")) {
						// 重心から最も遠い核の外周点（全体）
						dPointParamMap.put("d010", (Point) D345point[0].elementAt(0));
					} else {
						dPointParamMap.put("d010", ParameterStatus.missingValuePoint);
					}
					// c = 11, D4-1
					if (!Dgroup.equals("B")) {
						// D3から最も遠い核の外周点（母）
						dPointParamMap.put("d011", (Point) D345point[1].elementAt(0));
					} else {
						dPointParamMap.put("d011", ParameterStatus.missingValuePoint);
					}
					// c = 12, D4-2
					if (Dgroup.equals("C")) {
						// D3から最も遠い核の外周点（娘）
						dPointParamMap.put("d012", (Point) D345point[1].elementAt(1));
					} else {
						dPointParamMap.put("d012", ParameterStatus.missingValuePoint);
					}
					// c = 13, D4-3
					if (!Dgroup.equals("C")) {
						// D3から最も遠い核の外周点（全体）
						dPointParamMap.put("d013", (Point) D345point[1].elementAt(0));
					} else {
						dPointParamMap.put("d013", ParameterStatus.missingValuePoint);
					}
					// c = 14, D5-1
					if (!Dgroup.equals("B")) {
						// 重心を通り線分D3D4に対して垂直に交わる直線と核の外周と交わる遠いほうの点（母）
						dPointParamMap.put("d014", (Point) D345point[2].elementAt(0));
					} else {
						dPointParamMap.put("d014", ParameterStatus.missingValuePoint);
					}
					// c = 15, D5-2
					if (Dgroup.equals("C")) {
						// 重心を通り線分D3D4に対して垂直に交わる直線と核の外周と交わる遠いほうの点（娘）
						dPointParamMap.put("d015", (Point) D345point[2].elementAt(1));
					} else {
						dPointParamMap.put("d015", ParameterStatus.missingValuePoint);
					}
					// c = 16, D5-3
					if (!Dgroup.equals("C")) {
						// 重心を通り線分D3D4に対して垂直に交わる直線と核の外周と交わる遠いほうの点（全体）
						dPointParamMap.put("d016", (Point) D345point[2].elementAt(0));
					} else {
						dPointParamMap.put("d016", ParameterStatus.missingValuePoint);
					}
					// c = 17, D6-1
					if (Dgroup.equals("A1") || Dgroup.equals("C")) {
						// 核の重心とネックの中点を結んだ直線と核の外周との交点（母）
						Point[] intersection
								= Intersection((Point) Dpoint.elementAt(0),
								neckpoint, (Vector) Dedge.elementAt(0));
						dPointParamMap.put("d017", intersection[2]);
					} else {
						dPointParamMap.put("d017", ParameterStatus.missingValuePoint);
					}
					// c = 18, D6-2
					if (Dgroup.equals("C")) {
						// 核の重心とネックの中点を結んだ直線と核の外周との交点（娘）
						Point[] intersection =
								Intersection((Point) Dpoint.elementAt(1),
										neckpoint, (Vector) Dedge.elementAt(1));
						dPointParamMap.put("d018", intersection[2]);
					} else {
						dPointParamMap.put("d018", ParameterStatus.missingValuePoint);
					}
					// c = 19, D7
					if (Dgroup.equals("A1") || Dgroup.equals("C")) {
						// 核の重心と壁の外周の最遠点を結んだ直線と核の外周との交点
						Point[] intersection =
								Intersection((Point) Dpoint.elementAt(0),
										farfromneckpoint, (Vector) Dedge.elementAt(0));
						dPointParamMap.put("d019", intersection[2]);
					} else {
						dPointParamMap.put("d019", ParameterStatus.missingValuePoint);
					}
					// c = 20, D8
					if (Dgroup.equals("C")) {
						// 核の重心と芽の先端を結んだ直線と核の外周との交点
						Point[] intersection =
								Intersection((Point) Dpoint.elementAt(1),
										budtop, (Vector) Dedge.elementAt(1));
						dPointParamMap.put("d020", intersection[2]);
					} else {
						dPointParamMap.put("d020", ParameterStatus.missingValuePoint);
					}
					// c = 21, D9-1
					if (!Dgroup.equals("B")) {
						Point[] pp = Intersection((Point) Dpoint.elementAt(0),
								centerpoint, mother_edge);
						// 細胞中心から核の重心へ引いた直線と細胞外周との交点（母）
						if (pp[0].x == -1 && pp[2].x == -1
								&& !Dgroup.equals("A")) {
							dPointParamMap.put("d021",
									Intersection2((Point) Dpoint.elementAt(0),
											centerpoint,
											new Point(neck[0] % w, neck[0] / w),
											new Point(neck[1] % w, neck[1] / w))
							);
						} else {
							dPointParamMap.put("d021", pp[0]);
						}
					} else {
						dPointParamMap.put("d021", ParameterStatus.missingValuePoint);
					}
					// c = 22, D9-2
					if (Dgroup.equals("C") && budell_flag) {
						Point[] pp = Intersection((Point) Dpoint.elementAt(1),
								budcenterpoint, bud_edge);
						// 細胞中心から核の重心へ引いた直線と細胞外周との交点（娘）
						if (pp[0].x == -1 && pp[2].x == -1) {
							dPointParamMap.put("d022", Intersection2((Point) Dpoint
									.elementAt(1), budcenterpoint, new Point(
									neck[0] % w, neck[0] / w), new Point(neck[1]
									% w, neck[1] / w)));
						} else {
							dPointParamMap.put("d022", pp[0]);
						}
					} else {
						dPointParamMap.put("d022", ParameterStatus.missingValuePoint);
					}
					// c = 23, D10-1
					if (!Dgroup.equals("B")) {
						Point[] pp = Intersection(
								(Point) Dbrightpoint.elementAt(0), centerpoint,
								mother_edge);
						// 細胞中心から核の最大輝点へ引いた直線と細胞外周との交点（母）
						if (pp[0].x == -1 && pp[2].x == -1
								&& !Dgroup.equals("A")) {
							dPointParamMap.put("d023", Intersection2((Point) Dbrightpoint
									.elementAt(0), centerpoint, new Point(neck[0]
									% w, neck[0] / w), new Point(neck[1] % w,
									neck[1] / w)));
						} else {
							dPointParamMap.put("d023", pp[0]);
						}
					} else {
						dPointParamMap.put("d023", ParameterStatus.missingValuePoint);
					}
					// c = 24, D10-2
					if (Dgroup.equals("C") && budell_flag) {
						Point[] pp = Intersection(
								(Point) Dbrightpoint.elementAt(1), budcenterpoint,
								bud_edge);
						// 細胞中心から核の最大輝点へ引いた直線と細胞外周との交点（娘）
						if (pp[0].x == -1 && pp[2].x == -1) {
							dPointParamMap.put("d024",
									Intersection2((Point) Dbrightpoint
											.elementAt(1), budcenterpoint, new Point(
											neck[0] % w, neck[0] / w), new Point(neck[1]
											% w, neck[1] / w)));
						} else {
							dPointParamMap.put("d024", pp[0]);
						}
					} else {
						dPointParamMap.put("d024", ParameterStatus.missingValuePoint);
					}
					// c = 25, D11-1
					if (Dgroup.equals("B")) {
						dPointParamMap.put("d025",
								Intersection((Point) DpointB.elementAt(0),
										neckpoint, mother_edge)[0]);
						// D1-1とネックの中点を結ぶ直線と細胞外周との交点（母）
					} else if (Dgroup.equals("C") || Dgroup.equals("A1")) {
						dPointParamMap.put("d025",
								Intersection((Point) Dpoint.elementAt(0),
										neckpoint, mother_edge)[0]);
					} else {
						dPointParamMap.put("d025", ParameterStatus.missingValuePoint);
					}
					// c = 26, D11-2
					if (Dgroup.equals("B")) {
						dPointParamMap.put("d026",
								Intersection(neckpoint, (Point) DpointB
										.elementAt(1), bud_edge)[1]);
						// D1-2とネックの中点を結ぶ直線と細胞外周との交点（娘）
					} else if (Dgroup.equals("C")) {
						dPointParamMap.put("d026",
								Intersection(neckpoint, (Point) Dpoint
										.elementAt(1), bud_edge)[1]);
					} else {
						dPointParamMap.put("d026", ParameterStatus.missingValuePoint);
					}
					// c = 27, D12-1
					if (Dgroup.equals("B")) {
						dPointParamMap.put("d027",
								Intersection((Point) DbrightpointB
										.elementAt(0), neckpoint, mother_edge)[0]);
						// D2-1とネックの中点を結ぶ直線と細胞外周との交点（母）
					} else if (Dgroup.equals("C") || Dgroup.equals("A1")) {
						dPointParamMap.put("d027",
								Intersection((Point) Dbrightpoint
										.elementAt(0), neckpoint, mother_edge)[0]);
					} else {
						dPointParamMap.put("d027", ParameterStatus.missingValuePoint);
					}
					// c = 28, D12-2
					if (Dgroup.equals("B")) {
						dPointParamMap.put("d028",
								Intersection(neckpoint,
										(Point) DbrightpointB.elementAt(1), bud_edge)[1]);// D2-2とネックの中点を結ぶ直線と細胞外周との交点（娘）
					} else if (Dgroup.equals("C")) {
						dPointParamMap.put("d028",
								Intersection(neckpoint,
										(Point) Dbrightpoint.elementAt(1), bud_edge)[1]);
					} else {
						dPointParamMap.put("d028", ParameterStatus.missingValuePoint);
					}
					// c = 29, D13-1
					if (Dgroup.equals("B")) {
						if (inmother((Point) D345point[0].elementAt(0))
								&& !inmother((Point) D345point[1].elementAt(0))) {
							dPointParamMap.put("d029",
									Intersection((Point) D345point[0].elementAt(0), neckpoint, mother_edge)[0]);
						} else if (!inmother((Point) D345point[0].elementAt(0))
								&& inmother((Point) D345point[1].elementAt(0))) {
							dPointParamMap.put("d029",
									Intersection(neckpoint,
											(Point) D345point[1].elementAt(0), mother_edge)[1]);
							// B細胞についてD3-3orD4-3とネックの中点を結ぶ直線と細胞外周との交点（母）
						}
					} else {
						dPointParamMap.put("d029", ParameterStatus.missingValuePoint);
					}
					// c = 30, D13-2
					if (Dgroup.equals("B")) {
						if (!inmother((Point) D345point[0].elementAt(0))
								&& inmother((Point) D345point[1].elementAt(0))) {
							dPointParamMap.put("d030",
									Intersection((Point) D345point[0].elementAt(0), neckpoint, bud_edge)[0]);
						} else if (inmother((Point) D345point[0].elementAt(0))
								&& !inmother((Point) D345point[1].elementAt(0))) {
							dPointParamMap.put("d030",
									Intersection(neckpoint,
											(Point) D345point[1].elementAt(0), bud_edge)[1]);
							// B細胞についてD3-3orD4-3とネックの中点を結ぶ直線と細胞外周との交点（娘）
						}
					} else {
						dPointParamMap.put("d030", ParameterStatus.missingValuePoint);
					}
					// c=30
				}

				{
					int c = 0;

					// c = 0, D14-1
					if (Dgroup.equals("B"))
						Dbaseparam[c] = ((Vector) DcoverB.elementAt(0)).size();// 核領域の面積（母）
					else
						Dbaseparam[c] = ((Vector) Dcover.elementAt(0)).size();
					c++;
					// c = 1, D14-2
					if (Dgroup.equals("B"))
						Dbaseparam[c] = ((Vector) DcoverB.elementAt(1)).size();// 核領域の面積（娘）
					else if (Dgroup.equals("C"))
						Dbaseparam[c] = ((Vector) Dcover.elementAt(1)).size();
					c++;
					// c = 2, D14-3
					if (Dgroup.equals("A") || Dgroup.equals("A1"))
						Dbaseparam[c] = Dbaseparam[c - 2];// 核領域の面積（全体）
					else
						Dbaseparam[c] = Dbaseparam[c - 2] + Dbaseparam[c - 1];
					c++;
					// c = 3, D15-1
					if (Dgroup.equals("B"))
						Dbaseparam[c] = ((Integer) DtotalbrightB.elementAt(0))
								.intValue();// 核領域の輝度の合計（母）
					else
						Dbaseparam[c] = ((Integer) Dtotalbright.elementAt(0))
								.intValue();
					c++;
					// c = 4, D15-2
					if (Dgroup.equals("B"))
						Dbaseparam[c] = ((Integer) DtotalbrightB.elementAt(1))
								.intValue();// 核領域の輝度の合計（娘）
					else if (Dgroup.equals("C"))
						Dbaseparam[c] = ((Integer) Dtotalbright.elementAt(1))
								.intValue();
					c++;
					// c = 5, D15-3
					if (Dgroup.equals("A") || Dgroup.equals("A1"))
						Dbaseparam[c] = Dbaseparam[c - 2];// 核領域の輝度の合計（全体）
					else
						Dbaseparam[c] = Dbaseparam[c - 2] + Dbaseparam[c - 1];
					c++;
					// c = 6, D16-1
					if (Dgroup.equals("B"))
						Dbaseparam[c] = ((Integer) DmaxbrightB.elementAt(0))
								.intValue();// 核領域の最大輝度（母）
					else
						Dbaseparam[c] = ((Integer) Dmaxbright.elementAt(0))
								.intValue();
					c++;
					// c = 7, D16-2
					if (Dgroup.equals("B"))
						Dbaseparam[c] = ((Integer) DmaxbrightB.elementAt(1))
								.intValue();// 核領域の最大輝度（娘）
					else if (Dgroup.equals("C"))
						Dbaseparam[c] = ((Integer) Dmaxbright.elementAt(1))
								.intValue();
					c++;
					// c = 8, D16-3
					if (Dgroup.equals("C"))
						Dbaseparam[c] = Math.max(Dbaseparam[c - 2],
								Dbaseparam[c - 1]);// 核領域の最大輝度（全体）
					else if (Dgroup.equals("B"))
						Dbaseparam[c] = ((Integer) Dmaxbright.elementAt(0))
								.intValue();
					else
						Dbaseparam[c] = Dbaseparam[c - 2];
					c++;
					// c = 9, D17-1
					if (!Dgroup.equals("B"))
						Dbaseparam[c] = getfitness((Vector) Dedge.elementAt(0));
					c++;
					// c = 10, D17-2
					if (Dgroup.equals("C"))
						Dbaseparam[c] = getfitness((Vector) Dedge.elementAt(1));
					c++;
					// c = 11, D17-3
					if (!Dgroup.equals("C"))
						Dbaseparam[c] = getfitness((Vector) Dedge.elementAt(0));
					// c=11
				}


				// begin of dexppandparam
				{
					int c = 1;
					// D102
					// aka D1-1C1-2 (A)
					if (Dgroup.equals("A"))
						Dexpandparam[c] = Math.max(
								distance(longpoint[0], dPointParamMap.get("d000")),
								distance(longpoint[1], dPointParamMap.get("d000"))
						);
					c++;
					// D103
					// aka D1-1C1-2(!A)
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = distance(hippoint, dPointParamMap.get("d000"));
					c++;
					// D104
					if (Dgroup.equals("A"))
						Dexpandparam[c] = Math.max(
								distance(longpoint[0], dPointParamMap.get("d000")),
								distance(longpoint[1], dPointParamMap.get("d000")));// D1-3C1-2
					else if (!Dgroup.equals("C"))
						Dexpandparam[c] = distance(hippoint, dPointParamMap.get("d002"));
					if (Dgroup.equals("B") && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(hippoint, dPointParamMap.get("d003"));
					c++;
					// D105
					if (Dgroup.equals("A") && Dexpandparam[c - 3] != -1)
						Dexpandparam[c] = Dexpandparam[c - 3] / long_length;// D1-1C1-2/C103(A)
					c++;
					// D106
					if (!Dgroup.equals("A") && Dexpandparam[c - 3] != -1)
						Dexpandparam[c] = Dexpandparam[c - 3] / long_length;// D1-1C1-2/C103
					// (!A)
					c++;
					// D107
					if (!Dgroup.equals("C") && Dexpandparam[c - 3] != -1)
						Dexpandparam[c] = Dexpandparam[c - 3] / long_length;// D1-3C1-2/C103
					c++;
					// D108
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d000"));// D1-1M1
					c++;
					// D109
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d001"));// D1-2M1
					c++;
					// D110
					if (Dgroup.equals("A1") || Dgroup.equals("B"))
						Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d002"));// D1-3-1M1
					c++;
					// D111
					if (Dgroup.equals("B"))
						Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d003"));// D1-3-2M1
					c++;
					// D112
					if (!Dgroup.equals("A") && Dexpandparam[c - 4] != -1)
						Dexpandparam[c] = Dexpandparam[c - 4]
								/ distance(neckpoint, farfromneckpoint);// D1-1M1/C128
					c++;
					// D113
					if ((Dgroup.equals("B") || Dgroup.equals("C"))
							&& Dexpandparam[c - 4] != -1)
						Dexpandparam[c] = Dexpandparam[c - 4] / budlong_length;// D1-2M1/C107
					c++;
					// D114
					if ((Dgroup.equals("A1") || Dgroup.equals("B"))
							&& Dexpandparam[c - 4] != -1)
						Dexpandparam[c] = Dexpandparam[c - 4]
								/ distance(neckpoint, farfromneckpoint);// D1-3-1M1/C128
					c++;
					// D115
					if (Dgroup.equals("B") && Dexpandparam[c - 4] != -1)
						Dexpandparam[c] = Dexpandparam[c - 4] / budlong_length;// D1-3-2M1/C107
					c++;
					// D116
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = distance(dPointParamMap.get("d000"), neckpoint)
								+ distance(dPointParamMap.get("d001"), neckpoint);// D1-1M1D1-2
					c++;
					// D117
					Dexpandparam[c] = distance(centerpoint, dPointParamMap.get("d000"));// D1-1C1
					c++;
					// D118
					if (!Dgroup.equals("C"))
						Dexpandparam[c] = distance(centerpoint, dPointParamMap.get("d002"));// D1-3C1
					if (Dgroup.equals("B") && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(centerpoint, dPointParamMap.get("d003"));
					c++;
					// D119
					if ((Dgroup.equals("B") || Dgroup.equals("C")) && budell_flag)
						Dexpandparam[c] = distance(budcenterpoint, dPointParamMap.get("d001"));// D1-2C2
					c++;
					// D120
					if (Dgroup.equals("B") && budell_flag)
						Dexpandparam[c] = distance(budcenterpoint, dPointParamMap.get("d003"));// D1-3C2
					if (Dgroup.equals("B") && budell_flag && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(budcenterpoint, dPointParamMap.get("d002"));;
					c++;
					// D121
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = distance(budtop, dPointParamMap.get("d001"));// D1-2C4-2
					c++;
					// D122
					if (Dgroup.equals("B"))
						Dexpandparam[c] = distance(budtop, dPointParamMap.get("d003"));// D1-3C4-2
					if (Dgroup.equals("B") && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(budtop, dPointParamMap.get("d002"));
					c++;
					// D123
					if ((Dgroup.equals("B") || Dgroup.equals("C"))
							&& Dexpandparam[c - 2] != -1)
						Dexpandparam[c] = Dexpandparam[c - 2] / budlong_length;// D1-2C4-2/C107
					c++;
					// D124
					if (Dgroup.equals("B") && Dexpandparam[c - 2] != -1)
						Dexpandparam[c] = Dexpandparam[c - 2] / budlong_length;// D1-3C4-2/C107
					c++;
					// D125
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = distance(farfromneckpoint, dPointParamMap.get("d000"));// D1-1C10
					c++;
					// D126
					if (Dgroup.equals("A1") || Dgroup.equals("B"))
						Dexpandparam[c] = distance(farfromneckpoint, dPointParamMap.get("d002"));// D1-3C10
					if (Dgroup.equals("B") && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(farfromneckpoint, dPointParamMap.get("d003"));
					c++;
					// D127
					if (Dgroup.equals("A"))
						Dexpandparam[c] = Math.max(distance(longpoint[0],
								dPointParamMap.get("d004")), distance(longpoint[1],
								dPointParamMap.get("d004")));// D2-1C1-2 (A)
					c++;
					// D128
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = distance(hippoint, dPointParamMap.get("d004"));// D2-1C1-2
					// (!A)
					c++;
					// D129
					if (Dgroup.equals("A"))
						Dexpandparam[c] = Math.max(distance(longpoint[0],
								dPointParamMap.get("d000")), distance(longpoint[1],
								dPointParamMap.get("d004")));// D2-3C1-2
					else if (!Dgroup.equals("C"))
						Dexpandparam[c] = distance(hippoint, dPointParamMap.get("d006"));
					if (Dgroup.equals("B") && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(hippoint, dPointParamMap.get("d007"));
					c++;
					// D130
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d004"));// D2-1M1
					c++;
					// D131
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d005"));// D2-2M1
					c++;
					// D132
					if (Dgroup.equals("A1") || Dgroup.equals("B"))
						Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d006"));// D2-3-1M1
					c++;
					// D133
					if (Dgroup.equals("B"))
						Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d007"));// D2-3-2M1
					c++;
					// D134
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = distance(dPointParamMap.get("d004"), neckpoint)
								+ distance(dPointParamMap.get("d005"), neckpoint);// D2-1M1D2-2
					c++;
					// D135
					Dexpandparam[c] = distance(centerpoint, dPointParamMap.get("d004"));// D2-1C1
					c++;
					// D136
					if (!Dgroup.equals("C"))
						Dexpandparam[c] = distance(centerpoint, dPointParamMap.get("d006"));// D2-3C1
					if (Dgroup.equals("B") && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(centerpoint, dPointParamMap.get("d007"));
					c++;
					// D137
					if ((Dgroup.equals("B") || Dgroup.equals("C")) && budell_flag)
						Dexpandparam[c] = distance(budcenterpoint, dPointParamMap.get("d005"));// D2-2C2
					c++;
					// D138
					if (Dgroup.equals("B") && budell_flag)
						Dexpandparam[c] = distance(budcenterpoint, dPointParamMap.get("d007"));// D2-3C2
					if (Dgroup.equals("B") && budell_flag && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(budcenterpoint, dPointParamMap.get("d006"));
					c++;
					// D139
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = distance(budtop, dPointParamMap.get("d005"));// D2-2C4-2
					c++;
					// D140
					if (Dgroup.equals("B"))
						Dexpandparam[c] = distance(budtop, dPointParamMap.get("d007"));// D2-3C4-2
					if (Dgroup.equals("B") && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(budtop, dPointParamMap.get("d006"));
					c++;
					// D141
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = distance(farfromneckpoint, dPointParamMap.get("d004"));// D2-1C10
					c++;
					// D142
					if (Dgroup.equals("A1") || Dgroup.equals("B"))
						Dexpandparam[c] = distance(farfromneckpoint, dPointParamMap.get("d006"));// D2-3C10
					if (Dgroup.equals("B") && Dexpandparam[c] == -1)
						Dexpandparam[c] = distance(farfromneckpoint, dPointParamMap.get("d007"));
					c++;
					// D143
					if (Dgroup.equals("A1") || Dgroup.equals("C")) {
						if (dPointParamMap.get("d017").x != -1)
							Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d017"));// D6-1M1
						else
							Dexpandparam[c] = 0;
					}
					c++;
					// D144
					if (Dgroup.equals("C")) {
						if (dPointParamMap.get("d018").x != -1)
							Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d018"));// D6-2M1
						else
							Dexpandparam[c] = 0;
					}
					c++;
					// D145
					if (Dgroup.equals("A1") || Dgroup.equals("C"))
						Dexpandparam[c] = distance(farfromneckpoint,
								dPointParamMap.get("d019"));// D7C10
					c++;
					// D146
					if (Dgroup.equals("C"))
						Dexpandparam[c] = distance(budtop, dPointParamMap.get("d020"));// D8C4-2
					c++;
					// D147
					if (!Dgroup.equals("B")) {
						if (distance(centerpoint, dPointParamMap.get("d000")) == 0)
							Dexpandparam[c] = 0;
						else if (dPointParamMap.get("d021").x != -1)
							Dexpandparam[c] = distance(centerpoint, dPointParamMap.get("d000"))
									/ distance(centerpoint, dPointParamMap.get("d021"));// D1-1C1/C1D9-1
					}
					c++;
					// D148
					if (!Dgroup.equals("B")) {
						if (distance(centerpoint, dPointParamMap.get("d004")) == 0)
							Dexpandparam[c] = 0;
						else if (dPointParamMap.get("d023").x != -1)
							Dexpandparam[c] = distance(centerpoint, dPointParamMap.get("d004"))
									/ distance(centerpoint, dPointParamMap.get("d023"));// D2-1C1/C1D10-1
					}
					c++;
					// D149
					if (Dgroup.equals("C") && budell_flag) {
						if (distance(budcenterpoint, dPointParamMap.get("d001")) == 0)
							Dexpandparam[c] = 0;
						else if (dPointParamMap.get("d022").x != -1)
							Dexpandparam[c] = distance(budcenterpoint,
									dPointParamMap.get("d001"))
									/ distance(budcenterpoint, dPointParamMap.get("d022"));// D1-2C2/C2D9-2
					}
					c++;
					// D150
					if (Dgroup.equals("C") && budell_flag) {
						if (distance(budcenterpoint, dPointParamMap.get("d005")) == 0)
							Dexpandparam[c] = 0;
						else if (dPointParamMap.get("d024").x != -1)
							Dexpandparam[c] = distance(budcenterpoint,
									dPointParamMap.get("d005"))
									/ distance(budcenterpoint, dPointParamMap.get("d024"));// D2-2C2/C2D10-2
					}
					c++;
					// D151
					if (Dgroup.equals("C")) {
						if (dPointParamMap.get("d017").x != -1 && dPointParamMap.get("d018").x != -1
								&& distance(neckpoint, dPointParamMap.get("d017")) != 0)
							Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d018"))
									/ distance(neckpoint, dPointParamMap.get("d017"));// D6-2M1/D6-1M1;
						else
							Dexpandparam[c] = 0;
					}
					c++;
					// D152
					if (Dgroup.equals("A1") || Dgroup.equals("C")) {
						if (dPointParamMap.get("d017").x != -1
								&& distance(farfromneckpoint, dPointParamMap.get("d019")) != 0)
							Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d017"))
									/ distance(farfromneckpoint, dPointParamMap.get("d019"));// D6-1M1/D7C10
						else
							Dexpandparam[c] = 0;
					}
					c++;
					// D153
					if (Dgroup.equals("C")) {
						if (dPointParamMap.get("d018").x != -1
								&& distance(budtop, dPointParamMap.get("d020")) != 0)
							Dexpandparam[c] = distance(neckpoint, dPointParamMap.get("d018"))
									/ distance(budtop, dPointParamMap.get("d020"));// D6-2M1/D8C4-2
						else
							Dexpandparam[c] = 0;
					}
					c++;
					// D154
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = angle(hippoint, centerpoint,
								dPointParamMap.get("d000"));
					else
						Dexpandparam[c] = angle(longpoint[0], centerpoint,
								dPointParamMap.get("d000"));// angD1-1C1C1-2
					c++;
					// D155
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = angle(hippoint, centerpoint,
								dPointParamMap.get("d004"));
					else
						Dexpandparam[c] = angle(longpoint[0], centerpoint,
								dPointParamMap.get("d004"));// angD2-1C1C1-2
					c++;
					// D156
					if ((Dgroup.equals("B") || Dgroup.equals("C")) && budell_flag)
						Dexpandparam[c] = angle(budtop, budcenterpoint,
								dPointParamMap.get("d001"));// angD1-2C2C4-2
					c++;
					// D157
					if ((Dgroup.equals("B") || Dgroup.equals("C")) && budell_flag)
						Dexpandparam[c] = angle(budtop, budcenterpoint,
								dPointParamMap.get("d005"));// angD2-2C2C4-2
					c++;
					// D158
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = angle(longpoint[0], longpoint[1],
								dPointParamMap.get("d000"), dPointParamMap.get("d001"));// angD1-1D18-1C1-2
					c++;
					// D159
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = angle(longpoint[0], longpoint[1],
								dPointParamMap.get("d004"), dPointParamMap.get("d005"));// angD2-1D19-1C1-2
					c++;
					// D160
					if (Dgroup.equals("A1"))
						Dexpandparam[c] = angle(longpoint[0], longpoint[1],
								dPointParamMap.get("d008"), dPointParamMap.get("d011"));// angD4-1D20C1-2
					c++;
					// D161
					if (Dgroup.equals("B"))
						Dexpandparam[c] = angle(longpoint[0], longpoint[1],
								dPointParamMap.get("d010"), dPointParamMap.get("d013"));// angD4-3D21-1C1-2
					c++;
					// D162
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = angle(centerpoint, neckpoint,
								dPointParamMap.get("d000"), dPointParamMap.get("d001"));// angD1-1D22C1
					c++;
					// D163
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = angle(centerpoint, neckpoint,
								dPointParamMap.get("d004"), dPointParamMap.get("d005"));// angD2-1D23C1
					c++;
					// D164
					if (Dgroup.equals("A1"))
						Dexpandparam[c] = angle(centerpoint, neckpoint,
								dPointParamMap.get("d008"), dPointParamMap.get("d011"));// angD4-1D24C1
					c++;
					// D165
					if (Dgroup.equals("B"))
						Dexpandparam[c] = angle(centerpoint, neckpoint,
								dPointParamMap.get("d010"), dPointParamMap.get("d013"));// angD4-3D25C1
					c++;
					// D166
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = angle(budtop, neckpoint, dPointParamMap.get("d000"),
								dPointParamMap.get("d001"));// angD1-2D18-2C4-2
					c++;
					// D167
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = angle(budtop, neckpoint, dPointParamMap.get("d004"),
								dPointParamMap.get("d005"));// angD2-2D19-2C4-2
					c++;
					// D168
					if (Dgroup.equals("B"))
						Dexpandparam[c] = angle(budtop, neckpoint, dPointParamMap.get("d010"),
								dPointParamMap.get("d013"));// angD3-3D21-2C4-2
					c++;
					// D169
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = angle(centerpoint, neckpoint,
								dPointParamMap.get("d000"));// angD1-1M1C1
					c++;
					// D170
					if (!Dgroup.equals("A"))
						Dexpandparam[c] = angle(centerpoint, neckpoint,
								dPointParamMap.get("d004"));// angD2-1M1C1
					c++;
					// D171
					if (Dgroup.equals("A1"))
						Dexpandparam[c] = angle(centerpoint, neckpoint,
								dPointParamMap.get("d011"));// angD4-1M1C1
					c++;
					// D172
					if (Dgroup.equals("B"))
						Dexpandparam[c] = angle(centerpoint, neckpoint,
								dPointParamMap.get("d013"));// angD4-3M1C1
					c++;
					// D173
					if (!Dgroup.equals("B"))
						Dexpandparam[c] = distance(dPointParamMap.get("d000"), dPointParamMap.get("d008"));// D1-1D3-1
					c++;
					// D174
					if (Dgroup.equals("C"))
						Dexpandparam[c] = distance(dPointParamMap.get("d001"), dPointParamMap.get("d009"));// D1-2D3-2
					c++;
					// D175
					if (!Dgroup.equals("C"))
						Dexpandparam[c] = distance((Point) Dpoint.elementAt(0),
								dPointParamMap.get("d010"));// D1-3D3-3
					c++;
					// D176
					if (!Dgroup.equals("B"))
						Dexpandparam[c] = distance(dPointParamMap.get("d008"), dPointParamMap.get("d011"));// D3-1D4-1
					c++;
					// D177
					if (Dgroup.equals("C"))
						Dexpandparam[c] = distance(dPointParamMap.get("d009"), dPointParamMap.get("d012"));// D3-2D4-2
					c++;
					// D178
					if (!Dgroup.equals("C"))
						Dexpandparam[c] = distance(dPointParamMap.get("d010"), dPointParamMap.get("d013"));// D3-3D4-3
					c++;
					// D179
					if (!Dgroup.equals("B"))
						Dexpandparam[c] = distance(dPointParamMap.get("d000"), dPointParamMap.get("d014"));// D1-1D5-1
					c++;
					// D180
					if (Dgroup.equals("C"))
						Dexpandparam[c] = distance(dPointParamMap.get("d001"), dPointParamMap.get("d015"));// D1-2D5-2
					c++;
					// D181
					if (!Dgroup.equals("C"))
						Dexpandparam[c] = distance((Point) Dpoint.elementAt(0),
								dPointParamMap.get("d016"));// D1-3D5-3
					c++;
					// D182
					if (!Dgroup.equals("B") && dPointParamMap.get("d008").x != -1
							&& dPointParamMap.get("d011").x != -1 && dPointParamMap.get("d014").x != -1)
						Dexpandparam[c] = distance(dPointParamMap.get("d008"), dPointParamMap.get("d011"))
								/ distance(dPointParamMap.get("d000"), dPointParamMap.get("d014"));// D3-1D4-1/D1-1D5-1
					c++;
					// D183
					if (Dgroup.equals("C") && dPointParamMap.get("d009").x != -1
							&& dPointParamMap.get("d012").x != -1 && dPointParamMap.get("d015").x != -1)
						Dexpandparam[c] = distance(dPointParamMap.get("d009"), dPointParamMap.get("d012"))
								/ distance(dPointParamMap.get("d001"), dPointParamMap.get("d015"));// D3-2D4-2/D1-2D5-2
					c++;
					// D184
					if (!Dgroup.equals("C") && dPointParamMap.get("d010").x != -1
							&& dPointParamMap.get("d013").x != -1 && dPointParamMap.get("d016").x != -1)
						Dexpandparam[c] = distance(dPointParamMap.get("d010"), dPointParamMap.get("d013"))
								/ distance((Point) Dpoint.elementAt(0),
								dPointParamMap.get("d016"));// D3-3D4-3/D1-3D5-3
					c++;
					// D185
					if ((Dgroup.equals("B") || Dgroup.equals("C"))
							&& dPointParamMap.get("d025").x != -1 && dPointParamMap.get("d026").x != -1)
						Dexpandparam[c] = distance(dPointParamMap.get("d025"), neckpoint)
								+ distance(dPointParamMap.get("d026"), neckpoint);// D11-1M1D11-2
					c++;
					// D186
					if ((Dgroup.equals("B") || Dgroup.equals("C"))
							&& dPointParamMap.get("d027").x != -1 && dPointParamMap.get("d028").x != -1)
						Dexpandparam[c] = distance(dPointParamMap.get("d027"), neckpoint)
								+ distance(dPointParamMap.get("d028"), neckpoint);// D12-1M1D12-2
					c++;
					// D187
					if (Dgroup.equals("B") && dPointParamMap.get("d029").x != -1
							&& dPointParamMap.get("d030").x != -1)
						Dexpandparam[c] = distance(dPointParamMap.get("d029"), neckpoint)
								+ distance(dPointParamMap.get("d030"), neckpoint);// D13-1M1D13-2
					c++;
					// D188
					Dexpandparam[c] = distance(dPointParamMap.get("d000"), dPointParamMap.get("d004"));// D1-1D2-1
					c++;
					// D189
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = distance(dPointParamMap.get("d001"), dPointParamMap.get("d005"));// D1-2D2-2
					c++;
					// D190
					if (Dgroup.equals("B"))
						Dexpandparam[c] = distance((Point) Dpoint.elementAt(0),
								(Point) Dbrightpoint.elementAt(0));// D1-3D2-3
					else if (Dgroup.equals("A") || Dgroup.equals("A1"))
						Dexpandparam[c] = distance(dPointParamMap.get("d000"), dPointParamMap.get("d004"));
					c++;
					// D191
					Dexpandparam[c] = Dbaseparam[3] / Dbaseparam[0];// D15-1/D14-1
					c++;
					// D192
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = Dbaseparam[4] / Dbaseparam[1];// D15-2/D14-2
					c++;
					// D193
					Dexpandparam[c] = Dbaseparam[5] / Dbaseparam[2];// D15-3/D14-3
					c++;
					// D194
					Dexpandparam[c] = Dbaseparam[6] / Dexpandparam[c - 3];// D16-1/(D15-1/D14-1)
					c++;
					// D195
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = Dbaseparam[7] / Dexpandparam[c - 3];// D16-2/(D15-2/D14-2)
					c++;
					// D196
					Dexpandparam[c] = Dbaseparam[8] / Dexpandparam[c - 3];// D16-3/(D15-3/D14-3)
					c++;
					// D197
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = Dbaseparam[1] / Dbaseparam[0];// D14-2/D14-1
					c++;
					// D198
					if (Dgroup.equals("B") || Dgroup.equals("C"))
						Dexpandparam[c] = Dbaseparam[4] / Dbaseparam[3];// D15-2/D15-1
				}
				// end of dexpandparam

			}
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// DAPI画像の出力
	// ///////////////////////////////////////////////////////////////////////////////
	public void outDImage(Graphics g) {
		g.setColor(Color.blue);
		g.drawString(id + " " + Cgroup, point.x, point.y);
		g.setColor(Color.red);
		g.drawString(Dgroup, point.x, point.y - 15);
		g.setColor(Color.green);
		for (int j = 0; j < edge.size(); j++) {
			int p = ((Integer) edge.get(j)).intValue();
			g.fillRect(p % w, p / w, 1, 1);
		}
		g.setColor(Color.red);
		if (group > 1) {
			for (int j = 0; j < neck.length; j++) {
				g.fillOval(neck[j] % w - 2, neck[j] / w - 2, 4, 4);
			}
		}
		if (group > 1) {
			g.setColor(Color.green);
			g.fillOval(budtop.x - 2, budtop.y - 2, 4, 4);
		}
		g.setColor(Color.red);
		for (int j = 0; j < Dpoint.size(); j++) {
			Vector De = (Vector) (Dedge.get(j));
			for (int k = 0; k < De.size(); k++) {
				int p = ((Integer) De.get(k)).intValue();
				g.fillRect(p % w, p / w, 1, 1);
			}
		}
		g.setColor(Color.blue);
		for (int j = 0; j < Dpoint.size(); j++) {
			Point p = (Point) Dpoint.get(j);
			g.fillOval(p.x - 1, p.y - 1, 3, 3);
		}
		/*
		 * g.setColor(Color.red); for(int j=0;j <Dbrightpoint.size();j++) {
		 * Point p = (Point)Dbrightpoint.get(j); g.fillOval(p.x-1,p.y-1,3,3); }
		 */
		if (flag_ud) {
			g.setColor(Color.green);
			for (int j = 0; j < DpointB.size(); j++) {
				Point p = (Point) DpointB.get(j);
				g.fillOval(p.x - 1, p.y - 1, 3, 3);
			}
			/*
			 * g.setColor(Color.yellow); for(int j=0;j
			 * <DbrightpointB.size();j++) { Point p =
			 * (Point)DbrightpointB.get(j); g.fillOval(p.x-1,p.y-1,3,3); }
			 */
		}
		/*
		 * g.setColor(Color.black); for(int j=0;j <D345point[1].size();j++) {
		 * g.drawLine(((Point)D345point[0].get(j)).x,((Point)D345point[0].get(j)).y,((Point)D345point[1].get(j)).x,((Point)D345point[1].get(j)).y); }
		 * for(int j=0;j <D345point[2].size();j++) {
		 * g.drawLine(((Point)Dpoint.get(j)).x,((Point)Dpoint.get(j)).y,((Point)D345point[2].get(j)).x,((Point)D345point[2].get(j)).y); }
		 */
	}

	// ///////////////////////////////////////////////////////////////////////////////////
	// アクチンに関するデータをセット
	// ///////////////////////////////////////////////////////////////////////////////////
	public void setAState(int diff, int[] AImage) {
		int diffx = diff % w;
		if (diffx >= w / 2)
			diffx -= w;
		else if (diffx <= -w / 2)
			diffx += w;
		if (group >= 2 && !Agroup.equals("N")) {
			double a = neckpoint.y - budtop.y;
			double b = neckpoint.x - budtop.x;
			int motherbright = 0;
			int nearneckbright = 0;
			int inbudnearneckbright = 0;
			int inbudneartipbright = 0;
			int counter_mother = 0;
			int counter_nearneck = 0;
			int counter_inbudnearneck = 0;
			int counter_inbudneartip = 0;
			for (int j = 0; j < cover.size(); j++) {
				int po = ((Integer) cover.get(j)).intValue() - diff;
				if (po >= 0 && po < size && po / w == (po + diffx) / w) {
					int px = po % w;
					int py = po / w;
					double sx = ((neckpoint.x) * 11 - (budtop.x)) / 10;
					double sy = ((neckpoint.y) * 11 - (budtop.y)) / 10;
					int counter = 0;
					if ((a * (py - sy) + b * (px - sx))
							* (a * (budtop.y - sy) + b * (budtop.x - sx)) < 0) {
						counter_mother++;
						motherbright += AImage[po];
					} else {
						sx = ((neckpoint.x) * 9 + (budtop.x)) / 10;
						sy = ((neckpoint.y) * 9 + (budtop.y)) / 10;
						if ((a * (py - sy) + b * (px - sx))
								* (a * (budtop.y - sy) + b * (budtop.x - sx)) < 0) {
							counter_nearneck++;
							nearneckbright += AImage[po];
						}
						sx = neckpoint.x;
						sy = neckpoint.y;
						double tx = ((neckpoint.x) * 6 + (budtop.x) * 4) / 10;
						double ty = ((neckpoint.y) * 6 + (budtop.y) * 4) / 10;
						if ((a * (py - sy) + b * (px - sx))
								* (a * (budtop.y - sy) + b * (budtop.x - sx)) > 0
								&& (a * (py - ty) + b * (px - tx))
								* (a * (budtop.y - ty) + b
								* (budtop.x - tx)) < 0) {
							counter_inbudnearneck++;
							inbudnearneckbright += AImage[po];
						}
						sx = ((neckpoint.x) * 2 + (budtop.x) * 8) / 10;
						sy = ((neckpoint.y) * 2 + (budtop.y) * 8) / 10;
						if ((a * (py - sy) + b * (px - sx))
								* (a * (budtop.y - sy) + b * (budtop.x - sx)) < 0
								&& (a * (py - ty) + b * (px - tx))
								* (a * (budtop.y - ty) + b
								* (budtop.x - tx)) > 0) {
							counter_inbudneartip++;
							inbudneartipbright += AImage[po];
						}
					}
				}
			}
			if (counter_mother != 0)
				motherbright /= counter_mother;
			if (counter_nearneck != 0)
				nearneckbright /= counter_nearneck;
			if (counter_inbudnearneck != 0)
				inbudnearneckbright /= counter_inbudnearneck;
			if (counter_inbudneartip != 0)
				inbudneartipbright /= counter_inbudneartip;
			if (inbudneartipbright > motherbright + 60
					&& inbudneartipbright > inbudnearneckbright + 40
					&& inbudneartipbright > nearneckbright + 40)
				Agroup = "api";
			else if (group != 2 && inbudneartipbright > motherbright + 40
					&& inbudnearneckbright > motherbright + 40)
				Agroup = "iso";
			else if (group != 2 && nearneckbright > motherbright + 30
					&& nearneckbright > inbudneartipbright + 50)
				Agroup = "F";
			else if (motherbright > inbudneartipbright
					|| motherbright > inbudnearneckbright)
				Agroup = "E";
			else
				Agroup = "X";

			if (group == 2 && Agroup.equals("X")) {// smallだったらapiにかえてみる
				Agroup = "api";
			}

			if (group >= 2 && !Agroup.equals("N")) {

				int buds = 0;
				int api = 0;
				int iso = 0;
				int fcount = 0;
				int count = 0;
				double sx = ((budtop.x) * 3 + neckpoint.x) / 4;
				double sy = ((budtop.y) * 3 + neckpoint.y) / 4;
				double bx = (neckpoint.x * 2 + budtop.x) / 3;
				double by = (neckpoint.y * 2 + budtop.y) / 3;
				double mx = (neckpoint.x * 4 - (budtop.x)) / 3;
				double my = (neckpoint.y * 4 - (budtop.y)) / 3;
				for (int i = 0; i < actinpatchpoint.size(); i++) {
					int po = ((Integer) actinpatchpoint.elementAt(i))
							.intValue();
					int px = po % w;
					int py = po / w;
					int br = ((Integer) actinpatchbright.elementAt(i))
							.intValue()
							* ((Integer) actinpatchbright.elementAt(i))
							.intValue();
					if (!inmother(po)) {
						buds += br;
						if ((a * (py - sy) + b * (px - sx))
								* (a * (budtop.y - sy) + b * (budtop.x - sx)) > 0)
							api += br;// 先のほうにパッチがあるとき
						else
							iso += br;
					}
					if ((a * (py - by) + b * (px - bx))
							* (a * (neckpoint.y - by) + b * (neckpoint.x - bx)) > 0
							&& (a * (py - my) + b * (px - mx))
							* (a * (neckpoint.y - my) + b
							* (neckpoint.x - mx)) > 0)
						fcount += br;// 中心にあるパッチの重み
					count += br;
				}
				if (Agroup.equals("X")) {
					if (count > 0) {
						if ((double) fcount / count > 0.6)
							Agroup = "F";
						else if (buds > (count - buds) * 2.0) {
							if (api > iso)
								Agroup = "api";
							else
								Agroup = "iso";
						} else
							Agroup = "E";
					} else {
						Agroup = "E";
					}
				}
			}
		}

		farfromneckApoint = new Point[3];
		for (int i = 0; i < 3; i++)
			farfromneckApoint[i] = new Point(-1, -1);
		if (Agroup != "N" && group > 1) {// 母細胞側、芽側、および細胞全体でネックの中点から最も遠いアクチン領域上の点を求める
			double max = 0;
			int maxpoint = -1;
			double mmax = 0;
			int mmaxpoint = -1;
			double bmax = 0;
			int bmaxpoint = -1;
			for (int i = 0; i < Acover.size(); i++) {
				int p = ((Integer) Acover.get(i)).intValue();
				if (distance(p, neckpoint.y * w + neckpoint.x) > max) {
					maxpoint = p;
					max = distance(p, neckpoint.y * w + neckpoint.x);
				}
				if (inmother(p)
						&& distance(p, neckpoint.y * w + neckpoint.x) > mmax) {
					mmaxpoint = p;
					mmax = distance(p, neckpoint.y * w + neckpoint.x);
				} else if (!inmother(p)
						&& distance(p, neckpoint.y * w + neckpoint.x) > bmax) {
					bmaxpoint = p;
					bmax = distance(p, neckpoint.y * w + neckpoint.x);
				}
			}
			if (mmaxpoint != -1)
				farfromneckApoint[0] = new Point(mmaxpoint % w, mmaxpoint / w);
			if (bmaxpoint != -1)
				farfromneckApoint[1] = new Point(bmaxpoint % w, bmaxpoint / w);
			if (maxpoint != -1)
				farfromneckApoint[2] = new Point(maxpoint % w, maxpoint / w);
		}

		if (Agroup != "N" && group > 0) {

			// 最も遠いアクチンパッチ同士の距離（ただし、母細胞と芽の両方にパッチがある細胞については、ネックの中点を通した距離）を求める
			boolean check1 = false;
			boolean check2 = false;
			if (group > 1) {
				for (int i = 0; i < actinpatchpoint.size(); i++) {
					if (inmother(((Integer) actinpatchpoint.get(i)).intValue()))
						check1 = true;
					else
						check2 = true;
				}
			}
			if (check1 == false || check2 == false) {
				double max = 0;
				for (int i = 0; i < actinpatchpoint.size(); i++) {
					int p1 = ((Integer) actinpatchpoint.get(i)).intValue();
					for (int j = i + 1; j < actinpatchpoint.size(); j++) {
						int p2 = ((Integer) actinpatchpoint.get(j)).intValue();
						if (distance(p1, p2) > max)
							max = distance(p1, p2);
					}
				}
				maxpatchdistance = max;
			} else {
				double mmax = 0;
				double bmax = 0;
				for (int i = 0; i < actinpatchpoint.size(); i++) {
					int p = ((Integer) actinpatchpoint.get(i)).intValue();
					if (inmother(p) && distance(p, neckpoint) > mmax)
						mmax = distance(p, neckpoint);
					else if (!inmother(p) && distance(p, neckpoint) > bmax)
						bmax = distance(p, neckpoint);
				}
				maxpatchdistance = mmax + bmax;
			}

			actinpatchorder = new int[actinpatchpoint.size()];// 細胞中のアクチンパッチを輝度が高い順に並べる
			int max = 0;
			int min = 255;
			for (int i = 0; i < actinpatchpoint.size(); i++) {
				int br = ((Integer) actinpatchbright.get(i)).intValue();
				actinpatchorder[i] = br * 100 + i;// 1、10の位がパッチの番号、100以上の位が輝度を表すようにセット
				if (br > max)
					max = br;
				if (br < min)
					min = br;
			}
			Arrays.sort(actinpatchorder);// （実質的に）輝度でソート
			int copy[] = (int[]) (actinpatchorder.clone());
			for (int i = 0; i < actinpatchpoint.size(); i++) {
				actinpatchorder[i] = copy[actinpatchpoint.size() - i - 1] % 100;// ソート後、もとのパッチの番号にもどしてしまい直し
			}

			brightpatchnumber = 0;
			for (int i = 0; i < actinpatchorder.length; i++) {// 輝度が、その細胞のアクチンパッチの平均輝度以上であるアクチンパッチの数
				if (((Integer) actinpatchbright.get(actinpatchorder[i]))
						.intValue() > (max + min) / 2)
					brightpatchnumber++;
			}
		}

		if (Agroup != "N" && group > 1) {// 母細胞と芽のアクチンの偏りを求める
			double distbr = 0;
			double total = 0;
			for (int j = 0; j < Acover.size(); j++) {
				int pp = ((Integer) Acover.get(j)).intValue();
				if (pp - diff >= 0 && pp - diff < size
						&& pp / w == (pp - diffx) / w) {
					Point p = new Point(pp % w, pp / w);
					if (!p.equals(neckpoint)) {
						if (inmother(p))
							distbr -= Math.pow(AImage[pp - diff], 4)
									* distance(p, neckpoint)
									* Math.cos(angle(p, neckpoint,
									farfromneckpoint)
									* Math.PI / 180.0);
						else
							distbr += Math.pow(AImage[pp - diff], 4)
									* distance(p, neckpoint)
									* Math.cos(angle(p, neckpoint, budtop)
									* Math.PI / 180.0);
					}
					total += Math.pow(AImage[pp - diff], 4);
				}
			}
			if (distbr > 0) {
				budactincenterposition = distbr
						/ total
						/ (distance(neckpoint, budtop) + distance(neckpoint,
						farfromneckpoint));
				motheractincenterposition = -1;
			} else if (distbr < 0) {
				motheractincenterposition = -distbr
						/ total
						/ (distance(neckpoint, budtop) + distance(neckpoint,
						farfromneckpoint));
				budactincenterposition = -1;
			} else {
				budactincenterposition = 0;
				motheractincenterposition = 0;
			}
		}

		if (Agroup != "N" && group > 0) {
			int c = 0;
			// c = 0, A2-1
			Apointparam[c] = Acenterpoint[0][0];// アクチン領域の重心（母）
			c++;
			// c = 1, A2-2
			if (group > 1)
				Apointparam[c] = Acenterpoint[1][0];// アクチン領域の重心（娘）
			c++;
			// c = 2, A2-3
			Apointparam[c] = Acenterpoint[2][0];// アクチン領域の重心（全体）
			c++;
			// c = 3, A3-1
			Apointparam[c] = Apatchcenterpoint[0][0];// アクチンパッチの重心（母）
			c++;
			// c = 4, A3-2
			if (group > 1)
				Apointparam[c] = Apatchcenterpoint[1][0];// アクチンパッチの重心（娘）
			c++;
			// c = 5, A3-3
			Apointparam[c] = Apatchcenterpoint[2][0];// アクチンパッチの重心（全体）
			c++;
			// c = 6, A4-1
			Apointparam[c] = Acenterpoint[0][3];// 輝度4乗で重み付けしたアクチン領域の重心（母）
			c++;
			// c = 7, A4-2
			if (group > 1)
				Apointparam[c] = Acenterpoint[1][3];// 輝度4乗で重み付けしたアクチン領域の重心（娘）
			c++;
			// c = 8, A4-3
			Apointparam[c] = Acenterpoint[2][3];// 輝度4乗で重み付けしたアクチン領域の重心（全体）
			c++;
			// c = 9, A5-1
			Apointparam[c] = Apatchcenterpoint[0][3];// 輝度4乗で重み付けしたアクチンパッチの重心（母）
			c++;
			// c = 10, A5-2
			if (group > 1)
				Apointparam[c] = Apatchcenterpoint[1][3];// 輝度4乗で重み付けしたアクチンパッチの重心（娘）
			c++;
			// c = 11, A5-3
			Apointparam[c] = Apatchcenterpoint[2][3];// 輝度4乗で重み付けしたアクチンパッチの重心（全体）
			c++;
			// c = 12, A6-1
			if (group > 1)
				Apointparam[c] = farfromneckApoint[0];// ネックの中点から最も距離の遠いアクチン領域の点（母）
			c++;
			// c = 13, A6-2
			if (group > 1)
				Apointparam[c] = farfromneckApoint[1];// ネックの中点から最も距離の遠いアクチン領域の点（娘）
			c++;
			// c = 14, A6-3
			if (group > 1)
				Apointparam[c] = farfromneckApoint[2];// ネックの中点から最も距離の遠いアクチン領域の点（全体）

			c = 0;
			// c = 0, A7-1
			Abaseparam[c] = Aregionsize[0];// アクチン領域の大きさ（母）
			c++;
			// c = 1, A7-2
			if (group > 1)
				Abaseparam[c] = Aregionsize[1];// アクチン領域の大きさ（娘）
			c++;
			// c = 2, A8-1
			Abaseparam[c] = Atotalbright[0];// アクチン輝度の合計（母）
			c++;
			// c = 3, A8-2
			if (group > 1)
				Abaseparam[c] = Atotalbright[1];// アクチン輝度の合計（娘）
			c++;
			// c = 4, A9
			if (group > 1)
				Abaseparam[c] = actinonneckline;// ネックにおけるアクチン領域の占める割合

			c = 0;
			// A101
			Aexpandparam[c] = (double) Acover.size() / (double) cover.size();// アクチン領域の細胞に対する割合
			c++;
			// A102
			if (group > 1)
				Aexpandparam[c] = (double) Aregionsize[1]
						/ (double) Acover.size();// アクチン領域のうち芽に閉めるアクチン領域の割合
			c++;
			// A103
			if (group > 1)
				Aexpandparam[c] = motheractincenterposition;// アクチンの偏り(母細胞側）
			c++;
			// A104
			if (group > 1)
				Aexpandparam[c] = budactincenterposition;// アクチンの偏り（芽側）
			c++;
			// A120
			Aexpandparam[c] = actinpathlength;// アクチンパッチの総延長
			c++;
			// A121
			Aexpandparam[c] = maxpatchdistance;// アクチンパッチの最大距離
			c++;
			// A122
			Aexpandparam[c] = brightpatchnumber;
			c++;
			// A123
			Aexpandparam[c] = (double) totalpatchsize / (double) Acover.size();// アクチンパッチのアクチン領域に対する割合

		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////
	// actin画像出力
	// ///////////////////////////////////////////////////////////////////////////////////
	public void outAImage(Graphics g) {
		g.setColor(Color.blue);
		g.drawString(id + " " + Cgroup, point.x, point.y);
		g.setColor(Color.red);
		g.drawString(Agroup, point.x, point.y - 15);
		g.setColor(Color.green);
		for (int j = 0; j < edge.size(); j++) {
			int p = ((Integer) edge.get(j)).intValue();
			g.fillRect(p % w, p / w, 1, 1);
		}
		if (group > 1) {
			g.setColor(Color.red);
			for (int j = 0; j < 2; j++) {
				g.fillOval(neck[j] % w - 2, neck[j] / w - 2, 4, 4);
			}
		}
		if (group > 1) {
			g.setColor(Color.green);
			g.fillOval(budtop.x - 2, budtop.y - 2, 4, 4);
		}

		if (group > 1) {
			double sx = ((budtop.x) * 3 + neckpoint.x) / 4;
			double sy = ((budtop.y) * 3 + neckpoint.y) / 4;
			double bx = (neckpoint.x * 2 + budtop.x) / 3;
			double by = (neckpoint.y * 2 + budtop.y) / 3;
			double mx = (neckpoint.x * 4 - (budtop.x)) / 3;
			double my = (neckpoint.y * 4 - (budtop.y)) / 3;
			double a = neckpoint.y - budtop.y;
			double b = neckpoint.x - budtop.x;
			for (int j = 0; j < actinpatchpoint.size(); j++) {
				int p = ((Integer) actinpatchpoint.get(j)).intValue();
				int px = p % w;
				int py = p / w;
				if (!inmother(p)
						&& (a * (py - sy) + b * (px - sx))
						* (a * (budtop.y - sy) + b * (budtop.x - sx)) > 0)
					g.setColor(Color.red);// 先のほうにパッチがあるとき
				else if ((a * (py - by) + b * (px - bx))
						* (a * (neckpoint.y - by) + b * (neckpoint.x - bx)) > 0
						&& (a * (py - my) + b * (px - mx))
						* (a * (neckpoint.y - my) + b
						* (neckpoint.x - mx)) > 0)
					g.setColor(Color.yellow);// 中心にあるパッチの重み
				else if (!inmother(p))
					g.setColor(Color.green);
				else
					g.setColor(Color.blue);
				g.fillOval(p % w - 2, p / w - 2, 4, 4);
			}
		} else {
			g.setColor(Color.blue);
			for (int j = 0; j < actinpatchpoint.size(); j++) {
				int p = ((Integer) actinpatchpoint.get(j)).intValue();
				g.fillOval(p % w - 2, p / w - 2, 4, 4);
			}
		}
		/*
		 * g.setColor(Color.white); if(group>0 && Agroup!="N"){ for(int i=0;i
		 * <Acover.size();i++){ int p = ((Integer)Acover.get(i)).intValue();
		 * g.fillRect(p%w,p/w,1,1); } }
		 */
		/*
		 * if(group>0 && Agroup!="N") { g.setColor(Color.blue);
		 * g.fillOval(Acenterpoint[2][0].x-2,Acenterpoint[2][0].y-2,4,4);
		 * g.setColor(Color.green);
		 * g.fillOval(Acenterpoint[2][1].x-2,Acenterpoint[2][1].y-2,4,4);
		 * g.setColor(Color.red);
		 * g.fillOval(Acenterpoint[2][2].x-2,Acenterpoint[2][2].y-2,4,4);
		 * g.setColor(Color.yellow);
		 * g.fillOval(Acenterpoint[2][3].x-2,Acenterpoint[2][3].y-2,4,4);
		 * g.setColor(Color.black);
		 * g.fillOval(Acenterpoint[2][4].x-2,Acenterpoint[2][4].y-2,4,4); }
		 */

		/*
		 * g.setColor(Color.red); for(int i=0;i <actinpatchpath.size();i++){
		 * Point p = (Point)actinpatchpath.get(i); Point q =
		 * (Point)actinpatchpath.get((i+1)%actinpatchpath.size());
		 * g.drawLine(p.x,p.y,q.x,q.y); }
		 */
	}

	// ///////////////////////////////////////////////////////////////////////////////////
	// pがネックラインの母細胞側ならtrue、芽側ならfalse
	// ネックラインがなければtrue
	// ///////////////////////////////////////////////////////////////////////////////////
	public boolean inmother(int p) {
		if (group > 1) {
			int dx = neck[1] % w - neck[0] % w;
			int dy = neck[1] / w - neck[0] / w;
			if ((dx * (p / w) - dy * (p % w) + dy * (neck[0] % w) - dx
					* (neck[0] / w))
					* (dx * budtop.y - dy * budtop.x + dy * (neck[0] % w) - dx
					* (neck[0] / w)) > 0)
				return false;
			else
				return true;
		} else {
			return true;
		}
	}

	public boolean inmother(Point p) {
		if (group > 1) {
			int dx = neck[1] % w - neck[0] % w;
			int dy = neck[1] / w - neck[0] / w;
			if ((dx * (p.y) - dy * (p.x) + dy * (neck[0] % w) - dx
					* (neck[0] / w))
					* (dx * budtop.y - dy * budtop.x + dy * (neck[0] % w) - dx
					* (neck[0] / w)) > 0)
				return false;
			else
				return true;
		} else {
			return true;
		}
	}

	// ////////////////////////////////////////////////////////////////////////
	// 点集合を楕円近似したときのfitnessを求める
	// ////////////////////////////////////////////////////////////////////////
	public double getfitness(Vector ed) {
		double[] ellipse;
		double[][] D;
		double[][] S = new double[6][6];
		double[][] C = { { 0, 0, -2, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0 },
				{ -2, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 } };
		int es = ed.size();
		D = new double[es][6];
		for (int i = 0; i < es; i++) {
			int p = ((Integer) ed.get(i)).intValue();
			int x = p % w;
			int y = p / w;
			D[i][0] = x * x;
			D[i][1] = x * y;
			D[i][2] = y * y;
			D[i][3] = x;
			D[i][4] = y;
			D[i][5] = 1;
		}
		S = MatOp.BtB(D, D.length);
		ellipse = MatOp.generalEigenVector(C, S);
		if (ellipse == null) {// 楕円があたらなかったら
			return 1.0;
		} else {
			double k = Math.sqrt(-1
					/ (ellipse[1] * ellipse[1] - 4 * ellipse[0] * ellipse[2]));
			for (int i = 0; i < 6; i++) {
				ellipse[i] *= k;
			}
		}

		double a = ellipse[0] / (-ellipse[5]);
		double b = ellipse[1] / (-ellipse[5]);
		double c = ellipse[2] / (-ellipse[5]);
		double d = ellipse[3] / (-ellipse[5]);
		double e = ellipse[4] / (-ellipse[5]);
		double p = (-2 * c * d + b * e) / (4 * a * c - b * b);
		double q = (b * d - 2 * a * e) / (4 * a * c - b * b);
		double f = -a * p * p - b * p * q - c * q * q - d * p - e * q + 1;
		double sin2theta = b / Math.sqrt((a - c) * (a - c) + b * b);
		double cos2theta = (c - a) / Math.sqrt((a - c) * (a - c) + b * b);
		double sintheta = 0;
		if (b > 0)
			sintheta = Math.sqrt((1 - cos2theta) / 2);
		else
			sintheta = -Math.sqrt((1 - cos2theta) / 2);
		double costheta = Math.sqrt((1 + cos2theta) / 2);
		double aa = Math.sqrt(2 * f
				/ (a + c - Math.sqrt(b * b + (a - c) * (a - c))));
		double bb = Math.sqrt(2 * f
				/ (a + c + Math.sqrt(b * b + (a - c) * (a - c))));
		Point p1 = new Point((int) (p + aa * costheta - point.x), (int) (q - aa
				* sintheta - point.y));
		Point p2 = new Point((int) (p - aa * costheta - point.x), (int) (q + aa
				* sintheta - point.y));
		Point p3 = new Point((int) (p - bb * sintheta - point.x), (int) (q - bb
				* costheta - point.y));
		Point p4 = new Point((int) (p + bb * sintheta - point.x), (int) (q + bb
				* costheta - point.y));
		double ax = p1.distance(p2) * p3.distance(p4);
		double fit = 0;
		for (int i = 0; i < ed.size(); i++) {
			int po = ((Integer) ed.elementAt(i)).intValue();
			int x = po % w;
			int y = po / w;
			fit += (ellipse[0] * x * x + ellipse[1] * x * y + ellipse[2] * y
					* y + ellipse[3] * x + ellipse[4] * y + ellipse[5])
					* (ellipse[0] * x * x + ellipse[1] * x * y + ellipse[2] * y
					* y + ellipse[3] * x + ellipse[4] * y + ellipse[5]);
		}
		fit /= ed.size();
		fit /= Math.PI * ax / 4.0;
		fit /= Math.PI * ax / 4.0;
		return fit;
	}

	// ////////////////////////////////////////////////////////////////////////
	// 二点を結ぶ直線とあるedgeの交点三つ（一つ目の先、二つ目の先、間）を求める
	// ////////////////////////////////////////////////////////////////////////
	public Point[] Intersection(Point p1, Point p2, Vector ed) {
		Point pp1 = new Point(-1, -1);
		Point pp2 = new Point(-1, -1);
		Point pp3 = new Point(-1, -1);
		double mindist1 = 2000;
		double mindist2 = 2000;
		double mindist3 = 2000;
		if (p1.x != p2.x) {
			for (int i = 0; i < ed.size(); i++) {
				int p = ((Integer) ed.get(i)).intValue();
				double dist = Line2D.ptLineDist((double) (p1.x),
						(double) (p1.y), (double) (p2.x), (double) (p2.y),
						(double) (p % w), (double) (p / w));
				if (((p1.x > p2.x && p % w > p1.x) || (p1.x < p2.x && p % w < p1.x))
						&& dist < Math.min(1, mindist1)) {
					pp1 = new Point(p % w, p / w);
					mindist1 = dist;
				} else if (((p1.x > p2.x && p % w < p2.x) || (p1.x < p2.x && p
						% w > p2.x))
						&& dist < Math.min(1, mindist2)) {
					pp2 = new Point(p % w, p / w);
					mindist2 = dist;
				} else if (dist < Math.min(1, mindist3)) {
					pp3 = new Point(p % w, p / w);
					mindist3 = dist;
				}
			}
		} else if (p1.y != p2.y) {
			for (int i = 0; i < ed.size(); i++) {
				int p = ((Integer) ed.get(i)).intValue();
				double dist = Line2D.ptLineDist((double) (p1.x),
						(double) (p1.y), (double) (p2.x), (double) (p2.y),
						(double) (p % w), (double) (p / w));
				if (((p1.y > p2.y && p / w > p1.y) || (p1.y < p2.y && p / w < p1.y))
						&& dist < Math.min(1, mindist1)) {
					pp1 = new Point(p % w, p / w);
					mindist1 = dist;
				} else if (((p1.y > p2.y && p / w < p2.y) || (p1.y < p2.y && p
						/ w > p2.y))
						&& dist < Math.min(1, mindist2)) {
					pp2 = new Point(p % w, p / w);
					mindist2 = dist;
				} else if (dist < Math.min(1, mindist3)) {
					pp3 = new Point(p % w, p / w);
					mindist3 = dist;
				}
			}
		}
		Point[] ret = new Point[3];
		ret[0] = pp1;
		ret[1] = pp2;
		ret[2] = pp3;
		return ret;
	}

	// ////////////////////////////////////////////////////////////////////////
	// 二点を結ぶ直線と別の二点を結ぶ直線との交点を求める
	// ////////////////////////////////////////////////////////////////////////
	public Point Intersection2(Point p1, Point p2, Point p3, Point p4) {
		if ((p4.y - p3.y) * (p2.x - p1.x) - (p2.y - p1.y) * (p4.x - p3.x) != 0) {
			double x = (double) (p1.y * (p4.x - p3.x) * (p2.x - p1.x)
					- (p2.y - p1.y) * (p4.x - p3.x) * p1.x - p3.y
					* (p2.x - p1.x) * (p4.x - p3.x) + (p4.y - p3.y)
					* (p2.x - p1.x) * p3.x)
					/ (double) ((p4.y - p3.y) * (p2.x - p1.x) - (p2.y - p1.y)
					* (p4.x - p3.x));
			double y;
			if (p2.x != p1.x)
				y = (double) p1.y + (double) ((p2.y - p1.y) * (x - p1.x))
						/ (double) (p2.x - p1.x);
			else
				y = (double) p3.y + (double) ((p4.y - p3.y) * (x - p3.x))
						/ (double) (p4.x - p3.x);
			return new Point((int) (x + 0.5), (int) (y + 0.5));
		} else
			return new Point(-1, -1);
	}

	// ////////////////////////////////////////////////////////////////////////
	// 三点のなす角度を求める（90度以内）
	// ////////////////////////////////////////////////////////////////////////
	public double angle(Point p1, Point p2, Point p3) {
		double theta1 = -1;
		if (p1.x != p2.x)
			theta1 = Math.atan((p1.y - p2.y) / (double) (p1.x - p2.x)) * 180
					/ Math.PI;
		else if (p1.y != p2.y)
			theta1 = 90.0;
		double theta2 = -1;
		if (p3.x != p2.x)
			theta2 = Math.atan((p3.y - p2.y) / (double) (p3.x - p2.x)) * 180
					/ Math.PI;
		else if (p3.y != p2.y)
			theta2 = 90.0;
		if (theta1 != -1 && theta2 != -1)
			return theta90(theta1 - theta2);
		else
			return -1;
	}

	// ////////////////////////////////////////////////////////////////////////
	// p1p2とp3p4のなす角度を求める（90度以内）
	// ////////////////////////////////////////////////////////////////////////
	public double angle(Point p1, Point p2, Point p3, Point p4) {
		double theta1 = -1;
		if (p1.x != p2.x)
			theta1 = Math.atan((p1.y - p2.y) / (double) (p1.x - p2.x)) * 180
					/ Math.PI;
		else if (p1.y != p2.y)
			theta1 = 90.0;
		double theta2 = -1;
		if (p3.x != p4.x)
			theta2 = Math.atan((p3.y - p4.y) / (double) (p3.x - p4.x)) * 180
					/ Math.PI;
		else if (p3.y != p4.y)
			theta2 = 90.0;
		if (theta1 != -1 && theta2 != -1)
			return theta90(theta1 - theta2);
		else
			return -1;
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// 90度以内であらわす
	// ///////////////////////////////////////////////////////////////////////////////
	public double theta90(double th) {
		double r = th;
		if (r < 0) {
			while (r < 0) {
				r += 180;
			}
		} else if (r > 180) {
			while (r > 180) {
				r -= 180;
			}
		}
		if (r > 90) {
			r = 180 - r;
		}
		return r;
	}

	public double distance(int a, int b) {
		if (a >= 0 && b >= 0)
			return Point2D.distance(a % w, a / w, b % w, b / w);
		else
			return -1;
	}

	public double distance(Point a, Point b) {
		if (a.x >= 0 && b.x >= 0)
			return Point2D.distance(a.x, a.y, b.x, b.y);
		else
			return -1;
	}

	public double distance(int a, Point b) {
		if (a >= 0 && b.x >= 0)
			return Point2D.distance(a % w, a / w, b.x, b.y);
		else
			return -1;
	}

	// ////////////////////////////////////////////////////////////////////////
	// 輪郭の長さを返す。
	// ////////////////////////////////////////////////////////////////////////
	public double edgeSize(Vector v) {
		double r = 0;
		for (int i = 0; i < v.size() - 1; i++) {
			int p1 = ((Integer) v.elementAt(i)).intValue();
			int p2 = ((Integer) v.elementAt(i + 1)).intValue();
			if (p1 - p2 == 1 || p1 - p2 == -1 || p1 - p2 == w || p1 - p2 == -w)
				r += 1.0;
			if (p1 - p2 == w + 1 || p1 - p2 == w - 1 || p1 - p2 == -w + 1
					|| p1 - p2 == -w - 1)
				r += 1.41421356;
		}
		if (v.size() == 0) {
			return 0;
		}
		int p1 = ((Integer) v.elementAt(v.size() - 1)).intValue();
		int p2 = ((Integer) v.elementAt(0)).intValue();
		if (p1 - p2 == 1 || p1 - p2 == -1 || p1 - p2 == w || p1 - p2 == -w)
			r += 1.0;
		if (p1 - p2 == w + 1 || p1 - p2 == w - 1 || p1 - p2 == -w + 1
				|| p1 - p2 == -w - 1)
			r += 1.41421356;
		return r;
	}

	// ////////////////////////////////////////////////////////////////////////
	// xlsファイルを出力
	// ////////////////////////////////////////////////////////////////////////
	public void writeXLSBaseC(PrintWriter pw, int num) {
		pw.print(num + "\t");
		pw.print(id + "\t");
		pw.print(Cgroup + "\t");
		// C1-1 .. C5-2
		for (int i = 0; i < 10; i++) {
			pw.print("[" + Cpointparam[i].x + "," + Cpointparam[i].y + "]\t");
		}
		// C6 .. C9
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < Cpointsparam[i].size(); j++) {
				Point p = (Point) Cpointsparam[i].elementAt(j);
				pw.print("[" + p.x + "," + p.y + "]&");
			}
			pw.print("\t");
		}
		// C10
		pw.print("[" + Cpointparam[10].x + "," + Cpointparam[10].y + "]\t");
		for (int i = 0; i < 5; i++) {
			pw.print(Cbaseparam[i] + "\t");
		}
		pw.println();
	}

	public void writeXLSExpandC(PrintWriter pw, int num) {
		pw.print(num + "\t");
		pw.print(id + "\t");
		pw.print(Cgroup + "\t");
		for (int i = 0; i < 21; i++) {
			pw.print(Cexpandparam[i] + "\t");
		}
		pw.println();
	}

	public void writeXLSBaseD(PrintWriter pw, int num) {
		pw.print(num + "\t");
		pw.print(id + "\t");
		pw.print(Cgroup + "\t");
		pw.print(Dgroup + "\t");
		// D1-1 .. D13-2
		String[] dPointParamIds = ParameterStatus.dapiPointParamIds;
		for(int i = 0; i < dPointParamIds.length; i++ ) {
			Point p = ParameterStatus.missingValuePoint;
			if(dPointParamMap.get(dPointParamIds[i]) != null )
				p = dPointParamMap.get(dPointParamIds[i]);
			pw.print("[" + p.x + "," + p.y + "]\t");
		}
		// D14-1 .. D17-3
		for (int i = 0; i < 12; i++) {
			pw.print(Dbaseparam[i] + "\t");
		}
		pw.println();
	}

	public void writeXLSExpandD(PrintWriter pw, int num) {
		//image_number
		pw.print(num + "\t");
		//cell_id
		pw.print(id + "\t");
		//Cgroup
		pw.print(Cgroup + "\t");
		//Dgroup
		pw.print(Dgroup + "\t");
		//D101..D198
		for (int i = 0; i < 98; i++) {
			pw.print(Dexpandparam[i] + "\t");
		}
		pw.println();
	}

	public void writeXLSBaseA(PrintWriter pw, int num) {
		pw.print(num + "\t");
		pw.print(id + "\t");
		pw.print(Cgroup + "\t");
		pw.print(Agroup + "\t");
		// A2-1 ..A6-3
		for (int i = 0; i < 15; i++)
			pw.print("[" + Apointparam[i].x + "," + Apointparam[i].y + "]\t");

		// A7-1, A7-2, A8-1, A8-2, A9
		for (int i = 0; i < 5; i++)
			pw.print(Abaseparam[i] + "\t");
		pw.println();
	}

	public void writeXLSPatchA(PrintWriter pw, int num) {
		if (group > 0 && !Agroup.equals("N")) {
			for (int i = 0; i < actinpatchorder.length; i++) {
				pw.print(num + "\t");
				pw.print(id + "\t");
				pw.print("["
						+ ((Integer) actinpatchpoint.get(actinpatchorder[i]))
						.intValue()
						% w
						+ ","
						+ ((Integer) actinpatchpoint.get(actinpatchorder[i]))
						.intValue() / w + "]\t");
				pw.print(((Integer) actinpatchsize.get(actinpatchorder[i]))
						.intValue()
						+ "\t");
				pw.println(((Integer) actinpatchbright.get(actinpatchorder[i]))
						.intValue()
						+ "\t");
			}
		}
	}

	public void writeXLSExpandA(PrintWriter pw, int num) {
		pw.print(num + "\t");
		pw.print(id + "\t");
		pw.print(Cgroup + "\t");
		pw.print(Agroup + "\t");
		for (int i = 0; i < 8; i++) {
			pw.print(Aexpandparam[i] + "\t");
		}
		pw.println();
	}

	public void writeXLSVers(PrintWriter pw, int num, boolean calA, boolean calD) {
		versparam = new double[26];
		versparam[0] = Cbaseparam[0];
		versparam[1] = Cbaseparam[1];
		versparam[2] = Cexpandparam[17];
		versparam[3] = Cexpandparam[14];
		versparam[4] = Cexpandparam[13];
		versparam[5] = Cexpandparam[5];
		versparam[6] = Cexpandparam[4];
		versparam[7] = Cexpandparam[8];
		versparam[8] = Aexpandparam[0];
		versparam[9] = Aexpandparam[1];
		versparam[10] = Aexpandparam[2];
		versparam[11] = Aexpandparam[3];
		versparam[12] = Dexpandparam[0];
		if (Dgroup.equals("B"))
			versparam[13] = -1;
		else
			versparam[13] = Dbaseparam[0];
		if (Dgroup.equals("B"))
			versparam[14] = -1;
		else
			versparam[14] = Dbaseparam[1];
		versparam[15] = Dbaseparam[2];
		if (Dgroup.equals("A") || Dgroup.equals("A1") || Dgroup.equals("C"))
			versparam[16] = Dexpandparam[81];
		else
			versparam[16] = -1;
		if (Dgroup.equals("C"))
			versparam[17] = Dexpandparam[82];
		else
			versparam[17] = -1;
		if (Dgroup.equals("A") || Dgroup.equals("A1") || Dgroup.equals("B"))
			versparam[18] = Dexpandparam[83];
		else
			versparam[18] = -1;
		if (Dgroup.equals("A"))
			versparam[19] = Dexpandparam[4];
		else if (Dgroup.equals("A1") || Dgroup.equals("C"))
			versparam[19] = Dexpandparam[5];
		else if (Dgroup.equals("B"))
			versparam[19] = Dexpandparam[6];
		else
			versparam[19] = -1;
		if (Dgroup.equals("A") || Dgroup.equals("A1") || Dgroup.equals("C"))
			versparam[20] = Dexpandparam[46];
		else
			versparam[20] = -1;
		if (Dgroup.equals("A1") || Dgroup.equals("C"))
			versparam[21] = Dexpandparam[11];
		else if (Dgroup.equals("B"))
			versparam[21] = Dexpandparam[13];
		else
			versparam[21] = -1;
		if (Dgroup.equals("B"))
			versparam[22] = Dexpandparam[23];
		else if (Dgroup.equals("C"))
			versparam[22] = Dexpandparam[22];
		else
			versparam[22] = -1;
		if (Dgroup.equals("C"))
			versparam[23] = Dexpandparam[48];
		else
			versparam[23] = -1;
		if (Dgroup.equals("B"))
			versparam[24] = Dexpandparam[14];
		else if (Dgroup.equals("C"))
			versparam[24] = Dexpandparam[12];
		else
			versparam[24] = -1;
		if (Dgroup.equals("C"))
			versparam[25] = Dexpandparam[15];
		else
			versparam[25] = -1;

		pw.print(num + "\t");
		pw.print(id + "\t");
		pw.print(Cgroup + "\t");
		for (int i = 0; i < 8; i++)
			pw.print(versparam[i] + "\t");
		pw.print(Agroup + "\t");
		for (int i = 8; i < 12; i++)
			pw.print(versparam[i] + "\t");
		pw.print(Dgroup + "\t");
		for (int i = 12; i < 26; i++)
			pw.print(versparam[i] + "\t");
		pw.print(point.x + "\t" + point.y + "\t" + bottomrightPoint.x + "\t"
				+ bottomrightPoint.y);
		pw.println();
	}

	// ////////////////////////////////////////////////////////////////////////
	// 画像データのXMLファイルを出力
	// ////////////////////////////////////////////////////////////////////////
	public void writeImageDataXML(PrintWriter pw2, int num) {
		pw2.println("  <celldata id=\"" + id + "\">");
		pw2.println("   <Cgroup>" + Cgroup + "</Cgroup>");
		pw2.println("   <Dgroup>" + Dgroup + "</Dgroup>");
		pw2.println("   <Agroup>" + Agroup + "</Agroup>");
		pw2
				.println("   <upperleft>" + point.x + "," + point.y
						+ "</upperleft>");
		pw2.println("   <lowerright>" + bottomrightPoint.x + ","
				+ bottomrightPoint.y + "</lowerright>");

		if (group > 1)
			pw2.println("   <bud>" + (budtop.x) + "," + (budtop.y) + "</bud>");
		if (group > 1)
			pw2.println("   <neck>" + neck[0] % w + "," + neck[0] / w + " "
					+ neck[1] % w + "," + neck[1] / w + "</neck>");
		if (group > 0)
			pw2.println("   <mother>" + (longpoint[0].x) + ","
					+ (longpoint[0].y) + " " + (longpoint[1].x) + ","
					+ (longpoint[1].y) + " " + (shortpoint[0].x) + ","
					+ (shortpoint[0].y) + " " + (shortpoint[1].x) + ","
					+ (shortpoint[1].y) + "</mother>");
		if (bud_short_flag)
			pw2.println("   <daughter>" + (budlongpoint[0].x) + ","
					+ (budlongpoint[0].y) + " " + (budlongpoint[1].x) + ","
					+ (budlongpoint[1].y) + " " + (budshortpoint[0].x) + ","
					+ (budshortpoint[0].y) + " " + (budshortpoint[1].x) + ","
					+ (budshortpoint[1].y) + "</daughter>");
		if (edge != null && edge.size() > 0) {
			pw2.print("   <edge>");
			for (int i = 0; i < edge.size() - 1; i++) {
				int p = ((Integer) edge.get(i)).intValue();
				pw2.print((p % w) + "," + (p / w) + " ");
			}
			int p = ((Integer) edge.get(edge.size() - 1)).intValue();
			pw2.println((p % w) + "," + (p / w) + "</edge>");
		}
		if (Dpoint != null && Dpoint.size() > 0) {
			pw2.print("   <nucleus>");
			for (int i = 0; i < Dpoint.size() - 1; i++) {
				Point pp = (Point) Dpoint.get(i);
				pw2.print(pp.x + "," + pp.y + " ");
			}
			Point pp = (Point) Dpoint.get(Dpoint.size() - 1);
			pw2.println(pp.x + "," + pp.y + "</nucleus>");
		}
		if (actinpatchpoint != null && actinpatchpoint.size() > 0) {
			pw2.print("   <actin>");
			for (int i = 0; i < actinpatchpoint.size() - 1; i++) {
				int p = ((Integer) actinpatchpoint.get(i)).intValue();
				pw2.print((p % w) + "," + (p / w) + " ");
			}
			int p = ((Integer) actinpatchpoint.get(actinpatchpoint.size() - 1))
					.intValue();
			pw2.println((p % w) + "," + (p / w) + "</actin>");
		}
		pw2.println("  </celldata>");
	}

	//	アクチンパッチをノードとしたときの最小TSPパスの近似解を求める
	public void calcActinpathlength() {
		this.actinpatchpath = new Vector();
		if (this.actinpatchpoint.size() > 0) {
			if (this.getGroup() == 1) {
				int[] path = new int[this.actinpatchpoint.size()];
				this.actinpathlength = shortestpathlength(
						(Vector) this.actinpatchpoint.clone(), path);
				for (int j = 0; j < path.length; j++) {
					this.actinpatchpath.add(new Point(path[j] % w, path[j]
							/ w));
				}
			} else if (this.getGroup() > 1) {
				Vector mpatch = new Vector();
				Vector bpatch = new Vector();
				for (int j = 0; j < this.actinpatchpoint.size(); j++) {
					int p = ((Integer) this.actinpatchpoint.get(j))
							.intValue();
					if (this.inmother(p))
						mpatch.add(new Integer(p));
					else
						bpatch.add(new Integer(p));
				}
				if (mpatch.size() > 0 && bpatch.size() > 0) {
					mpatch.add(new Integer(this.neckpoint.y * w
							+ this.neckpoint.x));
					bpatch.add(new Integer(this.neckpoint.y * w
							+ this.neckpoint.x));
					int[] path = new int[mpatch.size()];
					double length = shortestpathlength(mpatch, path);
					int j = 0;
					while (path[j] != this.neckpoint.y * w
							+ this.neckpoint.x)
						j++;
					for (int k = j; k < path.length + j; k++) {
						this.actinpatchpath.add(new Point(path[k
								% path.length]
								% w, path[k % path.length] / w));
					}
					path = new int[bpatch.size()];
					this.actinpathlength = length
							+ shortestpathlength(bpatch, path);
					j = 0;
					while (path[j] != this.neckpoint.y * w
							+ this.neckpoint.x)
						j++;
					for (int k = j; k < path.length + j; k++) {
						this.actinpatchpath.add(new Point(path[k
								% path.length]
								% w, path[k % path.length] / w));
					}
				} else if (mpatch.size() > 0) {
					int[] path = new int[mpatch.size()];
					this.actinpathlength = shortestpathlength(mpatch, path);
					for (int j = 0; j < path.length; j++) {
						this.actinpatchpath.add(new Point(path[j] % w,
								path[j] / w));
					}
				} else if (bpatch.size() > 0) {
					int[] path = new int[bpatch.size()];
					this.actinpathlength = shortestpathlength(bpatch, path);
					for (int j = 0; j < path.length; j++) {
						this.actinpatchpath.add(new Point(path[j] % w,
								path[j] / w));
					}
				} else
					this.actinpathlength = -1;
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	//近似アルゴリズムで最小TSPパスを求める
	///////////////////////////////////////////////////////////////////////////
	private double shortestpathlength(Vector points, int[] ps) {
		int n = points.size();
		for (int i = 0; i < n; i++)
			ps[i] = ((Integer) points.get(i)).intValue();
		double length = 0;
		for (int i = 0; i < n; i++)
			length += distance(ps[i], ps[(i + 1) % n]);
		int i0 = 0;
		LOOP: while (true) {
			int i;
			for (i = i0; i < i0 + n; i++) {
				for (int j = i + 2; j < i + n - 1; j++) {
					double tmplength = distance(ps[i % n], ps[j % n])
							+ distance(ps[(i + 1) % n], ps[(j + 1) % n])
							- distance(ps[i % n], ps[(i + 1) % n])
							- distance(ps[j % n], ps[(j + 1) % n]);
					if (tmplength < -0.0001) {
						length += tmplength;
						for (int k = 0; k < (j - i) / 2; k++) {
							int tmp = ps[(i + 1 + k) % n];
							ps[(i + 1 + k) % n] = ps[(j - k) % n];
							ps[(j - k) % n] = tmp;
						}
						i0 = (i + 1) % n;
						continue LOOP;
					}
				}
			}
			for (i = i0; i < i0 + n; i++) {
				for (int k = i + 1; k <= i + 3; k++) {
					for (int j = k + 1; j < i + n - 1; j++) {
						double tmplength = distance(ps[i % n], ps[(k + 1) % n])
								+ distance(ps[j % n], ps[k % n])
								+ distance(ps[(i + 1) % n], ps[(j + 1) % n])
								- distance(ps[i % n], ps[(i + 1) % n])
								- distance(ps[j % n], ps[(j + 1) % n])
								- distance(ps[k % n], ps[(k + 1) % n]);
						if (tmplength < -0.0001) {
							length += tmplength;
							int[] tmp = new int[3];
							for (int l = i + 1; l <= k; l++)
								tmp[l - i - 1] = ps[l % n];
							for (int l = k + 1; l <= j; l++)
								ps[(l - k + i) % n] = ps[l % n];
							for (int l = 0; l < k - i; l++)
								ps[(j - k + i + 1 + l) % n] = tmp[k - i - 1 - l];
							i0 = (i + 1) % n;
							continue LOOP;
						}
						if (k == i + 1)
							continue;
						tmplength = distance(ps[i % n], ps[(k + 1) % n])
								+ distance(ps[j % n], ps[(i + 1) % n])
								+ distance(ps[k % n], ps[(j + 1) % n])
								- distance(ps[i % n], ps[(i + 1) % n])
								- distance(ps[j % n], ps[(j + 1) % n])
								- distance(ps[k % n], ps[(k + 1) % n]);
						if (tmplength < -0.0001) {
							length += tmplength;
							int[] tmp = new int[3];
							for (int l = i + 1; l <= k; l++)
								tmp[l - i - 1] = ps[l % n];
							for (int l = k + 1; l <= j; l++)
								ps[(l - k + i) % n] = ps[l % n];
							for (int l = 0; l < k - i; l++)
								ps[(j - l) % n] = tmp[k - i - 1 - l];
							i0 = (i + 1) % n;
							continue LOOP;
						}
					}
				}
			}
			return length;
		}
	}
}

//--------------------------------------
//$Log: Cell.java,v $
//Revision 1.7  2004/09/06 14:25:08  sesejun
//*** empty log message ***
//
//Revision 1.6  2004/09/06 13:44:06  sesejun
//CalMorphのおそらく正しい1_0のソース
//
//Revision 1.5  2004/07/29 03:03:05  sesejun
//サイズの出力Bugfix
//
//Revision 1.4  2004/07/29 02:33:02  sesejun
//各cellの座標の表示を行っていなかったのを追加。
//
//Revision 1.3  2004/06/30 17:14:21  sesejun
//計算部分と出力部分の分離。
//エラーを吐く際の
//