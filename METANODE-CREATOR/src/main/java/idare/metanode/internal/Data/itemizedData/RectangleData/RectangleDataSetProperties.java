package idare.metanode.internal.Data.itemizedData.RectangleData;

import idare.metanode.internal.Data.itemizedData.AbstractItemDataSetProperties;
import idare.metanode.internal.Data.itemizedData.AbstractItemNodeData;
import idare.metanode.internal.Interfaces.DataContainer;
import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Interfaces.NodeData;
import idare.metanode.internal.Properties.Localisation.Position;
/**
 * Properties for a Dataset with representation as rectangles 
 * It will be termed as HeatMap.
 * @author Thomas Pfau
 *
 */
public class RectangleDataSetProperties extends AbstractItemDataSetProperties{

	@Override
	public Position getLocalisationPreference() {
		return Position.EDGE;
	}

	@Override
	public boolean getItemFlexibility() {
		return true;
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
		return "Heatmap";
	}
	@Override
	public String getTypeName()
	{
		return "Heatmap";
	}
}
