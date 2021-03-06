package idare.imagenode.Interfaces.DataSetReaders;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
import idare.imagenode.exceptions.io.WrongFormat;
import idare.imagenode.internal.IDAREService;

import java.io.File;
import java.io.IOException;

import org.cytoscape.work.Tunable;
/**
 * A {@link IDAREDatasetReader} must be able to read a Datafile into a  
 * @author Thomas Pfau
 *
 */
public abstract class IDAREDatasetReader implements IDAREService{
	
	public static String IS_READY = "ready";
	public static String IS_SET_UP = "setupDone";
	
	private String statusMessage = IS_READY;
	
	/**
	 * Read an input File and create an IDARE compatible Workbook structure.
	 * @param inputfile  the file to read.
	 * @return A workbook containing the data from the file
	 * @throws WrongFormat  If the format cannot be read by this Reader, or if the file provided is not the file the reader was set up for. 
	 * @throws IOException  If there are problems with the IO of the file
	 */
	public abstract IDAREWorkbook readData(File inputfile) throws WrongFormat,IOException;
	/**
	 * Test, whether the file can potentially be read by this reader.
	 * THis only checks the File extension, and not the content.
	 * @param inputfile  The file to check
	 * @return  whether this parser can be used to read the file.
	 */
	public abstract boolean fileTypeAccepted(File inputfile);
	
	/**
	 * The Reader should provide a Task that can be used to set up the reader for 
	 * future use on the given inputFile.
	 * if null is returned, it is assumed, that no Task is needed 
	 * @param inputfile the input file to generate the setup task. This file can be read to extract information for options.
	 * @param twocolumn whether a {@link IDAREWorkbook} with label and id or a set with only an id is requested.
	 * @return a Task that can obtain input from the user via {@link Tunable}s, or null if no setup information is necessary
	 * @throws Exception Multiple Exceptions from IO to other issues. 
	 */
	public abstract IDAREReaderSetupTask getSetupTask(File inputfile, boolean twocolumn) throws Exception;
	
	/**
	 * Informs about the status of the reader.
	 * If the reader is ready to provide a SetupTask, it should return <code>IDAREDatasetReader.IS_READY</code>.
	 * If the reader is set up (i.e. getSetupTask completed successfully) it should return <code>IDAREDatasetReader.IS_SET_UP</code>
	 * @return  information, where the process of reading failed, or <code>IS_READY</code>
	 */
	public final String getStatusMessage()
	{
		return statusMessage;
	}
	/**
	 * This function allows setting the Status Message if an exception is thrown during reading. 
	 * @param Message the Message that will be set (comonly the errormessage received during one of the calls.
	 */
	public final void setStatusMessage(String Message)
	{
		statusMessage = Message;
	}
		
	/**
	 * Resets the reader. This sets the statusMessage and calls the resetReader() method implemented by the extending class.   
	 */
	public final void reset()
	{
		statusMessage = IDAREDatasetReader.IS_READY;
		resetReader();
	}
	/**
	 * Reset the reader, clearing any temporary fields. after this call the reader status should be "IS_READY"
	 * When extending the class, this Method should be overwritten but the reset method from the abstract class should also be called,
	 */
	public abstract void resetReader();
}
