package idare.imagenode.internal.DataManagement;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.EOOMarker;
import idare.imagenode.Utilities.IOUtils;
import idare.imagenode.exceptions.io.DuplicateIDException;
import idare.imagenode.exceptions.io.WrongFormat;
import idare.imagenode.internal.DataManagement.Events.DataSetAboutToBeChangedListener;
import idare.imagenode.internal.DataManagement.Events.DataSetChangeListener;
import idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionLoadedEvent;
/**
 * A class to manage Datasets
 * @author Thomas Pfau
 *
 */
public class DataSetManager{

	/**
	 * A Map that maps the ID of the DataSet to the DataSet Objects.
	 */
	private Map<Integer, DataSet> dataSets;
	/**
	 * An IDProvider to use unique ids for the datasets controlled by this manager.
	 */
	private DataSetIDProvider idprovider;
	/**
	 * Listeners that are informed when a dataset is about to be altered.
	 */
	private Vector<DataSetAboutToBeChangedListener> datasetsAboutToChangeListeners = new Vector<DataSetAboutToBeChangedListener>();
	/**
	 * Listeners that are informed when a dataset is altered.
	 */
	private Vector<DataSetChangeListener> datasetsChangedListeners = new Vector<DataSetChangeListener>();
	/**
	 * The Available types (classes) of datasets.
	 * The class Names are matched to a class representative
	 */
	private HashMap<String,Class<? extends DataSet>> availableDataSetTypes = new HashMap<String, Class<? extends DataSet>>();
	/**
	 * {@link DataSetLayoutProperties} that are available for a specific {@link DataSet} Class
	 */
	private HashMap<Class<? extends DataSet>,Collection<DataSetLayoutProperties>> dataSetPropertyOptions = new HashMap<Class<? extends DataSet>, Collection<DataSetLayoutProperties>>();
	/**
	 * The set of available {@link IDAREDatasetReader}s.
	 */
	private LinkedList<IDAREDatasetReader> dataSetReaders = new LinkedList<IDAREDatasetReader>();
	/**
	 * Default constructor initializing required fields.
	 */
	public DataSetManager()
	{
		dataSets = new HashMap<Integer, DataSet>();
		idprovider = new DataSetIDProvider();		
	}
	
	/**
	 * Clear all Datasets, and inform any listeners that the sets are being removed. 
	 */
	public void clearDataSets()
	{
		fireDataSetsRemoved(dataSets.values());
		dataSets.clear();
		availableDataSetTypes.clear();
		dataSetPropertyOptions.clear();
	}
	/**
	 * Get the Types of Datasets available to this Manager.
	 * @return a {@link Collection} of {@link String}s of available {@link DataSet} types
	 */
	public Collection<String> getAvailableDataSetTypes()
	{
		return availableDataSetTypes.keySet();
	}
	
	/**
	 * Register a DataSetType that can be accessed by the TypeName.
	 * @param TypeName the TypeName (string description) of the registered DataSetType
	 * @param dataSetClass The Class of the DataSet
	 * @throws DuplicateIDException IF an ID for a type of dataset is already present
	 */
	public void registerDataSetType(String TypeName, Class<? extends DataSet> dataSetClass) throws DuplicateIDException
	{
		if(!availableDataSetTypes.containsKey(TypeName))
		{	
			availableDataSetTypes.put(TypeName,dataSetClass);		
		}
		else
		{
			throw new DuplicateIDException(TypeName, "Duplicate Type Name for DataSet encountered. Not adding the new dataset type.");
		}
	}

	/**
	 * Remove a class from the available Datasetclasses.
	 * This is skipped, if the class matching to the typename does not match the class provided. 
	 * @param TypeName the TypeName (string description) of the deregistered DataSetType
	 * @param dataSetClass The Class of the DataSet
	 */
	public void deRegisterDataSetType(String TypeName, Class<? extends DataSet> dataSetClass)
	{
		if(availableDataSetTypes.get(TypeName) != null && availableDataSetTypes.get(TypeName).equals(dataSetClass))
		{
			availableDataSetTypes.remove(TypeName);		
		}
	}
	
	/**
	 * Add DataSetProperties for a specific dataset
	 * @param classType The class to register the properties for
	 * @param properties The properties to make available for the given class
	 * @return whether the properties could be added
	 */
	public boolean registerPropertiesForDataSet(Class<? extends DataSet> classType, DataSetLayoutProperties properties )
	{
//		PrintFDebugger.Debugging(this, "Registering Properties for class " + classType);

		if(!dataSetPropertyOptions.containsKey(classType))
		{
			dataSetPropertyOptions.put(classType, new Vector<DataSetLayoutProperties>());
		}
		// if its not yet in, add it.		
		if(! dataSetPropertyOptions.get(classType).contains(properties))
		{
			dataSetPropertyOptions.get(classType).add(properties);
		}
		//otherwise do nothing 
		else
		{
			return false;
		}
		Vector<DataSet> changedDatasets = new Vector<DataSet>();
		for(DataSet ds : dataSets.values())
		{
			//Check whether the class fits and if it could be added.
//			PrintFDebugger.Debugging(this, "Checking whether the properties fit to the dataset: " + ds.Description);
			if(ds.getClass().equals(classType))
			{
				PrintFDebugger.Debugging(this, "Class Types match");
			}
			if(ds.getClass().equals(classType) && ds.addPropertyOption(properties))
			{
//				PrintFDebugger.Debugging(this, "Found a changing Dataset: " + ds.Description);
				changedDatasets.add(ds);
			}
		}
		if(changedDatasets.size() > 0)
		{
			fireDataSetsChanged(changedDatasets);
		}

		return true;
	}
	
	/**
	 * Register a set of properties for a datasetclass.
	 * @param classType the class of the dataset which to register the properties for.
	 * @param properties the property options for the dataset that should eb registered.
	 * @return The properties that were not added because they are already present.
	 */
	public Collection<DataSetLayoutProperties> registerPropertiesForDataSet(Class<? extends DataSet> classType, Collection<DataSetLayoutProperties> properties )
	{
//		PrintFDebugger.Debugging(this, "Registering Properties for class " + classType);
		Vector<DataSetLayoutProperties> propertiesToAdd = new Vector<DataSetLayoutProperties>();
		Vector<DataSetLayoutProperties> notAddedProperties = new Vector<DataSetLayoutProperties>();
		if(!dataSetPropertyOptions.containsKey(classType))
		{
			dataSetPropertyOptions.put(classType, new Vector<DataSetLayoutProperties>());
		}
		//add all options 
		propertiesToAdd.addAll(properties);
		notAddedProperties.addAll(properties);
		//and remove all that are already present.
		propertiesToAdd.removeAll(dataSetPropertyOptions.get(classType));
		notAddedProperties.removeAll(propertiesToAdd);
		
		dataSetPropertyOptions.get(classType).addAll(properties);
		Vector<DataSet> changedDatasets = new Vector<DataSet>();
		for(DataSet ds : dataSets.values())
		{
			for(DataSetLayoutProperties props : propertiesToAdd)
			{
				if(ds.getClass().equals(classType) && ds.addPropertyOption(props))
				{
//					PrintFDebugger.Debugging(this, "Found a changing Dataset: " + ds.Description);
					changedDatasets.add(ds);
				}
			}
		}
		if(changedDatasets.size() > 0)
		{
			fireDataSetsChanged(changedDatasets);
		}
		return notAddedProperties;
		
	}
	
	/**
	 * Deregister Properties for a DataSet
	 * @param classType the class of the dataset to deregister items for.
	 * @param properties the properties to deregister.
	 */
	public void deregisterPropertiesForDataSet(Class<? extends DataSet> classType, DataSetLayoutProperties properties )
	{
		//if this was an option, we try to remove it from all datasets.
		
		if(dataSetPropertyOptions.containsKey(classType) && dataSetPropertyOptions.get(classType).remove(properties))
		{
			Vector<DataSet> changedDatasets = new Vector<DataSet>();	
			for(DataSet ds : dataSets.values())
			{
				if(ds.getClass().equals(classType) && ds.removePropertyOption(properties))
				{
					changedDatasets.add(ds);
				}
			}
			fireDataSetsChanged(changedDatasets);
		}				
	}
	
	/**
	 * Add a Listener that needs to be informed, if a dataset state is about to be changed, before the change actually happened 
	 * @param listener the {@link DataSetAboutToBeChangedListener} that listens
	 */
	public void addDataSetAboutToBeChangedListener(DataSetAboutToBeChangedListener listener)
	{
		datasetsAboutToChangeListeners.add(listener);
	}
	/**
	 * Remove a {@link DataSetAboutToBeChangedListener} listening to changes in {@link DataSet}s 
	 * @param listener the {@link DataSetAboutToBeChangedListener} that listens
	 */	
	public void removeDataSetAboutToBeChangedListener(DataSetAboutToBeChangedListener listener)
	{
		datasetsAboutToChangeListeners.remove(listener);
	}	
	
	/**
	 * Add a Listener that needs to be informed, if a dataset state was changed 
	 * @param listener the {@link DataSetChangeListener} that listens
	 */
	public void addDataSetChangeListener(DataSetChangeListener listener)
	{
		datasetsChangedListeners.add(listener);
	}
	/**
	 * Remove a Listener that needs to be informed, if a dataset state was changed 
	 * @param listener the {@link DataSetChangeListener} that listens
	 */	
	public void removeDataSetChangeListener(DataSetChangeListener listener)
	{
		datasetsChangedListeners.remove(listener);
	}
	public Collection<DataSet> getDataSets()
	{
		return dataSets.values();
	}
	
	/**
	 * Inform all listeners that a set of {@link DataSet}s has been changed.
	 * @param changed The set of Datasets that were changed
	 */
	private void fireDataSetsChanged(Collection<DataSet> changed)
	{
		Set<DataSetAboutToBeChangedListener> calisteners = new HashSet();
		calisteners.addAll(datasetsAboutToChangeListeners);
		for(DataSetAboutToBeChangedListener listener : calisteners)
		{
			listener.datasetsChanged(new DataSetsChangedEvent(this, changed, false,false,true));
		}		
		Set<DataSetChangeListener> clisteners = new HashSet();
		clisteners.addAll(datasetsChangedListeners);
		for(DataSetChangeListener listener : clisteners)
		{
			listener.datasetsChanged(new DataSetsChangedEvent(this, changed, false,false,true));
		}
	}
	
	
	/**
	 * Inform all listeners that a {@link DataSet} has been added.
	 * @param added The Datasets that were added
	 */
	private void fireDataSetAdded(DataSet added)
	{
		
		Set<DataSetAboutToBeChangedListener> calisteners = new HashSet();
		calisteners.addAll(datasetsAboutToChangeListeners);
//		PrintFDebugger.Debugging(this,"Onforming early listeners");

		for(DataSetAboutToBeChangedListener listener : calisteners)
		{
			listener.datasetChanged(new DataSetChangedEvent(this, added, true,false,false));
		}		
		Set<DataSetChangeListener> clisteners = new HashSet();
		clisteners.addAll(datasetsChangedListeners);
//		PrintFDebugger.Debugging(this,"Informing late listeners");

		for(DataSetChangeListener listener : clisteners)
		{
			listener.datasetChanged(new DataSetChangedEvent(this, added, true,false,false));
		}
	}
	/**
	 * Inform all listeners that a dataset has been removed
	 * @param removed The dataset that was removed
	 */
	private void fireDataSetRemoved(DataSet removed)
	{
		Set<DataSetAboutToBeChangedListener> calisteners = new HashSet();
		calisteners.addAll(datasetsAboutToChangeListeners);
		for(DataSetAboutToBeChangedListener listener : calisteners)
		{
			listener.datasetChanged(new DataSetChangedEvent(this, removed, false,true,false));
		}
		Set<DataSetChangeListener> clisteners = new HashSet();
		clisteners.addAll(datasetsChangedListeners);
		for(DataSetChangeListener listener : clisteners)
		{
			listener.datasetChanged(new DataSetChangedEvent(this, removed, false,true,false));
		}
	}
	/**
	 * Inform all listeners, that datasets have been removed.
	 * @param sets The datasets that was removed
	 */
	private void fireDataSetsRemoved(Collection<DataSet> sets)
	{
		Set<DataSetAboutToBeChangedListener> calisteners = new HashSet();
		calisteners.addAll(datasetsAboutToChangeListeners);
		for(DataSetAboutToBeChangedListener listener : calisteners)
		{
			listener.datasetsChanged(new DataSetsChangedEvent(this, sets, false,true,false));
		}
		Set<DataSetChangeListener> clisteners = new HashSet();
		clisteners.addAll(datasetsChangedListeners);
		for(DataSetChangeListener listener : clisteners)
		{
			listener.datasetsChanged(new DataSetsChangedEvent(this, sets, false,true,false));
		}
	}
	/**
	 * Add a Dataset to this Manager.
	 * @param newDataSet The Dataset to add
	 */
	public void addDataSet(DataSet newDataSet)
	{
		newDataSet.setID(idprovider.getNextID());
		dataSets.put(newDataSet.getID(),newDataSet);
//		PrintFDebugger.Debugging(this,"Fireig update event");
		fireDataSetAdded(newDataSet);
	}
	/**
	 * Remove a {@link DataSet} from this Manager
	 * @param setToRemove the Dataset to remove
	 */
	public void removeDataSet(DataSet setToRemove)
	{
		dataSets.remove(setToRemove.getID());		
		fireDataSetRemoved(setToRemove);
	}
	

	/**
	 * Get the dataset with the specified ID from this manager.
	 * @param id the requested ID
	 * @return the requested {@link DataSet} or <code>null</code> if no set has this id.
	 */
	public DataSet getDataSetForID(Integer id)
	{
		return dataSets.get(id);
	}
	/**
	 * Reset this DataSetManager clearing all datastructures.
	 */
	public void reset()
	{
		Vector<DataSet> toRemove = new Vector<DataSet>();
		toRemove.addAll(dataSets.values());		
		fireDataSetsRemoved(toRemove);
		dataSets.clear();
		idprovider.reset(0);
	}
	/**
	 * Handle A SessionLoadedevent, since order is relevant this object is not directly a listener but gets the command from a listener.
	 * @param arg0 the {@link SessionLoadedEvent} to obtain data from
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
//			PrintFDebugger.Debugging(this, "Error while reading the Datasets");
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}
		int maxsetID = 0;
		for(DataSet ds :datasets)
		{	
			maxsetID = Math.max(maxsetID, ds.getID());
			dataSets.put(ds.getID(),ds);
			idprovider.reset(maxsetID);				
			fireDataSetAdded(ds);
			//System.out.println(currentSet.toString());
		}


	}

	/**
	 * Get all {@link IDAREDatasetReader}s currently registered with IDARE.
	 * @return a {@link Vector} of DataSetReaders in the reverse order of their addition to IDARE.
	 */
	public Vector<IDAREDatasetReader> getAvailableReaders()
	{
		Vector<IDAREDatasetReader> readers = new Vector<IDAREDatasetReader>();
		readers.addAll(dataSetReaders);
		return readers;
	}
	
	/**
	 * Create a Dataset based on properties and a DataSetFile. and add it to the manager.
	 * @param TwoCols indicator whether to use twoColumn ID Indicators
	 * @param DataSetTypeName Class name of the dataset
	 * @param SetDescription Description of the Dataset
	 * @param dsWorkBook The {@link IDAREWorkbook} to use for this {@link DataSet}
	 * @return The Created Dataset with the data from the DataSetFile parsed.
	 * @throws WrongFormat Depending on the Dataset specific properties have to be matched by the file
	 * @throws InvalidFormatException Depending on the Dataset specific properties have to be matched by the file
	 * @throws DuplicateIDException Depending on the Dataset specific properties have to be matched by the file
	 * @throws IOException if IO on the workbook failed
	 * @throws ExecutionException if something during the processing went wrong
	 * @throws ClassNotFoundException if the class for the Dataset is not found
	 * @throws IllegalAccessException if something wents off with access
	 * @throws InstantiationException if the Dataset cannot be instantiated
	 */
	public DataSet createDataSet(boolean TwoCols, String DataSetTypeName , 
			String SetDescription, IDAREWorkbook dsWorkBook) 
	throws ExecutionException,WrongFormat, InvalidFormatException, DuplicateIDException, IOException, 
	ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		PrintFDebugger.Debugging(this,"Generating a dataset with twocolumnheaders set to " + TwoCols);
		DataSet ds = availableDataSetTypes.get(DataSetTypeName).newInstance();		
		//here we just supply all options available. The DataSet Class will care about a proper selection later during readWorkBookData
		PrintFDebugger.Debugging(this,"Setting Property options");
		ds.setPropertyOptionsUnchecked(dataSetPropertyOptions.get(availableDataSetTypes.get(DataSetTypeName)));
		PrintFDebugger.Debugging(this,"obtaining new id" );
		ds.setID(idprovider.getNextID());		
		ds.useTwoColHeaders = TwoCols;
		ds.Description = SetDescription;
		PrintFDebugger.Debugging(this,"setting up workbook");
		ds.setupWorkBook(dsWorkBook);
//		PrintFDebugger.Debugging(this,"Adding Dataset");
		addDataSet(ds);
		return ds;
	}	

	/**
	 * Write The current datasets to the given outputstream
	 * @param os The {@link ObjectOutputStream} to write to
	 * @throws IOException If there is a problem with writing
	 */
	private void writeDataSets(ObjectOutputStream os) throws IOException
	{
		for(DataSet ds : dataSets.values())
		{				
//			PrintFDebugger.Debugging(this, "Writing Dataset: " + ds.Description);
			os.writeObject(ds);
		}
//		PrintFDebugger.Debugging(this, "Finished writing Datasets");
		os.writeObject(new EOOMarker());
		
	}

	/**
	 * Read {@link DataSet}s form an {@link ObjectInputStream}.
	 * @param is the inputstream to read from
	 * @return a vector of {@link DataSet}s obtained from the given inputstream
	 * @throws IOException if there is a problem with the stream
	 */
	private Vector<DataSet> readDataSets(ObjectInputStream is) throws IOException
	{
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
//				PrintFDebugger.Debugging(this, "Didn't find the datasetclass for a dataset");
				currentObject = null;
			}
		}
//		PrintFDebugger.Debugging(this, "Finished reading Datasets");
		return datasets;
		//os.close();
	}
	/**
	 * Handle A {@link SessionAboutToBeSavedEvent}, ad add data of the DataManger to this Event. 
	 * Since order is necessary this class does not itself implement the SessionLoaded/Saved mechanism but relies on a managing class to keep the order of actions.
	 * @param arg0 a {@link SessionAboutToBeSavedEvent}
	 */	
	public void handleEvent(SessionAboutToBeSavedEvent arg0) {		
		
		LinkedList<File> DataFileList = new LinkedList<File>();
		//Create A Temporary Zip File
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
//			PrintFDebugger.Debugging(this, "Error during writing of the Datasets" );
			e.printStackTrace(System.out);
		}
	
		try{
			//if the DataFileList is empty, there are no datasets...
			if(!DataFileList.isEmpty())
			{
				arg0.addAppFiles(IMAGENODEPROPERTIES.DATASET_FILES, DataFileList);			
			}
		}
		catch(Exception e)
		{
//			PrintFDebugger.Debugging(this, "Could not save the DataSets.\n ");
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Register a {@link IDAREDatasetReader} to be available for file reading.
	 * Readers are used in their reverse order of registration. i.e. the later a reader was registered, the higher its precedence of usage.
	 * Multiple readers for the same file extensions can be available, and thus the latest registered reader will be tried first.
	 * @param reader the reader to add 
	 */
	public void registerDataSetReader(IDAREDatasetReader reader)
	{
		dataSetReaders.addFirst(reader);
	}
	
	/**
	 * DeRegister a {@link IDAREDatasetReader}. It will no longer be available for Datsetreading.
	 * @param reader the reader to deregister 
	 */
	public void deregisterDataSetReader(IDAREDatasetReader reader)
	{
		dataSetReaders.remove(reader);
	}
	
}

