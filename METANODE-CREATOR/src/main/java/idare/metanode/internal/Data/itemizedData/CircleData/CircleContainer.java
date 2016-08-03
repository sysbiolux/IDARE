package idare.metanode.internal.Data.itemizedData.CircleData;

import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemContainer;
import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemNodeData;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.Layout.ContainerLayout;

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
