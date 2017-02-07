package idare.imagenode.Utilities;

import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
/**
 * A few useful IO Functions 
 * @author Thomas Pfau
 *
 */
public class IOUtils {
	/**
	 * Get a Temporary file with a given Filename and a given Extension.
	 * IF the file already exists, suffixes will be added.
	 * @param FileName - Filename of the Temporary file
	 * @param Extension - File extension of the temporary file.
	 * @return The Temporary File Object.
	 */
	public static File getTemporaryFile(String FileName, String Extension)
	{
		
		File TempFolder = new File(System.getProperty("java.io.tmpdir") + File.separator + IMAGENODEPROPERTIES.IMAGENODE_TEMP_FOLDER);
		try{
			if(!TempFolder.exists() )

			{
				FileUtils.forceMkdir(TempFolder);			
			}
		}
		catch(IOException e)
		{			
			throw new RuntimeException(e);
		}
		
		File target = new File(System.getProperty("java.io.tmpdir") + File.separator + IMAGENODEPROPERTIES.IMAGENODE_TEMP_FOLDER + File.separator + FileName + Extension);
		int suffix = 0;
		while(target.exists())
		{
			target = new File(System.getProperty("java.io.tmpdir") + File.separator + IMAGENODEPROPERTIES.IMAGENODE_TEMP_FOLDER + File.separator + FileName + "_" + suffix + Extension);
			suffix++;
		}
//		PrintFDebugger.Debugging(IOUtils.class, "New temporary File created at:" + target.toPath().toString());
//		PrintFDebugger.Debugging(IOUtils.class, "File exists?:" + target.exists());
		return target;
	}
	
	/**
	 * Create the Temporary folder (if it does not exist).
	 * @throws IOException
	 */
	public static void createTemporaryFolder() throws IOException
	{
		// Clear this Apps Temporary folder
		File TempFolder = new File(System.getProperty("java.io.tmpdir") + File.separator + IMAGENODEPROPERTIES.IMAGENODE_TEMP_FOLDER);
		if(!TempFolder.exists() )
		{
			FileUtils.forceMkdir(TempFolder);			
		}		
	}
	
	
	/**
	 * Clear the Temporary folder.
	 * @throws IOException
	 */
	public static void clearTemporaryFolder() throws IOException
	{
		// Clear this Apps Temporary folder
		File TempFolder = new File(System.getProperty("java.io.tmpdir") + File.separator + IMAGENODEPROPERTIES.IMAGENODE_TEMP_FOLDER);
		if(TempFolder.exists() )
		{
			if(TempFolder.isDirectory())
			{	
				FileUtils.cleanDirectory(TempFolder);			
			}
			FileUtils.forceDelete(TempFolder);
		}
		FileUtils.forceMkdir(TempFolder);
	}

}
