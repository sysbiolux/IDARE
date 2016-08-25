package idare.imagenode.internal.Data.itemizedData.RectangleData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemDataSetProperties;
import idare.imagenode.Data.BasicDataTypes.itemizedData.ItemNodeData;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.Localisation.Position;
/**
 * Properties for a Dataset with representation as rectangles 
 * It will be termed as HeatMap.
 * @author Thomas Pfau
 *
 */
public class RectangleDataSetProperties extends AbstractItemDataSetProperties{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
		return new RectangleContainer(origin, (ItemNodeData) data);
	}

	@Override
	public DataContainer newContainerForData(NodeData data) {
		return new RectangleContainer(data.getDataSet(), (ItemNodeData)data);
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
