package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link MultiColorScale} ranging from red via yellow, green, cyan, blue and pink again to red  
 * @author Thomas Pfau
 *
 */
public class HSVScale extends MultiColorScale {
	private static final long serialVersionUID = 1001;
	public HSVScale()
	{
		super(new Color[]{Color.PINK,Color.RED,Color.YELLOW,Color.GREEN,Color.CYAN,Color.BLUE}
		,new float[]{0f,1f/5,2f/5,3f/5,4f/5,1f},128);
	}
	
}
