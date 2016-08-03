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
	public static int CELL_TYPE_BLANK = 0;
	public static int CELL_TYPE_NUMERIC = 1;
	public static int CELL_TYPE_STRING = 2;
	public static int CELL_TYPE_FORMULA = 3;
	public static int CELL_TYPE_UNKNOWN = 4;
	
	/**
	 * Get the Cell Type of 
	 * @return
	 */
	public int getCellType();	
	/**
	 * Get the index of the column of this cell.
	 * @return the column index
	 */
	public int getColumnIndex();
	/**
	 * Get the Numeric Value of this cell, if it can be converted to numeric. 
	 * @return the numeric value of this cell
	 */
	public double getNumericCellValue();
	/**
	 * get the index of the {@link IDARERow} this Cell is in.
	 * @return the row index
	 */
	public int getRowIndex();
	/**
	 * Get the string cell value, if the cell is a string cell.
	 * @return the string cell value.
	 */
	public String getStringCellValue();
	/**
	 * Get a string representation of the value of this cell (regardless what this cell actually represents).
	 * @return
	 */
	public String getFormattedCellValue();
}
