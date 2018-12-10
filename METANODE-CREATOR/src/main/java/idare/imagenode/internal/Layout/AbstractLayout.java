package idare.imagenode.internal.Layout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Vector;

import org.apache.batik.svggen.SVGGraphics2D;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.Utilities.StringDrawer;
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

public abstract class AbstractLayout implements ImageNodeLayout {
	public int imageHeight;
	public int imageWidth;
	public int labelHeight;	
	/**
	 * Default Constructor with predefined heights and width.
	 * @param ImageHeight the height of the images
	 * @param ImageWidth the width of the images
	 * @param LabelHeight the height of the label.
	 */
	public AbstractLayout(int ImageHeight, int ImageWidth, int LabelHeight)
	{
		this.imageHeight = ImageHeight;
		this.imageWidth = ImageWidth;
		this.labelHeight = LabelHeight;
	}
	@Override
	public abstract boolean isValid();

	@Override
	public abstract void writeLayout(ObjectOutputStream os) throws IOException;
	
	@Override
	public abstract boolean readLayout(DataSetManager dsm, ObjectInputStream os, Object currentobject) throws IOException;

	@Override
	public abstract void doLayout() throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException,
			WrongDatasetTypeException;
	@Override
	public abstract String getDataSetLabel(DataSetLink ds);

	@Override
	public abstract ColorMap getColorsForDataSet(DataSetLink ds);

	@Override
	public abstract Vector<? extends DataSetLink> getDatasetsInOrder();

	@Override
	public abstract void layoutNode(Collection<NodeData> datacollection, SVGGraphics2D svg);
	
	@Override
	public abstract void layoutLegendNode(Collection<NodeData> datacollection, SVGGraphics2D svg);
	
	
	/**
	 * Lay out the legend for a specific set of node data
	 * @param svg the {@link SVGGraphics2D} to draw in
	 * @param identifier the identifier to draw
	 */
	public void drawIdentifier(SVGGraphics2D svg, String identifier, boolean printLabel)
	{		
		if(!printLabel)
		{
			//do nothing
			return;
		}
		StringDrawer drawer = new StringDrawer(identifier);
		PrintFDebugger.Debugging(this, "Drawing ID for: " + identifier);
		drawer.drawInArea(getImageHeight(), getImageWidth(), getLabelHeight(), svg);
		/*Font currentFont = svg.getFont();		
		svg.setFont(LayoutUtils.scaleFont(new Dimension(getImageWidth(), getLabelHeight()),IMAGENODEPROPERTIES.IDFont, svg, identifier));
		svg.setColor(Color.black);		
		FontMetrics fm = svg.getFontMetrics();		
		Rectangle2D bounds = fm.getStringBounds(identifier, svg);		
		int xpos = (int) ((getImageWidth() - bounds.getWidth())/2);		
		int ypos = getImageHeight() + Math.max(fm.getAscent(), (getLabelHeight()-(fm.getAscent()+fm.getDescent()))/2 + fm.getAscent());			
		svg.drawString(identifier, xpos, ypos);
		svg.setFont(currentFont);*/
	}
	
	
	/**
	 * Set the image height (only the image not the label);
	 * @param height
	 */
	public void setImageHeight(int height)
	{
		this.imageHeight = height;
	}
	
	/**
	 * Get the image height (only the image not the label);
	 * @return The height of the image
	 */
	public int getImageHeight()
	{
		return this.imageHeight;
	}
	
	/**
	 * Set the image  width (only the image not the label);
	 * @param width
	 */
	public void setImageWidth(int width)
	{
		this.imageWidth = width;
	}
	
	/**
	 * Get the image width (only the image not the label);
	 * @return The width of the image
	 */
	public int getImageWidth()
	{
		return this.imageWidth;
	}
	
	/**
	 * Set the image dimensions (only the image not the label);
	 * @param dim The dimensions of the image 
	 */
	public void setImageDimensions(Dimension dim)
	{
		this.imageHeight = dim.height;
		this.imageWidth = dim.width;
	}
	
	/**
	 * Get the image dimensions (only the image not the label);
	 * @return The dimensions of the image 
	 */
	public Dimension getImageDimensions()
	{
		return new Dimension(this.imageWidth, this.imageHeight);
	}
	
	
	/**
	 * Set the label height 
	 * @param height The height of the label 
	 */
	public void setLabelHeight(int height)
	{
		this.labelHeight = height;
	}
	
	/**
	 * Get the label height 
	 * @return The height of the label 
	 */
	public int getLabelHeight()
	{
		return this.labelHeight;
	}
	
}
