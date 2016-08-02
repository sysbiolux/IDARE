package idare.metanode.internal.Data.itemizedData.RectangleData;

import idare.metanode.internal.Data.itemizedData.AbstractItemContainer;
import idare.metanode.internal.Data.itemizedData.AbstractItemNodeData;
import idare.metanode.internal.Interfaces.ContainerLayout;
import idare.metanode.internal.Interfaces.DataSet;
/**
 * Class to provide a Container for Rectangle related data
 * @author Thomas Pfau
 *
 */
public class RectangleContainer extends AbstractItemContainer{

	public RectangleContainer(DataSet origin, AbstractItemNodeData data)
	{
		super(origin,data);
		//System.out.println("Creating new Container for DataSet " + origin.getID() + " with " + data.getValueCount() + " items" );
	}
	public ContainerLayout createEmptyLayout()
	{
		return new RectangleContainerLayout();
	}
}
