package idare.metanode.internal.Interfaces;

import idare.metanode.internal.ColorManagement.ColorMap;

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
	 * @param data
	 * @param area
	 * @param DataSetLabel
	 */
	public abstract void createLayout(NodeData data, Rectangle area, String DataSetLabel);
	
	/**
	 * LAyout the Data according to the previously generated 
	 * @param data
	 * @param context
	 * @param Legend
	 * @param coloring
	 */
	public abstract void LayoutDataForNode(NodeData data, SVGGraphics2D context,  boolean Legend, ColorMap coloring );
		
}
