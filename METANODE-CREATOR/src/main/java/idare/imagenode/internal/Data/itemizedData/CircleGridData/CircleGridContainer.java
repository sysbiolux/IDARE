package idare.imagenode.internal.Data.itemizedData.CircleGridData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemContainer;
import idare.imagenode.Data.BasicDataTypes.itemizedData.ItemNodeData;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.internal.Data.itemizedData.CircleData.CircleContainerLayout;
/**
 * Container for a row of Circles, that can contain empty columns.
 * @author Thomas Pfau
 *
 */
public class CircleGridContainer extends AbstractItemContainer{

	/**
	 * Basic Constructor with source Dataset and nodedata
	 * @param origin
	 * @param data
	 */
	public CircleGridContainer(DataSet origin, ItemNodeData data)
	{
		super(origin,data);
	}
	@Override
	public ContainerLayout createEmptyLayout()
	{
		return new CircleContainerLayout();
	}
}
