package idare.imagenode.internal.GUI.Legend;


import idare.imagenode.GUI.Legend.Utilities.LegendSizeListener;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.internal.DataManagement.ImageNodeModel;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.DataManagement.Events.NodeChangedListener;
import idare.imagenode.internal.DataManagement.Events.NodeUpdateEvent;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Layout.NodeLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.w3c.dom.svg.SVGDocument;

/**
 * An LegendPanel for IDARE, should be displayed in the Results Panel of Cytoscape.
 * This Class contains the structures to align Node and description properly and rescale them.
 * @author Thomas Pfau
 *
 */
public class IDARELegend extends JScrollPane implements CytoPanelComponent, NodeChangedListener  {


		
	private MySVGCanvas Node;
	private JPanel Content;
	private NodeResizer resizer;
	private String currentNode; 
	private NodeManager manager;
	private boolean active;
	//	private JScrollPane NodePane;	
	
	/**
	 * Default constructor given a ContentPane to wrap this scrollerpane around and the 
	 * {@link NodeManager} to obtain layouts for updated nodes.
	 * @param ContentPane
	 * @param manager
	 */
	public IDARELegend(JPanel ContentPane, NodeManager manager) {			
		super(ContentPane);
		this.manager = manager;
		this.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		setBorder(null);
		setViewportBorder(null);
		Content = ContentPane;
		Node = new MySVGCanvas();			
		initialize();
		this.doLayout();
		this.revalidate();			
		active = false;			
	}
	/**
	 * Set the current Node (by its ID) to display a legend for
	 * @param ID
	 */
	public void setLegendNode(String ID)
	{
		currentNode = ID;
		updateLegendData();		
	}
	
	/**
	 * Determine whether the Legend is currently displaying information.
	 * @return true, if the legend is displaying information, false otherwise. 
	 */
	public boolean isActive()
	{
		return active;
	}
	/**
	 * Get the id representing the currently used node in the Legend.
	 * @return The String ID of the used nodes.
	 */
	public String getCurrentlyUsedNode()
	{
		return currentNode;
	}
	/**
	 * Update the legend Data. The legend will automatically ask for updated information on the current Node.
	 */
	public void updateLegendData()
	{
		if(currentNode != null & manager.isNodeLayouted(currentNode))
		{
			active = true;
			setLegendData(manager.getLayoutForNode(currentNode), manager.getNode(currentNode));
			getVerticalScrollBar().setValue(getVerticalScrollBar().getMinimum());
			getViewport().setViewPosition(new Point(0,0));
			getHorizontalScrollBar().setValue(0);

		}
	}
	/**
	 * Initialize this Legend
	 */
	private void initialize()
	{
		
		
		//Remove all Components that were listening to resize events from this legend.
		clearResizeListeners();
		//Clear the old content.
		Content.removeAll();
				
		//this.removeComponentListener(resizer);
		Content.setBackground(Color.white);
		Content.setLayout(new BoxLayout(Content,BoxLayout.PAGE_AXIS));		
		//Recreate the Node. and give it an initial size update.
		//Node.flush();
		//Node.flushImageCache();
		try{
			Node.flushImageCache();
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		Node.dispose();
		Node = new MySVGCanvas();
		
			
		//updateNodeSize();
		//Add the NodeResize Listener.
		resizer = new NodeResizer(Node);		
		//And Add the Node to the content (still empty)				
		revalidate();
	}
	/**
	 * reset the legend Panel when loading a new session
	 */
	public void reset()
	{
		currentNode = null;
		initialize();
		active = false;
		//revalidate();
	}
	/**
	 * Set the Legend Data using the given imagenode for data and the given layout for the node layout. 
	 * @param layout
	 * @param source
	 */
	public void setLegendData(NodeLayout layout, ImageNodeModel source)
	{
		try{
		initialize();
		//Now we have a legend, so lets set the Background and add the node.
		Content.setBackground(Color.black);
		
		this.addComponentListener(resizer);
		Content.add(Node);		
		SVGDocument doc = LayoutUtils.createSVGDoc();
		SVGGraphics2D g = new SVGGraphics2D(doc);		
		layout.layoutLegendNode(source.getData(), g);
		LayoutUtils.TransferGRaphicsToDocument(doc, new Dimension(IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH,(int)(IMAGENODEPROPERTIES.IMAGEHEIGHT * ((double)IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH/IMAGENODEPROPERTIES.IMAGEWIDTH))), g);
		Node.setAlignmentY(Node.TOP_ALIGNMENT);
		Node.setSVGDocument(doc);
		//Node.flush();
		Node.flushImageCache();
		updateNodeSize();
		setDataSetDescriptions(layout.getDatasetsInOrder(), layout);
		revalidate();
		repaint();
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
	/**
	 *	Update the Node size (with a certain minimal width and a maximal width which is the native width of the image.  
	 */
	private void updateNodeSize()
	{
		int availablewidth = getViewport().getSize().width-1;
		Node.setCanUpdatePreferredSize(true);
		if(availablewidth < IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH)
		{
			Node.setPreferredSize(new Dimension(IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH, 
					(int)((IMAGENODEPROPERTIES.IMAGEHEIGHT + IMAGENODEPROPERTIES.LABELHEIGHT) * (double)IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH / IMAGENODEPROPERTIES.IMAGEWIDTH)));
		}
		else
		{
			int optwidth = Math.min(availablewidth,IMAGENODEPROPERTIES.IMAGEWIDTH); 
			Node.setPreferredSize(new Dimension(optwidth,(int)((IMAGENODEPROPERTIES.IMAGEHEIGHT + IMAGENODEPROPERTIES.LABELHEIGHT)  * (double)optwidth / IMAGENODEPROPERTIES.IMAGEWIDTH)));
		}
		Node.setSize(Node.getPreferredSize());
		Node.setCanUpdatePreferredSize(false);
		
		//Node.invalidate();
	}
	/**
	 * Create the DataSetDescriptions based on the current layout and the set of DataSets.
	 * @param datasets
	 * @param layout
	 */
	private void setDataSetDescriptions(Vector<DataSet> datasets, NodeLayout layout)
	{

		for(DataSet ds : datasets)
		{
			JPanel DataSetPane = ds.getDataSetDescriptionPane(this,layout.getDataSetLabel(ds),layout.getColorsForDataSet(ds));			
			Content.add(Box.createRigidArea(new Dimension(0,2)));
			Content.add(DataSetPane);
			PrintFDebugger.Debugging(ds, "Layouting Pane");
			//DataSetPane.doLayout();
		}
		Content.revalidate();
	}
	
	/**
	 * Remove those components that listen to the resizing of this Legend.
	 */
	private void clearResizeListeners()
	{		
		for(ComponentListener listener : getComponentListeners())
		{
			if(listener instanceof LegendSizeListener)
			{
				PrintFDebugger.Debugging(listener, "Removing Listener from Legend");
				removeComponentListener(listener);
			}
		}
	}
	@Override
	public Component getComponent() {
		return this;
	}
	
	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}
	
	@Override
	public String getTitle() {		
		return "IDARE Node and Legend";		
	}	
	
	@Override
	public Icon getIcon() {
		return null;
	}
	/**
	 * The NOde Resizer will resize the Canvas containing the layouted node. 
	 * @author Thomas Pfau
	 *
	 */
	private class NodeResizer extends ComponentAdapter implements LegendSizeListener
	{
		MySVGCanvas canvas;
		public NodeResizer(MySVGCanvas canvas)
		{
			this.canvas = canvas;
		}
		@Override
		public void componentResized(ComponentEvent e)
		{
			JScrollPane source = (JScrollPane)e.getSource();
			int availablewidth = source.getViewport().getSize().width-1;
			//these are necessary to update the preferred size.
			canvas.setCanUpdatePreferredSize(true);
			if(availablewidth < IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH)
			{
				canvas.setPreferredSize(new Dimension(IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH, 
						(int)((IMAGENODEPROPERTIES.IMAGEHEIGHT + IMAGENODEPROPERTIES.LABELHEIGHT) * (double)IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH / IMAGENODEPROPERTIES.IMAGEWIDTH)));
			}
			else
			{
				int optwidth = Math.min(availablewidth,IMAGENODEPROPERTIES.IMAGEWIDTH); 
				canvas.setPreferredSize(new Dimension(optwidth,(int)((IMAGENODEPROPERTIES.IMAGEHEIGHT + IMAGENODEPROPERTIES.LABELHEIGHT)  * (double)optwidth / IMAGENODEPROPERTIES.IMAGEWIDTH)));
			}
			canvas.setCanUpdatePreferredSize(false);
			
			canvas.revalidate();
		}
	}
	
	/**
	 * This class is mainly a hack to only allow preferredsize changes when they are requested by the
	 * IDAREapp. The Original {@link JSVGCanvas} fires an awt event upon construction, that is only processed at some later stage, which updates the preferredsize and strongly interferes with initial layouting
	 * Not sure, whether this is fixed in newer BATIK version, but those newer versions did not work for me..
	 * 
	 * @author Thomas Pfau
	 *
	 */
	private class MySVGCanvas extends JSVGCanvas
	{
		
		private boolean canupdatepreferredsize = false;
		/**
		 * define whether a preferresizeupdate is accepted
		 * @param can
		 */
		public void setCanUpdatePreferredSize(boolean can)
		{
			canupdatepreferredsize = can;
		}
		@Override
		public void setPreferredSize(Dimension dim)
		{
			if(canupdatepreferredsize)
				super.setPreferredSize(dim);			
		}
	}


	@Override
	public void handleNodeUpdate(NodeUpdateEvent e) {
		// TODO Auto-generated method stub
		if(currentNode != null && e.getupdatedIDs().contains(currentNode))
		{
			updateLegendData();
		}
	}

}

