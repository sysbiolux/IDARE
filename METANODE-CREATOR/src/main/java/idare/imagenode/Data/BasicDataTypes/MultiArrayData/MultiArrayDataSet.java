package idare.imagenode.Data.BasicDataTypes.MultiArrayData;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.ColorManagement.ColorScale;
import idare.imagenode.ColorManagement.ColorScaleFactory;
import idare.imagenode.ColorManagement.ColorMapTypes.DiscreteColorMap;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell.CellType;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.Utilities.StringUtils;
import idare.imagenode.exceptions.io.DuplicateIDException;
import idare.imagenode.exceptions.io.WrongFormat;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.ColorManagement.ColorScales.LineScale;
import idare.imagenode.internal.Data.MultiArray.GraphData.GraphDataSetProperties;
import idare.imagenode.internal.Data.MultiArray.ScatterData.LargeScatterProperties;
import idare.imagenode.internal.Data.MultiArray.ScatterData.SmallScatterProperties;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Class to provide a basic setting for MultiArray Datasets.
 * This class will read in multiple sheets from a sourcefile, and generate Data structures accordingly.
 * If the headers of such a sheet are numeric values, the corresponding layouts have to treat the data quite differently to
 * to a string header based dataset, as numeric values indicate continous data, while string headers indicate discrete steps.
 * @author Thomas Pfau
 *
 */

public class MultiArrayDataSet extends DataSet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * 
	 */
	protected static String MultiArrayTypeName = "Multiarray Dataset"; 
	protected HashMap<String,MultiArrayNodeData> Data = new HashMap<>();
	protected HashMap<String,Vector<Comparable>> Headerlines = new HashMap<String, Vector<Comparable>>();	
	protected Vector<String> SheetNames = new Vector<String>();
	/**
	 * Indicator whether String headers are used
	 */
	public boolean stringheaders = false;
	/**
	 * Indicator whether numeric headers are used
	 */
	public boolean numericheaders = false;	
	/**
	 * Mixed headers can appear due to e.g. numeric strings
	 */
	public boolean mixedheaders = false;
	protected MultiArrayNodeData defaultData;
	protected Collection<ColorMap> colormaps = new Vector<ColorMap>();
	protected Vector<Comparable> allHeaders = new Vector<Comparable>();
	private SortedSet<HeaderPosition> headerpos = new TreeSet<MultiArrayDataSet.HeaderPosition>();
	/**
	 * Set Default options available.
	 */
	private void setDefaultOptions()
	{
		propertyOptions.add(new GraphDataSetProperties());
		propertyOptions.add(new LargeScatterProperties());
		propertyOptions.add(new SmallScatterProperties());
		datasetProperties = propertyOptions.firstElement();
	}

	/**
	 * Get the default properties for a {@link MultiArrayDataSet}.
	 * @return The default properties of a {@link MultiArrayDataSet}
	 */
	protected static Properties getDefaultProperties()
	{

		Properties setproperties = new Properties();
		setproperties.setProperty(DATASETDESCRIPTION, "A " + MultiArrayTypeName);
		setproperties.setProperty(DATASETCOLORSCALE, LineScale.class.getCanonicalName());		
		return setproperties;
	}

	/**
	 * Basic Constructor, generating an empty dataset.
	 */
	public MultiArrayDataSet() 
	{
		super(0,true,getDefaultProperties());	
		defaultData = new MultiArrayNodeData(this);
		setDefaultOptions();		
	}
	/**
	 * Constructor using an id, a two column indicator and the dataset properties.
	 * @param DataSetID The ID of the Dataset
	 * @param useTowColHeaders Whether to use two columns for label and id or only one for both
	 * @param DataSetProperties some properties of the dataset
	 */
	public MultiArrayDataSet(int DataSetID, boolean useTowColHeaders,
			Properties DataSetProperties) {
		super(DataSetID, useTowColHeaders, DataSetProperties);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getContainerForID(java.lang.String)
	 */
	@Override
	public DataContainer getContainerForID(String ID){
		// TODO Auto-generated method stub
		try{
			return datasetProperties.newContainerInstance(this, Data.get(ID));
		}
		catch(WrongDatasetTypeException e)
		{
			//This should not happen as we are using a valid property which was set during set
			return null;
		}
		
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#readWorkBookData(idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook)
	 */
	@Override
	public void readWorkBookData(IDAREWorkbook WB) throws WrongFormat,
	DuplicateIDException {
		if(isnumeric)
		{
			setupSheetData(WB);
			readNumericData(WB, numericstrings);
		}
		else
		{
			throw new WrongFormat("Cannot use Strings in a Graph Format");
		}
		Vector<Comparable> items = new Vector<Comparable>();
		items.addAll(SheetNames);
		//use discrete color scales
		Collection<ColorScale> scales = ColorScaleFactory.getNonWhiteDiscreteColorScales(items.size());
		for(ColorScale scale : scales)
		{
			scale.setColorCount(items.size());
			colormaps.add(new DiscreteColorMap(items, scale));
		}		
	}
	/**
	 * Set up the Header data for the different sheets.
	 * @param WB the workbook form which to obtain the sheets.
	 * @throws WrongFormat If there is a problem e.g. with strings instead of numeric values, or odd values.
	 */
	private void setupSheetData(IDAREWorkbook WB) throws WrongFormat
	{
		//First we will read the Header lines for all Sheets. 
		//While they can have different entries (if the numbers are numeric), no mix between numeric and string values is allowed.
		// in addition, no blank fields or empty fields are allowed in the headers.
		defaultData = new MultiArrayNodeData(this);
		for(int i = 0; i < WB.getNumberOfSheets(); i++)
		{			
			IDARESheet sheet = WB.getSheetAt(i);
			if(SheetNames.contains(sheet.getSheetName()))
			{
				throw new WrongFormat("Duplicate Sheet Name: " + sheet.getSheetName());
			}
			SheetNames.add(sheet.getSheetName());
			IDARERow HeaderRow = sheet.getRow(0);
			readHeaders(sheet.getSheetName(), HeaderRow, i);
			//int maxheadersize = 0;
			//for(String Sheet : Headerlines.keySet())
			//{
			//	maxheadersize = Math.max(maxheadersize, Headerlines.get(Sheet).size());
			//}
			//we do not have empty columns in this type of dataset.
		}
		//Now set the Header order		
		for(HeaderPosition Header : headerpos)
		{
			allHeaders.add(Header.header);
		}
	}
	/**
	 * Read the Header row of a Sheet with a specific sheet name.
	 * @param SheetName the name of the current sheet
	 * @param HeaderRow The HEader Row of the current Sheet
	 * @throws WrongFormat Only the same headers are allowed for each sheet if they are string headers, and no mixing of string and Numeric headers are allowed. 
	 *  
	 */
	private void readHeaders(String SheetName, IDARERow HeaderRow, int SheetNumber) throws WrongFormat
	{
		int offset = useTwoColHeaders ?  2 : 1;
		Vector<Comparable> Headers = new Vector<Comparable>();		
		for(int i = offset ; i < HeaderRow.getLastCellNum(); i++)			
		{
			IDARECell current = HeaderRow.getCell(i, IDARERow.RETURN_BLANK_AS_NULL);
			Comparable Header = null;							
			if(current == null)
			{
				throw new WrongFormat("Empty Column header in column " + (i+1) + " of sheet " + SheetName + " detected.\n No empty column headers allowed in a " + MultiArrayTypeName );
			}
			if(current.getCellType() == CellType.STRING)				
			{
				if(current.getStringCellValue().equals("")) 
				{
					throw new WrongFormat("Empty Column header in column " + (i+1) + " of sheet " + SheetName + " detected.\n No empty columns allowed in a " + MultiArrayTypeName);				
				}
				stringheaders = true;
				Header = current.getStringCellValue();
			}
			else if(current.getCellType() == CellType.NUMERIC || current.getCellType() == CellType.FORMULA)
			{
				numericheaders = true;
				Header = current.getNumericCellValue();
			}

			else{
				throw new WrongFormat("Unreadable cell type in headers in sheet " + SheetName + " and column " + i);
			}
			Headers.add(Header);
			headerpos.add(new HeaderPosition(Header, i, SheetNumber));
		}
		if(numericheaders & stringheaders)
		{
			numericheaders = false;
			stringheaders = false;
			mixedheaders = true;
		}
		Headerlines.put(SheetName, Headers);
	}
	/**
	 * Get the Data item corresponding to the ID, or create it using label and ID if it does not exist.
	 * @param ID the id for which to retrieve a {@link MultiArrayNodeData} object
	 * @param label potentially the label for initialization purposes // will be ignored, if the ID is already present.
	 * @return the {@link MultiArrayNodeData} corresponding to the given ID.
	 */
	private MultiArrayNodeData getNodeData(String ID, String label)
	{
		if(!Data.containsKey(ID) && ID != null && !ID.equals(""))
		{			
			MultiArrayNodeData item = new MultiArrayNodeData(this);
			item.setID(ID);
			item.setLabel(label);
			Data.put(ID, item );

		}
		return Data.get(ID);
	}

	/**
	 * generate the ValueDataSetValue for a given Row in a given Sheet.
	 * @param row the {@link IDARERow} to extract the values rom
	 * @param SheetName The name of the sheet containing the row.
	 * @return a {@link MultiArrayDataValue} that contains all values of the given row.
	 * @throws WrongFormat if the row does contain odd values
	 */
	private MultiArrayDataValue getValues(IDARERow row, String SheetName) throws WrongFormat
	{
		MultiArrayDataValue currentNodeValue = new MultiArrayDataValue(SheetName);
		CellType currentCellType = row.getCell(0,IDARERow.CREATE_NULL_AS_BLANK).getCellType();
		String currentID = "";
		String label = "";
		if(currentCellType == CellType.STRING)
		{		
			currentID = row.getCell(0,IDARERow.CREATE_NULL_AS_BLANK).getStringCellValue();
			label = row.getCell(0,IDARERow.CREATE_NULL_AS_BLANK).getStringCellValue();
		}
		else if(currentCellType == CellType.NUMERIC)
		{
			//DataFormatter df = new DataFormatter();
			currentID = row.getCell(0,IDARERow.CREATE_NULL_AS_BLANK).getFormattedCellValue();
			label = currentID;
		}
		else if(currentCellType == CellType.BLANK)
		{
			//No ID.
			//Don't do anything, this will be caught in the getNodeValue step.
		}		
		else
		{
			throw new WrongFormat("Could not read headers, only Numeric and String values are allowed for headers");
		}

		if(useTwoColHeaders)
		{
			currentCellType = row.getCell(1,IDARERow.CREATE_NULL_AS_BLANK).getCellType();
			if(currentCellType == CellType.STRING)
			{		

				label = row.getCell(1,IDARERow.CREATE_NULL_AS_BLANK).getStringCellValue();
			}
			else if(currentCellType == CellType.NUMERIC)
			{				
				//DataFormatter df = new DataFormatter();
				label = row.getCell(1,IDARERow.CREATE_NULL_AS_BLANK).getFormattedCellValue();						
			}			
			else
			{
				throw new WrongFormat("Could not read headers, only Numeric and String values are allowed for headers");
			}
		}
		MultiArrayNodeData currentNodeData = getNodeData(currentID,label);
		if(currentNodeData == null)
		{
			return null;
		}		

		//now, read the remaining line.

		int offset = useTwoColHeaders ? 2 : 1;
		Vector<Double> LineValues = new Vector<Double>();
		for(int i = offset; i < Headerlines.get(SheetName).size()+offset; i++)
		{
			IDARECell current = row.getCell(i, IDARERow.RETURN_BLANK_AS_NULL);
			if(current == null)
			{
				LineValues.add(null);
			}
			else if(current.getCellType() == CellType.STRING)
			{
				//Check for numeric values as strings and convert them if needed.
				if(StringUtils.isNumeric(current.getStringCellValue()))
				{

					Double cvalue = Double.parseDouble(current.getStringCellValue());			
					updateMinMaxVals(cvalue);
					if(Double.toString(MaxValue).equals("NaN"))
					{
						PrintFDebugger.Debugging(this, "In Column " + i + " and row " + row.getRowNum() + " : the Value cvalue was " + cvalue);
					}
					LineValues.add(cvalue);					
				}
				else
				{
					LineValues.add(null);
				}
			}
			else if(current.getCellType() == CellType.NUMERIC || current.getCellType() == CellType.FORMULA)
			{
				//in case of a formula or a nunmeric value just use that value.
				Double cvalue = current.getNumericCellValue();
				updateMinMaxVals(cvalue);
				LineValues.add(cvalue);
			}
			else
			{
				LineValues.add(null);
			}
		}
//		System.out.println("Setting LineValues for " + currentID + " in Sheet " + SheetName + " to a Vector of size " + LineValues.size()); 
		currentNodeValue.setEntryData(LineValues);
		return currentNodeValue;
	}

	/**
	 * update minimum and maximum values, given a new value.
	 * @param newval the new value that could change MinValue and MaxValue
	 */
	private void updateMinMaxVals(Double newval)
	{	
		MinValue = Math.min(MinValue, newval);
		MaxValue = Math.max(MaxValue, newval);
	}
	/**
	 * Read numeric Data 
	 * @param WB The Workbook to read from
	 * @param StringData whether this is a workbook that uses numeric strings.
	 * @throws DuplicateIDException if there are duplicate IDs
	 * @throws WrongFormat if there is a problem with the Workbook format
	 */
	private void readNumericData(IDAREWorkbook WB, boolean StringData) throws DuplicateIDException,WrongFormat
	{
		MinValue = Double.MAX_VALUE;
		MaxValue = Double.MIN_VALUE;		
		boolean hasentries = false;
		//skip the label row(s)
		for(int i = 0; i < WB.getNumberOfSheets(); i++ )
		{
			IDARESheet DataSheet = WB.getSheetAt(i);			
			Iterator<IDARERow> rowIterator = DataSheet.iterator();
			rowIterator.next();							
			while(rowIterator.hasNext())
			{			
				//Vector<NodeValue> rowData = new Vector<>();
				IDARERow row = rowIterator.next();
				if(row.getCell(0) == null)
				{
					//if the 0 cell is null, skip it.
					continue;
				}
				String ID = row.getCell(0).getStringCellValue();				
				MultiArrayDataValue currentValue = getValues(row, DataSheet.getSheetName());
				MultiArrayNodeData currentNodeData = Data.get(ID);

				//skip, if we have an invalid row.
				if(currentNodeData == null)
				{
					continue;
				}
				hasentries = true;
				//System.out.println("Setting Data for Sheet " + DataSheet.getSheetName() + " to currentValue with a size of " + currentValue.getEntryData().size());
				currentNodeData.addData(currentValue, DataSheet.getSheetName());
				//currentNodeData.printSheetsContained();
			}

		}
		if(!hasentries)
		{
			throw new WrongFormat("There is no usable data in the Dataset");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getDataForID(java.lang.String)
	 */
	@Override
	public NodeData getDataForID(String NodeID) {
		// TODO Auto-generated method stub
		if(Data.containsKey(NodeID))
		{
			return Data.get(NodeID);
		}
		else
		{
			return defaultData;
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
		Set<String> nodeIDs = new HashSet<String>();
		nodeIDs.addAll(Data.keySet());
		return nodeIDs;
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getLayoutContainer()
	 */
	@Override
	public DataContainer getLayoutContainer(DataSetLayoutProperties props) throws WrongDatasetTypeException{
		// TODO Auto-generated method stub
		return props.newContainerInstance(this, Data.values().iterator().next());						
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getDefaultData()
	 */
	@Override
	public NodeData getDefaultData() {
		// TODO Auto-generated method stub
		return defaultData;
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#getDataSetTypeName()
	 */	
	@Override
	public String getDataSetTypeName() {
		return MultiArrayTypeName;
	}

	/**
	 * Get the names of the Sheets represented in this DataSet
	 * @return a {@link Vector} of Strings with the names of the Sheets in this DataSet
	 */
	public Vector<String> getSetNames()
	{
		Vector<String> names = new Vector<String>();
		names.addAll(SheetNames);
		return names;
	}
	/**
	 * Get the Headers (Either String or Double, so handle with care)
	 * for a specific sheet in this Dataset, based on the sheet name.
	 * @param SheetName the Sheetname for which to obtain headers
	 * @return the HeaderColumn for the requested Sheet
	 */
	public Vector<Comparable> getHeadersForSheet(String SheetName)
	{
		Vector<Comparable> values = new Vector<Comparable>();
		for(Comparable comp : Headerlines.get(SheetName))
		{
			values.add(comp);
		}
		return values;
	}
	
	/**
	 * Get all values present in the headers of this DataSet
	 * @return a Vector of all headers in this dataset.
	 */
	public Vector<Comparable> getHeaders()
	{
		Vector<Comparable> headervals = new Vector<Comparable>();
		headervals.addAll(allHeaders);
		return headervals;
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataSet#reset()
	 */
	@Override 
	protected void reset()
	{
		Data = new HashMap<>();
		Headerlines = new HashMap<String, Vector<Comparable>>();	
		SheetNames = new Vector<String>();
		stringheaders = false;
		numericheaders = false;
		defaultData = null;
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
	 * This is a small helper class that allows the organisation of Headers 
	 * using their position and their value to arrange them.
	 * @author Thomas Pfau
	 *
	 */
	private class HeaderPosition implements Comparable<HeaderPosition>, Serializable
	{

		private Integer linePos;
		private Integer sheetPos;
		public Comparable header;
		public HeaderPosition(Comparable header, int Lineposition, int Sheetposition)
		{
			linePos = Lineposition;
			sheetPos = Sheetposition;
			this.header = header;
		}
		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(HeaderPosition o) {
			if(header.equals(o.header))
			{
				return header.compareTo(o.header);
			}
			if(linePos.compareTo(o.linePos) == 0)
			{
				return sheetPos.compareTo(o.sheetPos);
			}
			else
			{
				return linePos.compareTo(o.linePos);
			}
		}
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o)
		{
			try {
				return header.equals(((HeaderPosition)o).header); 
			}
			catch(ClassCastException e)
			{
				return false;
			}
		}
	}
}
