package lab.cb.scmd.util.staff;

import org.apache.commons.httpclient.HttpException;
import org.apache.util.HttpURL;
import org.apache.webdav.lib.WebdavResource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.swing.plaf.basic.BasicDirectoryModel;
//import javax.swing.filechooser.*;
//import java.util.*;
/*
 * @author Leo
 *
 * 機能
 * 1. MetaMorphから生成される写真を、(ORF)-A1.jpgの形式にrename
 * 2. 3枚の写真をそれぞれ色調を変えて合成したものの横にチェックボックスをつけて、
 * 	  この写真をつかうかどうかを入力してもらう。
 * 	  (写真の合成にはImageMagick(外部プログラム)を使う)
 * 3. WebDAVプロトコルを用いて、yeastに写真をuploadする。
 * 	  (Slide http://jakarta.apache.org/ に含まれているWebDav cliant libraryを使って実現)
 */

/*
 * @translator 叶畅
 *
 * 功能
 * 1. 从的MetaMorph产生的照片，重命名的形式（ORF）-A1.jpg
 * 2. 复选框3张照片下一而是分别通过改变颜色合成，
 * 提示输入是否使用这张照片。
 *（ImageMagick的对于照片的合成（使用外部程序））
 * 3，WebDAV协议，上传照片酵母。
 *（使用被包含在幻灯片http://jakarta.apache.org/ WebDAV的cliant库中实现）
 */

public class SCMDConverter extends JFrame implements ActionListener {

    final static public int P_SCMD_ROOT = 0;
    final static public int P_PHOTO_DIR = 1;
    final static public int P_IRFAN_VIEW = 2;
    final static public int P_IMAGE_MAGICK = 3;
    //static String username2 = "bird";
    final static public int P_CONVERT = 4;
    final static public int P_MOGRIFY = 5;
    final static public int P_ORF_TABLE = 6;
    final static public int P_PLATE_TABLE = 7;
    public static String[] PropertyNames =
            {"SCMD_ROOT", "PHOTO_DIR", "IRFAN_VIEW",
                    "IMAGE_MAGICK", "CONVERT", "MOGRIFY",
                    "ORF_TABLE", "PLATE_TABLE"};
    public static String[] UserProperties;
    //--------------------------------------------------------------------------------
    // ORF_QUERY: ORFを入手するCGIページ
    //
    // http://yeast.gi.k.u-tokyo.ac.jp/tools/get_orf.cgi?name=his3 にアクセスすると、
    // plain textで、
    //
    // yor203w	his3
    //
    // という一行だけのページが表示される
    // ORFが見つからない場合は、
    //
    // none
    //
    // ORFを一意に特定できない場合は、
    //
    // multiple
    //
    // が表示される。
    //--------------------------------------------------------------------------------
    //static String SCMD_ROOT = "http://yeast.gi.k.u-tokyo.ac.jp/";
    //static String SCMD_ROOT2 = "http://bird.gi.k.u-tokyo.ac.jp/";
    static String ORF_QUERY; // = SCMD_ROOT + "tools/get_orf.cgi?name=";
    static String PHOTO_FOLDER;// = "staff/photo";
    //static String PHOTO_FOLDER2 = "staff/images/new images/auto";
    static String SERVER; // = SCMD_ROOT + PHOTO_FOLDER;
    //static String SERVER2 = SCMD_ROOT2 + PHOTO_FOLDER2;
    static String username = "birdstaff";
    static String password;
    static JFrame passFrame;
    static String ORF_DESCRIPTIONS; // = "." + SLA + "orf_descriptions.tab";
    static String PLATE_TABLE; // = "." + SLA + "platetable.tab";
    static String IRFAN_VIEW; // = "." + SLA + "i_view32" + SLA + "i_view32.exe";
    static String CONVERT; // = "C:" + SLA + "Program Files" + SLA + "ImageMagick-5.5.6-Q16" + SLA + "convert.exe";
    static String MOGRIFY; // = "C:" + SLA + "Program Files" + SLA + "ImageMagick-5.5.6-Q16" + SLA + "mogrify.exe";
    static JRadioButton onlineButton;
    static JRadioButton offlineButton;
    //final String CONVERT = "convert.exe";
    //final String MOGRIFY = "mogrify.exe";
    static JLabel label;
    static boolean online;
    // Actin, ConA, DAPIのための配列
    public final String[] photoCategory = {"A", "C", "D"};
    public final String[] photoClassifier = {"Rh", "FITC", "DAPI"};
    public final int ACTINE = 0;
    public final int CON_A = 1;
    public final int DAPI = 2;
    final int MENU_FILE = 0;
    final int MENU_CONVERT = 1;
    // WindowsとLinuxでパスの区切り文字が違うので、File.separatorを使う
    final String SLA = File.separator;
    /*final String ROOTDIR =
        "D:"
            + SLA
            + "cygwin"
            + SLA
            + "home"
            + SLA
            + "leo"
            + SLA
            + "gi"
            + SLA
            + "bird"
            + SLA
            + "img";*/
    final String ROOTDIR = "C:" + SLA;
    // GUIでボタンクリックが起こったときのコマンド名
    final String EXIT = "exit";
    final String DIR_SELECT = "dir select";
    final String BUDIR_SELECT = "backupdir select";
    final String HELP = "help";
    final String ORF = "ORF";
    final String DIR_CHANGE = "dir change";
    final String BUDIR_CHANGE = "backupdir change";
    final String RENAME = "rename";
    final String SELECT_PHOTO = "select photo";
    final String SUBMIT = "submit";
    // 顕微鏡の写真ファイルをmatchするための正規表現
    final String FILE_PATTERN =
            "([1-9][0-9]*)_w[1-3](FITC|Rh|DAPI)"
                    + "(_s([1-9][0-9]*))?"
                    + ".(jpg|JPG|tif|TIF)";
    final int FP_FIRST_PHOTO_NUM = 1;
    final int FP_WAVE_TYPE = 2;
    final int FP_SECOND_PHOTO_NUM = 4;
    final int FP_PHOTO_TYPE = 5;
    JMenuBar menuBar;
    JFileChooser fc, fc2;
    String newline = "\n";
    String[] menuList;
    JMenu[] menu;
    JMenu help_menu;
    JButton dirSelectButton;
    JTextField inputDir;
    JRadioButton manipButton, autoButton, localButton, plateButton;
    JFrame plateFrame;
    JTextField plateField1, plateField2, plateField3;
    String[] plateposition;
    JTextField backupDir;
    JButton budirSelectButton;
    JButton selectButton, submitButton, renameButton;
    JTextField orfField; // ORFのテキストボックス
    JTextField geneNameField; // Standard Nameのテキストボックス
    JLabel orfMessage;
    JButton orfGetButton;
    JFrame confirmFrame;
    JSpinner firstNumber;
    SpinnerNumberModel sNm;
    JProgressBar progress;
    JFrame progressFrame;
    JFrame settingFrame;
    JCheckBox[] usecheck;
    JFrame usecheckFrame;
    int unusefileNum;
    int imgfilenum;
    JSpinner Currentspinner;
    int counter;
    String[] oldfiles;
    String[] newfiles;
    PhotoGroup[] pgs;
    PhotoGroup[] pgs2;
    boolean isReadyORF = false; // ORFがわかっているかどうか
    int currentimage;
    int currentkind;
    boolean renamed = false;
    String imagedir;
    Image[] img;
    SpinnerNumberModel sNm2;
    JFrame photoselectFrame;
    Canvas photocanvas;
    JLabel photoLabel;
    JRadioButton conAButton, ActinButton, DAPIButton, compositeButton;
    JCheckBox checkUseornot;

    public SCMDConverter(char[] pswd) {
        super("SCMD Toolkit");
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuList = new String[]{"File"};

        // menubar
        menu = new JMenu[menuList.length];
        for (int i = 0; i < menuList.length; i++) {
            menu[i] = menuBar.add(new JMenu(menuList[i]));
        }
        // help menu
        menuBar.add(Box.createHorizontalGlue());
        help_menu = menuBar.add(new JMenu("Help"));

        // sub menu
        JMenuItem mi1, mi2, help_submenu;
        mi1 = new JMenuItem("Open Directory");
        mi1.setActionCommand(DIR_SELECT);
        mi1.addActionListener(this);

        mi2 = new JMenuItem("Exit");
        mi2.setActionCommand(EXIT);
        mi2.addActionListener(this);

        help_submenu = new JMenuItem("使い方");
        help_submenu.setActionCommand(HELP);
        help_submenu.addActionListener(this);

        menu[MENU_FILE].add(mi1);
        menu[MENU_FILE].addSeparator();
        menu[MENU_FILE].add(mi2);
        help_menu.add(help_submenu);

        // directory button
        JPanel directorySelectPane = new JPanel();
        //directorySelectPane.setLayout(
        //	new BoxLayout(directorySelectPane, BoxLayout.X_AXIS));

        inputDir = new JTextField(ROOTDIR, 35);
        inputDir.setActionCommand(DIR_CHANGE);
        inputDir.addActionListener(this);
        setPanelSize(directorySelectPane, new Dimension(480, 65));

        dirSelectButton = new JButton("...");
        dirSelectButton.setActionCommand(DIR_SELECT);
        dirSelectButton.addActionListener(this);

        directorySelectPane.add(inputDir);
        directorySelectPane.add(dirSelectButton);
        directorySelectPane.setBorder(
                BorderFactory.createTitledBorder("Source Directory"));

		/*
		directorySelectPane.setBorder(BorderFactory.createCompoundBorder(
						   BorderFactory.createLineBorder(Color.DARK_GRAY),
						   directorySelectPane.getBorder()));
						   */


        JPanel backupDirectoryPane = new JPanel();

        backupDir = new JTextField(ROOTDIR, 35);
        backupDir.setActionCommand(BUDIR_CHANGE);
        backupDir.addActionListener(this);
        setPanelSize(backupDirectoryPane, new Dimension(480, 65));

        budirSelectButton = new JButton("...");
        budirSelectButton.setActionCommand(BUDIR_SELECT);
        budirSelectButton.addActionListener(this);

        backupDirectoryPane.add(backupDir);
        backupDirectoryPane.add(budirSelectButton);
        backupDirectoryPane.setBorder(
                BorderFactory.createTitledBorder("Backup Directory"));


        // ORF radio button
        // -- ORFをどのように取得するかを選択するボタン
        manipButton = new JRadioButton("manual");
        autoButton = new JRadioButton("auto (from web)");
        autoButton.setSelected(true);
        localButton = new JRadioButton("auto (from local file)");
        //localButton.setEnabled(false);
        plateposition = new String[3];
        plateButton = new JRadioButton("from plate position");
        ButtonGroup group = new ButtonGroup();
        group.add(autoButton);
        group.add(localButton);
        group.add(manipButton);
        group.add(plateButton);
        JLabel mLabel = new JLabel("ORF naming:");

        // 各radio buttonを押したときのAction
        manipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                orfField.setEditable(true);
                orfMessage.setText("input ORF by hand");
                orfGetButton.setEnabled(false);
            }
        });
        autoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isReadyORF = false;
                //selectButton.setEnabled(false);
                submitButton.setEnabled(false);
                //autoButton.setSelected(true);
                retrieveORF();
                orfField.setEditable(false);
                orfGetButton.setEnabled(true);
            }
        });
        localButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isReadyORF = false;
                //selectButton.setEnabled(false);
                submitButton.setEnabled(false);
                //localButton.setSelected(true);
                retrieveORF();
                orfField.setEditable(false);
                orfGetButton.setEnabled(true);
            }
        });
        plateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isReadyORF = false;
                submitButton.setEnabled(false);
                orfField.setEditable(false);
                plateFrame = new JFrame("Position on plates");
                JPanel platePane1 = new JPanel();
                JLabel platelabel1 = new JLabel("株のプレート上の位置を入力してください ");
                platePane1.add(platelabel1);
                JPanel platePane2 = new JPanel();
                JLabel platelabel2 = new JLabel("plate");
                plateField1 = new JTextField(6);
                plateField2 = new JTextField(2);
                JLabel platelabel3 = new JLabel("(alphabet)----");
                plateField3 = new JTextField(2);
                JLabel platelabel4 = new JLabel("(number)");
                platePane2.add(platelabel2);
                platePane2.add(plateField1);
                platePane2.add(plateField2);
                platePane2.add(platelabel3);
                platePane2.add(plateField3);
                platePane2.add(platelabel4);
                JPanel platePane3 = new JPanel();
                JButton plateButton = new JButton("OK");
                platePane3.add(plateButton);
                plateButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        plateposition[0] = plateField1.getText();
                        plateposition[1] = plateField2.getText();
                        plateposition[2] = plateField3.getText();
                        plateFrame.dispose();
                        retrieveORF();
                        orfGetButton.setEnabled(true);
                    }
                });
                JPanel contentPane = new JPanel(new BorderLayout());
                contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                contentPane.add(platePane1, BorderLayout.NORTH);
                contentPane.add(platePane2, BorderLayout.CENTER);
                contentPane.add(platePane3, BorderLayout.SOUTH);
                plateFrame.setContentPane(contentPane);
                plateFrame.pack();
                plateFrame.setVisible(true);

            }
        });


        JPanel orfSelectPane = new JPanel();
        setPanelSize(orfSelectPane, new Dimension(600, 30));
        orfSelectPane.add(mLabel);
        orfSelectPane.add(autoButton);
        orfSelectPane.add(localButton);
        orfSelectPane.add(manipButton);
        orfSelectPane.add(plateButton);

        // orf view panel
        geneNameField = new JTextField(new File(ROOTDIR).getName(), 10);
        geneNameField.setActionCommand(ORF);
        geneNameField.addActionListener(this);
        JLabel geneNameLabel = new JLabel("Standard Name (or ORF): ");
        geneNameLabel.setLabelFor(geneNameField);
        orfField = new JTextField(10);
        orfField.setEditable(false);
        JLabel orfLabel = new JLabel("ORF: ");
        geneNameLabel.setLabelFor(orfField);
        orfGetButton = new JButton("retrieve ORF");
        orfGetButton.setActionCommand(ORF);
        orfGetButton.addActionListener(this);
        orfGetButton.setAlignmentX(CENTER_ALIGNMENT);
        orfMessage = new JLabel("ORF is not retrieved yet");
        orfMessage.setForeground(new Color(60, 90, 255));
        orfMessage.setAlignmentX(CENTER_ALIGNMENT);
        JPanel orfPane = new JPanel();
        //orfPane.setAlignmentY(TOP_ALIGNMENT);
        JLabel[] labels = {geneNameLabel, orfLabel};
        JTextField[] textFields = {geneNameField, orfField};
        setPanelSize(orfPane, new Dimension(480, 40));
        orfPane.add(geneNameLabel);
        orfPane.add(geneNameField);
        orfPane.add(orfLabel);
        orfPane.add(orfField);

        JPanel orfPane2 = new JPanel();
        setPanelSize(orfPane2, new Dimension(480, 60));
        orfPane2.setLayout(new BoxLayout(orfPane2, BoxLayout.Y_AXIS));
        orfGetButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        orfMessage.setAlignmentX(JButton.CENTER_ALIGNMENT);
        orfPane2.add(orfGetButton);
        orfPane2.add(orfMessage);

        sNm = new SpinnerNumberModel(1, 1, 500, 1);
        firstNumber = new JSpinner(sNm);
        JLabel firstNumberLabel = new JLabel("First number of new name");
        firstNumberLabel.setLabelFor(firstNumber);
        JPanel fnPane = new JPanel();
        setPanelSize(fnPane, new Dimension(480, 40));
        fnPane.add(firstNumberLabel);
        fnPane.add(firstNumber);

        // Rename、select fileなどのbutton
        JPanel renamePanel = new JPanel();
        renameButton = new JButton("rename files");
        renameButton.setActionCommand(RENAME);
        renameButton.addActionListener(this);
        selectButton = new JButton("select photos");
        submitButton = new JButton("submit");
        selectButton.setActionCommand(SELECT_PHOTO);
        selectButton.addActionListener(this);
        //selectButton.setEnabled(false);
        submitButton.setActionCommand(SUBMIT);
        submitButton.addActionListener(this);
        submitButton.setEnabled(false);

        renamePanel.add(renameButton);
        renamePanel.add(selectButton);
        renamePanel.add(submitButton);

        // パネルを配置する
        Container c = this.getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(directorySelectPane);
        c.add(backupDirectoryPane);
        c.add(orfSelectPane);
        c.add(orfPane);
        c.add(orfPane2);
        c.add(fnPane);

        c.add(renamePanel);

        // mainのwindowが閉じるとプログラムを終了する
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

    }

    public static void main(String[] args) {
        UserProperties = new String[PropertyNames.length];
        // default values
        UserProperties[P_SCMD_ROOT] = "http://yeast.gi.k.u-tokyo.ac.jp/";
        UserProperties[P_PHOTO_DIR] = "staff/photo";
        UserProperties[P_IRFAN_VIEW] = "./i_view32/i_view32.exe";
        UserProperties[P_IMAGE_MAGICK] = "C:/Program Files/ImageMagick-5.5.6-Q16/";
        UserProperties[P_CONVERT] = "convert.exe";
        UserProperties[P_MOGRIFY] = "mogrify.exe";
        UserProperties[P_ORF_TABLE] = "orf_descriptions.tab";
        UserProperties[P_PLATE_TABLE] = "platetable.tab";

        if (args.length >= 1) {
            // load a setting file
            try {
                FileInputStream input = new FileInputStream(args[0]);
                Properties property = new Properties();
                property.load(input);

                for (int i = 0; i < PropertyNames.length; i++) {
                    String argument = property.getProperty(PropertyNames[i]);
                    if (argument != null)
                        UserProperties[i] = argument;
                }
                input.close();

            } catch (IOException e) {
                // cannot read file correctly
                System.out.println("invalid setting files");
            }
        }
        // set parameters
        PHOTO_FOLDER = UserProperties[P_PHOTO_DIR];
        ORF_QUERY = UserProperties[P_SCMD_ROOT] + "tools/get_orf.cgi?name=";
        SERVER = UserProperties[P_SCMD_ROOT] + UserProperties[P_PHOTO_DIR];
        ORF_DESCRIPTIONS = UserProperties[P_ORF_TABLE];
        PLATE_TABLE = UserProperties[P_PLATE_TABLE];
        IRFAN_VIEW = UserProperties[P_IRFAN_VIEW];
        CONVERT = UserProperties[P_IMAGE_MAGICK] + UserProperties[P_CONVERT];
        MOGRIFY = UserProperties[P_IMAGE_MAGICK] + UserProperties[P_MOGRIFY];

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        onlineButton = new JRadioButton("online");
        offlineButton = new JRadioButton("offline");
        ButtonGroup group = new ButtonGroup();
        onlineButton.setSelected(true);
        group.add(onlineButton);
        group.add(offlineButton);
        JPanel SelectPane = new JPanel();
        SelectPane.add(onlineButton);
        SelectPane.add(offlineButton);
        passFrame = new JFrame("Password");
        label = new JLabel("Enter the password: ");
        JPasswordField passwordField = new JPasswordField(10);
        passwordField.setEchoChar('*');
        passwordField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPasswordField input = (JPasswordField) e.getSource();
                char[] pswd = input.getPassword();
                password = new String(pswd);
                boolean check = true;
                online = false;
                if (onlineButton.isSelected()) {
                    online = true;
                    try {
                        HttpURL httpURL = new HttpURL(SERVER);
                        httpURL.setUserInfo(username, password);
                        WebdavResource wr = new WebdavResource(httpURL);
                        wr.close();
                    } catch (IOException e2) {
                        JOptionPane.showMessageDialog(null, "パスワードが違うかインターネット接続が正常でありません", "", JOptionPane.ERROR_MESSAGE);
                        check = false;
                    } catch (HttpException e2) {
                        JOptionPane.showMessageDialog(null, "パスワードが違うかインターネット接続が正常でありません", "", JOptionPane.ERROR_MESSAGE);
                        check = false;
                    }
                }
                if (check) {
                    passFrame.dispose();
                    SCMDConverter main_frame = new SCMDConverter(pswd);
                    main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    main_frame.setSize(620, 400);
                    main_frame.setVisible(true);
                }
            }
        });

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPane.add(SelectPane, BorderLayout.NORTH);
        contentPane.add(label, BorderLayout.WEST);
        contentPane.add(passwordField, BorderLayout.CENTER);

        passFrame.setContentPane(contentPane);
        passFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        passFrame.pack();
        passFrame.setVisible(true);
    }

    void displayErrorDialog(Exception e) {
        JOptionPane.showMessageDialog(null, "Exception", e.getMessage(), JOptionPane.ERROR_MESSAGE);
    }

    // GUIでパネルのサイズを固定しまうための関数
    public void setPanelSize(JPanel pane, Dimension dim) {
        pane.setMaximumSize(dim);
        pane.setMinimumSize(dim);
        pane.setPreferredSize(dim);
    }

    // ボタンが押されたときの処理
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case EXIT:
                // exit
                System.exit(0);
            case DIR_SELECT: {
                // select directory
                fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setCurrentDirectory(new File(inputDir.getText()));
                int ret = fc.showOpenDialog(SCMDConverter.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File dir = fc.getSelectedFile();
                    inputDir.setText(dir.getAbsolutePath());
                    geneNameField.setText(dir.getName());
                    isReadyORF = false;
                    //selectButton.setEnabled(false);
                    submitButton.setEnabled(false);
                    retrieveORF();
                    renamed = false;
                }
                break;
            }
            case BUDIR_SELECT: {
                // select directory
                fc2 = new JFileChooser();
                fc2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc2.setCurrentDirectory(new File(backupDir.getText()));
                int ret = fc2.showOpenDialog(SCMDConverter.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File dir = fc2.getSelectedFile();
                    backupDir.setText(dir.getAbsolutePath());
                }
                break;
            }
            case HELP:
                // display help message
                JFrame helpframe = new HelpFrame();
                JTextArea helpMessage = new JTextArea();
                helpMessage.setLineWrap(true);
                helpMessage.setWrapStyleWord(true);
                helpMessage.setEditable(false);
                helpMessage.setText(
                        "SCMD toolkitの使い方"
                                + newline
                                + newline
                                + "1. 画像ファイルの入っているフォルダとRename後のファイルを入れるバックアップフォルダを選択"
                                + newline
                                + "2. 画像ファイルの入っているフォルダ名が株のStandard nameもしくはORFではない場合、Standard name(or ORF)の項目に入力"
                                + newline
                                + "3. 付け替える名前の検索方法を選択（ネット(yeastサーバ)から検索 or ローカルファイル(orf_descriptions.tab)から検索　or 手動で入力)"
                                + newline
                                + "4. 名前が確定したらFirst number of new nameに付け替えるファイル名の番号を何番からにするかを入力してrename filesを選択"
                                + newline
                                + "5. 使用しない画像のチェックボックスを外してからRenameを選択"
                                + newline
                                + "6. rename logを確認して、問題がなければRename Executeを選択"
                                + newline
                                + "7. ファイル名が変換されてバックアップフォルダに入れられ、元のフォルダが削除される"
                                + newline
                                + "8. 使用しないとされた画像はunusableという名前のフォルダの中に入れられる"
                                + newline
                                + "9. もし必要ならば、さらにselect photoでそれぞれの画像を使用するかどうかを画像を見て選ぶ"
                                + newline
                                + "10. submitを選択"
                                + newline
                                + "11. yeastサーバーに画像ファイルとログファイルが送られる(unusableとその中身も)"
                                + newline
                                + newline
                                + "注：submitで送られるファイルは前回のrenameで名前が付け替えられたファイル"
                                + newline
                                + newline
                                + "すでに存在するフォルダに画像を送るとき、送りたい画像と同名の画像が送り先にすでに存在した場合は、その送り先の画像が以前このプログラムによって（エラーなしに）正常に送られたものならば上書きせず、そうでなければ上書きする"
                                + newline
                                + newline
                                + "submit時にネットワークのエラーによって送信が中断されたときはもう一度submitする"
                                + newline
                                + newline
                                + "submit終了時には画像ファイルが入っているこちら側のディレクトリにもサーバー側にあるlogのコピーができる。（名前はrename_log）この内容はテキストエディタで確認できる。形式は「元のファイル名」「付け替えたファイル名」「サーバーにアップされた時間」で、今までサーバー側のそのフォルダに送られたすべてのファイルの記録が書いてある");

                JScrollPane helpScrollPane = new JScrollPane(helpMessage);
                helpScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                Container contentPane = helpframe.getContentPane();
                contentPane.add(helpScrollPane);
                helpframe.setSize(new Dimension(400, 400));
                helpframe.setVisible(true);
                break;
            case ORF:
                // ORFを取得する
                isReadyORF = false;
                //selectButton.setEnabled(false);
                submitButton.setEnabled(false);
                retrieveORF();
                break;
            case DIR_CHANGE:
                File dir = new File(inputDir.getText());
                geneNameField.setText(dir.getName());
                isReadyORF = false;
                //selectButton.setEnabled(false);
                submitButton.setEnabled(false);
                retrieveORF();
                renamed = false;
                break;
            case RENAME:
                // file名をrenameする
                if (!retrieveORF()) {
                    // show an error dialog
                    JOptionPane.showMessageDialog(
                            this,
                            "ORFを決定してください。",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    // 顕微鏡のファイルをsortする
                    File path = new File(inputDir.getText());
                    String[] ls = path.list();
                    Pattern p = Pattern.compile(FILE_PATTERN);
                    String[] newName = new String[ls.length];

                    // PhotoNumComparetorで定義される順序でsortされた
                    // PhotoNumクラスの集合 -> PhotoGroup というMap
                    TreeMap fileMap = new TreeMap(new PhotoNumComparator());
                    imgfilenum = 0;
                    for (int i = 0; i < ls.length; i++) {
                        Matcher m = p.matcher(ls[i]);
                        if (m.matches()) {
                            imgfilenum++;
                            // ファイルがMetaMorphのパターンにマッチしている場合
                            for (int j = 1; j <= m.groupCount(); j++) {
                                PhotoNum pn;
                                // FP_FIRST_PHOTO_NUMの部分と、FP_SECOND_PHOTO_NUMの合成数
                                if (m.group(FP_SECOND_PHOTO_NUM) == null) {
                                    // 手動の顕微鏡でとられた写真
                                    pn =
                                            new PhotoNum(
                                                    new Integer(m.group(FP_FIRST_PHOTO_NUM)).intValue(),
                                                    -1);
                                } else {
                                    // 自動顕微鏡でとられた写真
                                    pn =
                                            new PhotoNum(
                                                    new Integer(m.group(FP_FIRST_PHOTO_NUM)).intValue(),
                                                    new Integer(m.group(FP_SECOND_PHOTO_NUM)).intValue());
                                }
                                // search treemap
                                PhotoGroup pg;
                                if (fileMap.containsKey(pn)) {
                                    // 既に同じ番号のPhotoGroupが存在する場合
                                    pg = (PhotoGroup) fileMap.get(pn);
                                    fileMap.remove(pn);
                                } else {
                                    pg = new PhotoGroup();
                                }
                                pg.add(ls[i], m.group(2));
                                fileMap.put(pn, pg);
                            }
                        }
                    }

                    Collection c = fileMap.values();
                    Iterator ci = c.iterator();
                    pgs = new PhotoGroup[c.size()];

                    int count = 0;
                    while (ci.hasNext()) {
                        PhotoGroup pg = (PhotoGroup) ci.next();
                        pgs[count] = pg;
                        count++;
                    }

                    JLabel checklabel = new JLabel("Remove checks of unusable images.");
                    JPanel checklabelPane = new JPanel();
                    setPanelSize(checklabelPane, new Dimension(470, 40));
                    checklabelPane.add(checklabel);
                    usecheck = new JCheckBox[count];
                    JPanel checkPane = new JPanel();
                    setPanelSize(checkPane, new Dimension(470, 30 * count));
                    for (int i = 0; i < count; i++) {
                        String orifile = "";
                        for (int j = 0; j < 3; j++) {
                            if (pgs[i].origFile[j] != null) {
                                orifile += pgs[i].origFile[j] + " ";
                            }
                        }
                        usecheck[i] = new JCheckBox(orifile, pgs[currentimage].useornot);
                        usecheck[i].setActionCommand(Integer.toString(i));
                        usecheck[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e1) {
                                pgs[Integer.parseInt(e1.getActionCommand())].useornot = usecheck[Integer.parseInt(e1.getActionCommand())].isSelected();
                            }
                        });
                        checkPane.add(usecheck[i]);
                    }
                    JScrollPane cs = new JScrollPane(checkPane);
                    cs.setVerticalScrollBarPolicy(
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    cs.setPreferredSize(new Dimension(470, 200));

                    usecheckFrame = new UsecheckFrame();
                    Container usecheckPane = usecheckFrame.getContentPane();
                    usecheckFrame.setSize(500, 347);

                    JButton checkokButton = new JButton("Rename");
                    JButton checkcancelButton = new JButton("Cancel");
                    JPanel checkbuttonPane = new JPanel();
                    setPanelSize(checkbuttonPane, new Dimension(470, 40));
                    checkbuttonPane.add(checkokButton);
                    checkbuttonPane.add(checkcancelButton);
                    checkcancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e1) {
                            usecheckFrame.dispose();
                        }
                    });

                    checkokButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e1) {
                            usecheckFrame.dispose();
                            int fileNum = sNm.getNumber().intValue();
                            unusefileNum = 1;
                            String filePrefix = orfField.getText();
                            Vector files = new Vector();
                            counter = 0;
                            oldfiles = new String[imgfilenum];
                            newfiles = new String[imgfilenum];
                            String renameText = "";        // file名をどのように変更するかを表示するためのHTML
                            renameText += "<html><center>"
                                    + "rename log"
                                    + "<table>"
                                    + "<tr bgcolor=white><td width=110>Original</td>"
                                    + "<td width=25></td><td width=110>New</td></tr>";
                            for (int j = 0; j < pgs.length; j++) {
                                if (pgs[j].useornot) {
                                    for (int i = 0; i < 3; i++) {
                                        if (pgs[j].origFile[i] != null) {
                                            String newFile =
                                                    filePrefix
                                                            + "-"
                                                            + photoCategory[i]
                                                            + fileNum
                                                            + ".jpg";
                                            pgs[j].addNewFile(newFile, photoClassifier[i]);
                                            files.add(pgs[j]);
                                            renameText += "<tr><td>"
                                                    + pgs[j].origFile[i]
                                                    + "</td><td> -- </td><td><font color=#7070FF>"
                                                    + pgs[j].newFile[i]
                                                    + "</font></td></tr>";
                                            oldfiles[counter] = pgs[j].origFile[i];
                                            newfiles[counter] = pgs[j].newFile[i];
                                            counter++;
                                        }
                                    }
                                    fileNum++;
                                    renameText += "<tr><td> </td></tr>";
                                } else {
                                    for (int i = 0; i < 3; i++) {
                                        if (pgs[j].origFile[i] != null) {
                                            String newFile =
                                                    filePrefix
                                                            + "-"
                                                            + "unusable"
                                                            + "-"
                                                            + photoCategory[i]
                                                            + unusefileNum
                                                            + ".jpg";
                                            pgs[j].addNewFile(newFile, photoClassifier[i]);
                                            files.add(pgs[j]);
                                        }
                                    }
                                    unusefileNum++;
                                }
                            }
                            renameText += "</table></center></html>";

                            // どのようにrenameされるか確認するpaneを作成
                            JLabel renameListText = new JLabel(renameText);
                            //renameListText.setFont(new Font("Arial", Font.BOLD, 12));
                            JScrollPane js = new JScrollPane(renameListText);
                            js.setVerticalScrollBarPolicy(
                                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                            js.setPreferredSize(new Dimension(270, 200));

                            confirmFrame = new RenameFrame();
                            Container confirmPane = confirmFrame.getContentPane();
                            JPanel listPane = new JPanel();
                            confirmFrame.setSize(300, 317);
                            listPane.add(js, BorderLayout.CENTER);

                            JButton okButton = new JButton("Rename Execute");
                            JButton cancelButton = new JButton("Cancel");
                            JPanel buttonPane = new JPanel();
                            setPanelSize(buttonPane, new Dimension(400, 40));
                            buttonPane.add(okButton);
                            buttonPane.add(cancelButton);

                            cancelButton.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    confirmFrame.dispose();
                                }
                            });

                            okButton.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    int max = 0;
                                    if (online) {
                                        try {
                                            HttpURL httpURL = new HttpURL(SERVER);
                                            httpURL.setUserInfo(username, password);
                                            WebdavResource wr = new WebdavResource(httpURL);
                                            String folder = orfField.getText();
                                            wr.setPath("/" + PHOTO_FOLDER + "/" + folder);
                                            if (wr.exists()) {
                                                wr.setPath("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log");
                                                if (wr.exists()) {
                                                    max = 0;
                                                    File log = new File(inputDir.getText() + SLA + "old_rename_log");
                                                    wr.getMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log", log);
                                                    PrintWriter pw;
                                                    FileReader fr = new FileReader(inputDir.getText() + SLA + "old_rename_log");
                                                    int ch;
                                                    int counter = 0;
                                                    String str;
                                                    while ((char) (ch = fr.read()) != '\t') {
                                                        while ((char) (ch = fr.read()) != '\t') ;
                                                        str = "";
                                                        while ((char) (ch = fr.read()) != 'A' && ch != 'C' && ch != 'D')
                                                            ;
                                                        while ((char) (ch = fr.read()) != '.') {
                                                            str += (char) ch;
                                                        }
                                                        if (Integer.parseInt(str) > max) max = Integer.parseInt(str);
                                                        while ((char) (ch = fr.read()) != '\n') ;
                                                    }
                                                    fr.close();
                                                    log.delete();
                                                }
                                            }
                                            wr.close();
                                        } catch (HttpException e2) {
                                            displayErrorDialog(e2);
                                        } catch (IOException e2) {
                                            displayErrorDialog(e2);
                                        }
                                    }
                                    try {
                                        String sourcedir = inputDir.getText();
                                        boolean flag = true;
                                        File bufolder = new File(backupDir.getText() + SLA + orfField.getText());
                                        if (bufolder.exists()) {
                                            int ans = JOptionPane.showConfirmDialog(null, "すでに同じORF名のついたバックアップフォルダが存在しますが、このフォルダ上に書き込みますか？", "同ORF名のフォルダが存在します", JOptionPane.YES_NO_OPTION);
                                            if (ans == JOptionPane.NO_OPTION) {
                                                flag = false;
                                            }
                                        }
                                        if (flag && max >= sNm.getNumber().intValue()) {
                                            int ans = JOptionPane.showConfirmDialog(null, "サーバー上の同ORF名のフォルダの中のファイルと番号が重なる可能性があります。（サーバーのフォルダ中の最大番号　" + max + "）　よろしいですか？", "", JOptionPane.YES_NO_OPTION);
                                            if (ans == JOptionPane.NO_OPTION) {
                                                flag = false;
                                            }
                                        }
                                        if (!flag) {
                                            confirmFrame.dispose();
                                            JOptionPane.showMessageDialog(null, "renameを中止しました", "", JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            bufolder.mkdirs();
                                            if (!bufolder.exists()) {
                                                confirmFrame.dispose();
                                                JOptionPane.showMessageDialog(null, "backup folderの生成に失敗しました", "", JOptionPane.ERROR_MESSAGE);
                                            } else {
                                                Process[] proc = new Process[counter];
                                                for (int i = 0; i < counter; i++) {
                                                    String[] cmd = {IRFAN_VIEW, sourcedir + SLA + oldfiles[i], "/resize=(696,520)", "/convert=" + backupDir.getText() + SLA + orfField.getText() + SLA + newfiles[i]};
                                                    proc[i] = Runtime.getRuntime().exec(cmd);
                                                    proc[i].waitFor();
                                                    //String[] cmd0 = {CONVERT, "-colorspace", "GRAY", backupDir.getText() + SLA + orfField.getText() + SLA + newfiles[i], backupDir.getText() + SLA + orfField.getText() + SLA + newfiles[i]};
                                                    //Process proc0 = Runtime.getRuntime().exec(cmd0);
                                                    //proc0.waitFor();
                                                    progress.setValue(i + 1);
                                                    progress.update(progress.getGraphics());
                                                }
                                                String[] cmd0 = {MOGRIFY, "-colorspace", "GRAY", backupDir.getText() + SLA + orfField.getText() + SLA + "*.jpg"};
                                                Process proc0 = Runtime.getRuntime().exec(cmd0);
                                                proc0.waitFor();

                                                File f1 = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "unusable");
                                                f1.mkdir();
                                                int k = 0;
                                                int l = 0;
                                                pgs2 = new PhotoGroup[pgs.length - unusefileNum + 1];
                                                for (int j = 0; j < pgs.length; j++) {
                                                    if (!pgs[j].useornot) {
                                                        for (int i = 0; i < 3; i++) {
                                                            if (pgs[j].origFile[i] != null) {
                                                                String[] cmd = {IRFAN_VIEW, sourcedir + SLA + pgs[j].origFile[i], "/resize=(696,520)", "/convert=" + backupDir.getText() + SLA + orfField.getText() + SLA + "unusable" + SLA + pgs[j].newFile[i]};
                                                                proc[i] = Runtime.getRuntime().exec(cmd);
                                                                proc[i].waitFor();
                                                                //String[] cmd0 = {CONVERT, "-colorspace", "GRAY", backupDir.getText() + SLA + orfField.getText() + SLA + newfiles[i], backupDir.getText() + SLA + orfField.getText() + SLA + newfiles[i]};
                                                                //Process proc0 = Runtime.getRuntime().exec(cmd0);
                                                                //proc0.waitFor();
                                                                progress.setValue(counter + k + 2);
                                                                progress.update(progress.getGraphics());
                                                                k++;
                                                            }
                                                        }
                                                    } else {
                                                        pgs2[l] = pgs[j];
                                                        l++;
                                                    }
                                                }
                                                //File path = new File(sourcedir);
                                                //String ls[] = path.list();
                                                //for(int i=0;i<ls.length;i++)
                                                //{
                                                //	new File(sourcedir + SLA + ls[i]).delete();
                                                //}
                                                //new File(sourcedir).delete();
                                                confirmFrame.dispose();
                                                renamed = true;
                                                JOptionPane.showMessageDialog(null, "ファイル名の付け替えが完了しました", "", JOptionPane.INFORMATION_MESSAGE);
                                                //selectButton.setEnabled(true);
                                                if (online) submitButton.setEnabled(true);
                                            }
                                        }
                                    } catch (IOException e2) {
                                        displayErrorDialog(e2);
                                    } catch (InterruptedException e2) {
                                        displayErrorDialog(e2);
                                    }
                                }
                            });

                            progress = new JProgressBar(0, imgfilenum);
                            progress.setValue(0);
                            progress.setStringPainted(true);
                            JPanel progressPane = new JPanel();
                            progressPane.add(progress);
                            setPanelSize(progressPane, new Dimension(400, 40));

                            listPane.add(buttonPane);
                            listPane.add(progressPane);
                            confirmPane.add(listPane);
                            confirmFrame.setVisible(true);
                        }
                    });
                    JPanel checklistPane = new JPanel();
                    setPanelSize(checklistPane, new Dimension(500, 347));
                    checklistPane.add(checklabelPane);
                    checklistPane.add(cs);
                    checklistPane.add(checkbuttonPane);
                    usecheckPane.add(checklistPane);
                    usecheckFrame.setVisible(true);
                }
                break;
            case SUBMIT:
                boolean flg = true;
                if (getORF2(orfField.getText()).status.equals(GeneName.ST_NONE)) {
                    int ans = JOptionPane.showConfirmDialog(null, "orf_descriptions.tab上に同じ名前のorfが存在しないフォルダを送信しようとしていますが、よろしいですか？", "", JOptionPane.YES_NO_OPTION);
                    if (ans == JOptionPane.NO_OPTION) flg = false;
                }
                if (flg) {
                    try {
                        HttpURL httpURL = new HttpURL(SERVER);
                        //HttpURL httpURL2 = new HttpURL(SERVER2);
                        httpURL.setUserInfo(username, password);
                        //httpURL2.setUserInfo(username2, password);
                        WebdavResource wr = new WebdavResource(httpURL);
                        //WebdavResource wr2 = new WebdavResource(httpURL2);
                        String folder = orfField.getText();
                        wr.setPath("/" + PHOTO_FOLDER + "/" + folder);
                        //wr2.setPath("/" + PHOTO_FOLDER2 + "/" + folder);
                        String logText = "";

                        progress = new JProgressBar(0, newfiles.length);
                        progress.setValue(0);
                        progress.setStringPainted(true);
                        JPanel progressPane = new JPanel();
                        progressPane.add(progress);
                        setPanelSize(progressPane, new Dimension(400, 80));
                        progressFrame = new ProgressFrame();
                        Container prPane = progressFrame.getContentPane();
                        progressFrame.setSize(300, 100);
                        prPane.add(progressPane);
                        int prcounter = 0;

                        if (!wr.exists()) {
                            progressFrame.setVisible(true);
                            wr.mkcolMethod("/" + PHOTO_FOLDER + "/" + folder);
                            //wr2.mkcolMethod("/" + PHOTO_FOLDER2 + "/" + folder);
                            for (int i = 0; i < newfiles.length; i++) {
                                File file = new File(backupDir.getText() + SLA + orfField.getText() + SLA + newfiles[i]);
                                if (file.exists()) {
                                    wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + newfiles[i], file);
                                    //wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + newfiles[i], file);
                                    Calendar cl = Calendar.getInstance();
                                    logText += oldfiles[i] + '\t' + newfiles[i] + '\t' + cl.getTime() + newline;
                                    prcounter++;
                                    progress.setValue(prcounter);
                                    progress.update(progress.getGraphics());
                                }
                            }
                            PrintWriter pw;
                            pw = new PrintWriter(new FileWriter(backupDir.getText() + SLA + orfField.getText() + SLA + "rename_log"));
                            pw.print(logText);
                            pw.print('\t' + "finished");
                            pw.flush();
                            pw.close();
                            File log = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "rename_log");
                            wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log", log);
                            //wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "rename_log", log);
                            File unuse = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "unusable");
                            if (unuse.exists()) {
                                wr.mkcolMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable");
                                //wr2.mkcolMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable");
                                String[] unusefiles = unuse.list();
                                for (int i = 0; i < unusefiles.length; i++) {
                                    File unusefile = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "unusable" + SLA + unusefiles[i]);
                                    wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
                                    //wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
                                    prcounter++;
                                    progress.setValue(prcounter);
                                    progress.update(progress.getGraphics());
                                }
                            }
                            JOptionPane.showMessageDialog(null, "yeastサーバーへのファイルのアップロードを完了しました", "", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            int ans = JOptionPane.showConfirmDialog(null, "すでにサーバー上に同じORF名のついたフォルダが存在しますが、このフォルダ上に書き込みますか？", "同ORF名のフォルダが存在します", JOptionPane.YES_NO_OPTION);
                            if (ans == JOptionPane.YES_OPTION) {
                                progressFrame.setVisible(true);
                                wr.setPath("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log");
                                if (!wr.exists()) {
                                    for (int i = 0; i < newfiles.length; i++) {
                                        File file = new File(backupDir.getText() + SLA + orfField.getText() + SLA + newfiles[i]);
                                        if (file.exists()) {
                                            wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + newfiles[i], file);
                                            //wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + newfiles[i], file);
                                            Calendar cl = Calendar.getInstance();
                                            logText += oldfiles[i] + '\t' + newfiles[i] + '\t' + cl.getTime() + newline;
                                            prcounter++;
                                            progress.setValue(prcounter);
                                            progress.update(progress.getGraphics());
                                        }
                                    }
                                    PrintWriter pw;
                                    pw = new PrintWriter(new FileWriter(backupDir.getText() + SLA + orfField.getText() + SLA + "rename_log"));
                                    pw.print(logText);
                                    pw.print('\t' + "finished");
                                    pw.flush();
                                    pw.close();
                                    File log = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "rename_log");
                                    wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log", log);
                                    //wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "rename_log", log);
                                    File unuse = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "unusable");
                                    if (unuse.exists()) {
                                        wr.mkcolMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable");
                                        //wr2.mkcolMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable");
                                        String[] unusefiles = unuse.list();
                                        for (int i = 0; i < unusefiles.length; i++) {
                                            File unusefile = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "unusable" + SLA + unusefiles[i]);
                                            wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
                                            //wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
                                            prcounter++;
                                            progress.setValue(prcounter);
                                            progress.update(progress.getGraphics());
                                        }
                                    }
                                    JOptionPane.showMessageDialog(null, "yeastサーバーへのファイルのアップロードを完了しました", "", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    File log = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "old_rename_log");
                                    wr.getMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log", log);
                                    PrintWriter pw;
                                    pw = new PrintWriter(new FileWriter(backupDir.getText() + SLA + orfField.getText() + SLA + "rename_log"));
                                    FileReader fr = new FileReader(backupDir.getText() + SLA + orfField.getText() + SLA + "old_rename_log");
                                    int ch;
                                    int counter = 0;
                                    String[] str = new String[1024];
                                    while ((char) (ch = fr.read()) != '\t') {
                                        pw.print((char) ch);
                                        while ((char) (ch = fr.read()) != '\t') pw.print((char) ch);
                                        pw.print('\t');
                                        str[counter] = "";
                                        while ((char) (ch = fr.read()) != '\t') {
                                            str[counter] += (char) ch;
                                            pw.print((char) ch);
                                        }
                                        pw.print('\t');
                                        counter++;
                                        while ((char) (ch = fr.read()) != '\n') pw.print((char) ch);
                                        pw.print('\n');
                                    }
                                    fr.close();
                                    log.delete();
                                    for (int i = 0; i < newfiles.length; i++) {
                                        File file = new File(backupDir.getText() + SLA + orfField.getText() + SLA + newfiles[i]);
                                        if (file.exists()) {
                                            wr.setPath("/" + PHOTO_FOLDER + "/" + folder + "/" + newfiles[i]);
                                            boolean flag = true;
                                            if (wr.exists()) {
                                                for (int j = 0; j < counter; j++) {
                                                    if (str[j].equals(newfiles[i])) flag = false;
                                                }
                                            }
                                            if (flag) {
                                                wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + newfiles[i], file);
                                                //wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + newfiles[i], file);
                                                Calendar cl = Calendar.getInstance();
                                                logText += oldfiles[i] + '\t' + newfiles[i] + '\t' + cl.getTime() + newline;
                                            }
                                        }
                                        prcounter++;
                                        progress.setValue(prcounter);
                                        progress.update(progress.getGraphics());
                                    }
                                    pw.print(logText);
                                    pw.print('\t' + "finished");
                                    pw.flush();
                                    pw.close();
                                    log = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "rename_log");
                                    wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log", log);
                                    //wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "rename_log", log);
                                    File unuse = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "unusable");
                                    if (unuse.exists()) {
                                        wr.setPath("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable");
                                        if (!wr.exists()) {
                                            wr.mkcolMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable");
                                            //wr2.mkcolMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable");
                                        }
                                        String[] unusefiles = unuse.list();
                                        for (int i = 0; i < unusefiles.length; i++) {
                                            File unusefile = new File(backupDir.getText() + SLA + orfField.getText() + SLA + "unusable" + SLA + unusefiles[i]);
                                            wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
                                            //wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
                                            prcounter++;
                                            progress.setValue(prcounter);
                                            progress.update(progress.getGraphics());
                                        }
                                    }
                                    JOptionPane.showMessageDialog(null, "yeastサーバーへのファイルのアップロードを完了しました", "", JOptionPane.INFORMATION_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "アップロードを中止しました", "", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        progressFrame.dispose();
                        wr.close();
                        //wr2.close();
                    } catch (IOException e2) {
                        // yeastサーバーへの書き込みエラー
                        System.out.println(e2.getMessage());
                        JOptionPane.showMessageDialog(null, "サーバーへのファイルの書き込みに失敗しました", "エラー：サーバーへの書き込みに失敗しました", JOptionPane.ERROR_MESSAGE);
                    } catch (HttpException e2) {
                        //yeastサーバーへのアクセスエラー
                        System.out.println(e2.getMessage());
                        JOptionPane.showMessageDialog(null, "サーバーへのファイルの書き込みに失敗しました", "エラー：サーバーへの書き込みに失敗しました", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            case SELECT_PHOTO:
                img = new Image[4];
                imagedir = backupDir.getText() + SLA + orfField.getText();
                if (!(renamed)) {
                    imagedir = inputDir.getText();
                    File path = new File(inputDir.getText());
                    String[] ls = path.list();
                    Pattern p = Pattern.compile(FILE_PATTERN);
                    String[] newName = new String[ls.length];

                    // PhotoNumComparetorで定義される順序でsortされた
                    // PhotoNumクラスの集合 -> PhotoGroup というMap
                    TreeMap fileMap = new TreeMap(new PhotoNumComparator());
                    for (int i = 0; i < ls.length; i++) {
                        Matcher m = p.matcher(ls[i]);
                        if (m.matches()) {
                            // ファイルがMetaMorphのパターンにマッチしている場合
                            for (int j = 1; j <= m.groupCount(); j++) {
                                PhotoNum pn;
                                // FP_FIRST_PHOTO_NUMの部分と、FP_SECOND_PHOTO_NUMの合成数
                                if (m.group(FP_SECOND_PHOTO_NUM) == null) {
                                    // 手動の顕微鏡でとられた写真
                                    pn =
                                            new PhotoNum(
                                                    new Integer(m.group(FP_FIRST_PHOTO_NUM)).intValue(),
                                                    -1);
                                } else {
                                    // 自動顕微鏡でとられた写真
                                    pn =
                                            new PhotoNum(
                                                    new Integer(m.group(FP_FIRST_PHOTO_NUM)).intValue(),
                                                    new Integer(m.group(FP_SECOND_PHOTO_NUM)).intValue());
                                }
                                // search treemap
                                PhotoGroup pg;
                                if (fileMap.containsKey(pn)) {
                                    // 既に同じ番号のPhotoGroupが存在する場合
                                    pg = (PhotoGroup) fileMap.get(pn);
                                    fileMap.remove(pn);
                                } else {
                                    pg = new PhotoGroup();
                                }
                                pg.add(ls[i], m.group(2));
                                fileMap.put(pn, pg);
                            }
                        }
                    }

                    Collection c = fileMap.values();
                    Iterator ci = c.iterator();
                    pgs2 = new PhotoGroup[c.size()];

                    int count = 0;
                    while (ci.hasNext()) {
                        PhotoGroup pg = (PhotoGroup) ci.next();
                        pg.newFile = pg.origFile;
                        pgs2[count] = pg;
                        count++;
                    }
                }

                currentimage = 0;
                currentkind = 1;

                JLabel settingLabel = new JLabel("now setting");
                JPanel settingPane = new JPanel();
                settingPane.add(settingLabel);
                setPanelSize(settingPane, new Dimension(300, 80));
                settingFrame = new JFrame("");
                Container setPane = settingFrame.getContentPane();
                settingFrame.setSize(300, 100);
                setPane.add(settingPane);
                settingFrame.setVisible(true);
                settingLabel.update(settingLabel.getGraphics());

                photoselectFrame = new PhotoselectFrame();
                Container photoselectPane = photoselectFrame.getContentPane();
                photoselectFrame.setSize(825, 750);
                if (pgs2[currentimage].newFile[currentkind] != null) {
                    photoLabel = new JLabel(pgs2[currentimage].newFile[currentkind]);
                } else {
                    photoLabel = new JLabel("none");
                }
                JPanel photonamePane = new JPanel();
                setPanelSize(photonamePane, new Dimension(825, 30));
                photonamePane.add(photoLabel);

                try {
                    for (int i = 0; i < 3; i++) {
                        if (pgs2[currentimage].newFile[i] != null) {
                            if (!renamed) {
                                String[] cmd = {IRFAN_VIEW, imagedir + SLA + pgs2[currentimage].newFile[i], "/resize=(696,520)", "/convert=" + imagedir + SLA + "im" + i + ".jpg"};
                                Process proc = Runtime.getRuntime().exec(cmd);
                                proc.waitFor();
                                img[i] = getToolkit().getImage(imagedir + SLA + "im" + i + ".jpg");
                            } else {
                                img[i] = getToolkit().getImage(imagedir + SLA + pgs2[currentimage].newFile[i]);
                            }
                        }
                    }
                    if (pgs2[currentimage].newFile[0] != null && pgs2[currentimage].newFile[1] != null && pgs2[currentimage].newFile[2] != null) {
                        if (!renamed) {
                            String[] cmd0 = {CONVERT, "-colorize", "0/100/100", imagedir + SLA + "im0.jpg", imagedir + SLA + "to0.jpg"};
                            Process proc0 = Runtime.getRuntime().exec(cmd0);
                            String[] cmd1 = {CONVERT, "-colorize", "100/0/100", imagedir + SLA + "im1.jpg", imagedir + SLA + "to1.jpg"};
                            Process proc1 = Runtime.getRuntime().exec(cmd1);
                            String[] cmd2 = {CONVERT, "-colorize", "100/100/0", imagedir + SLA + "im2.jpg", imagedir + SLA + "to2.jpg"};
                            Process proc2 = Runtime.getRuntime().exec(cmd2);
                            proc0.waitFor();
                            proc1.waitFor();
                            proc2.waitFor();
                        } else {
                            String[] cmd0 = {CONVERT, "-colorize", "0/100/100", imagedir + SLA + pgs2[currentimage].newFile[0], imagedir + SLA + "to0.jpg"};
                            Process proc0 = Runtime.getRuntime().exec(cmd0);
                            String[] cmd1 = {CONVERT, "-colorize", "100/0/100", imagedir + SLA + pgs2[currentimage].newFile[1], imagedir + SLA + "to1.jpg"};
                            Process proc1 = Runtime.getRuntime().exec(cmd1);
                            String[] cmd2 = {CONVERT, "-colorize", "100/100/0", imagedir + SLA + pgs2[currentimage].newFile[2], imagedir + SLA + "to2.jpg"};
                            Process proc2 = Runtime.getRuntime().exec(cmd2);
                            proc0.waitFor();
                            proc1.waitFor();
                            proc2.waitFor();
                        }
                        String[] cmd3 = {CONVERT, "-average", imagedir + SLA + "to0.jpg", imagedir + SLA + "to1.jpg", imagedir + SLA + "to2.jpg", imagedir + SLA + "to3.jpg"};
                        Process proc3 = Runtime.getRuntime().exec(cmd3);
                        proc3.waitFor();
                        String[] cmd4 = {CONVERT, "-gamma", "1.7/1.7/1.7", imagedir + SLA + "to3.jpg", imagedir + SLA + "convert" + currentimage + ".jpg"};
                        Process proc4 = Runtime.getRuntime().exec(cmd4);
                        proc4.waitFor();
                        img[3] = getToolkit().getImage(imagedir + SLA + "convert" + currentimage + ".jpg");
                    }
                } catch (IOException e2) {
                    displayErrorDialog(e2);
                } catch (InterruptedException e2) {
                    displayErrorDialog(e2);
                }

                photocanvas = new PhotoCanvas();
                photocanvas.setSize(new Dimension(825, 520));
                JPanel photoPane = new JPanel();
                setPanelSize(photoPane, new Dimension(825, 520));
                photoPane.add(photocanvas);

                conAButton = new JRadioButton("ConA");
                conAButton.setSelected(true);
                ActinButton = new JRadioButton("Actin");
                DAPIButton = new JRadioButton("DAPI");
                compositeButton = new JRadioButton("Composite");
                ButtonGroup photogroup = new ButtonGroup();
                photogroup.add(conAButton);
                photogroup.add(ActinButton);
                photogroup.add(DAPIButton);
                photogroup.add(compositeButton);
                conAButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentkind = 1;
                        photoselectupdate();
                    }
                });
                ActinButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentkind = 0;
                        photoselectupdate();
                    }
                });
                DAPIButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentkind = 2;
                        photoselectupdate();
                    }
                });
                compositeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentkind = 3;
                        photoselectupdate();
                    }
                });
                JPanel kindSelectPane = new JPanel();
                setPanelSize(kindSelectPane, new Dimension(825, 40));
                kindSelectPane.add(conAButton);
                kindSelectPane.add(ActinButton);
                kindSelectPane.add(DAPIButton);
                kindSelectPane.add(compositeButton);

                checkUseornot = new JCheckBox("use this image", pgs2[currentimage].useornot);
                checkUseornot.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        pgs2[currentimage].useornot = checkUseornot.isSelected();
                    }
                });
                JPanel checkPane = new JPanel();
                setPanelSize(checkPane, new Dimension(825, 30));
                checkPane.add(checkUseornot);

                JButton previousButton = new JButton("previous image");
                JButton nextButton = new JButton("next image");
                sNm2 = new SpinnerNumberModel(1, 1, pgs2.length, 1);
                Currentspinner = new JSpinner(sNm2);
                JButton jumpButton = new JButton("jump");

                previousButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (currentimage == 0) currentimage = pgs2.length - 1;
                        else currentimage--;
                        try {
                            for (int i = 0; i < 3; i++) {
                                if (pgs2[currentimage].newFile[i] != null) {
                                    if (!renamed) {
                                        String[] cmd = {IRFAN_VIEW, imagedir + SLA + pgs2[currentimage].newFile[i], "/resize=(696,520)", "/convert=" + imagedir + SLA + "im" + i + ".jpg"};
                                        Process proc = Runtime.getRuntime().exec(cmd);
                                        proc.waitFor();
                                        img[i] = getToolkit().createImage(imagedir + SLA + "im" + i + ".jpg");
                                    } else {
                                        img[i] = getToolkit().getImage(imagedir + SLA + pgs2[currentimage].newFile[i]);
                                    }
                                }
                            }
                            if (pgs2[currentimage].newFile[0] != null && pgs2[currentimage].newFile[1] != null && pgs2[currentimage].newFile[2] != null) {
                                File f1 = new File(imagedir + SLA + "convert" + currentimage + ".jpg");
                                if (!f1.exists()) {
                                    if (!renamed) {
                                        String[] cmd0 = {CONVERT, "-colorize", "0/100/100", imagedir + SLA + "im0.jpg", imagedir + SLA + "to0.jpg"};
                                        Process proc0 = Runtime.getRuntime().exec(cmd0);
                                        String[] cmd1 = {CONVERT, "-colorize", "100/0/100", imagedir + SLA + "im1.jpg", imagedir + SLA + "to1.jpg"};
                                        Process proc1 = Runtime.getRuntime().exec(cmd1);
                                        String[] cmd2 = {CONVERT, "-colorize", "100/100/0", imagedir + SLA + "im2.jpg", imagedir + SLA + "to2.jpg"};
                                        Process proc2 = Runtime.getRuntime().exec(cmd2);
                                        proc0.waitFor();
                                        proc1.waitFor();
                                        proc2.waitFor();
                                    } else {
                                        String[] cmd0 = {CONVERT, "-colorize", "0/100/100", imagedir + SLA + pgs2[currentimage].newFile[0], imagedir + SLA + "to0.jpg"};
                                        Process proc0 = Runtime.getRuntime().exec(cmd0);
                                        String[] cmd1 = {CONVERT, "-colorize", "100/0/100", imagedir + SLA + pgs2[currentimage].newFile[1], imagedir + SLA + "to1.jpg"};
                                        Process proc1 = Runtime.getRuntime().exec(cmd1);
                                        String[] cmd2 = {CONVERT, "-colorize", "100/100/0", imagedir + SLA + pgs2[currentimage].newFile[2], imagedir + SLA + "to2.jpg"};
                                        Process proc2 = Runtime.getRuntime().exec(cmd2);
                                        proc0.waitFor();
                                        proc1.waitFor();
                                        proc2.waitFor();
                                    }
                                    String[] cmd3 = {CONVERT, "-average", imagedir + SLA + "to0.jpg", imagedir + SLA + "to1.jpg", imagedir + SLA + "to2.jpg", imagedir + SLA + "to3.jpg"};
                                    Process proc3 = Runtime.getRuntime().exec(cmd3);
                                    proc3.waitFor();
                                    String[] cmd4 = {CONVERT, "-gamma", "1.7/1.7/1.7", imagedir + SLA + "to3.jpg", imagedir + SLA + "convert" + currentimage + ".jpg"};
                                    Process proc4 = Runtime.getRuntime().exec(cmd4);
                                    proc4.waitFor();
                                }
                                img[3] = getToolkit().getImage(imagedir + SLA + "convert" + currentimage + ".jpg");
                            }
                        } catch (IOException e2) {
                            displayErrorDialog(e2);
                        } catch (InterruptedException e2) {
                            displayErrorDialog(e2);
                        }
                        photoselectupdate();
                    }
                });
                nextButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (currentimage == pgs2.length - 1) currentimage = 0;
                        else currentimage++;
                        try {
                            for (int i = 0; i < 3; i++) {
                                if (pgs2[currentimage].newFile[i] != null) {
                                    if (!renamed) {
                                        String[] cmd = {IRFAN_VIEW, imagedir + SLA + pgs2[currentimage].newFile[i], "/resize=(696,520)", "/convert=" + imagedir + SLA + "im" + i + ".jpg"};
                                        Process proc = Runtime.getRuntime().exec(cmd);
                                        proc.waitFor();
                                        img[i] = getToolkit().createImage(imagedir + SLA + "im" + i + ".jpg");
                                    } else {
                                        img[i] = getToolkit().getImage(imagedir + SLA + pgs2[currentimage].newFile[i]);
                                    }
                                }
                            }
                            if (pgs2[currentimage].newFile[0] != null && pgs2[currentimage].newFile[1] != null && pgs2[currentimage].newFile[2] != null) {
                                File f1 = new File(imagedir + SLA + "convert" + currentimage + ".jpg");
                                if (!f1.exists()) {
                                    if (!renamed) {
                                        String[] cmd0 = {CONVERT, "-colorize", "0/100/100", imagedir + SLA + "im0.jpg", imagedir + SLA + "to0.jpg"};
                                        Process proc0 = Runtime.getRuntime().exec(cmd0);
                                        String[] cmd1 = {CONVERT, "-colorize", "100/0/100", imagedir + SLA + "im1.jpg", imagedir + SLA + "to1.jpg"};
                                        Process proc1 = Runtime.getRuntime().exec(cmd1);
                                        String[] cmd2 = {CONVERT, "-colorize", "100/100/0", imagedir + SLA + "im2.jpg", imagedir + SLA + "to2.jpg"};
                                        Process proc2 = Runtime.getRuntime().exec(cmd2);
                                        proc0.waitFor();
                                        proc1.waitFor();
                                        proc2.waitFor();
                                    } else {
                                        String[] cmd0 = {CONVERT, "-colorize", "0/100/100", imagedir + SLA + pgs2[currentimage].newFile[0], imagedir + SLA + "to0.jpg"};
                                        Process proc0 = Runtime.getRuntime().exec(cmd0);
                                        String[] cmd1 = {CONVERT, "-colorize", "100/0/100", imagedir + SLA + pgs2[currentimage].newFile[1], imagedir + SLA + "to1.jpg"};
                                        Process proc1 = Runtime.getRuntime().exec(cmd1);
                                        String[] cmd2 = {CONVERT, "-colorize", "100/100/0", imagedir + SLA + pgs2[currentimage].newFile[2], imagedir + SLA + "to2.jpg"};
                                        Process proc2 = Runtime.getRuntime().exec(cmd2);
                                        proc0.waitFor();
                                        proc1.waitFor();
                                        proc2.waitFor();
                                    }
                                    String[] cmd3 = {CONVERT, "-average", imagedir + SLA + "to0.jpg", imagedir + SLA + "to1.jpg", imagedir + SLA + "to2.jpg", imagedir + SLA + "to3.jpg"};
                                    Process proc3 = Runtime.getRuntime().exec(cmd3);
                                    proc3.waitFor();
                                    String[] cmd4 = {CONVERT, "-gamma", "1.7/1.7/1.7", imagedir + SLA + "to3.jpg", imagedir + SLA + "convert" + currentimage + ".jpg"};
                                    Process proc4 = Runtime.getRuntime().exec(cmd4);
                                    proc4.waitFor();
                                }
                                img[3] = getToolkit().getImage(imagedir + SLA + "convert" + currentimage + ".jpg");
                            }
                        } catch (IOException e2) {
                            displayErrorDialog(e2);
                        } catch (InterruptedException e2) {
                            displayErrorDialog(e2);
                        }
                        photoselectupdate();
                    }
                });
                jumpButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentimage = sNm2.getNumber().intValue() - 1;
                        try {
                            for (int i = 0; i < 3; i++) {
                                if (pgs2[currentimage].newFile[i] != null) {
                                    if (!renamed) {
                                        String[] cmd = {IRFAN_VIEW, imagedir + SLA + pgs2[currentimage].newFile[i], "/resize=(696,520)", "/convert=" + imagedir + SLA + "im" + i + ".jpg"};
                                        Process proc = Runtime.getRuntime().exec(cmd);
                                        proc.waitFor();
                                        img[i] = getToolkit().createImage(imagedir + SLA + "im" + i + ".jpg");
                                    } else {
                                        img[i] = getToolkit().getImage(imagedir + SLA + pgs2[currentimage].newFile[i]);
                                    }
                                }
                            }
                            if (pgs2[currentimage].newFile[0] != null && pgs2[currentimage].newFile[1] != null && pgs2[currentimage].newFile[2] != null) {
                                File f1 = new File(imagedir + SLA + "convert" + currentimage + ".jpg");
                                if (!f1.exists()) {
                                    if (!renamed) {
                                        String[] cmd0 = {CONVERT, "-colorize", "0/100/100", imagedir + SLA + "im0.jpg", imagedir + SLA + "to0.jpg"};
                                        Process proc0 = Runtime.getRuntime().exec(cmd0);
                                        String[] cmd1 = {CONVERT, "-colorize", "100/0/100", imagedir + SLA + "im1.jpg", imagedir + SLA + "to1.jpg"};
                                        Process proc1 = Runtime.getRuntime().exec(cmd1);
                                        String[] cmd2 = {CONVERT, "-colorize", "100/100/0", imagedir + SLA + "im2.jpg", imagedir + SLA + "to2.jpg"};
                                        Process proc2 = Runtime.getRuntime().exec(cmd2);
                                        proc0.waitFor();
                                        proc1.waitFor();
                                        proc2.waitFor();
                                    } else {
                                        String[] cmd0 = {CONVERT, "-colorize", "0/100/100", imagedir + SLA + pgs2[currentimage].newFile[0], imagedir + SLA + "to0.jpg"};
                                        Process proc0 = Runtime.getRuntime().exec(cmd0);
                                        String[] cmd1 = {CONVERT, "-colorize", "100/0/100", imagedir + SLA + pgs2[currentimage].newFile[1], imagedir + SLA + "to1.jpg"};
                                        Process proc1 = Runtime.getRuntime().exec(cmd1);
                                        String[] cmd2 = {CONVERT, "-colorize", "100/100/0", imagedir + SLA + pgs2[currentimage].newFile[2], imagedir + SLA + "to2.jpg"};
                                        Process proc2 = Runtime.getRuntime().exec(cmd2);
                                        proc0.waitFor();
                                        proc1.waitFor();
                                        proc2.waitFor();
                                    }
                                    String[] cmd3 = {CONVERT, "-average", imagedir + SLA + "to0.jpg", imagedir + SLA + "to1.jpg", imagedir + SLA + "to2.jpg", imagedir + SLA + "to3.jpg"};
                                    Process proc3 = Runtime.getRuntime().exec(cmd3);
                                    proc3.waitFor();
                                    String[] cmd4 = {CONVERT, "-gamma", "1.7/1.7/1.7", imagedir + SLA + "to3.jpg", imagedir + SLA + "convert" + currentimage + ".jpg"};
                                    Process proc4 = Runtime.getRuntime().exec(cmd4);
                                    proc4.waitFor();
                                }
                                img[3] = getToolkit().getImage(imagedir + SLA + "convert" + currentimage + ".jpg");
                            }
                        } catch (IOException e2) {
                            displayErrorDialog(e2);
                        } catch (InterruptedException e2) {
                            displayErrorDialog(e2);
                        }
                        photoselectupdate();
                    }
                });
                JPanel imageselectPane = new JPanel();
                setPanelSize(imageselectPane, new Dimension(825, 40));
                imageselectPane.add(previousButton);
                imageselectPane.add(nextButton);
                imageselectPane.add(Currentspinner);
                imageselectPane.add(jumpButton);

                JButton finishButton = new JButton("finish selection");
                finishButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        File f1, f2;
                        for (int i = 0; i < pgs2.length; i++) {
                            f1 = new File(imagedir + SLA + "convert" + i + ".jpg");
                            if (f1.exists()) f1.delete();
                        }
                        for (int i = 0; i < 4; i++) {
                            f1 = new File(imagedir + SLA + "to" + i + ".jpg");
                            if (f1.exists()) f1.delete();
                        }
                        for (int i = 0; i < 3; i++) {
                            f1 = new File(imagedir + SLA + "im" + i + ".jpg");
                            if (f1.exists()) f1.delete();
                        }
                        f1 = new File(imagedir + SLA + "unusable");
                        if (!f1.exists()) {
                            f1.mkdir();
                        }
                        for (int j = 0; j < pgs2.length; j++) {
                            for (int i = 0; i < 3; i++) {
                                if (pgs2[j].newFile[i] != null) {
                                    if (!pgs2[j].useornot) {
                                        f1 = new File(imagedir + SLA + pgs2[j].newFile[i]);
                                        f2 = new File(imagedir + SLA + "unusable" + SLA + pgs2[j].newFile[i]);
                                        f1.renameTo(f2);
                                    }
                                }
                            }
                        }
                        photoselectFrame.dispose();
                    }
                });
                JPanel finishPane = new JPanel();
                setPanelSize(finishPane, new Dimension(825, 40));
                finishPane.add(finishButton);

                photoselectPane.setLayout(new BoxLayout(photoselectPane, BoxLayout.Y_AXIS));
                photoselectPane.add(photonamePane);
                photoselectPane.add(photoPane);
                photoselectPane.add(kindSelectPane);
                photoselectPane.add(checkPane);
                photoselectPane.add(imageselectPane);
                photoselectPane.add(finishPane);
                settingFrame.dispose();
                photoselectFrame.setVisible(true);
                break;
        }
    }

    public void photoselectupdate() {

        if (currentkind != 3) {
            if (pgs2[currentimage].newFile[currentkind] != null) {
                photoLabel.setText(pgs2[currentimage].newFile[currentkind]);
            } else {
                photoLabel.setText("none");
            }
        } else {
            if (pgs2[currentimage].newFile[0] != null && pgs2[currentimage].newFile[1] != null && pgs2[currentimage].newFile[2] != null) {
                photoLabel.setText(pgs2[currentimage].newFile[0] + " + " + pgs2[currentimage].newFile[1] + " + " + pgs2[currentimage].newFile[2]);
            } else {
                photoLabel.setText("none");
            }
        }
        checkUseornot.setSelected(pgs2[currentimage].useornot);
        photocanvas.repaint();
    }

    // ORFをweb(あるいは、local file)から取得する
    public boolean retrieveORF() {
        boolean flag = false;
        if (autoButton.isSelected() || localButton.isSelected() || plateButton.isSelected()) {
            if (isReadyORF) {
                return true;
            } else {
                GeneName gn;
                if (autoButton.isSelected()) {
                    // retrieve ORF from SCMD
                    gn = getORF(geneNameField.getText());
                } else if (localButton.isSelected()) {
                    // retrieve ORF from local file
                    gn = getORF2(geneNameField.getText());
                } else {
                    gn = getORF3();
                }
                String orf = "";

                switch (gn.status) {
                    case GeneName.ST_DONE:
                        geneNameField.setText(gn.gene_name);
                        orf = gn.orf;
                        orfMessage.setText("ORF is retrieved");
                        flag = true;
                        isReadyORF = true;
                        break;
                    case GeneName.ST_MULTIPLE:
                        orfMessage.setText("cannot find an unique ORF");
                        break;
                    case GeneName.ST_NONE:
                        orfMessage.setText("cannot find any ORF");
                        break;
                    case GeneName.ST_NETWORK_ERROR:
                        orfMessage.setText("network connection error");
                        break;
                    case GeneName.ST_NOFILE_ERROR:
                        orfMessage.setText("orf_descriptions.tab is not exist");
                        break;
                    case GeneName.ST_FILEREAD_ERROR:
                        orfMessage.setText("error occured while reading orf_descriptions.tab");
                        break;
                    case GeneName.ST_NOFILE_ERROR2:
                        orfMessage.setText("platetable.tab is not exist");
                        break;
                    case GeneName.ST_FILEREAD_ERROR2:
                        orfMessage.setText("error occured while reading platetable.tab");
                        break;
                }

                orfField.setText(orf);
            }
        } else if (manipButton.isSelected()) {
            // input ORF by hand
            String orfinput = orfField.getText();
            if (orfinput.length() == 0) {
                // orf is not ready
                isReadyORF = false;
            } else {
                flag = true;
                isReadyORF = true;
            }
        }
        return flag;
    }

    // ORFをSCMD(http://yeast.gi.k.u-tokyo.ac.jp/tools/get_orf.cgi" から取得
    public GeneName getORF(String name) {
        String orf = "";
        String genename = "";
        if (name.equals("")) {
            return new GeneName(orf, genename, GeneName.ST_NONE);
        }
        try {
            // 空白を%20に変換
            String queryString = name.replaceAll(" ", "%20");
            URL query = new URL(ORF_QUERY + queryString);
            URLConnection con = query.openConnection();
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(con.getInputStream()));
            // webからORF情報を取得
            StreamTokenizer st = new StreamTokenizer(in);
            st.nextToken();
            if (st.ttype == StreamTokenizer.TT_WORD) {
                orf = st.sval;
                st.nextToken();
                if (st.ttype == StreamTokenizer.TT_WORD) {
                    genename = st.sval;
                }
            } else {
                orf = GeneName.ST_NONE;
            }
            if (orf.equals(GeneName.ST_MULTIPLE)) {
                return new GeneName("", name, GeneName.ST_MULTIPLE);
            } else if (orf.equals(GeneName.ST_NONE)) {
                return new GeneName("", name, GeneName.ST_NONE);
            }
        } catch (IOException e) {
            // connection error
            System.out.println(e.getMessage());
            return new GeneName("", name, GeneName.ST_NETWORK_ERROR);
        }
        if (genename.equals("")) {
            genename = orf;
        }
        return new GeneName(orf, genename, GeneName.ST_DONE);
    }

    //	ORFをlocal file(orf_descriptions.tab)から取得
    public GeneName getORF2(String name) {
        String orf = "";
        String genename = "";

        if (name.equals("")) {
            return new GeneName(orf, genename, GeneName.ST_NONE);
        }
        boolean flag = false;
        try {
            FileReader fr = new FileReader(ORF_DESCRIPTIONS);
            int ch;
            String groupname;
            String groupname2;
            while (!flag && (ch = fr.read()) != -1) {
                groupname = "";
                while ((char) ch != '\t') {
                    groupname += (char) ch;
                    ch = fr.read();
                }
                groupname = groupname.toLowerCase();
                if (name.equalsIgnoreCase(groupname)) {
                    flag = true;
                    orf = groupname;
                    ch = fr.read();
                    if ((char) ch == '\t') {
                        genename = orf;
                    } else {
                        groupname2 = "";
                        while ((char) ch != '\t') {
                            groupname2 += (char) ch;
                            ch = fr.read();
                        }
                        groupname2 = groupname2.toLowerCase();
                        genename = groupname2;
                    }
                } else if ((char) (ch = fr.read()) != '\t') {
                    groupname2 = "";
                    while ((char) ch != '\t') {
                        groupname2 += (char) ch;
                        ch = fr.read();
                    }
                    groupname2 = groupname2.toLowerCase();
                    if (name.equalsIgnoreCase(groupname2)) {
                        flag = true;
                        orf = groupname;
                        genename = groupname2;
                    }
                }
                while ((char) (ch = fr.read()) != '\n') ;
            }
        } catch (FileNotFoundException e) {
            // orf_descriptions.tabが存在しない
            displayErrorDialog(e);
            System.out.println(e.getMessage());
            return new GeneName("", name, GeneName.ST_NOFILE_ERROR);
        } catch (IOException e) {
            // orf_descriptions.tabの読み込みエラー
            displayErrorDialog(e);
            System.out.println(e.getMessage());
            return new GeneName("", name, GeneName.ST_FILEREAD_ERROR);
        }
        if (flag) {
            return new GeneName(orf, genename, GeneName.ST_DONE);
        } else {
            return new GeneName("", name, GeneName.ST_NONE);
        }
    }

    public GeneName getORF3() {
        String orf = "";

        boolean flag = false;
        try {
            FileReader fr = new FileReader(PLATE_TABLE);
            int ch;
            String platename;
            for (int i = 0; i < 65; i++) {
                platename = "";
                while ((char) (ch = fr.read()) != '\t') platename += (char) ch;
                if (platename.equalsIgnoreCase("plate" + plateposition[0])) {
                    int alp = (int) plateposition[1].charAt(0) - 64;
                    if (alp > 0 && alp < 9) {
                        for (int j = 0; j < alp; j++) {
                            while ((char) (ch = fr.read()) != '\n') ;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "株の位置のアルファベットが間違っています（AからHまで）", " ", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    int num;
                    try {
                        num = Integer.parseInt(plateposition[2]);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "株の位置の番号が間違っています（1から12まで）", "", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (num > 0 && num < 13) {
                        for (int j = 0; j < num - 1; j++) {
                            while ((char) (ch = fr.read()) != '\t' && (char) ch != '\n') ;
                            if ((char) ch == '\n') break;
                        }
                        if ((char) ch == '\n' && num != 1) {
                            JOptionPane.showMessageDialog(null, "その位置には株が存在しません", "", JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "株の位置の番号が間違っています（1から12まで）", "", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    while ((char) (ch = fr.read()) != '\t' && (char) ch != '\r') orf += (char) ch;
                    orf = orf.toLowerCase();
                    if (!orf.equals("")) flag = true;
                    else JOptionPane.showMessageDialog(null, "その位置には株が存在しません", "", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (i == 64) {
                    JOptionPane.showMessageDialog(null, "プレート名が間違っています", "", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                for (int j = 0; j < 10; j++) {
                    while ((char) (ch = fr.read()) != '\n') ;
                }
            }
        } catch (FileNotFoundException e) {
            displayErrorDialog(e);
            System.out.println(e.getMessage());
            return new GeneName("", "", GeneName.ST_NOFILE_ERROR2);
        } catch (IOException e) {
            // orf_descriptions.tabの読み込みエラー
            displayErrorDialog(e);
            System.out.println(e.getMessage());
            return new GeneName("", "", GeneName.ST_FILEREAD_ERROR2);
        }
        if (flag) {
            return new GeneName(orf, orf, GeneName.ST_DONE);
        } else {
            return new GeneName("", "", GeneName.ST_NONE);
        }
    }

    class PhotoCanvas extends Canvas {
        public void paint(Graphics g) {
            if (currentkind != 3) {
                if (pgs2[currentimage].newFile[currentkind] != null) {
                    g.drawImage(img[currentkind], 100, 10, 647, 490, this);
                } else {
                    g.drawString("No file", 400, 200);
                }
            } else {
                if (pgs2[currentimage].newFile[0] != null && pgs2[currentimage].newFile[1] != null && pgs2[currentimage].newFile[2] != null) {
                    g.drawImage(img[currentkind], 100, 10, 647, 490, this);
                } else {
                    g.drawString("Can't draw", 400, 200);
                }
            }
        }
    }

    // MetaMorphの写真ファイル名に含まれる二つの数を合成したもの
    class PhotoNum {
        public int headerNum;
        public int sNum;
        public PhotoNum(int n1, int n2) {
            headerNum = n1;
            sNum = n2;
        }
    }

    // A,C,D3枚の写真の新旧のファイル名を入れておくコンテナ
    class PhotoGroup {
        public String[] origFile;
        public String[] newFile;
        public boolean useornot;
        private int count;

        public PhotoGroup() {
            origFile = new String[3];
            newFile = new String[3];
            count = 0;
            useornot = true;
        }

        public int getCount() {
            return count;
        }

        // originalの写真名を加える typeは、{FITC, Rh, DAPI}にpattern matchした部分
        public void add(String file, String type) {
            switch (type) {
                case "FITC":
                    origFile[CON_A] = file;
                    break;
                case "Rh":
                    origFile[ACTINE] = file;
                    break;
                case "DAPI":
                    origFile[DAPI] = file;
                    break;
                default:
                    // do nothing
                    return;
            }
            count++;
        }

        // renameされた後の写真名を加える
        public void addNewFile(String file, String type) {
            switch (type) {
                case "FITC":
                    newFile[CON_A] = file;
                    break;
                case "Rh":
                    newFile[ACTINE] = file;
                    break;
                case "DAPI":
                    newFile[DAPI] = file;
                    break;
            }
        }
    }

    // 2つのPhotoNumクラスの大小を比較するcomparator
    // headerNum, sNumの順で比較する
    class PhotoNumComparator implements Comparator {
        public int compare(Object a, Object b) {
            PhotoNum ai = (PhotoNum) a;
            PhotoNum bi = (PhotoNum) b;
            if (ai.headerNum == bi.headerNum) {
                return ai.sNum - bi.sNum;
            } else {
                return ai.headerNum - bi.headerNum;
            }
        }

        public boolean equal(Object a, Object b) {
            PhotoNum ai = (PhotoNum) a;
            PhotoNum bi = (PhotoNum) b;
            return ((ai.headerNum == bi.headerNum) && (ai.sNum == bi.sNum));
        }
    }

    // とりあえず未使用
    class JPEGFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            Pattern p = Pattern.compile(FILE_PATTERN);
            Matcher m = p.matcher(name);
            return m.matches();
        }
    }

    // Help画面
    class HelpFrame extends JFrame {
        public HelpFrame() {
            super("Help");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        }
    }

    // Rename画面
    class RenameFrame extends JFrame {
        public RenameFrame() {
            super("Rename Files");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        }
    }

    class UsecheckFrame extends JFrame {
        public UsecheckFrame() {
            super("Select usable images");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
    }

    class ProgressFrame extends JFrame {
        public ProgressFrame() {
            super("Progress");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        }
    }

    class PhotoselectFrame extends JFrame {
        public PhotoselectFrame() {
            super("Select Photo");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        }
    }
}

class GeneName {
    public static final String ST_MULTIPLE = "multiple";
    public static final String ST_NONE = "none";
    public static final String ST_DONE = "done";
    public static final String ST_NETWORK_ERROR = "net error";
    public static final String ST_NOFILE_ERROR = "no file";
    public static final String ST_FILEREAD_ERROR = "fileread error";
    public static final String ST_NOFILE_ERROR2 = "no file2";
    public static final String ST_FILEREAD_ERROR2 = "fileread error2";
    public String orf;
    public String gene_name;
    public String status = null;
    GeneName(String orf_string, String gname_string, String status_string) {
        orf = orf_string;
        gene_name = gname_string;
        status = status_string;
    }
}
