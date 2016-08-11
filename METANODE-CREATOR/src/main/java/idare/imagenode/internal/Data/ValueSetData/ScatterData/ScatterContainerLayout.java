package idare.imagenode.internal.Data.ValueSetData.ScatterData;

import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetContainerLayout;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetDataSet;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetDataValue;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetNodeData;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.internal.ColorManagement.ColorMap;
import idare.imagenode.internal.Utilities.LayoutUtils;
import idare.imagenode.internal.Utilities.LegendLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import org.apache.batik.svggen.SVGGraphics2D;
/**
 * ContainerLayout Class for Graph Data, storing common information like the location fo axes, labels and frames.
 * @author Thomas Pfau
 *
 */
public class ScatterContainerLayout extends ValueSetContainerLayout {
	private static final long serialVersionUID = 1001;
	protected Path2D LegendAxes;
	protected Path2D NodeAxes;
	protected Line2D ZeroLine;
	protected Line2D LegendZeroLine;
	protected Rectangle2D LegendFrame;
	protected LegendLabel LabelForDataSet; 
	protected LegendLabel YMinval;
	protected LegendLabel YMaxval;
	protected LegendLabel YZeroval;
	protected Vector<LegendLabel> XLabels = new Vector<LegendLabel>(); 	
	protected Double yminval;
	protected Double ymaxval;
	protected Double yrange;
	protected Double xminval;
	protected Double xmaxval;
	protected Double xrange;
	protected Rectangle2D LineArea;
	protected Rectangle2D LegendLineArea;
	protected HashMap<Comparable,LabelAndPosition> itemxPositions = new HashMap<Comparable, ScatterContainerLayout.LabelAndPosition>();
	protected int labelSize;
	protected boolean numericHeaders = false;
	final static float dash1[] = {10.0f};
	final static BasicStroke dashedStroke =
			new BasicStroke(1.0f,
					BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER,
					10.0f, dash1, 0.0f);

	protected HashMap<String,Vector<Double>> LineHeaders = new HashMap<String, Vector<Double>>();
	/**
	 * Default Constructor
	 */
	 public ScatterContainerLayout(int LabelSize) {
		 super();
		 labelSize = LabelSize;
		 // TODO Auto-generated constructor stub
	 }
	 @Override
	 public void createLayout(NodeData data, Rectangle area, String DataSetLabel) {
		 ValueSetDataSet dataset = (ValueSetDataSet)data.getDataSet();
		 
		 //obtain the limits from the original Dataset
		 Double[] valuerange = dataset.getYAxisLimits();
		 Double[] displayrange = determineDisplayRange(valuerange);
		 yminval = displayrange[0];
		 ymaxval = displayrange[1];
		 yrange = displayrange[1] - displayrange[0];
		 xminval = Double.MAX_VALUE;
		 xmaxval = Double.MIN_VALUE;
		 if(dataset.numericheaders)
		 {
			 numericHeaders = true;
			 for(String Name : ((ValueSetDataSet)data.getDataSet()).getSetNames())
			 {

				 //Get the minimal and maximal x values. for plotting
				 Vector<Double> LineXValues = new Vector<Double>();
				 Vector<Comparable> Headers = ((ValueSetDataSet)data.getDataSet()).getHeadersForSheet(Name);
				 for(Comparable comp : Headers)
				 {
					 Double current = (Double) comp;
					 LineXValues.add(current);
					 xmaxval = Math.max(xmaxval, current);
					 xminval = Math.min(xminval, current);

				 }				
				 LineHeaders.put(Name, LineXValues);
			 }		 

		 }
		 else
		 {		
			 int i = 0;
			 xminval = 0.;
			 Vector<Comparable> headers = dataset.getHeaders();
			 HashMap<Comparable,String> HeaderLabels = LayoutUtils.getLabelsForData(headers);			 
			 for(Comparable header : headers)
			 {				 
				 System.out.println(header);
				 itemxPositions.put(header,new LabelAndPosition(0.5 + i, HeaderLabels.get(header)));
				 i++;
			 }
			 xmaxval = (double)i;
		 }
		 
		 
		 xrange = xmaxval - xminval;
		 if(numericHeaders)
		 {
			 //make some room at the edges.
			 xmaxval = xmaxval + 0.05 * xrange;
			 xminval = xminval - 0.05 * xrange;
			 xrange = xmaxval - xminval;
		 }
		 if(xrange == 0)
		 {
			 xmaxval = xmaxval + 0.1;
			 xminval = xminval - 0.1;
		 }
		 calcNormalLayoutpositions(area);
		 calcLegendLayoutPositions(area, DataSetLabel);
	 }
	 /**
	  * Calc the localisatin of default positions (LineArea, Axes)
	  * @param area - the area in which to plot the data.
	  */
	 private void calcNormalLayoutpositions(Rectangle area)
	 {
		 double xdrawMin = area.getX() + 2;
		 double xdrawMax = area.getX() + area.getWidth();
		 double xdrawRange = xdrawMax - xdrawMin;
		 double ydrawMin = area.getY();
		 double ydrawMax = area.getY() + area.getHeight() -2;
		 double ydrawRange = ydrawMax - ydrawMin;
		 //Leave a little margin for the area to allow for axes 
		 LineArea = new Rectangle2D.Double(xdrawMin+1,ydrawMin+1,xdrawRange-2,ydrawRange-2);
		 NodeAxes = new Path2D.Double();
		 NodeAxes.append(new Line2D.Double(new Point2D.Double(area.getX()+1,ydrawMin),new Point2D.Double(area.getX()+1,ydrawMin +area.getHeight()-1)), false);		
		 NodeAxes.append(new Line2D.Double(new Point2D.Double(area.getX()+1,ydrawMin + area.getHeight()-1),new Point2D.Double(xdrawMax,ydrawMin + area.getHeight()-1)), true);
		 if(ymaxval > 0 & yminval < 0)
		 {
			 double zerofraction = ymaxval/yrange;
			 double zerolineypos = ydrawMax - ydrawRange + zerofraction * ydrawRange ; 
			 ZeroLine = new Line2D.Double(xdrawMin,zerolineypos,xdrawMax,zerolineypos);
		 }
	 }
	 /**
	  * Calculate the Default positions (Labelpositions, Frame, Axes) for the Legendlayout.
	  * @param area - the area for plotting
	  * @param DataSetLabel - the label used for the Dataset
	  */
	 private void calcLegendLayoutPositions(Rectangle area,String DataSetLabel)
	 {
		 int FrameOffset = 1;
		 int FrameWidth = 2;
		 //Define the legend Frame
		 LegendFrame = new Rectangle.Double(area.getX()+FrameWidth/2,
				 area.getY()+FrameWidth/2,
				 area.getWidth()-FrameWidth,
				 area.getHeight()-FrameWidth);


		 //Define the new Area remaining for displaying the legend data
		 Rectangle2D newArea = new Rectangle2D.Double(FrameWidth,
				 FrameWidth,
				 area.getWidth()-2*FrameWidth,
				 area.getHeight()-2* FrameWidth);


		 Font LegendLabelFont = new Font(Font.MONOSPACED,Font.BOLD,Math.max(20,(int)Math.min((LegendFrame.getWidth() * 0.1),(LegendFrame.getHeight() * 0.1))));

		 //Determine the Label properties
		 FontMetrics fm = LayoutUtils.getSVGFontMetrics(LegendLabelFont);
		 int LabelWidth = fm.stringWidth(DataSetLabel);
		 int LabelHeight = fm.getAscent();		
		 int LabelOffSet = 2;
		 int BaseLinePositionx =  (int) (newArea.getWidth() - (LabelWidth + LabelOffSet)); // One pixel off from the upper right corner. 
		 int BaseLinePositiony =  (int) (LabelOffSet + LabelHeight);
		 LabelForDataSet = new LegendLabel(LegendLabelFont, new Point(BaseLinePositionx,BaseLinePositiony), DataSetLabel);
		 //Determine the labels for X and Y axis.
		 Double[] ylims = new Double[]{yminval,ymaxval}; 
		 HashMap<Double, String> ylabels = ColorMap.getDisplayStrings(ylims);


		 Font AxisLabelFont = new Font(Font.MONOSPACED,Font.BOLD,Math.max(12,(int)Math.min((newArea.getWidth() * 0.1),(newArea.getHeight() * 0.1))));	
		 
		 fm = LayoutUtils.getSVGFontMetrics(AxisLabelFont);

		 // place the y labels
		 // first, place the minimum value.
		 //this has to be placed at 2 * labeloffset + height of the text from the bottom.

		 int yminypos = (int)(newArea.getHeight() - (fm.getHeight() + 2 * fm.getDescent() + 2 * LabelOffSet));
		 int ymaxypos = LabelOffSet + fm.getAscent();

		 int xoffset = (int) FrameWidth + Math.max(fm.stringWidth(ylabels.get(ylims[0])),
				 fm.stringWidth(ylabels.get(ylims[1]))) 
				 + 2 * LabelOffSet;

		 YMinval = new LegendLabel(AxisLabelFont, new Point(xoffset - fm.stringWidth(ylabels.get(ylims[0])) - LabelOffSet,yminypos+fm.getAscent()/2), ylabels.get(ylims[0]));
		 YMaxval = new LegendLabel(AxisLabelFont, new Point(xoffset - fm.stringWidth(ylabels.get(ylims[1])) - LabelOffSet,ymaxypos), ylabels.get(ylims[1]));




		 // place the X Labels
		 int x_ypos =  (int)( newArea.getHeight() - LabelOffSet - fm.getDescent());		 
		 int xminxpos = xoffset + FrameOffset;
		 if(numericHeaders)
		 {
			 Double[] xlims = new Double[]{xminval,xmaxval}; 
			 HashMap<Double, String> xlabels = ColorMap.getDisplayStrings(xlims);
			 int xmaxxpos = area.width - (2*FrameOffset + LabelOffSet + fm.stringWidth(xlabels.get(xlims[1])));			 
			 XLabels.add(new LegendLabel(AxisLabelFont, new Point(xminxpos,x_ypos), xlabels.get(xlims[0])));
			 XLabels.add(new LegendLabel(AxisLabelFont, new Point(xmaxxpos,x_ypos), xlabels.get(xlims[1])));
		 }
		 else
		 {
			int xvalrange = area.width - xoffset - 3*FrameOffset; 
			for(Comparable comp : itemxPositions.keySet())
			{
				LabelAndPosition labpos = itemxPositions.get(comp);
				int cstringWidth = fm.stringWidth(labpos.label);
				double cxval = labpos.position;
				int cxpos = (int)((cxval / xrange) * xvalrange - cstringWidth/2) + xoffset;
				XLabels.add(new LegendLabel(AxisLabelFont, new Point(cxpos,x_ypos ), labpos.label));
			}
		 }
		 //Determine the Legend Axes.
		 LegendAxes = new Path2D.Double();
		 LegendAxes.append(new Line2D.Double(new Point(xoffset,2*FrameOffset),
				 new Point(xoffset,yminypos + fm.getDescent())), false);
		 LegendAxes.append(new Line2D.Double(new Point(xoffset,yminypos + fm.getDescent()), new Point(area.width-2*FrameOffset,yminypos + fm.getDescent())), true);

		 //And define the LegendLineArea
		 LegendLineArea = new Rectangle2D.Double(xoffset + FrameWidth,0,LegendFrame.getWidth() - xoffset - 2 *FrameWidth, yminypos + fm.getDescent() -1);



		 LabelWidth = fm.stringWidth(DataSetLabel);
		 if(ymaxval > 0 & yminval < 0)
		 {
			 //since top left is y = 0, we have to invert the zerofraction
			 double zerolineypos = getPlotPoint(0, 0, 20, LegendLineArea.getHeight()).getY(); 
			 LegendZeroLine = new Line2D.Double(xminxpos,zerolineypos,newArea.getWidth()-2*FrameOffset,zerolineypos);
			 int height = fm.getAscent();
			 double zerobaseline = zerolineypos + 0.5 * height;
			 YZeroval = new LegendLabel(AxisLabelFont, new Point(xoffset - fm.stringWidth("0")- FrameOffset  - LabelOffSet,(int)zerobaseline), "0");
		 }


	 }
	 /**
	  * Layout a legend node with the given NodeData, in the given graphics context with the given colormap.
	  * @param data
	  * @param g2d
	  * @param colors
	  */
	 private void LayoutLegendNode(NodeData data, SVGGraphics2D g2d, ColorMap colors)
	 {
		 Paint currentPaint = g2d.getPaint();
		 Stroke currentStroke = g2d.getStroke();
		 g2d.setPaint(Color.black);
		 g2d.setStroke(new BasicStroke(2));
		 //Draw standard comoponents
		 g2d.draw(LegendFrame);
		 g2d.translate(LegendFrame.getX(), LegendFrame.getY());		
		 g2d.draw(LegendAxes);
		 YMinval.draw(g2d);
		 for(LegendLabel lab : XLabels)
		 {
			 lab.draw(g2d);
		 }
		 YMaxval.draw(g2d);
		 //Draw the Zero Line if necessary.
		 if(LegendZeroLine != null)
		 {
			 g2d.setStroke(dashedStroke);
			 g2d.draw(LegendZeroLine);
			 YZeroval.draw(g2d);
		 }
		 //Plot the lines in the LegendLineArea
		 makePlot(data, g2d, LegendLineArea, colors);
		 //draw the Label after everything else...
		 LabelForDataSet.draw(g2d);
		 //restore the original graphics context
		 g2d.translate(-LegendFrame.getX(), -LegendFrame.getY());
		 g2d.setStroke(currentStroke);
		 g2d.setPaint(currentPaint);
	 }

	 /**
	  * 
	  * Layout a node with the given NodeData, in the given graphics context with the given colormap.
	  * @param data
	  * @param g2d
	  * @param colors
	  */
	 private void LayoutNode(NodeData data, SVGGraphics2D g2d, ColorMap colors)
	 {
		 Paint currentPaint = g2d.getPaint();
		 Stroke currentStroke = g2d.getStroke();
		 g2d.setPaint(Color.black);
		 g2d.setStroke(new BasicStroke(2));
		 g2d.draw(NodeAxes);		
		 if(ZeroLine != null)
		 {
			 g2d.setStroke(dashedStroke);
			 g2d.draw(ZeroLine);			
		 }
		 makePlot(data, g2d, LineArea,colors);
		 g2d.setStroke(currentStroke);
		 g2d.setPaint(currentPaint);
	 }
	 /**
	  * Plot the lines using the data from a node.
	  * @param data - the data to use
	  * @param g2d - the context to plot in
	  * @param area - the area available for plotting
	  * @param colors - the colors to use.
	  */
	 private void makePlot(NodeData data, SVGGraphics2D g2d, Rectangle2D area, ColorMap colors)
	 {
		 ValueSetNodeData nd = (ValueSetNodeData) data;		 
		 Paint origPaint = g2d.getPaint();
		 Stroke currentStroke = g2d.getStroke();
		 g2d.setStroke(new BasicStroke(3));
		 //move to the plotting area
		 g2d.translate(area.getX(), area.getY());
		 for(String name : ((ValueSetDataSet)data.getDataSet()).getSetNames())
		 {
			 ValueSetDataValue vsd = nd.getData(name);
			 if(vsd != null)
			 {
				 Vector<Double> LineYValues = vsd.getEntryData();
				 Color linecolor = colors.getColor(name);
				 g2d.setPaint(linecolor);
				 if(numericHeaders)
				 {
					 Vector<Comparable> LineXValues  = new Vector<Comparable>();
					 LineXValues.addAll(LineHeaders.get(name));
					 plotItems(name,LineXValues, LineYValues, area, g2d);
				 }
				 else
				 {
					 Vector<Comparable> lineHeaders = ((ValueSetDataSet)data.getDataSet()).getHeadersForSheet(name);
					 plotItems(name,lineHeaders,LineYValues,area,g2d);
				 }
				 
			 }						
		 }
		 g2d.translate(-area.getX(), -area.getY());
		 g2d.setStroke(currentStroke);
		 g2d.setPaint(origPaint);
	 }
	  
	 /**
	  * Plot the Items defined by the x and y values with a shape determined by the Sheetname on the provided graphics context.
	  * @param SheetName
	  * @param xvalues
	  * @param yvalues
	  * @param area
	  * @param g2d
	  * @return
	  */
	 private void plotItems(String SheetName, Vector<Comparable> xvalues, Vector<Double> yvalues, Rectangle2D area, SVGGraphics2D g2d)
	 {
		 Path2D result = new Path2D.Double();
		 boolean pathstarted = false; 
		 for(int i = 0; i < xvalues.size(); i++)
		 {
			 if(yvalues.get(i) != null)
			 {
				 Double val = yvalues.get(i);
				 Double coord;
				 if(numericHeaders)
				 {
					 coord = (Double)xvalues.get(i);
				 }
				 else
				 {
					 coord = itemxPositions.get(xvalues.get(i)).position;
				 }
				 Point2D loc = getPlotPoint(coord,val,area.getWidth(),area.getHeight());
				 Path2D marker = createMarker(SheetName, loc, labelSize);
				 g2d.draw(marker);
				 result.append(new Line2D.Double(loc,loc), false);
				 pathstarted = true;
			 }
		 }
	 }

	 
	 
	 /**
	  * Helper function to translate a x/y value pair to points in the plot area.
	  * @param xvalue
	  * @param yvalue
	  * @param width - plot area width
	  * @param height - plot area height
	  * @return
	  */
	 private Point2D getPlotPoint(double xvalue, double yvalue, double width, double height)
	 {
		 double xval = ((xvalue - xminval) / xrange) * width;
		 double yval = height - ((yvalue - yminval) / yrange) * height;
		 Point2D res = new Point2D.Double(xval,yval);
		 return res;

	 }

	 @Override
	 public void LayoutDataForNode(NodeData data, SVGGraphics2D context,
			 boolean Legend, ColorMap colors) {
		 // TODO Auto-generated method stub

		 if(Legend)
		 {
			 LayoutLegendNode(data,context, colors);
		 }
		 else
		 {
			 LayoutNode(data,context, colors);
		 }		
	 }
	 protected class LabelAndPosition implements Serializable
	 {

		private static final long serialVersionUID = 1L;
		public String label;
		 public Double position;
		 public LabelAndPosition(Double Position, String Label)
		 {
			 this.label = Label;
			 this.position = Position;
		 }
		 
	 }
	 /**
	  * Plot a Marker for the given ID 
	  * The choosen marker depends on the hash code of the itemID provided.
	  * @param itemID - the itemID to obtain the hash from
	  * @param center - the center of the marker
	  * @param graphics - the graphics to plot to.
	  * @param size - the size of the marker (total extension)
	  */

	 public static Path2D createMarker(String itemID, Point2D center, double size)
	 {
		 Path2D plotpath;		 
		 int itemHash = itemID.hashCode() % 5;
		 switch(itemHash)
		 {
		 	case 0: {
		 		plotpath = getStar(center.getX(), center.getY(), size);
		 		break;
		 	}
		 	case 1:
		 	{
		 		plotpath = getDiamond(center.getX(), center.getY(), size);
		 		break;
		 	}
		 	case 2:
		 	{
		 		plotpath = getPentagon(center.getX(), center.getY(), size);
		 		break;
		 	}
		 	case 3:
		 	{
		 		Rectangle2D rec = new Rectangle2D.Double(center.getX()-size/2.,center.getY()-size/2.,size,size);
		 		plotpath = new Path2D.Double(rec);
		 		break;
		 	}
		 	case 4:
		 	{
		 		Ellipse2D ell = new Ellipse2D.Double(center.getX()-size/2.,center.getY()-size/2.,size,size);
		 		plotpath = new Path2D.Double(ell);
		 		break;
		 	}
		 	default:
		 	{
		 		Rectangle2D rec = new Rectangle2D.Double(center.getX()-size/2.,center.getY()-size/2.,size,size);
		 		plotpath = new Path2D.Double(rec);
		 		break;
		 	}
		 		
		 }
		 return plotpath;
	 }	 	 	  
	 
	 /**
	  * Get a path for a star around a center with a given diameter
	  * @param centerx
	  * @param centery
	  * @param cross
	  * @return
	  */
	 private static Path2D getStar(double centerx, double centery, double cross)
	 {
		 Path2D starpath = new Path2D.Double();
		 double radius = cross/2.;
		 double radius2 = cross/4.;
		 double alpha = -1./10. * Math.PI ;
		 double alpha2 = 1./10. * Math.PI;		
		 starpath.append(new Line2D.Double(centerx + Math.cos(alpha)*radius,
				 centery +(Math.sin(alpha)*radius),										  
				 centerx + Math.cos(alpha2)*radius2,
				 centery +(Math.sin(alpha2)*radius2) ), false);
		 //alpha = 0.5 * Math.PI  + 2*Math.PI/5;
		 alpha += 2*Math.PI/5;
		 starpath.append(new Line2D.Double(centerx + Math.cos(alpha2)*radius2,
				 centery +(Math.sin(alpha2)*radius2) ,
				 centerx + Math.cos(alpha)*radius,
				 centery +(Math.sin(alpha)*radius)), true);
		 double oldalpha = alpha;

		 for(int i = 1; i < 5; i++)
		 {
			 alpha += 2*Math.PI/5;
			 alpha2 += 2*Math.PI/5;
			 starpath.append(new Line2D.Double(centerx + Math.cos(oldalpha)*radius,
					 centery +(Math.sin(oldalpha)*radius),
					 centerx + Math.cos(alpha2)*radius2,
					 centery +(Math.sin(alpha2)*radius2) ), true);
			 starpath.append(new Line2D.Double(centerx + Math.cos(alpha2)*radius2,
					 centery +(Math.sin(alpha2)*radius2) ,
					 centerx + Math.cos(alpha)*radius,
					 centery +(Math.sin(alpha)*radius) ), true);
			 oldalpha = alpha;
		 }
		 return starpath;
	 }
	 /**
	  * Get a path for a pentagon around a center with a given diameter
	  * @param centerx
	  * @param centery
	  * @param cross
	  * @return
	  */
	 private static Path2D getPentagon(double centerx, double centery, double cross)
	 {
		 Path2D starpath = new Path2D.Double();
		 double radius = cross/2.;
		 //rotate to the left.
		 double alpha = -0.5 * Math.PI ;
		 double oldalpha = alpha;
		 starpath.append(new Line2D.Double(centerx, centery-radius,
				 centerx + Math.cos(alpha)*radius,
				 centery +(Math.sin(alpha)*radius) ), false);
		 for(int i = 1; i <= 5; i++)
		 {
			 alpha = -0.5 * Math.PI + i * 2*Math.PI/5;					
			 starpath.append(new Line2D.Double(centerx + Math.cos(oldalpha)*radius,
					 centery +(Math.sin(oldalpha)*radius),
					 centerx + Math.cos(alpha)*radius,
					 centery +(Math.sin(alpha)*radius)),true);
			 oldalpha = alpha;
		 }
		 return starpath;
	 }
	 /**
	  * Get a path for a diamond around a center with a given diameter
	  * @param centerx
	  * @param centery
	  * @param cross
	  * @return
	  */
	 private static Path2D getDiamond(double centerx, double centery, double cross)
	 {
		 Path2D diamondpath = new Path2D.Double();
		 double radius = cross/2.;
		 //rotate to the left.
		 double alpha = -0.5 * Math.PI ;
		 double oldalpha = alpha;
		 diamondpath.append(new Line2D.Double(centerx, centery - radius,
				 centerx + radius,centery),false);
		 diamondpath.append(new Line2D.Double(centerx + radius,centery,
				 centerx,centery+ radius),true);
		 diamondpath.append(new Line2D.Double(centerx,centery+ radius,
				 centerx - radius ,centery),true);
		 diamondpath.append(new Line2D.Double(centerx - radius ,centery,
				 centerx, centery - radius),true);
		 return diamondpath;
	 }

	 
}

