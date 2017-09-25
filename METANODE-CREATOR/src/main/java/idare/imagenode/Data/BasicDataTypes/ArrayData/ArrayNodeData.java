package idare.imagenode.Data.BasicDataTypes.ArrayData;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.DataSets.NodeValue;

import java.util.Vector;
/**
 * NodeData that can be used for any {@link ArrayDataSet}
 * @author Thomas Pfau
 *
 */
public class ArrayNodeData extends NodeData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<NodeValue> values;
	/**
	 * Basic constructor using the dataset this is originating from
	 * @param origin The {@link DataSet} this {@link ArrayNodeData} belongs to,
	 */
	public ArrayNodeData(DataSet origin)
	{
		super(origin);		
	}
	
	/**
	 * Since {@link ArrayDataSet}s have ordered rows of data, this function can be used to get the position-th item in this data. 
	 * @param position the position of the {@link NodeValue} requested.
	 * @return the {@link NodeValue} stored at the requested position
	 */
	public NodeValue getData(int position) {
		// TODO Auto-generated method stub
		
		return values.get(position);
	}

	/**
	 * Set the Data of this {@link ArrayNodeData}. 
	 * @param Data The data To set.
	 */
	public void setData(Vector<NodeValue> Data) {
		// TODO Auto-generated method stub
		values = Data;
	}

	/**
	 * Return the number of Elements for one set of Data e.g. number of items (including empty items) in an {@link ArrayDataSet}
	 * @return the number of values stored in this {@link ArrayNodeData} 
	 */
	public int getValueCount()
	{
		return values.size();
	}
	
	/**
	 * Check whether the element at a specific position is set.
	 * @param pos the position to check
	 * @return true, if the item is not <code>null</code>
	 */
	public boolean isValueSet(int pos)
	{
		return ((ArrayDataSet)parent).isColumnSet(pos);
	}

	@Override
	public boolean isempty() {
		for(NodeValue val : values)
		{
			if(val != null && val.getValue() != null)
			{
				return false;
			}
		}
		return true;
	}

}
