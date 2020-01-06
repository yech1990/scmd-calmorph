// Calmorph despatcher

package lab.cb.scmd.util.rmi;

import java.io.File;
import java.io.PrintStream;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class CalMorphDespatcher extends UnicastRemoteObject implements
        CalMorphInterface {
    private String[] orflist = null;
    private Vector inputORFList = new Vector();
    private Set clientidmap = new TreeSet();

    private int currentorf = 0;

    private PrintStream out = System.out;

    // コンストラクタ
    public CalMorphDespatcher(String dir) throws RemoteException {
        makeORFlist(dir);
    }

    public static void main(String[] args) {
        if (args.length > 1)
            System.exit(1);
        if (System.getSecurityManager() == null) {
            // セキュリティマネージャーの設定
            System.setSecurityManager(new RMISecurityManager());
        }
        try {
            // サーバー側のリモートオブジェクトを生成
            CalMorphDespatcher obj = null;
            if (args.length == 0)
                obj = new CalMorphDespatcher(".");
            else
                obj = new CalMorphDespatcher(args[0]);
            // リモートオブジェクトに新しい名前を関連付けます
            Naming.rebind("CalMorph", obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void makeORFlist(String dir) {
        // search orf directories
        File baseDir = new File(dir);
        if (!baseDir.isDirectory()) {
            System.err.println(dir + " doesn't exist");
            System.exit(1);
        }
        File[] file = baseDir.listFiles();
        for (int i = 0; i < file.length; i++) {
            if (!file[i].isDirectory())
                continue; // skip non directory files

            String orfName = file[i].getName();
            inputORFList.add(orfName);
        }
        System.out.println("ORF size:" + inputORFList.size());
    }

    // ORFを返す
    public synchronized String[] getORFList(String clientname) throws RemoteException {
        int step = 5;
        int endorf = currentorf + step;
        System.out.println("Request from :" + clientname);
        if (currentorf >= inputORFList.size()) {
            clientidmap.remove(clientname);
            System.out.println("No more ORFs" + clientidmap.size());
            return new String[0];
        }
        clientidmap.add(clientname);
        System.out.println("Current ORF:" + currentorf);
        if (endorf > inputORFList.size()) {
            endorf = inputORFList.size();
            step = endorf - currentorf;
        }
        String[] ORFList = new String[step];
        for (int i = 0; i < step; i++) {
            ORFList[i] = inputORFList.get(i + currentorf).toString();
            System.out.println(ORFList[i]);
        }
        currentorf = endorf;
        if (currentorf >= inputORFList.size())
            System.out.println("finished!!!!");
        return ORFList;
    }

}