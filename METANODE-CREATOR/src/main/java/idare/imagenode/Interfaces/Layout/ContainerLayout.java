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
	 * @param data The nodeData to use to set up the layout.
	 * @param area the area in which to create the layout
	 * @param DataSetLabel the label of the corresponding Dataset to display somewhere in the area.
	 */
	public abstract void createLayout(NodeData data, Rectangle area, String DataSetLabel);
	
	/**
	 * Layout the Data according to the previously generated layout  
	 * @param data the data to layout
	 * @param context the graphics context in which to draw the layout 
	 * @param Legend whether this is a layout for the legend or not.
	 * @param coloring The ColorMap to use for the layout
	 */
	public abstract void LayoutDataForNode(NodeData data, SVGGraphics2D context,  boolean Legend, ColorMap coloring );
		
	/**
	 * This function allows to get a 
	 * Range of minimal and maximal values based on a valuerange to determine suitable axes.
	 * @param valuerange
	 * @return Values which are suitable to plot the data range in.
	 */
	public static final Double[] determineDisplayRange(Double[] valuerange)
	{
		//First, we determine, whether this set has an odd data range.

		boolean oddrange = (valuerange[1]/ (valuerange[1] - valuerange[0]) > 1e3);				 	
		double max = valuerange[1];
		double min = valuerange[0];
		//if this data has an odd range, we just return the min and max values...
		if(oddrange)
		{
			return valuerange;
		}		
		return new Double[]{roundToOrder(min, false),roundToOrder(max, true)};

	}
	
	/**
	 * Round the number to the numbers Order. Either rounding up or down.
	 * e.g. 5 rounded up is 10;
	 * 101 rounded up is 200
	 * etc.
	 * @param value the value to round
	 * @param up whether to round up or down.
	 * @return the rounded values.
	 */
	public static final Double roundToOrder(Double value, boolean up)
	{
		int order = (int)Math.floor(Math.log10(Math.abs(value)));
		Double exponent = Math.pow(10, order);
		Double val = up ? Math.ceil(value/exponent)*exponent :Math.floor(value/exponent)*exponent;
		return val;
	}
}
