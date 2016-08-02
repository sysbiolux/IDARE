package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link MultiColorScale} from Green via black to red 
 * @author Thomas Pfau
 *
 */
public class GreenBlackRed extends MultiColorScale{
	private static final long serialVersionUID = 1001;
	
	public GreenBlackRed() {
		// TODO Auto-generated constructor stub
		super(new Color[]{Color.GREEN,Color.BLACK,Color.RED},new float[]{0f,0.5f,1.0f},128);
	}
		
}
