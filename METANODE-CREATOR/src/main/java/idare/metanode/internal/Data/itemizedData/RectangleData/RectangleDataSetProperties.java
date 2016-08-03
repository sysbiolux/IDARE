package idare.metanode.internal.Data.itemizedData.RectangleData;

import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemDataSetProperties;
import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemNodeData;
import idare.metanode.Interfaces.DataSets.DataContainer;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.DataSets.NodeData;
import idare.metanode.Properties.Localisation.Position;
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
