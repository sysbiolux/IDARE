package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link MultiColorScale} ranging yellow to red
 * @author Thomas Pfau
 *
 */
public class YellowRed extends MultiColorScale{
	private static final long serialVersionUID = 1001;
	
	public YellowRed() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.YELLOW.brighter(),Color.RED},new float[]{0f,1.0f},128);
	}	
}
