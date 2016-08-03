package idare.imagenode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link MultiColorScale} ranging yellow to green
 * @author Thomas Pfau
 *
 */
public class YellowGreen extends MultiColorScale{
	private static final long serialVersionUID = 1001;
	
	public YellowGreen() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.YELLOW.brighter(),Color.GREEN.darker().darker()},new float[]{0f,1.0f},128);
	}	
}
