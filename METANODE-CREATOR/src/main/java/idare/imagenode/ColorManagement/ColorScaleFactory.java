package idare.imagenode.ColorManagement;

import idare.imagenode.internal.ColorManagement.ColorScales.BlackYellowRed;
import idare.imagenode.internal.ColorManagement.ColorScales.BlueWhiteRedScale;
import idare.imagenode.internal.ColorManagement.ColorScales.BlueYellowRed;
import idare.imagenode.internal.ColorManagement.ColorScales.DiscreteColorScale;
import idare.imagenode.internal.ColorManagement.ColorScales.FlagScale;
import idare.imagenode.internal.ColorManagement.ColorScales.GreenBlackRed;
import idare.imagenode.internal.ColorManagement.ColorScales.GreenYellow;
import idare.imagenode.internal.ColorManagement.ColorScales.HSVScale;
import idare.imagenode.internal.ColorManagement.ColorScales.JetScale;
import idare.imagenode.internal.ColorManagement.ColorScales.LineScale;
import idare.imagenode.internal.ColorManagement.ColorScales.PrismScale;
import idare.imagenode.internal.ColorManagement.ColorScales.RedWhiteBlueScale;
import idare.imagenode.internal.ColorManagement.ColorScales.WhiteBlack;
import idare.imagenode.internal.ColorManagement.ColorScales.WhiteBlue;
import idare.imagenode.internal.ColorManagement.ColorScales.WhiteRed;
import idare.imagenode.internal.ColorManagement.ColorScales.YellowGreen;
import idare.imagenode.internal.ColorManagement.ColorScales.YellowRed;

import java.awt.Color;
import java.util.Collection;
import java.util.Vector;

/**
 * The {@link ColorScaleFactory} can either generate individual {@link ColorScale}s or sets of {@link ColorScale}s 
 * which possess specific properties. 
 * @author Thomas Pfau
 *
 */
public class ColorScaleFactory {	
	public static String BLUEWHITERED = BlueWhiteRedScale.class.getCanonicalName();
	
	
	/**
	 * Get a ColorScale based on a given class name
	 * @param ColorScaleClass The java canonical class name of the {@link ColorScale}
	 * @return an instance of the requested {@link ColorScale} or a {@link BlueWhiteRedScale} if the class could not be found.
	 */
	@SuppressWarnings("rawtypes")
	public static ColorScale getColorScale(String ColorScaleClass)
	{		
		try{
			Class DSclass = Class.forName(ColorScaleClass);
			return (ColorScale) DSclass.newInstance();
		}
			
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		return new BlueWhiteRedScale();
	}
	
	/**
	 * Get a set of discreet {@link ColorScale}s.
	 * @param colorcount - The number of colors to use for the Discreet scale.  
	 * @return a Collection of Color Scales which are discreet
	 */ 
	public static Collection<ColorScale> getDiscreetColorScales(int colorcount)
	{
		Vector<ColorScale> scales = new Vector<ColorScale>();
		if(colorcount == 1)
		{
			scales.add(new DiscreteColorScale(new Color[]{ Color.MAGENTA}));
			scales.add(new DiscreteColorScale(new Color[]{ Color.RED }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.ORANGE }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.YELLOW }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.GREEN }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.GREEN.darker().darker().darker() }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.CYAN }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.BLUE }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.BLACK}));			
		}
		else
		{
		
		scales.add(new FlagScale());
		scales.add(new PrismScale());
		scales.add(new LineScale());
		scales.add(new HSVScale().getDiscreteColorScale(colorcount));
		scales.add(new JetScale().getDiscreteColorScale(colorcount));		
		if(colorcount <= 3)
		{
			scales.add(new BlueWhiteRedScale().getDiscreteColorScale(colorcount));
			scales.add(new BlueYellowRed().getDiscreteColorScale(colorcount));
			scales.add(new BlackYellowRed().getDiscreteColorScale(colorcount));
			scales.add(new GreenBlackRed().getDiscreteColorScale(colorcount));			
		}
				
		}
		
		return scales;
	}
	/**
	 * Get a set of continuous {@link ColorScale}s.  
	 * @return a Collection of Color Scales which are continuous
	 */

	public static Vector<ColorScale> getContinousColorScales()
	{
		Vector<ColorScale> scales = new Vector<ColorScale>();
		scales.add(new BlueWhiteRedScale());
		scales.add(new GreenBlackRed());
		scales.add(new WhiteRed());
		scales.add(new WhiteBlue());
		scales.add(new GreenYellow());
		scales.add(new YellowGreen());
		scales.add(new WhiteBlack());		
		scales.add(new YellowRed());
		scales.add(new BlueYellowRed());
		scales.add(new BlackYellowRed());
		scales.add(new RedWhiteBlueScale());
		scales.add(new HSVScale());
		scales.add(new JetScale());
		
		return scales;
	}
	
	public static Vector<ColorScale> getNonWhiteDiscreteColorScales(int colorcount)
	{
		Vector<ColorScale> scales = new Vector<ColorScale>();
		if(colorcount == 1)
		{
			scales.add(new DiscreteColorScale(new Color[]{ Color.MAGENTA}));
			scales.add(new DiscreteColorScale(new Color[]{ Color.RED }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.ORANGE }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.YELLOW }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.GREEN }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.CYAN }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.GREEN.darker().darker() }));			
			scales.add(new DiscreteColorScale(new Color[]{ Color.BLUE }));
			scales.add(new DiscreteColorScale(new Color[]{ Color.BLACK}));			
		}
		else
		{
			scales.add(new PrismScale());
			scales.add(new LineScale());
			scales.add(new HSVScale().getDiscreteColorScale(colorcount));
			scales.add(new JetScale().getDiscreteColorScale(colorcount));
			scales.add(new BlueYellowRed().getDiscreteColorScale(colorcount));
			scales.add(new BlackYellowRed().getDiscreteColorScale(colorcount));
			scales.add(new GreenYellow().getDiscreteColorScale(colorcount));
		}

		return scales;
	}
	
}
