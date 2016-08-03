package idare.metanode.internal.GUI.DataSetController;

import idare.ThirdParty.BoundsPopupMenuListener;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.Layout.DataSetProperties;
import idare.metanode.internal.ColorManagement.ColorMap;
import idare.metanode.internal.ColorManagement.ColorScalePane;
import idare.metanode.internal.DataManagement.DataSetManager;
import idare.metanode.internal.DataManagement.Events.DataSetChangeListener;
import idare.metanode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.metanode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.metanode.internal.Debug.PrintFDebugger;
import idare.metanode.internal.Layout.ColorMapDataSetBundle;
import idare.metanode.internal.Utilities.ColorScalePopupAdjuster;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * The DataSetSelectionModel provides specific renderers and Editors for the items in the current {@link DataSetSelectionTable}. 
 * @author Thomas Pfau
 *
 */
public class DataSetSelectionModel extends DefaultTableModel implements DataSetChangeListener{

	private HashMap<DataSet,ComboBoxRenderer> renderers = new HashMap<DataSet, DataSetSelectionModel.ComboBoxRenderer>();
	private HashMap<DataSet,ColorBoxRenderer> colorscalerenderer = new HashMap<DataSet, ColorBoxRenderer>();
	private HashMap<DataSet,ColorPaneBox> colorscaleBoxes = new HashMap<DataSet, ColorPaneBox>();
	private HashMap<DataSet,Vector<ColorScalePane>> colorscalesperdataset = new HashMap<DataSet, Vector<ColorScalePane>>();
	private HashMap<ColorScalePane,ColorMap> colorscaleselection = new HashMap<ColorScalePane, ColorMap>();
	public static String[] Column_Identifiers = new String[] {"Dataset Description", "Nodes", "Selected","Colors", "Visualisation Type"};	
	
	
	/**
	 * Create a new {@link DataSetSelectionModel} by providing the {@link DataSetManager}, which is used to build the model on. 
	 * The {@link DataSetManager} will initially be querried for all data of its {@link DataSet}s.
	 * @param dsm 
	 */
	public DataSetSelectionModel(DataSetManager dsm) {
		super();
		//Add this model as a listener to DataSetChanges. 
		dsm.addDataSetChangeListener(this);
		Collection<DataSet> dataSets = dsm.getDataSets();
		setColumnIdentifiers(Column_Identifiers);
		//generate one row per Dataset.
		for(DataSet ds: dataSets)
		{
			datasetAdded(ds);
		}		

	}
	/**
	 * Set up the ColorPaneSelection. This creates the renderers and editors necessary for this field in the Table.
	 * @param panes
	 * @param ds
	 */
	private void setupColorPaneSelection(Vector<ColorScalePane > panes, DataSet ds)
	{
		ColorPaneBox box = new ColorPaneBox(panes);		
		colorscaleBoxes.put(ds, box);
		ColorBoxRenderer renderer = new ColorBoxRenderer();
		box.setRenderer(renderer);
		BasicComboPopup popup = ((BasicComboPopup)box.getUI().getAccessibleChild(box,0));
        popup.getList().setFixedCellHeight(30);
        popup.getList().setVisibleRowCount(panes.size());
		popup.getList().setFixedCellHeight(30*panes.size());		
        JScrollPane c = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, popup.getList());
		c.setPreferredSize(popup.getList().getPreferredSize());
        ColorScalePopupAdjuster cpa = new ColorScalePopupAdjuster(panes.size());
        box.addPopupMenuListener(cpa);	        
        colorscalerenderer.put(ds, renderer);                
	}
	
	/**
	 * 
	 */
	public void updateDataSetProperties(DataSet ds)
	{
		
		int dsrow = getRowByDataSet(ds);
		setValueAt(ds.getNodeIDs().size(), dsrow, 1);
		
		setValueAt(ds.getNodeIDs().size(), dsrow, 1);
		setValueAt(ds.getNodeIDs().size(), dsrow, 1);
		Vector<ColorMap> cmoptions = ds.getColorMapOptions();
		Vector<ColorScalePane> panes = new Vector<ColorScalePane>();		
		for(ColorMap map : cmoptions)
		{
			ColorScalePane pane = map.getColorScalePane(); 
			panes.add(pane);
			colorscaleselection.put(pane, map);
		}		
		colorscalesperdataset.put(ds, panes);
		setupColorPaneSelection(panes, ds);			
		ComboBoxRenderer box = new ComboBoxRenderer(ds.getPropertyOptions());
		box.addPopupMenuListener(new BoundsPopupMenuListener(true,false));
		renderers.put(ds, box);
		setValueAt(ds.getPropertyOptions().get(0),dsrow,4);
	}
	
	@Override
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		//The Data retrieved from the Model can not be altered. only the remainder can.
		if (col == 1) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * Get the selected {@link DataSet}s along with the currently used {@link ColorMap}s 
	 * @return A vector of all selected {@link DataSet}s and their chosen {@link ColorMap}s in a {@link ColorMapDataSetBundle} 
	 */
	public Vector<ColorMapDataSetBundle> getSelectedDataSets()
	{
		Vector<ColorMapDataSetBundle> rv = new Vector<ColorMapDataSetBundle>();
		for(int i = 0; i < this.getRowCount(); i++)
		{
			if((Boolean) getValueAt(i, 2))
			{
				DataSet ds = (DataSet) getValueAt(i, 0);
				ColorMap cm = colorscaleselection.get((ColorScalePane) getValueAt(i,3));
				ColorMapDataSetBundle bundle = new ColorMapDataSetBundle();
				bundle.dataset = ds;
				bundle.map = cm;
				PrintFDebugger.Debugging(this, "Adding Dataset: " + ds.Description + " to the selected Datasets.");
				rv.add(bundle);
			}

		}
		return rv;
	}

	/**
	 * Set the properties of the {@link DataSet}s according to the selected values. 
	 */
	public void setDataSetProperties()
	{
		for(int i = 0; i < getRowCount(); i++)
		{
			DataSet ds = (DataSet) getValueAt(i, 0);					
			ds.setProperties((DataSetProperties)getValueAt(i,4));
		}
	}

	/**
	 * Move a {@link DataSet} up in the table (supplying the row of the dataset to move up
	 * @param row
	 */
	public void moveRowUp(int row)
	{
		//since the position is the 1 based (seemingly) we need to move it like this...
		moveRow(row, row, row-1);
	}
	/**
	 * Move a {@link DataSet} down in the table (supplying the row of the dataset to move down
	 * @param row
	 */
	public void moveRowDown(int row)
	{
		//since the position is the 1 based (seemingly) we need to move ti like this...
		moveRow(row, row, row+1);
	}
	/**
	 * Get the renderer for the properties in the specified row.
	 * @param row
	 * @return a ComboBoxRenderer for the appropriate properties
	 */
	public ComboBoxRenderer getPropertiesRenderer(int row)
	{
		return renderers.get(getValueAt(row, 0));
	}
	
	/**
	 * Get the renderer for the {@link ColorScalePane} in the specified row.
	 * @param row
	 * @return a ColorBoxRenderer for the appropriate {@link ColorScalePane}
	 */
	public ColorBoxRenderer getColorScaleRenderer(int row)
	{
		return colorscalerenderer.get(getValueAt(row, 0));
	}
	/**
	 * Get the Editor for the appropriate {@link ColorScalePane} renderer.
	 * @param row
	 * @return The {@link DefaultCellEditor} for the {@link ColorScalePane} in the requested row
	 */
	public DefaultCellEditor getColorScaleEditor(int row)
	{
		return new DefaultCellEditor(colorscaleBoxes.get(getValueAt(row, 0)));
	}
	
	/**
	 * Get the Editor for the appropriate {@link DataSet} renderer.
	 * @param row
	 * @return the {@link TableCellEditor} for the {@link DataSet} represented by the requested row
	 */
	public TableCellEditor getDataSetEditor(int row)
	{
		return new DataSetNameEditor((DataSet)getValueAt(row, 0));
	}
	
	/**
	 * Get the Editor for the appropriate {@link DataSetProperties} renderer.
	 * @param row
	 * @return get the {@link DefaultCellEditor} for the appropriate {@link DataSetProperties}
	 */
	public DefaultCellEditor getPropertiesEditor(int row)
	{
		return new DefaultCellEditor(renderers.get(getValueAt(row, 0)));
	}
	/**
	 * A Class to Render and adjust the DataSetProperties
	 * @author Thomas Pfau
	 */
	class ComboBoxRenderer extends JComboBox<DataSetProperties> implements TableCellRenderer {
		public ComboBoxRenderer(Vector items) {
			super(items);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			setSelectedItem(value);			
			return this;
		}
	}
		
	
	/**
	 * A JComboBox specific to ColorScale Panes
	 * @author Thomas Pfau
	 */
	class ColorPaneBox extends JComboBox<ColorScalePane> {
		public ColorPaneBox(Vector items) {
			super(items);
		}
	}
	/**
	 * A Renderer for Boxes with {@link ColorScalePane}s. Takes care both of Cell rendering and Popup rendering. 
	 * @author Thomas Pfau
	 *
	 */
	class ColorBoxRenderer extends JPanel
	implements ListCellRenderer,TableCellRenderer{
		public ColorBoxRenderer() {
			setOpaque(true);
			setBorder(null);
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		}

		/*
		 * This method finds the image and text corresponding
		 * to the selected value and returns the label, set up
		 * to display the text and image.
		 */
		public Component getListCellRendererComponent(
				JList list,				
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus) {
			//Get the selected index. (The index param isn't
			//always valid, so just use the value.)		
			JPanel pane = new JPanel();
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			//CostumPanel.setPreferredSize(new Dimension(0,20));
			return (ColorScalePane)value;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {			
			return (ColorScalePane)value;
		}
	}


	@Override 
	public void datasetChanged(DataSetChangedEvent e)
	{
		if(e.wasAdded())
		{
			datasetAdded(e.getSet());
		}
		if(e.wasRemoved())
		{
			datasetRemoved(e.getSet());
		}
		if(e.wasChanged())
		{
			updateDataSetProperties(e.getSet());		
		}
	}

	@Override 
	public void datasetsChanged(DataSetsChangedEvent e)
	{
		if(e.wasAdded())
		{
			//in case we need to add multiple sets, we simply add one set after another.
			for(DataSet ds : e.getSet())
			datasetAdded(ds);
		}
		if(e.wasRemoved())
		{
			dataSetsRemoved(e.getSet());
		}
		if(e.wasChanged())
		{
			for(DataSet ds : e.getSet())
			{
				updateDataSetProperties(ds);
			}
		}
	}
	
	/**
	 * Add a DataSet to this model. 
	 * @param ds
	 */
	private void datasetAdded(DataSet ds) {
		// TODO Auto-generated method stub
		Vector<Object> row = new Vector<Object>();
		row.add(ds);
		row.add(ds.getNodeIDs().size());
		row.add(false);
		Vector<ColorMap> cmoptions = ds.getColorMapOptions();
		Vector<ColorScalePane> panes = new Vector<ColorScalePane>();
		for(ColorMap map : cmoptions)
		{
			ColorScalePane pane = map.getColorScalePane(); 
			panes.add(pane);
			colorscaleselection.put(pane, map);
		}
		
		colorscalesperdataset.put(ds, panes);
		setupColorPaneSelection(panes, ds);
		row.add(panes.get(0));		
		ComboBoxRenderer box = new ComboBoxRenderer(ds.getPropertyOptions());
		box.addPopupMenuListener(new BoundsPopupMenuListener(true,false));
		renderers.put(ds, box);
		row.add(ds.getPropertyOptions().get(0));
		addRow(row);
	}
	/**
	 * Remove a {@link DataSet} from this model.
	 * @param ds
	 */
	private void datasetRemoved(DataSet ds) {
		// TODO Auto-generated method stub
		int rowindex = getRowByDataSet(ds);
		if(rowindex >=0)
		{
			removeRow(rowindex);
			renderers.remove(ds);
			colorscalerenderer.remove(ds);
			for(ColorScalePane pane : 	colorscalesperdataset.get(ds))
			{
				colorscaleselection.remove(pane);
			}
			colorscalesperdataset.remove(ds);
		}
	}
	/**
	 * Remove multiple DataSets from this model.
	 * @param dss
	 */
	private void dataSetsRemoved(Collection<DataSet> dss) {
		// TODO Auto-generated method stub
		for(DataSet ds : dss)
		{
			int rowindex = getRowByDataSet(ds);
			if(rowindex >=0)
			{
				removeRow(rowindex);
				renderers.remove(ds);
				colorscalerenderer.remove(ds);
				for(ColorScalePane pane : 	colorscalesperdataset.get(ds))
				{
					colorscaleselection.remove(pane);
				}
				colorscalesperdataset.remove(ds);
			}
		}
	}
	/**
	 * Determine the row that fits to provided Object.
	 * @param value - The object to look for in the first column of this model 
	 * @return the row number containing the provided object in the first column. If the provided object is <code>null</code> or not present -1 will be returned 
	 */
	private int getRowByDataSet(Object value) {
		if(value == null)
		{
			return -1;
		}
	    for (int i = getRowCount() - 1; i >= 0; --i) {
	        if (getValueAt(i, 0).equals(value)) {
	                return i;
	        }
	    }
	    return -1;
	 }
	/**
	 * An Editor that retrieves and sets the Description of the dataset represented by it. 
	 * @author Thomas Pfau
	 *
	 */
	private class DataSetNameEditor extends AbstractCellEditor implements TableCellEditor
	{
		DataSet ds;
		private JTextField field = new JTextField();
		public DataSetNameEditor(DataSet ds)
		{			
			super();
			field.setText(ds.Description);
			field.setBorder(null);		
			this.ds = ds;
		}
		@Override
		public Object getCellEditorValue() {
			ds.Description = field.getText();								
			return ds;
		}
		@Override
		public Component getTableCellEditorComponent(JTable arg0, Object arg1,
				boolean arg2, int arg3, int arg4) {
			
			return field;
		}
	}
}
