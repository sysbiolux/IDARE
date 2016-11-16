package idare.imagenode.ColorManagement;

import idare.imagenode.internal.ColorManagement.ColorScales.ColorScaleProperties;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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
	 * @param cs the colorscale to use
	 */
	public DiscreteColorScalePane(ColorScale cs) {
		super(cs);
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * This helper function paints a {@link LinearGradientPaint} on a graphics object.
	 * Unfortunately directly printing it to a panel will mess up the size, so we print it to a image instead in paintComponent 
	 * @param g The graphics object to paint to
	 * @param w The width of the gradient
	 * @param h the height of the gradient
	 */
	@Override
	protected void paintScale(Graphics g, int w, int h)
	{
		Graphics2D g2d = (Graphics2D) g;
		ColorScaleProperties csp = cs.getColorScaleProperties();
		double singlecolorwidth =  w /(double)csp.ColorSteps.length;
		for(int i = 0; i < csp.ColorSteps.length; i++)
		{
			Rectangle2D rec = new Rectangle2D.Double(i*singlecolorwidth,0,(i+1) * singlecolorwidth,h);
			g2d.setPaint(csp.ColorSteps[i]);
			g2d.draw(rec);
			g2d.fill(rec);
		}
	}
	
	@Override	
	protected void paintComponent(Graphics g) {			
		Graphics2D g2d = (Graphics2D) g;
		//Get the size of this component.
		int w = getWidth();
		int h = getHeight();		
		paintScale(g2d, w, h);
	}
	
}
