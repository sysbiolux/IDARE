package idare.imagenode.internal.ColorManagement.ColorScales;

import idare.imagenode.internal.ColorManagement.ColorScale;

import java.awt.Color;
/**
 * A Discrete {@link ColorScale} with Red, white blue and black as colors. 
 * @author Thomas Pfau
 *
 */
public class FlagScale extends DiscreteColorScale {
	private static final long serialVersionUID = 1001;
	
	protected static Color[] flagcolors = new Color[]{Color.RED,Color.WHITE,Color.BLUE,Color.BLACK}; 	
	

	public FlagScale() {
		// TODO Auto-generated constructor stub
		super(flagcolors);
	}
}
