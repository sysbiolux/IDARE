package idare.imagenode.Data.BasicDataTypes.ValueSetData;

import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.internal.ColorManagement.ColorMap;

import java.awt.Rectangle;

import org.apache.batik.svggen.SVGGraphics2D;
/**
 * Abstract class providing some functionalities for Layouts of Value Sets 
 * @author Thomas Pfau
 *
 */
public abstract class ValueSetContainerLayout extends ContainerLayout {
	private static final long serialVersionUID = 1001;
	/**
	 * Basic Constructor
	 */
	public ValueSetContainerLayout()
	{

	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.ContainerLayout#createLayout(idare.imagenode.Interfaces.DataSets.NodeData, java.awt.Rectangle, java.lang.String)
	 */
	@Override
	public abstract void createLayout(NodeData data, Rectangle area, String DataSetLabel);

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.ContainerLayout#LayoutDataForNode(idare.imagenode.Interfaces.DataSets.NodeData, org.apache.batik.svggen.SVGGraphics2D, boolean, idare.imagenode.internal.ColorManagement.ColorMap)
	 */
	@Override
	public abstract void LayoutDataForNode(NodeData data, SVGGraphics2D context,
			boolean Legend, ColorMap coloring);


}
