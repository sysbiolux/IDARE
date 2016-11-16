package idare.imagenode.internal.Data.Array.CircleData;

import idare.imagenode.Data.BasicDataTypes.ArrayData.AbstractArrayDataSetProperties;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayNodeData;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.Localisation.Position;
/**
 * Properties of an Array Dataset represented by Circles.
 * @author Thomas Pfau
 *
 */
public class CircleDataSetProperties extends AbstractArrayDataSetProperties{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
		return new CircleContainer(origin, (ArrayNodeData) data);
	}

//	@Override
//	public DataContainer newContainerForData(NodeData data) {
//		return new CircleContainer(data.getDataSet(), (ArrayNodeData)data);
//	}

	@Override
	public String toString()
	{
		return "Circles";
	}
	@Override
	public String getTypeName()
	{
		return "Circles";
	}
}
