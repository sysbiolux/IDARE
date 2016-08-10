package idare.imagenode.internal.DataSetReaders.POIReader;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDARETask;
import idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook;
import idare.imagenode.internal.exceptions.io.WrongFormat;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class POIReader implements IDAREDatasetReader{
	
	private String status = IDAREDatasetReader.IS_READY;
	
	@Override
	public IDAREWorkbook readData(File inputfile) throws WrongFormat,IOException {
		try{
			return new POIWorkBook(WorkbookFactory.create(inputfile));
		}
		catch(InvalidFormatException e)
		{		
			throw new WrongFormat(e.getMessage());
		}		
	}

	@Override
	public boolean fileTypeAccepted(File inputfile) {
		return inputfile.getName().endsWith(".xls") | inputfile.getName().endsWith(".xlsx"); 
	}
	

	@Override
	public IDARETask getSetupTask(File inputfile, boolean twocolumn) {
		status = IDAREDatasetReader.IS_SET_UP;
		return null;
	}

	@Override
	public String getStatus() {		
		return status;
	}

	@Override
	public void reset() {
		status = IDAREDatasetReader.IS_READY;
	}
	
}
