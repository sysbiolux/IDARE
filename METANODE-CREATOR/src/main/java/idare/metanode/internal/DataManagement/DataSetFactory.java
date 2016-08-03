package idare.metanode.internal.DataManagement;

import idare.metanode.Interfaces.DataSets.DataSet;
/**
 * A Class that loads different DataSets based on their classname
 * @author Thomas Pfau
 *
 */
public class DataSetFactory {
	
	/**
	 * Default constructor
	 */
	public DataSetFactory()
	{		
	}
	/**
	 * Generate a new instance for a class of a specific classname.
	 * @param DataSetClass - the classname for which an instance is requested
	 * @return an empty instance of the respective class.
	 * @throws ClassNotFoundException - IF the class is not found.
	 */
	public static DataSet getDataSet(String DataSetClass) throws ClassNotFoundException
	{		
		Class DSclass = Class.forName(DataSetClass);
		try{
			return (DataSet) DSclass.newInstance();
		}
		catch(Exception ex)
		{	
			ex.printStackTrace(System.out);
		}
		return null;
	}
}
