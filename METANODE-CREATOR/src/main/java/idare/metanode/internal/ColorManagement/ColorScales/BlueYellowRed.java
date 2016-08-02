package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A Colorscale from Blue via Yellow To Red
 * @author Thomas Pfau
 *
 */

public class BlueYellowRed extends MultiColorScale{
	private static final long serialVersionUID = 1001;
	
	public BlueYellowRed() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.BLUE,Color.YELLOW,Color.RED},new float[]{0f,0.5f,1.0f},128);
	}	
}
