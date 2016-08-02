package idare.metanode.internal.Data.ValueSetData.GraphData;

import idare.metanode.internal.Data.ValueSetData.ValueSetContainer;
import idare.metanode.internal.Interfaces.ContainerLayout;
import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Interfaces.NodeData;
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
