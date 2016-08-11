package idare.imagenode.internal.Data.itemizedData.TimeSeriesData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.ItemDataSet;
import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemDataSetProperties;
import idare.imagenode.Data.BasicDataTypes.itemizedData.ItemNodeData;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.Localisation.Position;
import idare.imagenode.internal.Data.itemizedData.RectangleData.RectangleContainer;
import idare.imagenode.internal.exceptions.io.WrongFormat;

/**
 * Properties for a Time Series DataSet
 * @author Thomas Pfau
 *
 */
public class TimeSeriesDataSetProperties extends AbstractItemDataSetProperties{

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
		return new RectangleContainer(origin, (ItemNodeData) data);
	}

	@Override
	public DataContainer newContainerForData(NodeData data) {
		return new RectangleContainer(data.getDataSet(), (ItemNodeData)data);
	}

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
			ItemDataSet ads = (ItemDataSet) set;
			if(((ItemNodeData)ads.getDefaultData()).getValueCount() >= 12)
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
