package idare.imagenode.internal.Layout;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;

/**
 * A simple bundle struct for a {@link DataSet} and a {@link ColorMap}
 * @author Thomas Pfau
 *
 */
public class DataSetLayoutInfoBundle implements DataSetLink{
	 
	public DataSet dataset;
	public ColorMap colormap;
	public DataSetLayoutProperties properties;
	public String Label;
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.DataSetLink#getDataSet()
	 */
	public DataSet getDataSet()
	{
		return dataset;
	}
}
