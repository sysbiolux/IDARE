package idare.metanode.internal.Data.itemizedData.TimeSeriesData;

import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemDataSetProperties;
import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemNodeData;
import idare.metanode.Interfaces.DataSets.DataContainer;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.DataSets.NodeData;
import idare.metanode.Properties.Localisation.Position;
import idare.metanode.internal.Data.itemizedData.RectangleData.RectangleContainer;

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
		return new RectangleContainer(origin, (AbstractItemNodeData) data);
	}

	@Override
	public DataContainer newContainerForData(NodeData data) {
		return new RectangleContainer(data.getDataSet(), (AbstractItemNodeData)data);
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
}
