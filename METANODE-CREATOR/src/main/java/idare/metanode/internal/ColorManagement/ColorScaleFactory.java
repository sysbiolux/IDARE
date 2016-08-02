package idare.metanode.internal.ColorManagement;

import java.util.Collection;
import java.util.Vector;

import idare.metanode.internal.ColorManagement.ColorScales.BlackYellowRed;
import idare.metanode.internal.ColorManagement.ColorScales.BlueWhiteRedScale;
import idare.metanode.internal.ColorManagement.ColorScales.BlueYellowRed;
import idare.metanode.internal.ColorManagement.ColorScales.FlagScale;
import idare.metanode.internal.ColorManagement.ColorScales.GreenBlackRed;
import idare.metanode.internal.ColorManagement.ColorScales.GreenYellow;
import idare.metanode.internal.ColorManagement.ColorScales.HSVScale;
import idare.metanode.internal.ColorManagement.ColorScales.JetScale;
import idare.metanode.internal.ColorManagement.ColorScales.LineScale;
import idare.metanode.internal.ColorManagement.ColorScales.PrismScale;
import idare.metanode.internal.ColorManagement.ColorScales.RedWhiteBlueScale;
import idare.metanode.internal.ColorManagement.ColorScales.WhiteBlack;
import idare.metanode.internal.ColorManagement.ColorScales.WhiteBlue;
import idare.metanode.internal.ColorManagement.ColorScales.WhiteRed;
import idare.metanode.internal.ColorManagement.ColorScales.YellowGreen;
import idare.metanode.internal.ColorManagement.ColorScales.YellowRed;

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
	 * @param ColorScaleClass - The java canonical class name of the {@link ColorScale}
	 * @return an instance of the requested {@link ColorScale} or a {@link BlueWhiteRedScale} if the class could not be found.
	 */
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
	 * @return a Collection of Color Scales which are discreet
	 */ 
	public static Collection<ColorScale> getDiscreetColorScales(int colorcount)
	{
		Vector<ColorScale> scales = new Vector<ColorScale>();
		scales.add(new FlagScale());
		scales.add(new PrismScale());
		scales.add(new LineScale());
		scales.add(new HSVScale().getDiscreteColorScale(colorcount));
		scales.add(new JetScale().getDiscreteColorScale(colorcount));
		scales.add(new BlueYellowRed().getDiscreteColorScale(colorcount));
		scales.add(new BlackYellowRed().getDiscreteColorScale(colorcount));
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
	
	
}
