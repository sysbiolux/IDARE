package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A Colorscale from Blue via White To Red
 * @author Thomas Pfau
 *
 */

public class BlueWhiteRedScale extends MultiColorScale{
	private static final long serialVersionUID = 1001;
	
	public BlueWhiteRedScale() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.BLUE,Color.WHITE,Color.RED},new float[]{0f,0.5f,1.0f},128);
	}
		
}
