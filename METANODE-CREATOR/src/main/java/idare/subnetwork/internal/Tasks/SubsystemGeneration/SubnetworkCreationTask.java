package idare.subnetwork.internal.Tasks.SubsystemGeneration;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.ThirdParty.DelayedVizProp;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.subnetwork.internal.NetworkViewSwitcher;
import idare.subnetwork.internal.NoNetworksToCreateException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;
/**
 * A Task creating Subnetworks for a Network.
 * @author Thomas Pfau
 *
 */
public class SubnetworkCreationTask extends AbstractTask implements RequestsUIHelper{
	public static final String subnetworkNameSeparator = "_";
	

	private final CyServiceRegistrar registry;
	
	
	@Tunable	
	public SubNetworkProperties params;  
	
	
	@Tunable(description="Keep parent network links")
	public boolean keepoldLinks;
	
	
	private final IDARESettingsManager ism;
	private final NetworkViewSwitcher nvs;
		
	private Map<CyNetworkView,Collection<DelayedVizProp>> LinkNodeProps;	
	private Map<CyNetworkView,Map<CyNode,Collection<CyNode>>> existingNodeLinks;
	private Map<CyNetwork,Map<Long,Collection<CyNetwork>>> createdNodeLinks;

	/**
	 * Default constructor 
	 * @param reg a {@link CyServiceRegistrar} to obtain necessary services from
	 * @param nvs The {@link NetworkViewSwitcher} to add the linker information to 
	 * @param ism the Settings Manager to obtain and NodeIDs and property data..
	 */
	public SubnetworkCreationTask(CyServiceRegistrar reg, NetworkViewSwitcher nvs, IDARESettingsManager ism) {
		super();
		this.registry = reg;		
		this.nvs = nvs;		
		this.ism = ism;
		LinkNodeProps = new HashMap<CyNetworkView, Collection<DelayedVizProp>>();
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		if(params.subSystems.isEmpty())
		{
			throw new NoNetworksToCreateException();
		}
		createSubNetworkViews(params.currentNetwork, params.currentNetworkView, params.ColumnName, params.subSystems);
	}

	/**
	 * Create the subnetwork views for all <b>subSystems</b> for the original network based on the Column represented by the columnname provided and 
	 * @param originalnetwork - The original network (including all subsystems)
	 * @param ColName - The column name in which the subsystems are located
	 * @param currentview - The View of the original Network (to function as a basis for style of the subnetworks)
	 * @param subSystems - A Collection of Objects identifying the different Subsystems 
	 */
	private synchronized void createSubNetworkViews(CyNetwork originalnetwork, CyNetworkView currentview, String ColName, Collection<Object> subSystems)
	{
		CyNetworkManager netMgr = registry.getService(CyNetworkManager.class);
		CyNetworkViewFactory networkViewFactory = registry.getService(CyNetworkViewFactory.class);
		CyEventHelper eventHelper = registry.getService(CyEventHelper.class);
		VisualMappingManager vmm = registry.getService(VisualMappingManager.class);
		CyNetworkViewManager networkViewManager = registry.getService(CyNetworkViewManager.class);
		CyRootNetworkManager cyRootNetMgr = registry.getService(CyRootNetworkManager.class);
		CyRootNetwork rootNetwork = cyRootNetMgr.getRootNetwork(originalnetwork);
		CyTable NodeTable = originalnetwork.getDefaultNodeTable();		

		//Get the existing Networks (for this SUBNETWORK Column, otherwise we might create LOADS and LOADS of linkers...
		
		//HashMap<CyNetwork,CyNetworkView> existingSubSystemViews = nvs.getExistingNetworks(originalnetwork,ColName);
		existingNodeLinks = new HashMap<CyNetworkView,Map<CyNode,Collection<CyNode>>>();
		createdNodeLinks = new HashMap<CyNetwork, Map<Long,Collection<CyNetwork>>>();
		HashMap<Object,CyNetworkView> subSystemViews = new HashMap<Object, CyNetworkView>();
		HashMap<Object,Set<CyNode>> subSystemNodes = new HashMap<Object, Set<CyNode>>();
		HashMap<Object,Set<CyEdge>> subSystemReactionEdges  = new HashMap<Object, Set<CyEdge>>();
		HashMap<Object,Set<LinkInfo>> subSystemOutGoingEdges = new HashMap<Object, Set<LinkInfo>>();
		HashMap<Object,CyNetwork> subSystemNetworks = new HashMap<Object, CyNetwork>();
		
		for(Object subSystem : subSystems)
		{
			//get the Reaction Nodes belonging to this subsystem
			subSystemNodes.put(subSystem,getSubSystemNodes(originalnetwork, NodeTable, ColName, subSystem));

			//And directly remove all ignored Nodes. This should not be necessary as there (should) be only reactions in this set yet, 			
			subSystemNodes.get(subSystem).removeAll(params.ignoredNodes);

			//Initialize the Set of internal subsystem Edges. 
			//this set will be filled by the extendNodeSet method.
			subSystemReactionEdges.put(subSystem, new HashSet<CyEdge>());
			
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
			
			//and remove all internal subsystem edges and locally ignored edges.

			//and add the local non branching ones back in.
			//while keeping the ignored ones in. Since ignore is a stronger condition than non branch we will stick to this. 
			subSystemNodes.get(subSystem).removeAll(params.ignoredNodes);

			//now we are set up. So lets create a new Network and a new network view
			CyNetwork subNetwork = rootNetwork.addSubNetwork(subSystemNodes.get(subSystem),subSystemReactionEdges.get(subSystem));
			String subNetworkName = subSystem.toString();					
			nvs.addNetworkToTree(originalnetwork, subNetwork, ColName, subNetworkName);
			subSystemNetworks.put(subSystem ,subNetwork);
			
			
			subNetwork.getRow(subSystemNetworks.get(subSystem)).set(CyNetwork.NAME, nvs.getSubNetworkName(subNetwork));
			
			//now get the outgoing Edges
			netMgr.addNetwork(subSystemNetworks.get(subSystem));
			CyNetworkView newModelView = networkViewFactory.createNetworkView(subSystemNetworks.get(subSystem));
			
			if(params.layoutAlgorithm == null)
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
			subSystemOutGoingEdges.put(subSystem,getLinkingInfo(subSystemNodes.get(subSystem), subSystemReactionEdges.get(subSystem), 
					originalnetwork, subNetwork, ColName, subNetworkName));			
			nvs.addSubNetworkView(subSystemNetworks.get(subSystem),newModelView);
			subSystemViews.put(subSystem,newModelView);	
			//At this point, no new nodes have yet been created 
			//so we should be able to simply check all nodes, whether they are linkers.
			//and create linkers from those nodes to the current nodes.
			createLinkerNodes(originalnetwork, subSystemNetworks.get(subSystem), subSystemViews.get(subSystem),
					ColName, subSystemOutGoingEdges.get(subSystem));
		}
			
		//Than add the links between the networks.
		eventHelper.flushPayloadEvents();
		
		for(CyNetworkView existingView : existingNodeLinks.keySet())				
		{
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
					LinkNodeProps.get(existingView).add(new DelayedVizProp(linkerNode, BasicVisualLexicon.NODE_X_LOCATION, outNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION) +(int)xmod  ,false));
					LinkNodeProps.get(existingView).add(new DelayedVizProp(linkerNode, BasicVisualLexicon.NODE_Y_LOCATION, outNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION) +(int)ymod  ,false));
					i++;
				}


			}
		}		
		for(CyNetworkView view : LinkNodeProps.keySet())
		{
			//create New Linker Nodes.
			//LinkNodeProps.put(subSystem, new Vector<DelayedVizProp>());
			//createLinkerNodes(originalnetwork, subSystemNetworks.get(subSystem), subSystemViews.get(subSystem),
			//		ColName, subSystemOutGoingEdges.get(subSystem));
			
			DelayedVizProp.applyAll(view, LinkNodeProps.get(view));											
			vmm.setVisualStyle(vmm.getVisualStyle(currentview), currentview);
			eventHelper.flushPayloadEvents();
			view.updateView();
			if(!networkViewManager.getNetworkViewSet().contains(view))
			{
				networkViewManager.addNetworkView(view);
				vmm.setVisualStyle(vmm.getVisualStyle(currentview), view);
				vmm.getVisualStyle(currentview).apply(view);				
				if(params.layoutAlgorithm != null)
				{
					Object context = params.layoutAlgorithm.createLayoutContext();
					insertTasksAfterCurrentTask(params.layoutAlgorithm.createTaskIterator(view, context, new HashSet<View<CyNode>>(), null));
				}
				view.fitContent();
				view.updateView();
			}			
		}		
		
	}
	
	
	/**
	 * Get the information for all potential nodes that can link to other subnetworks. 
	 * @param subSysNodes Nodes in the current subsystem  
	 * @param subSysEdges Edges in the current subsystem (these cannot function as links)
	 * @param parent the parent {@link CyNetwork}
	 * @param newNetwork The network to obtain the Nodes from.
	 * @param ColName The name of the column for which subnetworks are created
	 * @param NetworkID The ID of the current network (a potential option from the Column.
	 * @return a Set of LinkInfos for nodes that could potentially links to other networks.
	 */
	private Set<LinkInfo> getLinkingInfo(Set<CyNode> subSysNodes, Set<CyEdge> subSysEdges, CyNetwork parent,
			CyNetwork newNetwork, String ColName, String NetworkID)
	{
		HashSet<LinkInfo> outgoingEdges = new HashSet<LinkInfo>();		
		CyTable nodeTab = parent.getDefaultNodeTable();
		//nvs.getExistingNetworks(parent, ColName);
		for(CyNode node : subSysNodes)
		{
			if(params.ignoredNodes.contains(node) | params.noBranchNodes.contains(node))
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
					List<CyEdge> current_edges = parent.getAdjacentEdgeList(node, CyEdge.Type.ANY);
					for(CyEdge edge : current_edges)
					{
						//if its not in the subSystem Edges
						if(!subSysEdges.contains(edge))
						{
							CyRow OppositeSite = edge.getSource().getSUID() == node.getSUID() ? nodeTab.getRow(edge.getTarget().getSUID()) : nodeTab.getRow(edge.getSource().getSUID());
							String direction = edge.getSource().getSUID() == node.getSUID() ? LinkInfo.OUTGOING : LinkInfo.INCOMING;
							boolean reversible = !edge.isDirected();
							boolean isReaction =  OppositeSite.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION);
							//if the target node is not labeled as interaction, this can not be a linker node. 
							//and not the default (i.e. there is a value)
							if(isReaction && (OppositeSite.getRaw(ColName) == null ||
									OppositeSite.getRaw(ColName) != parent.getDefaultNodeTable().getColumn(ColName).getDefaultValue()))
							{								
								//if this is an edge that is not IN this set, and it targets a node, that has a non empty SUBSYSTEM Column, than this is an outgoing edge!
								//System.out.println("In subsystem " + NetworkID + " We will try to generate links for node " + noderow.get(CyNetwork.NAME, String.class));
								outgoingEdges.add(new LinkInfo(node,direction,reversible));
									
							}
						}
					}
				}

			}
		}		

		return outgoingEdges;
	}
	/**
	 * Extend the current set of nodes by walking along the edges and checking to see whether there are additional nodes to use.
	 * @param subSysNodes The nodes in the current subsystem
	 * @param subSysEdges the Edges in the current subsystem
	 * @param net the parent network
	 * @param Subsystem the current subsystem
	 * @param ColName the subsystem column
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
			if(params.ignoredNodes.contains(node))
			{
				continue;
			}	
			CyRow noderow = nodeTab.getRow(node.getSUID());
			if((nodeTab.getColumn(IDAREProperties.IDARE_NODE_TYPE) != null) && IDAREProperties.NodeType.IDARE_GENE.equals(noderow.get(IDAREProperties.IDARE_NODE_TYPE,String.class)))
			{
				//if this is a gene, we do not extend it.
				continue;
			}
			//get all edges from the remaining supplied nodes.
			if(nodeTab.getColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE) != null )
			{
				
				
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
						}
						if((isSourceSpecies || isSourceReaction) && subSysNodes.contains(edge.getSource()))
						{
							edges.add(edge);
						}
						// if neither source nor target are reactions, we can add it as well.
						if (!isSourceReaction && !isTargetReaction && !isSourceSpecies && !isTargetSpecies)
						{
							edges.add(edge);
						}							

					}

				}
				else if(isReaction)					
				{	//this is a Reaction (and part of the network) Thus we can savely add all adjacent nodes.  							
					edges.addAll(net.getAdjacentEdgeList(node, CyEdge.Type.ANY));
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
			if(params.ignoredNodes.contains(sourcenode) | params.ignoredNodes.contains(targetnode))
			{
				continue;
			}
			if(!keepoldLinks  && (nodeTab.getRow(sourcenode.getSUID()).get(IDAREProperties.IDARE_NODE_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_LINK) ||
					nodeTab.getRow(targetnode.getSUID()).get(IDAREProperties.IDARE_NODE_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_LINK)))
			{
				continue;
			}				
			//otherwise, this is either an edge to a non reaction node or it is an edge to a reaction node within the subsystem.
			//so we can simply add it.
			PrintFDebugger.Debugging(this, "Extending " + Subsystem + " to Nodes" + edge.getSource() + " and " + edge.getTarget());
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
	 * @param ColName - The Column Name in which the Subsystems are stored
	 * @param LinkingNodes - Info about all potentially linking nodes from this network
	 */
	@SuppressWarnings("unused")
	private void createLinkerNodes(CyNetwork originalnetwork, CyNetwork newnetwork, CyNetworkView newNetworkView, String ColName, 
			Set<LinkInfo> LinkingNodes)
			{		
		HashMap<CyNetwork, CyNetworkView> existingNetworks = nvs.getExistingNetworksForColumn(ColName);
		Collection<CyNetwork> OtherNetworks = new HashSet<CyNetwork>();
		OtherNetworks.addAll(existingNetworks.keySet());
		OtherNetworks.remove(newnetwork);
		//First create a map which assigns all external nodes connected to an internal node		
		for(LinkInfo linkNode : LinkingNodes)
		{								
			CyNode orignode = originalnetwork.getNode(linkNode.NodeSUID);
			CyNode sourcenode = newnetwork.getNode(linkNode.NodeSUID);
			//System.out.println("Trying to create a Link between" + sourceSubSysName + " and " + targetSubSysName);
			if(params.noBranchNodes.contains(orignode) || params.ignoredNodes.contains(orignode) || sourcenode == null)
			{
				continue;
			}
			for(CyNetwork targetnetwork: OtherNetworks)
			{
				PrintFDebugger.Debugging(this, "The target network is " + targetnetwork);				
				CyNode targetnode = targetnetwork.getNode(linkNode.NodeSUID); 
				if(targetnode != null)
				{					
					//if the link does exist, just skip it this happens if there are multiple linking reactions.
					if(createdNodeLinks.containsKey(newnetwork) && createdNodeLinks.get(newnetwork).containsKey(linkNode.NodeSUID) && createdNodeLinks.get(newnetwork).get(linkNode.NodeSUID).contains(targetnetwork))
					{						
						continue;
					}
					CyNode targetNetworkNode = targetnetwork.addNode();
					CyNode sourceNetworkNode = newnetwork.addNode();
					
					CyEdge targetNetworkEdge = null;
					CyEdge sourceNetworkEdge = null;
					if(linkNode.directed)
					{
						if(linkNode.direction == LinkInfo.INCOMING)
						{
							targetNetworkEdge = targetnetwork.addEdge(targetnode, targetNetworkNode, true);
							sourceNetworkEdge = newnetwork.addEdge(sourceNetworkNode,sourcenode, true);
						}
						else
						{
							targetNetworkEdge = targetnetwork.addEdge(targetNetworkNode, targetnode,  true);
							sourceNetworkEdge = newnetwork.addEdge(sourcenode,sourceNetworkNode, true);
						}
					}
					else
					{
						targetNetworkEdge = targetnetwork.addEdge(targetnode, targetNetworkNode,  false);
						sourceNetworkEdge = newnetwork.addEdge(sourcenode, sourceNetworkNode, false);					
					}
					
					//setup the createdNodeLinks part
					if(!createdNodeLinks.containsKey(targetnetwork))
					{
						createdNodeLinks.put(targetnetwork, new HashMap<Long, Collection<CyNetwork>>());						
					}
					if(!createdNodeLinks.containsKey(newnetwork))
					{
						createdNodeLinks.put(newnetwork, new HashMap<Long, Collection<CyNetwork>>());						
					}
					if(!createdNodeLinks.get(targetnetwork).containsKey(linkNode.NodeSUID))
					{
						createdNodeLinks.get(targetnetwork).put(linkNode.NodeSUID, new Vector<CyNetwork>());						
					}
					if(!createdNodeLinks.get(newnetwork).containsKey(linkNode.NodeSUID))
					{
						createdNodeLinks.get(newnetwork).put(linkNode.NodeSUID, new Vector<CyNetwork>());						
					}
					createdNodeLinks.get(newnetwork).get(linkNode.NodeSUID).add(targetnetwork);
					createdNodeLinks.get(targetnetwork).get(linkNode.NodeSUID).add(newnetwork);
					//set the Name of the new node to the SUBSYSTEM - > which is the subsystem represented by the "outer nodes SUBSYSTEM column"  
					newnetwork.getRow(sourceNetworkNode).set(CyNetwork.NAME, nvs.getSubNetworkName(targetnetwork));
					targetnetwork.getRow(targetNetworkNode).set(CyNetwork.NAME, nvs.getSubNetworkName(newnetwork));
					
					//Give the linkers an IDAREID
					newnetwork.getRow(sourceNetworkNode).set(IDAREProperties.IDARE_NODE_UID, ism.getNextNodeID());
					targetnetwork.getRow(targetNetworkNode).set(IDAREProperties.IDARE_NODE_UID, ism.getNextNodeID());
					
					//and also set the IDARE_NODE_NAME accordingly. 
					newnetwork.getRow(sourceNetworkNode).set(IDAREProperties.IDARE_NODE_NAME, nvs.getSubNetworkName(targetnetwork));
					targetnetwork.getRow(targetNetworkNode).set(IDAREProperties.IDARE_NODE_NAME, nvs.getSubNetworkName(newnetwork));
					//The New nodes IDARENodeType is Link 
					newnetwork.getRow(sourceNetworkNode).set(IDAREProperties.IDARE_NODE_TYPE, IDAREProperties.NodeType.IDARE_LINK);
					targetnetwork.getRow(targetNetworkNode).set(IDAREProperties.IDARE_NODE_TYPE, IDAREProperties.NodeType.IDARE_LINK);

					//Here we set the Metabolite Node that is linking out as target. (Since it is the same node and present in multiple subnetworks.)
					//Thus we need to refer to this node.
					newnetwork.getRow(sourceNetworkNode).set(IDAREProperties.LINK_TARGET, originalnetwork.getRow(orignode).get(IDAREProperties.IDARE_NODE_UID, Long.class));
					targetnetwork.getRow(targetNetworkNode).set(IDAREProperties.LINK_TARGET, originalnetwork.getRow(orignode).get(IDAREProperties.IDARE_NODE_UID, Long.class));
					
					//set the target Subsystem to the Subsystems SUID
					newnetwork.getRow(sourceNetworkNode).set(IDAREProperties.LINK_TARGET_SUBSYSTEM, targetnetwork.getRow(targetnetwork).get(IDAREProperties.IDARE_NETWORK_ID,Long.class));
					targetnetwork.getRow(targetNetworkNode).set(IDAREProperties.LINK_TARGET_SUBSYSTEM, newnetwork.getRow(newnetwork).get(IDAREProperties.IDARE_NETWORK_ID,Long.class));
						
					
					//get the view of the other subsystem
					CyNetworkView targetSubSystemView = existingNetworks.get(targetnetwork);
					CyNetworkView sourceSubSystemView = existingNetworks.get(newnetwork);									
					
					if(sourceSubSystemView == null)
					{					
						nvs.addNetworkLink(targetNetworkNode,  targetnetwork, newnetwork, sourcenode );
					}
					else
					{
						nvs.addLink(targetNetworkNode, targetnetwork, sourceSubSystemView, sourceSubSystemView.getNodeView(sourcenode));
					}
					if(targetSubSystemView == null)
					{					
						nvs.addNetworkLink(sourceNetworkNode, newnetwork, targetnetwork, targetnode);
					}
					else
					{
						nvs.addLink(sourceNetworkNode, newnetwork, targetSubSystemView, targetSubSystemView.getNodeView(targetnode));
					}
					if(targetSubSystemView != null)
					{
						if(!LinkNodeProps.containsKey(targetSubSystemView))
						{
							LinkNodeProps.put(targetSubSystemView, new Vector<DelayedVizProp>());
						}
						if(!existingNodeLinks.containsKey(targetSubSystemView))
						{
							existingNodeLinks.put(targetSubSystemView, new HashMap<CyNode, Collection<CyNode>>());
						}
						if(!existingNodeLinks.get(targetSubSystemView).containsKey(targetnode))
						{
							existingNodeLinks.get(targetSubSystemView).put(targetnode, new Vector<CyNode>());
						}
						existingNodeLinks.get(targetSubSystemView).get(targetnode).add(targetNetworkNode);
						LinkNodeProps.get(targetSubSystemView).add(new DelayedVizProp(targetNetworkNode, BasicVisualLexicon.NODE_WIDTH,
							nvs.getSubNetworkName(targetnetwork).toString().length() * 6., true));
						
						LinkNodeProps.get(targetSubSystemView).add(new DelayedVizProp(targetNetworkNode, BasicVisualLexicon.NODE_HEIGHT, 20., true));
					}
					if(sourceSubSystemView != null)
					{
											
						if(!LinkNodeProps.containsKey(sourceSubSystemView))
						{
							LinkNodeProps.put(sourceSubSystemView, new Vector<DelayedVizProp>());
						}
						
						if(!LinkNodeProps.containsKey(sourceSubSystemView))
						{
							LinkNodeProps.put(sourceSubSystemView, new Vector<DelayedVizProp>());
						}
						if(!existingNodeLinks.containsKey(sourceSubSystemView))
						{
							existingNodeLinks.put(sourceSubSystemView, new HashMap<CyNode, Collection<CyNode>>());
						}
						if(!existingNodeLinks.get(sourceSubSystemView).containsKey(sourcenode))
						{
							existingNodeLinks.get(sourceSubSystemView).put(sourcenode, new Vector<CyNode>());
						}
						existingNodeLinks.get(sourceSubSystemView).get(sourcenode).add(sourceNetworkNode);
								
						LinkNodeProps.get(sourceSubSystemView).add(new DelayedVizProp(sourceNetworkNode, BasicVisualLexicon.NODE_WIDTH,
							nvs.getSubNetworkName(newnetwork).toString().length() * 6., true));
						
						LinkNodeProps.get(sourceSubSystemView).add(new DelayedVizProp(sourceNetworkNode, BasicVisualLexicon.NODE_HEIGHT, 20., true));
					}
				}
			}
		}
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
	 * Helper class to Combine some information about the directionality of a Link
	 * @author Thomas Pfau
	 *
	 */
	private class LinkInfo
	{
		public final long NodeSUID;
		public String direction;
		public boolean directed;
		public static final String OUTGOING = "OUTGOING";
		public static final String INCOMING = "INCOMING";
		public LinkInfo(CyNode Link, String direction, boolean directed)
		{
			NodeSUID = Link.getSUID();
			this.direction = direction;
			this.directed = directed;
		}
	}

	@Override
	public void setUIHelper(TunableUIHelper arg0) {
		// TODO Auto-generated method stub
		
	}

}
