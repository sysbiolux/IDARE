package idare.imagenode.Interfaces.DataSetReaders;

import idare.imagenode.Interfaces.Plugin.IDAREService;
import idare.imagenode.internal.exceptions.io.WrongFormat;

import java.io.File;
import java.io.IOException;
/**
 * A {@link IDAREDatasetReader} must be able to read a Datafile into a  
 * @author Thomas Pfau
 *
 */
public interface IDAREDatasetReader extends IDAREService{

	/**
	 * Read an input File and create an IDARE compatible Workbook structure.
	 * @param inputfile - the file to read.
	 * @return A workbook containing the data from the file
	 * @throws WrongFormat - IF the format cannot be read by this Reader
	 * @throws IOException - If there are problems with the IO of the file
	 */
	public IDAREWorkbook readData(File inputfile) throws WrongFormat,IOException;
	/**
	 * Test, whether the file can potentially be read by this reader.
	 * @param inputfile - The file to check
	 * @return - whether this parser can be used to read the file.
	 */
	public boolean formatAccepted(File inputfile);
}
