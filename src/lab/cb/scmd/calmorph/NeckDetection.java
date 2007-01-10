package lab.cb.scmd.calmorph;

import java.awt.geom.Point2D;
import java.util.Vector;

import lab.cb.scmd.calmorph2.NumberLabeling;

public class NeckDetection {
	
	private int _width, _height, _size;
	
	private final static boolean _black = true;
	private final static boolean _white = false;
	private boolean flag_tmp;
	
	public NeckDetection(int width, int size) {
		_width = width;
		_height = size / width;
		_size = size;
		
		flag_tmp = false;
	}
	
    public Cell[] searchNeck(Cell[] cell, int[] label_of_each_pixel, int[] label_of_each_pixel_2) {
        int scorerad = 10;
        int scoremeanrad = 2;
        int scorethr = 920;
        Vector tmp = new Vector();
        for(int i=0;i<cell.length;i++) {
            int es;
            boolean neck1 = false;
            int[] score,scoretmp;
            if(cell[i].getGroup() > 0) {
                Vector neck = new Vector();
                int jj;
                int start;
                while(true) {
					if(cell[i].budcrush == 2 || cell[i].edge.size() < 10) {
						cell[i].budcrush = 0;
						for(int j=0;j<_size;j++){
							if(label_of_each_pixel_2[j]==i) label_of_each_pixel[j]=i;
							if(label_of_each_pixel[j] ==i && label_of_each_pixel_2[j] == -1) label_of_each_pixel[j] = -1;
						}
						cell[i].edge = cell[i].edge_2;
						cell[i].cover = cell[i].cover_2;
					}
                    es = cell[i].edge.size();
                    scoretmp = new int[es];
                    score = new int[es];
                    for(int j=0;j<es;j++) {
                        int p=((Integer)cell[i].edge.get(j)).intValue();
                        for(int x=-scorerad;x<=scorerad;x++) {//半径scorerad以内の細胞内pixelのカウント
                            for(int y=-scorerad;y<=scorerad;y++) {
                                if(Math.sqrt(x*x+y*y) <= scorerad && p+y*_width+x >= 0 && p+y*_width+x < _size && label_of_each_pixel[p+y*_width+x] == i) scoretmp[j]++;
                            }
                        }
                    }
                    for(int j=0;j<es;j++) {
                        int s=0;
                        for(int k=-scoremeanrad;k<=scoremeanrad;k++) {//scoremeanradの範囲を合計
                            s += scoretmp[(j+es+k)%es];
                        }
                        score[j] = s;
                    }
                    jj=0;
                    while(true) {
                        if(jj<-es) {
                            break;
                        }
                        if(score[(jj+es)%es] < scorethr) break;
                        jj--;
                    }
                    start=-1;
                    for(int j=0;j<es;j++) {
                        if(score[(j+jj+es)%es] >= scorethr && start < 0) {
                            start = j;
                        }
                        if(score[(j+jj+es)%es] < scorethr && start >=0) {
                            neck.add(new Integer((start+j-1)/2));
                            start = -1;
                        }
                    }
                    if(neck.size() < 2 && cell[i].budcrush == 1) {
                        cell[i].budcrush = 0;
                        for(int j=0;j<_size;j++){
                            if(label_of_each_pixel_2[j]==i) label_of_each_pixel[j]=i;
                            if(label_of_each_pixel[j] ==i && label_of_each_pixel_2[j] == -1) label_of_each_pixel[j] = -1;
                        }
                        cell[i].edge = cell[i].edge_2;
                        cell[i].cover = cell[i].cover_2;
                        neck.clear();
                    } else break;
                }
                if(neck.size() > 2 || cell[i].edge.size() < 10) {
                    cell[i].bud_ratio = 0;
                    cell[i].setGroup(0);
                    continue;
                }
                if(neck.size() == 2) {//一回目ネック２個
                    int n1=((Integer)neck.get(0)).intValue();
                    int n2=((Integer)neck.get(1)).intValue();
                    if((n2-n1)*2 < es) {
                        for(int j=0;j<n1;j++) {
                            cell[i].mother_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                        }
                        for(int j=n1;j<n2;j++) {
                            cell[i].bud_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                        }
                        for(int j=n2;j<es;j++) {
                            cell[i].mother_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                        }
                    } else {
                        int n=n1+es-n2;
                        for(int j=0;j<n1;j++) {
                            cell[i].bud_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                        }
                        for(int j=n1;j<n2;j++) {
                            cell[i].mother_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                        }
                        for(int j=n2;j<es;j++) {
                            cell[i].bud_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                        }
                    }
                    cell[i].bud_cover = getLinePixel(((Integer)cell[i].edge.get((jj+((Integer)neck.get(0)).intValue()+es)%es)).intValue(),
                    		((Integer)cell[i].edge.get((jj+((Integer)neck.get(1)).intValue()+es)%es)).intValue(), _width);
                    flag_tmp = false;
                    cell[i].bud_cover = getAreainBud(i,cell[i].bud_cover,cell[i].mother_edge,cell[i].bud_edge, _width, _height, cell, flag_tmp);
                    if(flag_tmp) {
                        Vector tmp_vec = cell[i].bud_edge;
                        cell[i].bud_edge = cell[i].mother_edge;
                        cell[i].mother_edge = tmp_vec;
                    }
                    if(cell[i].bud_cover.size() == cell[i].bud_edge.size()) {//芽のcover領域が小さすぎる場合complexに分類しなおし
                        for(int j=0;j<cell[i].bud_edge.size();j++) {
                            int p = ((Integer)cell[i].bud_edge.get(j)).intValue();
                            cell[i].mother_edge.add(new Integer(p));
                        }
                        cell[i].bud_edge = new Vector();
                        cell[i].bud_ratio = 0;
                        cell[i].setGroup(0);
                    } else {
                        cell[i].bud_ratio = Math.sqrt((double)cell[i].bud_cover.size()/(cell[i].cover.size()-cell[i].bud_cover.size()));
                        if(cell[i].bud_ratio == 0) cell[i].setGroup(1);
                        else if(cell[i].bud_ratio < 0.5) cell[i].setGroup(2);
                        else if(cell[i].bud_ratio < 0.7) cell[i].setGroup(3);
                        else cell[i].setGroup(4);
                    }
                    cell[i].neck = new int[2];
                    for(int j=0;j<2;j++) {
                        int k= (jj+((Integer)neck.get(j)).intValue()+es)%es;
                        cell[i].neck[j]=((Integer)cell[i].edge.get(k)).intValue();
                    }
                } else if(neck.size()==1) {//１回目ネック１個
                    for(int j=0;j<es;j++) {
                        cell[i].mother_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                    }
                    cell[i].bud_ratio = 0;
                    cell[i].setGroup(1);
                    neck1 = true;//一回目でネックをひとつ見つけている
                    tmp = neck;
                } else {
                    for(int j=0;j<es;j++) {
                        cell[i].mother_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                    }
                    cell[i].bud_ratio = 0;
                    cell[i].setGroup(1);
                }
                //noに分類されたものについてくびれの認識をゆるくしてみる。
                //small以外には変えない
                for(int th=scorethr-20;th>=820;th-=20) {
                    if(cell[i].getGroup() == 1) {
                        jj=0;
                        while(true) {
                            if(jj<-es) {
                                break;
                            }
                            if(score[(jj+es)%es] < th) break;
                            jj--;
                        }
                        start=-1;
                        neck = new Vector();
                        for(int j=0;j<es;j++) {
                            if(score[(j+jj+es)%es] >= th && start < 0) {
                                start = j;
                            }
                            if(score[(j+jj+es)%es] < th && start >=0) {
                                neck.add(new Integer((start+j-1)/2));
                                start = -1;
                            }
                        }
                        if(neck.size() == 2) {
                            Vector medge = new Vector();
                            Vector bedge = new Vector();
                            int n1=((Integer)neck.get(0)).intValue();
                            int n2=((Integer)neck.get(1)).intValue();
                            if((n2-n1)*2 < es) {
                                for(int j=0;j<n1;j++) {
                                    medge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                                for(int j=n1;j<n2;j++) {
                                    bedge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                                for(int j=n2;j<es;j++) {
                                    medge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                            } else {
                                int n=n1+es-n2;
                                for(int j=0;j<n1;j++) {
                                    bedge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                                for(int j=n1;j<n2;j++) {
                                    medge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                                for(int j=n2;j<es;j++) {
                                    bedge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                            }
                            Vector bud_cover = getLinePixel(((Integer)cell[i].edge.get((jj+((Integer)neck.get(0)).intValue()+es)%es)).intValue(),
                            		((Integer)cell[i].edge.get((jj+((Integer)neck.get(1)).intValue()+es)%es)).intValue(), _width);
                            flag_tmp = false;
                            bud_cover = getAreainBud(i,bud_cover,medge,bedge, _width, _height, cell, flag_tmp);
                            if(flag_tmp) {
                                Vector tmp_vec = bedge;
                                bedge = medge;
                                medge = tmp_vec;
                            }
                            if(bud_cover.size() < bedge.size()+5) {//芽のcover領域が小さすぎる場合noに分類しなおし
                            } else {
                                double bud_ratio = Math.sqrt((double)bud_cover.size()/(cell[i].cover.size()-bud_cover.size()));
								if(bud_ratio < 0.5 && bud_ratio > 0) {
									cell[i].setGroup(2);//smallに変える
								} else if(bud_ratio < 0.7 && bud_ratio > 0 && neck1) {//最初に一つネックがみつかってるときのみ
									cell[i].setGroup(3);//mediumに変える
								} else if(bud_ratio <= 1.0 && bud_ratio > 0 && neck1) {//最初に一つネックがみつかってるときのみ
									cell[i].setGroup(4);//largeに変える
								}
								if(cell[i].getGroup() > 1){
	                                cell[i].mother_edge = medge;
    	                            cell[i].bud_edge = bedge;
        	                        cell[i].bud_ratio = bud_ratio;
            	                    cell[i].bud_cover = bud_cover;
                	                cell[i].neck = new int[2];
									for(int j=0;j<2;j++) {
										int k= (jj+((Integer)neck.get(j)).intValue()+es)%es;
										cell[i].neck[j] = ((Integer)cell[i].edge.get(k)).intValue();
									}
								}
                            }
                        } else if(th == 820 && neck1) {//最初にneck一つをみつけ最後までいってもneckが二つ見つからない
                            neck = tmp;
                            int n = ((Integer)neck.get(0)).intValue();
                            int np = ((Integer)cell[i].edge.get((n+jj+es)%es)).intValue();
                            double mind = _width*_height;
                            int minj = 0;
                            //int nigrad = 10;
                            flag_tmp = false;
                            double prev_d=0,d=0;
                            for(int j=0;j<cell[i].edge.size();j++) {
                                int p = ((Integer)cell[i].edge.get((n+j+jj+es)%es)).intValue();
                                prev_d = d;
                                d = Point2D.distance(np%_width,np/_width,p%_width,p/_width);
                                if(flag_tmp) {
                                    if(prev_d < d) {
                                        if(mind > d) {
                                            mind = d;
                                            minj = j;
                                        }
                                    }
                                } else {
                                    if(prev_d > d) {
                                        if(mind > d) {
                                            mind = d;
                                            minj = j;
                                        }
                                        flag_tmp = true;
                                    } else {
                                    }
                                }
                            }
                            if((minj+n+es)%es< n) {
                                neck.add(0,new Integer((minj+n+es)%es));
                            } else {
                                neck.add(new Integer((minj+n+es)%es));
                            }
                            int n1=((Integer)neck.get(0)).intValue();
                            int n2=((Integer)neck.get(1)).intValue();
                            cell[i].mother_edge.clear();
                            if((n2-n1)*2 < es) {
                                for(int j=0;j<n1;j++) {
                                    cell[i].mother_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                                for(int j=n1;j<n2;j++) {
                                    cell[i].bud_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                                for(int j=n2;j<es;j++) {
                                    cell[i].mother_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                            } else {
                                for(int j=0;j<n1;j++) {
                                    cell[i].bud_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                                for(int j=n1;j<n2;j++) {
                                    cell[i].mother_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                                for(int j=n2;j<es;j++) {
                                    cell[i].bud_edge.add(new Integer(((Integer)cell[i].edge.get((j+jj+es)%es)).intValue()));
                                }
                            }
                            cell[i].bud_cover = getLinePixel(((Integer)cell[i].edge.get((jj+((Integer)neck.get(0)).intValue()+es)%es)).intValue(),
                            		((Integer)cell[i].edge.get((jj+((Integer)neck.get(1)).intValue()+es)%es)).intValue(), _width);
                            flag_tmp = false;
                            cell[i].bud_cover = getAreainBud(i,cell[i].bud_cover,cell[i].mother_edge,cell[i].bud_edge, _width, _height, cell, flag_tmp);
                            if(flag_tmp) {
                                Vector tmp_vec = cell[i].bud_edge;
                                cell[i].bud_edge = cell[i].mother_edge;
                                cell[i].mother_edge = tmp_vec;
                            }
                            if(cell[i].bud_cover.size() == cell[i].bud_edge.size()) {//芽のcover領域が小さすぎる場合noに分類しなおし
                                for(int j=0;j<cell[i].bud_edge.size();j++) {
                                    int p = ((Integer)cell[i].bud_edge.get(j)).intValue();
                                    cell[i].mother_edge.add(new Integer(p));
                                }
                                cell[i].bud_edge = new Vector();
                                cell[i].bud_ratio = 0;
                                cell[i].setGroup(1);
                            } else {
                                cell[i].bud_ratio = Math.sqrt((double)cell[i].bud_cover.size()/(cell[i].cover.size()-cell[i].bud_cover.size()));
                                if(cell[i].bud_ratio == 0) cell[i].setGroup(1);
                                else if(cell[i].bud_ratio < 0.5) cell[i].setGroup(2);
                                else if(cell[i].bud_ratio < 0.7) cell[i].setGroup(3);
                                else cell[i].setGroup(4);
                            }
                            cell[i].neck = new int[2];
                            for(int j=0;j<2;j++) {
                                int k= (jj+((Integer)neck.get(j)).intValue()+es)%es;
                                System.out.print("k = " + k + "	neck.size = " + neck.size());
                                cell[i].neck[j] = ((Integer)cell[i].edge.get(k)).intValue();
                                System.out.print("neck set	");
                            }
                        }
                    } else {
                    }
                }
            }
        }
        return cell;
    }
    
    private static Vector getLinePixel(int s,int g, int _width) {
        int dx = g%_width-s%_width;
        int dy = g/_width-s/_width;
        int x,x_,y,y_,plusx,plusy,c1,c2,d;
        Vector line = new Vector();
        
        if(Math.abs(dx) >= Math.abs(dy)) {
            if(dx >= 0) {//始点と方向を決める
                x = s%_width;
                x_ = g%_width;
                y = s/_width;
                if(dy >= 0) {//
                    plusy = 1;
                } else {
                    plusy = -1;
                    dy = -dy;
                }
            } else {
                x = g%_width;
                x_ = s%_width;
                y = g/_width;
                dx = -dx;
                if(dy >= 0) {//
                    plusy = -1;
                } else {
                    plusy = 1;
                    dy = -dy;
                }
            }
            d = 2*dy-dx;
            c1 = 2*(dy-dx);
            c2 = 2*dy;
            line.add(new Integer(y*_width+x));
            for(int i=x+1;i<=x_;i++) {
                if(d > 0) {
                    y += plusy;
                    d += c1;
                } else {
                    d += c2;
                }
                line.add(new Integer(y*_width+i));
            }
        } else {
            if(dy >= 0) {//始点と方向を決める
                y = s/_width;
                y_ = g/_width;
                x = s%_width;
                if(dx >= 0) {
                    plusx = 1;
                } else {
                    plusx = -1;
                    dx = -dx;
                }
            } else {//goal、start入れ替え
                y = g/_width;
                y_ = s/_width;
                x = g%_width;
                dy = -dy;
                if(dx >= 0) {
                    plusx = -1;
                } else {
                    plusx = 1;
                    dx = -dx;
                }
            }
            d = 2*dx-dy;
            c1 = 2*(dx-dy);
            c2 = 2*dx;
            line.add(new Integer(y*_width+x));
            for(int i=y+1;i<=y_;i++) {
                if(d > 0) {
                    x += plusx;
                    d += c1;
                } else {
                    d += c2;
                }
                line.add(new Integer(i*_width+x));
            }
        }
        return line;
    }
    
    private static Vector getAreainBud(int c,Vector n,Vector m,Vector b, int _width, int _height, Cell[] cell, boolean flag_tmp) {
        int top=_height,bottom=0,left=_width,right=0;//coverする長方形
        for(int i=0;i<m.size();i++) {
            int p = ((Integer)m.get(i)).intValue();
            if(top > p/_width) top = p/_width;
            if(bottom < p/_width) bottom = p/_width;
            if(left > p%_width) left = p%_width;
            if(right < p%_width) right = p%_width;
        }
        for(int i=0;i<b.size();i++) {
            int p = ((Integer)b.get(i)).intValue();
            if(top > p/_width) top = p/_width;
            if(bottom < p/_width) bottom = p/_width;
            if(left > p%_width) left = p%_width;
            if(right < p%_width) right = p%_width;
        }
        for(int i=0;i<n.size();i++) {
            int p = ((Integer)n.get(i)).intValue();
            if(top > p/_width) top = p/_width;
            if(bottom < p/_width) bottom = p/_width;
            if(left > p%_width) left = p%_width;
            if(right < p%_width) right = p%_width;
        }
        int wid = right-left+3;
        int hei = bottom-top+3;
        int s = wid*hei;
         int[] greytemp = new int[s];
         Vector ba = new Vector();//返すvector
        
        for(int i=0;i<s;i++) {
            greytemp[i] = 255;
        }
        //neck、bud_edgeで囲んだ領域を作る
        for(int i=0;i<b.size();i++) {
            int p = ((Integer)b.get(i)).intValue();
            int x = p%_width-left;
            int y = p/_width-top;
            greytemp[y*wid+x+1+wid] = 0;//小さいほうの座標
        }
        for(int i=0;i<n.size();i++) {
            int p = ((Integer)n.get(i)).intValue();
            int x = p%_width-left;
            int y = p/_width-top;
            greytemp[y*wid+x+1+wid] = 0;//小さいほうの座標
        }
        Vector[] vec = label(greytemp, 255, 0, false,wid,hei);//小さいほうでラベル付け
        
        if(vec.length == 2) {
            boolean flag_out=false;
            for(int i=0;i<vec[0].size();i++) {
                if(((Integer)vec[0].get(i)).intValue() == 0) {
                    flag_out = true;
                    break;
                }
            }
            if(flag_out) {//vec[0]が外部
                if(cell[c].cover.size()/2 >= vec[1].size()+b.size()) {//芽の領域確定
                    for(int i=0;i<cell[c].cover.size();i++) {
                        int p = ((Integer)cell[c].cover.get(i)).intValue();
                        int x = p%_width-left;
                        int y = p/_width-top;
                        if(y*wid+x+1+wid>=0 &&y*wid+x+1+wid < wid*hei) greytemp[y*wid+x+1+wid] = 0;//あとで修正・・・
                    }
                    for(int i=0;i<vec[1].size();i++) {
                        int p = ((Integer)vec[1].get(i)).intValue();//座標を戻す
                        int x = p%wid-1+left;
                        int y = p/wid-1+top;
                        if(greytemp[p] == 0) ba.add(new Integer(y*_width+x));
                    }
                    for(int i=0;i<b.size();i++) {//芽の輪郭部分も加える
                        int p = ((Integer)b.get(i)).intValue();
                        ba.add(new Integer(p));
                    }
                } else {//bud_edgeとmother_edgeを入れ替える
                    for(int i=0;i<vec[1].size();i++) {//greytempを埋める
                        int p = ((Integer)vec[1].get(i)).intValue();
                        greytemp[p] = 0;
                    }
                    for(int i=0;i<cell[c].cover.size();i++) {//cover領域でbudにされなかったものをいれる
                        int p = ((Integer)cell[c].cover.get(i)).intValue();
                        int x = p%_width-left;
                        int y = p/_width-top;
                        if(greytemp[y*wid+x+1+wid] == 255) ba.add(new Integer(p));
                    }
                    flag_tmp = true;
                }
            } else {//vec[1]が外部
                if(cell[c].cover.size()/2 >= vec[0].size()+b.size()) {//芽の領域確定
                    for(int i=0;i<cell[c].cover.size();i++) {
                        int p = ((Integer)cell[c].cover.get(i)).intValue();
                        int x = p%_width-left;
                        int y = p/_width-top;
                        if(y*wid+x+1+wid > 0 && y*wid+x+1+wid < wid*hei) greytemp[y*wid+x+1+wid] = 0;
                    }
                    for(int i=0;i<vec[0].size();i++) {
                        int p = ((Integer)vec[0].get(i)).intValue();//座標を戻す
                        int x = p%wid-1+left;
                        int y = p%hei-1+top;
                        if(greytemp[p] == 0) ba.add(new Integer(y*_width+x));
                    }
                    for(int i=0;i<b.size();i++) {//芽の輪郭部分も加える
                        int p = ((Integer)b.get(i)).intValue();
                        ba.add(new Integer(p));
                    }
                } else {//bud_edgeとmother_edgeを入れ替える
                    for(int i=0;i<vec[0].size();i++) {//greytempを埋める
                        int p = ((Integer)vec[0].get(i)).intValue();
                        greytemp[p] = 0;
                    }
                    for(int i=0;i<cell[c].cover.size();i++) {//cover領域でbudにされなかったものをいれる
                        int p = ((Integer)cell[c].cover.get(i)).intValue();
                        int x = p%_width-left;
                        int y = p/_width-top;
                        if(greytemp[y*wid+x+1+wid] == 255) ba.add(new Integer(p));
                    }
                    flag_tmp = true;
                }
            }
        } else if(vec.length == 1) {//内部が見つからない
        } else {//たまたま二つ以上に分割されたばあい・・・
            int v=0;
            for(int i=1;i<vec.length;i++) {
                v += vec[i].size();
            }
            if(cell[c].cover.size()/2 >= v+b.size()) {//芽の領域確定
                for(int i=0;i<cell[c].cover.size();i++) {
                    int p = ((Integer)cell[c].cover.get(i)).intValue();
                    int x = p%_width-left;
                    int y = p/_width-top;
                    if(y*wid+x+1+wid > 0 && y*wid+x+1+wid < wid*hei) greytemp[y*wid+x+1+wid] = 0;//あとで修正いるかも・・・
                }
                for(int j=1;j<vec.length;j++) {
                    for(int i=0;i<vec[j].size();i++) {
                        int p = ((Integer)vec[j].get(i)).intValue();//座標を戻す
                        int x = p%wid-1+left;
                        int y = p/wid-1+top;
                        if(greytemp[p] == 0) ba.add(new Integer(y*_width+x));
                    }
                }
                for(int i=0;i<b.size();i++) {//芽の輪郭部分も加える
                    int p = ((Integer)b.get(i)).intValue();
                    ba.add(new Integer(p));
                }
            } else {//bud_edgeとmother_edgeを入れ替える
                for(int j=1;j<vec.length;j++) {
                    for(int i=0;i<vec[j].size();i++) {//greytempを埋める
                        int p = ((Integer)vec[j].get(i)).intValue();
                        greytemp[p] = 0;
                    }
                }
                for(int i=0;i<cell[c].cover.size();i++) {//cover領域でbudにされなかったものをいれる
                    int p = ((Integer)cell[c].cover.get(i)).intValue();
                    int x = p%_width-left;
                    int y = p/_width-top;
                    if(greytemp[y*wid+x+1+wid] == 255) ba.add(new Integer(p));
                }
                flag_tmp = true;
            }
        }
        
        return ba;
    }
    
    private static Vector[] label(int[] grey,int color,int minco,boolean cornercut,int wid,int hei) {
        Vector[] vec;
        Vector<Integer> same;
        int[] lab = new int[wid*hei];
        
        int nlbl = 0;
        same = new Vector();
        for(int i=0;i<wid*hei;i++) {
            lab[i] = -1;
        }
        if(grey[0] == color) {
            lab[0] = nlbl;
            same.add(new Integer(nlbl++));
        }
        for(int j=1;j<wid;j++) {
            if(grey[j] == color) {
                if(lab[j-1] >= 0) {
                    lab[j] = NumberLabeling.smallestlabel(same,lab[j-1]);
                } else {
                    lab[j] = nlbl;
                    same.add(new Integer(nlbl++));
                }
            }
        }
        for(int i=1;i<hei;i++) {
            if(grey[i*wid] == color) {
                if(lab[(i-1)*wid] >= 0) {
                    lab[i*wid] = NumberLabeling.smallestlabel(same,lab[(i-1)*wid]);
                } else {
                    lab[i*wid] = nlbl;
                    same.add(new Integer(nlbl++));
                }
            }
            for(int j=1;j<wid;j++) {
                if(grey[i*wid+j] == color) {
                    int a1,a2;
                    if(lab[i*wid+j-1] >= 0) a1 = NumberLabeling.smallestlabel(same,lab[i*wid+j-1]);
                    else a1 = -1;
                    if(lab[(i-1)*wid+j] >= 0) a2 = NumberLabeling.smallestlabel(same,lab[(i-1)*wid+j]);
                    else a2 = -1;
                    if(a1 == -1 && a2 == -1) {
                        lab[i*wid+j] = nlbl;
                        same.add(new Integer(nlbl++));
                    } else if(a1 == -1) {
                        lab[i*wid+j] = a2;
                    } else if(a2 == -1) {
                        lab[i*wid+j] = a1;
                    } else if(a1 < a2) {
                        lab[i*wid+j] = a1;
                        same.set(a2,new Integer(a1));
                    } else {
                        lab[i*wid+j] = a2;
                        same.set(a1,new Integer(a2));
                    }
                }
            }
        }
        int maxl = -1;
        for(int i=0;i<same.size();i++) {
            int s = NumberLabeling.smallestlabel(same,i);
            if(maxl < s) maxl = s;
            same.set(i,new Integer(s));
        }
        nlbl = maxl;
        Vector[] vec2 = new Vector[nlbl+1];
        for(int i=0;i<nlbl+1;i++) {
            vec2[i] = new Vector();
        }
        for(int i=0;i<wid*hei;i++) {
            if(lab[i] < 0) {
            } else {
                vec2[((Integer)same.get(lab[i])).intValue()].add(new Integer(i));
            }
        }
        int num=0;
        boolean[] flag = new boolean[nlbl+1];//塊とみなすかどうか
        for(int i=0;i<nlbl+1;i++) {
            if(vec2[i].size() > minco) {//サイズ以上の塊について
                if(!cornercut) {//cornercutが指定されていなければ
                    flag[i] = true;
                    num++;
                } else {//cornercutが指定されていれば
                    flag[i]=true;
                    for(int j=0;j<vec2[i].size();j++) {
                        int p=((Integer)vec2[i].get(j)).intValue();
                        if(p < wid || p>wid*(hei-1) || p%wid == 0 || p%wid == wid-1) {//壁に接するpixelが存在
                            flag[i]=false;
                            break;
                        }
                    }
                    if(flag[i]) num++;
                }
            } else {
                flag[i] = false;
            }
        }
        vec = new Vector[num];
        int index=0;
        for(int i=0;i<vec.length;i++) {
            vec[i] = new Vector();
            while(index < nlbl+1) {
                if(flag[index]) break;
                index++;
            }
            if(index < nlbl+1) {
                for(int k=0;k<vec2[index].size();k++) {
                    vec[i].add(vec2[index].get(k));
                }
                index++;
            } else {
                break;
            }
        }
        return vec;
    }
}
