package idare.imagenode.internal.ImageManagement;

import idare.Properties.IDAREProperties;
import idare.ThirdParty.BufferedImageTranscoder;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.DataManagement.Events.NodeChangedListener;
import idare.imagenode.internal.DataManagement.Events.NodeUpdateEvent;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.VisualStyle.IDAREVisualStyle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.w3c.dom.svg.SVGDocument;
/**
 * The {@link ImageStorage} functions as both {@link CyCustomGraphicsFactory} and management for images used by IDARE.
 * In addition it provides the mapping function between Strings and the respective IDARE images.
 * 
 * @author Thomas Pfau
 *
 */
public class ImageStorage implements CyCustomGraphicsFactory,VisualMappingFunction<String,CyCustomGraphics<CustomGraphicLayer>>,DiscreteMapping<String, CyCustomGraphics<CustomGraphicLayer>>,NodeChangedListener {
	private HashMap<String, BufferedImage> imagenodes;
	private NodeManager nodeManager;
	private HashMap<String,CyCustomGraphics<CustomGraphicLayer>> graphicsmap;
	//private int width;
	//private int height;
	private Vector<GraphicsChangedListener> listeners;
	private IDAREVisualStyle visualstyle;
	
	VisualProperty costumVP;
	private boolean setupNeeded;
	
	/**
	 * Default Constructor defining the {@link VisualProperty} that this {@link VisualMappingFunction} maps to. 
	 * @param VisualcustomGraphiVP The VisualProperty to use
	 */
	public ImageStorage(VisualProperty<CyCustomGraphics<CustomGraphicLayer>>  VisualcustomGraphiVP) {
		//this class is responsible to create the Images and map IDs to the appropriate images.
		//It does however not work as an interface to the 
		costumVP = VisualcustomGraphiVP;
		listeners = new Vector<GraphicsChangedListener>();
		imagenodes = new HashMap<String, BufferedImage>();
		graphicsmap = new HashMap<String, CyCustomGraphics<CustomGraphicLayer>>();
		//this.width = (int)IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_WIDTH;
		//this.height = (int)IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT;
		setupNeeded = false;
	}
	
	/**
	 * set the {@link IDAREVisualStyle} linked to this {@link ImageStorage}
	 * @param ids the IDAREVisualStyle to use
	 */
	public void setVisualStyle(IDAREVisualStyle ids)
	{
		visualstyle = ids;
	}
	
	/**
	 *  Set the {@link NodeManager} linked to this {@link ImageStorage}
	 *  @param nm The NodeManager to use
	 */
	
	public void setNodeManager(NodeManager nm)
	{
		nodeManager = nm;
	}
	/**
	 * Add a listener that listens to changed images (and updates accordingly)
	 * @param listener the listener to add
	 */
	public void addImageLayoutChangedListener(GraphicsChangedListener listener)
	{
		listeners.addElement(listener);
	}
	/**
	 * Reset the images available in this mapping
	 */
	public synchronized void reset(){		
		imagenodes.clear();
		Vector<String> imagekeys = new Vector<String>();
		imagekeys.addAll(graphicsmap.keySet());
		graphicsmap.clear();		
		fireLayoutChange(imagekeys);		
		
	}
	/**
	 * Inform all listeners about the updated IDs (and provide them with the respective Images for those IDs. 
	 * @param IDs The IDs which were changed.
	 */
	private void fireLayoutChange(Collection<String> IDs)
	{
		long start = System.nanoTime();
		HashMap<String, CyCustomGraphics<CustomGraphicLayer>> newgraphics = new HashMap<String, CyCustomGraphics<CustomGraphicLayer>>();
		for(String id : IDs)
		{
			newgraphics.put(id,graphicsmap.get(id));
		}
		Vector<GraphicsChangedListener> clisteners = new Vector<GraphicsChangedListener>();
		clisteners.addAll(listeners);
		for(GraphicsChangedListener listener : clisteners)
		{
			listener.imageUpdated(new GraphicsChangedEvent(newgraphics));
		}
		PrintFDebugger.Debugging(this, "Updating the layout info took " + ((System.nanoTime() -start)/1000000) + " miliseconds");

	}
	
	/**
	 * Invalidate a specific ID (i.e. remove the ID from the mapping).
	 * @param id The id to invalidate and remove the image the associated image.
	 */
	public void invalidate(String id)
	{		
		imagenodes.remove(id);
		graphicsmap.remove(id);
		PrintFDebugger.Debugging(this, "Setting setupNeeded to true");
		setupNeeded = true;		
		generateGraphicsForID(id, false);	
		fireLayoutChange(Collections.singleton(id));
	}
	/**
	 * Invalidate all Images associated with the provided Strings.
	 * @param ids The IDs of the nodes for which to invalidate/remove the associated images.
	 */
	public void invalidate(Collection<String> ids)
	{

		Collection<String> changedIDs = new HashSet<String>();
		changedIDs.addAll(ids);
		for(String id : changedIDs)
		{
			imagenodes.remove(id);
			graphicsmap.remove(id);
		}
		PrintFDebugger.Debugging(this, "Setting setupNeeded to true");
		setupNeeded = true;
		generateGraphicsForIDs(changedIDs);
		fireLayoutChange(changedIDs);
	}
	/**
	 * Get the BufferedImage associated with a specific ID
	 * The Image will be generated if necessar (and possible)
	 * @param ID The ID of node an image is requested for
	 * @return The requested image, or null if it does not exists, and cannot be generated.
	 */
	public BufferedImage getimagenodeImageForItem(String ID)
	{	
		if(!imagenodes.containsKey(ID) && nodeManager.isNodeActive(ID))
		{

			SVGDocument doc = LayoutUtils.createSVGDoc();
			SVGGraphics2D g = new SVGGraphics2D(doc);	
			nodeManager.getLayoutForNode(ID).layoutNode(nodeManager.getNode(ID).getData(), g);
			LayoutUtils.TransferGraphicsToDocument(doc, null, g);
			//Element root = doc.getDocumentElement();
			//g.getRoot(root);
			//root.setAttribute("viewBox", "0 0 400 270");
			PrintFDebugger.Debugging(this,"Creating Image for Node: " + ID);
			imagenodes.put(ID, SVGToBufferedImage(doc, IMAGENODEPROPERTIES.IMAGEWIDTH));
			
			
		}			
		return imagenodes.get(ID);

	}

	/**
	 * Convert a {@link SVGDocument} to a BufferedImage with a specified width.
	 * @param svg The {@link SVGDocument} to obtain the data from
	 * @param width The width we want the resulting image to be
	 * @return A {@link BufferedImage} representing the {@link SVGDocument} provided scaled to the given width
	 */
	private BufferedImage SVGToBufferedImage(SVGDocument svg, int width)
	{
		BufferedImageTranscoder t = new BufferedImageTranscoder(); 

		t.addTranscodingHint(PNGTranscoder.KEY_WIDTH,  (float) width);
		t.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.white);		
		// t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height); 

		// t.addTranscodingHint(PNGTranscoder.KEY_,  (float) width); 

		TranscoderInput input = new TranscoderInput(svg); 
		try 
		{ 
			t.transcode(input, null); 
		} catch (TranscoderException e) 
		{ 
			throw new RuntimeException(e); 
		} 

		return t.getBufferedImage(); 
	} 

	/**
	 * Generate the Graphics for the respective IDs
	 * @param IDs - The IDs of the Nodes
	 */
	public synchronized void generateGraphicsForIDs(Collection<String> IDs)
	{
		for(String id : IDs)
		{
			if(nodeManager.isNodeLayouted(id)){
				//If we are during the loading of NOdes, we replace old ones.
				graphicsmap.put(id, getInstance(id));
			}			
		}
		PrintFDebugger.Debugging(this, "Setting setupNeeded to false");
		setupNeeded = false;
		//fireLayoutChange(IDs);
	}
	
	
	/**
	 * Get the Graphic for the respective ID 
	 * @param ID - The ID of the Node
	 * @param Setup - Whether this is an initial setup or a general update
	 */
	public synchronized void generateGraphicsForID(String ID, boolean Setup)
	{
		if(Setup)
		{
			Vector<String> changedIDs = new Vector<String>();
			for(String id : nodeManager.getLayoutedIDs())
			{
				PrintFDebugger.Debugging(this, "Node " + id + " was layouted");
				if(nodeManager.isNodeActive(id)){
					PrintFDebugger.Debugging(this, "Node " + id + " was active");
					//If we are during the loading of NOdes, we replace old ones.
					//do this only, if its not 
					if(!graphicsmap.containsKey(id))
					{
					graphicsmap.put(id, getInstance(id));
					changedIDs.add(id);
					}					
				}			
			}
			if(changedIDs.size() > 0)
			{
				fireLayoutChange(nodeManager.getActiveIDs());
			}
		}
		else if(ID != null)
		{			
			//for now, we will just create these things "on the fly and don't check whether there is a suitable 
			if(nodeManager.isNodeActive(ID)){
				//If we are during the loading of NOdes, we replace old ones.
				if(!graphicsmap.containsKey(ID)){
					graphicsmap.put(ID, getInstance(ID));				
					fireLayoutChange(Collections.singletonList(ID));
				}			
			}			
		}
		PrintFDebugger.Debugging(this, "Setting setupNeeded to false");
		setupNeeded = false;
	}


	@Override
	public String getPrefix() {
		return null;
	}

	@Override
	public boolean supportsMime(String mimeType) {
		return false;
	}

	@Override
	public IDARECustomGraphics getInstance(URL url) {

		return getInstance(url.toString());
	}

	@Override
	public IDARECustomGraphics getInstance(String input) {    	    	
		BufferedImage imagenode = getimagenodeImageForItem(input);
		if(imagenode != null)
		{
			//Use the image if there is one provided by the imageMatcher
			double imageheight = imagenode.getHeight();
			double imagewidth = imagenode.getWidth();
			//now, get the maximal extension
			double heightscale = imageheight/nodeManager.getLayoutForNode(input).getDisplayDimensions().getHeight();
			double widthscale = imagewidth/nodeManager.getLayoutForNode(input).getDisplayDimensions().getWidth();
			double scalefactor = Math.max(heightscale, widthscale);
			int usedwidth = (int)Math.floor(imagewidth/scalefactor);
			int usedheight = (int)Math.floor(imageheight/scalefactor);
			IDARECustomGraphics myCustomGraphics = new IDARECustomGraphics(imagenode,usedwidth,usedheight);
			myCustomGraphics.setDisplayName(input);
			return myCustomGraphics;
		}
		else
		{
			//There is no image, so we return null
			return null;
		}
	}

	@Override
	public CyCustomGraphics parseSerializableString(String string) {
		return null;
	}

	@Override
	public Class<? extends CyCustomGraphics> getSupportedClass() {
		return IDARECustomGraphics.class;
	}	
	/**
	 * Update all views using the visualstyle set for this Storage
	 */
	public void updateStyle()
	{
		if(visualstyle != null)
		{
			visualstyle.updateRelevantViews();
		}
	}
	
	
	@Override
	public void apply(CyRow arg0, View arg1) {
		
		String idareName = arg0.get(IDAREProperties.IDARE_NODE_NAME, String.class);
		//this will induce an addition of the Costum graphics.
		if(setupNeeded)
		{
			generateGraphicsForID(idareName, false);
		}
		//if this idareName is already mapped to a node, use the mapped graphics.
		if(graphicsmap.containsKey(idareName))
		{
			arg1.setVisualProperty(costumVP, graphicsmap.get(idareName));
		}
	}

	@Override
	public CyCustomGraphics<CustomGraphicLayer> getMappedValue(CyRow arg0) {
		String idareName = arg0.get(IDAREProperties.IDARE_NODE_NAME, String.class);			
		return getMapValue(idareName);
	}

	@Override
	public String getMappingColumnName() {
		return IDAREProperties.IDARE_NODE_NAME;
	}

	@Override
	public Class<String> getMappingColumnType() {
		return String.class;
	}

	@Override
	public VisualProperty getVisualProperty() {
		return costumVP;
	}





	@Override
	public Map<String, CyCustomGraphics<CustomGraphicLayer>> getAll() {
		long start = System.nanoTime();
		
		if(setupNeeded)
		{
			generateGraphicsForID("", true);
		}
		else
		{
			PrintFDebugger.Trace(this);
		}
		
		PrintFDebugger.Debugging(this, "Generating all graphics took " + ((System.nanoTime() -start)/1000000) + " miliseconds");

		return graphicsmap;
	}





	@Override
	public CyCustomGraphics<CustomGraphicLayer> getMapValue(String arg0) {		
		if(setupNeeded)
		{
			generateGraphicsForID(arg0, false);
		}	
		return graphicsmap.get(arg0);
	}





	@Override
	public <T extends CyCustomGraphics<CustomGraphicLayer>> void putAll(
			Map<String, T> arg0) {
		graphicsmap.putAll(arg0);
	}





	@Override
	public <T extends CyCustomGraphics<CustomGraphicLayer>> void putMapValue(
			String arg0, T arg1) {
		graphicsmap.put(arg0, arg1);
	}

	@Override
	public void handleNodeUpdate(NodeUpdateEvent e) {
		// TODO Auto-generatd method stub
		PrintFDebugger.Debugging(this, "Got a Node Update event");
		invalidate(e.getupdatedIDs());
	}
	
}


