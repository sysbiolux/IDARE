package idare.imagenode.Data.BasicDataTypes.itemizedData;

import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.internal.ColorManagement.ColorMap;
import idare.imagenode.internal.ColorManagement.ColorUtils;
import idare.imagenode.internal.Utilities.LayoutUtils;
import idare.imagenode.internal.Utilities.LegendLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGGraphics2D;

/**
 * The {@link AbstractItemContainerLayout} provides basic features for any itemized container layout, such as shape placements etc.
 * @author Thomas Pfau
 *
 */
public abstract class AbstractItemContainerLayout extends ContainerLayout {
	private static final long serialVersionUID = 1001;
	//Storage for values used in every layout process, to avoid recalculations.
	int rows;
	int cols;
	private HashMap<Integer,ShapePosition> DisplayNodePositions;
	private HashMap<Integer,ShapePosition> LegendNodePositions;
	private LegendLabel LabelForDataSet;
	private Point legendOffset;
	private Rectangle LegendFrame;
	private Point upperright;

	/**
	 * Basic constructor
	 */
	public AbstractItemContainerLayout()
	{
		//init the datastructures
		DisplayNodePositions = new HashMap<Integer, ShapePosition>();
		LegendNodePositions = new HashMap<Integer, ShapePosition>();
	}
	/**
	 * Generate a {@link ShapePosition} from a Rectangle and a given Shape.
	 * The Shape must be compatible with BATIK or implement its own {@link DefaultExtensionHandler}. see
	 * https://xmlgraphics.apache.org/batik/using/svg-generator.html
	 * @param currentShape the shape to be used for this {@link ShapePosition}
	 * @param position the position the shape should occupy
	 * @return The generated ShapePosition
	 */
	protected ShapePosition getShapePosition(Shape currentShape, Rectangle2D position)
	{
		return new ShapePosition(currentShape, position);	
	}
	/**
	 * Create the Positions used in the layout.
	 * In essence, this function will distribute the available area to fit all the items 
	 * (according to getValueCount())
	 * @param data the {@link ItemNodeData} to use for layout generation
	 * @param AreaAndPosition The area available for the layout
	 * @param DataSetLabel The Label of the Dataset for the Legend
	 * @param storage a Map to store the Shapelocations to
	 * @param Legend indication whether we are creating a legend layout of a non legend layout.
	 */
	private void createLayoutPositions(ItemNodeData data, Rectangle AreaAndPosition, String DataSetLabel, HashMap<Integer,ShapePosition> storage, boolean Legend)
	{
		
		//Now, we can simply get the row width and column width by dividing the Rectangle width and height
		double rowheight = (double)AreaAndPosition.height / rows;
		double colwidth = (double)AreaAndPosition.width / cols;		
		
		int currentrowpos = 0;
		int currentcolpos = 0;
		int itemid = 1;
		for(int i = 0; i < data.getValueCount(); i++)
		{
			if(data.isValueSet(i))
			{
				//for each item get the appropriate rectangle
				Rectangle2D currentpos = new Rectangle2D.Double((currentcolpos) * colwidth,
						(currentrowpos) * rowheight, colwidth,rowheight);
				//if we are generating the legend layout, we have to build legendshapes (which contain the lael for the item) 
				if(!Legend)
				{
					Shape currentShape = getShape(new Dimension((int)currentpos.getWidth(),(int)currentpos.getHeight()));
					storage.put(itemid, getShapePosition(currentShape, currentpos));
				}
				else
				{						
					ShapePosition currentShape = getLegendShape(currentpos, Integer.toString(itemid));
					storage.put(itemid, currentShape );
				}
				itemid++;
			}
			//adjust the position in the grid.
			currentcolpos++;
			if(currentcolpos % cols == 0)
			{
				currentcolpos = 0;
				currentrowpos++;
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.ContainerLayout#createLayout(idare.imagenode.Interfaces.DataSets.NodeData, java.awt.Rectangle, java.lang.String)
	 */
	@Override
	public void createLayout(NodeData data, Rectangle AreaAndPosition, String DataSetLabel) {
		//first, determine, whether we have a container with fixed sizes, or a container with flexible sizes
		ItemNodeData ndata = (ItemNodeData) data;
		upperright = new Point(AreaAndPosition.x,AreaAndPosition.y);
		//get the rows and columns to use.
		if(data.getDataSet().getLayoutContainer().getLocalisationPreference().Flexible)
		{
			calcRowsAndCols(AreaAndPosition.width, AreaAndPosition.height,ndata.getValueCount());
		}
		else
		{
			rows = data.getDataSet().getLayoutContainer().getMinimalSize().height;
			cols = data.getDataSet().getLayoutContainer().getMinimalSize().width;
		}
		//create Layout for normal node
		createLayoutPositions(ndata, AreaAndPosition, DataSetLabel, DisplayNodePositions, false);
		
		//Set up the Legend DataSet label and Legend Frame 
		int FrameOffset = 1;
		LegendFrame = new Rectangle(AreaAndPosition.x+FrameOffset,AreaAndPosition.y+FrameOffset,AreaAndPosition.width-2*FrameOffset,AreaAndPosition.height-2*FrameOffset);
		Font LegendLabelFont = new Font(Font.MONOSPACED,Font.BOLD,Math.max(20,(int)Math.min((LegendFrame.getWidth() * 0.1),(LegendFrame.getHeight() * 0.1))));
		FontMetrics fm = LayoutUtils.getSVGFontMetrics(LegendLabelFont);
		int LabelWidth = fm.stringWidth(DataSetLabel);
		int LabelHeight = fm.getAscent();		
		int LabelOffSet = 2;
		int BaseLinePositionx =  FrameOffset + LabelOffSet; // one Pixel to be set off from the border, one 
		int BaseLinePositiony =  FrameOffset + LabelOffSet + LabelHeight;
		LabelForDataSet = new LegendLabel(LegendLabelFont, new Point(BaseLinePositionx,BaseLinePositiony), DataSetLabel);
		
		
		//Defien the legend Rectangle. This is dependent on the shape of the area to plot in.
		Rectangle LegendRectangle;
		if(LegendFrame.getWidth() > LegendFrame.getHeight())
		{
			//move the area to plot the data to the right
			
			legendOffset = new Point(LabelWidth + 2* FrameOffset + 2* LabelOffSet, 2* FrameOffset);
			LegendRectangle = new Rectangle(0,0,LegendFrame.width - legendOffset.x - FrameOffset, LegendFrame.height - legendOffset.y - FrameOffset );
			//LegendRectangle = new Rectangle(LegendFrame.x+2*LabelOffSet+LabelWidth,LegendFrame.y+FrameOffset,LegendFrame.width-3*LabelOffSet-LabelWidth,LegendFrame.height-2*FrameOffset);
		}
		else
		{
			legendOffset = new Point(2*FrameOffset, 2*FrameOffset + 2 * LabelOffSet + fm.getHeight());
			
			LegendRectangle = new Rectangle(0,0,LegendFrame.width - legendOffset.x - FrameOffset, LegendFrame.height - legendOffset.y - FrameOffset );
			
			//LegendRectangle = new Rectangle(LegendFrame.x+2*LabelOffSet,LegendFrame.y+FrameOffset + fm.getHeight(),LegendFrame.width-3*LabelOffSet,LegendFrame.height-2*FrameOffset - fm.getHeight());
			
		}
		createLayoutPositions(ndata, LegendRectangle, DataSetLabel, LegendNodePositions, true);

	}

	/**
	 * Get the Shape to be used for individual items in the specified area.
	 * @param Area The size of the shape to be returned.
	 * @return A Shape with appropriate dimension
	 */
	public abstract Shape getShape(Dimension Area);
	/**
	 * Get the ShapePosition representing the current Legend using the label indicated, and the Dimension provided.
	 * @param Area Area to put the legend shape into
	 * @param itemid The Label for the {@link ShapePosition}.
	 * @return A shapeposition, that can draw itself using the provided area and d
	 */
	public abstract ShapePosition getLegendShape(Rectangle2D Area, String itemid);
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.ContainerLayout#LayoutDataForNode(idare.imagenode.Interfaces.DataSets.NodeData, org.apache.batik.svggen.SVGGraphics2D, boolean, idare.imagenode.internal.ColorManagement.ColorMap)
	 */
	@Override
	public void LayoutDataForNode(NodeData data, SVGGraphics2D context, boolean Legend, ColorMap colors) {
		//Move to position of this container.
		context.translate(upperright.x, upperright.y);
		ItemNodeData ndata = (ItemNodeData) data;
		if(Legend)
		{
			LayoutDataForLegendNode(ndata, context, colors);
		}
		else
		{
			LayoutDataForImageNode(ndata, context, colors);
		}
		//Return to origin.
		context.translate(-upperright.x, -upperright.y);
		
	}
	
	/**
	 * plot the information to a standard node (not a lgend)
	 * @param ndata  the data to use for color indication
	 * @param context the graphics context to plot to
	 * @param colors the Colormap to use.
	 */
	private void LayoutDataForImageNode(ItemNodeData ndata, SVGGraphics2D context, ColorMap colors)
	{
		int itemid = 1;
		
		for(int i = 0; i < ndata.getValueCount(); i++)
		{
			if(ndata.isValueSet(i))
			{
				Color ccolor = colors.getColor(ndata.getData(i).getValue());
				ShapePosition currentitem = DisplayNodePositions.get(itemid);
				currentitem.draw(context, ccolor);				
				itemid++;
			}
		}

	}
	/**
	 * Plot the information to a Legend node 
	 * @param ndata the data to use for color indication
	 * @param context the graphics context to plot to
	 * @param colors the Colormap to use.
	 */
	private void LayoutDataForLegendNode(ItemNodeData ndata, SVGGraphics2D context, ColorMap colors)
	{
		int itemid = 1;
		Stroke currentstroke = context.getStroke();
		//Set up the stroke for Frame and Label
		context.setStroke(new BasicStroke(2));
		context.setColor(Color.black);
		context.draw(new Rectangle2D.Double(1,1,LegendFrame.width, LegendFrame.height));		
		context.setStroke(currentstroke);
		LabelForDataSet.draw(context);
		//Move tot the plotting area of the items
		context.translate(legendOffset.x, legendOffset.y);		
		for(int i = 0; i < ndata.getValueCount(); i++)
		{
			if(ndata.isValueSet(i))
			{
				Color ccolor = colors.getColor(ndata.getData(i).getValue());
				ShapePosition currentitem = LegendNodePositions.get(itemid);
				currentitem.draw(context, ccolor);
				itemid++;
			}
		}
		//And return to the original position.
		context.translate(-legendOffset.x, -legendOffset.y);
		
	}
	/**
	 * Calc the rows and columns used for layouting, based on width heights and the number of items.
	 * @param width the available width
	 * @param height the available height
	 * @param itemcount the number of items to distribute.
	 */
	private void calcRowsAndCols(int width, int height, int itemcount)
	{

		int x = Math.min(height,width);
        int y = Math.max(height,width);        
        double maxside = Math.sqrt((double)((x*y) / itemcount));
        //System.out.println("x: " + x + " y: " + y + " maxside:" + maxside );
		
        int fitx = (int)Math.ceil(x / maxside);
        int fity = (int)Math.ceil(y / maxside);
        int xsize = x / fitx;
        int ysize = y / fity;
        // now we check the larger of ysize and xsize whether it fits
        // all items
        int bigsize = Math.max(ysize,xsize);
        int smallsize = Math.min(ysize,xsize);
        int size;
        if((x/bigsize) * (y/bigsize) >= itemcount)
            size = bigsize;
        else
            size = smallsize;
        
        int ntemp;
        int mtemp;
        if(size == ysize)
        {
            ntemp = Math.min((y/size),itemcount);
            mtemp = (itemcount + ntemp -1) / ntemp; // this is kind of a hack for ceil itemcount/ntemp. Since we add ntemp -1 we will get the ceil as integer division alwaysrounds down otherwise.
        }
        else
        {
            mtemp = Math.min((x/size),itemcount);
            ntemp = (itemcount + mtemp -1 ) /mtemp; // this is kind of a hack for ceil itemcount/mtemp. 
        }
        rows = ntemp;
        cols = mtemp;
        //and now adjust according to width and height.
        int tempm = rows;
        int tempn = cols;
        if(height > width)
        {
            rows = Math.max(tempn,tempm);
            cols = Math.min(tempn,tempm);
        }
        else
        {
            cols = Math.max(tempn,tempm);
            rows = Math.min(tempn,tempm);
        }          
	}
	/**
	 * A Shapeposition combines a shape with positioning information.
	 * When drawing it, it will translate to that position and draw the shape at that position.
	 * @author Thomas Pfau
	 *
	 */
	public class ShapePosition implements Serializable
	{
		protected Shape shape;
		protected Rectangle2D position;
		/***
		 * A Basic constructor using a shape and a Rectangle as position.
		 * @param shape
		 * @param position
		 */
		public ShapePosition(Shape shape, Rectangle2D position)
		{
			this.shape = shape;
			this.position = position;
		}
		/**
		 * Draw the shape in the given {@link SVGGraphics2D} context with the provided color
		 * @param context the context to draw in
		 * @param fillcolor the {@link Color} to use to fill the shape.
		 */
		public void draw(SVGGraphics2D context, Color fillcolor)
		{
			context.translate(position.getX(), position.getY());
			context.setPaint(fillcolor);
			context.fill(shape);
			context.setPaint(ColorUtils.getContrastingColor(fillcolor));
			context.draw(shape);
			//Move to the position of the Label.
			context.translate(-position.getX(), -position.getY());									
		}
		
	}

	/**
	 * A {@link LegendShapePosition} extends a {@link ShapePosition} by additionally drawing a Label in the center of the shape.
	 * @author Thomas Pfau
	 *
	 */
	public class LegendShapePosition extends ShapePosition 
	{		
		private Point2D FontPosition;
		private String ItemID;
		private Font labelFont;
		/**
		 * Create a {@link LegendShapePosition}. In additon to the shape and Position information for the ShapePosition,  
		 * we need the label, the position of the label and the labelfont.
		 * THe labelposition is necessary to avoid recalculations during every plot. 
		 * @param shape The shape to use
		 * @param position the position at which the shape will be plotted
		 * @param ItemID the LAbel used 
		 * @param FontPosition the postition the label will be plotted at
		 * @param labelfont The font used for the label.
		 */
		public LegendShapePosition(Shape shape, Rectangle2D position, String ItemID, Point2D FontPosition, Font labelfont)
		{
			super(shape,position);
			this.FontPosition = FontPosition;
			this.ItemID = ItemID;
			labelFont = labelfont;
		}
		/**
		 * Draw the shape at the specified position and then adding the label above it.
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
