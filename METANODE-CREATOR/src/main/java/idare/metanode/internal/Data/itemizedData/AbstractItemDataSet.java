package idare.metanode.internal.Data.itemizedData;

import idare.metanode.internal.ColorManagement.ColorMap;
import idare.metanode.internal.ColorManagement.ColorScale;
import idare.metanode.internal.ColorManagement.ColorScaleFactory;
import idare.metanode.internal.ColorManagement.ColorMapTypes.ContinousZeroBalancedMap;
import idare.metanode.internal.ColorManagement.ColorMapTypes.DiscreteColorMap;
import idare.metanode.internal.Data.itemizedData.CircleData.CircleDataSetProperties;
import idare.metanode.internal.Data.itemizedData.CircleGridData.CircleGridProperties;
import idare.metanode.internal.Data.itemizedData.RectangleData.RectangleDataSetProperties;
import idare.metanode.internal.Data.itemizedData.TimeSeriesData.TimeSeriesDataSetProperties;
import idare.metanode.internal.DataSetReaders.IDARECell;
import idare.metanode.internal.DataSetReaders.IDARERow;
import idare.metanode.internal.DataSetReaders.IDARESheet;
import idare.metanode.internal.DataSetReaders.IDAREWorkbook;
import idare.metanode.internal.Debug.PrintFDebugger;
import idare.metanode.internal.GUI.Legend.Utilities.TextPaneResizer;
import idare.metanode.internal.Interfaces.DataContainer;
import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Interfaces.DataSetProperties;
import idare.metanode.internal.Interfaces.NodeData;
import idare.metanode.internal.Interfaces.NodeValue;
import idare.metanode.internal.Utilities.GUIUtils;
import idare.metanode.internal.exceptions.io.DuplicateIDException;
import idare.metanode.internal.exceptions.io.WrongFormat;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
/**
 * An Abstract basis class for item based datasets.
 * The specific layout can be individually adjusted.
 * @author Thomas Pfau
 *
 */
public class AbstractItemDataSet extends DataSet{
	
	public static String DataSetType = "Itemized Dataset";	
	protected HashMap<String,String> NodeLabels= new HashMap<String,String>();
	protected HashMap<String,AbstractItemNodeData> Data = new HashMap<>();
	protected HashMap<Object,Double> DiscreetSet = new HashMap<>();	
	protected AbstractItemNodeData defaultEntry;
	private Vector<DataSetProperties> propertyOptions = new Vector<DataSetProperties>();
	protected Collection<ColorMap> colormaps = new Vector<ColorMap>();

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
	 * @return
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
	public AbstractItemDataSet()
	{
		super(0,true,getDefaultProperties());	
		defaultEntry = new AbstractItemNodeData(this);
		setDefaultOptions();
	}
	/**
	 * Basic constructor using a specific DataSetName
	 * @param DataSetName
	 */
	public AbstractItemDataSet(String DataSetName)
	{
		super(0,true,getDefaultProperties());
		Properties prop = getDefaultProperties();
		prop.setProperty(DATASETDESCRIPTION, DataSetName);
		setProperties(prop);
		defaultEntry = new AbstractItemNodeData(this);
		setDefaultOptions();
	}
	
	/**
	 * Constructor using an ID, indication for twoColumn use in the datafile, and a properties collection
	 * @param DataSetID
	 * @param useTwoCols
	 * @param properties
	 */
	public AbstractItemDataSet(int DataSetID,boolean useTwoCols,Properties properties) {
		super(DataSetID,useTwoCols,properties);
		defaultEntry = new AbstractItemNodeData(this);
		setDefaultOptions();
	}

	
			
	/**
	 * Read the names of the Datapoints (i.e. the header line in the source data.
	 * @param labelIterator
	 */
	private void readDataPointNames(Iterator<IDARECell> labelIterator)
	{

		int offset = 1;
		if(useTwoColHeaders)
		{
			offset++;
		}
		while (labelIterator.hasNext())
		{
			IDARECell cell = labelIterator.next();
			int position = cell.getColumnIndex() - offset;
			while(position > ColumnLabels.size())
			{
				//fill empty Headers with null.
				ColumnLabels.add(null);
			}
			//Check the cell type and format accordingly
			if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
			{
				ColumnLabels.add(Double.toString(cell.getNumericCellValue()));
			}
			else if(cell.getCellType() == Cell.CELL_TYPE_STRING)
			{
				ColumnLabels.add(cell.getStringCellValue());
			}
			else
			{
				// We can't read anything but strings and numeric values.
				ColumnLabels.add(null);
			}
		}
	}

	@Override 
	protected void reset()
	{
		NodeLabels= new HashMap<String,String>();
		Data = new HashMap<>();
		DiscreetSet = new HashMap<>();	
		defaultEntry = new AbstractItemNodeData(this);
	}
	
	@Override
	public void readWorkBookData(IDAREWorkbook WB) throws WrongFormat,DuplicateIDException
	{
		//We should clear the Data and everything;
		reset();
		IDARESheet DataSheet = WB.getSheetAt(0);
		Iterator<IDARERow> rowIterator = DataSheet.iterator();
		//Read the first row, which are the headers (and labels)
		IDARERow labelRow = rowIterator.next();
		Iterator<IDARECell> labelIterator = labelRow.cellIterator();
		//skip the first row.
		skipLabels(labelIterator);
		readDataPointNames(labelIterator);	

		//read the data with the appropriate settings.
		if(isnumeric)
		{			
			if(numericstrings)
			{
				readNumericData(WB,true);
			}
			else
			{
				readNumericData(WB,false);
			}
		}
		else
		{
			readStringData(WB);
		}
		//If the dataset is discrete, we can actually use multiple additional Colors.
		if(isdiscreet)
		{
			Collection<ColorScale> scales = ColorScaleFactory.getDiscreetColorScales(Valueset.size());
			for(ColorScale scale : scales)
			{
				scale.setColorCount(Valueset.size());
				colormaps.add(new DiscreteColorMap(Valueset, scale));
			}		
			 scales = ColorScaleFactory.getContinousColorScales();
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
		Vector<NodeValue> DefaultValues = new Vector<NodeValue>();
		for(int i = 0; i < columncount; i++)
		{
			DefaultValues.add(new NodeValue(true));
		}
		//add some values to the default entry
		defaultEntry.setData(DefaultValues);
	}
	
	/**
	 * Read String data
	 * @param WB - the workbook to read the data from.
	 * @throws WrongFormat
	 * @throws DuplicateIDException
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
		
		while(rowIterator.hasNext())
		{							
			Vector<NodeValue> rowData = new Vector<>();
			IDARERow row = rowIterator.next();
			AbstractItemNodeData currentData = getNodeData(row);
			if(currentData == null)
			{
				continue;
			}
			for(int i = 0; i < columncount; i++)
			{
				IDARECell currentCell = row.getCell(i+offset,Row.RETURN_BLANK_AS_NULL);
				if(currentCell == null)
				{
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
			throw new WrongFormat("There is no usable data in the Dataset");
		}
		if(Stringvalues.size() > 6)
		{
			throw new WrongFormat("Too many different values for String based data!");
		}
		Vector<String> sortedStrings = new Vector<String>();
		
		sortedStrings.addAll(Stringvalues);
		Collections.sort(sortedStrings);
		int Representative = 1;
		for(String value : sortedStrings)
		{
			DiscreetSet.put(value, new Double(Representative));
			Representative++;
		}
		
	}
	/**
	 * Read numeric data, and indicate whether this si stringdata (i.e. numbers that wer fgormatted to be strings)
	 * @param WB
	 * @param StringData
	 * @throws DuplicateIDException
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
		int currentrow = 1;
		while(rowIterator.hasNext())
		{
			PrintFDebugger.Debugging(this, "Reading Data for row " + currentrow);
			currentrow++;
					
			Vector<NodeValue> rowData = new Vector<>();
			IDARERow row = rowIterator.next();
			AbstractItemNodeData currentData = getNodeData(row);
			if(currentData == null)
			{
				continue;
			}
			for(int i = 0; i < columncount; i++)
			{
				IDARECell currentCell = row.getCell(i+offset,Row.RETURN_BLANK_AS_NULL);
				if(currentCell == null)
				{
					rowData.add(new NodeValue(true));
					continue;
				}
				else
				{
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
			throw new WrongFormat("There is no usable data in the Dataset");
		}
	}

	/**
	 * Read the data for a single Row
	 * @param currentRow
	 * @return an AbstractItemNodeData representing the Row.
	 */
	private AbstractItemNodeData getNodeData(IDARERow currentRow) throws WrongFormat
	{
		AbstractItemNodeData currentNodeData = new AbstractItemNodeData(this);
		//System.out.println(currentRow.toString());
		int currentCellType = currentRow.getCell(0,Row.CREATE_NULL_AS_BLANK).getCellType(); 
		if(currentCellType == Cell.CELL_TYPE_STRING)
		{		
			currentNodeData.setID(currentRow.getCell(0,Row.CREATE_NULL_AS_BLANK).getStringCellValue());
			currentNodeData.setLabel(currentRow.getCell(0,Row.CREATE_NULL_AS_BLANK).getStringCellValue());
		}
		else if(currentCellType == Cell.CELL_TYPE_NUMERIC)
		{
			//This is dangerous, as IDs can be screwed up...
			currentNodeData.setID(currentRow.getCell(0,Row.CREATE_NULL_AS_BLANK).getFormattedCellValue());
			currentNodeData.setLabel(currentRow.getCell(0,Row.CREATE_NULL_AS_BLANK).getFormattedCellValue());
		}
		else if(currentCellType == Cell.CELL_TYPE_BLANK)
		{
			//No ID.
			return null;
		}
		else
		{
			throw new WrongFormat("Could not read headers, only Numeric and String values are allowed for headers");
		}
		PrintFDebugger.Debugging(this, "The current label is : " + currentNodeData.getLabel() + " while the ID is " + currentNodeData.getID()	);
		
		if(useTwoColHeaders)
		{
			currentCellType = currentRow.getCell(1,Row.CREATE_NULL_AS_BLANK).getCellType();
			if(currentCellType == Cell.CELL_TYPE_STRING)
			{		
				currentNodeData.setLabel(currentRow.getCell(1,Row.CREATE_NULL_AS_BLANK).getStringCellValue());
			}
			else if(currentCellType == Cell.CELL_TYPE_NUMERIC)
			{
				DataFormatter df = new DataFormatter();
				currentNodeData.setLabel(currentRow.getCell(1,Row.CREATE_NULL_AS_BLANK).getFormattedCellValue());
			}
			else if(currentCellType == Cell.CELL_TYPE_BLANK)
			{
				//This is not nice... but we will allow a blank label.
				currentNodeData.setLabel("");
			}
			else
			{
				throw new WrongFormat("Could not read headers, only Numeric and String values are allowed for headers");
			}			
		}
		
		PrintFDebugger.Debugging(this, "The label after twoheadercols is : " + currentNodeData.getLabel() + " while the ID is " + currentNodeData.getID()	);
		if(currentNodeData.getID().equals(""))
		{
			//A Node without ID should not be generated
			return null;
		}
		NodeLabels.put(currentNodeData.getID(), currentNodeData.getLabel());
		return currentNodeData;
	}

	/**
	 * Determines whether this is a numeric or a string dataset and sets up the Entry details.
	 * This will only check whether this is a string or a numeric dataset
	 * and set the property accordingly
	 * @param WB
	 */
	private boolean setupDataSet(Workbook WB)
	{		
		Sheet DataSheet = WB.getSheetAt(0);
		Iterator<Row> rowIterator = DataSheet.iterator();
		//skip the label row
		rowIterator.next();
		boolean uninitialized = true;
		numericstrings = false;
		boolean isString = false;
		while(rowIterator.hasNext() && uninitialized)
		{
			Iterator<Cell> cellIterator = rowIterator.next().iterator();
			//Skip the first (and potentially second column)
			cellIterator.next();
			if(useTwoColHeaders)
				cellIterator.next();
			while(cellIterator.hasNext())
			{
				Cell currentcell = cellIterator.next();
				if(currentcell.getCellType() == Cell.CELL_TYPE_STRING)
				{
					if(currentcell.getStringCellValue().equals("") || currentcell.getStringCellValue().trim().equals(""))
					{
						//ignore empty cells
						continue;
					}
					else
					{
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
							continue;
						}
						//This is a plain String! so
						isString = true;
						isnumeric = false;
						uninitialized = false;
						break;
					}

				}
				if(currentcell.getCellType() == Cell.CELL_TYPE_NUMERIC)
				{
					isnumeric = true;
					uninitialized = false;
					break;
				}
			}
		}


		return true;
	}

	
	@Override
	public void setTwoColumnHeaders(boolean twocols) {
		// TODO Auto-generated method stub
		useTwoColHeaders = twocols;
	}
		
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
		for(String collabel : ColumnLabels)
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
			AbstractItemNodeData data = Data.get(ID);
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

	@Override
	public NodeData getDefaultData()
	{
		return defaultEntry;
	}
	
	@Override
	public DataContainer getContainerForID(String ID) {		
		return datasetProperties.newContainerForData(Data.get(ID));
	}
	
	@Override
	public DataContainer getLayoutContainer()
	{
		return datasetProperties.newContainerInstance(this, Data.values().iterator().next()); 
	}
	@Override
	public Vector<DataSetProperties> getPropertyOptions() {
		return propertyOptions;
	}
	@Override
	public void setProperties(DataSetProperties properties) {
		// TODO Auto-generated method stub
		datasetProperties = properties;
	}
	@Override
	public String getDataSetTypeName() {
		// TODO Auto-generated method stub
		return DataSetType;
	}
	@Override
	public JPanel getDataSetDescriptionPane(JScrollPane Legend, String DataSetLabel, ColorMap map)
	{
		
		return datasetProperties.getDataSetDescriptionPane(Legend, DataSetLabel, map, this);
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
	
	/**
	 * Check whether a specific column is set or empty.
	 * @param column
	 * @return whether the given column has values
	 */
	public boolean isColumnSet(int column)
	{
		return !emptycolumns[column]; 
	}
	
	/**
	 * Get the Label for a specific Column in the data, or null if it does not exist, or there sis no clear label.
	 * @param column
	 * @return the label for the requested Column
	 */
	public String getColumnLabel(int column)
	{
		return ColumnLabels.get(column);
	}
}

