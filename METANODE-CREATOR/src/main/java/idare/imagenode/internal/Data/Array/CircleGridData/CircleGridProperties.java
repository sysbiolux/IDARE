package idare.imagenode.internal.Data.Array.CircleGridData;

import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayDataSet;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayNodeData;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.Localisation.Position;
import idare.imagenode.exceptions.io.WrongFormat;
import idare.imagenode.internal.Data.Array.CircleData.CircleDataSetProperties;
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
		return new CircleGridContainer(origin, (ArrayNodeData) data);
	}

	@Override
	public DataContainer newContainerForData(NodeData data) {
		return new CircleGridContainer(data.getDataSet(), (ArrayNodeData) data);
	}
	@Override
	public String toString()
	{
		return "Gridded Circles";
	}
	@Override
	public String getTypeName()
	{
		return "Gridded Circles";
	}
	@Override
	public void testValidity(DataSet set) throws WrongFormat
	{
		try{
			ArrayDataSet ads = (ArrayDataSet) set;
			if(((ArrayNodeData)ads.getDefaultData()).getValueCount() >= 12)
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
