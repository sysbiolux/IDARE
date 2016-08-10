package idare.imagenode.internal.Data.itemizedData.RectangleData;

import idare.imagenode.Data.BasicDataTypes.itemizedData.AbstractItemContainerLayout;
import idare.imagenode.internal.ColorManagement.ColorUtils;
import idare.imagenode.internal.Utilities.LayoutUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.svggen.SVGGraphics2D;

/**
 * Class to provide a ContainerLayout for Rectangle related data
 * @author Thomas Pfau
 *
 */

public class RectangleContainerLayout extends AbstractItemContainerLayout{
	private static final long serialVersionUID = 1001;
	public RectangleContainerLayout() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Shape getShape(Dimension Area) {
		Rectangle2D item = new Rectangle2D.Double(0,0,Area.getWidth(),Area.getHeight());
		return item;
	}

	@Override
	public ShapePosition getLegendShape(Rectangle2D Area, String id) {
		
		Shape itemshape = getShape(new Dimension((int)Area.getWidth(),(int)Area.getHeight()));
		Rectangle2D rec = itemshape.getBounds2D();
		Font Labelfont = new Font(Font.MONOSPACED,Font.PLAIN,(int)(rec.getHeight() * 0.8));
        Labelfont = LayoutUtils.scaleSVGFont(new Dimension((int)Area.getWidth(),(int)Area.getHeight()), Labelfont, id);
		FontMetrics fm = LayoutUtils.getSVGFontMetrics(Labelfont);
		int LabelWidth = fm.stringWidth(id);
		int LabelHeight = fm.getAscent() - fm.getDescent(); // Number only have ascend for all I know, so we ignore the descend
		double fontx = rec.getX() + 0.5 * rec.getWidth() - LabelWidth / 2.0; // put it at the center (i.e. center of the item (xpos + 0.5 width) minus half the width of the label. 
		double fonty = rec.getY() + 0.5 * rec.getHeight() + 0.5 * LabelHeight; // since we use the ascend and SVG plots at x,y = baseline we have to get the baseline position.
		Point2D p = new Point2D.Double(fontx,fonty);
		RectangleLegendShapePosition item = new RectangleLegendShapePosition(itemshape, Area, id, p, Labelfont);				
		return item;
	}
	
	@Override 
	protected ShapePosition getShapePosition(Shape currentShape, Rectangle2D position)
	{
		return new RectangleShapePosition(currentShape, position);
	}
	
	
	/**
	 * A Class that provides A ShapePosition for a Rectangle 
	 * This is necessary, as we need a slightly different draw method (black borders around everything and not contrasting colors
	 * for Rectangles in comparison to circles.
	 * @author Thomas Pfau
	 *
	 */
	public class RectangleShapePosition extends ShapePosition
	{
		private static final long serialVersionUID = 1001;
		public RectangleShapePosition(Shape shape, Rectangle2D position)
		{
			super(shape,position);
		}
		@Override
		public void draw(SVGGraphics2D context, Color fillcolor)
		{
			//System.out.println("Plotting Shape at position " + position.getX() + "/" + position.getY() + " with width/height " + shape.getBounds().width  + "/" + shape.getBounds().height);
			context.translate(position.getX(), position.getY());
			context.setPaint(fillcolor);
			context.fill(shape);
			context.setPaint(Color.black);
			context.draw(shape);
			//Move to the position of the Label.
			context.translate(-position.getX(), -position.getY());									
		}
	}
	/**
	 * A Class that provides A Legend represenatation of a {@link RectangleShapePosition} for a Rectangle 
	 * This is necessary, as we need a slightly different draw method (black borders around everything and not contrasting colors
	 * for Rectangles in comparison to circles.
	 * @author Thomas Pfau
	 *
	 */
	public class RectangleLegendShapePosition extends RectangleShapePosition
	{
		private static final long serialVersionUID = 1001;
		private Point2D FontPosition;
		private String ItemID;
		private Font labelFont;
		/**
		 * Default Constructor
		 * @param shape
		 * @param position
		 * @param ItemID
		 * @param FontPosition
		 * @param labelfont
		 */
		public RectangleLegendShapePosition(Shape shape, Rectangle2D position, String ItemID, Point2D FontPosition, Font labelfont)
		{
			super(shape,position);
			this.FontPosition = FontPosition;
			this.ItemID = ItemID;
			labelFont = labelfont;
		}
		@Override
		/*
		 * (non-Javadoc)
		 * @see idare.imagenode.internal.Data.itemizedData.RectangleData.RectangleContainerLayout.RectangleShapePosition#draw(org.apache.batik.svggen.SVGGraphics2D, java.awt.Color)
		 */
		public void draw(SVGGraphics2D context, Color fillcolor)
		{
			super.draw(context, fillcolor);
			Font OrigFont = context.getFont();
			context.translate(position.getX(), position.getY());
			context.setColor(ColorUtils.getContrastingColor(fillcolor));
			context.setFont(labelFont);
			context.drawString(ItemID,(int)FontPosition.getX(),(int)FontPosition.getY());			
			context.translate(-position.getX(), -position.getY());
			context.setFont(OrigFont);

		}
	}

}