package idare.metanode.internal.ColorManagement;

import java.awt.Color;
/**
 * Color Utils provide some static convenience functions for Colors
 * @author Thomas Pfau
 *
 */
public class ColorUtils {

	/**
	 * Obtain the contrasting black/white color for a original color.
	 * The grey value will be computed and white or black will be returned depending on its value.  
	 * @param origcolor - The original color
	 * @return The contrasting colo on a black/white scale
	 */
    public static Color getContrastingColor(Color origcolor)
    {		            
    	if(origcolor == null)
    	{
    		return Color.BLACK;
    	}
    	double greycol = origcolor.getRed() * 0.3 + origcolor.getGreen() * 0.59 + origcolor.getBlue() * 0.11;
    	Color ReturnColor;
    	if(greycol/255 > 0.5)
    		ReturnColor = new Color(0,0,0);
    	else
    		ReturnColor = new Color(255,255,255);
    	return ReturnColor;
    }
}
