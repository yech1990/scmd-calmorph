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

import lab.cb.scmd.util.table.FlatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;

/**
 * @author sesejun
 * <p>
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ScatterPlotCanvas extends Canvas {
    private boolean _mouseDown;
    private Color _fgColor = new Color(null, 0, 0, 0);
    private Color _bgColor = new Color(null, 255, 255, 255);
    private Color _pointColor = new Color(null, 0, 0, 255);
    private Color _focusedColor = new Color(null, 255, 0, 0);
    private FlatTable _flatTable = null;
    private boolean _selectingRegion = false;
    private int[] _selectedRegionStart = {0, 0};
    private int[] _selectedRegionEnd = {0, 0};
    private int[] _axisColIndex = {0, 1};
    private double[] _realMax = {0.0, 0.0};
    private double[] _realMin = {0.0, 0.0};
    private int[] _canvasSize = {0, 0};
    private int[] _selectedPoints = new int[0];
    private Table _tableViewer;
    private Label _statusBar;
    private int TOPMARGIN = 30;
    private int BOTTOMMARGIN = 30; // including axis
    private int LEFTMARGIN = 30; // including axis
    private int RIGHTMARGIN = 5;
    private int OVALSIZE = 5;
    private MouseAdapter mouseAdapter = new MouseAdapter() {
        public void mouseDown(MouseEvent e) {
            _mouseDown = true;
        }

        public void mouseUp(MouseEvent e) {
            _mouseDown = false;
            if (_selectingRegion) {
                _selectingRegion = false;
                ArrayList<Integer> selectedrows = selectRows();
                //System.out.println("Mouse Up at " + _selectedRegionEnd[0] + ":" + _selectedRegionEnd[1] );
            }
        }

    };
    private MouseMoveListener mouseMoveListener = new MouseMoveListener() {
        public void mouseMove(MouseEvent e) {
            if (_mouseDown) {
                if (!_selectingRegion) {
                    //System.out.println("Mouse Down at " + e.x + ":" + e.y);
                    _selectingRegion = true;
                    _selectedRegionStart[0] = e.x;
                    _selectedRegionStart[1] = e.y;
                }
                _selectedRegionEnd[0] = e.x;
                _selectedRegionEnd[1] = e.y;
                redraw();
            }
        }
    };

    ScatterPlotCanvas(Composite comp, int flag) {
        super(comp, flag);

        // event listener
        this.addPaintListener(e -> redraw());
        this.addMouseMoveListener(e -> _statusBar.setText("(" + posToRealValue(0, e.x) + ", " + posToRealValue(1, e.y) + ")"));

        this.addMouseListener(mouseAdapter);
        this.addMouseMoveListener(mouseMoveListener);
    }

    void setFlatTable(FlatTable flatTable) {
        _flatTable = flatTable;
        for (int i = 0; i < _axisColIndex.length; i++) {
            setMaxAndMinValues(i);
        }
    }

    void setAxisColIndex(int dim, int n) {
        _axisColIndex[dim] = n;
        setMaxAndMinValues(dim);
    }

    private void setMaxAndMinValues(int dim) {
        int rowsize;
        double value;
        rowsize = _flatTable.getRowSize();
        _realMax[dim] = Double.MIN_VALUE;
        _realMin[dim] = Double.MAX_VALUE;
        for (int j = 0; j < rowsize; j++) {
            value = _flatTable.getCell(j, _axisColIndex[dim]).doubleValue();
            if (_realMax[dim] < value) {
                _realMax[dim] = value;
            }
            if (_realMin[dim] > value) {
                _realMin[dim] = value;
            }
        }
        double diff = _realMax[dim] - _realMin[dim];
        _realMin[dim] = _realMin[dim] - diff * 0.05;
        _realMax[dim] = _realMax[dim] + diff * 0.05;
    }

    private ArrayList<Integer> selectRows() {
        if (_flatTable == null) {
            return new ArrayList<>(0);
        }
        int rowsize = _flatTable.getRowSize();
        double xvalue, yvalue;
        ArrayList<Integer> rows = new ArrayList<>();
        if (_tableViewer != null)
            _tableViewer.removeAll();

        for (int i = 0; i < rowsize; i++) {
            xvalue = _flatTable.getCell(i, _axisColIndex[0]).doubleValue();
            yvalue = _flatTable.getCell(i, _axisColIndex[1]).doubleValue();
            if (!Double.isNaN(xvalue) &&
                    !Double.isNaN(yvalue) &&
                    ((realValueToPos(0, xvalue) - (_selectedRegionStart[0] - LEFTMARGIN)) * (realValueToPos(0, xvalue) - (_selectedRegionEnd[0] - LEFTMARGIN)) < 0) &&
                    ((realValueToPos(1, yvalue) - (_selectedRegionStart[1] - TOPMARGIN)) * (realValueToPos(1, yvalue) - (_selectedRegionEnd[1] - TOPMARGIN)) < 0)) {
                if (_tableViewer != null) {
                    TableItem item = new TableItem(_tableViewer, SWT.NULL);
                    String[] data = {
                            _flatTable.getRowLabel(i),
                            xvalue + "",
                            yvalue + ""
                    };
                    item.setText(data);
                }
                //System.out.print(xvalue + "(" + realValueToPos(0, xvalue)+ ")"+ ":");
                //System.out.println(yvalue + "(" + realValueToPos(1, yvalue)+ ")");
                rows.add(i);
            }
        }
        return rows;
    }

    public void redraw() {
        int width = this.getClientArea().width;
        int height = this.getClientArea().height;
        Image bgImage = new Image(Display.getCurrent(), this.getClientArea());
        GC gcBg = new GC(bgImage);

        // Double Buffering
        _canvasSize[0] = width - LEFTMARGIN - RIGHTMARGIN;
        _canvasSize[1] = height - BOTTOMMARGIN - TOPMARGIN;

        Image plotAreaImage = new Image(Display.getCurrent(), _canvasSize[0], _canvasSize[1]);
        GC gcPlotArea = new GC(plotAreaImage);
        gcPlotArea.setForeground(_fgColor);
        gcPlotArea.setBackground(_bgColor);
        gcPlotArea.fillRectangle(0, 0, _canvasSize[0], _canvasSize[1]);

        gcPlotArea.setLineWidth(3);

        plotPoints(gcPlotArea);
        plotSelectedPoints(gcPlotArea);
        showSelectedRegion(gcPlotArea);

        gcPlotArea.dispose();

        gcBg.setBackground(_bgColor);
        gcBg.fillRectangle(0, 0, width, height);
        gcBg.drawImage(plotAreaImage, LEFTMARGIN, TOPMARGIN);

        // Draw axis
        gcBg.setForeground(_fgColor);
        gcBg.drawLine(LEFTMARGIN, height - BOTTOMMARGIN, width - RIGHTMARGIN, height - BOTTOMMARGIN);
        gcBg.drawLine(LEFTMARGIN, TOPMARGIN, LEFTMARGIN, height - BOTTOMMARGIN);
        if (_flatTable != null) {
            //Font font = new Font(bgImage,"Courier",18,SWT.BOLD);
            gcBg.drawText(_flatTable.getColLabel(_axisColIndex[0]), width - RIGHTMARGIN - _flatTable.getColLabel(_axisColIndex[0]).length() * 10, height - BOTTOMMARGIN + 1);
            gcBg.drawText(_flatTable.getColLabel(_axisColIndex[1]), LEFTMARGIN, TOPMARGIN);
        }


        gcBg.dispose();
        // 表画面へ
        GC gc = new GC(this);
        gc.drawImage(bgImage, 0, 0);
        plotAreaImage.dispose();
        bgImage.dispose();
        gc.dispose();
    }

    /**
     * @param gc
     */
    private void plotPoints(GC gc) {
        if (_flatTable == null)
            return;
        int rowsize = _flatTable.getRowSize();
        int[] rows = new int[rowsize];
        for (int i = 0; i < rowsize; i++) {
            rows[i] = i;
        }
        plotPoints(gc, rows, _pointColor, OVALSIZE);
    }

    private void plotPoints(GC gc, int[] rows, Color color, int size) {
        if (_flatTable == null)
            return;
        int rowsize = rows.length;
        double xd, yd;
        int x, y;
        for (int row : rows) {
            xd = _flatTable.getCell(row, _axisColIndex[0]).doubleValue();
            x = (int) (realValueToRatio(0, xd) * _canvasSize[0]);
            yd = _flatTable.getCell(row, _axisColIndex[1]).doubleValue();
            y = (int) ((1 - realValueToRatio(1, yd)) * _canvasSize[1]);
            gc.setBackground(color);
            gc.fillOval(x - size / 2, y - size / 2, size, size);
        }
    }

    private void plotSelectedPoints(GC gc) {
        if (_flatTable == null || _selectedPoints == null)
            return;
        plotPoints(gc, _selectedPoints, _focusedColor, (int) (OVALSIZE * 1.5));
    }

    private void showSelectedRegion(GC gc) {
        gc.setLineWidth(1);
        gc.setForeground(new Color(null, 128, 128, 128));
        gc.drawRectangle(_selectedRegionStart[0] - LEFTMARGIN, _selectedRegionStart[1] - TOPMARGIN,
                _selectedRegionEnd[0] - _selectedRegionStart[0], _selectedRegionEnd[1] - _selectedRegionStart[1]);
    }

    private double realValueToRatio(int dim, double v) {
        return (v - _realMin[dim]) / (_realMax[dim] - _realMin[dim]);
    }

    private int realValueToPos(int dim, double v) {
        if (dim == 1)
            return (int) ((1 - realValueToRatio(dim, v)) * _canvasSize[dim]);
        return (int) (realValueToRatio(dim, v) * _canvasSize[dim]);
    }

    private double posToRealValue(int dim, int v) {
        if (dim == 1)
            return (1 - (v - TOPMARGIN) / (double) _canvasSize[dim]) * (_realMax[dim] - _realMin[dim]) + _realMin[dim];
        return (v - LEFTMARGIN) / (double) _canvasSize[dim] * (_realMax[dim] - _realMin[dim]) + _realMin[dim];
    }

    /**
     * @param tableViewer
     */
    void setTableView(Table tableViewer) {
        _tableViewer = tableViewer;
    }

    /**
     * @param statusBar
     */
    void setStatusBar(Label statusBar) {
        _statusBar = statusBar;
    }

    /**
     * @param selectedRows
     */
    void setSelectedPoints(TableItem[] selectedRows) {
        String rowLabel;
        int count = 0;
        for (TableItem row : selectedRows) {
            rowLabel = row.getText();
            if (_flatTable.getRowIndex(rowLabel) >= 0) {
                count++;
            }
        }
        _selectedPoints = new int[count];
        int index;
        count = 0;
        for (TableItem selectedRow : selectedRows) {
            rowLabel = selectedRow.getText();
            index = _flatTable.getRowIndex(rowLabel);
            if (index >= 0) {
                _selectedPoints[count++] = index;
            }
        }
    }

    void setSelectedPoints(String[] selectedRowLabels) {
        int count = 0;
        for (String rowLabel : selectedRowLabels) {
            if (_flatTable.getRowIndex(rowLabel) >= 0) {
                count++;
            }
        }
        _selectedPoints = new int[count];
        int index;
        count = 0;
        for (String selectedRowLabel : selectedRowLabels) {
            index = _flatTable.getRowIndex(selectedRowLabel);
            if (index >= 0) {
                _selectedPoints[count++] = index;
            }
        }
    }

    /**
     *
     */
    void removeSelectedPoints() {
        _selectedPoints = new int[0];
    }


}
