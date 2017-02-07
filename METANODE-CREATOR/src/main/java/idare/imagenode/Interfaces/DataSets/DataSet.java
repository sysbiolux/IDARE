package idare.imagenode.Interfaces.DataSets;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell.CellType;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.Properties.Localisation.Position;
import idare.imagenode.Utilities.StringUtils;
import idare.imagenode.exceptions.io.DuplicateIDException;
import idare.imagenode.exceptions.io.WrongFormat;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.IDAREService;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.omg.IOP.CodecPackage.FormatMismatch;

/**
 * A {@link DataSet} is a storage for data. This abstract class provides several utility methods which are common to most datasets (such as the determination of some properties for datasets).  
 * @author Thomas Pfau
 *
 */
@SuppressWarnings("rawtypes") 
public abstract class DataSet implements IDAREService, Serializable{
	
	public static String DATASETCOLORSCALE = "Dataset ColorScale";
	public static String DATASETDESCRIPTION = "Dataset Description";
	public static final String PROPERTYOPTIONSAVAILABLE = "Options Available";
	//public static String DataSetType = "Default DataSet"; 
		
	/**
	 * An Indicator whether the dataset uses numeric representation
	 */
	public boolean isnumeric = false;
	
	/**
	 * An Indicator whether the dataset uses numeric representation
	 */
	public boolean isstring = false;
	/**
	 * an indicator whether the dataset uses numeric data represented by strings
	 */
	public boolean numericstrings = false;
	/**
	 * An Indicator whether this is a discreet data set (i.e. no more than 5 different values)
	 */
	public boolean isdiscreet = false;
	/**
	 * An Indicator whether this dataset uses one or two columns to provide label/ID
	 */
	public boolean useTwoColHeaders;
	/**
	 * The Source File of this Dataset
	 */
	//public File SourceFile;
	/**
	 * The Description of this Dataset.
	 */
	public String Description;

	/**
	 * The properties to be used for this dataset, need to be set during reading of data.
	 */
	protected DataSetLayoutProperties datasetProperties;


	protected Vector<DataSetLayoutProperties> propertyOptions = new Vector<DataSetLayoutProperties>();
	protected Vector<Comparable> Valueset;
	protected int dataSetID;
	protected Double MinValue;
	protected Double MaxValue;
	
	
	protected static Properties getDefaultProperties()
	{
		Properties setproperties = new Properties();
		setproperties.setProperty(DATASETDESCRIPTION, "A default Dataset");	
		return setproperties;
	}
	
	public DataSet()
	{
		this(0,true,getDefaultProperties());
	}
	
	public DataSet(int DataSetID, boolean useTowColHeaders, Properties props)
	{
		dataSetID = DataSetID;
		this.useTwoColHeaders = useTowColHeaders;
		Description = props.getProperty(DATASETDESCRIPTION);
		Valueset = new Vector<>();
//		ValueTranslation = new HashMap<Comparable, Double>();
//		TranslationToObject = new HashMap<>();
	}	
	
	public void setProperties(Properties props)
	{
		String dsdesc = props.getProperty(DATASETDESCRIPTION);
		if(dsdesc != null)
		{
		
			Description = props.getProperty(DATASETDESCRIPTION);
		}
	}
	
	
	/**
	 * This function loads calls the implementing classes function setupWorkBook to interpret the Workbook provided and 
	 * tests, whether there are viable {@link DataSetLayoutProperties} in IDARE that can be used with this DataSet and the given Workbook. 
	 * @param WB The Workbook this DataSet should use to obtain data
	 * @return whether the parsing was successful
	 * @throws WrongFormat if none of the readers can read the file properly.
	 * @throws DuplicateIDException if there are duplicate ids in the files. 
	 * @throws IOException if there is a problem with the provided file.
	 */
	public final boolean loadWorkBook(IDAREWorkbook WB) throws WrongFormat,DuplicateIDException,IOException{
		if(WB == null)
		{		
			throw new WrongFormat("No Reader available for the file format, or invalid Format\n" );
		}
		setupWorkBook(WB);
		
		
		boolean valid = false;
		HashMap<String,WrongFormat> formatmismatchInfo = new HashMap<String, WrongFormat>();
		Vector<DataSetLayoutProperties> possibleprops = new Vector<DataSetLayoutProperties>(); 
		for(DataSetLayoutProperties props : getPropertyOptions())
		{
			try{
				props.testValidity(this);
				valid = true;
				possibleprops.add(props);
			}
			catch(WrongFormat e)
			{
				formatmismatchInfo.put(props.getTypeName(),e);
			}
		}
		if(!valid)
		{
			String message = "Format does not match any of the available options for " + getDataSetTypeName() +":\n";
			for(String type : formatmismatchInfo.keySet())
			{
				message += type + ": " + formatmismatchInfo.get(type).getMessage() + "\n";
			}
			throw new WrongFormat(message);
		}
		else
		{
			setPropertyOptionsUnchecked(possibleprops);
		}
		
		return true;

	}
	/**
	 * reset this Dataset to a stage as if it would have been just initialized.
	 */
	protected void reset()
	{
		isnumeric = false;
		numericstrings = false;
		isdiscreet = false;	
		Valueset = new Vector<Comparable>();
	}
	/**
	 * Define what type of headers are being used.
	 * @param twocols
	 */
	public void setTwoColumnHeaders(boolean twocols)
	{
		useTwoColHeaders = twocols;
	}	
	
	/**
	 * Set up the data properties from the provided workbook and read the data into this dataset.
	 * @param WB The WorkBook 
	 * @throws WrongFormat if there are problems with the format of the data in the Workbook.
	 * @throws DuplicateIDException if there are duplicate IDs in the Workbook (e.g. sheets which have the same identifier twice). 
	 */
	public final void setupWorkBook(IDAREWorkbook WB) throws WrongFormat,DuplicateIDException
	{
		preProcessWorkBook(WB);
//		PrintFDebugger.Debugging(this,"Determining Data Properties");
		determineDataProperties(WB);
		//now we can go on to read the data appropriately.  
		//up until this point it was onyl determined, whether the data is discreet (i.e. less than 6 different entries excluding null)
		// or whether we have continous data. Also the type of data was determined.
		//readWorkbookdata should also set up the Colormaps, as this is the first time that all items are being looked at.
//		PrintFDebugger.Debugging(this,"reading Data");
		readWorkBookData(WB);
		setupPropertyOptions();
	}

	/**
	 * 
	 */
	public void setupPropertyOptions() throws WrongFormat
	{
		Vector<DataSetLayoutProperties> props = new Vector<>();
		props.addAll(propertyOptions);
		String ErrorMessage = "";
		for(DataSetLayoutProperties cprop : props)
		{
			try{
				cprop.testValidity(this);
			}
			catch(Exception e)
			{
				propertyOptions.remove(cprop);
				ErrorMessage += e.getMessage() + "\n";
			}
		}
		if(propertyOptions.isEmpty())
		{
			throw new WrongFormat("No fitting Layout Properties available. Individual Errors were:\n" + ErrorMessage);
		}
	}
	
	
	/**
	 * Sets up the Data Type of this DataSet (reading all information to determine whether this is a numeric or a String data set 
	 * and whether this is a discreet or a continuous dataset.
	 * @param WB
	 * @throws WrongFormat
	 */
	private final void determineDataProperties(IDAREWorkbook WB) throws WrongFormat
	{		
		IDARESheet DataSheet = WB.getSheetAt(0);
		Iterator<IDARERow> rowIterator = DataSheet.iterator();
		//Skip the HEader row!
		rowIterator.next();
		//skip the label row		
		boolean uninitialized = true;
		int maxcolumn = 0;
		while(rowIterator.hasNext() && uninitialized)
		{
			Iterator<IDARECell> cellIterator = rowIterator.next().iterator();
			//Skip the first (and potentially second column)
			IDARECell startingCell = skipLabels(cellIterator);			
			while(cellIterator.hasNext())
			{
				IDARECell currentcell;
				if(startingCell != null)
				{
					currentcell = startingCell;
					startingCell = null;
				}
				else
				{
					currentcell = cellIterator.next();
				}
				if(currentcell == null)
				{
					continue;
				}
				maxcolumn = Math.max(maxcolumn, currentcell.getColumnIndex());

				if(currentcell.getCellType() == CellType.BLANK)
				{
					continue;
				}
				if(currentcell.getCellType() == CellType.STRING)
				{
					if(currentcell.getStringCellValue().equals("") || currentcell.getStringCellValue().trim().equals(""))
					{
						//ignore empty cells
						continue;
					}
					else
					{
						//emptycolumns.set(currentcell.getColumnIndex(), false);
						if(uninitialized)
						{
//							System.out.println("Found a String value: " + currentcell.getStringCellValue());
							//Now to avoid stupid things in Excel, like set formatting to String or similar things, check whether this is actually a numeric value.
							if(StringUtils.isNumeric(currentcell.getStringCellValue()))
							{
								//We want at least 2 samples which show this behaviour before we assume this to be numeric.
								if(numericstrings)
								{
									isnumeric = true;
									uninitialized = false;
									break;
								}
								numericstrings = true;
							//	continue;
							}		
							else
							{
								//This is a plain String! so
								isstring = true;
								isdiscreet = true;
								uninitialized = false;
							}
						}
					}

				}
				if(currentcell.getCellType() == CellType.NUMERIC || currentcell.getCellType() == CellType.FORMULA)
				{
					//System.out.println("Found a numeric value: " + currentcell.getNumericCellValue());
					if(uninitialized)
					{
						isnumeric = true;
						uninitialized = false;
					}

				}
			}

		}
		
		if(isnumeric && isstring)
		{
			throw new WrongFormat("Detected both String and multiple numeric values. Please use either Strings or numeric values as entries.");
		}
		//we check whether we have a discreet numeric set.
		if(isnumeric)
		{	
			Set<Comparable> entries = new HashSet<>(); 
			rowIterator = DataSheet.iterator();
			//Again skip the first row.
			rowIterator.next();
			while(rowIterator.hasNext() && isdiscreet)
			{				
				Iterator<IDARECell> cellIterator = rowIterator.next().iterator();			
				IDARECell startingCell = skipLabels(cellIterator);	
				
				while(cellIterator.hasNext())
				{
					IDARECell current; 
					if(startingCell != null)
					{
						current = startingCell;
						startingCell = null;
					}
					else{
						current = cellIterator.next();
					}
					if(current == null)
					{
						//skip null Cells 
						continue;
					}
					//blank cells are an NA type.
					if(current.getCellType() == CellType.BLANK)
					{
						entries.add(null);
					}
					else if(current.getCellType() == CellType.STRING)
					{
						//ignore empty cells with any number of spaces, they are undefined.
						if(current.getStringCellValue().trim().equals(""))
						{
							entries.add(null);
						}
						else
						{
							if(numericstrings)
							{
								if(StringUtils.isNumeric(current.getStringCellValue()))
								{
									entries.add(Double.parseDouble(current.getStringCellValue()));
								}
								else
								{
									throw new WrongFormat("Expected a Numeric Value or numeric formula. Got " + current.getStringCellValue() + "instead");	
								}
							}
							else
							{
								
								throw new WrongFormat("Expected a Numeric Value or numeric formula. Got " + current.getStringCellValue() + "instead");
							}
						}
					}
					else 
					{
						entries.add(current.getNumericCellValue());
					}
				}
				if(entries.size() > 6)
				{
					isdiscreet = false;					
				}
			}
			if(isdiscreet)
			{
				Vector<Comparable> entrynames = new Vector<Comparable>();
				entrynames.addAll(entries);
				if(entries.contains(null))
				{
					entries.remove(null);
				}			
				if(entries.size()>5)
				{
					isdiscreet = false;
				}
				Valueset.addAll(entrynames);
				Collections.sort(Valueset);
			}

		}
		else
		{
			Set<Comparable> entries = new HashSet<>(); 
			rowIterator = DataSheet.iterator();
			//Again skip the first row.
			rowIterator.next();
			while(rowIterator.hasNext() && isdiscreet)
			{
				Iterator<IDARECell> cellIterator = rowIterator.next().iterator();			
				IDARECell startingCell = skipLabels(cellIterator);	
				while(cellIterator.hasNext())
				{
					IDARECell current; 
					if(startingCell != null)
					{
						current = startingCell;
						startingCell = null;
					}
					else{
						current = cellIterator.next();
					}
					//blank cells are NA cells
					if(current.getCellType() == CellType.BLANK)
					{
						entries.add(null);
					}
					else if(current.getCellType() == CellType.STRING)
					{
						//ignore empty cells with any number of spaces, they are undefined.
						if(current.getStringCellValue().trim().equals(""))
						{
							entries.add(null);
						}
						else
						{
							entries.add(current.getStringCellValue());

						}
					}
					else 
					{
						throw new WrongFormat("Expected a String value. Got a non string value instead");
					}
					if(entries.size() > 6)
					{
						throw new WrongFormat("Using String Values with more than 5 different values is not possible.");				
					}
				}
			}			
			Vector<Comparable> entrynames = new Vector<Comparable>();
			if(entries.contains(null))
			{
				entries.remove(null);													
			}
			if(entries.size() >= 6)
			{
				throw new WrongFormat("Using String Values with more than 5 different values is not possible.");				
			}
			entrynames.addAll(entries);
			Valueset.addAll(entrynames);
			Collections.sort(Valueset);
		}

	}
	
	/**
	 * Skip the Cells containing the labels from a cell iterator 
	 * @param currentiterator the Celliterator for a specific row.
	 */
	public IDARECell skipLabels(Iterator<IDARECell> currentiterator)
	{
//		System.out.println("Skipping a column");
		IDARECell currentCell = currentiterator.next();
		int offset = useTwoColHeaders ? 2 : 1;
		while(currentiterator.hasNext())
		{
			if(currentCell != null)
			{
				PrintFDebugger.Debugging(this, "Current index is:" + currentCell.getColumnIndex() + " while offset is " + offset);
				if(currentCell.getColumnIndex() >= offset)
				{
					return currentCell;
				}
			}
			else
			{
				currentCell = currentiterator.next();
			}
			currentCell = currentiterator.next();
		}
		return null;
		
	}
	
	/**
	 * Return the flexibility of this dataset for layout purposes.
	 * @return whether this dataset can be layed out flexible or not 
	 */
	public boolean isFlexibility() {
		return datasetProperties.getItemFlexibility();
	}
	/**
	 * Get the preferred layout localisation of this dataset
	 * @return the preferred position.
	 */
	public Position getPreferredposition() {
		return datasetProperties.getLocalisationPreference();
	}
	/**
	 * The String representation of a DataSet is its description.
	 * @return The description of this set
	 */
	public String toString()
	{
		return Description;
	}
	/**
	 * Set the {@link DataSetLayoutProperties} currently used for this dataset
	 * @param properties The {@link DataSetLayoutProperties} to use for layout of this {@link DataSet} 
	 * 
	 */
	public void setProperties(DataSetLayoutProperties properties)
	{
		datasetProperties = properties;
	}
	
	/**
	 * Get the selection of different properties available for this dataset
	 * @return The possible DataSetproperties for this Type of Dataset
	 */
	public final Vector<DataSetLayoutProperties> getPropertyOptions() {
		Vector<DataSetLayoutProperties> props = new Vector<DataSetLayoutProperties>();
		props.addAll(propertyOptions);
		return props;
	}
	
	/**
	 * Sets the Options potentially available to this Dataset.
	 * This call does not check, whether the options supplied are actually viable options for this dataset!
	 * @param options The possible DataSetproperties for this Dataset
	 */
	public final void setPropertyOptionsUnchecked(Collection<DataSetLayoutProperties> options) {
		propertyOptions = new Vector<DataSetLayoutProperties>();
		propertyOptions.addAll(options);			
	}
	
	/**
	 * Add Property options to this Dataset, and indicate whether the addition was successfull. 
	 * @param propsToAdd
	 * @return whether the Properties were added. Returns false if either the properties are not valid or are already part of the propertyset
	 */
	public final boolean addPropertyOption(DataSetLayoutProperties propsToAdd)
	{
		try{
//			PrintFDebugger.Debugging(this, "Testing validity" );
			propsToAdd.testValidity(this);
//			PrintFDebugger.Debugging(this, "Success" );
			if(!propertyOptions.contains(propsToAdd))
			{
//				PrintFDebugger.Debugging(this, "PropertyOptions dont contain the new properties" );
				propertyOptions.add(propsToAdd);
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(WrongFormat e)
		{
			//Did not fit to this dataset.			
			e.printStackTrace(System.out);
			return false;
		}
	}
	
	/**
	 * Get the limits (minimum and maximum value) if this is a numeric dataset. 
	 * @return if the dataset is numeric, an Array of two doubles with result[0] being the minimum and results[1] being the maximal value. Otherwise null. 
	 */
	public Double[] getYAxisLimits()
	{		
		if(isnumeric)
		{
			return new Double[]{MinValue,MaxValue};
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Get a Description of this DataSet in the form of a {@link JPanel}.
	 * The function provides the Legend this is associated with (to be able to adjust the size of components for proper scaling).	 
	 * @param Legend The scroll pane to use for width adjustments
	 * @param DataSetLabel the label assigned to this dataset for description generation
	 * @param map the colormap assigned to this dataset for layouting
	 * @return the JPanel representing the DataDescription. 
	 */
	public JPanel getDataSetDescriptionPane(JScrollPane Legend, String DataSetLabel, ColorMap map)
	{		
		//we simply obtain the pane produced by the LayoutProperties.
		return datasetProperties.getDataSetDescriptionPane(Legend, DataSetLabel, map, this);
	}

	
	/**
	 * Get the {@link NodeData} for a specific ID.
	 * @param NodeID The ID of the {@link NodeData} requested.
	 * @return the requested NodeData (if it is not present an appropriate empty entry should be returned.)
	 */
	public abstract NodeData getDataForID(String NodeID);
	/**
	 * Set the ID of this DataSet
	 * @param id The ID assigned to this {@link DataSet}
	 */
	public abstract void setID(int id);
	/**
	 * Get the ID of this set.
	 * @return the internal ID of this {@link DataSet}
	 */
	public abstract int getID();
	/**
	 * Get the set of Node IDs contained in this {@link DataSet}.
	 * @return the set of node IDs in this {@link DataSet}
	 */
	public abstract Set<String> getNodeIDs();

	/**
	 * Get the {@link ColorMap}s available for this {@link DataSet}
	 * The objects in this vector should always be the same (i.e. two successive calls should yield colormaps that can be compare with ==) 
	 * @return A collection of {@link ColorMap}s available to this {@link DataSet}
	 */
	public abstract Vector<ColorMap> getColorMapOptions();
	
	/**
	 * Read the Data for this workbook into the dataset.
	 * This function should also remove any inappropriate property options, as those can only be determined now.
	 * @param WB
	 * @throws FormatMismatch
	 * @throws DuplicateIDException
	 */
	public abstract void readWorkBookData(IDAREWorkbook WB) throws WrongFormat,DuplicateIDException;
	/**
	 * Get the appropriate DataContainer for an ID.
	 * @return a Container for a specific ID 
	 */
	public abstract DataContainer getContainerForID(String ID);
	
	/**
	 * Get a sample container for Layout purposes
	 * @return a sample container using Default data. 
	 */
	public abstract DataContainer getLayoutContainer(DataSetLayoutProperties properties) throws WrongDatasetTypeException;
		
	
	/**
	 *  Get a default NodeData object for this dataset
	 *  @return the Default Data used in this dataset.
	 */
	public abstract NodeData getDefaultData();
	
	/**
	 * Get the Headers in this DataSet
	 * @return return all headers (either Double or String) in this {@link DataSet}.
	 */
	public abstract Vector<Comparable> getHeaders();
	/**
	 * Remove the given PropertyOptions from this dataset.
	 * @param propsToRemove
	 * @return whether the property was remoed from the set of properties (i.e. if it had been present)
	 */
	public final boolean removePropertyOption(DataSetLayoutProperties propsToRemove)
	{
		return propertyOptions.remove(propsToRemove);
	}
	
	/**
	 * Get the general Name for this Type of DataSet
	 */
	public abstract String getDataSetTypeName();

	/**
	 * Do preprocessing for the setup Process. This function is called before the actual data setup.
	 * @param WB The WorkBook to be preprocessed
	 * @throws WrongFormat
	 */
	public void preProcessWorkBook(IDAREWorkbook WB) throws WrongFormat
	{
		
	}
}
