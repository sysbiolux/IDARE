package idare.subsystems.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import idare.Properties.IDAREProperties;
import idare.ThirdParty.DelayedVizProp;
import idare.metanode.internal.Debug.PrintFDebugger;
/**
 * A Task creating Subnetworks for a Network.
 * @author Thomas Pfau
 *
 */
public class SubNetworkCreationTask extends AbstractTask{
	public static final String subnetworkNameSeparator = "_";
	
	private final CyNetworkViewManager networkViewManager;
	
	private final CyNetworkViewFactory networkViewFactory;
	private final CyEventHelper eventHelper;
	private final CyApplicationManager applicationManager;
	private final CyNetworkManager networkManager;
	private final CyLayoutAlgorithm layoutAlgorithm;
	private final VisualMappingManager vmm;
	private final CyRootNetworkManager rootManager;	
	private CyServiceRegistrar reg;
	private final NetworkViewSwitcher nvs;

	private final Vector<Object> subSystems;
	private final Set<CyNode> ignoredNodes;
	private final Set<CyNode> noBranchNodes;
	private Map<Object,Collection<DelayedVizProp>> LinkNodeProps;
	private final String colString;
	private Map<CyNetworkView,Map<CyNode,Collection<CyNode>>> existingNodeLinks;

	/**
	 * Default Constructor
	 * @param rootManager
	 * @param networkViewManager
	 * @param networkViewFactory
	 * @param eventHelper
	 * @param applicationManager
	 * @param networkManager
	 * @param layout
	 * @param ColumnName
	 * @param vmm
	 * @param nvs
	 * @param subSystems
	 * @param ignoredNodes
	 * @param noBranchNodes
	 */
	public SubNetworkCreationTask(CyRootNetworkManager rootManager,CyNetworkViewManager networkViewManager,
			CyNetworkViewFactory networkViewFactory, CyEventHelper eventHelper,
			CyApplicationManager applicationManager, CyNetworkManager networkManager, CyLayoutAlgorithm layout,
			String ColumnName, VisualMappingManager vmm, NetworkViewSwitcher nvs, 
			Vector<Object> subSystems,Set<CyNode> ignoredNodes,Set<CyNode> noBranchNodes) {
		super();
		this.networkViewManager = networkViewManager;
		this.networkViewFactory = networkViewFactory;
		this.eventHelper = eventHelper;
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
		this.colString = ColumnName;
		this.layoutAlgorithm = layout;	
		this.vmm = vmm;
		this.nvs = nvs;
		this.subSystems = subSystems;
		this.ignoredNodes = ignoredNodes;
		this.noBranchNodes = noBranchNodes;
		this.rootManager = rootManager;
		LinkNodeProps = new HashMap<Object, Collection<DelayedVizProp>>();
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		createSubNetworkViews(applicationManager.getCurrentNetwork(), colString, applicationManager.getCurrentNetworkView(),subSystems);
	}

	/**
	 * Create the subnetwork views for all <b>subSystems</b> for the original network based on the Column represented by the columnname provided and 
	 * @param originalnetwork - The original network (including all subsystems)
	 * @param ColName - The column name in which the subsystems are located
	 * @param currentview - The View of the original Network (to function as a basis for style of the subnetworks)
	 * @param subSystems - A Collection of Objects identifying the different Subsystems 
	 */
	private synchronized void createSubNetworkViews(CyNetwork originalnetwork, String ColName, CyNetworkView currentview, Collection<Object> subSystems)
	{
		Set<String> subnetworknames = new HashSet<String>();
		CyRootNetwork rootNetwork = rootManager.getRootNetwork(originalnetwork);
		CyTable NodeTable = originalnetwork.getDefaultNodeTable();		
		HashSet<String> diffvals = new HashSet<String>();
		String parentName = originalnetwork.getRow(originalnetwork).get(CyNetwork.NAME,String.class);
		for(CyRow row : NodeTable.getAllRows())
		{
			try{
				String currentName = row.getRaw(ColName).toString();
				diffvals.add(currentName);
				String subNetworkName = parentName + subnetworkNameSeparator + currentName;
				subnetworknames.add(subNetworkName);
			}
			catch(NullPointerException e)
			{
				PrintFDebugger.Debugging(this, "Nullpointer found for some reason");
			}
		}		
		//Get the existing Networks (for this SUBNETWORK Column, otherwise we might create LOADS and LOADS of linkers...
		HashMap<CyNetwork,CyNetworkView> existingSubSystemViews = nvs.getExistingNetworks(diffvals);
		existingNodeLinks = new HashMap<CyNetworkView,Map<CyNode,Collection<CyNode>>>();
		HashMap<Object,CyNetworkView> subSystemViews = new HashMap<Object, CyNetworkView>();
		HashMap<Object,Set<CyNode>> subSystemNodes = new HashMap<Object, Set<CyNode>>();
		HashMap<Object,Set<CyEdge>> subSystemReactionEdges  = new HashMap<Object, Set<CyEdge>>();
		HashMap<Object,Set<CyEdge>> subSystemOutGoingEdges = new HashMap<Object, Set<CyEdge>>();
		HashMap<Object,CyNetwork> subSystemNetworks = new HashMap<Object, CyNetwork>();

		for(Object subSystem : subSystems)
		{
			//get the Reaction Nodes belonging to this subsystem
			subSystemNodes.put(subSystem,getSubSystemNodes(originalnetwork, NodeTable, ColName, subSystem));

			//And directly remove all ignored Nodes. This should not be necessary as there (should) be only reactions in this set yet, 			
			subSystemNodes.get(subSystem).removeAll(ignoredNodes);

			//assume, that there could eb nodes other than reactions in this set.


			//Initialize the Set of internal subsystem Edges. 
			//this set will be filled by the extendNodeSet method.
			subSystemReactionEdges.put(subSystem, new HashSet<CyEdge>());

			//get the adjacent Edges
			//we need to make sure, that ignored nodes do not get connected to the subnetwork. Thus, we will
			//subSystemReactionEdges.put(subSystem, getAdjacentEdges(subSystemNodes.get(subSystem),originalnetwork, false));
			//and remove those pointing to ignored nodes, otherwise they will be included in the subnetworks.

			//and add the Metabolite Nodes			
			//subSystemNodes.get(subSystem).addAll(getAdjacentCyNodes(subSystemReactionEdges.get(subSystem)));
			//now set up the ignored and non branching parts since they only now have their corresponding nodes in the subnetwork....

			/*
			 * lets do extend this subsystem so that it contains all Nodes that can be associated to the subsystem
			 * This mean: get all nodes connected to nodes within the network.
			 * Remove anything, that is in another network (i.e. has a distinct subnetwork set as property)
			 * During the process we have to ignore all (non gene) edges starting from locally non Branching nodes
			 * And we have to ignore all nodes which are ignored.
			 */
			int csize = subSystemNodes.get(subSystem).size();
			extendNodeSet(subSystemNodes.get(subSystem), subSystemReactionEdges.get(subSystem), originalnetwork, subSystem, ColName);
			while(subSystemNodes.get(subSystem).size() > csize)
			{
				csize = subSystemNodes.get(subSystem).size();
				extendNodeSet(subSystemNodes.get(subSystem), subSystemReactionEdges.get(subSystem), originalnetwork, subSystem, ColName);
			}

			//now get the outgoing Edges
			subSystemOutGoingEdges.put(subSystem,getOutgoingEdges(subSystemNodes.get(subSystem), subSystemReactionEdges.get(subSystem), originalnetwork, ColName));
			//and remove all internal subsystem edges and locally ignored edges.

			//and add the local non branching ones back in.
			//while keeping the ignored ones in. Since ignore is a stronger condition than non branch we will stick to this. 
			subSystemNodes.get(subSystem).removeAll(ignoredNodes);

			//now we are set up. So lets create a new Network and a new network view
			CyNetwork subNetwork = rootNetwork.addSubNetwork(subSystemNodes.get(subSystem),subSystemReactionEdges.get(subSystem));
			subSystemNetworks.put(subSystem ,subNetwork);
			String subNetworkName = parentName + subnetworkNameSeparator + subSystem.toString();
			subNetwork.getRow(subSystemNetworks.get(subSystem)).set(CyNetwork.NAME, subNetworkName);
			
			
			networkManager.addNetwork(subSystemNetworks.get(subSystem));
			CyNetworkView newModelView = networkViewFactory.createNetworkView(subSystemNetworks.get(subSystem));
			if(layoutAlgorithm == null)
			{
				//in case we want to keep the layout, lets try to put all nodes to their respective new positions.
				for(CyNode node : subNetwork.getNodeList())
				{
					CyNode orignode = originalnetwork.getNode(node.getSUID());
					if(orignode != null)
					{
						View<CyNode> cview = currentview.getNodeView(orignode);
						newModelView.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, cview.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
						newModelView.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, cview.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
					}
				}
			}
			nvs.addSubNetwork(subSystemNetworks.get(subSystem),newModelView);
			subSystemViews.put(subSystem,newModelView);	
			//At this point, no new nodes have yet been created 
			//so we should be able to simply check all nodes, whether they are linkers.
			//and create linkers from those nodes to the current nodes.
			PrintFDebugger.Debugging(this, "Setting up External Linkers for Network " + subSystem.toString());
			createLinkersToExternalNetworks(subNetwork,subSystemViews.get(subSystem),subNetworkName,subSystem.toString());
		}
			
		//Than add the links between the networks.

		for(Object subSystem : subSystems)
		{
			//create New Linker Nodes.
			LinkNodeProps.put(subSystem, new Vector<DelayedVizProp>());
			eventHelper.flushPayloadEvents();
			createLinkerNodes(originalnetwork, subSystemNetworks.get(subSystem), subSystemViews.get(subSystem),
					NodeTable, ColName, subSystem, subSystemOutGoingEdges.get(subSystem), subSystemNodes.get(subSystem),subSystemViews, existingSubSystemViews);
			eventHelper.flushPayloadEvents();
			DelayedVizProp.applyAll(subSystemViews.get(subSystem), LinkNodeProps.get(subSystem));
			for(CyNetworkView existingView : existingNodeLinks.keySet())				
			{
				Vector<DelayedVizProp> CurrentViewProps = new Vector<DelayedVizProp>();
				for(CyNode sourcenode : existingNodeLinks.get(existingView).keySet())
				{
					Collection<CyNode> outlinks = existingNodeLinks.get(existingView).get(sourcenode);
					int OutlinkCount = outlinks.size();
					int i = 0;
					View<CyNode> outNodeView = existingView.getNodeView(sourcenode);
					for(CyNode linkerNode : outlinks)
					{
						double alpha = i* 2*Math.PI / OutlinkCount;
						double xmod = Math.sin(alpha)*40;
						double ymod = Math.cos(alpha)*40;
						CurrentViewProps.add(new DelayedVizProp(linkerNode, BasicVisualLexicon.NODE_X_LOCATION, outNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION) +(int)xmod  ,false));
						CurrentViewProps.add(new DelayedVizProp(linkerNode, BasicVisualLexicon.NODE_Y_LOCATION, outNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION) +(int)ymod  ,false));
						i++;
					}


				}
				DelayedVizProp.applyAll(existingView, CurrentViewProps);							
				vmm.setVisualStyle(vmm.getVisualStyle(existingView), existingView);
				eventHelper.flushPayloadEvents();
				existingView.updateView();
			}
			networkViewManager.addNetworkView(subSystemViews.get(subSystem));
			vmm.setVisualStyle(vmm.getVisualStyle(currentview), subSystemViews.get(subSystem));
			vmm.getVisualStyle(currentview).apply(subSystemViews.get(subSystem));
			if(layoutAlgorithm != null)
			{
				Object context = layoutAlgorithm.createLayoutContext();
				insertTasksAfterCurrentTask(layoutAlgorithm.createTaskIterator(subSystemViews.get(subSystem), context, new HashSet<View<CyNode>>(), null));
			}
			subSystemViews.get(subSystem).fitContent();
			subSystemViews.get(subSystem).updateView();

		}
		
	}
	
	/**
	 * 
	 * @param subNetwork
	 */
	private void createLinkersToExternalNetworks(CyNetwork subNetwork, CyNetworkView subnetworkView, String subNetworkName, String subnetworkID)
	{
		CyTable nodeTable = subNetwork.getDefaultNodeTable();
		List<CyRow> rows = nodeTable.getAllRows();
		Vector<CyRow> linkerRows = new Vector<CyRow>();
		for(CyRow row : rows)
		{
			if(row.isSet(IDAREProperties.LINK_TARGET) && !row.get(IDAREProperties.LINK_TARGET,Long.class).equals(nodeTable.getColumn(IDAREProperties.LINK_TARGET).getDefaultValue()))
			{
				linkerRows.add(row);
			}
		}		
				
		PrintFDebugger.Debugging(this, "There are " + linkerRows.size() + " rows which have a linker set in the network.");
		//this should only contain rows with linker set.
		HashMap<CyNetworkView,Vector<DelayedVizProp>> CurrentViewProps = new HashMap<CyNetworkView,Vector<DelayedVizProp>>();		
		for(CyRow row : linkerRows)
		{
			//get the target network
			String targetNetworkName = row.get(IDAREProperties.LINK_TARGET_SUBSYSTEM, String.class);
			Long targetNodeID = row.get(IDAREProperties.LINK_TARGET, Long.class);
			CyNode subSysNode = subNetwork.getNode(row.get(CyNode.SUID, Long.class));
			// get the adjacent node (which the outside linker should link to)
			// there is only ever one edge for a linker
			CyNode subSysTargetNode = null;
			for(CyEdge edge : subNetwork.getAdjacentEdgeIterable(subSysNode, CyEdge.Type.ANY))
			{
				subSysTargetNode = edge.getSource().equals(subSysNode) ? edge.getTarget() : edge.getSource();
			}			
			View<CyNode> subSysTargetView = subnetworkView.getNodeView(subSysTargetNode);
			CyNetwork targetNetwork = null;
			for(CyNetwork net : networkManager.getNetworkSet())
			{
				if(net.getRow(net).get(CyNetwork.NAME, String.class).equals(targetNetworkName))
				{
					targetNetwork = net;
				}
			}		
			if(targetNetwork != null)
			{
				//get the matching node in the target network
				Collection<CyRow> matchrows = targetNetwork.getDefaultNodeTable().getMatchingRows(IDAREProperties.IDARE_NODE_UID, targetNodeID);
				PrintFDebugger.Debugging(this, "Trying to find the matching node for " + targetNodeID + " in network " + targetNetworkName + " found a total of " + matchrows.size() + " matching nodes");
				
				//There can only be the one item!
				CyRow targetRow = null;
				for(CyRow crow : matchrows)
				{
					targetRow = crow;
					CyNode targetnode = targetNetwork.getNode(targetRow.get(CyNode.SUID, Long.class));								
					CyNode linker = targetNetwork.addNode();
					//set the Name of the new node to the SUBSYSTEM - > which is the subsystem represented by the "outer nodes SUBSYSTEM column"  
					targetNetwork.getRow(linker).set(CyNetwork.NAME, subnetworkID);
					//and also set the IDARE_NODE_NAME accordingly. 
					targetNetwork.getRow(linker).set(IDAREProperties.IDARE_NODE_NAME, subnetworkID);
					//The New nodes IDARENodeType is Link 
					targetNetwork.getRow(linker).set(IDAREProperties.IDARE_NODE_TYPE, IDAREProperties.NodeType.IDARE_LINK);
					//Here we set the Metabolite Node that is linking out as target. (Since it is the same node and present in multiple subnetworks.)
					//Thus we need to refer to this node.
					targetNetwork.getRow(linker).set(IDAREProperties.LINK_TARGET, subNetwork.getRow(subSysTargetNode).get(IDAREProperties.IDARE_NODE_UID, Long.class));
					//and set the IDARETargetSubSystem property to point to the right Network. -> Need a listener here....
					targetNetwork.getRow(linker).set(IDAREProperties.LINK_TARGET_SUBSYSTEM, subNetworkName);

									
					CyEdge linkerEdge = targetNetwork.addEdge(targetnode, linker, true);
					Collection<CyNetworkView> views = networkViewManager.getNetworkViews(targetNetwork);
					//get the matching node in the subsystem network.

					for(CyNetworkView view : views)
					{
						View<CyNode> outNodeView = view.getNodeView(targetnode);
						double alpha = Math.random()* 2*Math.PI;
						double xmod = Math.sin(alpha)*40;
						double ymod = Math.cos(alpha)*40;
						if(!CurrentViewProps.containsKey(view))
						{
							CurrentViewProps.put(view, new Vector<DelayedVizProp>());
						}
						CurrentViewProps.get(view).add(new DelayedVizProp(linker, BasicVisualLexicon.NODE_X_LOCATION, outNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION) +(int)xmod  ,false));
						CurrentViewProps.get(view).add(new DelayedVizProp(linker, BasicVisualLexicon.NODE_Y_LOCATION, outNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION) +(int)ymod  ,false));
						PrintFDebugger.Debugging(this, "Adding a visual property that sets node " + targetnode.getSUID() + " in network " + targetNetworkName + " to position " + (int)xmod + "/" + (int)ymod);
					}					
					PrintFDebugger.Debugging(this, "Adding a linker to node " + targetnode.getSUID() + " in network " + targetNetworkName);
					nvs.addLink(linker, targetNetwork, subnetworkView, subSysTargetView);
				}
			}
			
		}
		eventHelper.flushPayloadEvents();
		for(CyNetworkView view : CurrentViewProps.keySet())
		{
			DelayedVizProp.applyAll(view, CurrentViewProps.get(view));										
			//eventHelper.flushPayloadEvents();
			view.updateView();
		}
	}
	
	/**
	 * Get all outgoing edges from a defined subsystem. This is defined as edges leading to reactions which are not part of the nodes of this subsystem
	 * @param subSysNodes -  all nodes in the subSystem
	 * @param net - the original network
	 * @return
	 */
	private Set<CyEdge> getOutgoingEdges(Set<CyNode> subSysNodes, Set<CyEdge> subSysEdges,CyNetwork net, String ColName)
	{
		HashSet<CyEdge> outgoingEdges = new HashSet<CyEdge>();		
		CyTable nodeTab = net.getDefaultNodeTable();
		for(CyNode node : subSysNodes)
		{
			//ignore the non branching nodes and ignored nodes
			if(ignoredNodes.contains(node) | noBranchNodes.contains(node))
			{
				continue;				
			}							
			if(nodeTab.getColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE) != null )
			{
				boolean isSpecies= false;				
				CyRow noderow = nodeTab.getRow(node.getSUID());

				if(noderow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) != null)
				{
					isSpecies = noderow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_SPECIES);
				}
				if(isSpecies)
				{
					List<CyEdge> current_edges = net.getAdjacentEdgeList(node, CyEdge.Type.ANY);
					for(CyEdge edge : current_edges)
					{
						//if its not in the subSystem Edges
						if(!subSysEdges.contains(edge))
						{
							CyRow OppositeSite = edge.getSource().getSUID() == node.getSUID() ? nodeTab.getRow(edge.getTarget().getSUID()) : nodeTab.getRow(edge.getSource().getSUID());
							//and not the default (i.e. there is a value)
							if(OppositeSite.getRaw(ColName) == null || OppositeSite.getRaw(ColName) != net.getDefaultNodeTable().getColumn(ColName).getDefaultValue())
							{
								//if this is an edge that is not IN this set, and it targets a node, that has a non empty SUBSYSTEM Column, than this is an outgoing edge!
								outgoingEdges.add(edge);							
							}
						}
					}
				}
				/*
				CyRow noderow = nodeTab.getRow(node.getSUID());
				boolean isSpecies = false;
				if(noderow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) != null )
				{
					//only check if it is non null
					isSpecies = noderow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_SPECIES);
				}
				//Only species are supposed to link between subnetworks!
				if(isSpecies)
				{
					List<CyEdge> current_edges = net.getAdjacentEdgeList(node, CyEdge.Type.ANY);
					for(CyEdge edge : current_edges)
					{
						//We can be sure, that we do not have a reaction node, so we can simply check both source and target node whether they are reactions
						CyRow sourcerow = nodeTab.getRow(edge.getSource().getSUID());
						boolean isSourceReaction = false;
						if(sourcerow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) != null)
						{
							//If this is null, it is definitely no reaction but something else.
							//so we only check it if it is non null.
							isSourceReaction = sourcerow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION);
						}

						CyRow targetrow = nodeTab.getRow(edge.getTarget().getSUID());
						boolean isTargetReaction = false;
						if(targetrow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) != null)
						{
							//If this is null, it is definitely no reaction but something else.
							//so we only check it if it is non null.
							isTargetReaction = targetrow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION);
						}						
						//now if either source or target node are reactions AND not in the subsystem, then this is an outgoing edge!
						if(isSourceReaction)
						{
							if(!subSysNodes.contains(edge.getSource()))
							{
								outgoingEdges.add(edge);
							}
						}
						if(isTargetReaction)
						{
							if(!subSysNodes.contains(edge.getTarget()))
							{
								outgoingEdges.add(edge);
							}
						}

					}
				}*/

			}
		}		

		return outgoingEdges;
	}
	/**
	 * Extend the current set of nodes by walking along the edges and checking to see whether there are additional nodes to use.
	 * @param subSysNodes
	 * @param subSysEdges
	 * @param net
	 * @param Subsystem
	 * @param ColName
	 */
	private void extendNodeSet(Set<CyNode> subSysNodes, Set<CyEdge> subSysEdges, CyNetwork net, Object Subsystem, String ColName)
	{
		CyTable nodeTab = net.getDefaultNodeTable();
		Set<CyEdge> edges = new HashSet<CyEdge>();
		//Class coltype = net.getDefaultNodeTable().getColumn(ColName).getListElementType();
		//boolean isList = coltype != null;
		for(CyNode node : subSysNodes)
		{
			//skip the ignored nodes
			if(ignoredNodes.contains(node))
			{
				continue;
			}
			//get all edges from the remaining supplied nodes.
			if(nodeTab.getColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE) != null )
			{
				CyRow noderow = nodeTab.getRow(node.getSUID());
				//If this is a gene or Protein or metabolite, only add edges which do not point to reactions outside the current subsystem.
				//In fact, we should never extend from a gene.
				boolean isReaction = false;
				boolean isSpecies = false;
				if(noderow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) != null)
				{
					//If this is null, it is definitely no reaction but something else.
					//so we only check it if it is non null.
					isReaction = noderow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION);
					isSpecies = noderow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_SPECIES);
				}
				//This is not a reaction or species, so we only add nodes which are not reactions or species!
				if(!isReaction & !isSpecies) // isProtein || isSpecies || isGene)
				{
					PrintFDebugger.Debugging(this, "Found a Non Species, non interaction item. extending.");
					List<CyEdge> current_edges = net.getAdjacentEdgeList(node, CyEdge.Type.ANY);
					for(CyEdge edge : current_edges)
					{												
						//We can be sure, that we do not have a reaction node, 
						//so we can simply check both source and target node whether they are reactions
						CyRow sourcerow = nodeTab.getRow(edge.getSource().getSUID());
						boolean isSourceReaction = false;
						boolean isSourceSpecies= false;
						if(sourcerow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) != null)
						{
							//If this is null, it is definitely no reaction but something else.
							//so we only check it if it is non null.
							isSourceReaction = sourcerow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION);
							isSourceSpecies = sourcerow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_SPECIES);
						}

						CyRow targetrow = nodeTab.getRow(edge.getTarget().getSUID());
						boolean isTargetReaction = false;
						boolean isTargetSpecies = false;
						if(targetrow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) != null)
						{
							//If this is null, it is definitely no reaction but something else.
							//so we only check it if it is non null.
							isTargetReaction = targetrow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION);
							isTargetSpecies = targetrow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_SPECIES);
						}

						//boolean isTargetReaction = targetrow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION);
						//if the target is a reaction and its part of the subNet, we can add it, otherwise we do nothing.

						if((isTargetReaction || isTargetSpecies) && subSysNodes.contains(edge.getTarget()))
						{
							edges.add(edge);
							PrintFDebugger.Debugging(this, "Extending to node " + edge.getTarget().getSUID());
						}
						if((isSourceSpecies || isSourceReaction) && subSysNodes.contains(edge.getSource()))
						{
							edges.add(edge);
							PrintFDebugger.Debugging(this, "Extending to node " + edge.getSource().getSUID());
						}
						// if neither source nor target are reactions, we can add it as well.
						if (!isSourceReaction && !isTargetReaction && !isSourceSpecies && !isTargetSpecies)
						{
							edges.add(edge);
							PrintFDebugger.Debugging(this, "Extending to Source node " + edge.getSource().getSUID());
							PrintFDebugger.Debugging(this, "Extending to node target " + edge.getTarget().getSUID());
						}							

					}

				}
				else if(isReaction)					
				{	//this is a Reaction (and part of the network) Thus we can savely add all adjacent nodes.  							
					edges.addAll(net.getAdjacentEdgeList(node, CyEdge.Type.ANY));
					PrintFDebugger.Debugging(this, "Adding all edges for node" + node.getSUID());
				}
				else if(isSpecies)
				{
					//This is a species. Thus, we can add all attached nodes, which are not reactions nodes.
					List<CyEdge> current_edges = net.getAdjacentEdgeList(node, CyEdge.Type.ANY);					
					for(CyEdge edge : current_edges)
					{
						CyRow OppositeSite = edge.getSource().getSUID() == node.getSUID() ? nodeTab.getRow(edge.getTarget().getSUID()) : nodeTab.getRow(edge.getSource().getSUID());
						if(OppositeSite.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) != IDAREProperties.NodeType.IDARE_REACTION)
						{
							edges.add(edge);
						}
/*						
						if(net.getDefaultNodeTable().getColumn(ColName).getListElementType() != null)
						{
							if((OppositeSite.getRaw(ColName) == null) || //if the opposite site is null i.e. it is not set and thus definitely not in another Subsystem
									(OppositeSite.getRaw(ColName).equals(net.getDefaultNodeTable().getColumn(ColName).getDefaultValue())) || // or its at the default (and therefore again not in another subsystem)
									OppositeSite.getList(ColName,net.getDefaultNodeTable().getColumn(ColName).getListElementType()).contains(Subsystem)) // or it is in the very same subsystem, we add the edge.
							{
								PrintFDebugger.Debugging(this,"Found Adjacent Node in the SubSystem");
								edges.add(edge);
							}
						}
						else
						{
							if((OppositeSite.getRaw(ColName) == null) ||//if the opposite site is null i.e. it is not set and thus definitely not in another Subsystem
									OppositeSite.getRaw(ColName).equals(net.getDefaultNodeTable().getColumn(ColName).getDefaultValue()) ||  // or its at the default (and therefore again not in another subsystem)
									(OppositeSite.getRaw(ColName).equals(Subsystem))) // or it is in the same subsystem.
							{
								PrintFDebugger.Debugging(this,"Found Adjacent Node in the SubSystem");
								edges.add(edge);

							}
						}
						/*
						CyRow targetrow = nodeTab.getRow(edge.getTarget().getSUID());
						if(targetrow.getRaw(ColName) == net.getDefaultNodeTable().getColumn(ColName).getDefaultValue())
						{
							edges.add(edge);
						}*/
					}
				}
			}


		}
		//Now get all nodes from the CyNetwork, that are not reactions and which are connected to the network by any of the edges
		//add the 
		Set<CyNode> newnodes = new HashSet<CyNode>();

		for(CyEdge edge : edges)
		{			
			CyNode sourcenode = edge.getSource();
			CyNode targetnode = edge.getTarget();
			//if Any of the nodes is in the ignored node we skip the edge 
			if(ignoredNodes.contains(sourcenode) | ignoredNodes.contains(targetnode))
			{
				continue;
			}
			//otherwise, this is either an edge to a non reaction node or it is an edge to a reaction node within the subsystem.
			//so we can simply add it.
			newnodes.add(edge.getSource());
			newnodes.add(edge.getTarget());
			subSysEdges.add(edge);

		}
		subSysNodes.addAll(newnodes);
	}

	/**
	 * Create Linker Nodes for all Subsystems. 
	 * We will only create one linker for each Metabolite (i.e. Node in the original network linking out) and each external subsystem.
	 * 
	 * @param originalnetwork -  A reference to the network the subsystems are based on. 
	 * @param newnetwork - The SubNetwork to which linkers should be added. 
	 * @param newNetworkView - The {@link CyNetworkView} to the SubNetwork
	 * @param NodeTable - The NodeTable of the original Network
	 * @param ColName - The Column Name in which the Subsystems are stored
	 * @param origSubSystem - The SubSystem Identifier for the SubSystem the linker nodes should be added to
	 * @param OutGoingEdges - A Set of Edges that are pointing out of the SubNetwork
	 * @param SubsystemNodes - A Set of Nodes that are in the SubSystem (represented by <b>newnetwork</b>)
	 * @param subSystemViews - A Map mapping SubSystem identifiers to {@link CyNetworkView}s.
	 * @return the Set of created Linker Nodes.
	 */
	private Set<CyNode> createLinkerNodes(CyNetwork originalnetwork, CyNetwork newnetwork, CyNetworkView newNetworkView, CyTable NodeTable,
			String ColName, Object origSubSystem, Set<CyEdge> OutGoingEdges, Set<CyNode> SubsystemNodes, HashMap<Object,CyNetworkView> subSystemViews, HashMap<CyNetwork, CyNetworkView> existingNetworks)
			{
		
		//First create a map which assigns all external nodes connected to an internal node
		HashMap<CyNode, Set<ExternalCyNodeDirection>> innerToOuterNode = new HashMap<CyNode, Set<ExternalCyNodeDirection>>();
		HashMap<CyNode, Set<String>> innerNodeToSubSystem = new HashMap<CyNode, Set<String>>();
		Set<CyNode> LinkerNodes = new HashSet<CyNode>();

		HashMap<Object,NetworkAndView> existingNetworkMatch = new HashMap<Object, SubNetworkCreationTask.NetworkAndView>();
		for(CyNetwork network : existingNetworks.keySet())
		{
			existingNetworkMatch.put(network.getRow(network).get(CyNetwork.NAME,String.class), new NetworkAndView(network, existingNetworks.get(network)));
		}

		for(CyEdge OutEdge : OutGoingEdges)
		{

			CyNode OutTarget = OutEdge.getTarget(); 
			CyNode outSource = OutEdge.getSource();
			List<Object> targetSubSystemNames = new LinkedList<Object>();
			List<Object> sourceSubSystemNames = new LinkedList<Object>();
			if(NodeTable.getColumn(ColName).getListElementType() == null)
			{
				Object targetSubSysName = NodeTable.getRow(OutEdge.getTarget().getSUID()).getRaw(ColName);
				targetSubSystemNames.add(targetSubSysName);
				Object sourceSubSysName = NodeTable.getRow(OutEdge.getSource().getSUID()).getRaw(ColName);
				sourceSubSystemNames.add(sourceSubSysName);
			}
			else
			{
				if(NodeTable.getRow(OutEdge.getTarget().getSUID()).getList(ColName, NodeTable.getColumn(ColName).getListElementType()) != null)
				{
					//if this has something stored here:
					for(Object item : NodeTable.getRow(OutEdge.getTarget().getSUID()).getList(ColName, NodeTable.getColumn(ColName).getListElementType()))
					{
						targetSubSystemNames.add(item);	
					}
				}
				else
				{
					targetSubSystemNames.add(null);
				}
				if(NodeTable.getRow(OutEdge.getSource().getSUID()).getList(ColName, NodeTable.getColumn(ColName).getListElementType()) != null)
				{

					for(Object item : NodeTable.getRow(OutEdge.getSource().getSUID()).getList(ColName, NodeTable.getColumn(ColName).getListElementType()))
					{
						sourceSubSystemNames.add(item);	
					}								
				}
				else
				{
					sourceSubSystemNames.add(null);
				}
			}

			//If the Target Node is not within the subsystem and the Target Node is in a subsystem 
			//and the original node is not in the ignored or non branching and the target subsystem is in the list of created subystems
			//then and only then create the linker node. Otherwise skip it.
			for(Object targetSubSysName : targetSubSystemNames)
			{
				for(Object sourceSubSysName : sourceSubSystemNames)
				{
					if(!SubsystemNodes.contains(OutTarget) && !isempty(NodeTable.getRow(OutEdge.getTarget().getSUID()), ColName) 
							&& !noBranchNodes.contains(outSource) && !ignoredNodes.contains(outSource) && (subSystems.contains(targetSubSysName) || existingNetworkMatch.containsKey(targetSubSysName)))
					{
						//Do a Sanity check and see whether at least source is in
						if(!SubsystemNodes.contains(OutEdge.getSource()))
						{
							continue;
						}
						//the target node is external. so assign it to the source node.
						if(!innerToOuterNode.containsKey(OutEdge.getSource()))
						{
							//create a new Set if it does not exist

							innerToOuterNode.put(OutEdge.getSource(),new HashSet<ExternalCyNodeDirection>());
							innerNodeToSubSystem.put(OutEdge.getSource(),new HashSet<String>());
						}
						else
						{

							if(innerNodeToSubSystem.get(OutEdge.getSource()).contains(targetSubSysName.toString()) || (!subSystems.contains(targetSubSysName) && !existingNetworkMatch.containsKey(targetSubSysName)))
							{	
								//if the current edge points to a subsystem already added to this node, skip it.
								//also skip it if it is not one of the created subsystems

								continue;
							}
						}
						// and add the node.
						innerToOuterNode.get(OutEdge.getSource()).add(new ExternalCyNodeDirection(OutEdge.getTarget(), OutEdge.getSource(),ExternalCyNodeDirection.OUTGOING,targetSubSysName));				
						innerNodeToSubSystem.get(OutEdge.getSource()).add(targetSubSysName.toString());

					}
					//If the Target Node is not within the subsystem and the Target Node is in a subsystem 
					//and the original node is not in the ignored or non branching and the target subsystem is in the list of created subystems
					//then and only then create the linker node. Otherwise skip it.
					else if(!SubsystemNodes.contains(OutEdge.getSource()) && !isempty(NodeTable.getRow(OutEdge.getSource().getSUID()), ColName)&& 
							!noBranchNodes.contains(OutTarget) && !ignoredNodes.contains(OutTarget) && (subSystems.contains(sourceSubSysName) || existingNetworkMatch.containsKey(sourceSubSysName)))
					{

						//Do a Sanity check and see whether at least source is in
						if(!SubsystemNodes.contains(OutEdge.getTarget()))
						{
							continue;
						}

						if(!innerToOuterNode.containsKey(OutEdge.getTarget()))
						{
							//create a new Set if it does not exist
							innerToOuterNode.put(OutEdge.getTarget(),new HashSet<ExternalCyNodeDirection>());
							innerNodeToSubSystem.put(OutEdge.getTarget(),new HashSet<String>());
						}
						else
						{
							if(innerNodeToSubSystem.get(OutEdge.getTarget()).contains(sourceSubSysName.toString()) || (! subSystems.contains(sourceSubSysName) && !existingNetworkMatch.containsKey(sourceSubSysName)))
							{	
								//if the current edge points to a subsystem already added to this node, skip it.
								//also skip it if it is not one of the created subsystems

								continue;
							}
						}
						// and add the node.
						innerToOuterNode.get(OutEdge.getTarget()).add(new ExternalCyNodeDirection(OutEdge.getSource(),OutEdge.getTarget(), ExternalCyNodeDirection.INCOMING,sourceSubSysName));				
						innerNodeToSubSystem.get(OutEdge.getTarget()).add(sourceSubSysName.toString());
					}					
				}
			}

		}

		//now, this is set up, so we need to create the corresponding Nodes and edges for the new network.
		for(CyNode node : innerToOuterNode.keySet())
		{
			for(ExternalCyNodeDirection Target : innerToOuterNode.get(node)){
				CyNode newNode = newnetwork.addNode();
				LinkerNodes.add(newNode);
				//set the Name of the new node to the SUBSYSTEM - > which is the subsystem represented by the "outer nodes SUBSYSTEM column"  
				newnetwork.getRow(newNode).set(CyNetwork.NAME, Target.externalNodeSubSystem.toString());
				//and also set the IDARE_NODE_NAME accordingly. 
				newnetwork.getRow(newNode).set(IDAREProperties.IDARE_NODE_NAME, Target.externalNodeSubSystem.toString());
				//The New nodes IDARENodeType is Link 
				newnetwork.getRow(newNode).set(IDAREProperties.IDARE_NODE_TYPE, IDAREProperties.NodeType.IDARE_LINK);
				//Here we set the Metabolite Node that is linking out as target. (Since it is the same node and present in multiple subnetworks.)
				//Thus we need to refer to this node.
				newnetwork.getRow(newNode).set(IDAREProperties.LINK_TARGET, originalnetwork.getRow(Target.internalNode).get(IDAREProperties.IDARE_NODE_UID, Long.class));

				//get the view of the other subsystem
				CyNetworkView targetSubSystemView = null;
				CyNetwork targetSubNetwork = null;

				//TODO!!! Finish the implementations
				if(existingNetworkMatch.containsKey(Target.externalNodeSubSystem))
				{					
					targetSubNetwork = existingNetworkMatch.get(Target.externalNodeSubSystem).network;
					targetSubSystemView = existingNetworkMatch.get(Target.externalNodeSubSystem).view;
				}
				else
				{					
					targetSubSystemView = subSystemViews.get(Target.externalNodeSubSystem);
					targetSubNetwork = targetSubSystemView.getModel();
				}

				//and set the IDARETargetSubSystem property to point to the right Network. -> Need a listener here....
				newnetwork.getRow(newNode).set(IDAREProperties.LINK_TARGET_SUBSYSTEM, targetSubNetwork.getRow(targetSubNetwork).get(CyNetwork.NAME,String.class));
				if(targetSubSystemView == null)
				{					
					nvs.addNetworkLink(newNode, newnetwork, existingNetworkMatch.get(Target.externalNodeSubSystem).network, Target.internalNode);
				}
				else
				{
					nvs.addLink(newNode, newnetwork, targetSubSystemView, targetSubSystemView.getNodeView(Target.internalNode));
				}
				//Add the original node to the linker list
				if(!existingNodeLinks.containsKey(newNetworkView))
				{
					existingNodeLinks.put(newNetworkView, new HashMap<CyNode, Collection<CyNode>>());
				}
				if(!existingNodeLinks.get(newNetworkView).containsKey(node))
				{
					existingNodeLinks.get(newNetworkView).put(node, new Vector<CyNode>());							
				}
				existingNodeLinks.get(newNetworkView).get(node).add(newNode);

				//Now create a new edge
				LinkNodeProps.get(origSubSystem).add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_WIDTH,
						Target.externalNodeSubSystem.toString().length() * 6., true));
				LinkNodeProps.get(origSubSystem).add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_HEIGHT, 20., true));
				//TODO: This can be refined depending on the Edge Direction....
				newnetwork.addEdge(node, newNode, false);						
				if(existingNetworkMatch.containsKey(Target.externalNodeSubSystem))
				{
					//now this gets interesting. We need to create an additional node in the external subsystem, that links back to the origin node.
					CyNetwork existingNetwork = existingNetworkMatch.get(Target.externalNodeSubSystem).network;
					CyNetworkView existingView = existingNetworkMatch.get(Target.externalNodeSubSystem).view;
					CyNode newlinker = existingNetwork.addNode();

					existingNetwork.getRow(newlinker).set(CyNetwork.NAME, newnetwork.getRow(newnetwork).get(CyNetwork.NAME, String.class));
					//and also set the IDARE_NODE_NAME accordingly. 
					existingNetwork.getRow(newlinker).set(IDAREProperties.IDARE_NODE_NAME, newnetwork.getRow(newnetwork).get(CyNetwork.NAME, String.class));
					//The New nodes IDARENodeType is Link 
					existingNetwork.getRow(newlinker).set(IDAREProperties.IDARE_NODE_TYPE, IDAREProperties.NodeType.IDARE_LINK);
					existingNetwork.getRow(newlinker).set(IDAREProperties.LINK_TARGET_SUBSYSTEM, newnetwork.getRow(newnetwork).get(CyNetwork.NAME,String.class));
					CyNetworkView newSubSysView = subSystemViews.get(origSubSystem);
					//eventHelper.flushPayloadEvents();
					View<CyNode> newSubSystemNodeView = newSubSysView.getNodeView(Target.internalNode);	
					nvs.addLink(newlinker, existingNetwork, newSubSysView, newSubSystemNodeView);
					if(existingView != null)
					{	
						if(!existingNodeLinks.containsKey(existingView))
						{
							existingNodeLinks.put(existingView, new HashMap<CyNode, Collection<CyNode>>());
						}
						if(!existingNodeLinks.get(existingView).containsKey(Target.internalNode))
						{
							existingNodeLinks.get(existingView).put(Target.internalNode, new Vector<CyNode>());							
						}
						existingNodeLinks.get(existingView).get(Target.internalNode).add(newlinker);
						//create the views otherwise we will get null pointers when putting the views.						
					}
					existingNetwork.addEdge(Target.internalNode, newlinker, false);
				}

			}			
		}
		return LinkerNodes;
	}

	/**
	 * Check whether the entry of Column ColName in CyRow row is empty. I.e. it is either null or its string representation is an empty string.
	 * @param row - The row in question
	 * @param ColName -  the Column to check.
	 * @return Whether the column is empty in the given row
	 */
	public static boolean isempty(CyRow row, String ColName)
	{
		//we will assume, that a null value means it is empty. If its not null, we will check 
		if(row.getRaw(ColName) != null)
		{
			//Anything that is not "" or null will be considered as not empty. 
			if(row.getRaw(ColName).toString() != "")
			{
				return false;
			}			
		}
		return true;
	}
	/**
	 * Check whether a given Object is either empty or an empty String
	 * @return Whether the entry is empty or represented by an empty string
	 */
	public static boolean isempty(Object Entry)
	{
		//we will assume, that a null value means it is empty. If its not null, we will check 
		if(Entry != null)
		{
			//Anything that is not "" or null will be considered as not empty. 
			if(!Entry.toString().equals(""))
			{
				return false;
			}			
		}
		return true;
	}
	/**
	 * Get all nodes from a Given Network belonging to a specific Subsystem -  This is essentially the function from the Cookbook. 
	 * @param network - The Network to look up the nodes
	 * @param table - The corresponding Table
	 * @param ColName - The Name of the Column where the Subsystem information is stored
	 * @param value - The Value looked for in the column
	 * @return A Set of CyNodes in the specified subsystem.
	 */
	private Set<CyNode> getSubSystemNodes(CyNetwork network, CyTable table, String ColName, Object value)
	{

		Collection<CyRow> matchingRows = null;
		if(table.getColumn(ColName) == null)
		{
			return new HashSet<CyNode>();
		}		
		if( table.getColumn(ColName).getListElementType() == null)
		{
			matchingRows = table.getMatchingRows(ColName, value);
		}
		else
		{
			List<CyRow> rows = table.getAllRows();
			matchingRows = new Vector<CyRow>();
			for(CyRow row : rows)
			{
				if(row.isSet(ColName))
				{
					for(Object item : row.getList(ColName, table.getColumn(ColName).getListElementType()))
					{
						if(item.equals(value))
						{
							if(!matchingRows.contains(row))
							{
								matchingRows.add(row);
							}
						}
					}
				}
			}	
		}					

		final Set<CyNode> nodes = new HashSet<CyNode>();
		final String primaryKeyColname = table.getPrimaryKey().getName();
		for (final CyRow row : matchingRows)
		{
			final Long nodeId = row.get(primaryKeyColname, Long.class);
			if (nodeId == null)
			{
				continue;
			}

			final CyNode node = network.getNode(nodeId);
			if (node == null)
			{
				continue;
			}
			nodes.add(node);
		}
		return nodes;		
	}


	/**
	 * Get the Adjacent Edges to the provided nodes in the provided network
	 * @param Nodes - The Nodes to retrieve Edges for
	 * @param net -  The Network in which to retrieve edges
	 * @param useGeneEdges - Whether to include Edges Labeled as GENE Edges or not (this is necessary to avoid linking things over Genes)
	 * @return A Set of CyEdges linking out of the pprovided Nodes.
	 */
	private Set<CyEdge> getAdjacentEdges(Set<CyNode> Nodes, CyNetwork net, boolean useGeneEdges)
	{
		Set<CyEdge> edges = new HashSet<CyEdge>();
		for(CyNode node : Nodes)
		{
			List<CyEdge> edgelist = net.getAdjacentEdgeList(node, CyEdge.Type.ANY);
			List<CyEdge> metaboliteEdges = new LinkedList<CyEdge>();
			for(CyEdge edge : edgelist)
			{
				CyTable tab = net.getDefaultEdgeTable();
				if(tab.getColumn(IDAREProperties.IDARE_EDGE_PROPERTY) != null && useGeneEdges){
					//if we created this and added the appropriate field, than we can restrict it, otherwise "We dont know"
					CyRow edgerow = net.getDefaultEdgeTable().getRow(edge.getSUID());
					//if(!edgerow.get(IDAREProperties.IDARE_EDGE_PROPERTY, String.class).equals(IDAREProperties.GENE_EDGE_ID))
					//{
					//	metaboliteEdges.add(edge);
					//}
				}
				else
				{
					metaboliteEdges.add(edge);
				}

			}
			edges.addAll(metaboliteEdges);
		}

		return edges;
	}	
	/**
	 * Get all nodes adjacent to a given set of Edges (i.e. all sources and targets)
	 * @param Edges
	 * @return
	 */
	private Set<CyNode> getAdjacentCyNodes(Set<CyEdge> Edges)
	{
		Set<CyNode> nodes = new HashSet<CyNode>();
		for(CyEdge edge : Edges)
		{
			nodes.add(edge.getSource());
			nodes.add(edge.getTarget());			
		}		
		return nodes;
	}
	/**
	 * Helper class to Combine some information about the directionality 
	 * @author Thomas Pfau
	 *
	 */
	private class ExternalCyNodeDirection{
		public static final String OUTGOING = "OUTGOING";
		public static final String INCOMING = "INCOMING";
		public final CyNode externalNode;
		public final Object externalNodeSubSystem;
		public final CyNode internalNode;
		public final String direction;

		public ExternalCyNodeDirection(CyNode externalNode,CyNode internalNode, String direction, Object externalNodeSubSystem)
		{
			this.direction = direction;
			this.externalNode = externalNode; 
			this.internalNode = internalNode;
			this.externalNodeSubSystem = externalNodeSubSystem;
		}

	}
	/**
	 * Helper class to Combine some information about the directionality 
	 * @author Thomas Pfau
	 *
	 */
	private class NetworkAndView{
		public final CyNetwork network;
		public final CyNetworkView view;

		public NetworkAndView(CyNetwork network, CyNetworkView view)
		{
			this.network = network;
			this.view = view; 

		}

	}

}
