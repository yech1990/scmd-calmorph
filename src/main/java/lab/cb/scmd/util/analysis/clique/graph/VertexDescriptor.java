package lab.cb.scmd.util.analysis.clique.graph;
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
public class VertexDescriptor {
    Integer id; /* enumerate vertex */
    String name;

    public static void main(String[] args) {
    }

    public VertexDescriptor() {
    }

    VertexDescriptor(Integer i) {
        id = i;
        setName(i.toString());
    }

    public VertexDescriptor(Integer i, String str) {
        id = i;
        setName(str);
    }

    public void setId(Integer i) {
        id = i;
    }

    public Integer getId() {
        return id;
    }

    public void setName(String str) {
        name = str;
    }

    public String getName() {
        return name;
    }

}
