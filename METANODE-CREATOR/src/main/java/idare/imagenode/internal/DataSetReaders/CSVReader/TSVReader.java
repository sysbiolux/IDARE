package idare.imagenode.internal.DataSetReaders.CSVReader;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
import idare.imagenode.exceptions.io.WrongFormat;

import java.io.File;
import java.io.IOException;

/**
 * A Tab separated File reader is the same as a comma separated file reader, except that it also accepts tsv files and 
 * uses Tab instead of commas as separator
 * @author Thomas Pfau
 *
 */
public class TSVReader extends CSVReader {
	private static String separatorString = "\t";

	@Override
	public IDAREWorkbook readData(File inputfile) throws WrongFormat,
	IOException {
		return super.readData(inputfile);		
	}
	
	@Override
	public boolean fileTypeAccepted(File inputfile) {
		String CaseInsensitiveFileName = inputfile.getName().toLowerCase();
		return CaseInsensitiveFileName.endsWith(".csv") | CaseInsensitiveFileName.endsWith(".tsv") ; 
	}
	
	@Override
	protected String getSeparator()
	{
		return separatorString;
	}
}
