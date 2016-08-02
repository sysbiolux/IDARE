package idare.metanode.internal.DataSetReaders.POIReader;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import idare.metanode.internal.DataSetReaders.IDAREWorkbook;
import idare.metanode.internal.Interfaces.DatasetReader;

public class POIReader implements DatasetReader{
	
	@Override
	public IDAREWorkbook readData(File inputfile) throws InvalidFormatException,IOException {
		return new POIWorkBook(WorkbookFactory.create(inputfile));
	}
	
	
}
