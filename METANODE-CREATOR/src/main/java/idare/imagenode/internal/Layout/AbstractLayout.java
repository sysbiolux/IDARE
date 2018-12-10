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
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent;

public abstract class AbstractLayout implements ImageNodeLayout {

	private Dimension imageDimension;
	private boolean printLabel;
	
	/**
	 * Default layout, with default sizes, printing the label.
	 */
	public AbstractLayout()
	{
		int width = IMAGENODEPROPERTIES.IMAGEWIDTH;
		int height = IMAGENODEPROPERTIES.IMAGEHEIGHT;
		printLabel = true;
		imageDimension = new Dimension(width, height);
	}	
	@Override
	public abstract boolean isValid();

	@Override
	public void writeLayout(ObjectOutputStream os) throws IOException
	{
		os.writeObject(imageDimension);
		os.writeBoolean(printLabel);		
	}

	@Override
	public boolean readLayout(DataSetManager dsm, ObjectInputStream os, Object currentobject) throws IOException {
		// TODO Auto-generated method stub
		
		if(currentobject instanceof Dimension)
		{
			imageDimension = (Dimension) currentobject;
			printLabel = os.readBoolean();
			return true;
		}
		else
		{
			return false;
		}

	}
	

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
	
	@Override
	public abstract void datasetChanged(DataSetChangedEvent e);	

	@Override
	public abstract void datasetsChanged(DataSetsChangedEvent e);
	
	@Override
	public Dimension getImageSize() {
		return imageDimension;		
	}

	@Override
	public boolean imageIncludesLabel() {
		// TODO Auto-generated method stub
		return printLabel;
	}

	@Override
	public void setImageDimension(Dimension imageDimension)
	{
		this.imageDimension = imageDimension;
	}
		
	@Override
	public void  setImageIncludesLabel(boolean includeLabel)
	{
		printLabel = includeLabel;
	}
	
	
	/**
	 * Lay out the legend for a specific set of node data
	 * @param svg the {@link SVGGraphics2D} to draw in
	 * @param identifier the identifier to draw
	 */
	public void drawIdentifier(SVGGraphics2D svg, String identifier)
	{
		Font currentFont = svg.getFont();		
		svg.setFont(LayoutUtils.scaleFont(new Dimension(IMAGENODEPROPERTIES.IMAGEWIDTH, IMAGENODEPROPERTIES.LABELHEIGHT),IMAGENODEPROPERTIES.IDFont, svg, identifier));
		svg.setColor(Color.black);		
		FontMetrics fm = svg.getFontMetrics();		
		Rectangle2D bounds = fm.getStringBounds(identifier, svg);		
		int xpos = (int) ((IMAGENODEPROPERTIES.IMAGEWIDTH - bounds.getWidth())/2);		
		int ypos = IMAGENODEPROPERTIES.IMAGEHEIGHT + fm.getAscent();
		svg.drawString(identifier, xpos, ypos);
		svg.setFont(currentFont);
	}

	
	/**
	 * Get the Dimension of the object displayed on the nodes in a network.
	 * @return The dimensions of the object in the network view.
	 */
	public Dimension getDisplayDimensions()
	{
		Dimension imageDim = new Dimension();
		Double width = imageDimension.getWidth()*IMAGENODEPROPERTIES.IDARE_DISPLAY_SIZE_FACTOR;
		Double height = 0.;
		if(printLabel)
		{
			height = imageDimension.getHeight()*IMAGENODEPROPERTIES.IDARE_DISPLAY_SIZE_FACTOR + IMAGENODEPROPERTIES.LABELHEIGHT*IMAGENODEPROPERTIES.IDARE_DISPLAY_SIZE_FACTOR;
		}
		else
		{
			height = imageDimension.getHeight()*IMAGENODEPROPERTIES.IDARE_DISPLAY_SIZE_FACTOR;
		}
		imageDim.setSize(width, height);
		return imageDim;
	}

}
