package idare.imagenode.Interfaces.DataSetReaders;

import idare.imagenode.Interfaces.Plugin.IDAREService;
import idare.imagenode.internal.exceptions.io.WrongFormat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.FutureTask;

import javax.swing.JFrame;

import org.cytoscape.work.Task;
import org.cytoscape.work.swing.TunableUIHelper;
/**
 * A {@link IDAREDatasetReader} must be able to read a Datafile into a  
 * @author Thomas Pfau
 *
 */
public interface IDAREDatasetReader extends IDAREService{
	
	public static String IS_READY = "ready";
	public static String IS_SET_UP = "setupDone";
	
	/**
	 * Read an input File and create an IDARE compatible Workbook structure.
	 * @param inputfile - the file to read.
	 * @return A workbook containing the data from the file
	 * @throws WrongFormat - If the format cannot be read by this Reader, or if the file provided is not the file the reader was set up for. 
	 * @throws IOException - If there are problems with the IO of the file
	 */
	public IDAREWorkbook readData(File inputfile) throws WrongFormat,IOException;
	/**
	 * Test, whether the file can potentially be read by this reader.
	 * THis only checks the File extension, and not the content.
	 * @param inputfile - The file to check
	 * @return - whether this parser can be used to read the file.
	 */
	public boolean fileTypeAccepted(File inputfile);
	
	/**
	 * The Reader should provide a Task that can be used to set up the reader for 
	 * future use on the given inputFile.
	 * if null is returned, it is assumed, that no Task 
	 * @param inputfile
	 * @param twocolumn
	 * @return
	 */
	public IDARETask getSetupTask(File inputfile, boolean twocolumn);
	
	/**
	 * Informs about the status of the reader.
	 * If the reader is ready to provide a SetupTask, it should return <code>IDAREDatasetReader.IS_READY</code>.
	 * If the reader is set up it should return <code>IDAREDatasetReader.IS_SET_UP</code>
	 * @return - information, where the process of reading failed, or <code>IS_READY</code>
	 */
	public String getStatus();
	
	/**
	 * Reset the reader, clearing any temporary fields. after this call the reader status should be "IS_READY"
	 */
	public void reset(); 
}
