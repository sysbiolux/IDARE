package idare.imagenode.internal.Data.itemizedData.RectangleData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemContainer;
import idare.imagenode.Data.BasicDataTypes.itemizedData.ItemNodeData;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
/**
 * Class to provide a Container for Rectangle related data
 * @author Thomas Pfau
 *
 */
public class RectangleContainer extends AbstractItemContainer{

	public RectangleContainer(DataSet origin, ItemNodeData data)
	{
		super(origin,data);
	}
	public ContainerLayout createEmptyLayout()
	{
		return new RectangleContainerLayout();
	}
}
