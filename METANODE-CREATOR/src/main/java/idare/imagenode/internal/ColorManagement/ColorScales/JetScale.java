package idare.imagenode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link MultiColorScale} ranging from a dark blue via blue, cyan, yellow and red to a dark red 
 * @author Thomas Pfau
 *
 */
public class JetScale extends MultiColorScale{
	private static final long serialVersionUID = 1001;
	public JetScale(){
		super(new Color[]{new Color(0,0,102),Color.BLUE,Color.CYAN,Color.YELLOW,Color.RED,new Color(102,0,0)},
				new float[]{0f,1f/8,3f/8,5f/8,7f/8,1f},128);
	}

}
