package idare.imagenode.internal.DataSetReaders.CSVReader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.usermodel.AutoFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
/**
 * A Sheet implementation to be used for CSV files.
 * @author Thomas Pfau
 *
 */
public class CSVSheet implements Sheet {
	HashMap<Integer,Row> Data;
	HashMap<Row,Integer> Datapos;
	Vector<Integer> rowpositions;
	CSVWorkbook wb;
	String SheetName;
	/**
	 * Simple constructor. The data is simply stored in a vector.
	 * @param workbook - the enclosing workbook.
	 * @param SheetName The name of the Sheet
	 */
	public CSVSheet(CSVWorkbook workbook, String SheetName)
	{
		wb = workbook;
		Data = new HashMap<Integer, Row>();
		Datapos = new HashMap<Row, Integer>();
		rowpositions = new Vector<Integer>();
		this.SheetName = SheetName;
	}
	@Override
	public Iterator<Row> iterator() {
		// TODO Auto-generated method stub		
		return new RowIter(Data);
	}

	/**
	 * get the row position of a row.
	 * @param row The row to get the row number for
	 * @return the position of the provided row
	 */
	public int getRowNumber(Row row)
	{
		return Datapos.get(row);
	}
	
	
	@Override
	public int addMergedRegion(CellRangeAddress arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addValidationData(DataValidation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void autoSizeColumn(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void autoSizeColumn(int arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Drawing createDrawingPatriarch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createFreezePane(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createFreezePane(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}


	
	@Override
	public Row createRow(int arg0) {
		// TODO Auto-generated method stub

		Row newRow = new CSVRow(this);
		if(!Data.containsKey(arg0))
		{
			rowpositions.add(arg0);
		}
		else
		{
			//remove an old cell.
			Datapos.remove(Data.get(arg0));
		}
		Datapos.put(newRow, arg0);
		Data.put(arg0,newRow);
		return newRow;			
	}

	@Override
	public void createSplitPane(int arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getAutobreaks() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getColumnBreaks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellStyle getColumnStyle(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getColumnWidth(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DataValidationHelper getDataValidationHelper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDefaultColumnWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getDefaultRowHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getDefaultRowHeightInPoints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getDisplayGuts() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getFirstRowNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getFitToPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Footer getFooter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getForceFormulaRecalculation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Header getHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getHorizontallyCenter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getLastRowNum() {
		// TODO Auto-generated method stub
		return Data.size();
	}

	@Override
	public short getLeftCol() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMargin(short arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CellRangeAddress getMergedRegion(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumMergedRegions() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PaneInformation getPaneInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPhysicalNumberOfRows() {
		// TODO Auto-generated method stub
		return Data.size();
	}

	@Override
	public PrintSetup getPrintSetup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getProtect() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Row getRow(int arg0) {
		// TODO Auto-generated method stub
		return Data.get(arg0);
	}

	@Override
	public int[] getRowBreaks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getRowSumsBelow() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getRowSumsRight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getScenarioProtect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SheetConditionalFormatting getSheetConditionalFormatting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSheetName() {
		return SheetName;
	}

	@Override
	public short getTopRow() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getVerticallyCenter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Workbook getWorkbook() {
		// TODO Auto-generated method stub
		return wb;
	}

	@Override
	public void groupColumn(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void groupRow(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isColumnBroken(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isColumnHidden(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisplayFormulas() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisplayGridlines() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisplayRowColHeadings() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisplayZeros() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrintGridlines() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRightToLeft() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRowBroken(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void protectSheet(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public CellRange<? extends Cell> removeArrayFormula(Cell arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeColumnBreak(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeMergedRegion(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRow(Row arg0) {
		// TODO Auto-generated method stub
		Data.remove(arg0);
	}

	@Override
	public void removeRowBreak(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<Row> rowIterator() {
		// TODO Auto-generated method stub
		return new RowIter(Data);
	}

	@Override
	public CellRange<? extends Cell> setArrayFormula(String arg0,
			CellRangeAddress arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutoFilter setAutoFilter(CellRangeAddress arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAutobreaks(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColumnBreak(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColumnGroupCollapsed(int arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColumnHidden(int arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColumnWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDefaultColumnStyle(int arg0, CellStyle arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDefaultColumnWidth(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDefaultRowHeight(short arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDefaultRowHeightInPoints(float arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisplayFormulas(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisplayGridlines(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisplayGuts(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisplayRowColHeadings(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisplayZeros(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFitToPage(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setForceFormulaRecalculation(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHorizontallyCenter(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMargin(short arg0, double arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPrintGridlines(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRightToLeft(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRowBreak(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRowGroupCollapsed(int arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRowSumsBelow(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRowSumsRight(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelected(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVerticallyCenter(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shiftRows(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		Collections.sort(rowpositions);
		int othershift = arg1-arg0;
		HashMap<Integer, Row> newdata = new HashMap<Integer, Row>();
		HashMap<Row,Integer> newpos = new HashMap<Row, Integer>();
		Vector<Integer> newrowpos = new Vector<Integer>();
		
		for(Integer i = arg0; i <= arg1; i++)
		{
			if(Data.containsKey(i))
			{
				newdata.put(i+arg2, Data.get(i));
				newpos.put(Data.get(i), i+arg2);
				newrowpos.add(i+arg2);
				Data.remove(i);
				Datapos.remove(Data.get(i));
				rowpositions.remove(i);
			}			
		}
		for(Integer i = arg1+1; i <= arg1 + arg2; i++)
		{
			if(Data.containsKey(i))
			{
				newdata.put(i-othershift, Data.get(i));
				newpos.put(Data.get(i), i-othershift);
				newrowpos.add(i-othershift);
				Data.remove(i);
				Datapos.remove(Data.get(i));
				rowpositions.remove(i);
			}
		}
		Data.putAll(newdata);
		Datapos.putAll(newpos);
		rowpositions.addAll(newrowpos);
		
	}

	@Override
	public void shiftRows(int arg0, int arg1, int arg2, boolean arg3,
			boolean arg4) {
		// TODO Auto-generated method stub
		shiftRows(arg0, arg1, arg2);
	}

	@Override
	public void ungroupColumn(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ungroupRow(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}	
	/**
	 * Row iteratro implementation to return the rows in the order they are in the sheet.
	 * @author Thomas Pfau
	 *
	 */
	private class RowIter implements Iterator<Row>
	{

			private HashMap<Integer, Row> map;
			private Vector<Integer> cellpositions;
			Iterator<Integer> iter;
			public RowIter(HashMap<Integer, Row> entry)
			{
				this.cellpositions = new Vector<Integer>();
				cellpositions.addAll(entry.keySet());
				Collections.sort(cellpositions);
				iter = cellpositions.iterator();
				map = entry;
			}
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Row next() {
				return map.get(iter.next());
			}
			
		
	}

	@Override
	public float getColumnWidthInPixels(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int addMergedRegionUnsafe(CellRangeAddress region) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void validateMergedRegions() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeMergedRegions(Collection<Integer> indices) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<CellRangeAddress> getMergedRegions() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isPrintRowAndColumnHeadings() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setPrintRowAndColumnHeadings(boolean show) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setZoom(int scale) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void showInPane(int toprow, int leftcol) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Comment getCellComment(CellAddress ref) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<CellAddress, ? extends Comment> getCellComments() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Drawing<?> getDrawingPatriarch() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<? extends DataValidation> getDataValidations() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CellRangeAddress getRepeatingRows() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CellRangeAddress getRepeatingColumns() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setRepeatingRows(CellRangeAddress rowRangeRef) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setRepeatingColumns(CellRangeAddress columnRangeRef) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getColumnOutlineLevel(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public Hyperlink getHyperlink(int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Hyperlink getHyperlink(CellAddress addr) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<? extends Hyperlink> getHyperlinkList() {
		// TODO Auto-generated method stub
		return new LinkedList<Hyperlink>();
	}
	@Override
	public CellAddress getActiveCell() {
		// TODO Auto-generated method stub
		return new CellAddress(0,0);
	}
	@Override
	public void setActiveCell(CellAddress address) {
		// TODO Auto-generated method stub
		
	}
}
