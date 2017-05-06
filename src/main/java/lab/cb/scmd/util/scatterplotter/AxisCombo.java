//--------------------------------------
//SCMD Project
//
//AxisCombo.java
//Since: 2004/06/13
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.scatterplotter;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import lab.cb.scmd.util.table.FlatTable;

/**
 * @author sesejun
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AxisCombo  {
	Combo axisCombo;
	public AxisCombo (Composite comp, int flag) {
		axisCombo = new Combo(comp, flag);
		axisCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				}
			});
	}

	/**
	 * @param table
	 */
	public void setColNames(FlatTable table, int selectPos ) {
		int colsize = table.getColSize();
		axisCombo.removeAll();
		for( int i = 0 ; i < colsize; i++ ) {
			axisCombo.add( table.getColLabel(i) );
		}
		axisCombo.select(selectPos);
	}

	/**
	 * @param string
	 */
	public void add(String string) {
		axisCombo.add(string);
	}

	/**
	 * @param i
	 */
	public void select(int i) {
		axisCombo.select(i);
	}

	/**
	 * @param xaxisGrid
	 */
	public void setLayoutData(GridData xaxisGrid) {
		axisCombo.setLayoutData(xaxisGrid);
	}
	
	public int getSelectionIndex() {
		return axisCombo.getSelectionIndex();
	}
}
