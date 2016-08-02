package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * Properties of a colorscale. This is just a bundle class that contains the ColorSteps 
 * and associated fractions in one object.
 * @author Thomas Pfau
 *
 */
public class ColorScaleProperties {
	public Color[] ColorSteps;
	public float[] fractions;	
	public ColorScaleProperties(Color[] colors, float[] fractions) {
		this.fractions = fractions;
		this.ColorSteps = colors;
	}
	
}
