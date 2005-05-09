package lab.cb.scmd.util.analysis.clique;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import lab.cb.scmd.util.analysis.clique.graph.AdjacencyListGraph;
import lab.cb.scmd.util.analysis.clique.graph.VertexDescriptor;

/*
 * Created on 2003/12/17
 *
 */

/**
 * @author sesejun
 *
 */
public class MaximumCliques {
	public static void main(String[] args) {
		AdjacencyListGraph g = new AdjacencyListGraph();
		
		VertexDescriptor v1 = new VertexDescriptor(new Integer(1), "a");
		VertexDescriptor v2 = new VertexDescriptor(new Integer(2), "b");
		VertexDescriptor v3 = new VertexDescriptor(new Integer(3), "c");
		VertexDescriptor v4 = new VertexDescriptor(new Integer(4), "d");
		VertexDescriptor v5 = new VertexDescriptor(new Integer(5), "e");
		
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);
		g.addVertex(v5);
		
		g.addAdjacency(v1,v2);
		g.addAdjacency(v1,v3);
		g.addAdjacency(v1,v4);
		g.addAdjacency(v2,v5);
		g.addAdjacency(v2,v4);
		g.addAdjacency(v4,v5);
		
		MaximumCliques mc = new MaximumCliques();
		ArrayList al = mc.maximumCliques(g);
		for( int i = 0; i < al.size(); i++ ) {
			Integer [] list = (Integer [])al.get(i);
			for(int j = 0; j < list.length; j++ ) {
				System.out.print("\t" + list[j]);
			}
			System.out.println();
		}
	}
	
	public ArrayList maximumCliques(AdjacencyListGraph adjGraph) {
		ArrayList cliques;
		Set vertexIds = adjGraph.getVertexIds();
		Object[] vertexArray = vertexIds.toArray();
		Arrays.sort(vertexArray);
		int vertexSize = vertexArray.length;
		ArrayList latticeNodes = new ArrayList();
		int n = 1;
		ArrayList maxCliques = new ArrayList();
		for( int i = 0; i < vertexSize; i++ ) {
			Integer [] vertexId = new Integer[1];
			vertexId[0] = (Integer)vertexArray[i];
			latticeNodes.add(vertexId);
		}
		cliques = maximumCliquesBFS(latticeNodes, n, adjGraph, maxCliques );
		while( cliques.size() != 0 ) {
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
	private ArrayList maximumCliquesBFS(ArrayList nodes, int depth, 
		AdjacencyListGraph adjGraph, ArrayList maxCliques ) {
		int size = nodes.size();
		Integer [] baseVertexIds;
		Integer [] objVertexIds;
		Integer [] nVertexIds;
		ArrayList nnodes = new ArrayList();
		for(int i = 0; i < size; i++ ) {
			baseVertexIds = (Integer [])nodes.get(i);
			for( int j = i + 1; j < size; j++ ) {
				objVertexIds = (Integer [])nodes.get(j);
				nVertexIds = mergeVertex(baseVertexIds, objVertexIds, depth, adjGraph);
				if( nVertexIds == null )
					break;
				if( nVertexIds.length == 0 )
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
        ArrayList<Integer[]> latticeNodes = new ArrayList<Integer[]>(); // all nodes in adjGraph
        int n = 1;
        for( int i = 0; i < vertexSize; i++ ) {
            Integer [] vertexId = new Integer[1];
            vertexId[0] = (Integer)vertexArray[i];
            latticeNodes.add(vertexId);
        }
        if(vertexSize <= 0 )
            return new ArrayList<Integer[]> (); 
        ArrayList<Integer[]>  connectedComponents = new ArrayList<Integer[]> ();  
        while( latticeNodes.size() > 0  ) {
            ArrayList<Integer[]> nnodes = new ArrayList<Integer[]> ();
            nnodes.add(latticeNodes.get(0));
            nnodes = findConnectedComponentsBFS(latticeNodes, n, adjGraph, connectedComponents, latticeNodes.get(0), nnodes);
            Integer[] component = new Integer [nnodes.size()]; 
            for( int i = 0; i < nnodes.size(); i++ ) {
                latticeNodes.remove(nnodes.get(i));
                Integer[] element =  nnodes.get(i);
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
     * @param latticeNodes
     * @param n
     * @param adjGraph2
     * @param maxComponents
     * @return
     */
    private ArrayList<Integer[]> findConnectedComponentsBFS(ArrayList<Integer[]> nodes, int depth, 
                AdjacencyListGraph adjGraph, ArrayList<Integer[]> maxComponents) {
        int size = nodes.size();
        Integer [] baseVertexIds;
        Integer [] objVertexIds;
        Integer [] nVertexIds;
        ArrayList<Integer[]> nnodes = new ArrayList<Integer[]>();
        int i = 0;
        baseVertexIds = (Integer [])nodes.get(i);
        for( int j = i + 1; j < size; j++ ) {
            objVertexIds = (Integer [])nodes.get(j);
            if( !adjGraph.hasAdjacency(baseVertexIds[0], objVertexIds[0]) )
                continue;
            if( !nodes.contains(objVertexIds) )
                continue;
            nnodes.add(objVertexIds);
            nnodes = findConnectedComponentsBFS(nodes, depth+1, adjGraph, maxComponents, objVertexIds, nnodes);
                
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
     * @param i
     * @param adjGraph
     * @param maxComponents
     * @param objVertexIds
     */
    private ArrayList<Integer[]> findConnectedComponentsBFS(ArrayList<Integer[]> nodes, int depth, 
            AdjacencyListGraph adjGraph, ArrayList<Integer[]> maxComponents, 
            Integer[] baseVertexIds, ArrayList<Integer[]> nnodes) {
        int size = nodes.size();
        Integer [] objVertexIds;
        Integer [] nVertexIds;
        int i = baseVertexIds[0].intValue();
        for( int j = 0; j < size; j++ ) {
            objVertexIds = (Integer [])nodes.get(j);
            if( i == objVertexIds[0].intValue() )
                continue;
            if( !adjGraph.hasAdjacency(baseVertexIds[0], objVertexIds[0]) )
                continue;
            if( nnodes.contains(objVertexIds) )
                continue;
            nnodes.add(objVertexIds);
            nnodes = findConnectedComponentsBFS(nodes, depth+1, adjGraph, maxComponents, objVertexIds, nnodes);
        }
        return nnodes;
    }

    /**
	 * @param nVertexIds
	 * @param maxCliques
	 */
	private void addMaxCliques(Integer[] nVertexIds, ArrayList maxCliques) {
		for(int i = 0; i < maxCliques.size(); i++ ) {
			if( isSubClique( (Integer[])maxCliques.get(i), nVertexIds ) ) {
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
		while( c < child.length && p < parent.length ) {
			if( child[c] == parent[p] ) {
				c++;
				p++;
			} else if ( child[c] != parent[p]) {
				p++;
			}
		}
		if( c == child.length )
			return true;
		return false;
	}

	/**
	 * @param baseVertex
	 * @param objVertex
	 * @return
	 */
	private Integer[] mergeVertex( Integer [] baseVertex, Integer[] objVertex, int depth, AdjacencyListGraph adjGraph) {
		if( depth <= 1 || hasSamePrefix(baseVertex, objVertex, depth - 1) ) {
			Integer [] nVertex = new Integer[depth + 1];
			for( int i = 0; i < depth ; i++ ) {
				nVertex[i] = baseVertex[i];
			}
			if( hasCompleteLinkage(baseVertex, objVertex[depth-1], adjGraph) ) {
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
	 * @param integer
	 * @param adjGraph
	 * @return
	 */
	private boolean hasCompleteLinkage(Integer[] baseVertex, Integer objVertex, AdjacencyListGraph adjGraph) {
	
		for( int i = 0 ; i < baseVertex.length; i++ ) {
			if( !adjGraph.hasAdjacency(baseVertex[i], objVertex) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param baseVertex
	 * @param objVertex
	 * @param i
	 * @return
	 */
	private boolean hasSamePrefix(Integer[] baseVertex, Integer[] objVertex, int n) {
		for( int i = 0; i < n; i++ ) {
			if( baseVertex[i] != objVertex[i] ) {
				return false; 
			}
		}
		return true;
	}
	
}
