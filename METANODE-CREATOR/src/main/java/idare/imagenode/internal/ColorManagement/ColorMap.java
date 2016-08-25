package idare.imagenode.internal.ColorManagement;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;

import java.awt.Color;
import java.awt.Dimension;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
/**
 * A {@link ColorMap} provides an interface to a {@link DataSet} to obtain colors for specific values present in the {@link DataSet}.
 * @author Thomas Pfau
 *
 */
public abstract class ColorMap implements Serializable{

	
	private static final long serialVersionUID = 1001;
	
	protected ColorScale cs;
	
	/**
	 * Generate a new {@link ColorMap} using the provided {@link ColorScale}
	 * @param cs
	 */
	public ColorMap(ColorScale cs)
	{
		this.cs = cs;		
	}
	
	/**
	 * Provides appropriate (not too long) representations of the values provided.
	 * The input values have to be ordered from smallest to largest and it is assumed, that values[0] is the minimum and values[last] is the largest value.
	 * @param values
	 * @return A map matching Strings to the provided Double values.
	 */
	public static HashMap<Double,String> getDisplayStrings(Double[] values)
	{		
		HashMap<Double, String> translation = new HashMap<Double, String>();		 		
		Double max = values[values.length-1];
		Double min = values[0];
		//Check whether the values are integers.
		boolean allint = true;

		for(int i = 0; i < values.length; i++){
			if(Math.floor(values[i]) != Math.ceil(values[i]))
			{
				allint = false;
				break;
			}
		}
		//we define an odd range a range that is three orders of magnitude different from the max value.
		//if both values are smaller than zero we assume min to be the "larger" value and check min.
		// these are essentially instances, where we have e.g. max = 1000 and min = 999 (i.e. values that are very much distorted).
		boolean oddrange = (max / (max - min) > 1e3);
		if(max <= 0)
		{
			oddrange = (Math.abs(min)/ (max - min) > 1e3);
		}
		//if it is an oddrange, we will just use "min and max for display".
		if(oddrange)
		{
			translation.put(values[0], "Min");
			translation.put(values[values.length -1], "Min");
			return translation;
		}
		//Otherwise we have "sensible value.
		int order;
		if(max <= 0)
		{
			order = (int)Math.floor(Math.log10(Math.abs(min)));
		}
		else
		{			
			order = (int)Math.floor(Math.log10(Math.abs(max)));
		}
		for(double value : values)
		{
			//The normal assumption is a scientific notation, 
			String FormatString = "#.###E0";
			//if we have integer Values
			boolean intval = Math.floor(value) == Math.ceil(value);
			order = (int)Math.floor(Math.log10(Math.abs(value)));
			//But if we have integer values with less than 5 digits, we just use those.
			if(order < 5 & intval)
			{
				FormatString = "#0";
			}
			//And if have two or less digits but not too small, we use a notation rounded to two entries. 
			else if(order < 3 & order > -3)
			{
				FormatString = "#.##";
			}
			NumberFormat nf = new DecimalFormat(FormatString);
	
			translation.put(value, nf.format(value));
		}
		return translation;
		
	}
	
	public Color getDefaultColor()
	{
		return cs.getColor(0);
	}
	/**
	 * Get a visual representation of the {@link ColorScale} used in this map.
	 * This is in fact, the {@link ColorScalePane} produced by the enclosed {@link ColorScale}  
	 * @return a {@link ColorScalePane} that can be displayed.
	 */
	public ColorScalePane getColorScalePane()
	{
		return cs.getColorScalePane();
	}
	
	/**
	 * Set a specific {@link ColorScale} to be used by this {@link ColorMap}.
	 * The Colormap will be reset to use the provided ColorScale.
	 * @param scale the {@link ColorScale} to be used.
	 */
	public abstract void setColorScale(ColorScale scale);
	
	/**
	 * Get a Component that represents this {@link ColorMap}
	 * @param Legend The legend this component is part of (to obtain resize events).
	 * @return A {@link JComponent} that plots information about this {@link ColorMap}
	 */
	public abstract JComponent getColorMapComponent(JScrollPane Legend);
	 
	/**
	 * Get the {@link Color} that is associated with the {@link Comparable} object provided.
	 * @param Value A {@link Comparable} value.
	 * @return a {@link Color} 
	 */
	public abstract Color getColor(Comparable Value);

}	
