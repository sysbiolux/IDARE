package idare.imagenode.Data.BasicDataTypes.ValueSetData;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import javax.swing.JPanel;
/**
 * A SetEntryPanel is a simple abstract representation of a marker or shape along with the color of a specific entry.
 * IT assumes, that the shape will be provided by the implementing class.
 * @author Thomas Pfau
 *
 */
public abstract class SetEntryPanel extends JPanel {
	private static final long serialVersionUID = 1001;
	private Color ShapeColor;
	/**
	 * Default Constructor using a given color for representation. 
	 * @param ShapeColor - the color that represents this entry
	 */
	public SetEntryPanel(Color ShapeColor)
	{
		super();
		this.ShapeColor = ShapeColor;

	}
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		Paint cpaint = g2d.getPaint();
		Stroke cstroke = g2d.getStroke();
		Color ccolor= g2d.getColor();
		Dimension size = getSize();
		int ext = Math.min(size.height - 2, size.width - 2);
		g2d.setPaint(ShapeColor);
		g2d.setColor(ShapeColor);
		g2d.setStroke(new BasicStroke(3));				
		Shape shape = getShape(0,0,ext,ext); 
		plotShape(shape,g2d);
		g2d.setStroke(cstroke);
		g2d.setColor(ccolor);
		g2d.setPaint(cpaint);
	}
	/**
	 * Plot the shape. This will normally draw the shape provided by the getShape method. 
	 * However, a extending class may overwrite this to plot the shape in other ways or set different properties to the stroke/Paint.
	 * Note however, that any changes to the g2d styles should be reverted before returning. 
	 * @param shape - the {@link Shape} to plot
	 * @param g2d - the {@link Graphics2D} to plot in
	 */
	public void plotShape(Shape shape,Graphics2D g2d)
	{
		g2d.draw(shape);
	}
	
	/**
	 * Get the shape that represents this entry. The shape should be in the bounding box defined by the provided x and y positions 
	 * along with the width and height
	 * NOTE: The resulting Shape has to use the basic java shapes (otherwise batik might get into trouble plotting them)  
	 * @param xpos - the leftmost x position to be occupied by this shape
	 * @param ypos - the uppermost y position to be occupied by this shape
	 * @param width - the width (right to left) of this shape
	 * @param height - the height (top to bottom) of the requested shape.
	 * @return - a shape localised to the given specifications.  NOTE: The resulting Shape has to use the basic java shapes (otherwise batik might get into trouble plotting them)
	 */
	public abstract Shape getShape(double xpos,double ypos, double width, double height);	
	
}
