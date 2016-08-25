package idare.imagenode.internal.Data.itemizedData.CircleGridData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.ItemDataSet;
import idare.imagenode.Data.BasicDataTypes.itemizedData.ItemNodeData;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.Localisation.Position;
import idare.imagenode.internal.Data.itemizedData.CircleData.CircleDataSetProperties;
import idare.imagenode.internal.exceptions.io.WrongFormat;
/**
 * Properties of a Gridded Circle Dataset.
 * @author Thomas Pfau
 *
 */
public class CircleGridProperties extends CircleDataSetProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
		return new CircleGridContainer(origin, (ItemNodeData) data);
	}

	@Override
	public DataContainer newContainerForData(NodeData data) {
		return new CircleGridContainer(data.getDataSet(), (ItemNodeData) data);
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
	@Override
	public void testValidity(DataSet set) throws WrongFormat
	{
		try{
			ItemDataSet ads = (ItemDataSet) set;
			if(((ItemNodeData)ads.getDefaultData()).getValueCount() >= 12)
			{
				throw new WrongFormat("Too many items for layout type " + getTypeName());
			}
		}
		catch(ClassCastException e)
		{
			throw new WrongFormat("Invalid dataset type for " + getTypeName());
		}
	}
}
