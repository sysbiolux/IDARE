package idare.metanode.internal.Data.ValueSetData;

import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Interfaces.NodeData;
import idare.metanode.internal.exceptions.io.DuplicateIDException;

import java.util.HashMap;

public class ValueSetNodeData extends NodeData {

	private HashMap<String,ValueSetDataValue> Data;
		
	/**
	 * Basic constructor providing a source DataSet
	 * @param origin
	 */
	public ValueSetNodeData(DataSet origin) {
		super(origin);
		// TODO Auto-generated constructor stub
		Data = new HashMap<String, ValueSetDataValue>();
	}	
	/**
	 * Add a new {@link ValueSetDataValue} to this NodeData, for a specific SetID
	 * @param newset - the NodeData
	 * @param VectorSetID - the SetID (Sheet Name) 
	 * @throws DuplicateIDException
	 */
	public void addData(ValueSetDataValue newset,String VectorSetID) throws DuplicateIDException
	{
		if(Data.containsKey(VectorSetID)) throw new DuplicateIDException(this.id,"Duplicate ID in sheet " + VectorSetID);				
		Data.put(VectorSetID,newset);
	}
	/**
	 * Get the Data stored for a specific Sheetname, returns null if this sheetname does have no associated data.
	 * @param SheetName -  the requested sheetname
	 * @return A {@link ValueSetDataValue} for the requested SheetName
	 */
	public ValueSetDataValue getData(String SheetName)
	{
		return Data.get(SheetName);
	}
	
		

}
