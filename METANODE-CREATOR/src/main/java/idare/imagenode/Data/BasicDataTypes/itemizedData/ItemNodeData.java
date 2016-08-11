package idare.imagenode.Data.BasicDataTypes.itemizedData;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.DataSets.NodeValue;

import java.util.Vector;
/**
 * NodeData that can be used for any itemized Dataset
 * @author Thomas Pfau
 *
 */
public class ItemNodeData extends NodeData {

	private Vector<NodeValue> values;
	/**
	 * Basic constructor using the dataset this is originating from
	 * @param origin
	 */
	public ItemNodeData(DataSet origin)
	{
		super(origin);		
	}
	
	/**
	 * SinceItemized Datasets have ordered rows of data, this function can be used to get the position-th item in this data. 
	 * @param position
	 * @return the {@link NodeValue} stored at the requested position
	 */
	public NodeValue getData(int position) {
		// TODO Auto-generated method stub
		
		return values.get(position);
	}

	/**
	 * Set the Data of this {@link ItemNodeData}. 
	 * @param Data
	 */
	public void setData(Vector<NodeValue> Data) {
		// TODO Auto-generated method stub
		values = Data;
	}

	/**
	 * Return the number of Elements for one set of Data e.g. number of items (including empty items) in an itemized dataset
	 * @return the number of values stored in this {@link ItemNodeData} 
	 */
	public int getValueCount()
	{
		return values.size();
	}
	
	/**
	 * Check whether the element at a specific position is set.
	 * @param pos
	 * @return - true, if the item is not <code>null</code>
	 */
	public boolean isValueSet(int pos)
	{
		return ((ItemDataSet)parent).isColumnSet(pos);
	}

}
