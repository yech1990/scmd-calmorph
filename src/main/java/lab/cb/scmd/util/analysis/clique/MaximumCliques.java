package lab.cb.scmd.util.analysis.clique;

import lab.cb.scmd.util.analysis.clique.graph.AdjacencyListGraph;
import lab.cb.scmd.util.analysis.clique.graph.VertexDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/*
 * Created on 2003/12/17
 *
 */

/**
 * @author sesejun
 */
public class MaximumCliques {
    public static void main(String[] args) {
        AdjacencyListGraph g = new AdjacencyListGraph();

        VertexDescriptor v1 = new VertexDescriptor(1, "a");
        VertexDescriptor v2 = new VertexDescriptor(2, "b");
        VertexDescriptor v3 = new VertexDescriptor(3, "c");
        VertexDescriptor v4 = new VertexDescriptor(4, "d");
        VertexDescriptor v5 = new VertexDescriptor(5, "e");

        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);

        g.addAdjacency(v1, v2);
        g.addAdjacency(v1, v3);
        g.addAdjacency(v1, v4);
        g.addAdjacency(v2, v5);
        g.addAdjacency(v2, v4);
        g.addAdjacency(v4, v5);

        MaximumCliques mc = new MaximumCliques();
        ArrayList<Integer[]> al = mc.maximumCliques(g);
        for (Object o : al) {
            Integer[] list = (Integer[]) o;
            for (Integer integer : list) {
                System.out.print("\t" + integer);
            }
            System.out.println();
        }
    }

    ArrayList<Integer[]> maximumCliques(AdjacencyListGraph adjGraph) {
        ArrayList<Integer[]> cliques;
        Set vertexIds = adjGraph.getVertexIds();
        Object[] vertexArray = vertexIds.toArray();
        Arrays.sort(vertexArray);
        int vertexSize = vertexArray.length;
        ArrayList<Integer[]> latticeNodes = new ArrayList<>();
        int n = 1;
        ArrayList<Integer[]> maxCliques = new ArrayList<>();
        for (Object o : vertexArray) {
            Integer[] vertexId = new Integer[1];
            vertexId[0] = (Integer) o;
            latticeNodes.add(vertexId);
        }
        cliques = maximumCliquesBFS(latticeNodes, n, adjGraph, maxCliques);
        while (cliques.size() != 0) {
            //	System.out.println("depth: " + n + "\tsize: " + cliques.size());
            n++;
            cliques = maximumCliquesBFS(cliques, n, adjGraph, maxCliques);
        }

        return maxCliques;
    }

    /**
     * @param
     * @return
     */
    private ArrayList<Integer[]> maximumCliquesBFS(ArrayList<Integer[]> nodes, int depth,
                                                   AdjacencyListGraph adjGraph, ArrayList<Integer[]> maxCliques) {
        int size = nodes.size();
        Integer[] baseVertexIds;
        Integer[] objVertexIds;
        Integer[] nVertexIds;
        ArrayList<Integer[]> nnodes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            baseVertexIds = nodes.get(i);
            for (int j = i + 1; j < size; j++) {
                objVertexIds = nodes.get(j);
                nVertexIds = mergeVertex(baseVertexIds, objVertexIds, depth, adjGraph);
                if (nVertexIds == null)
                    break;
                if (nVertexIds.length == 0)
                    continue;

                nnodes.add(nVertexIds);
                addMaxCliques(nVertexIds, maxCliques);
            }
        }

        return nnodes;
    }

    /**
     *
     */
    public ArrayList<Integer[]> findConnectedComponents(AdjacencyListGraph adjGraph) {
        ArrayList<Integer[]> components;
        Set vertexIds = adjGraph.getVertexIds();
        Object[] vertexArray = vertexIds.toArray();
        Arrays.sort(vertexArray);
        int vertexSize = vertexArray.length;
        ArrayList<Integer[]> latticeNodes = new ArrayList<>(); // all nodes in adjGraph
        int n = 1;
        for (Object o : vertexArray) {
            Integer[] vertexId = new Integer[1];
            vertexId[0] = (Integer) o;
            latticeNodes.add(vertexId);
        }
        if (vertexSize <= 0)
            return new ArrayList<>();
        ArrayList<Integer[]> connectedComponents = new ArrayList<>();
        while (latticeNodes.size() > 0) {
            ArrayList<Integer[]> nnodes = new ArrayList<>();
            nnodes.add(latticeNodes.get(0));
            nnodes = findConnectedComponentsBFS(latticeNodes, n, adjGraph, connectedComponents, latticeNodes.get(0), nnodes);
            Integer[] component = new Integer[nnodes.size()];
            for (int i = 0; i < nnodes.size(); i++) {
                latticeNodes.remove(nnodes.get(i));
                Integer[] element = nnodes.get(i);
                component[i] = element[0];
            }
//            System.out.print(nnodes.size() + "\t" + latticeNodes.size());
//            for(int i = 0; i < nnodes.size(); i++ ) {
//                System.out.print("\t" + component[i]);
//            }
//            System.out.println();
            connectedComponents.add(component);
        }

        return connectedComponents;
    }

    /**
     * @param nodes
     * @param depth
     * @param adjGraph
     * @param maxComponents
     * @return
     */
    private ArrayList<Integer[]> findConnectedComponentsBFS(ArrayList<Integer[]> nodes, int depth,
                                                            AdjacencyListGraph adjGraph, ArrayList<Integer[]> maxComponents) {
        int size = nodes.size();
        Integer[] baseVertexIds;
        Integer[] objVertexIds;
        ArrayList<Integer[]> nnodes = new ArrayList<>();
        int i = 0;
        baseVertexIds = nodes.get(i);
        for (int j = i + 1; j < size; j++) {
            objVertexIds = nodes.get(j);
            if (adjGraph.hasAdjacency(baseVertexIds[0], objVertexIds[0]))
                continue;
            if (!nodes.contains(objVertexIds))
                continue;
            nnodes.add(objVertexIds);
            nnodes = findConnectedComponentsBFS(nodes, depth + 1, adjGraph, maxComponents, objVertexIds, nnodes);

//            nVertexIds = mergeVertex(baseVertexIds, objVertexIds, depth, adjGraph);
//            if( nVertexIds == null )
//                break;
//            if( nVertexIds.length == 0 )
//                continue;
//            nnodes.add(nVertexIds);
//            maxComponents.add(nVertexIds);
//            //addMaxCliques(nVertexIds, maxComponents);
        }

        return nnodes;
    }

    /**
     * @param nodes
     * @param depth
     * @param adjGraph
     * @param maxComponents
     * @param baseVertexIds
     * @param nnodes
     */
    private ArrayList<Integer[]> findConnectedComponentsBFS(ArrayList<Integer[]> nodes, int depth,
                                                            AdjacencyListGraph adjGraph, ArrayList<Integer[]> maxComponents,
                                                            Integer[] baseVertexIds, ArrayList<Integer[]> nnodes) {
        int size = nodes.size();
        Integer[] objVertexIds;
        Integer[] nVertexIds;
        int i = baseVertexIds[0];
        for (int j = 0; j < size; j++) {
            objVertexIds = nodes.get(j);
            if (i == objVertexIds[0])
                continue;
            if (adjGraph.hasAdjacency(baseVertexIds[0], objVertexIds[0]))
                continue;
            if (nnodes.contains(objVertexIds))
                continue;
            nnodes.add(objVertexIds);
            nnodes = findConnectedComponentsBFS(nodes, depth + 1, adjGraph, maxComponents, objVertexIds, nnodes);
        }
        return nnodes;
    }

    /**
     * @param nVertexIds
     * @param maxCliques
     */
    private void addMaxCliques(Integer[] nVertexIds, ArrayList<Integer[]> maxCliques) {
        for (int i = 0; i < maxCliques.size(); i++) {
            if (isSubClique(maxCliques.get(i), nVertexIds)) {
                maxCliques.remove(i);
                i--;
            }
        }
        maxCliques.add(nVertexIds);
    }

    /**
     * @param child
     * @param parent
     * @return
     */
    private boolean isSubClique(Integer[] child, Integer[] parent) {
        int c = 0, p = 0;
        while (c < child.length && p < parent.length) {
            if (child[c].equals(parent[p])) {
                c++;
                p++;
            } else if (!child[c].equals(parent[p])) {
                p++;
            }
        }
        return c == child.length;
    }

    /**
     * @param baseVertex
     * @param objVertex
     * @return
     */
    private Integer[] mergeVertex(Integer[] baseVertex, Integer[] objVertex, int depth, AdjacencyListGraph adjGraph) {
        if (depth <= 1 || hasSamePrefix(baseVertex, objVertex, depth - 1)) {
            Integer[] nVertex = new Integer[depth + 1];
            if (depth >= 0) System.arraycopy(baseVertex, 0, nVertex, 0, depth);
            if (hasCompleteLinkage(baseVertex, objVertex[depth - 1], adjGraph)) {
                nVertex[depth] = objVertex[depth - 1];
            } else {
                return new Integer[0];
            }
            return nVertex;
        }
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param baseVertex
     * @param objVertex
     * @param adjGraph
     * @return
     */
    private boolean hasCompleteLinkage(Integer[] baseVertex, Integer objVertex, AdjacencyListGraph adjGraph) {

        for (Integer vertex : baseVertex) {
            if (adjGraph.hasAdjacency(vertex, objVertex)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param baseVertex
     * @param objVertex
     * @param n
     * @return
     */
    private boolean hasSamePrefix(Integer[] baseVertex, Integer[] objVertex, int n) {
        for (int i = 0; i < n; i++) {
            if (!baseVertex[i].equals(objVertex[i])) {
                return false;
            }
        }
        return true;
    }

}
