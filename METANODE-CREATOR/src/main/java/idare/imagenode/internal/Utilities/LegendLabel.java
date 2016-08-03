package idare.imagenode.internal.Utilities;

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
	
	String Label;
	Point Position;
	Font LabelFont;
	/**
	 * Default Constructor providing the Font, position and String used when writing this label.
	 * @param labelFont
	 * @param position
	 * @param Label
	 */
	public LegendLabel(Font labelFont, Point position, String Label)
	{
		this.Label = Label;
		this.LabelFont = labelFont;
		this.Position = position;
	}
	/**
	 * Draw the Label in the given {@link SVGGraphics2D} context
	 * @param svg
	 */
	public void draw(SVGGraphics2D svg)
	{
		Font OrigFont = svg.getFont();
		svg.setFont(LabelFont);
		svg.drawString(Label, Position.x, Position.y);
		svg.setFont(OrigFont);
	}
}
