package idare.imagenode.internal.Data.ValueSetData.GraphData;

import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
/**
 * Container Class for GraphData
 * @author Thomas Pfau
 *
 */
public class GraphContainer extends ValueSetContainer {
	public GraphContainer(DataSet origin, NodeData data)
	{
		super(origin,data);
	}
	@Override
	public ContainerLayout createEmptyLayout()
	{
		return new GraphContainerLayout();
	}
}
