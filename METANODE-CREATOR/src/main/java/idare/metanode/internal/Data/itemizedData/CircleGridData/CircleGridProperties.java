package idare.metanode.internal.Data.itemizedData.CircleGridData;

import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemNodeData;
import idare.metanode.Interfaces.DataSets.DataContainer;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.DataSets.NodeData;
import idare.metanode.Properties.Localisation.Position;
import idare.metanode.internal.Data.itemizedData.CircleData.CircleDataSetProperties;
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
