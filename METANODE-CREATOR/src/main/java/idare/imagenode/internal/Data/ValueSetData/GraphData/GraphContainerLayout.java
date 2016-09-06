package idare.imagenode.internal.Data.ValueSetData.GraphData;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetContainerLayout;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetDataSet;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetDataValue;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetNodeData;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.Utilities.LegendLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Vector;

import org.apache.batik.svggen.SVGGraphics2D;
/**
 * ContainerLayout Class for Graph Data, storing common information like the location fo axes, labels and frames.
 * @author Thomas Pfau
 *
 */
public class GraphContainerLayout extends ValueSetContainerLayout {
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
	protected LegendLabel XMinval;
	protected LegendLabel XMaxval;
	protected Double yminval;
	protected Double ymaxval;
	protected Double yrange;
	protected Double xminval;
	protected Double xmaxval;
	protected Double xrange;
	protected Rectangle2D LineArea;
	protected Rectangle2D LegendLineArea;	
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
	 public GraphContainerLayout() {
		 super();
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
		 xrange = xmaxval - xminval;
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
		 Double[] xlims = new Double[]{xminval,xmaxval}; 
		 HashMap<Double, String> xlabels = ColorMap.getDisplayStrings(xlims);
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
		 int xmaxxpos = area.width - (2*FrameOffset + LabelOffSet + fm.stringWidth(xlabels.get(xlims[1])));
		 XMinval = new LegendLabel(AxisLabelFont, new Point(xminxpos,x_ypos), xlabels.get(xlims[0]));
		 XMaxval = new LegendLabel(AxisLabelFont, new Point(xmaxxpos,x_ypos), xlabels.get(xlims[1]));
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
		 XMinval.draw(g2d);
		 XMaxval.draw(g2d);
		 YMaxval.draw(g2d);
		 LabelForDataSet.draw(g2d);
		 //Draw the Zero Line if necessary.
		 if(LegendZeroLine != null)
		 {
			 g2d.setStroke(dashedStroke);
			 g2d.draw(LegendZeroLine);
			 YZeroval.draw(g2d);
		 }
		 //Plot the lines in the LegendLineArea
		 plotLines(data, g2d, LegendLineArea, colors);
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
		 plotLines(data, g2d, LineArea,colors);
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
	 private void plotLines(NodeData data, SVGGraphics2D g2d, Rectangle2D area, ColorMap colors)
	 {
		 ValueSetNodeData nd = (ValueSetNodeData) data;
		 Paint origPaint = g2d.getPaint();
		 Stroke currentStroke = g2d.getStroke();
		 g2d.setStroke(new BasicStroke(3));
		 //move to the plotting area
		 g2d.translate(area.getX(), area.getY());
		 for(String name : ((ValueSetDataSet)data.getDataSet()).getSetNames())
		 {
//			 System.out.println("Plotting Line for Sheet " + name); 
			 ValueSetDataValue vsd = nd.getData(name);
			 if(vsd != null)
			 {
				 
				 Vector<Double> LineYValues = vsd.getEntryData();				 
				 Vector<Double> LineXValues = LineHeaders.get(name);
//				 System.out.println("Found the Data for the Sheet with a length of " + LineYValues.size() + " while the X values have a size of " + LineXValues.size());
				 Color linecolor = colors.getColor(name);
				 Path2D currentpath = getPath(LineXValues, LineYValues, area);
				 g2d.setPaint(linecolor);
				 g2d.draw(currentpath);
			 }						
		 }
		 g2d.translate(-area.getX(), -area.getY());
		 g2d.setStroke(currentStroke);
		 g2d.setPaint(origPaint);
	 }

	 /**
	  * Determine a Path in the given ara using xvalues and yvalues
	  * @param xvalues
	  * @param yvalues
	  * @param area
	  * @return
	  */
	 private Path2D getPath(Vector<Double> xvalues, Vector<Double> yvalues, Rectangle2D area)
	 {
		 Path2D result = new Path2D.Double();
		 boolean pathstarted = false; 
		 for(int i = 0; i < xvalues.size(); i++)
		 {
			 if(!pathstarted)
			 {
				 if(yvalues.get(i) != null)
				 {
					 Double val = yvalues.get(i);
					 Double coord = xvalues.get(i);
					 Point2D loc = getPlotPoint(coord,val,area.getWidth(),area.getHeight());
					 result.append(new Line2D.Double(loc,loc), false);
					 pathstarted = true;
				 }
			 }
			 else
			 {
				 if(yvalues.get(i) != null)
				 {
					 Double val = yvalues.get(i);
					 Double coord = xvalues.get(i);
					 Point2D loc = getPlotPoint(coord,val,area.getWidth(),area.getHeight());
					 result.append(new Line2D.Double(loc,loc),true);
				 }

			 }
		 }
		 return result;
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

}

