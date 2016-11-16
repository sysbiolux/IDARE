package idare.imagenode.internal.Data.Array.CircleData;

import idare.imagenode.Data.BasicDataTypes.ArrayData.AbstractArrayContainer;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayNodeData;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Properties.Localisation;
import idare.imagenode.Properties.Localisation.Position;

/**
 * CircleContainer is a container for Circles representing the data items.
 * @author Thomas Pfau
 *
 */
public class CircleContainer extends AbstractArrayContainer{

	/**
	 * Basic Constructor using the source dataset and an instance of the data.
	 * @param origin 
	 * @param data
	 */
	public CircleContainer(DataSet origin, ArrayNodeData data)
	{
		super(origin,data);
		loc = new Localisation(Position.FREE, true);
	}
	@Override
	public ContainerLayout createEmptyLayout()
	{
		return new CircleContainerLayout();
	}
}
