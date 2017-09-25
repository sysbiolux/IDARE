package idare.imagenode.Utilities;

import java.awt.Font;
import java.awt.Point;
import java.io.Serializable;

import org.apache.batik.svggen.SVGGraphics2D;
/**
 * Class that represents a LegendLabel, which can be drawn in a specific graphics context.
 * @author Thomas Pfau
 *
 */
public class LegendLabel implements Serializable
{
	
	public String Label;
	Point Position;
	Font LabelFont;
	/**
	 * Default Constructor providing the Font, position and String used when writing this label.
	 * @param labelFont The {@link Font} to use for the label
	 * @param position the position (lower left) to use
	 * @param Label The content of the label.
	 */
	public LegendLabel(Font labelFont, Point position, String Label)
	{
		this.Label = Label;
		this.LabelFont = labelFont;
		this.Position = position;
	}
	/**
	 * Draw the Label in the given {@link SVGGraphics2D} context
	 * @param svg the {@link SVGGraphics2D} context to draw in
	 */
	public void draw(SVGGraphics2D svg)
	{
		Font OrigFont = svg.getFont();
		svg.setFont(LabelFont);
		svg.drawString(Label, Position.x, Position.y);
		svg.setFont(OrigFont);
	}
	
	/**
	 * Draw the Label in the given {@link SVGGraphics2D} context
	 * @param svg the {@link SVGGraphics2D} context to draw in
	 * @param Label The label to write in the context.
	 */
	public void draw(SVGGraphics2D svg, String Label)
	{
		Font OrigFont = svg.getFont();
		svg.setFont(LabelFont);
		svg.drawString(Label, Position.x, Position.y);
		svg.setFont(OrigFont);
	}
	
}
