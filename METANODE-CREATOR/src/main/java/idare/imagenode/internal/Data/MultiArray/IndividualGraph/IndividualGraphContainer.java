package idare.imagenode.internal.Data.MultiArray.IndividualGraph;

import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
/**
 * Container Class for GraphData
 * @author Thomas Pfau
 *
 */
public class IndividualGraphContainer extends MultiArrayContainer {
	public IndividualGraphContainer(DataSet origin, NodeData data)
	{
		super(origin,data);
	}
	@Override
	public ContainerLayout createEmptyLayout()
	{
		return new IndividualGraphContainerLayout();
	}
}
