package idare.metanode.internal.Data.itemizedData.CircleGridData;

import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemContainer;
import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemNodeData;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.Layout.ContainerLayout;
import idare.metanode.internal.Data.itemizedData.CircleData.CircleContainerLayout;
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
