package idare.metanode.internal.ColorManagement.ColorScales;

import java.awt.Color;
/**
 * A {@link DiscreteColorScale} with multiple colors fitting for lines 
 * @author Thomas Pfau
 *
 */
public class LineScale extends DiscreteColorScale {
	private static final long serialVersionUID = 1001;
	protected static Color[] linecolors = new Color[]{new Color(51,153,255), new Color(255,51,51), new Color(204,204,0), new Color(153,0,153),new Color(153,0,0), new Color(0,102,204)}; 	
	
	public LineScale() {
		// TODO Auto-generated constructor stub
		super(linecolors);
	}
	
}
