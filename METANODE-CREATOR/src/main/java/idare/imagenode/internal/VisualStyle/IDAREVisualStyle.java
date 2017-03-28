package idare.imagenode.internal.VisualStyle;


import idare.Properties.IDAREProperties;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.ImageManagement.GraphicsChangedEvent;
import idare.imagenode.internal.ImageManagement.GraphicsChangedListener;
import idare.imagenode.internal.ImageManagement.ImageStorage;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

/**
 * A Visual Style using information from the IDARE columns (defined in {@link IDAREProperties}) for better visibility of the networks.
 * @author Thomas Pfau 
 *
 */
public class IDAREVisualStyle implements SessionLoadedListener, GraphicsChangedListener{
	
	private VisualStyleFactory visualStyleFactoryServiceRef;
	private VisualMappingManager vmmServiceRef;
	private VisualMappingFunctionFactory vmfFactoryD;
	private VisualMappingFunctionFactory vmfFactoryP;
	private CyEventHelper eventHelper;	
	private CyNetworkViewManager cyNetViewMgr;
	private VisualStyle vs;
	
	
	private ImageStorage imf;
	private NodeManager nm;
	
	public static String IDARE_STYLE_TITLE = "IDARE Visual Style";
/**
 * Create a new Visual Style and Activator.
 * @param visualStyleFactoryServiceRef - Service reference for visual style
 * @param vmmServiceRef - Visual Mapping Manager
 * @param vmfFactoryD - Visualmapping function factory for Discrete mappings
 * @param vmfFactoryP - Visualmapping function factory for passthrough mappings
 * @param eventHelper - Cytoscape Event helper
 * @param imf - The IDARE specific mapping function for images.
 * @param cyNetViewMgr - A Cytoscape Network View Manager
 * @param idm - The Nodemanager of IDARE
 */
	public IDAREVisualStyle(VisualStyleFactory visualStyleFactoryServiceRef,	VisualMappingManager vmmServiceRef,
			VisualMappingFunctionFactory vmfFactoryD,VisualMappingFunctionFactory vmfFactoryP, CyEventHelper eventHelper,
			ImageStorage imf, CyNetworkViewManager cyNetViewMgr, NodeManager idm)
	{
		//super("Setup Network for IDARE Style");		
		this.visualStyleFactoryServiceRef = visualStyleFactoryServiceRef;
		this.vmmServiceRef = vmmServiceRef;
		this.vmfFactoryD	= vmfFactoryD;
		this.vmfFactoryP	= vmfFactoryP;		
		this.eventHelper = eventHelper;		
		this.cyNetViewMgr = cyNetViewMgr;		
		this.imf = imf;		
		this.nm = idm;
		
		vs = this.addStyle();		
		this.applyToAll();
	}
		
	/**
	 * Remove the Visualstyle from Cytoscape.
	 */
	public void shutdown()
	{
		vmmServiceRef.removeVisualStyle(vs);
	}
	
	/**
	 * Reset the Visual Style to use its default properties
	 */
	public void reset()
	{
		setupVisualStyle();
		updateRelevantViews();
	}
	
	/**
	 * Setup the visual Style
	 */
	public void setupVisualStyle(){
		// Lets assume the following Style: 
				// Reactions are Diamonds with a bluish background
				// Metabolites are circles with a red/orange background
				// Genes are Squares (which will hopefully be filled by IDARE nodes 
		IDAREDependentMapper<Integer> LabelTransparency = new IDAREDependentMapper<Integer>(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), BasicVisualLexicon.NODE_LABEL_TRANSPARENCY,nm,0);
		//and the sizes of imagenodes get adjusted
		IDAREDependentMapper<Double> imagenodeHeight = new IDAREDependentMapper<Double>(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), BasicVisualLexicon.NODE_HEIGHT,nm,IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT);
		IDAREDependentMapper<Double> imagenodeWidth = new IDAREDependentMapper<Double>(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), BasicVisualLexicon.NODE_WIDTH,nm,IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_WIDTH);
		
		
		PassthroughMapping NameMapping = (PassthroughMapping) this.vmfFactoryP.createVisualMappingFunction(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), String.class, BasicVisualLexicon.NODE_LABEL);
		// 2. DiscreteMapping - Set node shape based on attribute value
		DiscreteMapping<String,NodeShape> ShapeMapping = (DiscreteMapping<String, NodeShape>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(), String.class, BasicVisualLexicon.NODE_SHAPE);
		// If attribute value is "diamon", map the nodeShape to DIAMOND
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, NodeShapeVisualProperty.ELLIPSE);
		// If attribute value is "triangle", map the nodeShape to TRIANGLE		
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, NodeShapeVisualProperty.RECTANGLE);
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_GENE, NodeShapeVisualProperty.DIAMOND);
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, NodeShapeVisualProperty.HEXAGON);
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_LINK, NodeShapeVisualProperty.RECTANGLE);

		
		DiscreteMapping<String, Integer> NodeLabelSize = (DiscreteMapping<String, Integer>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(), String.class, BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		NodeLabelSize.putMapValue(IDAREProperties.NodeType.IDARE_LINK, 20);
		
		DiscreteMapping<String, ArrowShape> EdgeTargetArrow = (DiscreteMapping<String, ArrowShape>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.ColumnHeaders.IDARE_EDGE_PROPERTY.toString(), String.class, BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
		EdgeTargetArrow.putMapValue(IDAREProperties.EdgeType.REACTION_REVERSIBLE, ArrowShapeVisualProperty.DELTA);
		EdgeTargetArrow.putMapValue(IDAREProperties.EdgeType.REACTANT_EDGE, ArrowShapeVisualProperty.DELTA);
		
		
		DiscreteMapping<String, ArrowShape> EdgeSourceArrow = (DiscreteMapping<String, ArrowShape>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.ColumnHeaders.IDARE_EDGE_PROPERTY.toString(), String.class, BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
		EdgeSourceArrow.putMapValue(IDAREProperties.EdgeType.REACTION_REVERSIBLE, ArrowShapeVisualProperty.DELTA);
		EdgeSourceArrow.putMapValue(IDAREProperties.EdgeType.PRODUCT_EDGE, ArrowShapeVisualProperty.DELTA);
		
		
		DiscreteMapping<String,Paint> NodeColorMapping = (DiscreteMapping<String,Paint>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(), String.class, BasicVisualLexicon.NODE_FILL_COLOR);

		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, Color.RED);
		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, Color.BLUE);
		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_GENE, Color.orange);
		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, Color.GREEN);
		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_LINK, Color.white);


		DiscreteMapping<String, Double> BorderMap = (DiscreteMapping<String, Double>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(), String.class, BasicVisualLexicon.NODE_BORDER_WIDTH);
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, 2.);
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, 2.);
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_GENE, 2.);
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, 2.);	
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_LINK, 0.0);
		
		DiscreteMapping<String,Integer> BorderTransparancyMap =(DiscreteMapping<String, Integer>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(), String.class, BasicVisualLexicon.NODE_BORDER_TRANSPARENCY);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, 255);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, 255);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_GENE, 255);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, 255);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_LINK, 0);
		//BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_imagenode, 0);
		

		IDAREDependentMapper<Integer> TransparancyMap = new IDAREDependentMapper<Integer>(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(), BasicVisualLexicon.NODE_TRANSPARENCY,nm,0);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, 255);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, 255);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_GENE, 255);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, 255);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_LINK, 0);

		//labels of imagenodes don't get displayed
		//IDAREDependentMapper<Integer> LabelTransparency = new IDAREDependentMapper<Integer>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_LABEL_TRANSPARENCY,idm,0);
		//and the sizes of imagenodes get adjusted
		//IDAREDependentMapper<Double> imagenodeHeight = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_HEIGHT,idm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT);
		//IDAREDependentMapper<Double> imagenodeWidth = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_WIDTH,idm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_WIDTH);
		//IDAREDependentMapper<NodeShape> imagenodeShape = new IDAREDependentMapper<NodeShape>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_SHAPE,idm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_SHAPE);
		
		vs.addVisualMappingFunction(LabelTransparency);
		vs.addVisualMappingFunction(imagenodeHeight);
		vs.addVisualMappingFunction(imagenodeWidth);
		vs.addVisualMappingFunction(NameMapping);		
		vs.addVisualMappingFunction(ShapeMapping);
		vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.TRIANGLE);
		vs.addVisualMappingFunction(NodeColorMapping);
		vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.BLACK);
		vs.addVisualMappingFunction(TransparancyMap);
		vs.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 255);
		vs.addVisualMappingFunction(BorderMap);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 255);
		vs.addVisualMappingFunction(EdgeSourceArrow);
		vs.addVisualMappingFunction(EdgeTargetArrow);
		vs.addVisualMappingFunction(NodeLabelSize);
		vs.addVisualMappingFunction(imf);
		//vs.addVisualMappingFunction(BorderColorMapping);
		vs.addVisualMappingFunction(BorderTransparancyMap);
		//vs.addVisualMappingFunction(imagenodeShape);
	}
	
	/**
	 * Add this Style to the Style options.
	 *   
	 * @return - The {@link VisualStyle} created by this object.
	 */
	private VisualStyle addStyle()
	{
		Iterator it = vmmServiceRef.getAllVisualStyles().iterator();
		VisualStyle oldstyle = null;
		
		IDAREDependentMapper<Integer> LabelTransparency = new IDAREDependentMapper<Integer>(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), BasicVisualLexicon.NODE_LABEL_TRANSPARENCY,nm,0);
		//and the sizes of imagenodes get adjusted
		IDAREDependentMapper<Double> imagenodeHeight = new IDAREDependentMapper<Double>(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), BasicVisualLexicon.NODE_HEIGHT,nm,IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT);
		IDAREDependentMapper<Double> imagenodeWidth = new IDAREDependentMapper<Double>(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), BasicVisualLexicon.NODE_WIDTH,nm,IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_WIDTH);
		//IDAREDependentMapper<NodeShape> imagenodeShape = new IDAREDependentMapper<NodeShape>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_SHAPE,idm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_SHAPE);
		

		while (it.hasNext()){
			VisualStyle curVS = (VisualStyle)it.next();
			if (curVS.getTitle().equalsIgnoreCase(IDARE_STYLE_TITLE))
			{
				PrintFDebugger.Debugging(this, "Updating an old style");
				oldstyle = curVS;
				curVS.removeVisualMappingFunction(LabelTransparency.getVisualProperty());
				curVS.addVisualMappingFunction(LabelTransparency);
				curVS.removeVisualMappingFunction(imagenodeHeight.getVisualProperty());
				curVS.addVisualMappingFunction(imagenodeHeight);
				curVS.removeVisualMappingFunction(imagenodeWidth.getVisualProperty());
				curVS.addVisualMappingFunction(imagenodeWidth);
				curVS.removeVisualMappingFunction(imf.getVisualProperty());
				curVS.addVisualMappingFunction(imf);				
				return oldstyle;
			}
		}
		
		vs= this.visualStyleFactoryServiceRef.createVisualStyle(IDARE_STYLE_TITLE);
		vs.setTitle(IDARE_STYLE_TITLE);
		//CyTable attrForTest = cymanager.getCurrentNetwork().getDefaultNodeTable();
		// 1. pass-through mapping
		setupVisualStyle();
		this.vmmServiceRef.addVisualStyle(vs);						
		return vs;
	}
	
	/**
	 * Apply the contained visualstyle to a given Networkview and update that view.
	 * @param view
	 */
	public void applyStyleToNetwork(CyNetworkView view) {
		// TODO Auto-generated method stub
		
			vs.apply(view);
			view.updateView();
			
	}
	/**
	 * Check whether this style 
	 */
	public void applyToAll()
	{
		Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		for(CyNetworkView networkView : cyNetViewMgr.getNetworkViewSet())
		{			
			VisualStyle cvs = vmmServiceRef.getVisualStyle(networkView);			
			if(cvs.getTitle().equals(this.vs.getTitle()))
			{
				views.add(networkView);
			}
		}
		vs = addStyle();
		eventHelper.flushPayloadEvents();
		for(CyNetworkView networkView : views)
		{
			vs.apply(networkView);
			vmmServiceRef.setVisualStyle(vs, networkView);
			networkView.updateView();
		}
	
	}	
	
	
	@Override
	public void handleEvent(SessionLoadedEvent arg0) {
		/**First get all Views which currently have this set as their visual style, so that we can apply it again later on**/
		
		this.applyToAll();				
		
	}
	/**
	 * Update all views, that are associated with the IDARE visualstyle
	 */
	public void updateRelevantViews()
	{
		for(CyNetworkView networkView : cyNetViewMgr.getNetworkViewSet())
		{			
			VisualStyle cvs = vmmServiceRef.getVisualStyle(networkView);
			if(cvs.getTitle().equals(this.vs.getTitle()))
			{
				
				imf.getAll();
				applyStyleToNetwork(networkView);
				networkView.updateView();				
			}
		}
	}


	@Override
	public void imageUpdated(GraphicsChangedEvent e) {
		// TODO Auto-generated method stub
		updateRelevantViews();
	}
	
	
}
