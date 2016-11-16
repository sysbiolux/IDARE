package idare.imagenode.internal.Data.Array.TimeSeriesData;

import idare.imagenode.Data.BasicDataTypes.ArrayData.AbstractArrayDataSetProperties;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayDataSet;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayNodeData;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.Localisation.Position;
import idare.imagenode.exceptions.io.WrongFormat;
import idare.imagenode.internal.Data.Array.RectangleData.RectangleContainer;

/**
 * Properties for a Time Series DataSet
 * @author Thomas Pfau
 *
 */
public class TimeSeriesDataSetProperties extends AbstractArrayDataSetProperties{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
	public Position getLocalisationPreference() {
		// TODO Auto-generated method stub
		return Position.CENTER;
	}

	@Override
	public boolean getItemFlexibility() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataContainer newContainerInstance(DataSet origin, NodeData data) {
		return new RectangleContainer(origin, (ArrayNodeData) data);
	}

//	@Override
//	public DataContainer newContainerForData(NodeData data) {
//		return new RectangleContainer(data.getDataSet(), (ArrayNodeData)data);
//	}

	@Override
	public String toString()
	{
		return "Time Series";
	}
	@Override
	public String getTypeName()
	{
		return "Time Series";
	}
	@Override
	public void testValidity(DataSet set) throws WrongFormat
	{
		try{
			ArrayDataSet ads = (ArrayDataSet) set;
			if(((ArrayNodeData)ads.getDefaultData()).getValueCount() >= 12)
			{
				throw new WrongFormat("Too many items for layout type " + getTypeName());
			}
		}
		catch(ClassCastException e)
		{
			throw new WrongFormat("Invalid dataset type for " + getTypeName());
		}
	}
}
