package idare.metanode.internal.Data.ValueSetData.GraphData;

import idare.metanode.Data.BasicDataTypes.ValueSetData.ValueSetContainer;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.DataSets.NodeData;
import idare.metanode.Interfaces.Layout.ContainerLayout;
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
