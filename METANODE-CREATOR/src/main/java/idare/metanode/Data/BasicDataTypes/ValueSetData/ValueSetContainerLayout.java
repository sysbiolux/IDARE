package idare.metanode.Data.BasicDataTypes.ValueSetData;

import idare.metanode.Interfaces.DataSets.NodeData;
import idare.metanode.Interfaces.Layout.ContainerLayout;
import idare.metanode.internal.ColorManagement.ColorMap;

import java.awt.Rectangle;

import org.apache.batik.svggen.SVGGraphics2D;

public abstract class ValueSetContainerLayout extends ContainerLayout {
	private static final long serialVersionUID = 1001;
	/**
	 * Basic Constructor
	 */
	public ValueSetContainerLayout()
	{
		
	}
	@Override
	public abstract void createLayout(NodeData data, Rectangle area, String DataSetLabel);
	

	@Override
	public abstract void LayoutDataForNode(NodeData data, SVGGraphics2D context,
			boolean Legend, ColorMap coloring);
	
	/**
	 * Specific to ValueSetContainerLayouts, this function allows to get a 
	 * Range of minimal and maximal values based on a valuerange to determine suitable axes.
	 * @param valuerange
	 * @return Values which are suitable to plot the data range in.
	 */
	protected Double[] determineDisplayRange(Double[] valuerange)
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
	 * @param value - the value to round
	 * @param up - whether to round up or down.
	 * @return the rounded values.
	 */
	public static Double roundToOrder(Double value, boolean up)
	{
		int order = (int)Math.floor(Math.log10(Math.abs(value)));
		Double exponent = Math.pow(10, order);
		Double val = up ? Math.ceil(value/exponent)*exponent :Math.floor(value/exponent)*exponent;
		return val;
	}
	
}
