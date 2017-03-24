package idare.imagenode.Data.BasicDataTypes.ArrayData;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.ColorManagement.ColorScale;
import idare.imagenode.ColorManagement.ColorScaleFactory;
import idare.imagenode.ColorManagement.ColorMapTypes.ContinousZeroBalancedMap;
import idare.imagenode.ColorManagement.ColorMapTypes.DiscreteColorMap;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell.CellType;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.DataSets.NodeValue;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.exceptions.io.DuplicateIDException;
import idare.imagenode.exceptions.io.WrongFormat;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.Data.Array.CircleData.CircleDataSetProperties;
import idare.imagenode.internal.Data.Array.CircleGridData.CircleGridProperties;
import idare.imagenode.internal.Data.Array.RectangleData.RectangleDataSetProperties;
import idare.imagenode.internal.Data.Array.TimeSeriesData.TimeSeriesDataSetProperties;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.junit.validator.TestClassValidator;

/**
 * An Abstract basis class for item based datasets.
 * The specific layout can be individually adjusted.
 * @author Thomas Pfau
 *
 */
public class ArrayDataSet extends DataSet{
	//private static final Logger logger = LoggerFactory.getLogger(AbstractItemDataSet.class);
	public static String DataSetType = "Array Dataset";	
	protected HashMap<String,String> NodeLabels= new HashMap<String,String>();
	protected HashMap<String,ArrayNodeData> Data = new HashMap<>();
	//protected HashMap<Serializable,Double> DiscreetSet = new HashMap<>();	
	protected ArrayNodeData defaultEntry;
	protected Vector<ColorMap> colormaps = new Vector<ColorMap>();
	protected int columncount;
	protected boolean[] emptycolumns;
	public static final String DEFAULT_SERIES_NAME = "Dataset Series"; 

	/**
	 * A Vector indicating the labels of each column
	 */
	protected Vector<String> columnLabels = new Vector<String>();
	/**
	 * Set the default visualisation options for this type of Data.
	 */
	private void setDefaultOptions()
	{
		propertyOptions.add(new CircleGridProperties());		
		propertyOptions.add(new CircleDataSetProperties());
		propertyOptions.add(new RectangleDataSetProperties());
		propertyOptions.add(new TimeSeriesDataSetProperties());
		datasetProperties = propertyOptions.firstElement();
	}
	
	/**
	 * Get the Default Properties (Description/Colorscale etc) for this dataset.
	 * @return The default properties of this Dataset
	 */
	protected static Properties getDefaultProperties()
	{
		
		Properties setproperties = new Properties();
		setproperties.setProperty(DATASETDESCRIPTION, "An Abstract Item Dataset");
		setproperties.setProperty(DATASETCOLORSCALE, ColorScaleFactory.BLUEWHITERED);
		
		return setproperties;
	}
	
	
	/**
	 * Basic constructor using the default settings.
	 */
	public ArrayDataSet()
	{
		super(0,true,getDefaultProperties());	
		defaultEntry = new ArrayNodeData(this);
		setDefaultOptions();
	}
	/**
	 * Basic constructor using a specific DataSetName
	 * @param DataSetName The name of the Dataset
	 */
	public ArrayDataSet(String DataSetName)
	{
		super(0,true,getDefaultProperties());
		Properties prop = getDefaultProperties();
		prop.setProperty(DATASETDESCRIPTION, DataSetName);
		setProperties(prop);
		defaultEntry = new ArrayNodeData(this);
		setDefaultOptions();
	}
	
	/**
	 * Constructor using an ID, indication for twoColumn use in the datafile, and a properties collection
	 * @param DataSetID The ID of the {@link ArrayDataSet}
	 * @param useTwoCols Whether to use individual columns for label and ID or use one column for both
	 * @param properties properties of this {@link ArrayDataSet}
	 */
	public ArrayDataSet(int DataSetID,boolean useTwoCols,Properties properties) {
		super(DataSetID,useTwoCols,properties);
		defaultEntry = new ArrayNodeData(this);
		setDefaultOptions();
	}
		
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#preProcessWorkBook(idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook)
	 */
	@Override
	public void preProcessWorkBook(IDAREWorkbook WB) throws WrongFormat
	{
		//System.out.println("Lets set up the Workbook properties");
		determineNonEmptyColumns(WB);
		//readDataPointNames(WB.getSheetAt(0).rowIterator().next().cellIterator());			
	}
	
	
	/**
	 * Read the names of the Datapoints (i.e. the header line in the source data.
	 * @param labelIterator The iterator for the first row, to obtain the headers
	 */
	private void readDataPointNames(Iterator<IDARECell> labelIterator)
	{
		
		
//		System.out.println("Reading Header row");
		int offset = 1;
		if(useTwoColHeaders)
		{
			offset++;
		}
		//skipLabels(labelIterator);
		IDARECell cell = null;
		
		while (labelIterator.hasNext())
		{			
			if(cell == null)
			{
				cell = skipLabels(labelIterator);				
			}
			else
			{
				cell = labelIterator.next();	
			}
			
			int position = cell.getColumnIndex() - offset;
//			System.out.println("The offset is " + offset + " and the Cell type is " + cell.getCellType() + " while the index is " + cell.getColumnIndex());
			while(position > columnLabels.size())
			{
				//fill empty Headers with null.
//				System.out.println("Adding automatic null header at position" + (cell.getColumnIndex()-offset));
				columnLabels.add(null);
			}
			//Check the cell type and format accordingly
			if(cell.getCellType() == CellType.NUMERIC)
			{
				columnLabels.add(Double.toString(cell.getNumericCellValue()));
//				System.out.println("Adding header " + Double.toString(cell.getNumericCellValue()) + " at position" + (cell.getColumnIndex()-offset));
			}
			else if(cell.getCellType() == CellType.STRING)
			{
				columnLabels.add(cell.getStringCellValue());
//				System.out.println("Adding header " + cell.getStringCellValue() + " at position" + (cell.getColumnIndex()-offset));
			}
			else
			{
				// We can't read anything but strings and numeric values.
//				System.out.println("Adding null header at position" + (cell.getColumnIndex()-offset));
				columnLabels.add(null);
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#reset()
	 */
	
	@Override 
	protected void reset()
	{
		NodeLabels= new HashMap<String,String>();
		Data = new HashMap<>();
		//DiscreetSet = new HashMap<>();	
		defaultEntry = new ArrayNodeData(this);
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#readWorkBookData(idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook)
	 */
	@Override
	public void readWorkBookData(IDAREWorkbook WB) throws WrongFormat,DuplicateIDException
	{
		//We should clear the Data and everything;
//		PrintFDebugger.Debugging(this,"Resetting");
		reset();
//		PrintFDebugger.Debugging(this,"Obtaining first sheet");
		IDARESheet DataSheet = WB.getSheetAt(0);
		Iterator<IDARERow> rowIterator = DataSheet.iterator();
		//Read the first row, which are the headers (and labels)
		IDARERow labelRow = rowIterator.next();
		Iterator<IDARECell> labelIterator = labelRow.cellIterator();
		//skip the first row.
//		System.out.println("Skipping Labels for Header Row")
//		PrintFDebugger.Debugging(this,"Read Datapoint Names");
		readDataPointNames(labelIterator);	

		//read the data with the appropriate settings.
		if(isnumeric)
		{			
			if(numericstrings)
			{
//				PrintFDebugger.Debugging(this,"Read numeric data, with numeric strings");
				readNumericData(WB,true);
			}
			else
			{
//				PrintFDebugger.Debugging(this,"Read numeric data without strings");
				readNumericData(WB,false);
			}
		}
		else
		{
//			PrintFDebugger.Debugging(this,"Read String Data");
			readStringData(WB);
		}
		
		//If the dataset is discrete, we can actually use multiple additional Colors.
		if(isdiscreet)
		{
//			PrintFDebugger.Debugging(this,"Generating discrete ColorScales");
			Collection<ColorScale> scales = ColorScaleFactory.getDiscreetColorScales(Valueset.size());
//			PrintFDebugger.Debugging(this,"Adding ColorMaps");
			try{
			for(ColorScale scale : scales)
			{
//				PrintFDebugger.Debugging(this,"Setting Color Count for " + scale.getClass().getSimpleName() + " to " + Valueset.size());
				scale.setColorCount(Valueset.size());
//				PrintFDebugger.Debugging(this,"Initializing new ColorMap");
				DiscreteColorMap map = new DiscreteColorMap(Valueset, scale);
//				PrintFDebugger.Debugging(this,"Adding new ColorMap");
				colormaps.add(map);
			}	
			}
			catch(Exception e)
			{
			e.printStackTrace(System.out);	
			}
			//PrintFDebugger.Debugging(this,"Generating continous ColorScales");
			//scales = ColorScaleFactory.getContinousColorScales();
			 
			//for(ColorScale scale : scales)
			//{
			//	scale.setColorCount(Valueset.size());
			//	colormaps.add(new DiscreteColorMap(Valueset, scale));
			//}		
			
		}
		else
		{
			Collection<ColorScale> scales = ColorScaleFactory.getContinousColorScales();
			for(ColorScale scale : scales)
			{
				colormaps.add(new ContinousZeroBalancedMap(MaxValue,MinValue,scale));
			}			
		}
//		PrintFDebugger.Debugging(this,"Generating Setting Default value");
		Vector<NodeValue> DefaultValues = new Vector<NodeValue>();
		for(int i = 0; i < columncount; i++)
		{
			DefaultValues.add(new NodeValue(true));
		}
		//add some values to the default entry
		defaultEntry.setData(DefaultValues);
//		PrintFDebugger.Debugging(this,"Done");		
	}
	
	/**
	 * Read String data
	 * @param WB the workbook to read the data from.
	 * @throws WrongFormat If there is a format problem
	 * @throws DuplicateIDException If duplicate item IDs (i.e. two rows with the same ID entry) are found
	 */
	private void readStringData(IDAREWorkbook WB) throws WrongFormat,DuplicateIDException
	{
		Set<String> Stringvalues = new HashSet<>();
		IDARESheet DataSheet = WB.getSheetAt(0);
		Iterator<IDARERow> rowIterator = DataSheet.iterator();
		//skip the label row
		rowIterator.next();	
		//And set the ID row(s)
		int offset = 1;
		boolean hasentries = false;
		if(useTwoColHeaders)
		{
			offset = 2;
		}
		PrintFDebugger.Debugging(this, "Reading rows");

		while(rowIterator.hasNext())
		{							
			Vector<NodeValue> rowData = new Vector<>();
			//Get the current row.
			IDARERow row = rowIterator.next();
			ArrayNodeData currentData = getNodeData(row);
			PrintFDebugger.Debugging(this, "Reading Row " + row.getRowNum());
			if(currentData == null)
			{
				PrintFDebugger.Debugging(this, "Row Returned null");
				continue;
			}
			for(int i = 0; i < columncount; i++)
			{
				PrintFDebugger.Debugging(this, "Reading Column " + i+offset	);
				IDARECell currentCell = row.getCell(i+offset,IDARERow.RETURN_BLANK_AS_NULL);
				//currentCell.getColumnIndex();
				if(currentCell == null)
				{
					PrintFDebugger.Debugging(this, "Cell was null");
					rowData.add(new NodeValue(true));
					continue;
				}
				else
				{
					hasentries = true;
					rowData.add(new NodeValue(currentCell.getStringCellValue()));
					Stringvalues.add(currentCell.getStringCellValue());
				}				
			}		
			currentData.setData(rowData);
			if(Data.containsKey(currentData.getID()))
			{
				throw new DuplicateIDException(currentData.getID(),"In line " + row.getRowNum() + " Duplicate Entry ID detected (" + currentData.getID() + "!" );
			}
			Data.put(currentData.getID(), currentData);		
			
		}
		if(!hasentries)
		{
			System.out.println("StringData!");
			throw new WrongFormat("There is no usable data in the Dataset");
		}
		if(Stringvalues.size() > 6)
		{
			throw new WrongFormat("Too many different values for String based data!");
		}
		Vector<String> sortedStrings = new Vector<String>();
		
		sortedStrings.addAll(Stringvalues);
		Collections.sort(sortedStrings);
		//int Representative = 1;
		//for(String value : sortedStrings)
		//{
		//	DiscreetSet.put(value, new Double(Representative));
		//	PrintFDebugger.Debugging(this, "Adding " + value + " to the available options");
		//	Representative++;
		//}
		
	}
	/**
	 * Read numeric data, and indicate whether this si stringdata (i.e. numbers that wer fgormatted to be strings)
	 * @param WB The Workbook to read
	 * @param StringData Whether StringData is used (i.e. strings that represent numeric values)
	 * @throws DuplicateIDException If duplicate id are encountered
	 */
	private void readNumericData(IDAREWorkbook WB, boolean StringData) throws DuplicateIDException, WrongFormat
	{
		MinValue = Double.MAX_VALUE;
		MaxValue = Double.MIN_VALUE;
		IDARESheet DataSheet = WB.getSheetAt(0);
		Iterator<IDARERow> rowIterator = DataSheet.iterator();
		//skip the label row
		rowIterator.next();	
		boolean hasentries = false;
		int offset = 1;
		if(useTwoColHeaders)
		{
			offset = 2;
		}
		while(rowIterator.hasNext())
		{
					
			Vector<NodeValue> rowData = new Vector<>();
			IDARERow row = rowIterator.next();
			ArrayNodeData currentData = getNodeData(row);
			if(currentData == null)
			{
				continue;
			}
			for(int i = 0; i < columncount; i++)
			{
				IDARECell currentCell = row.getCell(i+offset,IDARERow.RETURN_BLANK_AS_NULL);
				if(currentCell == null)
				{
//					System.out.println("Adding null Cell");
					rowData.add(new NodeValue(true));
					continue;
				}
				else
				{
//					System.out.println("Adding non null Cell");
					hasentries = true;					
					double currentvalue = currentCell.getNumericCellValue();
					rowData.add(new NodeValue(currentvalue));
					MinValue = MinValue.compareTo(currentvalue) < 0 ? MinValue : currentvalue;
					MaxValue = MaxValue.compareTo(currentvalue) > 0 ? MaxValue : currentvalue;
				}				
			}		
			currentData.setData(rowData);
			if(Data.containsKey(currentData.getID()))
			{
				throw new DuplicateIDException(currentData.getID(),"In line " + row.getRowNum() + " Duplicate Entry ID detected (" + currentData.getID() + "!" );
			}
			Data.put(currentData.getID(), currentData);
		}
		if(!hasentries)
		{
			System.out.println("Numeric Data!");
			throw new WrongFormat("There is no usable data in the Dataset");
		}
	}

	/**
	 * Read the data for a single Row
	 * @param currentRow The {@link IDARERow} to read the data from.
	 * @return an AbstractItemNodeData representing the Row.
	 */
	private ArrayNodeData getNodeData(IDARERow currentRow) throws WrongFormat
	{
		ArrayNodeData currentNodeData = new ArrayNodeData(this);
		//System.out.println(currentRow.toString());
		CellType currentCellType = currentRow.getCell(0,IDARERow.CREATE_NULL_AS_BLANK).getCellType(); 
		if(currentCellType == CellType.STRING)
		{		
			currentNodeData.setID(currentRow.getCell(0,IDARERow.CREATE_NULL_AS_BLANK).getStringCellValue());
			currentNodeData.setLabel(currentRow.getCell(0,IDARERow.CREATE_NULL_AS_BLANK).getStringCellValue());
		}
		else if(currentCellType == CellType.NUMERIC)
		{
			//This is dangerous, as IDs can be screwed up...
			currentNodeData.setID(currentRow.getCell(0,IDARERow.CREATE_NULL_AS_BLANK).getFormattedCellValue());
			currentNodeData.setLabel(currentRow.getCell(0,IDARERow.CREATE_NULL_AS_BLANK).getFormattedCellValue());
		}
		else if(currentCellType == CellType.BLANK)
		{
			//No ID.
			return null;
		}
		else
		{
			throw new WrongFormat("Could not read headers, only Numeric and String values are allowed for headers");
		}		
		if(useTwoColHeaders)
		{
			currentCellType = currentRow.getCell(1,IDARERow.CREATE_NULL_AS_BLANK).getCellType();
			if(currentCellType == CellType.STRING)
			{		
				currentNodeData.setLabel(currentRow.getCell(1,IDARERow.CREATE_NULL_AS_BLANK).getStringCellValue());
			}
			else if(currentCellType == CellType.NUMERIC)
			{
				//DataFormatter df = new DataFormatter();
				currentNodeData.setLabel(currentRow.getCell(1,IDARERow.CREATE_NULL_AS_BLANK).getFormattedCellValue());
			}
			else if(currentCellType == CellType.BLANK)
			{
				//This is not nice... but we will allow a blank label.
				currentNodeData.setLabel("");
			}
			else
			{
				throw new WrongFormat("Could not read headers, only Numeric and String values are allowed for headers");
			}			
		}
		//System.out.println("Created NodeData with Label " + currentNodeData.getLabel() + " and ID " + currentNodeData.getID());
		if(currentNodeData.getID().equals(""))
		{
			//A Node without ID should not be generated
			//System.out.println("NodeID equals empty String, returning null ");
			return null;
		}
		NodeLabels.put(currentNodeData.getID(), currentNodeData.getLabel());
		return currentNodeData;
	}

	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#setTwoColumnHeaders(boolean)
	 */
	@Override
	public void setTwoColumnHeaders(boolean twocols) {
		// TODO Auto-generated method stub
		useTwoColHeaders = twocols;
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getDataForID(java.lang.String)
	 */
	@Override
	public NodeData getDataForID(String NodeID) {
	
		if(Data.containsKey(NodeID))
		{
			return Data.get(NodeID);
		}
		else
		{
			return defaultEntry;
		}
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#setID(int)
	 */
	@Override
	public void setID(int id) {
		// TODO Auto-generated method stub
		this.dataSetID = id;
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getID()
	 */
	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return dataSetID;
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getNodeIDs()
	 */
	@Override
	public Set<String> getNodeIDs() {
		// TODO Auto-generated method stub
		return Data.keySet();
	}
	/**
	 * Essentially a toString function to write this dataset.
	 * @return a string representing this dataset
	 */
	public String writeDataSet()
	{
		StringBuffer res = new StringBuffer();
		int offset = 1;
		if(useTwoColHeaders)
		{
			offset++;
		}
		for(int i = 0; i < offset-1; i++)
		{
			res.append('\t');
		}
		for(String collabel : columnLabels)
		{
			res.append('\t');
			if(collabel != null)
			{
				res.append(collabel);
			}
						
		}
		res.append('\n');
		//header done
		for(String ID : Data.keySet())
		{
			ArrayNodeData data = Data.get(ID);
			res.append(data.getID());
			if(useTwoColHeaders)
			{
				res.append("\t" + data.getLabel());
			}
			for(int i = 0 ; i < data.getValueCount(); i++)				
			{
				res.append('\t');
				if(!(data.getData(i).getValue() == null))
				{	
					if(isnumeric)

					{
						res.append((Double)(data.getData(i).getValue()));
					}	
					else
					{
						res.append((String)(data.getData(i).getValue()));
					}	
				}
				
			}
			res.append('\n');
		}
		return res.toString();
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getDefaultData()
	 */
	@Override
	public NodeData getDefaultData()
	{
		return defaultEntry;
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getContainerForID(java.lang.String)
	 */
	@Override
	public DataContainer getContainerForID(String ID) {
		try{
			return datasetProperties.newContainerInstance(this,Data.get(ID));
		}
		catch(WrongDatasetTypeException e)
		{
			//this should never happen.
			return null;
		}
		
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getLayoutContainer()
	 */
	@Override
	public DataContainer getLayoutContainer(DataSetLayoutProperties props) throws WrongDatasetTypeException
	{
		return props.newContainerInstance(this, Data.values().iterator().next()); 
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getDataSetTypeName()
	 */
	@Override
	public String getDataSetTypeName() {
		// TODO Auto-generated method stub
		return DataSetType;
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getColorMapOptions()
	 */
	@Override
	public Vector<ColorMap> getColorMapOptions() {
		// TODO Auto-generated method stub
		Vector<ColorMap> cms = new Vector<ColorMap>();
		cms.addAll(colormaps);
		return cms;
	}
	
	/**
	 * Check whether a specific column is set or empty.
	 * @param column the index of the column to check (this index excludes the ID and label columns and starts with the first data column as 0) 
	 * @return whether the given column has values
	 */
	public boolean isColumnSet(int column)
	{
		return !emptycolumns[column]; 
	}
	
	/**
	 * Get the Label for a specific Column in the data, or null if it does not exist, or there is no clear label.
	 * @param column get the label for the requested column index. if isColumnSet(column) returns false, getColumnLabel(column) will return null
	 * @return the label for the requested Column, or null if the header is not set.
	 */
	public String getColumnLabel(int column)
	{
//		System.out.println("The Column Label for column " + column + " is " + columnLabels.get(column));
		return columnLabels.get(column);
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getHeaders()
	 */
	@Override
	public Vector<Comparable> getHeaders() {
		Vector<Comparable> headers = new Vector<Comparable>();
		headers.addAll(columnLabels);
		return headers;
	}
	/**
	 * Determine the columns which are non empty, 
	 * This can be derived from the Header column as all columns which are set have to contain a label.
	 * @param WB The {@link IDAREWorkbook} to determine empty columns from. 
	 */	
	protected void determineNonEmptyColumns(IDAREWorkbook WB)
	{
		//The Relevant HEader Row for this type of dataset is the first Row in the first (and only) sheet.
		IDARERow HeaderRow = WB.getSheetAt(0).iterator().next();
		Iterator<IDARECell> cellIterator = HeaderRow.iterator();
		PrintFDebugger.Debugging(this, "Skipping Labels");
		IDARECell CurrentCell = skipLabels(cellIterator);
		PrintFDebugger.Debugging(this, "While determining nonempty columns,");
		CurrentCell.getColumnIndex();
		int labelcolumns = 1;
		if(useTwoColHeaders)
		{
			labelcolumns++;
		}
		PrintFDebugger.Debugging(this, "The number of labelcolumns is : " + labelcolumns);		
		if(CurrentCell != null)
		{
			columncount = Math.max(columncount, CurrentCell.getColumnIndex()-labelcolumns + 1);
		}
		
		while(cellIterator.hasNext())
		{						
			CurrentCell= cellIterator.next();			
			columncount = Math.max(columncount, CurrentCell.getColumnIndex()-labelcolumns + 1);
		}
		
		PrintFDebugger.Debugging(this,"We have a Workbook with "+ columncount + "Columns");
		emptycolumns = new boolean[columncount];
		for(int i = 0; i < columncount ; i++)
		{
			IDARECell current = HeaderRow.getCell(i+labelcolumns, IDARERow.RETURN_BLANK_AS_NULL);
			if(current != null)
			{
				emptycolumns[i] = false;
			}
			else
			{
				emptycolumns[i] = true;
			}
			
		}
		
		
	}	
	
}

