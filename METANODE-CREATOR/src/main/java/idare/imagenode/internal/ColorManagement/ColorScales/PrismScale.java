package idare.imagenode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link DiscreteColorScale} providing the colors of a prism.
 * @author Thomas Pfau
 *
 */
public class PrismScale extends DiscreteColorScale {
	private static final long serialVersionUID = 1001;
	
	protected static Color[] availablecolors = new Color[]{Color.RED,Color.ORANGE,Color.YELLOW,Color.GREEN,Color.BLUE,new Color(160,32,240)}; 
	
	public PrismScale() {
		// TODO Auto-generated constructor stub
		super(availablecolors);
	}
	
}
