package lab.cb.scmd.util.analysis.clique.graph;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/*
 * Created on 2003/10/25
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author sesejun
 * <p>
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AdjacencyListGraph extends GraphStructure {
    protected HashMap vertexList = new HashMap();
    /*
     * adjacencyList -- (id, TreeSet (id1, id2,...))
     */
    protected HashMap adjacencyList = new HashMap();

    public AdjacencyListGraph() {

    }

    public void addVertex(VertexDescriptor v) {
        Integer id = v.getId();
        vertexList.put(id, v);
        TreeSet ts = new TreeSet();
        adjacencyList.put(id, ts);
    }

    public VertexDescriptor getVertex(VertexDescriptor v) {
        return getVertex(v.getId());
    }

    public VertexDescriptor getVertex(Integer vertexId) {
        return (VertexDescriptor) vertexList.get(vertexId);
    }

    public boolean isInclude(Integer vertexId) {
        return vertexList.get(vertexId) != null;
    }

    public void addAdjacency(Integer vsid, Integer veid) {
        TreeSet ts;
        if (isDirectedGraph == false) {
            ts = (TreeSet) adjacencyList.get(vsid);
            ts.add(veid);
            ts = (TreeSet) adjacencyList.get(veid);
            ts.add(vsid);
        } else {
            ts = (TreeSet) adjacencyList.get(vsid);
            ts.add(veid);
        }
    }

    public void addAdjacency(VertexDescriptor vs, VertexDescriptor ve) {
        addAdjacency(vs.getId(), ve.getId());
    }

    public Object[] getAdjacency(Integer vertexId) {
        return ((TreeSet) adjacencyList.get(vertexId)).toArray();
    }

    public boolean hasAdjacency(Integer v1, Integer v2) {
        if (v1.intValue() > v2.intValue()) {
            Integer tmp = v1;
            v1 = v2;
            v2 = tmp;
        }
        Object[] adjs = getAdjacency(v1);
        for (int i = 0; i < adjs.length; i++) {
            if (v2.equals(adjs[i])) {
                return true;
            }
        }
        return false;
    }

    public void print() {
        PrintStream fOut = System.out;
        VertexDescriptor v;

        Set keyset = vertexList.keySet();
        Iterator keyiterator = keyset.iterator();
        while (keyiterator.hasNext()) {
            Integer id = (Integer) keyiterator.next();
            v = (VertexDescriptor) vertexList.get(id);
            fOut.print(v.getName() + "(" + v.getId() + ")" + ": ");
            Object[] ss = getAdjacency(id);
            //TreeSet ts = (TreeSet)adjacencyList.get(id);
            //Iterator iterator = ts.iterator();
            //while( iterator.hasNext() ) {
            for (int i = 0; i < ss.length; i++) {
                v = (VertexDescriptor) vertexList.get(ss[i]);
                fOut.print(v.getName() + "(" + v.getId() + ")" + ", ");
            }
            //}
            fOut.println();
        }
    }

    public Set getVertexIds() {
        return vertexList.keySet();
    }

    public static void main(String[] args) {
        AdjacencyListGraph g = new AdjacencyListGraph();

        VertexDescriptor v1 = new VertexDescriptor(new Integer(1), "a");
        VertexDescriptor v2 = new VertexDescriptor(new Integer(2), "b");
        VertexDescriptor v3 = new VertexDescriptor(new Integer(3), "c");
        VertexDescriptor v4 = new VertexDescriptor(new Integer(4), "d");
        VertexDescriptor v5 = new VertexDescriptor(new Integer(5), "e");
        VertexDescriptor v6 = new VertexDescriptor(new Integer(6), "f");

        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
        g.addVertex(v6);

        g.addAdjacency(v1, v2);
        g.addAdjacency(v1, v3);
        g.addAdjacency(v1, v4);
        g.addAdjacency(v2, v5);
        g.addAdjacency(v2, v4);

        g.print();
    }
}
