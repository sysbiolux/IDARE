package idare.imagenode.internal.Layout.Manual.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.svg.SVGDocument;

import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.Utilities.GUI.JSVGGlassCanvas;
import idare.imagenode.internal.Debug.PrintFDebugger;

public class IDPanel extends JPanel {

	
	public IDPanel()
	{
		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());
	}
	
	public void setID(String ID)
	{
		this.removeAll();
		//create an SVG document.
		SVGDocument doc = LayoutUtils.createSVGDoc();
		SVGGraphics2D g = new SVGGraphics2D(doc);
		//layout the data in the SVGGraphics Context of this document.
//		PrintFDebugger.Debugging(this, "Drawing identifier " + ID);
		drawIdentifier(g, ID);
		LayoutUtils.TransferGraphicsToDocument(doc, this.getSize(), g);
		JSVGCanvas canvas = new JSVGGlassCanvas(this);
		canvas.setSVGDocument(doc);		
		this.add(canvas,BorderLayout.CENTER);
		revalidate();
		//canvas.revalidate();
	}
	
	/**
	 * Lay out the legend for a specific set of node data
	 * @param svg the {@link SVGGraphics2D} to draw in
	 * @param identifier the identifier to draw
	 */
	private void drawIdentifier(SVGGraphics2D svg, String identifier)
	{
		Font currentFont = svg.getFont();		
		svg.setFont(LayoutUtils.scaleFont(this.getSize(), IMAGENODEPROPERTIES.IDFont,svg, identifier));
		svg.setColor(Color.black);		
		FontMetrics fm = svg.getFontMetrics();		
		Rectangle2D bounds = fm.getStringBounds(identifier, svg);
		PrintFDebugger.Debugging(this, "The size of this panel is " + this.getSize());
		PrintFDebugger.Debugging(this, "The size of bounds are " + bounds);
		int xpos = (int) ((this.getSize().width - bounds.getWidth())/2);		
		int ypos = fm.getAscent();
		PrintFDebugger.Debugging(this, "Drawing identifier " + identifier + " at position " + xpos + "/" + ypos);	
		svg.drawString(identifier, xpos, ypos);
		svg.setFont(currentFont);
	}

	
}
