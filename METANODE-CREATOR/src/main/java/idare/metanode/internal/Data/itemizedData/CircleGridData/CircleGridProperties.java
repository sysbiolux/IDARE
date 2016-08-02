package idare.metanode.internal.Data.itemizedData.CircleGridData;

import idare.metanode.internal.Data.itemizedData.AbstractItemNodeData;
import idare.metanode.internal.Data.itemizedData.CircleData.CircleDataSetProperties;
import idare.metanode.internal.Interfaces.DataContainer;
import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Interfaces.NodeData;
import idare.metanode.internal.Properties.Localisation.Position;
/**
 * Properties of a Gridded Circle Dataset.
 * @author Thomas Pfau
 *
 */
public class CircleGridProperties extends CircleDataSetProperties {

	@Override
	public Position getLocalisationPreference() {
		return Position.CENTER;
	}

	@Override
	public boolean getItemFlexibility() {
		return false;
	}

	@Override
	public DataContainer newContainerInstance(DataSet origin, NodeData data) {
		return new CircleGridContainer(origin, (AbstractItemNodeData) data);
	}

	@Override
	public DataContainer newContainerForData(NodeData data) {
		return new CircleGridContainer(data.getDataSet(), (AbstractItemNodeData) data);
	}
	@Override
	public String toString()
	{
		return "Gridded Items";
	}
	@Override
	public String getTypeName()
	{
		return "Gridded Items";
	}
}
