package idare.imagenode.internal.Layout;

import idare.imagenode.Interfaces.DataSets.DataSet;

public interface DataSetLink {
	
	/**
	 * Get the DataSet this link refers to
	 * @return the dataset referred to by this link
	 */
	public DataSet getDataSet();
	
}
