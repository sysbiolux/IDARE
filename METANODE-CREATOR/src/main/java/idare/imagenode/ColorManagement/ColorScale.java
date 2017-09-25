package idare.imagenode.ColorManagement;

import idare.imagenode.internal.ColorManagement.ColorScales.ColorScaleProperties;

import java.awt.Color;
import java.io.Serializable;


/**
 * A {@link ColorScale} provides {@link Color}s based on its properties for values between 0 and 1.  
 * @author Thomas Pfau
 *
 */
public interface ColorScale extends Serializable{

	/**
	 * get a color
	 * @param value a value between 0 and 1.
	 * @return the appropriate color on the scale.
	 */
	public Color getColor(double value);
	/**
	 * Get the Properties of this ColorScale.
	 * The properties show the fractions and associated colors used.
	 * @return The {@link ColorScaleProperties} of this {@link ColorScale} 
	 */
	public ColorScaleProperties getColorScaleProperties();
	/**
	 * Get a {@link ColorScalePane} as a description for this {@link ColorScale}
	 * @return A {@link ColorScalePane} that can represents this ColorScale
	 */
	public ColorScalePane getColorScalePane();
	
	/**
	 * Set the {@link ColorScaleProperties} used by this {@link ColorScale}
	 * This can e.g. adapt a certain color to a specific point in the scale (e.g. for 0 balancing) but can also be used to stretch
	 * specific parts of the {@link ColorScale}.
	 * @param props The {@link ColorScaleProperties} to be used.
	 */
	public void setColorScaleProperties(ColorScaleProperties props);
	
	/**
	 * Set the number of colors used by this scale. 
	 * This function does not need to have an effect and can potentially be simply ignored, but it will be called by discreet color maps and potentially other methods.
	 * @param count the number of {@link Color}s to use.
	 */
	public void setColorCount(int count);
	
	/**
 	 * Move a point in the 0-1 scale to the target value.
	 * If Either origval or targetval are outside the 0-1 range, nothing will happen.
	 * THis should only influence the visual presentation.
	 * @param origval A value in the range of [0.0,1.0]. that should be moved 
	 * @param targetval The value the origval shall be moved to in the range of [0.0,1.0]. 
	 */
	public void movePointOnScale(float origval, float targetval);
	
//	/**
//	 * The hashcode of a colorscale is derived from its ColorScaleProperties. Two Colorscales with the same properties have the same 
//	 * hashCode.
//	 */
//	public int hashCode();
//	
//
//	/**
//	 * Two colorscale are equal if they have the same ColorScaleProperties. 
//	 * @param obj
//	 * @return whether the properties of the given Object is the same as the properties of this colorscale. 
//	 */
//	public boolean equals(Object obj);

}
