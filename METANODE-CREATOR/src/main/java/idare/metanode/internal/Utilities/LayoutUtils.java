package idare.metanode.internal.Utilities;

import idare.metanode.internal.Debug.PrintFDebugger;
import idare.metanode.internal.Properties.LabelGenerator;
import idare.metanode.internal.Properties.METANODEPROPERTIES;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;
import java.util.Vector;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
/**
 * Class that provides several utility function to handle SVG Objects (like DOM initialization etc pp
 * @author Thomas Pfau
 *
 */
public class LayoutUtils {
	/**
	 * Create an SVG Document using a default DOM implementation.
	 * @return An SVG Document with default settings
	 */
	public static SVGDocument createSVGDoc()
	{
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
	    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
	    SVGDocument doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
	    return doc;
	    
	}
	
	public static FontMetrics getSVGFontMetrics(Font font)
	{
		SVGDocument doc = createSVGDoc();			
	    SVGGraphics2D svg = new SVGGraphics2D(doc);	
		svg.setFont(font);
		return svg.getFontMetrics();
	}
	
	
	/**
	 * Transfer data that was writen to an {@link SVGGraphics2D} context to a document using a specified canvasdimension.
	 * @param doc - the {@link SVGDocument} to write to.
	 * @param canvasdimension - the target canvas dimension (can be <code>null</code>, in which instance default sizes are used (400/290)
	 * @param g
	 */
	public static void TransferGRaphicsToDocument(SVGDocument doc, Dimension canvasdimension, SVGGraphics2D g )
	{		
		if(canvasdimension == null)
		{
			canvasdimension = new Dimension(METANODEPROPERTIES.IMAGEWIDTH,METANODEPROPERTIES.IMAGEHEIGHT+METANODEPROPERTIES.LABELHEIGHT);
		}
		g.setSVGCanvasSize(new Dimension(canvasdimension.width,canvasdimension.height));
		Element root = doc.getDocumentElement();
		g.getRoot(root);
		PrintFDebugger.Debugging(root, "Setting property viewbox to: " + "0 0 "+ METANODEPROPERTIES.IMAGEWIDTH +" " + (METANODEPROPERTIES.IMAGEHEIGHT+METANODEPROPERTIES.LABELHEIGHT));
		root.setAttribute("viewBox", "0 0 "+ METANODEPROPERTIES.IMAGEWIDTH +" " + (METANODEPROPERTIES.IMAGEHEIGHT+METANODEPROPERTIES.LABELHEIGHT) );		
	}
	
	/**
	 * Scale a Font so that a given label fits into a specific dimension in an SVG context. 
	 * @param dim - the dimension the label should fit in.
	 * @param font - the font to scale.
	 * @param label - the String that should fit into the dimension. 
	 * @return the font scaled to the provided Dimension using a default svg graphics context
	 */
	public static Font scaleSVGFont(Dimension dim, Font font, String label) {		
		float fontSize = font.getSize();
		FontMetrics fm = getSVGFontMetrics(font);
	    int width = fm.stringWidth(label);
	    int height = fm.getHeight();	    
	    fontSize = (float)Math.floor(Math.min((float)(dim.width / (float)width ) * fontSize,(float)(dim.height/ (float) height) * fontSize)) ;
	    return font.deriveFont(fontSize);
	}
	
	/**
	 * Scale a Font so that a given label fits into a specific dimension. 
	 * @param dim - the dimension the label should fit in.
	 * @param font - the font to scale.
	 * @param g - the graphics context to use
	 * @param label - the String that should fit into the dimension. 
	 * @return the font scaled to the provided Dimension
	 */
	public static Font scaleFont(Dimension dim, Font font, Graphics g, String label) {		
		float fontSize = font.getSize();
	    int width = g.getFontMetrics(font).stringWidth(label);
	    int height = g.getFontMetrics(font).getHeight();	    
	    fontSize = (float)Math.floor(Math.min((float)(dim.width / (float)width ) * fontSize,(float)(dim.height/ (float) height) * fontSize)) ;
	    return font.deriveFont(fontSize);
	}
	
	/**
	 * Get alphanumeric unique labels for a set of entries (i.e. for labeling items or similar things)
	 * This function provides the same labels when called with the same entries 
	 * @param entries - Comparable objects (not necessarily of the same type)
	 * @return a Mapping of the provided objects to alphanumeric labels.
	 */
	public static HashMap<Comparable,String> getLabelsForData(Vector<Comparable> entries)
	{
		LabelGenerator lab = new LabelGenerator();
		HashMap<Comparable,String> Labels = new HashMap<Comparable, String>();
		for(Comparable comp : entries)
		{
			Labels.put(comp,lab.getLabel());
		}
		return Labels;
	}

}
