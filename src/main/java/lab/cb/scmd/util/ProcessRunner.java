//--------------------------------------
// SCMDProject
// 
// ProcessRunner.java 
// Since: 2004/07/13
//
// $URL: http://phenome.gi.k.u-tokyo.ac.jp/devel/svn/phenome/trunk/SCMD/src/lab/cb/scmd/util/ProcessRunner.java $ 
// $LastChangedBy: leo $ 
//--------------------------------------

package lab.cb.scmd.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;

//import java.io.InputStreamReader;

import lab.cb.scmd.exception.UnfinishedTaskException;

/**
 * @author leo
 *
 */
public class ProcessRunner
{
    static public String[] tokenize(String cmd) {
        Vector list = new Vector();

        int cursor = 0;


        StringBuffer token = new StringBuffer();
        while (cursor < cmd.length())
        {
            int nextCursor = -1;
            char c = cmd.charAt(cursor);
            switch (c)
            {
                case '\"':
                    // cursorを次のdouble quoationまで進める
                    nextCursor = cmd.indexOf('\"', cursor+1);
                    if(nextCursor == -1)
                        nextCursor = cmd.length()-2;
                    list.add(cmd.substring(cursor+1, nextCursor));
                    cursor = nextCursor + 1;
                    break;
                case '\'':
                    nextCursor = cmd.indexOf('\'', cursor+1);
                    if(nextCursor == -1)
                        nextCursor = cmd.length()-2;
                    list.add(cmd.substring(cursor+1, nextCursor));
                    cursor = nextCursor + 1;
                    break;
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                case '\f':
                    if(token.length() > 0)
                        list.add(token.toString());
                    token = new StringBuffer();
                    break;
                default:
                    token.append(c);
                    break;
            }
            cursor++;
        }
        if(token.length() > 0)
            list.add(token.toString());

        String[] array = new String[list.size()];
        for(int i=0; i<array.length; i++)
            array[i] = (String) list.get(i);
        return array;
    }

    static public void run(OutputStream out, String cmd) throws UnfinishedTaskException {
        try
        {
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(tokenize(cmd));
            PipeWorker errLogger = new PipeWorker(p.getErrorStream(), System.out);
            Thread logThread = new Thread(errLogger);
            logThread.start();
            (new PipeWorker(p.getInputStream(), out)).run();
            p.waitFor();
            logThread.join();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new UnfinishedTaskException(e);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            throw new UnfinishedTaskException(e);
        }
    }

    /**
     * コマンドラインに、 パイプで入力を渡して実行 (in.read() --> Process.out.write()) | Process |
     * (Process.in.read --> out.write())
     *
     * @param in
     *            入力
     * @param out
     *            コマンドラインの結果の出力先
     * @param cmd
     * @throws UnfinishedTaskException
     */
    static public void run(InputStream in, OutputStream out, String cmd) throws UnfinishedTaskException {
        try
        {
            Process p = Runtime.getRuntime().exec(tokenize(cmd));
            PipeWorker errLogger = new PipeWorker(p.getErrorStream(), System.err);
            Thread logThread = new Thread(errLogger);
            logThread.start();
            Thread thread = new Thread(new PipeWorker(in, p.getOutputStream()));
            thread.start();

            PipeWorker worker = new PipeWorker(p.getInputStream(), out);
            worker.run();

            p.waitFor();
            thread.join();
            logThread.join();
        }
        catch (IOException e)
        {
            throw new UnfinishedTaskException(e);
        }
        catch (InterruptedException e)
        {
            throw new UnfinishedTaskException(e);
        }

    }
}

class PipeWorker implements Runnable
{
    BufferedInputStream _in  = null;
    OutputStream        _out = null;

    public PipeWorker(InputStream in, OutputStream out)
    {
        _in = new BufferedInputStream(in);
        _out = out;
    }

    protected void finalize() {
    /*
     * try { if(_in != null) { _in.close(); _in = null; } if(_out != null) {
     * _out.close(); _out = null; } } catch (IOException e) {
     * System.err.println("PipeWorkder(finalize):" + e); }
     */
    }

    public void run() {
        try
        {
            int readBytes = 0;
            byte[] buffer = new byte[1024];
            while ((readBytes = _in.read(buffer)) != -1)
            {
                _out.write(buffer, 0, readBytes);
            }
            _out.flush();
            _in.close();

            if(_out != System.out && _out != System.err)
                _out.close();
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }
}

//--------------------------------------
// $Log: ProcessRunner.java,v $
// Revision 1.16  2004/09/06 09:16:43  leo
// command line を正確にtokenizeした
//
// Revision 1.15 2004/09/06 07:53:06 leo
// *** empty log message ***
//
// Revision 1.14 2004/09/06 07:04:55 leo
// *** empty log message ***
//
// Revision 1.13 2004/09/06 06:58:58 leo
// bufferを使ってみた
//
// Revision 1.12 2004/09/06 06:55:24 leo
// *** empty log message ***
//
// Revision 1.11 2004/09/06 06:50:44 leo
// *** empty log message ***
//
// Revision 1.10 2004/09/06 06:49:01 leo
// *** empty log message ***
//
// Revision 1.9 2004/09/06 06:46:25 leo
// *** empty log message ***
//
// Revision 1.8 2004/09/06 06:43:50 leo
// *** empty log message ***
//
// Revision 1.7 2004/09/06 06:22:54 leo
// debug用にログを出力するようにした
//
// Revision 1.6 2004/09/06 06:04:55 leo
// processとthreadの待ち時間の順を変更
//
// Revision 1.5 2004/09/06 06:02:22 leo
// debug用にログを出力するようにした
//
// Revision 1.4 2004/08/12 14:50:18 leo
// *** empty log message ***
//
// Revision 1.3 2004/08/06 14:42:36 leo
// add default constructor
//
// Revision 1.2 2004/07/13 15:42:16 leo
// *** empty log message ***
//
// Revision 1.1 2004/07/13 08:07:20 leo
// プロセスを起動してコマンドラインを実行するツールを導入
//
//--------------------------------------
