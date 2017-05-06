package lab.cb.scmd.util.table;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.AbstractCollection;
import java.util.LinkedList;
import java.util.Vector;

import lab.cb.scmd.exception.SCMDException;

public class AppendableTable extends BasicTable {

    public AppendableTable(String tableName, String[] columnNames ) {
        this(tableName, columnNames, false);
    }

    public AppendableTable(String tableName, String[] columnNames, boolean hasRowIndex) {
        _tableName = tableName;
        setHasRowLabel(hasRowIndex);
        setColLabel(columnNames);
    }

    /**
     * @param labelList
     *            StringのCollection (Vector, LinkedList, etc.)
     */
    public AppendableTable(String tableName, AbstractCollection columnNames) {
        this(tableName, columnNames, false);
    }

    public AppendableTable(String tableName, AbstractCollection labelList, boolean hasRowIndex) {
        _tableName = tableName;
        setColLabel(labelList);
        setHasRowLabel(hasRowIndex);
    }

    public void append(AbstractCollection dataList) {
        Object[] dataArray = dataList.toArray();
        append(dataArray);
    }

    public void append(Object[] dataList) {
        Vector row = new Vector(_colSize);
        int columnno = 0;
        if( hasRowLabel() == true ) {
            addRowLabel(dataList[columnno++].toString());
        }
        for( int i = columnno; i < dataList.length; i++ ) {
            if( dataList[i] != null)
                row.add(new Cell(dataList[i].toString()));
            else
                row.add(new Cell());
        }
        _rows.add(row);
    }

    public void appendFromFile(String fileName, boolean rowindex, boolean colindex) throws SCMDException {
        // load from tab-delimited file
        try
        {
            BufferedReader fileReader = new BufferedReader(new FileReader(fileName));

            String line = "";
            int curcolumn = 0;
            // read labels
            boolean columnSizeFlag = false;
            if( colindex == true ) {
                line = fileReader.readLine();
            } else {
            }

            // read data
            LinkedList rowLabelList = new LinkedList();
            while ((line = fileReader.readLine()) != null)
            {
                if( columnSizeFlag == false ) {
                    _colSize = line.split(DELIMITER).length;
                }
                readAndSetOneRow(rowindex, line, rowLabelList);
            }
            if( rowindex == true ) {
                setRowLabel(rowLabelList);
            }
            fileReader.close();
        }
        catch (IOException e)
        {
            throw new SCMDException("error occured while reading " + fileName
                    + "\n" + e.getMessage());
        }
    }

    /**
     * @param rowindex
     * @param line
     * @param rowLabelList
     * @throws SCMDException
     */
    private void readAndSetOneRow(boolean rowindex, String line, LinkedList rowLabelList) throws SCMDException {
        Vector row = new Vector(_colSize);

        String[] tokens = line.split(DELIMITER);

        int columnno = 0;
        if( rowindex == true ) {
            if( tokens.length > 0 ) {
                String token = tokens[columnno++];
                rowLabelList.add(trimDoubleQuotation(token));
            } else
                throw new SCMDException ("invalid row labels");
        }
        for( int i = columnno; i < tokens.length; i++ ) {
            String token = tokens[i];
            row.add(new Cell(token));
        }
        while (row.size() < _colSize)
        {
            row.add(new Cell());
        }
        _rows.add(row);
    }

    String trimDoubleQuotation(String str)
    {
        if(str.startsWith("\""))
        {
            if(str.endsWith("\""))
                return str.substring(1, str.length() - 1);
            else
                return str;
        }
        else
            return str;
    }
    public void addRowLabel(String label) {
        _rowLabelToRowIndexMap.put(label, new Integer(_rowLabel.size()));
        _rowLabel.add(label);
    }

    /**
     *
     */
    public void print(PrintStream out) {
        out.print(_tableName);
        for( int col = 0; col < getColSize(); col++ ) {
            out.print("\t" + this.getColLabel(col));
        }
        out.println();
        for( int row = 0; row < getRowSize(); row++ ) {
            if( this.hasRowLabel() )
                out.print(this.getRowLabel(row) + "\t");
            for( int col = 0; col < getColSize() - 1; col++) {
                out.print(this.getCell(row, col).toString() + "\t");
            }
            out.println(this.getCell(row, getColSize()-1));
        }
    }

}
