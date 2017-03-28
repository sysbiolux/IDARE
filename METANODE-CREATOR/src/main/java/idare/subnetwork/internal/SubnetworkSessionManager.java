package idare.subnetwork.internal;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.view.model.CyNetworkView;
/**
 * A SessionListener that restores the SubNetwork Interaction Links from a former Session. 
 * @author thomas
 *
 */
public class SubnetworkSessionManager{

	private NetworkViewSwitcher nvs;	

	private	IDARESettingsManager IDAREIDMgr;


	public SubnetworkSessionManager(NetworkViewSwitcher nvs, IDARESettingsManager iDAREIDMgr) {
		super();
		this.nvs = nvs;
		IDAREIDMgr = iDAREIDMgr;
	}

	// restore the networkview links from the CyTables
	public void handleSessionLoadedEvent(SessionLoadedEvent e){
		nvs.handleEvent(e);
		//First check whether this system is set up for IDARE use.				
		CySession session = e.getLoadedSession();

		Set<CyNetwork> networks = e.getLoadedSession().getNetworks();
		//IDARE Networs must have the appropriate Fields in their NODE Tables. so check which Networks are IDARENetworks
		Set<CyNetwork> IDARENetworks = new HashSet<CyNetwork>();
		for(CyNetwork network : networks)
		{
			if(isIDARENetwork(network))
			{
				IDARENetworks.add(network);
			}
		}
		//Lets assume, that we only link nodes within one network.
		//first get all node IDs and thereby initialize the IDAREIDManager.
		//since we will (hopefully) only have one session at each time this should protect us from applying the same IDs in two different sessions....
		//and well... if there is a Manual user error than we are "Screwed" anyways....
		//IDAREIDMgr.reset();
		nvs.reset();
		Set<CyNode> NodeList = new HashSet<CyNode>();
		HashMap<Long,Long> IDAREToNodeSUID = new HashMap<Long, Long>();
		HashSet<NodeAndNetworkStruct> LinkerNodes = new HashSet<NodeAndNetworkStruct>();
		HashMap<Long,CyNetwork> networkIDToNetwork = new HashMap<Long, CyNetwork>();
		//first collect all nodes - this is to avoid having duplicate Nodes in different networks (Root vs SubNetwork) leading to ID errors.
		for(CyNetwork network : IDARENetworks)
		{
			networkIDToNetwork.put(network.getRow(network).get(IDAREProperties.IDARE_NETWORK_ID, Long.class), network);
//			PrintFDebugger.Debugging(this, "Network " + network.getDefaultNetworkTable().getRow(network.getSUID()).get(CyNetwork.NAME, String.class) + " has SUID " + network.getRow(network).get(CyNetwork.SUID, Long.class));

			Collection<CyNode> nodeset = network.getNodeList();

			for(CyNode node: nodeset)
			{

				if(!NodeList.contains(node))
				{
					CyRow row = network.getRow(node);
					Long id = row.get(IDAREProperties.IDARE_NODE_UID, Long.class);					
					try
					{					
						if(id != null)
						{		
							//This is a Node With an IDARE ID, so it could be linked to, lets store it.
							IDAREToNodeSUID.put(id, node.getSUID());
						}
						if(network.getRow(node).get(IDAREProperties.IDARE_NODE_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
						{
							//Now, if this is a linker, Lets save it with the network it is in.
//							PrintFDebugger.Debugging(this, "Found a Linker Node: " + node );
							LinkerNodes.add(new NodeAndNetworkStruct(node, network));
						}
					}
					catch (IllegalArgumentException ille)
					{
//						PrintFDebugger.Debugging(this,"Duplicate IDs found in the networks... Something went wrong");						
						IDAREToNodeSUID.clear();
						LinkerNodes.clear();						
						abort(IDARENetworks);
						throw new RuntimeException("Duplicate IDs found in the networks... Something went wrong");						
					}

				}
			}

			NodeList.addAll(network.getNodeList());
		}
		//now add all IDs


		//the IDs were fine so far. Lets restore the links...
		//To do so, we need to look at all linker nodes.

		for(NodeAndNetworkStruct node : LinkerNodes)
		{
			Long TargetID = node.network.getRow(node.node).get(IDAREProperties.IDARE_LINK_TARGET, Long.class);			
			Long TargetSubSystem = node.network.getRow(node.node).get(IDAREProperties.IDARE_LINK_TARGET_SUBSYSTEM, Long.class);
			CyNode TargetNode = node.network.getNode(IDAREToNodeSUID.get(TargetID));
			if(TargetNode == null)
			{
				PrintFDebugger.Debugging(this, "For some reason, the target node of this Link to " + TargetSubSystem + " is null and had id " + TargetID + ". The SUID of the target node was " + IDAREToNodeSUID.get(TargetID) + " This should not happen");
				PrintFDebugger.Debugging(this, "The originating  network had ID " + node.network.getRow(node.network).get(IDAREProperties.IDARE_NETWORK_ID, Long.class));
				continue;
				
			}
			@SuppressWarnings("unused")
			CyNetworkView LinkView = null;
			CyNetworkView TargetView = null;
			for(CyNetworkView view : session.getNetworkViews())
			{
				try{
					if(view.getModel().getRow(view.getModel()).get(IDAREProperties.IDARE_NETWORK_ID, Long.class).equals(TargetSubSystem))
					{
						LinkView = view;
						nvs.addSubNetworkView(view.getModel(),view);
						//Lets assume, that we have the correct view for the target subsystem...
						//we will make sure, that this view has the respective node.
						if(view.getNodeView(TargetNode) != null)
						{
							TargetView = view;
						}
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace(System.out);
				}

			}
			//now we have both views, so we can set up the NetworkSwitcher.
			//if they are however null, we will add a silent network link since obviously the Network exists, but there is no view for it.			
			if(TargetView != null && TargetView.getNodeView(TargetNode) != null)
			{				
				nvs.addLink(node.node, node.network, TargetView, TargetView.getNodeView(TargetNode));
			}
			else
			{
				//create a network link
				nvs.addNetworkLink(node.node, node.network,	networkIDToNetwork.get(TargetSubSystem) , TargetNode);
			}
		}
	}

	/**
	 * Abort loading and reset all IDARENetworks removing the linkers and linker IDs from these networks.
	 * @param IDARENetworks
	 */
	private void abort(Set<CyNetwork> IDARENetworks)
	{
		//IDAREIDMgr.reset();			
		//
		for(CyNetwork network : IDARENetworks)
		{
			Collection<CyNode> nodeset = network.getNodeList();

			Set<CyNode> deletedNodes = new HashSet<CyNode>();
			for(CyNode node : nodeset)
			{
				//Delete all Linker Nodes (as they are pointless now....)
				if(network.getRow(node).get(IDAREProperties.IDARE_NODE_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
				{
					List<CyEdge> edges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);					
					network.removeEdges(edges);
					network.removeNodes(Collections.singletonList(node));
					deletedNodes.add(node);
				}
				//and reset the IDAREIds.
				else
				{
					network.getRow(node).set(IDAREProperties.IDARE_NODE_UID, IDAREIDMgr.getNextNodeID());
				}
			}			
		}
	}
	/**
	 * Check whether this network has all necessary Table Columns for a IDARE Network
	 * @param network - the network to check
	 * @return whether this network has all necessary table columns. 
	 */
	private boolean isIDARENetwork(CyNetwork network) {
		CyTable NodeTable = network.getDefaultNodeTable();

		if(NodeTable.getColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE) == null)
		{
			return false;			
		}
		if(NodeTable.getColumn(IDAREProperties.IDARE_LINK_TARGET) == null)
		{
			return false;			
		}
		if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_UID) == null)
		{
			return false;			
		}
		if(NodeTable.getColumn(IDAREProperties.IDARE_LINK_TARGET_SUBSYSTEM) == null)
		{
			return false;			
		}

		return true;
	}


}
