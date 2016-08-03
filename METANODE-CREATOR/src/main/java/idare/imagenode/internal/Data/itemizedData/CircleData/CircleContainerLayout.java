package idare.imagenode.internal.Data.itemizedData.CircleData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemContainerLayout;
import idare.imagenode.internal.Utilities.LayoutUtils;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Layout for Circle items.
 * @author Thomas Pfau
 *
 */
public class CircleContainerLayout extends AbstractItemContainerLayout{
	private static final long serialVersionUID = 1001;
	/**
	 * Basic Constructor
	 */
	public CircleContainerLayout() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Shape getShape(Dimension Area) {
		//get the minimal side of the rectangle
		
		boolean heigherthanwide = Area.getWidth() < Area.getHeight();
		double cross = Math.min(Area.getWidth(),Area.getHeight()) - 2; // one pixel off at both edges
		double yoffset = 1;
		double xoffset = 1;
		if(heigherthanwide)
		{
			 yoffset = (Area.getHeight() / 2) - (cross / 2);
		}
		else
		{
			xoffset = (Area.getWidth() / 2) - (cross / 2);
		}
		Ellipse2D item = new Ellipse2D.Double(xoffset,yoffset,cross,cross);
		return item;
	}

	@Override
	public ShapePosition getLegendShape(Rectangle2D Area, String id) {
		
		Shape itemshape = getShape(new Dimension((int)Area.getWidth(),(int)Area.getHeight()));
		Rectangle2D rec = itemshape.getBounds2D();
		Font Labelfont = new Font(Font.MONOSPACED,Font.PLAIN,(int)(rec.getHeight() * 0.8));		
		FontMetrics fm = LayoutUtils.getSVGFontMetrics(Labelfont);
		int LabelWidth = fm.stringWidth(id);
		int LabelHeight = fm.getAscent() - fm.getDescent(); // Number only have ascend for all I know, so we ignore the descend
		double fontx = rec.getX() + 0.5 * rec.getWidth() - LabelWidth / 2.0; // put it at the center (i.e. center of the item (xpos + 0.5 width) minus half the width of the label. 
		double fonty = rec.getY() + 0.5 * rec.getHeight() + 0.5 * LabelHeight; // since we use the ascend and SVG plots at x,y = baseline we have to get the baseline position.
		Point2D p = new Point2D.Double(fontx,fonty);
		LegendShapePosition item = new LegendShapePosition(itemshape, Area, id, p, Labelfont);				
		return item;
	}
	

}