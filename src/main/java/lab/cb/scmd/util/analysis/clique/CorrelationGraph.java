package lab.cb.scmd.util.analysis.clique;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

import lab.cb.scmd.util.analysis.clique.graph.AdjacencyListGraph;
import lab.cb.scmd.util.analysis.clique.graph.VertexDescriptor;
import lab.cb.scmd.util.analysis.table.DataMatrix;

/*
 * Created on 2003/12/17
 *
 */

/**
 * @author sesejun
 *
 * compute connected components or cliques
 * between parameters. 
 * Export format is for GraphViz
 *
 * 2005/05/10 add cramer's v option
 */
public class CorrelationGraph {
    protected DataMatrix dm = new DataMatrix();
    protected DataMatrix nm = new DataMatrix();
    protected AdjacencyListGraph adjGraph = new AdjacencyListGraph();
    protected double THRESHOLD = 0.90;
    protected boolean isSmallerCorrelation = false;
    boolean findclique = false;
    boolean countComponents = false;
    boolean coloredNodes = false;
    boolean showTable = false;
    protected double[][] corMatrix;
    boolean USECRAMERSV = true;

    public static void main(String[] args) {
        int num = 0;
        CorrelationGraph cc = new CorrelationGraph();
        if( args[num].equals("-c") ) {
            cc.setFindClique( true );
            num++;
        }
        if( args[num].equals("-t") ) {
            cc.setShowTable( true );
            num++;
        }
        if( args[num].equals("-s") ) {
            cc.setSmallerCorrelation( true );
            num++;
        }
        if( args[num].equals("-n") ) {
            cc.setConnectedComponents( true );
            num++;
        }
        if( args[num].equals("-m")) {
            cc.setCountComponents(true);
            num++;
        }
        if( args[num].equals("-r")) {
            cc.setColoredNodes( true );
            num++;
        }
        if( args[num].equals("-v")) {
            cc.setUseCramersV(true);
            num++;
        }
        cc.setThreshold(Double.parseDouble(args[num++]));
        cc.load(args[num++]);
        String [] genes = new String [args.length - num];
        for( int i = num; i < args.length; i++ ) {
            genes[i-num] = args[i];
        }
        cc.computeCorrelations(genes);
    }

    /**
     * @param b
     */
    private void setUseCramersV(boolean b) {
        USECRAMERSV = b;
    }

    /**
     * @param b
     */
    private void setCountComponents(boolean b) {
        countComponents = b;
    }

    /**
     * @param b
     */
    private void setColoredNodes(boolean b) {
        coloredNodes = b;
    }

    /**
     * @param b
     */
    private void setConnectedComponents(boolean b) {
        countComponents = true;
    }

    /**
     * @param b
     */
    private void setShowTable(boolean b) {
        showTable = b;
    }

    /**
     * @param b
     */
    private void setSmallerCorrelation(boolean b) {
        isSmallerCorrelation = b;
    }

    /**
     * @param b
     */
    private void setFindClique(boolean b) {
        findclique = b;
    }

    /**
     * @param d
     */
    private void setThreshold(double d) {
        THRESHOLD = d;
    }

    /**
     *
     */
    private void computeCorrelations(Object [] genes) {
        int size = dm.getColumnSize();
        if( genes.length == 0 ) {
            genes = dm.getAllRowName().toArray();
        }
        nm = dm.subMatrix(genes, dm.getAllColumnName().toArray());
        double [] row1;
        double [] row2;
        VertexDescriptor v;
        for( int i = 0; i < size; i++ ) {
            v = new VertexDescriptor(new Integer(i), nm.getColumnName(i));
            adjGraph.addVertex(v);
        }

        corMatrix = new double [size][size];
        boolean[] usedParams = new boolean[size];
        for( int i = 0; i < size; i++ ) {
            row1 = nm.getOneColumn(i);
            boolean isUsed = true;
            for( int j = i + 1; j < size; j++ ) {
                row2 = nm.getOneColumn(j);
                double cor = 0.0;
                if( USECRAMERSV ) {
                    // pearson's correlation
                    cor = correlation(row1, row2);
                } else {
                    // p-value using binomial test
                    cor = cramersv(row1, row2);
                }
                corMatrix[i][j] = cor;
                // for finding maximum clique
                if( ( isSmallerCorrelation == false && ( cor >= THRESHOLD || cor <= -1.0 * THRESHOLD ) ) ||
                        ( isSmallerCorrelation == true && cor <= THRESHOLD && cor >= -1.0 * THRESHOLD ) ) {
                    adjGraph.addAdjacency(new Integer(i), new Integer(j));
                    isUsed = true;
                }
            }
            usedParams[i] = isUsed;
        }
        if( showTable ) {
            System.out.print("Corr");
            for(int i = 1; i < size; i++ ) {
                System.out.print("\t" + nm.getColumnName(i));
            }
            System.out.println();
            for(int i = 0; i < size; i++ ) {
                for( int j = 1; j < i + 1; j++) {
                    System.out.print("\t");
                }
                System.out.print(nm.getColumnName(i));
                for( int j = i + 1; j < size; j++ ) {
                    System.out.print("\t" + corMatrix[i][j]);
                }
                System.out.println();
            }
        }else if( findclique ) {
            maximumCliques();
        } else if( countComponents ) {
            countConnectedComponents();
        } else {
            printAdjacencies();
        }
    }

    /**
     * @param row1
     * @param row2
     * @return
     */
    private double cramersv(double[] row1, double[] row2) {
        // Both row1 and row2 take -1, 0, 1.
        int[][] count = new int [3][3];
        int[] ni = new int[3];
        int[] nj = new int[3];

        for( int i = 0; i < 3; i++ )
            for( int j = 0; j < 3; j++ )
                count[i][j] = 0;
        for( int i = 0; i < 3; i++ ) {
            ni[i] = 0;
            nj[i] = 0;
        }

        int n = row1.length;
        for(int i = 0; i < n; i++ ) {
            int r1 = 0;
            int r2 = 0;
            if( row1[i] < 0 )
                r1 = 2;
            else if( row1[i] > 0 )
                r1 = 1;
            else
                r1 = 0;
            if( row2[i] < 0 )
                r2 = 2;
            else if ( row2[i] > 0 )
                r2 = 1;
            else
                r2 = 0;
            count[r1][r2]++;
            ni[r1]++;
            nj[r2]++;
        }

        double chisqr = 0.0;
        for( int i = 0; i < 3; i++ )
            for( int j = 0; j < 3; j++) {
                double eij = ni[i] * nj[j] / (double)n;
                chisqr += ( count[i][j] - eij ) * ( count[i][j] - eij ) / eij;
            }
        double phi = Math.sqrt(chisqr / n);
        double cramersV = phi / Math.sqrt(3 - 1);
        return cramersV;
    }

    /**
     *
     */
    private void printAdjacencies() {
        System.out.println("graph Corr { ");
        int size = dm.getColumnSize();
        HashSet<Integer> availableList = new HashSet<Integer> ();
        HashSet<Integer> components = new HashSet<Integer>();
        for( int i = 0; i < size; i++ ) {
            availableList.add(i);
        }
        if( coloredNodes == true ) {
            System.out.println("\tnode [style=filled];");
            for( int i = 0; i < size; i++ ) {
                String color = "";
                if( dm.getColumnName(i).charAt(0) == 'A') {
                    System.out.println("\t\"" + dm.getColumnName(i) + "\" [color=darkorange1];");
                }
                if( dm.getColumnName(i).charAt(0) == 'C') {
                    System.out.println("\t\"" + dm.getColumnName(i) + "\" [color=greenyellow];");
                }
                if( dm.getColumnName(i).charAt(0) == 'D') {
                    System.out.println("\t\"" + dm.getColumnName(i) + "\" [color=cadetblue1];");
                }
            }
        }
        int count = 0;
        for( int i = 0; i < size; i++ ) {
            for( int j = i + 1; j < size; j++ ) {
                double cor = corMatrix[i][j];
                if( ( isSmallerCorrelation == false && ( cor >= THRESHOLD || cor <= -1.0 * THRESHOLD ) ) ||
                        ( isSmallerCorrelation == true && cor <= THRESHOLD && cor >= -1.0 * THRESHOLD ) ) {
                    System.out.print("\t\"" + dm.getColumnName(i) + "\" -- ");
                    System.out.print("\"" + dm.getColumnName(j) + "\"");
                    DecimalFormat exFormat = new DecimalFormat("##.#####");
                    if( coloredNodes != true ) {
                        System.out.print(" [label=\"" + exFormat.format( cor ) + "\"]");
                    }
                    System.out.println(";");
                    if(availableList.contains(i))
                        availableList.remove(i);
                    if(availableList.contains(j))
                        availableList.remove(j);

                    if(components.contains(i) && components.contains(j)) {
                        count--;
                    } else if ( !components.contains(i) && !components.contains(j) ) {
                        count++;
                        components.add(i);
                        components.add(j);
                    }
                }
            }
        }
        for(Integer i: availableList) {
            System.out.println("\t\"" + dm.getColumnName(i) + "\";");
        }
        System.out.println("}");
        //System.err.println("Threshold:\t" + THRESHOLD + "\tsize:\t" + components.size());
        System.err.println("Threshold:\t" + THRESHOLD + "\tsize:\t" + count + "\t" + availableList.size());
    }

    /**
     *
     */
    private void countConnectedComponents() {
        MaximumCliques mc = new MaximumCliques();
        ArrayList cliques = mc.findConnectedComponents(adjGraph);

        System.err.println("Threshold: " + THRESHOLD + "\t" + "Components: " + cliques.size());
        System.out.println("graph CLIQUE { ");
        for( int i = 0; i < cliques.size(); i++ ) {
            Integer [] list = (Integer [])cliques.get(i);
            /*
            for(int j = 0; j < list.length; j++ ) {
                System.out.print("\t" + list[j] + "(" + dm.getColumnName(list[j].intValue()) + ")");
            }
            System.out.println();
            */
            for( int j = 0; j < list.length; j++ ) {
                for( int k = j + 1; k < list.length; k++ ) {
                    System.out.print("\t\"" + dm.getColumnName(list[j].intValue()) + "\" -- ");
                    System.out.print("\"" + dm.getColumnName(list[k].intValue()) + "\"");
                    DecimalFormat exFormat = new DecimalFormat("##.#####");
                    int n = list[j].intValue();
                    int m = list[k].intValue();
                    if( n > m ) {
                        int tmp = n;
                        n = m;
                        m = tmp;
                    }
                    System.out.print(" [label=\"" + exFormat.format( corMatrix[n][m] ) + "\"];\n");
                }
            }
            if( list.length > 1 )
                System.out.println();
        }
        System.out.println("}");
    }

    /**
     *
     */
    private void maximumCliques() {
        MaximumCliques mc = new MaximumCliques();
        ArrayList cliques = mc.maximumCliques(adjGraph);
        System.out.println("graph CLIQUE { ");
        for( int i = 0; i < cliques.size(); i++ ) {
            Integer [] list = (Integer [])cliques.get(i);
            /*
            for(int j = 0; j < list.length; j++ ) {
                System.out.print("\t" + list[j] + "(" + dm.getColumnName(list[j].intValue()) + ")");
            }
            System.out.println();
            */
            for( int j = 0; j < list.length; j++ ) {
                for( int k = j + 1; k < list.length; k++ ) {
                    System.out.print("\t\"" + dm.getColumnName(list[j].intValue()) + "\" -- ");
                    System.out.print("\"" + dm.getColumnName(list[k].intValue()) + "\"");
                    DecimalFormat exFormat = new DecimalFormat("##.#####");
                    System.out.print(" [label=\"" + exFormat.format( corMatrix[list[j].intValue()][list[k].intValue()] ) + "\"];\n");
                }
            }
            System.out.println();
        }
        System.out.println("}");
    }

    /**
     * @param row1
     * @param row2
     * @return
     */
    private double correlation(double[] row1, double[] row2) {
        ArrayStats stat = new ArrayStats();
        double avg1 = stat.avg(row1);
        double avg2 = stat.avg(row2);
        int size = row1.length;
        double cov = 0.0;
        double var1 = 0.0, var2 = 0.0;
        double diff1, diff2;
        for( int i = 0; i < size; i++ ) {
            if( Double.isNaN(row1[i]) || Double.isNaN(row2[i]))
                continue;
            diff1 = row1[i] - avg1;
            diff2 = row2[i] - avg2;
            cov += diff1 * diff2;
            var1 += diff1 * diff1;
            var2 += diff2 * diff2;
        }
        var1 = Math.sqrt(var1);
        var2 = Math.sqrt(var2);

        return cov / ( var1 * var2 );
    }

    /**
     * @param string
     */
    private void load(String filename) {
        dm.load(filename);
    }
}
