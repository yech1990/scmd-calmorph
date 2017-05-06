package lab.cb.scmd.util.staff;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.plaf.basic.BasicDirectoryModel;
//import javax.swing.filechooser.*;
//import java.util.*;
import java.io.*;
//import java.net.*; 
import java.util.regex.*;
import java.util.*;

import org.apache.util.HttpURL;
import org.apache.webdav.lib.WebdavResource;
import org.apache.commons.httpclient.*;

public class SCMDSubmitter extends JFrame implements ActionListener
{
	//static String SCMD_ROOT = "http://yeast.gi.k.u-tokyo.ac.jp/";
	//static String SCMD_ROOT2 = "http://bird.gi.k.u-tokyo.ac.jp/";
	static String ORF_QUERY; // = SCMD_ROOT + "tools/get_orf.cgi?name=";
	static String PHOTO_FOLDER;// = "staff/photo";
	//static String PHOTO_FOLDER2 = "staff/images/new images/auto";
	static String SERVER; // = SCMD_ROOT + PHOTO_FOLDER;
	//static String SERVER2 = SCMD_ROOT2 + PHOTO_FOLDER2;
	static String username = "birdstaff";
	//static String username2 = "bird";
//  	final static String SCMD_ROOT = "http://yeast.gi.k.u-tokyo.ac.jp/";
//  	final static String SCMD_ROOT2 = "http://bird.gi.k.u-tokyo.ac.jp/";
//  	final static String PHOTO_FOLDER = "staff/photo";
//  	final static String PHOTO_FOLDER2 = "staff/images/new images/auto";
//  	final static String SERVER = SCMD_ROOT + PHOTO_FOLDER;
//  	final static String SERVER2 = SCMD_ROOT2 + PHOTO_FOLDER2;
//	final static String username = "birdstaff";
//	final static String username2 = "bird";

	static String password;

	static JFrame passFrame;
	JFileChooser fc;


	public final String photoCategory[] = { "A", "C", "D" };
	public final String photoClassifier[] = { "Rh", "FITC", "DAPI" };
	public final int ACTINE = 0;
	public final int CON_A = 1;
	public final int DAPI = 2;

	final String SLA = File.separator;
	final String ROOTDIR = "C:" + SLA;
	String newline = "\n";

	JButton dirSelectButton;
	JButton exitButton;

	JTextField inputDir;
	static JLabel label;
	static JRadioButton onlineButton;
	static JRadioButton offlineButton;

	final String EXIT = "exit";
	final String DIR_SELECT = "dir select";
	final String DIR_CHANGE = "dir change";
	final String SUBMIT = "submit";

	JButton submitButton;
	JFrame confirmFrame;

	JProgressBar progress;

	final String FILE_PATTERN =
			"[-0-9a-z]*-(A|C|D)([1-9][0-9]*).jpg";
	final int IMAGE_TYPE = 1;
	final int PHOTO_NUM = 2;

	public SCMDSubmitter(char[] pswd)
	{
		super("SCMD Toolkit");
		JPanel directorySelectPane = new JPanel();

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

		JPanel submitPanel = new JPanel();
		submitButton = new JButton("submit");
		submitButton.setActionCommand(SUBMIT);
		submitButton.addActionListener(this);
		submitPanel.add(submitButton);

		JPanel exitPanel = new JPanel();
		exitButton = new JButton("exit");
		exitButton.setActionCommand(EXIT);
		exitButton.addActionListener(this);
		exitPanel.add(exitButton);

		submitPanel.add(submitButton);
		Container c = this.getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(directorySelectPane);
		c.add(submitPanel);
		c.add(exitPanel);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

	}


	// GUIでパネルのサイズを固定しまうための関数
	public void setPanelSize(JPanel pane, Dimension dim)
	{
		pane.setMaximumSize(dim);
		pane.setMinimumSize(dim);
		pane.setPreferredSize(dim);
	}

	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		if (command.equals(EXIT))
		{
			// exit
			System.exit(0);
		}
		else if (command.equals(DIR_SELECT))
		{
			// select directory
			fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setCurrentDirectory(new File(inputDir.getText()));
			int ret = fc.showOpenDialog(SCMDSubmitter.this);
			if (ret == JFileChooser.APPROVE_OPTION)
			{
				File dir = fc.getSelectedFile();
				inputDir.setText(dir.getAbsolutePath());
			}
		}
		else if (command.equals(SUBMIT))
		{
			File path = new File(inputDir.getText());
			String ls[] = path.list();
			Pattern p = Pattern.compile(FILE_PATTERN);
			TreeMap fileMap = new TreeMap(new PhotoNumComparator());
			int imgfilenum = 0;
			for (int i = 0; i < ls.length; i++)
			{
				Matcher m = p.matcher(ls[i]);
				if (m.matches())
				{
					imgfilenum++;
					for (int j = 1; j <= m.groupCount(); j++)
					{
						Integer pn = new Integer(m.group(PHOTO_NUM));
						PhotoGroup pg;
						if (fileMap.containsKey(pn))
						{
							pg = (PhotoGroup) fileMap.get(pn);
							fileMap.remove(pn);
						}
						else
						{
							pg = new PhotoGroup();
						}
						pg.add(ls[i], m.group(IMAGE_TYPE));
						fileMap.put(pn, pg);
					}
				}
			}
			Collection c = fileMap.values();
			Iterator ci = c.iterator();

			try
			{
				HttpURL httpURL = new HttpURL(SERVER);
				//HttpURL httpURL2 = new HttpURL(SERVER2);
				httpURL.setUserInfo(username, password);
				//httpURL2.setUserInfo(username2, password);
				WebdavResource wr = new WebdavResource(httpURL);
				//WebdavResource wr2 = new WebdavResource(httpURL2);
				String folder = path.getName();
				wr.setPath("/" + PHOTO_FOLDER + "/" + folder);
				//wr2.setPath("/" + PHOTO_FOLDER2 + "/" + folder);
				String logText = "";

				progress = new JProgressBar(0,c.size());
				progress.setValue(0);
				progress.setStringPainted(true);
				JPanel progressPane = new JPanel();
				progressPane.add(progress);
				setPanelSize(progressPane, new Dimension(400, 80));
				ProgressFrame progressFrame = new ProgressFrame();
				Container prPane = progressFrame.getContentPane();
				progressFrame.setSize(300, 100);
				prPane.add(progressPane);
				int prcounter = 0;

				if(!wr.exists())
				{
					progressFrame.setVisible(true);
					wr.mkcolMethod("/" + PHOTO_FOLDER + "/" + folder);
					//wr2.mkcolMethod("/" + PHOTO_FOLDER2 + "/" + folder);
					while(ci.hasNext())
					{
						PhotoGroup pg = (PhotoGroup) ci.next();
						for(int j=0; j<3; j++)
						{
							if(pg.File[j] != null)
							{
								File file = new File(inputDir.getText() + SLA + pg.File[j]);
								wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + pg.File[j], file);
								//wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + pg.File[j], file);
								Calendar cl = Calendar.getInstance();
								logText += pg.File[j] + '\t' + pg.File[j] + '\t' + cl.getTime() + newline;
								prcounter++;
								progress.setValue(prcounter);
								progress.update(progress.getGraphics());

							}
						}
					}
					PrintWriter pw;
					pw = new PrintWriter(new FileWriter(inputDir.getText() + SLA + "rename_log"));
					pw.print(logText);
					pw.print('\t' + "finished");
					pw.flush();
					pw.close();
					File log = new File(inputDir.getText() + SLA + "rename_log");
					wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log", log);
					//wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "rename_log", log);
					File unuse = new File(inputDir.getText() + SLA +"unusable");
					if(unuse.exists())
					{
						wr.mkcolMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable");
						//wr2.mkcolMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable");
						String unusefiles[] = unuse.list();
						for(int i = 0; i < unusefiles.length; i++)
						{
							File unusefile = new File(inputDir.getText() + SLA + "unusable" + SLA + unusefiles[i]);
							wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
							//wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
						}
					}
					JOptionPane.showMessageDialog(null, "yeastサーバーへのファイルのアップロードを完了しました", "", JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					int ans = JOptionPane.showConfirmDialog(null, "すでにサーバー上に同じORF名のついたフォルダが存在しますが、このフォルダ上に書き込みますか？", "同ORF名のフォルダが存在します", JOptionPane.YES_NO_OPTION);
					if(ans == JOptionPane.YES_OPTION)
					{
						progressFrame.setVisible(true);
						wr.setPath("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log");
						if(!wr.exists())
						{
							while(ci.hasNext())
							{
								PhotoGroup pg = (PhotoGroup) ci.next();
								for(int j=0; j<3; j++)
								{
									if(pg.File[j] != null)
									{
										File file = new File(inputDir.getText() + SLA + pg.File[j]);
										wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + pg.File[j], file);
										//wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + pg.File[j], file);
										Calendar cl = Calendar.getInstance();
										logText += pg.File[j] + '\t' + pg.File[j] + '\t' + cl.getTime() + newline;
										prcounter++;
										progress.setValue(prcounter);
										progress.update(progress.getGraphics());
									}
								}
							}
							PrintWriter pw;
							pw = new PrintWriter(new FileWriter(inputDir.getText() + SLA + "rename_log"));
							pw.print(logText);
							pw.print('\t' + "finished");
							pw.flush();
							pw.close();
							File log = new File(inputDir.getText() + SLA + "rename_log");
							wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log", log);
							//wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "rename_log", log);
							File unuse = new File(inputDir.getText() + SLA +"unusable");
							if(unuse.exists())
							{
								wr.mkcolMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable");
								//wr2.mkcolMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable");
								String unusefiles[] = unuse.list();
								for(int i = 0; i < unusefiles.length; i++)
								{
									File unusefile = new File(inputDir.getText() + SLA + "unusable" + SLA + unusefiles[i]);
									wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
									//wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
								}
							}
							JOptionPane.showMessageDialog(null, "yeastサーバーへのファイルのアップロードを完了しました", "", JOptionPane.INFORMATION_MESSAGE);
						}
						else
						{
							File log = new File(inputDir.getText() + SLA + "old_rename_log");
							wr.getMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log", log);
							PrintWriter pw;
							pw = new PrintWriter(new FileWriter(inputDir.getText() + SLA + "rename_log"));
							FileReader fr = new FileReader(inputDir.getText() + SLA + "old_rename_log");
							int ch;
							int counter = 0;
							String[] str = new String[1024];
							while((char)(ch = fr.read()) != '\t')
							{
								pw.print((char)ch);
								while((char)(ch = fr.read()) != '\t') pw.print((char)ch);
								pw.print('\t');
								str[counter] = "";
								while((char)(ch = fr.read()) != '\t')
								{
									str[counter] += (char)ch;
									pw.print((char)ch);
								}
								pw.print('\t');
								counter++;
								while((char)(ch = fr.read()) != '\n') pw.print((char)ch);
								pw.print('\n');
							}
							fr.close();
							log.delete();
							while(ci.hasNext())
							{
								PhotoGroup pg = (PhotoGroup) ci.next();
								for(int j=0; j<3; j++)
								{
									if(pg.File[j] != null)
									{
										File file = new File(inputDir.getText() + SLA + pg.File[j]);
										wr.setPath("/" + PHOTO_FOLDER + "/" + folder + "/" + pg.File[j]);
										boolean flag = true;
										if(wr.exists())
										{
											for(int k = 0; k < counter; k++)
											{
												if(str[k].equals(pg.File[j])) flag = false;
											}
										}
										if(flag)
										{
											wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + pg.File[j], file);
											//wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + pg.File[j], file);
											Calendar cl = Calendar.getInstance();
											logText += pg.File[j] + '\t' + pg.File[j] + '\t' + cl.getTime() + newline;
										}
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
							log = new File(inputDir.getText() + SLA + "rename_log");
							wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "rename_log", log);
							//wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "rename_log", log);
							File unuse = new File(inputDir.getText() + SLA +"unusable");
							if(unuse.exists())
							{
								wr.setPath("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable");
								if(!wr.exists())
								{
									wr.mkcolMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable");
									//wr2.mkcolMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable");
								}								String unusefiles[] = unuse.list();
								for(int i = 0; i < unusefiles.length; i++)
								{
									File unusefile = new File(inputDir.getText() + SLA + "unusable" + SLA + unusefiles[i]);
									wr.putMethod("/" + PHOTO_FOLDER + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
									//wr2.putMethod("/" + PHOTO_FOLDER2 + "/" + folder + "/" + "unusable" + "/" + unusefiles[i], unusefile);
								}
							}
							JOptionPane.showMessageDialog(null, "yeastサーバーへのファイルのアップロードを完了しました", "", JOptionPane.INFORMATION_MESSAGE);
						}
					}
					else
					{
						JOptionPane.showMessageDialog(null, "アップロードを中止しました", "", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				progressFrame.dispose();
				wr.close();
				//wr2.close();
			}
			catch (IOException e2)
			{
				// yeastサーバーへの書き込みエラー
				System.out.println(e2.getMessage());
				JOptionPane.showMessageDialog(null, "サーバーへのファイルの書き込みに失敗しました", "エラー：サーバーへの書き込みに失敗しました", JOptionPane.ERROR_MESSAGE);
			}
			catch (HttpException e2)
			{
				//yeastサーバーへのアクセスエラー
				System.out.println(e2.getMessage());
				JOptionPane.showMessageDialog(null, "サーバーへのファイルの書き込みに失敗しました", "エラー：サーバーへの書き込みに失敗しました", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static String PropertyNames[] =
			{"SCMD_ROOT", "PHOTO_DIR", "IRFAN_VIEW",
					"IMAGE_MAGICK", "CONVERT", "MOGRIFY",
					"ORF_TABLE", "PLATE_TABLE"};
	final static public int P_SCMD_ROOT = 0;
	final static public int P_PHOTO_DIR = 1;
	final static public int P_IRFAN_VIEW = 2;
	final static public int P_IMAGE_MAGICK = 3;
	final static public int P_CONVERT = 4;
	final static public int P_MOGRIFY = 5;
	final static public int P_ORF_TABLE = 6;
	final static public int P_PLATE_TABLE = 7;

	public static String UserProperties[];

	public static void main(String args[])
	{
		UserProperties = new String[PropertyNames.length];
		// default values
		UserProperties[P_SCMD_ROOT]     = "http://yeast.gi.k.u-tokyo.ac.jp/";
		UserProperties[P_PHOTO_DIR]     = "staff/photo";
		UserProperties[P_IRFAN_VIEW]    = "./i_view32/i_view32.exe";
		UserProperties[P_IMAGE_MAGICK]  = "C:/Program Files/ImageMagick-5.5.6-Q16/";
		UserProperties[P_CONVERT]       = "convert.exe";
		UserProperties[P_MOGRIFY]       = "mogrify.exe";
		UserProperties[P_ORF_TABLE]     = "orf_descriptions.tab";
		UserProperties[P_PLATE_TABLE]   = "platetable.tab";
		if(args.length >= 1)
		{
			// load a setting file
			try
			{
				FileInputStream input = new FileInputStream(args[0]);
				Properties property = new Properties();
				property.load(input);


				for (int i = 0; i < PropertyNames.length; i++)
				{
					String argument = property.getProperty(PropertyNames[i]);
					if(argument != null)
						UserProperties[i] = argument;
				}
				input.close();
			}
			catch(IOException e)
			{
				// cannot read file correctly
				System.out.println("invalid setting files");
			}
		}

		// set parameters
		PHOTO_FOLDER = UserProperties[P_PHOTO_DIR];
		ORF_QUERY = UserProperties[P_SCMD_ROOT] + "tools/get_orf.cgi?name=";
		SERVER = UserProperties[P_SCMD_ROOT] + UserProperties[P_PHOTO_DIR];
		//ORF_DESCRIPTIONS = UserProperties[P_ORF_TABLE];
		//PLATE_TABLE = UserProperties[P_PLATE_TABLE];
		//IRFAN_VIEW = UserProperties[P_IRFAN_VIEW];
		//CONVERT = UserProperties[P_IMAGE_MAGICK] + UserProperties[P_CONVERT];
		//MOGRIFY = UserProperties[P_IMAGE_MAGICK] + UserProperties[P_MOGRIFY];

		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		passFrame = new JFrame("Password");
		label = new JLabel("Enter the password: ");
		JPasswordField passwordField = new JPasswordField(10);
		passwordField.setEchoChar('*');
		passwordField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPasswordField input = (JPasswordField)e.getSource();
				char[] pswd = input.getPassword();
				password = new String(pswd);
				boolean check = true;
				try{
					HttpURL httpURL = new HttpURL(SERVER);
					httpURL.setUserInfo(username, password);
					WebdavResource wr = new WebdavResource(httpURL);
					wr.close();
				}
				catch(IOException e2){
				}
				catch(HttpException e2){
					label.setText("incorrect password: Enter again");
					check = false;
				}
				if(check){
					passFrame.dispose();
					SCMDSubmitter main_frame = new SCMDSubmitter(pswd);
					main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					main_frame.setSize(500, 200);
					main_frame.setVisible(true);
				}
			}
		});

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		contentPane.add(label, BorderLayout.WEST);
		contentPane.add(passwordField, BorderLayout.CENTER);

		passFrame.setContentPane(contentPane);
		passFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { System.exit(0); }
		});
		passFrame.pack();
		passFrame.setVisible(true);
	}

	class PhotoGroup
	{
		public String File[];

		private int count;
		public PhotoGroup()
		{
			File = new String[3];
			count = 0;
		}
		public int getCount()
		{
			return count;
		}

		public void add(String file, String type)
		{
			if (type.equals("C"))
			{
				File[CON_A] = file;
			}
			else if (type.equals("A"))
			{
				File[ACTINE] = file;
			}
			else if (type.equals("D"))
			{
				File[DAPI] = file;
			}
		}
	}
	class PhotoNumComparator implements Comparator
	{
		public int compare(Object a, Object b)
		{
			Integer ai = (Integer)a;
			Integer bi = (Integer)b;
			return ai.intValue() - bi.intValue();
		}
		public boolean equal(Object a, Object b)
		{
			Integer ai = (Integer)a;
			Integer bi = (Integer)b;
			return ai.intValue() == bi.intValue();
		}
	}

	class ProgressFrame extends JFrame
	{
		public ProgressFrame()
		{
			super("Progress");
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		}
	}
}

