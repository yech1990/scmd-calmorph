package lab.cb.scmd.calmorph;

import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;

/**
 * 
 * CalMorph DisruptantProcess
 * Version that can specify multiple files?
 * 
 * @author mattun
 *
 */
class AllProcess extends Thread {
    File[] files;
    String outdir;
    int maximage;
    GUIFrame gui;
    boolean objectsave,calD,calA,outimage,outsheet;
    int objectload;
    
    public AllProcess(File[] files,String outdir,int maximage,boolean objectsave,int objectload,GUIFrame gui,boolean calD,boolean calA,boolean outimage,boolean outsheet) {
        this.files = files;
        this.outdir = outdir;
        this.maximage = maximage;
        this.objectsave = objectsave;
        this.objectload = objectload;
        this.gui = gui;
        this.calD = calD;
        this.calA = calA;
        this.outimage = outimage;
        this.outsheet = outsheet;
    }
    public void run() {
        for(int i=0;i<files.length;i++) {
            String name = files[i].getName();
            String path = files[i].getAbsolutePath();
            DisruptantProcess dp = new DisruptantProcess(name,path,outdir,null,maximage,objectsave,objectload,gui,false,calD,calA,outimage,outsheet);
			if(i==0) dp.setPrintFile("GUI");
            try
            {
                dp.process();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if(gui.flag_reset) reset();
        }
        gui.state_jtf.setText("finish");
        reset();
    }
    public void reset() {
        for(int j=0;j<gui.jcoms.size();j++) {
            ((JComponent)gui.jcoms.get(j)).setEnabled(true);
        }
        gui.setDefaultFiles();
        gui.reset_btn.setEnabled(false);
    }
}
