package idare.imagenode.Interfaces.DataSetReaders;

import java.util.Iterator;
/**
 * An {@link IDARESheet} is a sheet containing multiple rows (i.e. entries for individual items) present in a given workbook.
 * It commonly represents a unit such as all data from one experiment or one condition.
 * @author Thomas Pfau
 *
 */
public interface IDARESheet extends Iterable<IDARERow>{

	/**
	 * Get the index of the last Row
	 * @return
	 */
	public int getLastRowNum();
	/**
	 * Get the total number of non null rows in this Sheet.
	 * @return
	 */
	public int getPhysicalNumberOfRows();
	/**
	 * Get the Row with the provided row index 
	 * @param arg0 - the index for which a {@link IDARERow} is requested
	 * @return - the requested Row or null, if it does not exist.
	 */
	public IDARERow getRow(int arg0);
	/**
	 * Get the Name of this Sheet.
	 * @return - the name of this sheet.
	 */
	public String getSheetName();	
	/**
	 * Get an Iterator over all rows in this Sheet.
	 * @return - The iterator iterating over all Rows in this sheet. 
	 */
	public Iterator<IDARERow> rowIterator();	

}
