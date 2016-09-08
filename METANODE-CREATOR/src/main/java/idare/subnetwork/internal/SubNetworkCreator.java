package idare.subnetwork.internal;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.VisualStyle.IDAREVisualStyle;
import idare.subnetwork.internal.GUI.SubnetworkColumnChooser;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * A Class to provide a CyAction that allows the creation of Subnetworks based on a column in the current Network table.
 * @author Thomas Pfau
 *
 */
public class SubNetworkCreator extends AbstractCyAction{

	private static final long serialVersionUID = -7278615397589015714L;
	
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyEventHelper eventHelper;
	private final CyApplicationManager applicationManager;
	private final CyNetworkManager networkManager;
	private final CyLayoutAlgorithmManager LayoutManager;	
	private final DialogTaskManager dtm;
	private final VisualMappingManager vmm;
	private CySwingApplication cySwingApp;
	private final CyRootNetworkManager rootManager;
	private final NetworkViewSwitcher nvs;
	public IDARESettingsManager IDAREIdmgr;	
	
	
	private boolean accepted = false;
	private String choosenAlgorithm;
	private int choosenColumn = 0;
	private HashMap<String,CyLayoutAlgorithm> algorithms;
	private Vector<String> NetworkColumns;
	private Vector<Object> subSystems;
	private Set<CyNode> ignoredMetas;
	private Set<CyNode> noBranchMetas;
	private String nodeTypeColumn;
	private String compoundName;
	private String interactionName;
	private String IDColName;
	/**
	 * Default Constructor
	 * @param rootManager
	 * @param applicationManager
	 * @param menuTitle
	 * @param networkViewManager
	 * @param networkViewFactory
	 * @param eventHelper
	 * @param networkFactory
	 * @param networkManager
	 * @param LayoutManager
	 * @param dtm
	 * @param vmm
	 * @param nvs
	 * @param IDAREIdmgr
	 * @param cySwingApp
	 */
	public SubNetworkCreator(CyRootNetworkManager rootManager, final CyApplicationManager applicationManager, final String menuTitle,
			CyNetworkViewManager networkViewManager, CyNetworkViewFactory networkViewFactory, 
			CyEventHelper eventHelper,CyNetworkFactory networkFactory, CyNetworkManager networkManager,
			CyLayoutAlgorithmManager LayoutManager, DialogTaskManager dtm, VisualMappingManager vmm, 
			NetworkViewSwitcher nvs, IDARESettingsManager IDAREIdmgr, CySwingApplication cySwingApp) {		

		super(menuTitle, applicationManager, null, null);
		//this.nm = nm;
		this.networkViewManager = networkViewManager;
		this.networkViewFactory = networkViewFactory;
		this.eventHelper = eventHelper;
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
		this.LayoutManager = LayoutManager;
		this.dtm = dtm;
		this.rootManager = rootManager;
		this.vmm = vmm;
		this.nvs = nvs;
		this.IDAREIdmgr = IDAREIdmgr;
		this.cySwingApp = cySwingApp;
		setPreferredMenu("Apps");
		subSystems= new Vector<Object>();
		ignoredMetas = new HashSet<CyNode>();
		noBranchMetas = new HashSet<CyNode>();
	}
	
	/**
	 * Create A Subnetwork with "link" nodes to external  
	 * Get Node Sets belonging to one "SUBSYSTEM"
	 * To do this we will let the user decide, which column to use to extract the subsystems from. 
	 * This will also assume that those are representing reactions (ReactionSet) 
	 * -> i.e. all adjacent edges will be collected. (EdgeSet)
	 * -> all nodes connected by these edges will be collected (those are the metabolites) (MetaboliteSet)
	 * -> edges out of these nodes not contained in the original EdgeSet will be collected. (OutWardEdges)
	 * -> Nodes Connected to these Edges not in MetaboliteSet will be collected (AdjacentReactions)
	 * -> SubSystem Representation Nodes will be created in each newly created network if the selected Column is not empty in the respective nodes.
	 */		
	@Override
	public void actionPerformed(ActionEvent arg0) {
		accepted = false;
		//TODO: Offer a choice for the network to create subnetworks for.
		
		CyNetwork originalnetwork = applicationManager.getCurrentNetwork();
		if(originalnetwork == null)
		{
			JOptionPane.showMessageDialog(cySwingApp.getJFrame(), "Please select a Network for Subsystem generation");
			return;
		}

		CyTable table = applicationManager.getCurrentNetwork().getDefaultNodeTable();

		subSystems = getDifferentSubSystems(table, CyNode.SUID);
		Collection<CyLayoutAlgorithm> layouts = LayoutManager.getAllLayouts();
		algorithms = new HashMap<String,CyLayoutAlgorithm>();

		for(CyLayoutAlgorithm layout : layouts)
		{
			algorithms.put(layout.getName(),layout);
			//algoNames.add(layout.getName());
		}
		Collection<CyColumn> columns = applicationManager.getCurrentNetwork().getDefaultNodeTable().getColumns();
		NetworkColumns = new Vector<String>();
		for(CyColumn col : columns)
		{
			if(!col.getName().equals(CyNode.SUID))
			{
				NetworkColumns.add(col.getName());
			}
		}
		accepted = false;
		
		
		//final BipartitionChooser GUI = new BipartitionChooser(NetworkColumns,this,applicationManager.getCurrentNetwork(),cySwingApp);
		final SubnetworkColumnChooser GUI = new SubnetworkColumnChooser(NetworkColumns, applicationManager.getCurrentNetwork(), cySwingApp, this);
		
		GUI.setVisible(true);
	}
	
		
	/**
	 * Get a Vector of SubSystem Identifiers from the given CyTable and the given Column name (i.e. all different entries in that column)
	 * @param table - The table to look up
	 * @param ColName - The String identifying the Column to look up the different subsystem identifiers 
	 * @return a {@link Vector} of column Identifiers
	 */
	public static Vector<Object> getDifferentSubSystems(CyTable table, String ColName)
	{
		Vector<Object> SubsystemTypes = new Vector<Object>();		
		List<CyRow> rows = table.getAllRows();
		//we do not need to make any difference between the objects since they are simply comparable by equals();
		//lets get the Column first.
		CyColumn col = table.getColumn(ColName);
		if(col == null)
		{
			return SubsystemTypes;
		}
		if( col.getListElementType() == null)
		{
			//this is a "normal" column, so we can just go on. 
			for(CyRow row : rows)
			{				
				if(row.isSet(ColName))
				{
					if(row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) == null || !row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION))
					{
						//skip anything that is not a species for subnetwork selection, as anything beside species will not be allowed.
						continue;
					}
					if(SubNetworkCreationTask.isempty(row.get(ColName, table.getColumn(ColName).getType())))
					{
						continue;
					}
					if(!SubsystemTypes.contains(row.get(ColName, table.getColumn(ColName).getType())))
					{
						SubsystemTypes.add(row.get(ColName, table.getColumn(ColName).getType()));
					}

				}			
			}	
		}
		else
		{			
			//this is a list column, so we need to get all List elements and add them to the SubSystem Types 

			for(CyRow row : rows)
			{
				if(row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) == null || !row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION))
				{
					//skip anything that is not a species for subnetwork selection, as anything beside species will not be allowed.
					continue;
				}

				if(row.isSet(ColName))
				{
					for(Object item : row.getList(ColName, table.getColumn(ColName).getListElementType()))
					{
						if(SubNetworkCreationTask.isempty(item))
						{
							continue;
						}
						if(!SubsystemTypes.contains(item))
						{
							SubsystemTypes.add(item);
						}
					}					
				}			
			}	
		}
		return SubsystemTypes;
	}
	
	/**
	 * Run the subnetwork creation.
	 */
	public void runSubNetworkCreation()
	{
		if(accepted)
		{			
			SubNetworkCreatorTaskFactory SATF = new SubNetworkCreatorTaskFactory(rootManager,networkViewManager,networkViewFactory,
				eventHelper,applicationManager,networkManager,algorithms.get(choosenAlgorithm),NetworkColumns.get(choosenColumn),vmm,nvs,subSystems,ignoredMetas,noBranchMetas, IDAREIdmgr);
			dtm.execute(SATF.createTaskIterator());
		}
	}
	
	/**
	 * Accept the current settings. 
	 */
	public void accept()
	{
		accepted = true;
	}
	/**
	 * Set the algorithm choice (as position of the algorithm in the algorithm vector)
	 * @param algo
	 */
	public void setChoosenAlgorithm(String algo)
	{
		choosenAlgorithm = algo;
	}
	/**
	 * Set the currently choosen Column of the Table (based on its position in the Column vector
	 * @param col
	 */
	public void setChoosenColumn(int col)
	{
		choosenColumn = col;
	}

	/**
	 * Set the Set of Subsystems that should be created
	 * @param subSystems
	 */
	public void setCreatedSubSystems(Vector<Object> subSystems)
	{
		this.subSystems = subSystems;
	}
	/**
	 * Define the ignored CyNodes that are skipped in SubNetwork creation
	 * @param IgnoredMetas
	 */
	public void setIgnoredCyNodes(Set<CyNode> IgnoredMetas)
	{
		this.ignoredMetas = IgnoredMetas;
	}
	/**
	 * Define the non Branching MEtas (i.e. the Metabolites which do not link to other Subnetworks.
	 * @param noBranchMetas
	 */
	public void setNoBranchMetas(Set<CyNode> noBranchMetas) 
	{
		this.noBranchMetas = noBranchMetas;
	}
	/**
	 * Set the Type Column
	 * @param type
	 */
	public void setTypeColumn(String type)
	{		
		this.nodeTypeColumn = type;
	}
	/**
	 * Set the Compound Name
	 * @param name
	 */
	public void setCompoundName(String name)
	{
		this.compoundName = name;
	}
	/**
	 * Set the Interaction Name 
	 * @param name
	 */
	public void setInteractionName(String name)
	{
		this.interactionName = name;
	}
	/**
	 * Get the NodeType Column
	 * @return The name of the column.
	 */
	public String getNodeTypeColumn() {
		return nodeTypeColumn;
	}
	/**
	 * Get the compound name
	 * @return the Name for compounds.
	 */
	public String getCompoundName() {
		return compoundName;
	}
	/**
	 * Get the interaction name
	 * @return the Name for interactions.
	 */

	public String getInteractionName() {
		return interactionName;
	}	
	/**
	 * Set the Name of the ID Column
	 * @param IDColName - the name of the ID Column
	 */
	public void setIDColName(String IDColName) {
		this.IDColName = IDColName;
	}	
	/**
	 * Get the Name of the ID Column
	 * @return the name of the ID Column
	 */
	public String getIDColName() {
		return IDColName;
	}	
	/**
	 * Get the names of all layout algorithms available
	 * @return a Vector of algorithmn names
	 */
	public Collection<String> getAlgorithmNames()
	{
		Vector<String> algoNames = new Vector<String>();
		algoNames.addAll(algorithms.keySet());
		return algoNames;
	}
	/**
	 * Get the names of all networkColumns
	 * @return a Vector of Network Column Names
	 */
	public Vector<String> getNetworkColumns()
	{
		return this.NetworkColumns;
	}
	
	/**
	 * Get the Names of already existing subsystems (to avoid duplication).
	 * @return a {@link Set} of {@link String}s of Existing SubSystem Names
	 */
	public Set<String> getExistingSubSystemNames(CyNetwork selectednetwork, String ColumnName)
	{
		return nvs.getSubNetworkWorksForNetwork(selectednetwork,ColumnName);
	}


	
}

