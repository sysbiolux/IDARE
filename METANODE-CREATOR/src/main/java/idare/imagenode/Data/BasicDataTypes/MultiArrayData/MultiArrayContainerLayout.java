package idare.imagenode.Data.BasicDataTypes.MultiArrayData;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;

import java.awt.Rectangle;

import org.apache.batik.svggen.SVGGraphics2D;
/**
 * Abstract class providing some functionalities for Layouts of Value Sets 
 * @author Thomas Pfau
 *
 */
public abstract class MultiArrayContainerLayout extends ContainerLayout {
	private static final long serialVersionUID = 1001;
	/**
	 * Basic Constructor
	 */
	public MultiArrayContainerLayout()
	{

	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.ContainerLayout#createLayout(idare.imagenode.Interfaces.DataSets.NodeData, java.awt.Rectangle, java.lang.String)
	 */
	@Override
	public abstract void setupLayout(NodeData data, Rectangle area, String DataSetLabel, DataSetLayoutProperties props)  throws WrongDatasetTypeException;

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.ContainerLayout#LayoutDataForNode(idare.imagenode.Interfaces.DataSets.NodeData, org.apache.batik.svggen.SVGGraphics2D, boolean, idare.imagenode.internal.ColorManagement.ColorMap)
	 */
	@Override
	public abstract void LayoutDataForNode(NodeData data, SVGGraphics2D context,
			boolean Legend, ColorMap coloring);


}
