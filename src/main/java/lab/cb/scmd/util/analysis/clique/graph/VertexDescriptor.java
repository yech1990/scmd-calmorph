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
    private Integer id; /* enumerate vertex */
    private String name;

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

    public static void main(String[] args) {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer i) {
        id = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String str) {
        name = str;
    }

}
