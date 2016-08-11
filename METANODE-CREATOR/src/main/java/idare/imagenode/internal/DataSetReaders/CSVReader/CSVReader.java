package idare.imagenode.internal.DataSetReaders.CSVReader;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDAREReaderSetupTask;
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
public class CSVReader extends IDAREDatasetReader {

	private static String separatorString = ",";
	private Boolean twoColumn;
	@Override
	public IDAREWorkbook readData(File inputfile) throws WrongFormat,
	IOException {
		int idcolumncount = twoColumn ? 2 : 1;
		return new POIWorkBook(new CSVWorkbook(inputfile, getSeparator(),idcolumncount));		
	}

	@Override
	public boolean fileTypeAccepted(File inputfile) {
		return inputfile.getName().toLowerCase().endsWith(".csv"); 
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
	public IDAREReaderSetupTask getSetupTask(File inputfile, boolean twocolumn) {
		twoColumn = twocolumn;
		return null;
	}


	@Override
	public void resetReader() {
		// TODO Auto-generated method stub
		twoColumn = null;
	}

}
