package idare.imagenode.Interfaces.DataSetReaders;


/**
 * A general class of cells in a Workbook used for IDARE.
 * @author Thomas Pfau
 *
 */
public interface IDARECell {
	
	/**
	 * Different Types of Cells that can be available.
	 */
	public static enum CellType{
	BLANK,
	NUMERIC,
	STRING, 
	FORMULA,
	UNKNOWN
	}
	
	/**
	 * Get the Cell Type of 
	 * @return the Type of the Cell (defined by the static definitions in {@link IDARECell}
	 */
	public CellType getCellType();	
	/**
	 * Get the index of the column of this cell.
	 * @return the column index
	 */
	public int getColumnIndex();
	/**
	 * Get the Numeric Value of this cell, if it can be converted to numeric. 
	 * @return the numeric value of this cell
	 * @throws Exception - Can throw exceptions if the contained value is either not a parsable double or the Cell Type is String.
	 */
	public double getNumericCellValue();
	/**
	 * Get the string cell value, if the cell is a string cell.
	 * @return the string cell value.
	 * @throws Exception - Can throw exceptions if the cell type is numeric.
	 */
	public String getStringCellValue();
	/**
	 * Get a string representation of the value of this cell (regardless what this cell actually represents).
	 * @return a String representing the value of this cell.
	 */
	public String getFormattedCellValue();
}
