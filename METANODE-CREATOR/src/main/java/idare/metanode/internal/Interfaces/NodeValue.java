package idare.metanode.internal.Interfaces;

import idare.metanode.internal.Properties.NodeValueType;
/**
 * A NodeValue represents the Value at a specific node. 
 * This is mainly for plotting purposes and different {@link DataSet}s can use them in different ways.
 * @author Thomas Pfau
 *
 */
public class NodeValue implements Comparable<NodeValue>{

	protected Comparable value = null;
	protected NodeValueType type;

	/**
	 * Create a {@link NodeValue} using a Double value.
	 * @param value
	 */
	public NodeValue(Double value)
	{
		this.value = value;
		type = NodeValueType.numeric;
	}
	/**
	 * Create a NodeValue indicating whether it is a numeric or non numeric value.
	 * @param isnumeric
	 */
	public NodeValue(boolean isnumeric)
	{
		this.value = null;
		if(isnumeric)
		{
			type = NodeValueType.numeric;	
		}
		else
		{
			type = NodeValueType.string;
		}
	}

	/**
	 * Create a NodeValue as a String.
	 * @param value
	 */
	public NodeValue(String value)
	{
		this.value = value;
		type = NodeValueType.string;
	}
	/**
	 * Get the Type of NodeValue (numeric, String, Vector)
	 * @return the type of the {@link NodeValue}
	 */
	public NodeValueType getType()
	{
		return type;
	}
	/**
	 * Get the value of this {@link NodeValue}
	 * @return A Comparable representing the {@link NodeValue}
	 */
	public Comparable getValue()
	{
		return value;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(NodeValue o) {
		//first, check if o is null;
		if(o == null)
		{
			//everything is larger than null
			return 1;
		}
		//then check if this value is null (and if it is whether the value of o is null in which case they are the "the same")
		// otherwise o is larger!
		if(this.value == null)
		{
			if(o.value == null)
			{
				return 0;
			}
			else
			{
				return -1;
			}
		}
		//now, we know that this value is not null, so if o has a null value we can be sure, that this is larger.
		if(o.value == null)
		{
			return 1;
		}
		if(o.type == this.type)
		{
			//this should fit, as we have the same value type!			
			return this.value.compareTo(o.value);
		}
		else
		{
			if(this.type == NodeValueType.string)
			{
				return -1;
			}
			else
			{
				if(o.type == NodeValueType.string ){
					return 1;
				}
				else
				{
					if(this.type == NodeValueType.numeric)
					{
						return 1;						
					}
					else
					{
						return -1;
					}
				}
			}
		}
		// TODO Auto-generated method stub		
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		try{
			NodeValue compareNode = (NodeValue)o;
			if(this.compareTo(compareNode) == 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(ClassCastException cce)
		{
			return false;
		}
	}
	/**
	 * This class is only a representation of its value, so its hash is the same as its value. or 0 if its null
	 */

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		
		return value == null ? 0 : value.hashCode();
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "" + value;
	}
}
