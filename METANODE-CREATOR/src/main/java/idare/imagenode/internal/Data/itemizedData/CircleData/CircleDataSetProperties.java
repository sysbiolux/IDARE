package idare.imagenode.internal.Data.itemizedData.CircleData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemDataSetProperties;
import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemNodeData;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.Localisation.Position;
/**
 * Properties of an Itemized Dataset represented by Circles.
 * @author Thomas Pfau
 *
 */
public class CircleDataSetProperties extends AbstractItemDataSetProperties{

	@Override
	public Position getLocalisationPreference() {
		// TODO Auto-generated method stub
		return Position.EDGE;
	}

	@Override
	public boolean getItemFlexibility() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public DataContainer newContainerInstance(DataSet origin, NodeData data) {
		return new CircleContainer(origin, (AbstractItemNodeData) data);
	}

	@Override
	public DataContainer newContainerForData(NodeData data) {
		return new CircleContainer(data.getDataSet(), (AbstractItemNodeData)data);
	}

	@Override
	public String toString()
	{
		return "Items";
	}
	@Override
	public String getTypeName()
	{
		return "Items";
	}
}
