package lab.cb.scmd.calmorph;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GUIFrame extends JFrame {
    JTextField out_jtf,state_jtf;
    JTextArea filename_jta;
    JButton reset_btn;
    JCheckBox outimage_chb,procD_chb,procA_chb,objectsave_chb;
    JRadioButton allsheet, versonly;
    ButtonGroup outsheet;
    JComboBox objectload_cob,maximage_cob;
    Vector jcoms;
    boolean flag_reset;
    File out;
    File[] files;
    
    public GUIFrame() {
        addWindowListener(createClosingListener()); 
        jcoms = new Vector();
        flag_reset = false;
        setTitle("CalMorph");
        setBounds(0,0,600,400);
        getContentPane().setLayout(new GridLayout(1,2));
        setControlFrame();
    }
    public void setControlFrame() {
        JPanel hor_jp,bor_jp,ver_jp;
        JLabel jl;
        JComboBox cob;
        JButton jb;

        JPanel right_jp = new JPanel();
        JPanel left_jp = new JPanel();
        right_jp.setLayout(new BoxLayout(right_jp,BoxLayout.Y_AXIS));
        left_jp.setLayout(new BoxLayout(left_jp,BoxLayout.Y_AXIS));
        
        //left_jpの準備
        bor_jp = new JPanel(new BorderLayout());
        bor_jp.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jl = new JLabel("input disruptants");
        hor_jp.add(jl);
        bor_jp.add(hor_jp,BorderLayout.NORTH);
        hor_jp = new JPanel();
        filename_jta = new JTextArea("");
        filename_jta.setEditable(false);
        JScrollPane jsp = new JScrollPane();
        jsp.getViewport().setView(filename_jta);
        jsp.setPreferredSize(new Dimension(200,200));
        hor_jp.add(jsp);
        setDefaultFiles();
        jb = createSelectButton();
        jcoms.add(jb);
        hor_jp.add(jb);
        bor_jp.add(hor_jp,BorderLayout.CENTER);
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        procD_chb = new JCheckBox("DAPI");
        jcoms.add(procD_chb);
        procD_chb.setSelected(true);
        hor_jp.add(procD_chb);
        procA_chb = new JCheckBox("actin");
        jcoms.add(procA_chb);
        procA_chb.setSelected(true);
        hor_jp.add(procA_chb);
        bor_jp.add(hor_jp,BorderLayout.SOUTH);
        left_jp.add(bor_jp);
        
        bor_jp = new JPanel(new BorderLayout());
        bor_jp.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jl = new JLabel("output");
        hor_jp.add(jl);
        bor_jp.add(hor_jp,BorderLayout.NORTH);
        ver_jp = new JPanel();
        ver_jp.setLayout(new BoxLayout(ver_jp,BoxLayout.Y_AXIS));
        hor_jp = new JPanel(new FlowLayout());
        out = new File("data");
        if(!out.exists()) out.mkdir();
        out_jtf = new JTextField(out.getAbsolutePath());
        out_jtf.setPreferredSize(new Dimension(200,20));
        out_jtf.setEditable(false);
        hor_jp.add(out_jtf);
        jb = createOutSelectButton();
        jcoms.add(jb);
        hor_jp.add(jb);
        ver_jp.add(hor_jp);
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
//////////////////////////////////////////////　内部用出力シート選択部分
        allsheet = new JRadioButton("all sheet");
        allsheet.setSelected(true);
        jcoms.add(allsheet);
        hor_jp.add(allsheet);
		versonly = new JRadioButton("versatile only");
        versonly.setSelected(false);
        jcoms.add(versonly);
        hor_jp.add(versonly);
        ver_jp.add(hor_jp);
        outsheet = new ButtonGroup();
        outsheet.add(allsheet);
        outsheet.add(versonly);
//////////////////////////////////////////////
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        outimage_chb = new JCheckBox("outimage");
        outimage_chb.setSelected(true);
        jcoms.add(outimage_chb);
        hor_jp.add(outimage_chb);
        ver_jp.add(hor_jp);

        bor_jp.add(ver_jp,BorderLayout.CENTER);
        left_jp.add(bor_jp);
        
        
        //right_jpの準備
        bor_jp = new JPanel(new BorderLayout());
        bor_jp.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jl = new JLabel("options");
        hor_jp.add(jl);
        bor_jp.add(hor_jp,BorderLayout.NORTH);
        ver_jp = new JPanel();
        ver_jp.setLayout(new BoxLayout(ver_jp,BoxLayout.Y_AXIS));
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        /*objectsave_chb = new JCheckBox("objectsave");
        objectsave_chb.setSelected(false);
        hor_jp.add(objectsave_chb);
        jcoms.add(objectsave_chb);
        ver_jp.add(hor_jp);
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jl = new JLabel("  objectload");
        hor_jp.add(jl);
        String[] s = new String[4];
        for(int i=0;i<4;i++) {
            s[i] = new String(""+(i-1));
        }
        objectload_cob = new JComboBox(s);
        objectload_cob.setSelectedIndex(0);
        objectload_cob.setEditable(true);
        jcoms.add(objectload_cob);
        hor_jp.add(objectload_cob);
        ver_jp.add(hor_jp);*/
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jl = new JLabel("  maximage");
        hor_jp.add(jl);
        String[] s = new String[10];
        for(int i=0;i<10;i++) {
            s[i] = new String(""+((i+1)*50));
        }
        maximage_cob = new JComboBox(s);
        maximage_cob.setSelectedIndex(3);
        maximage_cob.setEditable(true);
        jcoms.add(maximage_cob);
        hor_jp.add(maximage_cob);
        ver_jp.add(hor_jp);
        bor_jp.add(ver_jp,BorderLayout.CENTER);
        
        right_jp.add(bor_jp);
        bor_jp = new JPanel(new BorderLayout());
        bor_jp.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        hor_jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jl = new JLabel("status");
        hor_jp.add(jl);
        bor_jp.add(hor_jp,BorderLayout.NORTH);
        ver_jp = new JPanel();
        ver_jp.setLayout(new BoxLayout(ver_jp,BoxLayout.Y_AXIS));
        hor_jp = new JPanel(new FlowLayout());
        jb = createRunButton();
        jcoms.add(jb);
        hor_jp.add(jb);
        reset_btn = createResetButton();
        reset_btn.setEnabled(false);
        hor_jp.add(reset_btn);
        hor_jp.add(createQuitButton());
        ver_jp.add(hor_jp);
        hor_jp = new JPanel(new FlowLayout());
        state_jtf = new JTextField("parameter setting･･･");
        state_jtf.setPreferredSize(new Dimension(200,20));
        state_jtf.setEditable(false);
        hor_jp.add(state_jtf);
        ver_jp.add(hor_jp);
        bor_jp.add(ver_jp,BorderLayout.CENTER);
        right_jp.add(bor_jp);
        
        getContentPane().add(left_jp);
        getContentPane().add(right_jp);
    }
    public WindowListener createClosingListener() {
        WindowListener wl = new WindowListener() {
            public void windowActivated(WindowEvent e) {}
            public void windowClosed(WindowEvent e) {}
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
            public void windowDeactivated(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowOpened(WindowEvent e) {}
        };
        return wl;
    }
    public JButton createRunButton() {
        Action a = new AbstractAction("run") {
            public void actionPerformed(ActionEvent e) {
                flag_reset = false;
                for(int j=0;j<jcoms.size();j++) {
                    ((JComponent)jcoms.get(j)).setEnabled(false);
                }
                state_jtf.setText("run");
                while(!state_jtf.getText().equals("run"));
                start();
                reset_btn.setEnabled(true);
            }
        };
        return new JButton(a);
    }
    public void start() {
        String outdir = out.getAbsolutePath();
        int maximage = Integer.parseInt((String)maximage_cob.getSelectedItem());
        boolean objectsave = false;//objectsave_chb.isSelected();
        int objectload = -1;//Integer.parseInt((String)objectload_cob.getSelectedItem());
        boolean calD = procD_chb.isSelected();
        boolean calA = procA_chb.isSelected();
        boolean outsheet = allsheet.isSelected();
        //boolean outsheet = true;
        boolean outimage = outimage_chb.isSelected();
        AllProcess ap = new AllProcess(files,outdir,maximage,objectsave,objectload,this,calD,calA,outimage,outsheet);
        ap.start();
    }
    public JButton createResetButton() {
        Action a = new AbstractAction("reset") {
            public void actionPerformed(ActionEvent e) {
                state_jtf.setText("please wait･･･");
                //System.out.println("please wait･･･");
                flag_reset = true;
            }
        };
        return new JButton(a);
    }
    public JButton createQuitButton() {
        Action a = new AbstractAction("quit") {
            public void actionPerformed(ActionEvent e) {
                 state_jtf.setText("quit");
                 //System.out.println("quit");
                 System.exit(0);
            }
        };
        return new JButton(a);
    }
    public JButton createSelectButton() {
        Action a = new AbstractAction("･･･") {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(".");
                fc.setAcceptAllFileFilterUsed(false);
                javax.swing.filechooser.FileFilter fil = new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }
                    public String getDescription() {
                        return "directories";
                    }
                };
                fc.setFileFilter(fil);
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setMultiSelectionEnabled(true);
                int result = fc.showOpenDialog(null);
                if(result == JFileChooser.APPROVE_OPTION) {
                    files = fc.getSelectedFiles();
                    filename_jta.setText("");
                    for(int i=0;i<files.length;i++) {
                         filename_jta.append(files[i].getName()+"\n");
                         //System.out.println(files[i].getAbsolutePath());
                    }
                }
            }
        };
        return new JButton(a);
    }
    public JButton createOutSelectButton() {
        Action a = new AbstractAction("･･･") {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(".");
                fc.setAcceptAllFileFilterUsed(false);
                javax.swing.filechooser.FileFilter fil = new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }
                    public String getDescription() {
                        return "directorie";
                    }
                };
                fc.setFileFilter(fil);
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fc.showOpenDialog(null);
                if(result == JFileChooser.APPROVE_OPTION) {
                    out = fc.getSelectedFile();
                    out_jtf.setText(out.getPath());
                }
            }
        };
        return new JButton(a);
    }
    public void setDefaultFiles() {
        File f = new File("image");
        filename_jta.setText("");
        if(f.exists() && f.isDirectory()) {
            files = f.listFiles();
            for(int i=0;i<files.length;i++) {
                filename_jta.append(files[i].getName()+"\n");
            }
        } else {
            files = null;
        }
    }
}
