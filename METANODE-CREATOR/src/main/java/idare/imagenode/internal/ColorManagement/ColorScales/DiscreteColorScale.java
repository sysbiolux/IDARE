package idare.imagenode.internal.ColorManagement.ColorScales;

import idare.imagenode.ColorManagement.ColorScalePane;
import idare.imagenode.ColorManagement.DiscreteColorScalePane;

import java.awt.Color;

/**
 * A Discrete color scale.
 * This type of scale contains a fixed set of colors, that will be used to provide colors.
 * It must be initialized with a count as to how many colors are required.
 * This will automatically generate repetitions of colors. 
 * @author Thomas Pfau
 *
 */
public class DiscreteColorScale extends MultiColorScale{
	private static final long serialVersionUID = 1001;
	/**
	 * A general field from which additional colors are generated.
	 */
	protected Color[] availablecolors;	
	
	/**
	 * Create a color array given a number of colors necessary and the {@link Color}s available
	 * @param count how many colors shoudl be generated
	 * @param availablecolors which colors shall be used to generate the array
	 * @return a {@link Color} array containing exactly count {@link Color}s
	 */
	protected static Color[] createColors(int count,Color[] availablecolors)
	{
		Color[] usedcolors = new Color[count];
		for(int i = 0 ; i < count; i++)
			usedcolors[i] = availablecolors[i%availablecolors.length];
		
		return usedcolors;
	}		
	/**
	 * Get a equally distanced array between 0 and 1.
	 * If only one color is requested, a zero will be returned.
	 * Otherwise the resulting array contains 0 at position 0, 1 at the last position, and n-2 equally distanced entries between 0 and 1 in ascending order.   
	 * @param len how many fractions. Has to be > 0
	 * @return an array of fractions
	 */
	private static float[] getFractions(int len)
	{
		float[] fractions = new float[len];
		if(len == 1)
		{
			fractions[0] = 0;
			return fractions;
		}
		for(int i = 0; i < len; i++)
		{			
			fractions[i] = (float) i / (float)(len-1);
//			PrintFDebugger.Debugging(DiscreteColorScale.class, "Creating new fraction: " + fractions[i] );
		}
		return fractions;
	}
	
	/**
	 * Basic constructors using a set of colors and initializing the scale with as many colors.
	 * @param availablecolors the {@link Color} array to use for color generation
	 */
	public DiscreteColorScale(Color[] availablecolors)
	{
		super(createColors(availablecolors.length,availablecolors),getFractions(availablecolors.length),availablecolors.length);
		this.availablecolors = availablecolors;
		
	}
	
	/**
	 * A constructor for a scale with colorcount colors using the availablecolors array to generate these colors.
	 * @param availablecolors the colors to use for scale generation
	 * @param colorcount the number of colors available from this scale
	 */
	public DiscreteColorScale(Color[] availablecolors, int colorcount)
	{
		super(createColors(colorcount,availablecolors),getFractions(colorcount),colorcount);
		this.availablecolors = availablecolors;
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorScales.MultiColorScale#getColorForValue(double)
	 */
	@Override
	protected Color getColorForValue(double value) {
		int colorpos = 0;
		while(value > fractions[colorpos])
		{
			colorpos++;			
		}
		return ColorSteps[colorpos];
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorScales.MultiColorScale#setColorCount(int)
	 */
	@Override
	public void setColorCount(int count)
	{
		this.fractions = getFractions(count);
		this.ColorSteps = createColors(count,availablecolors);
		usedColors = new Color[count];
		initColors();
	};
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorScales.MultiColorScale#getColorScalePane()
	 */
	@Override
	public ColorScalePane getColorScalePane()
	{
		return new DiscreteColorScalePane(this);
	}
}
