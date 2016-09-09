package idare.imagenode.internal.Data.Array.RectangleData;

import idare.imagenode.Data.BasicDataTypes.ArrayData.AbstractArrayContainer;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayNodeData;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
/**
 * Class to provide a Container for Rectangle related data
 * @author Thomas Pfau
 *
 */
public class RectangleContainer extends AbstractArrayContainer{

	public RectangleContainer(DataSet origin, ArrayNodeData data)
	{
		super(origin,data);
	}
	public ContainerLayout createEmptyLayout()
	{
		return new RectangleContainerLayout();
	}
}
