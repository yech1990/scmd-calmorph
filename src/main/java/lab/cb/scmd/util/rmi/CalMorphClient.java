// calmorph client

package lab.cb.scmd.util.rmi;

import java.io.*;
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
     * @param server
     */
    private CalMorphClient(String server) {
        _servername = server;
        String randomkey = System.currentTimeMillis() + ((int) (Math.random() * 1000.0) + "");
        clientid = randomkey + "@" + _servername;
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            usage();
            System.exit(1);
        }
        CalMorphClient client = new CalMorphClient(args[0]);
        client.inoutdir(args[1], args[2], args[3]);
        client.runCalmorphOnServer();
    }

    /**
     *
     */
    private static void usage() {
        System.out.println("CalMorphClient <servername> <calmorph args>");
    }

    /**
     * @param inputdir
     * @param resultdir
     * @param xmldir
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
            System.setSecurityManager(new SecurityManager());
            // リモートオブジェクトの参照(スタブ)を取得します
            System.out.println("rmi://" + _servername + "/CalMorph");
            obj = (CalMorphInterface) Naming.lookup("rmi://" + _servername + "/CalMorph");
            // サーバから、ORFを取得する
            String[] orflist;
            while (true) {
                orflist = obj.getORFList(clientid);
                if (orflist == null || orflist.length == 0)
                    break;
                System.out.println("Achieving ORF list from server.");
                System.out.println("ORF Size: " + orflist.length);
                for (String s : orflist) {
                    System.out.println("Running... " + s);
                    runCalMorphForOrf(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * @param orf
     */
    private void runCalMorphForOrf(String orf) {
        try {
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
