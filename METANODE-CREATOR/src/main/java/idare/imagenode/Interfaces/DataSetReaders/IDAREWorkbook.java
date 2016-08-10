package idare.imagenode.Interfaces.DataSetReaders;

/**
 * An {@link IDAREWorkbook} contains one or multiple sets of data in individual {@link IDARESheet}s
 * @author Thomas Pfau
 *
 */
public interface IDAREWorkbook{	


	/**
	 * Get the number of individual data sheets present in this workbook
	 * @return - the number of non null sheets in this Workbook
	 */
	public int getNumberOfSheets();

	/**
	 * Get the sheet with the specified name
	 * @param arg0 - the name of the requested sheet
	 * @return - the requested {@link IDARESheet}, or null if it does not exist.
	 */
	public IDARESheet getSheet(String arg0);

	/**
	 * Get the Sheet at the requested position, or null if it does not exist-
	 * @param arg0 - the 0-based position of the Sheet.
	 * @return the sheet at the requested position
	 */
	public IDARESheet getSheetAt(int arg0);

	/**
	 * Get the 0-based index of a sheet with the provided name
	 * @param arg0 - the name of the {@link IDARESheet}
	 * @return - the index of the Sheet with the requested name, no two sheets are allowed to have the same name.
	 */
	public int getSheetIndex(String arg0);

	/**
	 * Get the index of the provided {@link IDARESheet}.
	 * @param arg0 - the Sheet for which to obtain the index.
	 * @return - the index of the provided sheet.
	 */
	public int getSheetIndex(IDARESheet arg0);

	/**
	 * Get the name of the sheet at the provided position 
	 * @param arg0 - the position of the sheet for which to obtain the name
	 * @return - the name of the {@link IDARESheet}, or null if the position does not contain a sheet.
	 */
	public String getSheetName(int arg0);


}


