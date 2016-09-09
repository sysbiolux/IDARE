package idare.imagenode.internal.Data.Array.CircleGridData;

import idare.imagenode.Data.BasicDataTypes.ArrayData.AbstractArrayContainer;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayNodeData;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.internal.Data.Array.CircleData.CircleContainerLayout;
/**
 * Container for a row of Circles, that can contain empty columns.
 * @author Thomas Pfau
 *
 */
public class CircleGridContainer extends AbstractArrayContainer{

	/**
	 * Basic Constructor with source Dataset and nodedata
	 * @param origin
	 * @param data
	 */
	public CircleGridContainer(DataSet origin, ArrayNodeData data)
	{
		super(origin,data);
	}
	@Override
	public ContainerLayout createEmptyLayout()
	{
		return new CircleContainerLayout();
	}
}
