package idare.metanode.internal.DataSetReaders.CSVReader;

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
/**
 * A implementation of a {@link Cell} for use in CSV files.
 * A csv cell can only ever contain numeric, blank or string values. and will act as such. 
 * @author Thomas Pfau
 *
 */
public class CSVCell implements Cell {

	
	private int type; 
	private Double numericValue = 0.;
	private String StringValue = "";
	private CSVRow row; 
	//private int columnindex = 0;
	/**
	 * Create a new cell with of a specific type and in a specific row. 
	 * @param type
	 * @param row
	 */
	public CSVCell(int type, CSVRow row)
	{
		
		if(type == Cell.CELL_TYPE_BLANK || type == Cell.CELL_TYPE_NUMERIC || type == Cell.CELL_TYPE_STRING)
			this.type = type;
		else 
			this.type = CELL_TYPE_STRING;
		this.row = row;
	}
	
	@Override
	public CellRangeAddress getArrayFormulaRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getBooleanCellValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getCachedFormulaResultType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Comment getCellComment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCellFormula() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellStyle getCellStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCellType() {
		return type;		
	}

	@Override
	public int getColumnIndex() {
		return row.getColumnNumber(this);
	}

	@Override
	public Date getDateCellValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getErrorCellValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Hyperlink getHyperlink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getNumericCellValue() {
		if(type == Cell.CELL_TYPE_NUMERIC)
		{
			return numericValue;
		}
		else if(type == Cell.CELL_TYPE_BLANK)
			{
			return 0;
			}
		else
		{
			throw new IllegalStateException("Cell type is String but Numeric value requested");
		}
		
	}

	@Override
	public RichTextString getRichStringCellValue() {				
		return null;
	}

	@Override
	public Row getRow() {
		// TODO Auto-generated method stub
		return row;
	}

	@Override
	public int getRowIndex() {
		// TODO Auto-generated method stub
		return row.getRowNum();
	}

	@Override
	public Sheet getSheet() {
		// TODO Auto-generated method stub
		return row.getSheet();
	}

	@Override
	public String getStringCellValue() {
		if(type == Cell.CELL_TYPE_STRING)
		{
			return StringValue;
		}
		else if(type == Cell.CELL_TYPE_BLANK)
			{
			return "";
			}
		else
		{
			throw new IllegalStateException("Cell type is Numeric but String value requested");
		}
		
	}

	@Override
	public boolean isPartOfArrayFormulaGroup() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeCellComment() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAsActiveCell() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCellComment(Comment arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCellErrorValue(byte arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCellFormula(String arg0) throws FormulaParseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCellStyle(CellStyle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCellType(int arg0) {
		this.type = arg0;
	}

	@Override
	public void setCellValue(double arg0) {
		// TODO Auto-generated method stub
		type = CELL_TYPE_NUMERIC;
		numericValue = arg0;
		StringValue = "";
	}

	@Override
	public void setCellValue(Date arg0) {
		type = CELL_TYPE_STRING;
		StringValue = arg0.toString();				
	}

	@Override
	public void setCellValue(Calendar arg0) {
		type = CELL_TYPE_STRING;
		StringValue = arg0.toString();		
	}

	@Override
	public void setCellValue(RichTextString arg0) {
		type = CELL_TYPE_STRING;
		StringValue = arg0.toString();	

	}

	@Override
	public void setCellValue(String arg0) {
		type = CELL_TYPE_STRING;
		StringValue = arg0;	
	}

	@Override
	public void setCellValue(boolean arg0) {
		type = CELL_TYPE_NUMERIC;
		StringValue = "";
		numericValue = arg0 ?  1. : 0.; 
	}

	@Override
	public void setHyperlink(Hyperlink arg0) {
		// TODO Auto-generated method stub

	}
	
	public String toString()
	{
		String res = StringValue;
		if(type == Cell.CELL_TYPE_NUMERIC)
		{
			res = Double.toString(numericValue);
		}
		return res;
	}






}
