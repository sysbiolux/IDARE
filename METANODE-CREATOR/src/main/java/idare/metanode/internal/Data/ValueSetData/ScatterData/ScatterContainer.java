package idare.metanode.internal.Data.ValueSetData.ScatterData;

import idare.metanode.Data.BasicDataTypes.ValueSetData.ValueSetContainer;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.DataSets.NodeData;
import idare.metanode.Interfaces.Layout.ContainerLayout;
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
