package idare.metanode.internal.Data.itemizedData.CircleGridData;

import idare.metanode.internal.Data.itemizedData.AbstractItemContainer;
import idare.metanode.internal.Data.itemizedData.AbstractItemNodeData;
import idare.metanode.internal.Data.itemizedData.CircleData.CircleContainerLayout;
import idare.metanode.internal.Interfaces.ContainerLayout;
import idare.metanode.internal.Interfaces.DataSet;
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
	public CircleGridContainer(DataSet origin, AbstractItemNodeData data)
	{
		super(origin,data);
		//System.out.println("Creating new Container for DataSet " + origin.getID() + " with " + data.getValueCount() + " items" );
	}
	@Override
	public ContainerLayout createEmptyLayout()
	{
		return new CircleContainerLayout();
	}
}
