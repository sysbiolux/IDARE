package idare.imagenode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link MultiColorScale} from green to yellow 
 * @author Thomas Pfau
 *
 */
public class GreenYellow extends MultiColorScale{

	private static final long serialVersionUID = 1001;
	public GreenYellow() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.GREEN.darker().darker(),Color.YELLOW.brighter()},new float[]{0f,1.0f},128);
	}	
}
