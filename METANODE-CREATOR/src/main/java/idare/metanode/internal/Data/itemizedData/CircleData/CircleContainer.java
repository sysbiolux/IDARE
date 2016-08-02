package idare.metanode.internal.Data.itemizedData.CircleData;

import idare.metanode.internal.Data.itemizedData.AbstractItemContainer;
import idare.metanode.internal.Data.itemizedData.AbstractItemNodeData;
import idare.metanode.internal.Interfaces.ContainerLayout;
import idare.metanode.internal.Interfaces.DataSet;

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
