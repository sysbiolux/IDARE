package idare.metanode.internal.ColorManagement;

import idare.metanode.internal.ColorManagement.ColorScales.ColorScaleProperties;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
/**
 * A Discrete color scale pane
 * The difference to a normal colorscalepane is, that this pane will 
 * plot with discrete colors and will not use a linear gradient to walk between the colors. 
 * @author Thomas Pfau
 *
 */
public class DiscreteColorScalePane extends ColorScalePane{
	private static final long serialVersionUID = 1001;
	/**
	 * A Basic {@link DiscreteColorScalePane}.
	 * It assumes, that the colors are spaced equally in the {@link ColorScale} provided.  
	 * @param cs
	 */
	public DiscreteColorScalePane(ColorScale cs) {
		super(cs);
		// TODO Auto-generated constructor stub
	}

	@Override	
	protected void paintComponent(Graphics g) {			
		Graphics2D g2d = (Graphics2D) g;
		//Get the size of this component.
		int w = getWidth();
		int h = getHeight();		
		ColorScaleProperties csp = cs.getColorScaleProperties();
		double singlecolorwidth =  w /(double)csp.ColorSteps.length;
		//Draw one rectangle per color
		for(int i = 0; i < csp.ColorSteps.length; i++)
		{
			Rectangle2D rec = new Rectangle2D.Double(i*singlecolorwidth,0,(i+1) * singlecolorwidth,h);
			g2d.setPaint(csp.ColorSteps[i]);
			g2d.draw(rec);
			g2d.fill(rec);
		}
	}

}
