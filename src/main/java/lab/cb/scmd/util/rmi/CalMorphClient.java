// calmorph client

package lab.cb.scmd.util.rmi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;

public class CalMorphClient {
    private String _servername = "localhost";
    private String cmd_prefix = "java -Xmx768m -Xms768m -jar CalMorph.jar ";
    private String cmd_suffix = "";
    private String _inputdir = "";
    private CalMorphInterface obj = null;
    private String clientid = ""; // クライアントの識別のため
    /**
     * @param string
     */
    public CalMorphClient(String server) {
        _servername = server;
        String randomkey = System.currentTimeMillis() + ((int)(Math.random()*1000.0) + "");
        clientid = randomkey + "@" + _servername;
    }

    public static void main(String args[]) {
        if( args.length != 4 ) {
            usage();
            System.exit(1);
        }
        CalMorphClient client = new CalMorphClient(args[0]);
        client.inoutdir(args[1], args[2], args[3]);
        client.runCalmorphOnServer();
      }

    /**
     * @param string
     * @param string2
     * @param string3
     */
    private void inoutdir(String inputdir, String resultdir, String xmldir) {
        _inputdir = inputdir + "/"; 
        cmd_suffix = resultdir + " " + xmldir;
    }

    /**
     * 
     */
    private void runCalmorphOnServer() {
        try {
          // セキュリティマネージャーを設定します
          System.setSecurityManager(new RMISecurityManager());
          // リモートオブジェクトの参照(スタブ)を取得します
          System.out.println("rmi://" + _servername + "/CalMorph");
          obj = (CalMorphInterface)Naming.lookup("rmi://" + _servername + "/CalMorph");
          // サーバから、ORFを取得する
          String[] orflist = null;
          while( true ) {
              orflist = obj.getORFList(clientid);
              if( orflist == null || orflist.length == 0 )
                  break;
              System.out.println("Achieving ORF list from server.");
              System.out.println("ORF Size: " + orflist.length);
              for( int i = 0; i < orflist.length; i++ ) {
                  System.out.println("Running... " + orflist[i]);
                  runCalMorphForOrf(orflist[i]);
              }
          }
        } catch(Exception e) {
          e.printStackTrace();
          System.exit(1);
        }
    }

    /**
     * @param string
     */
    private void runCalMorphForOrf(String orf) {
        try	{
            String cmd = cmd_prefix;
            cmd += " " + _inputdir + orf + " ";
            cmd += cmd_suffix;
            System.out.println(cmd);
            
            Process process = Runtime.getRuntime().exec(cmd);
//            Process process = Runtime.getRuntime().exec("sleep 1");
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            PrintStream out = System.out;
            while ((line = br.readLine()) != null) {
                out.println(line);
            }
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private static void usage() {
        System.out.println("CalMorphClient <servername> <calmorph args>");
    }
}
