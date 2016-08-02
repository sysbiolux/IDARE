package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link MultiColorScale} ranging from red via white to blue
 * @author Thomas Pfau
 *
 */
public class RedWhiteBlueScale extends MultiColorScale{
	private static final long serialVersionUID = 1001;
	
	public RedWhiteBlueScale() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.RED,Color.WHITE,Color.BLUE},new float[]{0f,0.5f,1.0f},128);
	}
}
