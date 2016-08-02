package idare.metanode.internal.DataManagement;

import idare.metanode.internal.DataManagement.Events.DataSetAboutToBeChangedListener;
import idare.metanode.internal.DataManagement.Events.DataSetChangeListener;
import idare.metanode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.metanode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.metanode.internal.Debug.PrintFDebugger;
import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Interfaces.DataSetProperties;
import idare.metanode.internal.Properties.METANODEPROPERTIES;
import idare.metanode.internal.Utilities.IOUtils;
import idare.metanode.internal.exceptions.io.DuplicateIDException;
import idare.metanode.internal.exceptions.io.WrongFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionLoadedEvent;
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
	private HashMap<String,String> AvailableDataSetTypes = new HashMap<String, String>();
	private HashMap<String,Collection<DataSetProperties>> DataSetPropertyOptions = new HashMap<String,Collection<DataSetProperties>>();  
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
	 * Add an Entry to the TypeName -> classname translation table.
	 * @param TypeName
	 * @param className
	 */
	public void registerDataSetType(String TypeName, String className)
	{
		AvailableDataSetTypes.put(TypeName,className);
	}
	
	/**
	 * Add DataSetProperties for a specific dataset
	 */
	public boolean registerPropertiesForDataSet(Class datasetclass, DataSetProperties properties )
	{
		PrintFDebugger.Debugging(this, "Registering " + properties.getTypeName() + " for DataSetType " + datasetclass.getName());

		String classname = datasetclass.getCanonicalName();
		if(AvailableDataSetTypes.values().contains(classname))
		{
			if(!DataSetPropertyOptions.containsKey(classname)){
				DataSetPropertyOptions.put(classname,new Vector<DataSetProperties>());
			}
			if(DataSetPropertyOptions.get(classname).contains(properties))
			{
				PrintFDebugger.Debugging(this, "Properties already present");
				return false;
			}
			DataSetPropertyOptions.get(classname).add(properties);
			Vector<DataSet> changedSets = new Vector<DataSet>();
			for(DataSet ds : DataSets.values())
			{
				if(datasetclass.isInstance(ds))
				{
					Collection<DataSetProperties> validOptions = new HashSet<DataSetProperties>();
					for(DataSetProperties props : DataSetPropertyOptions.get(classname))
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
		if(AvailableDataSetTypes.values().contains(classname))
		{
			if(!DataSetPropertyOptions.containsKey(classname)){
				DataSetPropertyOptions.put(classname,new Vector<DataSetProperties>());
			}
			for(DataSetProperties props : properties)
			{
				PrintFDebugger.Debugging(this, "Registering " + props.getTypeName() + " for DataSetType " + datasetclass.getName());

				if(DataSetPropertyOptions.get(classname).contains(properties))
				{
					presentprops.add(props);
				}
				else
				{
					DataSetPropertyOptions.get(classname).add(props);
				}
			}
			
		}
		Vector<DataSet> changedSets = new Vector<DataSet>();
		for(DataSet ds : DataSets.values())
		{
			if(datasetclass.isInstance(ds))
			{
				Collection<DataSetProperties> validOptions = new HashSet<DataSetProperties>();
				for(DataSetProperties props : DataSetPropertyOptions.get(classname))
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
	public void deregisterPropertiesForDataSet(Class datasetclass, DataSetProperties properties )
	{
		PrintFDebugger.Debugging(this, "DeRegistering " + properties.getTypeName());

		String classname = datasetclass.getCanonicalName();
		if(AvailableDataSetTypes.values().contains(classname))
		{
			if(!DataSetPropertyOptions.containsKey(classname)){
				return;
			}
			else
			{
				DataSetPropertyOptions.get(classname).remove(properties);
			}
		}
		Vector<DataSet> changedSets = new Vector<DataSet>();
		for(DataSet ds : DataSets.values())
		{
			if(datasetclass.isInstance(ds))
			{
				ds.setPropertyOptions(DataSetPropertyOptions.get(classname));
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
		PrintFDebugger.Debugging(this, "Informing, that there were changes in the DataSets");
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
		PrintFDebugger.Debugging(this,"Reading Datasets");
		try{
			IOUtils.clearTemporaryFolder();
		}
		catch(IOException e)
		{			
		}
		
		List<File> DataSetFiles = arg0.getLoadedSession().getAppFileListMap().get(METANODEPROPERTIES.DATASET_FILES);
		List<File> DataSetPropertyFiles = arg0.getLoadedSession().getAppFileListMap().get(METANODEPROPERTIES.DATASET_PROPERTIES);
		
		if(DataSetPropertyFiles == null || DataSetFiles == null || DataSetPropertyFiles.isEmpty())
		{
			//There is nothing to load!
			return;
		}
		//There should only ever be one entry in the properties!
		File PropertyFile = DataSetPropertyFiles.get(0);
		//File Format for properties:
		//File : FileName (duplicates will be moved to a _XY
		//ID : The internal ID
		//TwoColumn : 0/1
		//DataType : Classname
		HashMap<String, DataSet> tempDS  = new HashMap<String, DataSet>();
		try
		{
		tempDS = readDataSetProperties(PropertyFile);
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, "Could not read the DataSet Properties.\n " + e.toString());		
			return;
		}
		HashMap<String,File> FileMap = new HashMap<>();
		DataSets = new HashMap<Integer, DataSet>();
		for(File f : DataSetFiles)
		{
			//System.out.println("Putting File : " + f.getName() );
			FileMap.put(f.getName(), f);
		}
		int maxsetID = 0;
		for(String SetName : tempDS.keySet())
		{
			DataSet currentSet = tempDS.get(SetName);			
			try{
				currentSet.parseFile(FileMap.get(SetName));
				maxsetID = Math.max(maxsetID, currentSet.getID());
				idprovider.reset(maxsetID);				
				DataSets.put(currentSet.getID(),currentSet);
				fireDataSetAdded(currentSet);
						
				//DataSets.put(currentSet.getID(),currentSet);
				
				
			}
			//If we get any error, the Dataset is not added to the current set of datasets and the maxid is not updated (could still be higher) 
			catch(InvalidFormatException e)
			{
				//This should not happen, as we saved this file 
			}
			catch(WrongFormat e)
			{
				//This should not happen, as we saved this file
			}
			catch( DuplicateIDException e)
			{
				//This should not happen, as we saved this file
			}
			catch(IOException e)
			{
				//This should not happen neither. 
			}
			
			//System.out.println(currentSet.toString());
		}


	}
	/**
	 * Read the property file 
	 * @param PropertyFile - The Property File
	 * @return A Map that maps filenames to datasets.
	 * @throws IOException - IOException, if there is a problem while reading the file
	 */
	private HashMap<String,DataSet> readDataSetProperties(File PropertyFile) throws IOException
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
	 */
	private DataSet createDataSet(int SetID, boolean TwoCols, String dataSetClassName, String SetDescription)
	{
		try{
			DataSet ds = DataSetFactory.getDataSet(dataSetClassName);
			ds.setPropertyOptions(DataSetPropertyOptions.get(dataSetClassName));
			ds.setID(SetID);
			ds.useTwoColHeaders = TwoCols;
			ds.Description = SetDescription;
			return ds;
		}
		catch(ClassNotFoundException e)
		{		
			JOptionPane.showMessageDialog(null, "Did not find the class for DataSet with description. " + SetDescription + "\n It is likely that a plugin is missing");
			e.printStackTrace(System.out);
		}
		return null;
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
	public DataSet createDataSet(boolean TwoCols, String DataSetTypeName , String SetDescription, File DataSetFile) throws WrongFormat, InvalidFormatException, DuplicateIDException, IOException, ClassNotFoundException
	{
			DataSet ds = DataSetFactory.getDataSet(AvailableDataSetTypes.get(DataSetTypeName));
			ds.setPropertyOptions(DataSetPropertyOptions.get(AvailableDataSetTypes.get(DataSetTypeName)));
			ds.setID(idprovider.getNextID());
			ds.useTwoColHeaders = TwoCols;
			ds.Description = SetDescription;
			ds.parseFile(DataSetFile);
			addDataSet(ds);
			return ds;		
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
		for(DataSet ds : DataSets.values())
		{			
			String SourceFileName = ds.SourceFile.getName();
			String Extension = SourceFileName.substring(SourceFileName.lastIndexOf("."));
			String Name = SourceFileName.substring(0,SourceFileName.lastIndexOf(".")-1);			
			File TempFile = IOUtils.getTemporaryFile(Name,Extension);
			try{
				Files.copy(ds.SourceFile.toPath(), TempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				ds.SourceFile = TempFile;
				
			}
			catch(IOException e)
			{				
				JOptionPane.showMessageDialog(null, "Could not save the DataSets.\n " + e.toString());
				return;
			}
			descriptionbf.append(writeDataSetProperties(ds));
			DataFileList.add(ds.SourceFile);
		}
		File DataPropertiesFile = new File(System.getProperty("java.io.tmpdir") + File.separator + METANODEPROPERTIES.DATASET_PROPERTIES_FILE_NAME);
		PropertiesList.add(DataPropertiesFile);
		try{
		BufferedWriter bw = new BufferedWriter(new FileWriter(DataPropertiesFile));			
		bw.write(descriptionbf.toString());
		bw.close();
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, "Could not save the DataSet Properties.\n " + e.toString());
		}
		try{
			//if the DataFileList is empty, there are no datasets...
			if(!DataFileList.isEmpty())
			{
				arg0.addAppFiles(METANODEPROPERTIES.DATASET_FILES, DataFileList);			
				arg0.addAppFiles(METANODEPROPERTIES.DATASET_PROPERTIES, PropertiesList);
			}
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "Could not save the DataSets.\n " + e.toString());
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
		String SourceFileName = set.SourceFile.getName();
		res.append("File : " + SourceFileName + "\n");
		res.append("ID : " + set.getID() + "\n");
		res.append("TwoColumn : " + set.useTwoColHeaders + "\n");
		res.append("DataType : " + set.getClass().getCanonicalName() + "\n");
		res.append("Description : " + set.Description + "\n");
		res.append("\n");

		return res.toString();
	}
	
		
}

