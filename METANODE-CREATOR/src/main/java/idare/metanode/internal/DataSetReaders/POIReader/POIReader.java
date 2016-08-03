package idare.metanode.internal.DataSetReaders.POIReader;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import idare.metanode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.metanode.Interfaces.DataSetReaders.IDAREWorkbook;
import idare.metanode.internal.exceptions.io.WrongFormat;

public class POIReader implements IDAREDatasetReader{
	
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
	public boolean formatAccepted(File inputfile) {
		return inputfile.getName().endsWith(".xls") | inputfile.getName().endsWith(".xlsx"); 
	}
	
}
