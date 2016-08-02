package idare.metanode.internal.Interfaces;

import idare.metanode.internal.DataSetReaders.IDAREWorkbook;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
/**
 * A {@link DatasetReader} must be able to read a Datafile into a  
 * @author Thomas Pfau
 *
 */
public interface DatasetReader {

	public IDAREWorkbook readData(File inputfile) throws InvalidFormatException,IOException;
	
}
