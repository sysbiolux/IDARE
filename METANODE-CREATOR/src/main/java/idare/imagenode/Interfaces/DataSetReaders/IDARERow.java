package idare.imagenode.Interfaces.DataSetReaders;

import java.util.Iterator;

/**
 * An {@link IDARERow} is a row in a data sheet.
 * @author Thomas Pfau
 *
 */
public interface IDARERow extends Iterable<IDARECell> {
	
	/**
	 * A Few Values to see how to handle missing or empty cells.
	 */
	public static int CREATE_NULL_AS_BLANK = 0;
	public static int RETURN_BLANK_AS_NULL = 1;	
	public static int RETURN_NULL_AND_BLANK = 2;
	
	/**
	 * Obtain an Iterator over the {@link IDARECell}s of this row.
	 * @return the iterator starting with the first cell.
	 */
	public Iterator<IDARECell> cellIterator();
	/**
	 * Get the {@link IDARECell} in the specified column from this Row.
	 * @param arg0 - the column requested
	 * @return - the {@link IDARECell} at the specified position, or null, if it does not exist 
	 */
	public IDARECell getCell(int arg0);	
	/**
	 * Get the {@link IDARECell} in the specified column from this Row, using the specified policy to handle non existing or blank cells.
	 * @param arg0 - the column requested
	 * @return - the {@link IDARECell} at the specified position (blank or nulls will be handled according to the provided policy. 
	 */
	public IDARECell getCell(int arg0, int arg1);
	/**
	 * Get the index of the first non null cell in this row
	 * @return the index of the first non null column.
	 */
	public short getFirstCellNum();
	/**
	 * Gets the index of the last cell contained in this row PLUS ONE.!
	 * @return
	 */
	public short getLastCellNum();
	/**
	 * Get the number of non null Cells in this row.
	 * @return
	 */
	public int getPhysicalNumberOfCells();
	/**
	 * Get the position of this row in the enclosing {@link IDARESheet};
	 * @return
	 */
	public int getRowNum();
	
}
