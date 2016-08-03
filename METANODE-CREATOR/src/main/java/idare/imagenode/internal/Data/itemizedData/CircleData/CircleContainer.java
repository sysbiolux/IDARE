package idare.imagenode.internal.Data.itemizedData.CircleData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemContainer;
import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemNodeData;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.ContainerLayout;

/**
 * CircleContainer is a container for Circles representing the data items.
 * @author Thomas Pfau
 *
 */
public class CircleContainer extends AbstractItemContainer{

	/**
	 * Basic Constructor using the source dataset and an instance of the data.
	 * @param origin
	 * @param data
	 */
	public CircleContainer(DataSet origin, AbstractItemNodeData data)
	{
		super(origin,data);
	}
	@Override
	public ContainerLayout createEmptyLayout()
	{
		return new CircleContainerLayout();
	}
}
