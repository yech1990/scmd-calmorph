//--------------------------------------
// SCMDProject
// 
// ThroughFilter.java 
// Since: 2004/08/07
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.xml;

/**
 * @author leo
 */
public class ThroughFilter implements TextContentFilter {

    /**
     *
     */
    public ThroughFilter() {
    }

    public String filter(String textContent) {
        return textContent;
    }

}


//--------------------------------------
// $Log: ThroughFilter.java,v $
// Revision 1.1  2004/08/07 12:30:11  leo
// Filterを切り替えられるようにしました
//
//--------------------------------------