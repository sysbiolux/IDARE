package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;

/**
 * A Colorscale from Black via Yellow To Red
 * @author Thomas Pfau
 *
 */
public class BlackYellowRed extends MultiColorScale{
	private static final long serialVersionUID = 1001;
	
	public BlackYellowRed() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.BLACK,Color.YELLOW,Color.RED},new float[]{0f,0.5f,1.0f},128);
	}	
}
