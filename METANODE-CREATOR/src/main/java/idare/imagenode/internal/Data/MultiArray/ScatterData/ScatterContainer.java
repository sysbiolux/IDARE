package idare.imagenode.internal.Data.MultiArray.ScatterData;

import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
/**
 * Container Class for GraphData
 * @author Thomas Pfau
 *
 */
public class ScatterContainer extends MultiArrayContainer {
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
