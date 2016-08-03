package idare.imagenode.Interfaces.Layout;

import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.internal.ColorManagement.ColorMap;

import java.awt.Rectangle;
import java.io.Serializable;

import org.apache.batik.svggen.SVGGraphics2D;


/**
 * Classes implementing this interface need to be able to generate a layout when provided with 
 * an area to generate the layout 
 * @author Thomas Pfau
 *
 */
public abstract class ContainerLayout implements Serializable{
	private static final long serialVersionUID = 1001;
	
	/**
	 * Create the Layout for a given set of data, in a specified area using a specified Label for the underlying Dataset.
	 * @param data - The nodeData to use to set up the layout.
	 * @param area - the area in which to create the layout
	 * @param DataSetLabel - the label of the corresponding Dataset to display somewhere in the area.
	 */
	public abstract void createLayout(NodeData data, Rectangle area, String DataSetLabel);
	
	/**
	 * Layout the Data according to the previously generated layout  
	 * @param data - the data to layout
	 * @param context - the graphics context in which to draw the layout 
	 * @param Legend - whether this is a layout for the legend or not.
	 * @param coloring - The ColorMap to use for the layout
	 */
	public abstract void LayoutDataForNode(NodeData data, SVGGraphics2D context,  boolean Legend, ColorMap coloring );
		
}
