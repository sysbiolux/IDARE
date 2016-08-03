package idare.metanode.Interfaces.DataSetReaders;

import idare.metanode.Interfaces.Plugin.IDAREService;
import idare.metanode.internal.exceptions.io.WrongFormat;

import java.io.File;
import java.io.IOException;
/**
 * A {@link IDAREDatasetReader} must be able to read a Datafile into a  
 * @author Thomas Pfau
 *
 */
public interface IDAREDatasetReader extends IDAREService{

	public IDAREWorkbook readData(File inputfile) throws WrongFormat,IOException;
	public boolean formatAccepted(File inputfile);
}
