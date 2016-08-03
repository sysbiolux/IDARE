package idare.imagenode.internal.VisualStyle;


import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.imagenode.Properties.METANODEPROPERTIES;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.ImageManagement.GraphicsChangedEvent;
import idare.imagenode.internal.ImageManagement.GraphicsChangedListener;
import idare.imagenode.internal.ImageManagement.ImageStorage;

import java.awt.Color;
import java.awt.Paint;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
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
	private CyApplicationManager cyAppMgr;
	
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
 * @param cyApp - the cytoscape App Manager. 
 */
	public IDAREVisualStyle(VisualStyleFactory visualStyleFactoryServiceRef,	VisualMappingManager vmmServiceRef,
			VisualMappingFunctionFactory vmfFactoryD,VisualMappingFunctionFactory vmfFactoryP, CyEventHelper eventHelper,
			ImageStorage imf, CyNetworkViewManager cyNetViewMgr, NodeManager idm, CyApplicationManager cyApp)
	{
		//super("Setup Network for IDARE Style");		
		this.visualStyleFactoryServiceRef = visualStyleFactoryServiceRef;
		this.vmmServiceRef = vmmServiceRef;
		this.vmfFactoryD	= vmfFactoryD;
		this.vmfFactoryP	= vmfFactoryP;		
		this.eventHelper = eventHelper;		
		this.cyNetViewMgr = cyNetViewMgr;		
		cyAppMgr = cyApp;
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
		IDAREDependentMapper<Integer> LabelTransparency = new IDAREDependentMapper<Integer>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_LABEL_TRANSPARENCY,nm,0);
		//and the sizes of MetaNodes get adjusted
		IDAREDependentMapper<Double> MetaNodeHeight = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_HEIGHT,nm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT);
		IDAREDependentMapper<Double> MetaNodeWidth = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_WIDTH,nm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_WIDTH);
		
		
		PassthroughMapping NameMapping = (PassthroughMapping) this.vmfFactoryP.createVisualMappingFunction(IDAREProperties.IDARE_NODE_NAME, String.class, BasicVisualLexicon.NODE_LABEL);
		// 2. DiscreteMapping - Set node shape based on attribute value
		DiscreteMapping<String,NodeShape> ShapeMapping = (DiscreteMapping<String, NodeShape>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.IDARE_NODE_TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
		// If attribute value is "diamon", map the nodeShape to DIAMOND
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, NodeShapeVisualProperty.ELLIPSE);
		// If attribute value is "triangle", map the nodeShape to TRIANGLE		
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, NodeShapeVisualProperty.RECTANGLE);
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_GENE, NodeShapeVisualProperty.DIAMOND);
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, NodeShapeVisualProperty.HEXAGON);
		ShapeMapping.putMapValue(IDAREProperties.NodeType.IDARE_LINK, NodeShapeVisualProperty.RECTANGLE);

		
		DiscreteMapping<String, Integer> NodeLabelSize = (DiscreteMapping<String, Integer>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.IDARE_NODE_TYPE, String.class, BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		NodeLabelSize.putMapValue(IDAREProperties.NodeType.IDARE_LINK, 20);
		
		DiscreteMapping<String, ArrowShape> EdgeTargetArrow = (DiscreteMapping<String, ArrowShape>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.IDARE_EDGE_PROPERTY, String.class, BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
		EdgeTargetArrow.putMapValue(IDAREProperties.EdgeType.REACTION_REVERSIBLE, ArrowShapeVisualProperty.DELTA);
		EdgeTargetArrow.putMapValue(IDAREProperties.EdgeType.REACTANT_EDGE, ArrowShapeVisualProperty.DELTA);
		
		
		DiscreteMapping<String, ArrowShape> EdgeSourceArrow = (DiscreteMapping<String, ArrowShape>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.IDARE_EDGE_PROPERTY, String.class, BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
		EdgeSourceArrow.putMapValue(IDAREProperties.EdgeType.REACTION_REVERSIBLE, ArrowShapeVisualProperty.DELTA);
		EdgeSourceArrow.putMapValue(IDAREProperties.EdgeType.PRODUCT_EDGE, ArrowShapeVisualProperty.DELTA);
		
		
		DiscreteMapping<String,Paint> NodeColorMapping = (DiscreteMapping<String,Paint>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.IDARE_NODE_TYPE, String.class, BasicVisualLexicon.NODE_FILL_COLOR);

		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, Color.RED);
		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, Color.BLUE);
		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_GENE, Color.orange);
		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, Color.GREEN);
		NodeColorMapping.putMapValue(IDAREProperties.NodeType.IDARE_LINK, Color.white);


		DiscreteMapping<String, Double> BorderMap = (DiscreteMapping<String, Double>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.IDARE_NODE_TYPE, String.class, BasicVisualLexicon.NODE_BORDER_WIDTH);
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, 2.);
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, 2.);
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_GENE, 2.);
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, 2.);	
		BorderMap.putMapValue(IDAREProperties.NodeType.IDARE_LINK, 0.0);
		
		DiscreteMapping<String,Integer> BorderTransparancyMap =(DiscreteMapping<String, Integer>) this.vmfFactoryD.createVisualMappingFunction(IDAREProperties.IDARE_NODE_TYPE, String.class, BasicVisualLexicon.NODE_BORDER_TRANSPARENCY);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, 255);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, 255);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_GENE, 255);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, 255);
		BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_LINK, 0);
		//BorderTransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_METANODE, 0);
		

		IDAREDependentMapper<Integer> TransparancyMap = new IDAREDependentMapper<Integer>(IDAREProperties.IDARE_NODE_TYPE, BasicVisualLexicon.NODE_TRANSPARENCY,nm,0);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_SPECIES, 255);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_REACTION, 255);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_GENE, 255);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_PROTEIN, 255);
		TransparancyMap.putMapValue(IDAREProperties.NodeType.IDARE_LINK, 0);

		//labels of Metanodes don't get displayed
		//IDAREDependentMapper<Integer> LabelTransparency = new IDAREDependentMapper<Integer>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_LABEL_TRANSPARENCY,idm,0);
		//and the sizes of MetaNodes get adjusted
		//IDAREDependentMapper<Double> MetaNodeHeight = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_HEIGHT,idm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT);
		//IDAREDependentMapper<Double> MetaNodeWidth = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_WIDTH,idm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_WIDTH);
		//IDAREDependentMapper<NodeShape> MetaNodeShape = new IDAREDependentMapper<NodeShape>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_SHAPE,idm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_SHAPE);
		
		vs.addVisualMappingFunction(LabelTransparency);
		vs.addVisualMappingFunction(MetaNodeHeight);
		vs.addVisualMappingFunction(MetaNodeWidth);
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
		//vs.addVisualMappingFunction(MetaNodeShape);
	}
	
	/**
	 * Add this Style to the Style options.  
	 * @return - The {@link VisualStyle} created by this object.
	 */
	private VisualStyle addStyle()
	{
		Iterator it = vmmServiceRef.getAllVisualStyles().iterator();
		VisualStyle oldstyle = null;
		
		IDAREDependentMapper<Integer> LabelTransparency = new IDAREDependentMapper<Integer>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_LABEL_TRANSPARENCY,nm,0);
		//and the sizes of MetaNodes get adjusted
		IDAREDependentMapper<Double> MetaNodeHeight = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_HEIGHT,nm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT);
		IDAREDependentMapper<Double> MetaNodeWidth = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_WIDTH,nm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_WIDTH);
		//IDAREDependentMapper<NodeShape> MetaNodeShape = new IDAREDependentMapper<NodeShape>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_SHAPE,idm,METANODEPROPERTIES.IDARE_NODE_DISPLAY_SHAPE);
		

		while (it.hasNext()){
			VisualStyle curVS = (VisualStyle)it.next();
			if (curVS.getTitle().equalsIgnoreCase(IDARE_STYLE_TITLE))
			{
				oldstyle = curVS;
				curVS.removeVisualMappingFunction(LabelTransparency.getVisualProperty());
				curVS.addVisualMappingFunction(LabelTransparency);
				curVS.removeVisualMappingFunction(MetaNodeHeight.getVisualProperty());
				curVS.addVisualMappingFunction(MetaNodeHeight);
				curVS.removeVisualMappingFunction(MetaNodeWidth.getVisualProperty());
				curVS.addVisualMappingFunction(MetaNodeWidth);
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
	/**
	 * Test whether a Network has the table Columns required for usage in IDARE
	 * @param network
	 * @return whether the network is set up to be used with IDARE.
	 */
	public static boolean isSetupNetwork(CyNetwork network)
	{
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable EdgeTable = network.getDefaultEdgeTable();
		if(EdgeTable.getColumn(IDAREProperties.IDARE_EDGE_PROPERTY) == null || NodeTable.getColumn(IDAREProperties.IDARE_NODE_TYPE) == null 
				|| 	NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME) == null ||	NodeTable.getColumn(IDAREProperties.LINK_TARGET) == null
				|| NodeTable.getColumn(IDAREProperties.IDARE_NODE_UID) == null || NodeTable.getColumn(IDAREProperties.LINK_TARGET_SUBSYSTEM) == null
				|| NodeTable.getColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE) == null)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Set the matching IDARENames to the values set in the Selected Column(except that Strings will be used in the IDARE_NAME column
	 * @param network - the network for which to assign the names
	 * @param IDs - The IDs which are transferred
	 * @param ColForNames - The Column to get the IDs from.
	 */
	public static void setIDARENames(CyNetwork network, Collection<String> IDs, String ColForNames)
	{
		CyTable nodeTable = network.getDefaultNodeTable();
		Set<CyRow> matchingrows = new HashSet<CyRow>();
		for(String id : IDs)
		{
			matchingrows.addAll(nodeTable.getMatchingRows(ColForNames, id));
		}
		for(CyRow row : matchingrows)
		{
			row.set(IDAREProperties.IDARE_NODE_NAME, row.get(ColForNames,String.class));
		}
	}
	
	/**
	 * Set up the provided network using the Provided IDCol for the IDARE Names and the NodeTypesCol for the IDARE Node Types
	 * @param network - the network to set up
	 */
	public static void initNetwork(CyNetwork network)
	{
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable EdgeTable = network.getDefaultEdgeTable();
		try{
			EdgeTable.createColumn(IDAREProperties.IDARE_EDGE_PROPERTY, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}

		try{
			NodeTable.createColumn(IDAREProperties.IDARE_NODE_TYPE, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}

		try{
			NodeTable.createColumn(IDAREProperties.IDARE_NODE_NAME, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}


		try{
			NodeTable.createColumn(IDAREProperties.LINK_TARGET, Long.class, false);
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}

		try{
			NodeTable.createColumn(IDAREProperties.IDARE_NODE_UID, Long.class, false, null);			
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}

		try{
			NodeTable.createColumn(IDAREProperties.LINK_TARGET_SUBSYSTEM, String.class, false);
		}

		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}
		try{
			NodeTable.createColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class, false);
		}

		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	/**
	 * Set up the provided network using the Provided IDCol for the IDARE Names and the NodeTypesCol for the IDARE Node Types
	 * @param network - the network to set up
	 * @param IDAREIdmgr - A IDARE ID Manager to set up the IDs of all  
	 * @param NodeTypeCol
	 * @param IDCol
	 */
	public static void SetupNetwork(CyNetwork network, IDARESettingsManager IDAREIdmgr, String NodeTypeCol, String IDCol )
	{
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable EdgeTable = network.getDefaultEdgeTable();
		initNetwork(network);
		List<CyRow> NodeRows = NodeTable.getAllRows();
		
		for(CyRow row : NodeRows)
		{
			// if the column is either not set, or at the default value, initialize it.
			//furthermore we can initialize the IDARE_TARGET_ID property for save/restore functionality
			if(!row.isSet(IDAREProperties.IDARE_NODE_UID))
			{
				//row.set(IDAREProperties.IDARE_NODE_TYPE, row.get(NodeTypeCol,String.class));
				Long id = IDAREIdmgr.getNextID();
				row.set(IDAREProperties.IDARE_NODE_UID, id);

			}
			if(!row.isSet(IDAREProperties.IDARE_NODE_TYPE))
			{				
				
				row.set(IDAREProperties.IDARE_NODE_TYPE,IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
			}
			else
			{
				//if the value is not null but still the default, than also set it to the type value.
				if(row.get(IDAREProperties.IDARE_NODE_TYPE,String.class) != null && 
						row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(NodeTable.getColumn(IDAREProperties.IDARE_NODE_TYPE).getDefaultValue()))
				{
					if(!row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
					{
						//never change a linker node type.
						row.set(IDAREProperties.IDARE_NODE_TYPE,IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
					}
				}
			}
						
			if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME).getDefaultValue() == null)
			{
				System.out.println("Default Value Wrong!!!");
			}
			if(!row.isSet(IDAREProperties.IDARE_NODE_NAME) && NodeTable.getColumn(IDCol) != null)
			{
				if(!row.isSet(IDAREProperties.IDARE_NODE_TYPE) || !row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
				{
					row.set(IDAREProperties.IDARE_NODE_NAME, row.get(IDCol,String.class));
				}
			}
			else if (NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME) != null && (NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME).getDefaultValue() == null || NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME).getDefaultValue().equals(row.get(IDAREProperties.IDARE_NODE_NAME, String.class))))
			{
				row.set(IDAREProperties.IDARE_NODE_NAME, row.get(IDCol,String.class));
			}
		}
		List<CyRow> EdgeRows = EdgeTable.getAllRows();
		for(CyRow row: EdgeRows)
		{
			if(!row.isSet(IDAREProperties.IDARE_EDGE_PROPERTY) || row.get(IDAREProperties.IDARE_EDGE_PROPERTY, String.class).equals(EdgeTable.getColumn(IDAREProperties.IDARE_EDGE_PROPERTY).getDefaultValue()))
			{
				if(row.isSet(IDAREProperties.SBML_EDGE_TYPE))
				{
					row.set(IDAREProperties.IDARE_EDGE_PROPERTY, row.get(IDAREProperties.SBML_EDGE_TYPE, String.class));
				}
			}
		}

	}
	
	/**
	 * Set up the provided network using the Provided IDCol for the IDARE Names and the NodeTypesCol for the IDARE Node Types
	 * @param network - the network to set up
	 * @param IDAREIdmgr - A IDARE ID Manager to set up the IDs of all  
	 * @param NodeTypeCol
	 * @param IDCol
	 */
	public static void SetNetworkData(CyNetwork network, IDARESettingsManager IDAREIdmgr, String NodeTypeCol, String IDCol, boolean overwrite , NodeManager nm)
	{
		if(!isSetupNetwork(network))
		{
			SetupNetwork(network, IDAREIdmgr, NodeTypeCol, IDCol);
		}
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable EdgeTable = network.getDefaultEdgeTable();
		
		List<CyRow> NodeRows = NodeTable.getAllRows();
		Set<String> IDAREIDs = new HashSet<String>();
		for(CyRow row : NodeRows)
		{
			//if the column is either not set, or at the default value, initialize it.
			//furthermore we can initialize the IDARE_TARGET_ID property for save/restore functionality
			if(!row.isSet(IDAREProperties.IDARE_NODE_UID))
			{
				//row.set(IDAREProperties.IDARE_NODE_TYPE, row.get(NodeTypeCol,String.class));
				Long id = IDAREIdmgr.getNextID();
				row.set(IDAREProperties.IDARE_NODE_UID, id);

			}			
			//if the row is not set, or is equal to null or is at the default, set it to an updated value.
			if(!row.isSet(IDAREProperties.IDARE_NODE_TYPE))
			{
				row.set(IDAREProperties.IDARE_NODE_TYPE,IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
			}
			if(overwrite)
			{
				PrintFDebugger.Debugging(IDAREIdmgr, "Asking Property manager to obtain value for entry " + row.get(NodeTypeCol, String.class) + " it returned " + IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
				if(!row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
				{
					row.set(IDAREProperties.IDARE_NODE_TYPE,IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
				}
			}
			if(!row.isSet(IDAREProperties.IDARE_NODE_TYPE) || !row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
			{
				//DO NOT UPDATE LINKER NODE NAMES!
				row.set(IDAREProperties.IDARE_NODE_NAME, row.get(IDCol,String.class));
			}
			IDAREIDs.add(row.get(IDCol, String.class));
		}
		nm.updateNetworkNodes();
	}
	/**
	 * Get the set of nodeIDs present in the provided network
	 * @param network - the {@link CyNetwork} to retrieve the nodeids from
	 * @param IDCol - The column in the networks node {@link CyTable} containing the IDs.
	 * @return - a {@link Set} of IDs present in the provided network
	 */
	public static Set<String> getNetworkIDAREIDs(CyNetwork network, String IDCol)
	{
		Set<String> ids = new HashSet<String>();
		if(isSetupNetwork(network))
		{
			List<CyRow> NodeRows = network.getDefaultNodeTable().getAllRows();
			for(CyRow row : NodeRows)
			{
				ids.add(row.get(IDCol,String.class));			
				
			}

		}
		return ids;
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
		Set<CyNetworkView> views = new HashSet<CyNetworkView>();
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
