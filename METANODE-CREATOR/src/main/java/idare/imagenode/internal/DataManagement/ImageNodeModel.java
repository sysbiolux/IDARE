package idare.imagenode.internal.DataManagement;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;

import java.util.Collection;
import java.util.Vector;

/**
 * A Class storing which Datasets are associated with a specific StringID.
 * @author Thomas Pfau
 *
 */
public class ImageNodeModel {

	String ID;
	private Vector<DataSet> nodeDataEntries;
	/**
	 * Default constructor using the ID of this Node
	 * @param ID
	 */
	public ImageNodeModel(String ID)
	{
		this.ID = ID;
		nodeDataEntries = new Vector<>();
	}
	/**
	 * Remove a {@link DataSet} from this node 
	 * @param oldData the dataset to remove
	 */
	public void removeData(DataSet oldData)
	{		
		nodeDataEntries.remove(oldData);
	}
	/**
	 * Add a {@link DataSet} to this node.
	 * @param newData the DataSet to add to the node
	 */
	public void addData(DataSet newData)
	{
		nodeDataEntries.add(newData);
	}
	
	/**
	 * Get all {@link NodeData} associated with this Node.
	 * @return the Collection of all {@link NodeData} associated with this imagenode
	 */
	public Collection<NodeData> getData()
	{
		Vector<NodeData> data = new Vector<NodeData>();
		for(DataSet ds : nodeDataEntries)
		{
			data.add(ds.getDataForID(ID));
		}
		return data;
	}
	/**
	 * Check whether this node still has associated DataSets.
	 * @return whether there is Data in the imagenode.
	 */
	public boolean isValid()
	{
		return nodeDataEntries.size() > 0;
	}
}
