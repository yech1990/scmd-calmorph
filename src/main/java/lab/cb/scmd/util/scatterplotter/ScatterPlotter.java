//--------------------------------------
//SCMD Project
//
//ScatterPlotCanvas.java
//Since: 2004/06/13
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.scatterplotter;

import lab.cb.scmd.exception.SCMDException;
import lab.cb.scmd.util.table.FlatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * @author sesejun
 * <p>
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ScatterPlotter {
    private Shell shell;
    private Display display;
    private Label statusBar;
    private MenuItem mItemOpen;
    private ScatterPlotCanvas scatterPlot;
    private AxisCombo xaxisCombo, yaxisCombo;
    private Button axisChangeButton;
    private Table tableViewer;
    private Text searchBox;
    private TableColumn labelColumn;
    private TableColumn[] axisColumn = new TableColumn[2];

    private FlatTable _dataTable;
    private String _fileName = "";
    private String PROGNAME = "ScatterPlotter";
    private String[] EXTENSIONS = {"*.txt", "*.xls"};
    private String DELIMITER = ",";

    public Shell open(Display display) {
        this.display = display;
        shell = new Shell(display);
        shell.setText(PROGNAME);
        shell.setLayout(new GridLayout(1, true));
        Composite comp = new Composite(shell, SWT.NO_FOCUS);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        comp.setLayout(layout);
        GridData compGrid = new GridData();
        compGrid.horizontalAlignment = GridData.FILL;
        compGrid.verticalAlignment = GridData.FILL;
        compGrid.grabExcessHorizontalSpace = true;
        compGrid.grabExcessVerticalSpace = true;
        comp.setLayoutData(compGrid);

        shell.setLayout(new FillLayout());
        SashForm sash = new SashForm(comp, SWT.HORIZONTAL | SWT.NULL);
        GridData sashLayout = new GridData();
        sashLayout.horizontalAlignment = GridData.FILL;
        sashLayout.verticalAlignment = GridData.FILL;
        sashLayout.grabExcessHorizontalSpace = true;
        sashLayout.grabExcessVerticalSpace = true;
        sash.setLayoutData(sashLayout);
//	    sash.setWeights(new int[] {20,80});

        Composite leftComp = new Composite(sash, SWT.NO_FOCUS);
        GridLayout leftLayout = new GridLayout();
        leftLayout.numColumns = 3;
        leftComp.setLayout(leftLayout);
        GridData leftCompGrid = new GridData();
        leftCompGrid.horizontalAlignment = GridData.FILL;
        leftCompGrid.verticalAlignment = GridData.FILL;
        leftCompGrid.grabExcessHorizontalSpace = true;
        leftCompGrid.grabExcessVerticalSpace = true;
        leftComp.setLayoutData(leftCompGrid);

        // set menus
        Menu menubar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menubar);
        MenuItem mItemFile = new MenuItem(menubar, SWT.CASCADE);
        mItemFile.setText("File(&F)");
        Menu menuFile = new Menu(mItemFile);
        mItemFile.setMenu(menuFile);
        mItemOpen = new MenuItem(menuFile, SWT.PUSH);
        mItemOpen.setText("Open(&O)");
        new MenuItem(menuFile, SWT.SEPARATOR);
        MenuItem mItemExit = new MenuItem(menuFile, SWT.PUSH);
        mItemExit.setText("Exit(&X)");

        mItemOpen.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                getOpenFileName();

                if (_fileName != null) {
                    loadBegin();
                    try {
                        _dataTable = new FlatTable(_fileName, true, true);
                        scatterPlot.setFlatTable(_dataTable);
                        xaxisCombo.setColNames(_dataTable, 0);
                        yaxisCombo.setColNames(_dataTable, 1);
                        axisColumn[0].setText(_dataTable.getColLabel(0));
                        axisColumn[1].setText(_dataTable.getColLabel(1));
//						addColNamesToComboList(table);
                        scatterPlot.redraw();
                    } catch (SCMDException e1) {
                        System.err.println("No Such File Found.");
                    }
                    loadEnd();
                }
            }
        });

        mItemExit.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        // x axis
        xaxisCombo = new AxisCombo(leftComp, SWT.READ_ONLY);
        GridData xaxisGrid = new GridData();
        xaxisGrid.horizontalAlignment = GridData.FILL;
        xaxisGrid.grabExcessHorizontalSpace = true;
        xaxisCombo.add("X axis");
        xaxisCombo.select(0);
        xaxisCombo.setLayoutData(xaxisGrid);

        // y axis
        yaxisCombo = new AxisCombo(leftComp, SWT.READ_ONLY);
        GridData yaxisGrid = new GridData();
        yaxisGrid.horizontalAlignment = GridData.FILL;
        yaxisGrid.grabExcessHorizontalSpace = true;
        yaxisCombo.add("Y axis");
        yaxisCombo.select(0);
        yaxisCombo.setLayoutData(yaxisGrid);

        // button
        axisChangeButton = new Button(leftComp, SWT.NULL);
        GridData buttonGrid = new GridData(GridData.FILL_HORIZONTAL);
        axisChangeButton.setLayoutData(buttonGrid);
        axisChangeButton.setText("Change");

        axisChangeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int xcol = xaxisCombo.getSelectionIndex();
                int ycol = yaxisCombo.getSelectionIndex();
                scatterPlot.setAxisColIndex(0, xcol);
                scatterPlot.setAxisColIndex(1, ycol);
                axisColumn[0].setText(_dataTable.getColLabel(xcol));
                axisColumn[1].setText(_dataTable.getColLabel(ycol));
                scatterPlot.redraw();
            }
        });

        // graph
        scatterPlot = new ScatterPlotCanvas(leftComp, SWT.FULL_SELECTION | SWT.BORDER);
        GridData spGrid = new GridData();
        spGrid.horizontalAlignment = GridData.FILL;
        spGrid.verticalAlignment = GridData.FILL;
        spGrid.grabExcessHorizontalSpace = true;
        spGrid.grabExcessVerticalSpace = true;
        spGrid.horizontalSpan = 3;
        scatterPlot.setLayoutData(spGrid);

        //
        Composite rightComp = new Composite(sash, SWT.NO_FOCUS);
        GridLayout rightLayout = new GridLayout();
        rightLayout.numColumns = 1;
        rightComp.setLayout(rightLayout);
        GridData rightCompGrid = new GridData();
        rightCompGrid.horizontalAlignment = GridData.FILL;
        rightCompGrid.verticalAlignment = GridData.FILL;
        rightCompGrid.grabExcessHorizontalSpace = true;
        rightCompGrid.grabExcessVerticalSpace = true;
        rightComp.setLayoutData(rightCompGrid);

//	    //group
        Group searchGroup = new Group(rightComp, SWT.NONE);
        searchGroup.setText("Search");
        GridLayout searchGroupLayout = new GridLayout();
        searchGroupLayout.numColumns = 2;
        searchGroup.setLayout(searchGroupLayout);
        GridData searchGroupGrid = new GridData();
        searchGroupGrid.horizontalAlignment = GridData.FILL;
        //searchGroupGrid.verticalAlignment = GridData. GridData.FILL;
        searchGroupGrid.grabExcessHorizontalSpace = true;
        //searchGroupGrid.grabExcessVerticalSpace = true;
        searchGroup.setLayoutData(searchGroupGrid);


        searchBox = new Text(searchGroup, SWT.SINGLE | SWT.BORDER);
        searchBox.setText("");
        GridData searchBoxGrid = new GridData();
        searchBoxGrid.horizontalAlignment = GridData.FILL;
        searchBoxGrid.grabExcessHorizontalSpace = true;
        searchBox.setLayoutData(searchBoxGrid);

        Button searchButton = new Button(searchGroup, SWT.BORDER);
        searchButton.setText("Search");
        GridData searchButtonGrid = new GridData();
        searchButtonGrid.horizontalAlignment = GridData.FILL;
        searchButtonGrid.grabExcessHorizontalSpace = true;
        searchButton.setLayoutData(searchButtonGrid);

        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String searchStr = searchBox.getText();
                scatterPlot.setSelectedPoints(searchStr.split(DELIMITER));
                scatterPlot.redraw();
            }
        });

        // table
        tableViewer = new Table(rightComp, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        GridData tableGrid = new GridData();
        tableGrid.horizontalAlignment = GridData.FILL;
        tableGrid.verticalAlignment = GridData.FILL;
        tableGrid.grabExcessHorizontalSpace = true;
        tableGrid.grabExcessVerticalSpace = true;
        tableViewer.setLayoutData(tableGrid);
        tableViewer.setLinesVisible(true);
        tableViewer.setHeaderVisible(true);
        labelColumn = new TableColumn(tableViewer, SWT.LEFT);
        labelColumn.setText("Label");
        labelColumn.setWidth(100);
        axisColumn[0] = new TableColumn(tableViewer, SWT.LEFT);
        axisColumn[0].setText("X axis");
        axisColumn[0].setWidth(100);
        axisColumn[1] = new TableColumn(tableViewer, SWT.LEFT);
        axisColumn[1].setText("Y axis");
        axisColumn[1].setWidth(100);
        scatterPlot.setTableView(tableViewer);

        tableViewer.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TableItem[] selectedRows = tableViewer.getSelection();
                scatterPlot.setSelectedPoints(selectedRows);
                scatterPlot.redraw();
            }
        });


        // status bar
        statusBar = new Label(comp, SWT.LEFT);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        statusBar.setLayoutData(gridData);
        statusBar.setText("Select data file");
        scatterPlot.setStatusBar(statusBar);

        shell.open();
        return shell;
    }

    private void getOpenFileName() {
        FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
        openDialog.setFilterExtensions(EXTENSIONS);
        _fileName = openDialog.open();
    }

    private void loadBegin() {
        //［開く］メニューを選択不可にする
        mItemOpen.setEnabled(false);
        tableViewer.removeAll();
        scatterPlot.removeSelectedPoints();
        shell.setText(PROGNAME + " - " + _fileName);
        // ステータスバーに状態を表示
        statusBar.setText("Loading...");
    }

    private void loadEnd() {
        mItemOpen.setEnabled(true);
        statusBar.setText("Done");
    }

    private void close() {
        shell.close();
        shell.dispose();
    }

    public static void main(String[] args) {
        Display display = new Display();

        ScatterPlotter viewer = new ScatterPlotter();
        Shell shell = viewer.open(display);

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
