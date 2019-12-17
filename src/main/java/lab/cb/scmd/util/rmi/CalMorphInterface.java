package lab.cb.scmd.util.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Created on 2004/08/23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author sesejun
 * <p>
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface CalMorphInterface extends Remote {
    String[] getORFList(String clientname) throws RemoteException;
}
