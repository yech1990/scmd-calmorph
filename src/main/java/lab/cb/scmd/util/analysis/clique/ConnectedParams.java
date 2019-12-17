/*
 * Created on 2004/12/21
 *
 */
package lab.cb.scmd.util.analysis.clique;

import java.util.HashSet;
import java.util.Iterator;


/**
 * @author sesejun
 */
public class ConnectedParams {
    String name = "";
    HashSet<ConnectedParams> connections = new HashSet<ConnectedParams>();

    public ConnectedParams(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addConnection(ConnectedParams connection) {
        connections.add(connection);
    }

    public boolean contains(ConnectedParams conn) {
        return connections.contains(conn);
    }

    public boolean contains(String name) {
        Iterator<ConnectedParams> it = connections.iterator();
        while (it.hasNext()) {
            ConnectedParams conn = it.next();
            if (conn.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
