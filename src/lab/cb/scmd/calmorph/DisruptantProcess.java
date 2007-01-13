// ------------------------------------
// SCMD Project
//  
// DisruptantProcess.java
// Since: 2004/04/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------
package lab.cb.scmd.calmorph;

import java.io.*;

/**
 * 
 * 画像解析後のファイルを吐き出す
 * 
 * @author mattun
 *
 */
class DisruptantProcess {
    String name,path,outdir,xmldir;
    GUIFrame gui;
    boolean objectsave,outstate,calD,calA,outimage,outsheet;
    int maximage,objectload;

    String DATAFILESUFFIX	= ".xls";
    String SUMMARYFILE		= "_data";
    String SDFILE			= "_SD";
    String VERSATILEFILE 	= "versatile";
    String ACTINFILE		= "actin";
    String CONAFILE			= "conA";
    String DAPIFILE			= "dapi";

    public DisruptantProcess(String name,String path,String outdir,String xmldir,int maximage,boolean objectsave,int objectload,GUIFrame gui,boolean outstate,boolean calD,boolean calA,boolean outimage,boolean outsheet) {
        this.name = name;
        this.path = path;
        this.outdir = outdir;
        this.xmldir = xmldir;
        this.maximage = maximage;
        this.objectsave = objectsave;
        this.objectload = objectload;
        this.gui = gui;
        this.outstate = outstate;
        this.calD = calD;
        this.calA = calA;
        this.outimage = outimage;
        this.outsheet = outsheet;
    }
    public void process() {
        File f = new File(outdir+"/"+name);
        if(!f.exists()) f.mkdir();
        if(xmldir!=null){
	        f = new File(xmldir+"/"+name);
			if(!f.exists()) f.mkdir();
        }
        PrintWriter pwbaseC = null;
        PrintWriter pwexpandC = null;
        PrintWriter pwbaseD = null;
        PrintWriter pwexpandD = null;
        PrintWriter pwbaseA = null;
        PrintWriter pwexpandA = null;
        PrintWriter pwpatchA = null;
        PrintWriter pwvers = null;
		PrintWriter pw2 = null;
		PrintWriter pwxml = null;
		
		//	ファイルに書き込むためのPrintWriterを作成する
        try {
        	pwvers = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+name+"/"+name+".xls")));
        	if(outsheet){
	            pwbaseC = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+name+"/"+name+"_conA_basic.xls")));
    	        pwexpandC = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+name+"/"+name+"_conA_biological.xls")));
            	if(calD) {
            		pwbaseD = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+name+"/"+name+"_dapi_basic.xls")));
            		pwexpandD = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+name+"/"+name+"_dapi_biological.xls")));
            	}
            	if(calA){
	        	    pwbaseA = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+name+"/"+name+"_actin_basic.xls")));
    	    	    pwexpandA = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+name+"/"+name+"_actin_biological.xls")));
    	    	    pwpatchA = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+name+"/"+name+"_actin_patch.xls")));
            	}
        	}
			pw2 =  new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+name+"/exclusion_log.txt")));
			if(xmldir!=null) pwxml = new PrintWriter(new BufferedWriter(new FileWriter(xmldir+"/"+name+"/"+name+"_image.xml")));

        } catch (Exception e) {
            System.err.println("DisruptantProcess.process():"+e);
        }
        //	ファイルにヘッダー情報を書き込む
        setXLSHeaderVers(pwvers);
        if(outsheet){
	        setXLSHeaderBaseC(pwbaseC);
    	    setXLSHeaderExpandC(pwexpandC);
    	    if(calD){
		        setXLSHeaderBaseD(pwbaseD);
    		    setXLSHeaderExpandD(pwexpandD);
    	    }
    	    if(calA){
		        setXLSHeaderBaseA(pwbaseA);
    		    setXLSHeaderExpandA(pwexpandA);
    		    setXLSHeaderPatchA(pwpatchA);
   	    	}
        }
        if(pwxml!=null) setXMLHeader(pwxml);
        int startid = 0;

        //	平均値計算？
        AverageData ad = new AverageData(name,outdir);
        
        for(int i=1;i<=maximage;i++) {
			try
			{
            if(gui != null && gui.flag_reset) return;
            CellImage image = new CellImage(name,path,i,outdir,startid,calD,calA);
            System.out.println("error = " + image.err);
            if(image.err){
            	if(!image.err_kind.equals("")) pw2.println(i + ": " + image.err_kind);
            	pw2.flush();
            	 continue; 
            }
            System.out.println(objectsave + "/" + objectload + "/ "+ outimage + "/" + outsheet);
            image.setOptions(objectsave,objectload,outimage,outsheet);
            if(gui != null) gui.state_jtf.setText("proc:"+name+"-"+i+"...");
            else if(outstate) System.err.println("proc:"+name+"-"+i+"...");
            //	画像から解析？
            startid += image.process(pwbaseC,pwexpandC,pwbaseD,pwexpandD,pwbaseA,pwexpandA,pwpatchA,pwvers,pwxml);
			if(!image.err) ad.addCellData(image);
			else pw2.println(i + ": " + image.err_kind);
			}
			catch(Exception e){
				pw2.println(i + ": unexpected error");
				System.out.println(name + " : " + i + ": unexpected error [in DisruptantProcess.process()]");
			}
        }
        ad.printDataXLS(calA,calD,outsheet);
        ad.printSDDataXLS(calA,calD,outsheet);
        pwvers.flush();
        pwvers.close();
        if(outsheet){
	        pwbaseC.flush();
	        pwbaseC.close();
	        pwexpandC.flush();
	        pwexpandC.close();
		    if(calD){
	        	pwbaseD.flush();
	        	pwbaseD.close();
		        pwexpandD.flush();
	    	    pwexpandD.close();
	        }
	        if(calA){
		        pwbaseA.flush();
	    	    pwbaseA.close();
	        	pwexpandA.flush();
	        	pwexpandA.close();
	        	pwpatchA.flush();
	        	pwpatchA.close();
	        }
        }
        pw2.flush();
        pw2.close();
        if(pwxml!=null){
        	setXMLBottom(pwxml);
        	pwxml.flush();
        	pwxml.close();
        }
    }
    /**
     * FILENAME_conA_basic.xls
     * @param pw
     */
    public void setXLSHeaderBaseC(PrintWriter pw) {
    	String[] headerBaseC = {
    			"image_number",
    			"cell_id",
    			"Cgroup",
    			"C1-1",
    			"C1-2",
    			"C2-1",
    			"C2-2",
    			"C3-1",
    			"C3-2",
    			"C4-1",
    			"C4-2",
    			"C5-1",
    			"C5-2",
    			"C6",
    			"C7",
    			"C8",
    			"C9",
    			"C10",
    			"C11-1",
    			"C11-2",
    			"C12-1",
    			"C12-2",
    			"C13"
    	};
		printOneLine(pw, headerBaseC, "\t");
    }
    /**
     * FILENAME_conA_biological.xls
     * @param pw
     */
    public void setXLSHeaderExpandC(PrintWriter pw) {
    	pw.print("image_number\tcell_id\tCgroup\t");
        for(int i=101;i<=118;i++) pw.print("C" + i + "\t");
        pw.print("C126\tC127\tC128\t");
        pw.println();
    }
    /**
     * FILENAME_dapi_basic.xls
     * @param pw
     */
    public void setXLSHeaderBaseD(PrintWriter pw) {
    	String[] headerBaseD = {
    			"image_number",
    			"cell_id",
    			"Cgroup",
    			"Dgroup",
    			"D1-1",
    			"D1-2",
    			"D1-3-1",
    			"D1-3-2",
    			"D2-1",
    			"D2-2",
    			"D2-3-1",
    			"D2-3-2",
    			"D3-1",
    			"D3-2",
    			"D3-3",
    			"D4-1",
    			"D4-2",
    			"D4-3",
    			"D5-1",
    			"D5-2",
    			"D5-3",
    			"D6-1",
    			"D6-2",
    			"D7",
    			"D8",
    			"D9-1",
    			"D9-2",
    			"D10-1",
    			"D10-2",
    			"D11-1",
    			"D11-2",
    			"D12-1",
    			"D12-2",
    			"D13-1",
    			"D13-2",
    			"D14-1",
    			"D14-2",
    			"D14-3",
    			"D15-1",
    			"D15-2",
    			"D15-3",
    			"D16-1",
    			"D16-2",
    			"D16-3",
    			"D17-1",
    			"D17-2",
    			"D17-3"
    	};
		printOneLine(pw, headerBaseD, "\t");
    }
    /**
     * FILENAME_dapi_biological.xls
     * @param pw
     */
    public void setXLSHeaderExpandD(PrintWriter pw) {
    	pw.print("image_number\tcell_id\tCgroup\tDgroup\t");
        for(int i=101;i<=198;i++) pw.print("D" + i + "\t");
        pw.println();
    }
    /**
     * FILENAME_actin_basic.xls
     * @param pw
     */
    public void setXLSHeaderBaseA(PrintWriter pw) {
    	String[] headerBaseA = {
    			"image_number",
    			"cell_id",
    			"Cgroup",
    			"Agroup",
    			"A2-1",
    			"A2-2",
    			"A2-3",
    			"A3-1",
    			"A3-2",
    			"A3-3",
    			"A4-1",
    			"A4-2",
    			"A4-3",
    			"A5-1",
    			"A5-2",
    			"A5-3",
    			"A6-1",
    			"A6-2",
    			"A6-3",
    			"A7-1",
    			"A7-2",
    			"A8-1",
    			"A8-2",
    			"A9"
    	};
    	printOneLine(pw, headerBaseA, "\t");
    }
    /**
     * FILENAME_dapi_biological.xls
     * @param pw
     */
    public void setXLSHeaderExpandA(PrintWriter pw) {
    	String[] headerExpandA = {
    			"image_number",
    			"cell_id",
    			"Cgroup",
    			"Agroup",
    			"A101",
    			"A102",
    			"A103",
    			"A104",
    			"A120",
    			"A121",
    			"A122",
    			"A123"		
    	};
    	for(int i = 0; i < headerExpandA.length; i++ ) {
    		pw.print(headerExpandA[i] + "\t");
    	}
        pw.println();
    }
    /**
     * FILENAME_actin_patch.xls
     * @param pw
     */
    public void setXLSHeaderPatchA(PrintWriter pw) {
        pw.println("image_number\tcell_id\tposition\tsize\tbrightness\t");
    }
    /**
     * FILENAME.xls
     * @param pw
     */
    public void setXLSHeaderVers(PrintWriter pw) {
    	String[] versatiles = {
				"image_number",
				"cell_id",
				"Cgroup",
				"mother_cell_size",
				"bud_cell_size",
				"bud_ratio",
				"axis_ratio_in_mother",
				"axis_ratio_in_bud",
				"bud_direction",
				"neck_position",
				"neck_width",
				"Agroup",
				"actin_region_ratio",
				"bud_actin_ratio",
				"mother_actin_gravity_point",
				"bud_actin_gravity_point",
				"Dgroup",
				"nuclear_number",
				"nuclear_size_in_mother",
				"nuclear_size_in_bud",
				"nuclear_size_in_cell",
				"nuclear_axis_ratio_in_mother",
				"nuclear_axis_ratio_in_bud",
				"nuclear_axis_ratio_in_cell",
				"hip_nuclear",
				"mother_cell_center_nuclear",
				"neck_nuclear_in_mother",
				"bud_top_nuclear",
				"bud_cell_center_nuclear",
				"neck_nuclear_in_bud",
				"length_between_nucleus",
				"x1",
				"y1",
				"x2",
				"y2"
		};
    	printOneLine(pw, versatiles, "\t");
//        pw.println("image_number\tcell_id\tCgroup\tmother_cell_size\tbud_cell_size\tbud_ratio\taxis_ratio_in_mother\taxis_ratio_in_bud\tbud_direction\tneck_position\tneck_width\tAgroup\tactin_region_ratio\tbud_actin_ratio\tmother_actin_gravity_point\tbud_actin_gravity_point\tDgroup\tnuclear_number\tnuclear_size_in_mother\tnuclear_size_in_bud\tnuclear_size_in_cell\tnuclear_axis_ratio_in_mother\tnuclear_axis_ratio_in_bud\tnuclear_axis_ratio_in_cell\thip_nuclear\tmother_cell_center_nuclear\tneck_nuclear_in_mother\tbud_top_nuclear\tbud_cell_center_nuclear\tneck_nuclear_in_bud\tlength_between_nucleus\t");
    }
    public void setXMLHeader(PrintWriter pwxml) {
        pwxml.println("<imagedata orf=\"" + name +"\">");
    }
    public void setXMLBottom(PrintWriter pwxml) {
        pwxml.println("</imagedata>");
    }
	public void setPrintFile(String mode) {
		try {
			if(mode.equals("GUI") || !new File(outdir+"/" + VERSATILEFILE + DATAFILESUFFIX).exists()) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/" + VERSATILEFILE + DATAFILESUFFIX)));
				pw.print("name\tcell_without_complex\t");
				pw.print("mother_cell_size\tbud_cell_size\tbud_ratio\taxis_ratio_in_mother\taxis_ratio_in_bud\tbud_direction\t");
				pw.print("neck_position\tneck_width\tno_bud_ratio\tsmall_bud_ratio\tmedium_bud_ratio\tlarge_bud_ratio\t");
				pw.print("actin_region_ratio\tbud_actin_ratio\tmother_actin_gravity_point\tbud_actin_gravity_point\tactin_A_ratio\tactin_B_ratio\tactin_api_ratio\tactin_iso_ratio\tactin_E_ratio\tactin_F_ratio\tactin_N_ratio\t");
				pw.print("nuclear_number\tnuclear_size_in_mother\tnuclear_size_in_bud\tnuclear_size_in_cell\tnuclear_axis_ratio_in_mother\tnuclear_axis_ratio_in_bud\tnuclear_axis_ratio_in_cell\t");
				pw.print("hip_nuclear\tmother_cell_center_nuclear\tneck_nuclear_in_mother\tbud_top_nuclear\tbud_cell_center_nuclear\tneck_nuclear_in_bud\tlength_between_nucleus\t");
				pw.println("nuclear_A_ratio\tnuclear_A1_ratio\tnuclear_B_ratio\tnuclear_C_ratio\tnuclear_D_ratio\tnuclear_E_ratio\tnuclear_F_ratio\t");
				pw.flush();
				pw.close();
			}
			if(mode.equals("GUI") || !new File(outdir+"/" + VERSATILEFILE + SDFILE + DATAFILESUFFIX).exists()) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/" +VERSATILEFILE + SDFILE + DATAFILESUFFIX)));
				pw.print("name\tmother_cell_size\t#\tbud_cell_size\t#\tbud_ratio\t#\taxis_ratio_in_mother\t#\taxis_ratio_in_bud\t#\tbud_direction\t#\t");
				pw.print("neck_position\t#\tneck_width\t#\t");
				pw.print("actin_region_ratio\t#\tbud_actin_ratio\t#\tmother_actin_gravity_point\t#\tbud_actin_gravity_point\t#\t");
				pw.print("nuclear_number\t#\tnuclear_size_in_mother\t#\tnuclear_size_in_bud\t#\tnuclear_size_in_cell\t#\tnuclear_axis_ratio_in_mother\t#\tnuclear_axis_ratio_in_bud\t#\tnuclear_axis_ratio_in_cell\t#\t");
				pw.println("hip_nuclear\t#\tmother_cell_center_nuclear\t#\tneck_nuclear_in_mother\t#\tbud_top_nuclear\t#\tbud_cell_center_nuclear\t#\tneck_nuclear_in_bud\t#\tlength_between_nucleus\t#\t");
				pw.flush();
				pw.close();
			}
			if(mode.equals("CUI") || outsheet){
			if(mode.equals("GUI") || !new File(outdir+"/"+ CONAFILE + SUMMARYFILE + DATAFILESUFFIX).exists()) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"+ CONAFILE + SUMMARYFILE + DATAFILESUFFIX)));
				pw.print("name\tC11-1\tC11-2\tC12-1\tC12-2\tC13\t");
				for(int i=101;i<=128;i++) pw.print("C" + i + "\t");
				pw.println();
				pw.flush();
				pw.close();
			}
			if(mode.equals("GUI") || !new File(outdir+"/" + CONAFILE + SDFILE + DATAFILESUFFIX ).exists()) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/"  + CONAFILE + SDFILE + DATAFILESUFFIX )));
				pw.print("name\tC11-1\t#\tC11-2\t#\tC12-1\t#\tC12-2\t#\tC13\t#\t");
		        for(int i=101;i<=118;i++) pw.print("C" + i + "\t#\t");
    		    pw.print("C126\t#\tC127\t#\tC128\t#\t");
    		    pw.println();
				pw.flush();
				pw.close();
			}
			if(calA && (mode.equals("GUI") || !new File(outdir+ "/" + ACTINFILE + SUMMARYFILE + DATAFILESUFFIX).exists())) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/" + ACTINFILE + SUMMARYFILE + DATAFILESUFFIX)));
				pw.print("name\tA7-1\tA7-2\tA8-1\tA8-2\tA9\t");
				for(int i=101;i<=123;i++) pw.print("A" + i + "\t");
				pw.println();
				pw.flush();
				pw.close();
			}
			if(calA && (mode.equals("GUI") || !new File(outdir+"/" + ACTINFILE + SDFILE + DATAFILESUFFIX ).exists())) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/" + ACTINFILE + SDFILE + DATAFILESUFFIX )));
				pw.print("name\tA7-1\t#\tA7-2\t#\tA8-1\t#\tA8-2\t#\tA9\t#\t");
    		    pw.print("A101\t#\tA102\t#\tA103\t#\tA104\t#\tA120\t#\tA121\t#\tA122\t#\tA123\t#\t");
    		    pw.println();
				pw.flush();
				pw.close();
			}
			if(calD && (mode.equals("GUI") || !new File(outdir+"/" + DAPIFILE + SUMMARYFILE + DATAFILESUFFIX).exists())) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/" + DAPIFILE + SUMMARYFILE + DATAFILESUFFIX)));
				pw.print("name\tD14-1\tD14-2\tD14-3\tD15-1\tD15-2\tD15-3\tD16-1\tD16-2\tD16-3\tD17-1\tD17-2\tD17-3\t");
				for(int i=101;i<=216;i++) pw.print("D" + i + "\t");
				pw.println();
				pw.flush();
				pw.close();
			}
			if(calD && (mode.equals("GUI") || !new File(outdir+"/"+ DAPIFILE + SDFILE + DATAFILESUFFIX).exists())) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"/" + DAPIFILE + SDFILE + DATAFILESUFFIX)));
				pw.print("name\tD14-1\t#\tD14-2\t#\tD14-3\t#\tD15-1\t#\tD15-2\t#\tD15-3\t#\tD16-1\t#\tD16-2\t#\tD16-3\t#\tD17-1\t#\tD17-2\t#\tD17-3\t#\t");
				for(int i=101;i<=198;i++) pw.print("D" + i + "\t#\t");
    		    pw.println();
				pw.flush();
				pw.close();
			}
			}
		} catch(Exception e) {
			System.err.println("AllProcess.setPrintFile():"+e);
		}
	}
	
	private void printOneLine(PrintWriter pw, String[] strary, String separator) {
		int length = strary.length;
		for( int i = 0; i < strary.length - 1; i++ ) {
			pw.print(strary[i] + "\t");
		}
		pw.println(strary[length - 1]);
	}
}

//--------------------------------------
//$Log: DisruptantProcess.java,v $
//Revision 1.13  2004/09/06 14:25:08  sesejun
//*** empty log message ***
//
//Revision 1.12  2004/09/06 13:44:06  sesejun
//CalMorphのおそらく正しい1_0のソース
//
//Revision 1.11  2004/07/29 04:27:38  sesejun
//dapi_basic のheaderがないバグを修正
//
//Revision 1.10  2004/07/29 04:03:58  sesejun
//actin_biologicalのheader出力忘れを修正
//
//Revision 1.9  2004/07/29 03:10:49  sesejun
//remove bug of cona biological filename
//
//Revision 1.8  2004/07/29 02:33:02  sesejun
//各cellの座標の表示を行っていなかったのを追加。
//
//Revision 1.7  2004/07/01 10:23:29  sesejun
//表示部と計算部をある程度分離
//
//Revision 1.6  2004/06/30 17:14:21  sesejun
//計算部分と出力部分の分離。
//エラーを吐く際の
//
//Revision 1.5 2004/06/25 01:42:05 sesejun
//CalMorph_version_1.0相当へ変更
//一部、変更が元にもどってしまっている可能性大
//気がついたら、再度変更してください。
//
//Revision 1.4 2004/06/24 02:06:42 sesejun
//- Headerを変数からよみこむように変更
//- 染色画像読み込みに失敗した場合のエラーハンドリングを変更
//
//Revision 1.3 2004/06/09 10:17:40 sesejun
//process() でスタックトレースの表示を追加
//
//Revision 1.2 2004/06/01 12:43:30 nakatani
//ファイル名変更（data.xls-->versatile.xls, SDdata.xls-->versatile_SD.xls）
//
