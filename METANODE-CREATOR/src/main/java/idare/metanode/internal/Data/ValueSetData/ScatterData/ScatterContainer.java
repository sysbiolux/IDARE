package idare.metanode.internal.Data.ValueSetData.ScatterData;

import idare.metanode.internal.Data.ValueSetData.ValueSetContainer;
import idare.metanode.internal.Interfaces.ContainerLayout;
import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Interfaces.NodeData;
/**
 * Container Class for GraphData
 * @author Thomas Pfau
 *
 */
public class ScatterContainer extends ValueSetContainer {
	private int labelSize; 
	public ScatterContainer(DataSet origin, NodeData data, int labelsize)
	{
		super(origin,data);
		labelSize = labelsize;
	}
	@Override
	public ContainerLayout createEmptyLayout()
	{
		return new ScatterContainerLayout(labelSize);
	}
}
