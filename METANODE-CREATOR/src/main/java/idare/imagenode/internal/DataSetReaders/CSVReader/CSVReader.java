package idare.imagenode.internal.DataSetReaders.CSVReader;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDARETask;
import idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook;
import idare.imagenode.internal.DataSetReaders.POIReader.POIWorkBook;
import idare.imagenode.internal.exceptions.io.WrongFormat;

import java.io.File;
import java.io.IOException;
/**
 * The CSV Reader assumes, that 
 * @author Thomas Pfau
 *
 */
public class CSVReader implements IDAREDatasetReader {

	private static String separatorString = ",";
	private String status = IDAREDatasetReader.IS_READY;
	private Boolean twoColumn;
	@Override
	public IDAREWorkbook readData(File inputfile) throws WrongFormat,
	IOException {
		int idcolumncount = twoColumn ? 2 : 1;
		return new POIWorkBook(new CSVWorkbook(inputfile, getSeparator(),idcolumncount));		
	}

	@Override
	public boolean fileTypeAccepted(File inputfile) {
		return inputfile.getName().endsWith(".csv"); 
	}
	/**
	 * Get the column separator used in this reader 
	 * @return - he string representing the column separator
	 */
	protected String getSeparator()
	{
		return separatorString;
	}

	@Override
	public IDARETask getSetupTask(File inputfile, boolean twocolumn) {
		twoColumn = twocolumn;
		status = IDAREDatasetReader.IS_SET_UP;
		return null;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		status = IDAREDatasetReader.IS_READY;
		twoColumn = null;
	}

}
