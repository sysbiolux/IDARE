package idare.imagenode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link MultiColorScale} ranging White to black
 * @author Thomas Pfau
 *
 */
public class WhiteBlack extends MultiColorScale{

	private static final long serialVersionUID = 1001;
	public WhiteBlack() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.WHITE,Color.BLACK},new float[]{0f,1.0f},128);
	}	
}
