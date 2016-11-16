package idare.imagenode.internal.ColorManagement.ColorScales;

import java.awt.Color;
import java.util.Arrays;

import idare.imagenode.internal.Debug.PrintFDebugger;
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
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + Arrays.hashCode(ColorSteps);
//		result = prime * result + Arrays.hashCode(fractions);
//		return result;
//	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//		{
//			PrintFDebugger.Debugging(this, "Classes do not match");
//			return false;
//		}
//			
//		ColorScaleProperties other = (ColorScaleProperties) obj;
//		if (!Arrays.equals(ColorSteps, other.ColorSteps))
//		{
//			PrintFDebugger.Debugging(this, "ColorSteps do not match");
//			for(Color c : ColorSteps)
//			{
//				System.out.print(c.getRed() + "/" + c.getGreen() + "/" + c.getBlue() + "\t");
//			}
//			System.out.println("");
//			for(Color c : other.ColorSteps)
//			{
//				System.out.print(c.getRed() + "/" + c.getGreen() + "/" + c.getBlue() + "\t");
//			}
//			return false;
//		}
//		if (!Arrays.equals(fractions, other.fractions))
//		{
//			PrintFDebugger.Debugging(this, "Fractions do not match");
//			for(int i = 1; i < fractions.length & i < other.fractions.length; i++)
//			{
//				System.out.println( fractions[i] - other.fractions[i]);
//			}
//			return false;
//		}
//		return true;
//	}
	
}
