package idare.metanode.internal.Data.ValueSetData;

import idare.metanode.internal.ColorManagement.ColorMap;
import idare.metanode.internal.ColorManagement.ColorScale;
import idare.metanode.internal.ColorManagement.ColorScaleFactory;
import idare.metanode.internal.ColorManagement.ColorMapTypes.DiscreteColorMap;
import idare.metanode.internal.ColorManagement.ColorScales.LineScale;
import idare.metanode.internal.Data.ValueSetData.GraphData.GraphDataSetProperties;
import idare.metanode.internal.Data.ValueSetData.ScatterData.LargeScatterProperties;
import idare.metanode.internal.Data.ValueSetData.ScatterData.SmallScatterProperties;
import idare.metanode.internal.DataSetReaders.IDARECell;
import idare.metanode.internal.DataSetReaders.IDARERow;
import idare.metanode.internal.DataSetReaders.IDARESheet;
import idare.metanode.internal.DataSetReaders.IDAREWorkbook;
import idare.metanode.internal.Debug.PrintFDebugger;
import idare.metanode.internal.IO.StringUtils;
import idare.metanode.internal.Interfaces.DataContainer;
import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Interfaces.DataSetProperties;
import idare.metanode.internal.Interfaces.NodeData;
import idare.metanode.internal.Interfaces.NodeValue;
import idare.metanode.internal.exceptions.io.DuplicateIDException;
import idare.metanode.internal.exceptions.io.WrongFormat;

import java.awt.datatransfer.DataFlavor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
/**
 * Class to provide a basic setting for ValueSet Datasets.
 * This class will read in multiple sheets from a sourcefile, and generate Data structures accordingly.
 * 
 * @author Thomas Pfau
 *
 */

public class ValueSetDataSet extends DataSet{

	protected HashMap<String,ValueSetNodeData> Data = new HashMap<>();
	protected HashMap<String,Vector<Comparable>> Headerlines = new HashMap<String, Vector<Comparable>>();	
	protected Vector<String> SheetNames = new Vector<String>();
	public boolean stringheaders = false;
	public boolean numericheaders = false;	
	public boolean mixedheaders = false;
	Vector<DataSetProperties> propertyOptions = new Vector<DataSetProperties>();
	protected ValueSetNodeData defaultData;
	protected Collection<ColorMap> colormaps = new Vector<ColorMap>();
	protected Vector<Comparable> allHeaders = new Vector<Comparable>();
	private SortedSet<HeaderPosition> headerpos = new TreeSet<ValueSetDataSet.HeaderPosition>();
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
	 * Get the default properties for a valueset.
	 * @return
	 */
	protected static Properties getDefaultProperties()
	{

		Properties setproperties = new Properties();
		setproperties.setProperty(DATASETDESCRIPTION, "A Value Set Dataset");
		setproperties.setProperty(DATASETCOLORSCALE, LineScale.class.getCanonicalName());		
		return setproperties;
	}

	/**
	 * Basic Constructor, generating an empty dataset.
	 */
	public ValueSetDataSet() 
	{
		super(0,true,getDefaultProperties());	
		defaultData = new ValueSetNodeData(this);
		setDefaultOptions();		
	}
	/**
	 * Constructor using an id, a two column indicator and the dataset properties.
	 * @param DataSetID
	 * @param useTowColHeaders
	 * @param DataSetProperties
	 */
	public ValueSetDataSet(int DataSetID, boolean useTowColHeaders,
			Properties DataSetProperties) {
		super(DataSetID, useTowColHeaders, DataSetProperties);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DataContainer getContainerForID(String ID) {
		// TODO Auto-generated method stub
		return datasetProperties.newContainerInstance(this, Data.get(ID));
	}

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
		Collection<ColorScale> scales = ColorScaleFactory.getDiscreetColorScales(items.size());
		for(ColorScale scale : scales)
		{
			scale.setColorCount(items.size());
			colormaps.add(new DiscreteColorMap(items, scale));
		}		
	}
	/**
	 * Set up the Header data for the different sheets.
	 * @param WB the workbook form which to obtain the sheets.
	 * @throws WrongFormat
	 */
	private void setupSheetData(IDAREWorkbook WB) throws WrongFormat
	{
		//First we will read the Header lines for all Sheets. 
		//While they can have different entries (if the numbers are numeric), no mix between numeric and string values is allowed.
		// in addition, no blank fields or empty fields are allowed in the headers.
		defaultData = new ValueSetNodeData(this);
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
			int maxheadersize = 0;
			for(String Sheet : Headerlines.keySet())
			{
				maxheadersize = Math.max(maxheadersize, Headerlines.get(Sheet).size());
			}
			//we do not have empty columns in this type of dataset.
			emptycolumns = new boolean[maxheadersize];					
		}
		//Now set the Header order		
		for(HeaderPosition Header : headerpos)
		{
			allHeaders.add(Header.header);
		}
	}
	/**
	 * Read the Header row of a Sheet with a specific sheet name.
	 * @param SheetName - the name of the current sheet
	 * @param HeaderRow - The HEader Row of the current Sheet
	 * @throws WrongFormat - Only the same headers are allowed for each sheet if they are string headers, and no mixing of string and Numeric headers are allowed. 
	 *  
	 */
	private void readHeaders(String SheetName, IDARERow HeaderRow, int SheetNumber) throws WrongFormat
	{
		int offset = useTwoColHeaders ?  2 : 1;
		Vector<Comparable> Headers = new Vector<Comparable>();		
		for(int i = offset ; i < HeaderRow.getLastCellNum(); i++)			
		{
			IDARECell current = HeaderRow.getCell(i, Row.RETURN_BLANK_AS_NULL);
			Comparable Header = null;							
			if(current == null)
			{
				throw new WrongFormat("No empty column headers allowed in a ValueSet dataset");
			}
			if(current.getCellType() == Cell.CELL_TYPE_STRING)				
			{
				if(current.getStringCellValue().equals("")) 
				{
					throw new WrongFormat("No empty columns allowed in a Graph dataset");				
				}
				stringheaders = true;
				Header = current.getStringCellValue();
			}
			else if(current.getCellType() == Cell.CELL_TYPE_NUMERIC || current.getCellType() == Cell.CELL_TYPE_FORMULA)
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
		PrintFDebugger.Debugging(this,"Finished Reading Headers for sheet " + SheetName);
		String res = "";
		for(Comparable comp : Headers)
		{
			res += comp.toString()+"\t";
		}
		PrintFDebugger.Debugging(this,"The Headers for sheet " + SheetName + " are :\n" + res);
		Headerlines.put(SheetName, Headers);
	}
	/**
	 * Get the Data item corresponding to the ID, or create it using label and ID if it does not exist.
	 * @param ID - the id for which to retrieve a {@link ValueSetNodeData} object
	 * @param label - potentially the label for initialization purposes // will be ignored, if the ID is already present.
	 * @return the {@link ValueSetNodeData} corresponding to the given ID.
	 */
	private ValueSetNodeData getNodeData(String ID, String label)
	{
		if(!Data.containsKey(ID) && ID != null && !ID.equals(""))
		{			
			ValueSetNodeData item = new ValueSetNodeData(this);
			item.setID(ID);
			item.setLabel(label);
			Data.put(ID, item );

		}
		return Data.get(ID);
	}

	/**
	 * generate the ValueDataSetValue for a given Row in a given Sheet.
	 * @param row
	 * @param SheetName
	 * @return
	 */
	private ValueSetDataValue getValues(IDARERow row, String SheetName) throws WrongFormat
	{
		ValueSetDataValue currentNodeValue = new ValueSetDataValue(SheetName);
		int currentCellType = row.getCell(0,Row.CREATE_NULL_AS_BLANK).getCellType();
		String currentID = "";
		String label = "";
		if(currentCellType == Cell.CELL_TYPE_STRING)
		{		
			currentID = row.getCell(0,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
			label = row.getCell(0,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
		}
		else if(currentCellType == Cell.CELL_TYPE_NUMERIC)
		{
			DataFormatter df = new DataFormatter();
			currentID = row.getCell(0,Row.CREATE_NULL_AS_BLANK).getFormattedCellValue();
			label = currentID;
		}
		else if(currentCellType == Cell.CELL_TYPE_BLANK)
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
			currentCellType = row.getCell(1,Row.CREATE_NULL_AS_BLANK).getCellType();
			if(currentCellType == Cell.CELL_TYPE_STRING)
			{		
				
				label = row.getCell(1,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
			}
			else if(currentCellType == Cell.CELL_TYPE_NUMERIC)
			{				
				DataFormatter df = new DataFormatter();
				label = row.getCell(1,Row.CREATE_NULL_AS_BLANK).getFormattedCellValue();						
			}			
			else
			{
				throw new WrongFormat("Could not read headers, only Numeric and String values are allowed for headers");
			}
		}
		ValueSetNodeData currentNodeData = getNodeData(currentID,label);
		if(currentNodeData == null)
		{
			return null;
		}		

		//now, read the remaining line.
		
		int offset = useTwoColHeaders ? 2 : 1;
		Vector<Double> LineValues = new Vector<Double>();
		PrintFDebugger.Debugging(this, "Reading Row for Sheet " + SheetName);
		PrintFDebugger.Debugging(this, "Reading Row with " + (Headerlines.get(SheetName).size()) + " entries" );
		for(int i = offset; i < Headerlines.get(SheetName).size()+offset; i++)
		{
			IDARECell current = row.getCell(i, Row.RETURN_BLANK_AS_NULL);
			if(current == null)
			{
				LineValues.add(null);
			}
			else if(current.getCellType() == Cell.CELL_TYPE_STRING)
			{
				//Check for numeric values as strings and convert them if needed.
				if(StringUtils.isNumeric(current.getStringCellValue()))
				{

					Double cvalue = Double.parseDouble(current.getStringCellValue());
					updateMinMaxVals(cvalue);
					LineValues.add(cvalue);					
				}
				else
				{
					LineValues.add(null);
				}
			}
			else if(current.getCellType() == Cell.CELL_TYPE_NUMERIC || current.getCellType() == Cell.CELL_TYPE_FORMULA)
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
		String Line = "";
		for(Double i : LineValues)
		{
			Line += i + "\t";
		}
		PrintFDebugger.Debugging(this, "Line is : " + Line );

		currentNodeValue.setEntryData(LineValues);
		return currentNodeValue;
	}

	/**
	 * update minimum and maximum values, given a new value.
	 * @param newval
	 */
	private void updateMinMaxVals(Double newval)
	{	
		MinValue = Math.min(MinValue, newval);
		MaxValue = Math.max(MaxValue, newval);
	}

	private void readNumericData(IDAREWorkbook WB, boolean StringData) throws DuplicateIDException,WrongFormat
	{
		MinValue = Double.MAX_VALUE;
		MaxValue = Double.MIN_VALUE;		
		boolean hasentries = false;
		//skip the label row(s)
		int offset = useTwoColHeaders ? 2 : 1;
		for(int i = 0; i < WB.getNumberOfSheets(); i++ )
		{
			IDARESheet DataSheet = WB.getSheetAt(i);			
			Iterator<IDARERow> rowIterator = DataSheet.iterator();
			rowIterator.next();							
			while(rowIterator.hasNext())
			{			
				Vector<NodeValue> rowData = new Vector<>();
				IDARERow row = rowIterator.next();
				if(row.getCell(0) == null)
				{
					//if the 0 cell is null, skip it.
					continue;
				}
				String ID = row.getCell(0).getStringCellValue();				
				ValueSetDataValue currentValue = getValues(row, DataSheet.getSheetName());
				ValueSetNodeData currentNodeData = Data.get(ID);

				//skip, if we have an invalid row.
				if(currentNodeData == null)
				{
					continue;
				}
				hasentries = true;
				currentNodeData.addData(currentValue, DataSheet.getSheetName());
			}

		}
		if(!hasentries)
		{
			throw new WrongFormat("There is no usable data in the Dataset");
		}
	}

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

	@Override
	public void setID(int id) {
		// TODO Auto-generated method stub
		this.dataSetID = id;
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return dataSetID;
	}

	@Override
	public Set<String> getNodeIDs() {
		// TODO Auto-generated method stub
		Set<String> nodeIDs = new HashSet<String>();
		nodeIDs.addAll(Data.keySet());
		return nodeIDs;
	}

	@Override
	public DataContainer getLayoutContainer() {
		// TODO Auto-generated method stub
		return datasetProperties.newContainerInstance(this, Data.values().iterator().next());						
	}

	@Override
	public NodeData getDefaultData() {
		// TODO Auto-generated method stub
		return defaultData;
	}

	@Override
	public Vector<DataSetProperties> getPropertyOptions() {
		Vector<DataSetProperties> props = new Vector<DataSetProperties>();
		props.addAll(propertyOptions);
		return props;
	}

	@Override
	public void setProperties(DataSetProperties properties) {
		// TODO Auto-generated method stub
		this.datasetProperties = properties;
	}

	@Override
	public String getDataSetTypeName() {
		return "Valueset Dataset";
	}

	@Override
	public JPanel getDataSetDescriptionPane(JScrollPane Legend,
			String DataSetLabel, ColorMap map) {
		return datasetProperties.getDataSetDescriptionPane(Legend, DataSetLabel, map, this);
	}
	/**
	 * Get the limits (minimum and maximum value) if this is a numeric dataset. 
	 * @return A Array of two doubles with result[0] being the minimum and results[1] being the maximal value. 
	 */
	public Double[] getYAxisLimits()
	{		
		return new Double[]{MinValue,MaxValue};
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
	 * @param SheetName
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
	public Vector<Comparable> getAllHeaders()
	{
		Vector<Comparable> headervals = new Vector<Comparable>();
		headervals.addAll(allHeaders);
		return headervals;
	}


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


	@Override
	public Vector<ColorMap> getColorMapOptions() {
		// TODO Auto-generated method stub
		Vector<ColorMap> cms = new Vector<ColorMap>();
		cms.addAll(colormaps);
		return cms;
	}

	@Override
	public void setPropertyOptions(Collection<DataSetProperties> options) {
		propertyOptions = new Vector<DataSetProperties>();
		propertyOptions.addAll(options);	
		}	

	private class HeaderPosition implements Comparable<HeaderPosition>
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
