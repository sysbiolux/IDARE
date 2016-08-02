package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link MultiColorScale} ranging white to blue
 * @author Thomas Pfau
 *
 */
public class WhiteBlue extends MultiColorScale{

	private static final long serialVersionUID = 1001;
	public WhiteBlue() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.WHITE,Color.BLUE},new float[]{0f,1.0f},128);
	}	
}
