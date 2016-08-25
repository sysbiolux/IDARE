package idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell;
/**
 * Basic implementation of a Simple Cell for a {@link BasicIDAREWorkbook}.
 * @author Thomas Pfau
 *
 */
public class BasicIDARECell implements IDARECell {

	private CellType type;
	private String value;
	private BasicIDARERow containingRow;
	
	/**
	 * Basic Constructor indicating the containing row and the type of the cell
	 * @param type the {@link CellType} of the cell
	 * @param containingRow The Row containing this cell
	 */
	public BasicIDARECell(CellType type, BasicIDARERow containingRow)
	{		
		this.type = type;
		this.containingRow = containingRow;
		if(type == CellType.STRING)
		{
			value = "";
		}
		if(type == CellType.NUMERIC)
		{
			value = "0";
		}
	}
	/**
	 * Basic Constructor indicating the containing row and the type of the cell, along with the value to be stored.
	 * @param type the {@link CellType} of this cell
	 * @param value the value of this cell
	 * @param containingRow the row containing this cell
	 */
	public BasicIDARECell(CellType type, String value, BasicIDARERow containingRow)
	{
		this.type = type;
		this.containingRow = containingRow;
		if(this.type != CellType.BLANK)
		{
			this.value = value;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDARECell#getCellType()
	 */
	@Override
	public CellType getCellType() {
		return type;
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDARECell#getColumnIndex()
	 */
	@Override
	public int getColumnIndex() {		 
		return containingRow.getCellIndex(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDARECell#getNumericCellValue()
	 */
	@Override
	public double getNumericCellValue() {
		if(type == CellType.STRING)
		{
			throw new IllegalStateException("Cannot request a numeric value from a string cell");
		}
		return Double.parseDouble(value);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDARECell#getStringCellValue()
	 */
	@Override
	public String getStringCellValue() {
		if(type == CellType.NUMERIC)
		{
			throw new IllegalStateException("Cannot request a string value from a numeric cell");
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.IDARECell#getFormattedCellValue()
	 */
	@Override
	public String getFormattedCellValue() {
		if(type == CellType.BLANK)
		{
			return "";
		}
		return value;
	}
	
	/**
	 * Set the value (this is unchecked wrt the type defined).
	 * @param value the value this Cell should have
	 */
	public void setValue(String value)
	{
		this.value = value; 
	}
	
}
