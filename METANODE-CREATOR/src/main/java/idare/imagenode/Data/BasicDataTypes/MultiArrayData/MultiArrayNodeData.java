package idare.imagenode.Data.BasicDataTypes.MultiArrayData;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.exceptions.io.DuplicateIDException;

import java.util.HashMap;

/**
 * {@link NodeData} for {@link MultiArrayDataSet}s 
 * @author Thomas Pfau
 *
 */
public class MultiArrayNodeData extends NodeData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String,MultiArrayDataValue> Data;
		
	/**
	 * Basic constructor providing a source DataSet
	 * @param origin
	 */
	public MultiArrayNodeData(DataSet origin) {
		super(origin);
		// TODO Auto-generated constructor stub
		Data = new HashMap<String, MultiArrayDataValue>();
	}	
	/**
	 * Add a new {@link MultiArrayDataValue} to this NodeData, for a specific SetID
	 * @param newset the NodeData
	 * @param VectorSetID the SetID (Sheet Name) 
	 * @throws DuplicateIDException
	 */
	public void addData(MultiArrayDataValue newset,String VectorSetID) throws DuplicateIDException
	{
		if(Data.containsKey(VectorSetID)) throw new DuplicateIDException(this.id,"Duplicate ID in sheet " + VectorSetID);
		//System.out.println("Setting Data for ID" + VectorSetID + " to a Vector with size " + newset.getEntryData().size());
		Data.put(VectorSetID,newset);
	}
	/**
	 * Get the Data stored for a specific Sheetname, returns null if this sheetname does have no associated data.
	 * @param SheetName  the requested sheetname
	 * @return A {@link MultiArrayDataValue} for the requested SheetName
	 */
	public MultiArrayDataValue getData(String SheetName)
	{
		//System.out.println("Getting value for " + SheetName + " which is " + Data.get(SheetName));
		
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
	@Override
	public boolean isempty() {
		for(String set : Data.keySet())
		{
			if(Data.get(set) != null)
			{
				MultiArrayDataValue val = Data.get(set);
				for(Double entry : val.getEntryData())
				{
					if(entry != null)
					{
						return false;
					}
				}
			}
		}
		return true;
	}
		

}
