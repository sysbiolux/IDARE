package idare.metanode.internal.Data.itemizedData.TimeSeriesData;

import idare.metanode.internal.Data.itemizedData.AbstractItemDataSetProperties;
import idare.metanode.internal.Data.itemizedData.AbstractItemNodeData;
import idare.metanode.internal.Data.itemizedData.RectangleData.RectangleContainer;
import idare.metanode.internal.Interfaces.DataContainer;
import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Interfaces.NodeData;
import idare.metanode.internal.Properties.Localisation.Position;

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
