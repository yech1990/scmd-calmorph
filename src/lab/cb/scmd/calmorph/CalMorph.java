package lab.cb.scmd.calmorph;

//------------------------------------
// SCMD Project
//  
// CalMorph.java 
// Since:  2004/04/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

import java.io.*;

class CalMorph {
    boolean outstate,objectsave,outimage;
    int maximage,objectload;
    String name,path,outdir,xmldir;
    
    public static void main(String[] s) {
        CalMorph cm = new CalMorph();
        File in=null,out=null,xml=null;
        if(s.length == 1) {
            System.err.println("CalMorph");
            System.err.println("gui usage: java CalMorph");
            System.err.println("cui usage: java CalMorph [OPTION] inputdir outputdir xmldir");
            System.err.println("OPTION:");
            System.err.println("  --outstate");
            System.err.println("  --objectsave");
            System.err.println("  --objectload=value");
            System.err.println("  --outimage");
            System.exit(1);
        } else if(s.length == 0) {//引数0でGUI起動
            GUIFrame gui = new GUIFrame();
            gui.setVisible(true);
		} else if(s[s.length-3].substring(0,1).equals("-")) {
			System.err.println("directory name \""+s[s.length-3]+"\" is not allowed");
			System.exit(1);
        } else if(s[s.length-2].substring(0,1).equals("-")) {
            System.err.println("directory name \""+s[s.length-2]+"\" is not allowed");
            System.exit(1);
        } else if(s[s.length-1].substring(0,1).equals("-")) {
            System.err.println("directory name \""+s[s.length-1]+"\" is not allowed");
            System.exit(1);
        } else {
            //optionのデフォルト値
            cm.maximage = 200;
            cm.outstate = false;
            cm.objectsave = false;
            cm.objectload = -1;
            for(int i=0;i<s.length-3;i++) {
                cm.optionSearch(s[i]);
            }
            in = new File(s[s.length-3]);
            out = new File(s[s.length-2]);
            xml = new File(s[s.length-1]);
            if(in.exists()) {
                cm.name = in.getName();
                cm.path = in.getAbsolutePath()+"/"+in.getName();
            } else {
                System.err.println("error:"+in.getName()+" not exist");
                System.exit(1);
            }
            if(out.exists()) {
                cm.outdir = out.getAbsolutePath();
            } else {
                out.mkdir();
                cm.outdir = out.getAbsolutePath();
            }
			if(xml.exists()) {
				cm.xmldir = xml.getAbsolutePath();
			} else {
				xml.mkdir();
				cm.xmldir = xml.getAbsolutePath();
			}
            
			String ls[] = in.list();
			for (int i = 0; i < ls.length; i++)
			{
				if(ls[i].substring(ls[i].length()-4,ls[i].length()).equals(".jpg")){
					int j=ls[i].length()-5;
					while(ls[i].charAt(j) != 'A' && ls[i].charAt(j) != 'C' && ls[i].charAt(j) != 'D' && j>0) j--;
					if(j>0){
						int k = Integer.parseInt(ls[i].substring(j+1,ls[i].length()-4));
						if(k>cm.maximage) cm.maximage = k;
					}
				}
			}
            cm.main();
        }
    }
    public CalMorph() {
    }
    public void main() {
    	
        DisruptantProcess dp = new DisruptantProcess(name,path,outdir,xmldir,maximage,objectsave,objectload,null,outstate,true,true,outimage,true);
		dp.setPrintFile("CUI");
        dp.process();
    }
    public void optionSearch(String option) {
        if(option.length() < 10) {
            System.err.println("Unrecognized option:"+option);
            System.exit(1);
        } else if(option.substring(0,10).equals("--outstate")) {
            outstate=true;
            System.err.println("outstate");
        } else if(option.length() == 12 && option.substring(0,12).equals("--objectsave")) {
            objectsave=true;
            System.err.println("objectsave");
        } else if(option.length() >= 13 && option.substring(0,13).equals("--objectload=")) {
            objectload = Integer.parseInt(option.substring(13));
            System.err.println("objectload="+objectload);
        } else if(option.length() == 10 && option.substring(0,10).equals("--outimage")) {
            outimage=true;
            System.err.println("outimage");
        } else {
            System.err.println("Unrecognized option:"+option);
            System.exit(1);
        }
    }
}

//--------------------------------------
//$Log: CalMorph.java,v $
//Revision 1.6  2004/09/06 13:44:06  sesejun
//CalMorphのおそらく正しい1_0のソース
//
//Revision 1.5  2004/07/20 01:44:22  sesejun
//利用していないimportを削除
//
//Revision 1.4  2004/07/01 10:23:29  sesejun
//表示部と計算部をある程度分離
//
//Revision 1.3  2004/06/30 17:14:21  sesejun
//計算部分と出力部分の分離。
//エラーを吐く際の
//
//Revision 1.2  2004/06/25 01:42:05  sesejun
//CalMorph_version_1.0相当へ変更
//一部、変更が元にもどってしまっている可能性大
//気がついたら、再度変更してください。
//