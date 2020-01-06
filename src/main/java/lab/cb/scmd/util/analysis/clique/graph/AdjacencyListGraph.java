package lab.cb.scmd.util.analysis.clique.graph;

import java.io.PrintStream;
import java.util.HashMap;
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
    private HashMap<Integer, VertexDescriptor> vertexList = new HashMap<>();
    /*
     * adjacencyList -- (id, TreeSet (id1, id2,...))
     */
    private HashMap<Integer, TreeSet<Integer>> adjacencyList = new HashMap<>();

    public AdjacencyListGraph() {

    }

    public static void main(String[] args) {
        AdjacencyListGraph g = new AdjacencyListGraph();

        VertexDescriptor v1 = new VertexDescriptor(1, "a");
        VertexDescriptor v2 = new VertexDescriptor(2, "b");
        VertexDescriptor v3 = new VertexDescriptor(3, "c");
        VertexDescriptor v4 = new VertexDescriptor(4, "d");
        VertexDescriptor v5 = new VertexDescriptor(5, "e");
        VertexDescriptor v6 = new VertexDescriptor(6, "f");

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

    public void addVertex(VertexDescriptor v) {
        Integer id = v.getId();
        vertexList.put(id, v);
        TreeSet<Integer> ts = new TreeSet<>();
        adjacencyList.put(id, ts);
    }

    public VertexDescriptor getVertex(VertexDescriptor v) {
        return getVertex(v.getId());
    }

    public VertexDescriptor getVertex(Integer vertexId) {
        return vertexList.get(vertexId);
    }

    public boolean isInclude(Integer vertexId) {
        return vertexList.get(vertexId) != null;
    }

    public void addAdjacency(Integer vsid, Integer veid) {
        TreeSet<Integer> ts;
        if (!isDirectedGraph) {
            ts = adjacencyList.get(vsid);
            ts.add(veid);
            ts = adjacencyList.get(veid);
            ts.add(vsid);
        } else {
            ts = adjacencyList.get(vsid);
            ts.add(veid);
        }
    }

    public void addAdjacency(VertexDescriptor vs, VertexDescriptor ve) {
        addAdjacency(vs.getId(), ve.getId());
    }

    private Object[] getAdjacency(Integer vertexId) {
        return (adjacencyList.get(vertexId)).toArray();
    }

    public boolean hasAdjacency(Integer v1, Integer v2) {
        if (v1 > v2) {
            Integer tmp = v1;
            v1 = v2;
            v2 = tmp;
        }
        Object[] adjs = getAdjacency(v1);
        for (Object adj : adjs) {
            if (v2.equals(adj)) {
                return false;
            }
        }
        return true;
    }

    public void print() {
        PrintStream fOut = System.out;
        VertexDescriptor v;

        Set<Integer> keyset = vertexList.keySet();
        for (Integer id : keyset) {
            v = vertexList.get(id);
            fOut.print(v.getName() + "(" + v.getId() + ")" + ": ");
            Object[] ss = getAdjacency(id);
            //TreeSet ts = (TreeSet)adjacencyList.get(id);
            //Iterator iterator = ts.iterator();
            //while( iterator.hasNext() ) {
            for (Object s : ss) {
                v = vertexList.get(s);
                fOut.print(v.getName() + "(" + v.getId() + ")" + ", ");
            }
            //}
            fOut.println();
        }
    }

    public Set<Integer> getVertexIds() {
        return vertexList.keySet();
    }
}
