package idare.imagenode.ColorManagement;

import idare.imagenode.internal.ColorManagement.ColorScales.ColorScaleProperties;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * A Colorscale pane is a graphical representation of a {@link ColorScale}, that can be added to Swing Components.
 * It is usualy providing a simple bar with all colors used in the scale. 
 * @author Thomas Pfau
 *
 */
public class ColorScalePane extends JPanel{
	private static final long serialVersionUID = 1001;
	ColorScale cs;
	/**
	 * Create a {@link ColorScalePane} using the provided ColorScale.
	 * This class will assume, that the {@link ColorScale} is continuous and will thus generate a Linear gradient from the {@link ColorScale}.  
	 * @param cs
	 */
	public ColorScalePane(ColorScale cs) {
		// TODO Auto-generated constructor stub
		super();
		this.cs = cs;
		this.setBorder(null);
		setPreferredSize(new Dimension(100,40));
	}
	
	/**
	 * This helper function paints a {@link LinearGradientPaint} on a graphics object.
	 * Unfortunately directly printing it to a panel will mess up the size, so we print it to a image instead in paintComponent 
	 * @param g The graphics object to paint to
	 * @param w The width of the gradient
	 * @param h the height of the gradient
	 */
	protected void paintScale(Graphics g, int w, int h)
	{
		Graphics2D g2d = (Graphics2D) g;
		Rectangle2D rec = new Rectangle2D.Double(0,0,w,h);
		ColorScaleProperties csp = cs.getColorScaleProperties();
		LinearGradientPaint gp = new LinearGradientPaint(new Point2D.Float(0,0), new Point2D.Float(w,0),csp.fractions,csp.ColorSteps);
		g2d.setPaint(gp);
		g2d.fill(rec);
	}	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);		
		Graphics2D g2d = (Graphics2D) g;
		int w = getWidth();
		int h = getHeight();	
		BufferedImage bi = getImage(w,h);		
		g2d.drawImage(bi, 0, 0, null);
	}	
	
	/**
	 * Get an image that represents this ColorScalePane, with the given width and height
	 * @param width
	 * @param height
	 * @return an Image of this colorscalepane
	 */
	public BufferedImage getImage(int width, int height)
	{
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g2 = bi.getGraphics();
		paintScale(g2, width, height);
		return bi;
	}
}


