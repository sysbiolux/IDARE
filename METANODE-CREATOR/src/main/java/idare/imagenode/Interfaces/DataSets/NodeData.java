package idare.imagenode.Interfaces.DataSets;

import java.io.Serializable;

import com.thoughtworks.xstream.io.json.JsonWriter.Node;



/**
 * A Data Entry will always only have numbers stored in it as it is used for data representation on color scales or in graphs.
 * @author Thomas Pfau
 *
 */
public abstract class NodeData implements Serializable{
	
	protected String label;
	protected String id;
	protected DataSet parent;
	/**
	 * Generic contstructor using the source DataSet.
	 * @param origin
	 */
	public NodeData(DataSet origin)
	{
		parent = origin;
	}
	
	/**
	 * Get the label of this {@link NodeData} (which can be used for display purposes.
	 * @return the Label (commonly for visualisation purposes) of this {@link NodeData}
	 */
	public String getLabel()
	{
		return label;
	}
	/**
	 * Get the ID of this nodeData.
	 * @return the ID of this NdoeData
	 */
	public String getID()
	{
		return id;
	}
	/**
	 * Get the originating {@link DataSet} this {@link NodeData} belongs to.
	 * @return the {@link DataSet} this NodeData originated from.
	 */
	public DataSet getDataSet()
	{
		return parent;
	}
	/**
	 * Set the Label of this {@link NodeData}
	 * @param id The Label to assign to this {@link NodeData}  
	 */
	public void setLabel(String id)
	{
		this.label = id;
	}
	/**
	 * Set the ID of this {@link NodeData}
	 * @param id The ID to assign to this {@link NodeData}
	 */
	public void setID(String id)
	{
		this.id = id;
	}

	
}
