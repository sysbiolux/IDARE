package idare.imagenode.internal.GUI.DataSetController;

import idare.imagenode.ColorManagement.ColorScalePane;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.internal.GUI.DataSetController.DataSetSelectionModel.ColorPaneBox;
import idare.imagenode.internal.GUI.DataSetController.DataSetSelectionModel.ComboBoxRenderer;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * A DataSetSelectionTable, that is specific to the {@link DataSetSelectionModel}, using its unique renderers and editors.
 * @author Thomas Pfau
 *
 */
public class DataSetSelectionTable extends JTable {

	DataSetSelectionModel tablemodel;
	public DataSetSelectionTable(DataSetSelectionModel mod)
	{
		super(mod);
		tablemodel = mod;
	}
	/**
	 * Move the selected entry (we assume single selection) down one row.
	 * If the selected row is already the top row, nothing happens.
	 */
	public void moveEntryUp()
	{
		int row = getSelectedRow();
		if(row > 0)
		{
			tablemodel.moveRowUp(row);
		}
		getSelectionModel().setSelectionInterval(row-1, row-1);
	}
	/**
	 * Move the selected entry (we assume single selection) down one row.
	 * If the selected row is already the last row, nothing happens.
	 */
	public void moveEntryDown()
	{
		int row = getSelectedRow();
		if(row >= 0 & row < getRowCount()-1 )
		{
			tablemodel.moveRowDown(row);
		}
		getSelectionModel().setSelectionInterval(row+1, row+1);
	}
	
	@Override
	public TableCellEditor getCellEditor(int row, int column) {
	   Object value = super.getValueAt(row, column);
	   if(value != null) {
		   // we need very specific Editors for ColorScales and Dataset Properties.
	      if(value instanceof DataSetLayoutProperties) {
	           return tablemodel.getPropertiesEditor(row);
	      }
	      if(value instanceof ColorScalePane)
	      {
	    	  return tablemodel.getColorScaleEditor(row);
	      }
	      if(value instanceof DataSet)
	      {
	    	  return tablemodel.getDataSetEditor(row);
	      }
	            return getDefaultEditor(value.getClass());
	   }
	   return super.getCellEditor(row, column);
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
	   Object value = super.getValueAt(row, column);
	   
	   if(value != null) {
		   // we need very specific renderers for ColorScales and Dataset Properties.

	      if(value instanceof ComboBoxRenderer || value instanceof DataSetLayoutProperties) {
	    	  TableCellRenderer current = tablemodel.getPropertiesRenderer(row);
	    	  if(current != null)
	    		  return current;
	    	  else
	    		  return super.getCellRenderer(row, column);
	      }
	      if(value instanceof ColorPaneBox|| value instanceof ColorScalePane) {
	    	  TableCellRenderer current = tablemodel.getColorScaleRenderer(row);
	    	  if(current != null)
	    		  return current;
	    	  else
	    		  return super.getCellRenderer(row, column);
	      }
	      
	            return getDefaultRenderer(value.getClass());
	   }
	   return super.getCellRenderer(row, column);
	}	
}