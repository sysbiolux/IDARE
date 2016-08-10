package idare.imagenode.internal.DataManagement;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.DataSetProperties;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.DataManagement.Events.DataSetAboutToBeChangedListener;
import idare.imagenode.internal.DataManagement.Events.DataSetChangeListener;
import idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.GUI.DataSetAddition.Tasks.DataSetAdderTaskFactory;
import idare.imagenode.internal.Utilities.EOOMarker;
import idare.imagenode.internal.Utilities.IOUtils;
import idare.imagenode.internal.exceptions.io.DuplicateIDException;
import idare.imagenode.internal.exceptions.io.WrongFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.work.swing.TunableUIHelper;
/**
 * A class to manage Datasets
 * @author Thomas Pfau
 *
 */
public class DataSetManager{

	private Map<Integer, DataSet> DataSets;
	private DataSetIDProvider idprovider;
	private Vector<DataSetAboutToBeChangedListener> toChangeListener = new Vector<DataSetAboutToBeChangedListener>();
	private Vector<DataSetChangeListener> changedListener = new Vector<DataSetChangeListener>();
	private HashMap<String,Class<? extends DataSet>> AvailableDataSetTypes = new HashMap<String, Class<? extends DataSet>>();
	private HashMap<Class<? extends DataSet>,Collection<DataSetProperties>> DataSetPropertyOptions = new HashMap<Class<? extends DataSet>,Collection<DataSetProperties>>();
	private LinkedList<IDAREDatasetReader> dataSetReaders = new LinkedList<IDAREDatasetReader>();
	/**
	 * Default constructor initializing required fields.
	 */
	public DataSetManager()//NodeManager manager )
	{
		DataSets = new HashMap<Integer, DataSet>();
		idprovider = new DataSetIDProvider();		
	}
	
	/**
	 * Clear all Datasets, and inform any listeners that the sets are being removed. 
	 */
	public void clearDataSets()
	{
		fireDataSetsRemoved(DataSets.values());
		DataSets.clear();
		AvailableDataSetTypes.clear();
		DataSetPropertyOptions.clear();
	}
	/**
	 * Get the Types of Datasets available to this Manager.
	 * @return a {@link Collection} of {@link String}s of available {@link DataSet} types
	 */
	public Collection<String> getAvailableDataSetTypes()
	{
		return AvailableDataSetTypes.keySet();
	}
	
	/**
	 * Register a DataSetType that can be accessed by the TypeName.
	 * @param TypeName
	 * @param The Class of the DataSet
	 */
	public void registerDataSetType(String TypeName, Class<? extends DataSet> dataSetClass) throws DuplicateIDException
	{
		if(!AvailableDataSetTypes.containsKey(TypeName))
		{	
			AvailableDataSetTypes.put(TypeName,dataSetClass);		
		}
		else
		{
			throw new DuplicateIDException(TypeName, "Duplicate Type Name for DataSet encountered. Not adding the new dataset type.");
		}
	}

	/**
	 * Remove a class from the available Datasetclasses.
	 * This is skipped, if the class matching to the typename does not match the class provided. 
	 * @param TypeName
	 * @param The Class of the DataSet
	 */
	public void deRegisterDataSetType(String TypeName, Class<? extends DataSet> dataSetClass)
	{
		if(AvailableDataSetTypes.get(TypeName) != null && AvailableDataSetTypes.get(TypeName).equals(dataSetClass))
		{
			AvailableDataSetTypes.remove(TypeName);		
		}
	}
	
	/**
	 * Add DataSetProperties for a specific dataset
	 */
	public boolean registerPropertiesForDataSet(Class<? extends DataSet> datasetclass, DataSetProperties properties )
	{

		String classname = datasetclass.getCanonicalName();
		if(AvailableDataSetTypes.values().contains(datasetclass))
		{
			if(!DataSetPropertyOptions.containsKey(datasetclass)){
				DataSetPropertyOptions.put(datasetclass,new Vector<DataSetProperties>());
			}
			if(DataSetPropertyOptions.get(datasetclass).contains(properties))
			{
				return false;
			}
			DataSetPropertyOptions.get(datasetclass).add(properties);
			Vector<DataSet> changedSets = new Vector<DataSet>();
			for(DataSet ds : DataSets.values())
			{
				if(datasetclass.isInstance(ds))
				{
					Collection<DataSetProperties> validOptions = new HashSet<DataSetProperties>();
					for(DataSetProperties props : DataSetPropertyOptions.get(datasetclass))
					{
						try{
							props.testValidity(ds);
							validOptions.add(props);
						}
						catch(WrongFormat e)
						{
							//cannot use these properties.
							continue;
						}
					}
					ds.setPropertyOptions(validOptions);
					changedSets.add(ds);
				}
			}
			fireDataSetsChanged(changedSets);
			
			return true;
		}
		return false;
	}
	
	/**
	 * Register a set of properties for a datasetclass.
	 * @param datasetclass - the class of the dataset which to register the properties for.
	 * @param properties - the property options for the dataset that should eb registered.
	 * @return - The properties that were not added because they are already present.
	 */
	public Collection<DataSetProperties> registerPropertiesForDataSet(Class datasetclass, Collection<DataSetProperties> properties )
	{
		Collection<DataSetProperties> presentprops = new Vector<DataSetProperties>();
		String classname = datasetclass.getCanonicalName();
		if(AvailableDataSetTypes.values().contains(datasetclass))
		{
			if(!DataSetPropertyOptions.containsKey(datasetclass)){
				DataSetPropertyOptions.put(datasetclass,new Vector<DataSetProperties>());
			}
			for(DataSetProperties props : properties)
			{

				if(DataSetPropertyOptions.get(datasetclass).contains(properties))
				{
					presentprops.add(props);
				}
				else
				{
					DataSetPropertyOptions.get(datasetclass).add(props);
				}
			}
			
		}
		Vector<DataSet> changedSets = new Vector<DataSet>();
		for(DataSet ds : DataSets.values())
		{
			if(datasetclass.isInstance(ds))
			{
				Collection<DataSetProperties> validOptions = new HashSet<DataSetProperties>();
				for(DataSetProperties props : DataSetPropertyOptions.get(datasetclass))
				{
					try{
						props.testValidity(ds);
						validOptions.add(props);
					}
					catch(WrongFormat e)
					{
						//cannot use these properties.
						continue;
					}
				}
				ds.setPropertyOptions(validOptions);
				changedSets.add(ds);
			}
		}
		fireDataSetsChanged(changedSets);
		return presentprops;
	}
	
	/**
	 * Deregister Properties for a DataSet
	 * @param datasetclass - the class of the dataset to deregister items for.
	 * @param properties - the properties to deregister.
	 */
	public void deregisterPropertiesForDataSet(Class<? extends DataSet> datasetclass, DataSetProperties properties )
	{

		String classname = datasetclass.getCanonicalName();
		if(AvailableDataSetTypes.values().contains(datasetclass))
		{
			if(!DataSetPropertyOptions.containsKey(datasetclass)){
				return;
			}
			else
			{
				DataSetPropertyOptions.get(datasetclass).remove(properties);
			}
		}
		Vector<DataSet> changedSets = new Vector<DataSet>();
		for(DataSet ds : DataSets.values())
		{
			if(datasetclass.isInstance(ds))
			{
				ds.setPropertyOptions(DataSetPropertyOptions.get(datasetclass));
				changedSets.add(ds);				
			}
		}
		fireDataSetsChanged(changedSets);
		
	}
	
	/**
	 * Add a Listener that needs to be informed, if a dataset state is about to be changed, before the change actually happened 
	 * @param listener - the {@link DataSetAboutToBeChangedListener} that listens
	 */
	public void addDataSetAboutToBeChangedListener(DataSetAboutToBeChangedListener listener)
	{
		toChangeListener.add(listener);
	}
	/**
	 * Remove a {@link DataSetAboutToBeChangedListener} listening to changes in {@link DataSet}s 
	 * @param listener - the {@link DataSetAboutToBeChangedListener} that listens
	 */	
	public void removeDataSetAboutToBeChangedListener(DataSetAboutToBeChangedListener listener)
	{
		toChangeListener.remove(listener);
	}	
	
	/**
	 * Add a Listener that needs to be informed, if a dataset state was changed 
	 * @param listener - the {@link DataSetChangeListener} that listens
	 */
	public void addDataSetChangeListener(DataSetChangeListener listener)
	{
		changedListener.add(listener);
	}
	/**
	 * Remove a Listener that needs to be informed, if a dataset state was changed 
	 * @param listener - the {@link DataSetChangeListener} that listens
	 */	
	public void removeDataSetChangeListener(DataSetChangeListener listener)
	{
		changedListener.remove(listener);
	}
	public Collection<DataSet> getDataSets()
	{
		return DataSets.values();
	}
	
	/**
	 * Inform all listeners that a set of {@link DataSet}s has been changed.
	 * @param added
	 */
	private void fireDataSetsChanged(Collection<DataSet> changed)
	{
		Set<DataSetAboutToBeChangedListener> calisteners = new HashSet();
		calisteners.addAll(toChangeListener);
		for(DataSetAboutToBeChangedListener listener : calisteners)
		{
			listener.datasetsChanged(new DataSetsChangedEvent(this, changed, false,false,true));
		}		
		Set<DataSetChangeListener> clisteners = new HashSet();
		clisteners.addAll(changedListener);
		for(DataSetChangeListener listener : clisteners)
		{
			listener.datasetsChanged(new DataSetsChangedEvent(this, changed, false,false,true));
		}
	}
	
	
	/**
	 * Inform all listeners that a {@link DataSet} has been added.
	 * @param added
	 */
	private void fireDataSetAdded(DataSet added)
	{
		
		Set<DataSetAboutToBeChangedListener> calisteners = new HashSet();
		calisteners.addAll(toChangeListener);
		for(DataSetAboutToBeChangedListener listener : calisteners)
		{
			listener.datasetChanged(new DataSetChangedEvent(this, added, true,false,false));
		}		
		Set<DataSetChangeListener> clisteners = new HashSet();
		clisteners.addAll(changedListener);
		for(DataSetChangeListener listener : clisteners)
		{
			listener.datasetChanged(new DataSetChangedEvent(this, added, true,false,false));
		}
	}
	/**
	 * Inform all listeners that a dataset has been removed
	 * @param removed
	 */
	private void fireDataSetRemoved(DataSet removed)
	{
		Set<DataSetAboutToBeChangedListener> calisteners = new HashSet();
		calisteners.addAll(toChangeListener);
		for(DataSetAboutToBeChangedListener listener : calisteners)
		{
			listener.datasetChanged(new DataSetChangedEvent(this, removed, false,true,false));
		}
		Set<DataSetChangeListener> clisteners = new HashSet();
		clisteners.addAll(changedListener);
		for(DataSetChangeListener listener : clisteners)
		{
			listener.datasetChanged(new DataSetChangedEvent(this, removed, false,true,false));
		}
	}
	/**
	 * Inform all listeners, that datasets have been removed.
	 * @param sets
	 */
	private void fireDataSetsRemoved(Collection<DataSet> sets)
	{
		Set<DataSetAboutToBeChangedListener> calisteners = new HashSet();
		calisteners.addAll(toChangeListener);
		for(DataSetAboutToBeChangedListener listener : calisteners)
		{
			listener.datasetsChanged(new DataSetsChangedEvent(this, sets, false,true,false));
		}
		Set<DataSetChangeListener> clisteners = new HashSet();
		clisteners.addAll(changedListener);
		for(DataSetChangeListener listener : clisteners)
		{
			listener.datasetsChanged(new DataSetsChangedEvent(this, sets, false,true,false));
		}
	}
	/**
	 * Add a Dataset to this Manager.
	 * @param newDataSet - The Dataset to add
	 */
	public void addDataSet(DataSet newDataSet)
	{
		newDataSet.setID(idprovider.getNextID());
		DataSets.put(newDataSet.getID(),newDataSet);
		fireDataSetAdded(newDataSet);
	}
	/**
	 * Remove a {@link DataSet} from this Manager
	 * @param setToRemove
	 */
	public void removeDataSet(DataSet setToRemove)
	{
		DataSets.remove(setToRemove.getID());		
		fireDataSetRemoved(setToRemove);
	}
	

	/**
	 * Get the dataset with the specified ID from this manager.
	 * @param id - the requested ID
	 * @return the requested {@link DataSet} or <code>null</code> if no set has this id.
	 */
	public DataSet getDataSetForID(Integer id)
	{
		return DataSets.get(id);
	}
	/**
	 * Reset this DataSetManager clearing all datastructures.
	 */
	public void reset()
	{
		Vector<DataSet> toRemove = new Vector<DataSet>();
		toRemove.addAll(DataSets.values());		
		fireDataSetsRemoved(toRemove);
		DataSets.clear();
		idprovider.reset(0);
	}
	/**
	 * Handle A SessionLoadedevent, since order is relevant this object is not directly a listener but gets the command from a listener.
	 * @param arg0
	 */
	public void handleEvent(SessionLoadedEvent arg0) {
		// First, clear the temporary Folder!. This folder contains data from an old session and thus needs to be reset.		
		try{
			IOUtils.clearTemporaryFolder();
		}
		catch(IOException e)
		{			
		}
		
		List<File> DataSetFiles = arg0.getLoadedSession().getAppFileListMap().get(IMAGENODEPROPERTIES.DATASET_FILES);		
		
		if(DataSetFiles == null)
		{
			//There is nothing to load!
			return;
		}
		Vector<DataSet> datasets = new Vector<DataSet>();
		try{
			ObjectInputStream os = new ObjectInputStream(new FileInputStream(DataSetFiles.get(0)));

			datasets = readDataSets(os);
			os.close();
		}
		catch(IOException e)
		{
			PrintFDebugger.Debugging(this, "Error while reading the Datasets");
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}
		int maxsetID = 0;
		for(DataSet ds :datasets)
		{	
			maxsetID = Math.max(maxsetID, ds.getID());
			DataSets.put(ds.getID(),ds);
			idprovider.reset(maxsetID);				
			fireDataSetAdded(ds);
			//System.out.println(currentSet.toString());
		}


	}
		
/*	/**
	 * Read the property file 
	 * @param PropertyFile - The Property File
	 * @return A Map that maps filenames to datasets.
	 * @throws IOException - IOException, if there is a problem while reading the file
	 */
/*	private HashMap<String,DataSet> readDataSetProperties(File PropertyFile) throws IOException
	{
		HashMap<String,DataSet>  FileNameToSet = new HashMap<String, DataSet>();
		BufferedReader br = new BufferedReader(new FileReader(PropertyFile));
		String currentline = br.readLine();
		String SetFileName = "";
		int SetID = 0;
		Boolean Twocolumn = false;;
		String DataType = "";
		String SetDescription = "";
		while(currentline != null)
		{			
			String[] items = currentline.split(" : ");
			if(!currentline.contains(" : "))
			{
				DataSet ds = createDataSet(SetID,Twocolumn,DataType,SetDescription);
				if(ds != null)
				{
					FileNameToSet.put(SetFileName,ds);
				}
				SetFileName = "";
				SetDescription = "";
				SetID = 0;
				Twocolumn = false;
				DataType = "";
			}
			else
			{
				switch(items[0])
				{
				case "File":
				{
					SetFileName = items[1].trim();
					break;
				}
				case "ID":
				{
					SetID = Integer.parseInt(items[1].trim());
					break;
				}
				case "TwoColumn":
				{
					Twocolumn = Boolean.parseBoolean(items[1].trim());
					break;
				}
				case "DataType":
				{
					DataType = items[1].trim();
					break;
				}
				case "Description":
				{
					if(items.length < 2)
					{
						SetDescription = "";
					}
					else
					{
						SetDescription = items[1].trim();
					}
					break;
				}
				}				
			}
			
			currentline = br.readLine();
		}
		return FileNameToSet;
	}
	/**
	 * Create a new {@link DataSet} Instance for given properties
	 * @param SetID - ID of the Set to create
	 * @param TwoCols - twocolum flag of the set
	 * @param dataSetClassName - Type (i.e. classname) of the set
	 * @param SetDescription - Description of the set.
	 * @return the dataset
	 
	private DataSet createDataSet(int SetID, boolean TwoCols, String dataSetClassName, String SetDescription)
	{
		try{
			DataSet ds = DataSetFactory.getDataSet(dataSetClassName);
			ds.setPropertyOptions(DataSetPropertyOptions.get(ds.getClass()));
			ds.setID(SetID);
			ds.useTwoColHeaders = TwoCols;
			ds.Description = SetDescription;
			return ds;
		}
		catch(ClassNotFoundException e)
		{		
			JOptionPane.showMessageDialog(null, "Did not find the class for DataSet with description. " + SetDescription + "\n It is likely that a plugin is missing");
			e.printStackTrace(System.out);
			throw new RuntimeException("Did not find the class for the Dataset with description "+ SetDescription +".\n This is likely due to a missing plugin.");
		}
	}*/
	
	public Vector<IDAREDatasetReader> getAvailableReaders()
	{
		Vector<IDAREDatasetReader> readers = new Vector<IDAREDatasetReader>();
		readers.addAll(dataSetReaders);
		return readers;
	}
	
	/**
	 * Create a Dataset based on properties and a DataSetFile. and add it to the manager.
	 * @param TwoCols - indicator whether to use twoColumn ID Indicators
	 * @param DataSetTypeName - Clas name of the dataset
	 * @param SetDescription - Description of the Dataset
	 * @param DataSetFile - File to load into the dataset
	 * @return - The Created Dataset with the data from the DataSetFile parsed.
	 * @throws WrongFormat - Depending on the Dataset specific properties have to be matched by the file
	 * @throws InvalidFormatException - Depending on the Dataset specific properties have to be matched by the file
	 * @throws DuplicateIDException - Depending on the Dataset specific properties have to be matched by the file
	 * @throws IOException
	 */
	public DataSet createDataSet(boolean TwoCols, String DataSetTypeName , 
			String SetDescription, IDAREWorkbook dsWorkBook) 
	throws ExecutionException,WrongFormat, InvalidFormatException, DuplicateIDException, IOException, 
	ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		System.out.println("Generating a dataset with twocolumnheaders set to " + TwoCols);
		DataSet ds = AvailableDataSetTypes.get(DataSetTypeName).newInstance();				
		ds.setPropertyOptions(DataSetPropertyOptions.get(AvailableDataSetTypes.get(DataSetTypeName)));
		ds.setID(idprovider.getNextID());
		ds.useTwoColHeaders = TwoCols;
		ds.Description = SetDescription;
		ds.parseFile(dsWorkBook);
		addDataSet(ds);

		return ds;
	}	

	
	private void writeDataSets(ObjectOutputStream os) throws IOException
	{
		//ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(LayoutFile));
		for(DataSet ds : DataSets.values())
		{				
			PrintFDebugger.Debugging(this, "Writing Dataset: " + ds.Description);
			os.writeObject(ds);
		}
		PrintFDebugger.Debugging(this, "Finished writing Datasets");
		os.writeObject(new EOOMarker());
		//os.close();
	}

	private Vector<DataSet> readDataSets(ObjectInputStream is) throws IOException
	{
		//ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(LayoutFile));
		Object currentObject = null;
		Vector<DataSet> datasets = new Vector<DataSet>();
		try{
			currentObject = is.readObject();
		}
		catch(ClassNotFoundException e)
		{
			//skip this object;
			PrintFDebugger.Debugging(this, "Didn't find the datasetclass for the first dataset");
		}
		while(!(currentObject instanceof EOOMarker))
		{
			if(currentObject instanceof DataSet)
			{
				DataSet ds = (DataSet)currentObject;
				datasets.add(ds);				
			}
			try{
				currentObject = is.readObject();
			}
			catch(ClassNotFoundException e)
			{
				PrintFDebugger.Debugging(this, "Didn't find the datasetclass for a dataset");
				currentObject = null;
			}
		}
		PrintFDebugger.Debugging(this, "Finished reading Datasets");
		return datasets;
		//os.close();
	}
	/**
	 * Handle A {@link SessionAboutToBeSavedEvent}, ad add data of the DataManger to this Event. 
	 * Since order is necessary this class does not itself implement the SessionLoaded/Saved mechanism but relies on a managing class to keep the order of actions.
	 * @param arg0
	 */	
	public void handleEvent(SessionAboutToBeSavedEvent arg0) {		
		
		//Map<String,List<File>> FileList = arg0.getAppFileListMap();
		LinkedList<File> PropertiesList = new LinkedList<>();
		LinkedList<File> DataFileList = new LinkedList<File>();
		//Create A Temporary Zip File
		StringBuffer descriptionbf = new StringBuffer();
		//Vector<String> existingFileNames = new Vector<>();
		File DataSetsFile = IOUtils.getTemporaryFile("DataSetFile",".bin");
		DataFileList.add(DataSetsFile);
		try
		{
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(DataSetsFile));
			writeDataSets(os);
			os.close();
		}
		catch(Exception e)
		{
			PrintFDebugger.Debugging(this, "Error during writing of the Datasets" );
			e.printStackTrace(System.out);
		}
		//for(DataSet ds : DataSets.values())
		//{			
			
			
			//String SourceFileName = ds.SourceFile.getName();
			//String Extension = SourceFileName.substring(SourceFileName.lastIndexOf("."));
			//String Name = SourceFileName.substring(0,SourceFileName.lastIndexOf("."));			
			//File TempFile = IOUtils.getTemporaryFile(Name,Extension);
			//try{
			//	Files.copy(ds.SourceFile.toPath(), TempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			//	ds.SourceFile = TempFile;				
			//}
			//catch(IOException e)
			//{			
			//	PrintFDebugger.Debugging(this, "Could not save the DataSets.\n");
			//	e.printStackTrace(System.out);
			//	throw new RuntimeException("IDAREApp: Could not save the DataSets.\n " + e.toString());				
			//}
			//descriptionbf.append(writeDataSetProperties(ds));
			//DataFileList.add(ds.SourceFile);
		//}
		//File DataPropertiesFile = new File(System.getProperty("java.io.tmpdir") + File.separator + IMAGENODEPROPERTIES.DATASET_PROPERTIES_FILE_NAME);
		//PropertiesList.add(DataPropertiesFile);
		//try{
		//BufferedWriter bw = new BufferedWriter(new FileWriter(DataPropertiesFile));			
		//bw.write(descriptionbf.toString());
		//bw.close();
		//}
		//catch(IOException e)
		//{
		//	JOptionPane.showMessageDialog(null, "Could not save the DataSet Properties.\n " + e.toString());
		//}
		try{
			//if the DataFileList is empty, there are no datasets...
			if(!DataFileList.isEmpty())
			{
				arg0.addAppFiles(IMAGENODEPROPERTIES.DATASET_FILES, DataFileList);			
		//		arg0.addAppFiles(IMAGENODEPROPERTIES.DATASET_PROPERTIES, PropertiesList);
			}
		}
		catch(Exception e)
		{
			PrintFDebugger.Debugging(this, "Could not save the DataSets.\n ");
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write DataSet Properties to a string (which can be read by <code>readDataSetProperties()</code>
	 * @param set - the set to obtain the properties from.
	 * @return the string representing the properties of the provided set
	 */
	private String writeDataSetProperties(DataSet set)
	{
		StringBuffer res = new StringBuffer();
		//String SourceFileName = set.SourceFile.getName();
		//res.append("File : " + SourceFileName + "\n");
		res.append("ID : " + set.getID() + "\n");
		res.append("TwoColumn : " + set.useTwoColHeaders + "\n");
		res.append("DataType : " + set.getClass().getCanonicalName() + "\n");
		res.append("Description : " + set.Description + "\n");
		res.append("\n");

		return res.toString();
	}
	
	/**
	 * Register a {@link IDAREDatasetReader} to be available for file reading.
	 * Readers are used in their reverse order of registration. i.e. the later a reader was registered, the higher its precedence of usage.
	 * Multiple readers for the same file extensions can be available, and thus the latest registered reader will be tried first.
	 * @param reader - the reader to add 
	 */
	public void registerDataSetReader(IDAREDatasetReader reader)
	{
		dataSetReaders.addFirst(reader);
	}
	
	/**
	 * DeRegister a {@link IDAREDatasetReader}. It will no longer be available for Datsetreading.
	 * @param reader - the reader to deregister 
	 */
	public void deregisterDataSetReader(IDAREDatasetReader reader)
	{
		dataSetReaders.remove(reader);
	}
	
}

