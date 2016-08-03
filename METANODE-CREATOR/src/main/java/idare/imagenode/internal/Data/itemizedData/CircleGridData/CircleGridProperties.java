package idare.imagenode.internal.Data.itemizedData.CircleGridData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemNodeData;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.Localisation.Position;
import idare.imagenode.internal.Data.itemizedData.CircleData.CircleDataSetProperties;
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
