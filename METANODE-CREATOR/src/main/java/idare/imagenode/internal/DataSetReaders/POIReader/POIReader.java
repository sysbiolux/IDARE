package idare.imagenode.internal.DataSetReaders.POIReader;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDAREReaderSetupTask;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
import idare.imagenode.exceptions.io.WrongFormat;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class POIReader extends IDAREDatasetReader{
	
	
	@Override
	public IDAREWorkbook readData(File inputfile) throws WrongFormat,IOException {
		return new POIWorkBook(WorkbookFactory.create(inputfile));
	}

	@Override
	public boolean fileTypeAccepted(File inputfile) {
		String CaseInsensitiveFileName = inputfile.getName().toLowerCase();
		return CaseInsensitiveFileName.endsWith(".xls") | CaseInsensitiveFileName.endsWith(".xlsx"); 
	}
	

	@Override
	public IDAREReaderSetupTask getSetupTask(File inputfile, boolean twocolumn) {
		return null;
	}


	@Override
	public void resetReader() {
	}
	
}
