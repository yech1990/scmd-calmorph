package lab.cb.scmd.calmorph2;

import java.util.Vector;

public class Labeling {
	
	public Labeling() {
		
	}
    
	// TODO
	public Vector[] label(boolean[] binary_image, boolean color,int size_threshold, int width, int height, boolean corner_cut) {
    	int size = binary_image.length;
        Vector<Integer> same = new Vector<Integer>();
        int[] label = new int[size];
        
        NumberLabeling num_label = new NumberLabeling(width, size); // arg3 same 削除
        num_label.executeNumberLabeling(binary_image, color);
        int label_number = num_label.getLabelNumber();
        
        // sameをセットし直す　（必要？）
        int max_label = -1;
        for ( int i = 0; i < same.size(); i++ ) {
            int s = num_label.smallestlabel(i);
            if ( max_label < s ) { max_label = s; }
            same.set(i, new Integer(s));
        }
        
        label_number = max_label;
        Vector[] vec2 = new Vector[label_number + 1];
        for ( int i = 0; i < label_number + 1; i++ ) {
            vec2[i] = new Vector();
        }
        for ( int i = 0; i < size; i++ ) {
            if ( label[i] < 0 ) { }
            else { vec2[( (Integer)same.get(label[i]) ).intValue()].add(new Integer(i)); }
        }
        
        int number = 0;
        boolean[] flags = new boolean[label_number + 1];  //塊とみなすかどうか
        for ( int i = 0 ; i < label_number + 1; i++ ) {
            if ( vec2[i].size() > size_threshold ) {      //サイズ以上の塊について
                if ( !corner_cut ) {               //corner_cutが指定されていなければ
                    flags[i] = true;
                    number++;
                } else {                           //cornercutが指定されていれば
                    flags[i]=true;
                    for ( int j = 0; j < vec2[i].size(); j++ ) {
                        int p = ( (Integer)vec2[i].get(j) ).intValue();
                        if ( p < width || p > width * (height-1) || p % width == 0 || p % width == width - 1 ) {  //壁に接するpixelが存在
                            flags[i] = false;
                            break;
                        }
                    }
                    if ( flags[i] ) { number++; }
                }
            } else { flags[i] = false; }
        }
        
        Vector<Integer>[] result = new Vector[number];
        int index = 0;
        for ( int i = 0; i < result.length; i++ ) {
            result[i] = new Vector<Integer>();
            while ( index < label_number + 1 ) {
                if ( flags[index] ) { break; }
                index++;
            }
            if ( index < label_number + 1 ) {
                for ( int k = 0; k < vec2[index].size(); k++ ) {
                    result[i].add( (Integer)vec2[index].get(k) );
                }
                index++;
            } else { break; }
        }
        
        return result;
    }
	
}
