package idare.imagenode.Data.BasicDataTypes.ValueSetData;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.internal.exceptions.io.DuplicateIDException;

import java.util.Collection;
import java.util.HashMap;

/**
 * {@link NodeData} for {@link ValueSetDataSet}s 
 * @author Thomas Pfau
 *
 */
public class ValueSetNodeData extends NodeData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
	 * @param newset the NodeData
	 * @param VectorSetID the SetID (Sheet Name) 
	 * @throws DuplicateIDException
	 */
	public void addData(ValueSetDataValue newset,String VectorSetID) throws DuplicateIDException
	{
		if(Data.containsKey(VectorSetID)) throw new DuplicateIDException(this.id,"Duplicate ID in sheet " + VectorSetID);
		System.out.println("Setting Data for ID" + VectorSetID + " to a Vector with size " + newset.getEntryData().size());
		Data.put(VectorSetID,newset);
	}
	/**
	 * Get the Data stored for a specific Sheetname, returns null if this sheetname does have no associated data.
	 * @param SheetName  the requested sheetname
	 * @return A {@link ValueSetDataValue} for the requested SheetName
	 */
	public ValueSetDataValue getData(String SheetName)
	{
		System.out.println("Getting value for " + SheetName + " which is " + Data.get(SheetName));
		
		return Data.get(SheetName);
	}
	
	public void printSheetsContained()
	{
		System.out.println("ID : " + id);
		for(String sheetname : Data.keySet())
		{
			System.out.println("Stored data for sheet " + sheetname + " has a size of " + Data.get(sheetname).getEntryData().size());
		}
	}
		

}
