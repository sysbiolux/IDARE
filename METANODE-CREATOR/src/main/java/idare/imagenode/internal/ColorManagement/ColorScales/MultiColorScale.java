package idare.imagenode.internal.ColorManagement.ColorScales;

import idare.imagenode.ColorManagement.ColorScale;
import idare.imagenode.ColorManagement.ColorScalePane;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.awt.Color;
import java.util.Arrays;
/**
 * A {@link MultiColorScale} is a color scale, that contains multiple colors and commonly has gradients between 
 * those colors that will be used to obtain the colors for plotting
 * It will provide a discrete number of colors, to reduce computational cost in calculating the color at each request.  
 * @author Thomas Pfau
 *
 */
public abstract class MultiColorScale implements ColorScale {
	private static final long serialVersionUID = 1001;
	protected Color[] ColorSteps;	
	protected float[] fractions;
	//We provide a maximum of 100 Colors;
	protected Color[] usedColors;
	/**
	 * General constructor using a set of colors, a float array for the positions of the colors on a 0-1 scale and the number of discrete colors to generate.
	 * Colors and fractions have to be of equal size. 
	 * @param Colors the colors to use
	 * @param fractions the positions of these colors on the scale
	 * @param colorcount the number of colors generated
	 */
	public MultiColorScale(Color[] Colors, float[] fractions, int colorcount)
	{
		this.fractions = fractions;
		this.ColorSteps = Colors;
		usedColors = new Color[colorcount];
		initColors();
		
	}
	/**
	 * A Constructor generating a colorscale using the given colors and the given fractions. The colorscale will use 100 colors.
	 * @param Colors the colors to use
	 * @param fractions the positions of these colors on the scale
	 */
	public MultiColorScale(Color[] Colors, float[] fractions) {
		//We will throw an error if the sizes are not equal.
		this(Colors,fractions,200);
	}
	
	
	/**
	 * Initialize the colors.
	 */
	protected void initColors()
	{
		//if only one color is requested, than this color will be for the value 0.
		if(usedColors.length == 1)
		{
			usedColors[0] = getColorForValue(0);
			return;
		}
		
		for(int i=0; i < usedColors.length; i++)
		{
			usedColors[i] = getColorForValue((double)i/(usedColors.length-1));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorScale#getColor(double)
	 */
	@Override
	public Color getColor(double value)
	{
		//values have to be between 0 and 1, otherwise we correct them.
		if(value > 1)
			value = 1;		
		if(value < 0)
			value = 0;
		//this ensure, that even with only 1 color, that color will always be chosen.
		int entry = (int)(Math.round(value * (usedColors.length-1)));
		return usedColors[entry];
	}
	
	/**
	 * Get the color associated with the provided value
	 * @param value The Value to obtain a color for
	 * @return The Color assigned to the given value.
	 */
	protected Color getColorForValue(double value) {
		// TODO Auto-generated method stub
		Color sourceColor = ColorSteps[0];
		Color targetColor = ColorSteps[0];
		float sourceFraction = 0;
		float targetFraction = 0;
		int colorpos = 0;
		//No values larger than 1 or smaller than 0 accedpted.
		//This also catched the single color case, where we simply return the 0 color.
		if(value >= 1.f)
		{
			return ColorSteps[ColorSteps.length-1];
		}
		if(value <= 0)
		{
			return ColorSteps[0];
		}
		while(value > fractions[colorpos])
		{
			sourceColor = ColorSteps[colorpos];			
			sourceFraction = fractions[colorpos];			
			colorpos++;
			targetColor = ColorSteps[colorpos];
			targetFraction = fractions[colorpos];
		}
		//The new color will be in between the source and the target color at the according position
		double range = targetFraction - sourceFraction;
		double pos = value - sourceFraction;
		int rrange = targetColor.getRed() - sourceColor.getRed();
		int grange = targetColor.getGreen() - sourceColor.getGreen();
		int brange = targetColor.getBlue() - sourceColor.getBlue();
		int alphrange = targetColor.getAlpha() - sourceColor.getAlpha();
		int r = (int)(sourceColor.getRed() + pos/range * rrange);
		int g = (int)(sourceColor.getGreen() + pos/range * grange);
		int b = (int)(sourceColor.getBlue() + pos/range * brange);
		int alph = (int)(sourceColor.getAlpha() + pos/range * alphrange);		
		return new Color(r,g,b,alph);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorScale#getColorScaleProperties()
	 */
	@Override
	public ColorScaleProperties getColorScaleProperties() {
		return new ColorScaleProperties(ColorSteps, fractions);
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorScale#getColorScalePane()
	 */
	@Override
	public ColorScalePane getColorScalePane()
	{
		return new ColorScalePane(this);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorScale#setColorScaleProperties(idare.imagenode.internal.ColorManagement.ColorScales.ColorScaleProperties)
	 */
	@Override
	public void setColorScaleProperties(ColorScaleProperties props) {
		this.fractions = props.fractions;
		this.ColorSteps = props.ColorSteps;
		initColors();
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorScale#setColorCount(int)
	 */
	@Override
	public void setColorCount(int count)
	{
		//Do nothing.
	};
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorScale#movePointOnScale(float, float)
	 */
	@Override
	public void movePointOnScale(float origval, float targetval)
	{
		//the first and the last fraction never change.
		//everything else, that is		
		float oldlowerrange = origval;		
		float oldupperrange = 1-origval;
//		PrintFDebugger.Debugging(this, "Mobving point " + origval + " to " + targetval);
		float newlowerrange = targetval;
		float newupperrange = 1 - targetval;
		
		float lowerrangechange = newlowerrange / oldlowerrange;
		float upperrangechange = newupperrange / oldupperrange;
		
		for(int i = 1; i < fractions.length-1; i++)
		{
//			PrintFDebugger.Debugging(this, "Moving" + fractions[i]+ " to: ");
			if( fractions[i] < origval)
			{
				fractions[i] = targetval + lowerrangechange * (fractions[i]-origval);
//				PrintFDebugger.Debugging(this, ""+fractions[i]);
				
			}
			else
			{
				fractions[i] = targetval + upperrangechange * (fractions[i]-origval);
//				PrintFDebugger.Debugging(this, ""+fractions[i]);
			}
		}
		initColors();
//		float scalelower = targetval / origval;
//		float scaleupper = (1-targetval)/origval;
//		if(targetval < origval)
//		{
//			scalelower = (1-targetval) / origval;
//			scaleupper =  targetval / origval ;
//		}
//		for(int i = 1; i < fractions.length-1; i++)
//		{
//			if(fractions[i] < origval)
//			{
//				fractions[i] = fractions[i]*scalelower;
//				PrintFDebugger.Debugging(this, "New Fraction " + fractions[i]);
//			}
//			else
//			{
//				fractions[i] = targetval + (fractions[i]-origval) * scaleupper;
//
//			}
//		}

	}
	
	/**
	 * Get a discrete Version of this color scale.
	 * @param colorcount the number of colors requested for this colorscale. 
	 * @return a discrete color scale built from this {@link MultiColorScale}
	 */
	public DiscreteColorScale getDiscreteColorScale(int colorcount)
	{
		Color[] ccolors = new Color[colorcount]; 
		if(colorcount == 1)
		{
			ccolors[0] = getColorForValue(0);			
		}
		else{
			for(int i=0; i < colorcount; i++)
			{
			ccolors[i] = getColorForValue((double)i/(colorcount-1));
			}
		}
		return new DiscreteColorScale(ccolors); 
	}
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + getColorScaleProperties().hashCode();
//		return result;
//	}
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (!(obj instanceof MultiColorScale))
//		{
//			PrintFDebugger.Debugging(this, "Classes do not match " + getClass() + "/" + obj.getClass() );
//			return false;
//		}
//		MultiColorScale other = (MultiColorScale) obj;
//		if (!getColorScaleProperties().equals(other.getColorScaleProperties()))
//		{
//			PrintFDebugger.Debugging(this, "Properties do not match");
//			return false;
//		}
//		return true;
//	}
	
	
}
