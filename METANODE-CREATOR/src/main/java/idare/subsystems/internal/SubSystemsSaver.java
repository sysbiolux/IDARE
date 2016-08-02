package idare.subsystems.internal;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.metanode.internal.Debug.PrintFDebugger;
import idare.metanode.internal.Utilities.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JOptionPane;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
/**
 * A SessionListener that restores the SubNetwork Interaction Links from a former Session. 
 * @author thomas
 *
 */
public class SubSystemsSaver implements SessionLoadedListener{
	
	private NetworkViewSwitcher nvs;	
	
	private	IDARESettingsManager IDAREIDMgr;
	
	
	public SubSystemsSaver(NetworkViewSwitcher nvs, IDARESettingsManager iDAREIDMgr) {
		super();
		this.nvs = nvs;
		IDAREIDMgr = iDAREIDMgr;
	}
	
	// restore the networkview links from the CyTables
	@Override
	public void handleEvent(SessionLoadedEvent e){
		
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
		boolean aborted = false;
		HashMap<Long,CyNode> IDAREToNode = new HashMap<Long, CyNode>();
		HashSet<NodeAndNetworkStruct> LinkerNodes = new HashSet<NodeAndNetworkStruct>();
		HashMap<String,CyNetwork> NetworkNameToNetwork = new HashMap<String, CyNetwork>();
		//first collect all nodes - this is to avoid having duplicate Nodes in different networks (Root vs SubNetwork) leading to ID errors.
		HashMap<Long,CyNode> ids = new HashMap<Long,CyNode>();
		for(CyNetwork network : IDARENetworks)
		{
			NetworkNameToNetwork.put(network.getRow(network).get(CyNetwork.NAME, String.class), network);
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
							//for each network we check for duplicate IDs. 
							//If there are duplicate IDs, we will reset the IDARESettings Manager and reset all ids.
							//Since a CyNode can be present in multiple networks, we have to check whether a given id 
							//always maps to the same node (as defined by the SUID).
							if(!ids.containsKey(id))
							{
								ids.put(id,node);								
							}
							else
							{
								if(!ids.get(id).getSUID().equals(node.getSUID()))
								{
									aborted = true;
									break;
								}
							}
							//This is a Node With an IDARE ID, so it could be linked to, lets store it.
							IDAREToNode.put(id, node);
							PrintFDebugger.Debugging(this, "Found a Node with an IDARE id of: " + id);
						}
						if(network.getRow(node).get(IDAREProperties.IDARE_NODE_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
						{
							//Now, if this is a linker, Lets save it with the network it is in.
							LinkerNodes.add(new NodeAndNetworkStruct(node, network));
							PrintFDebugger.Debugging(this, "Found a Linker Node with id: " + id);
						}
					}
					catch (IllegalArgumentException ille)
					{
						JOptionPane.showMessageDialog(null,"Duplicate IDs found in the networks... Something went wrong - resetting IDARE Rows");
						PrintFDebugger.Debugging(this, "Duplicate IDs found. Aborting network connections");
						IDAREToNode.clear();
						LinkerNodes.clear();
						aborted = true;
						break;
					}

				}
			}
			
			NodeList.addAll(network.getNodeList());
		}
		//now add all IDs

		if(aborted)
		{
			//reset the ID Manager.
			IDAREIDMgr.reset();			
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
					network.getRow(node).set(IDAREProperties.IDARE_NODE_UID, IDAREIDMgr.getNextID());
					}
				}
				NodeList.removeAll(deletedNodes);
			}
									
		}
		else
		{
			//the IDs were fine so far. Lets restore the links...
			//To do so, we need to look at all linker nodes.
			PrintFDebugger.Debugging(this, "Looping over linker nodes. There are a total of " + LinkerNodes.size() + " Links to create");
			
			for(NodeAndNetworkStruct node : LinkerNodes)
			{
				
				Long TargetID = node.network.getRow(node.node).get(IDAREProperties.LINK_TARGET, Long.class);			
				String TargetSubSystem = node.network.getRow(node.node).get(IDAREProperties.LINK_TARGET_SUBSYSTEM, String.class);				
				CyNode TargetNode = IDAREToNode.get(TargetID);
				PrintFDebugger.Debugging(this, "Trying to establish a link between node (SUID)" + node.node.getSUID() + " and node (IDAREID)" + TargetID + " in network " + TargetSubSystem);
				@SuppressWarnings("unused")
				CyNetworkView LinkView = null;
				CyNetworkView TargetView = null;
				for(CyNetworkView view : session.getNetworkViews())
				{
					try{
					if(view.getModel().getRow(view.getModel()).get(CyNetwork.NAME, String.class).equals(TargetSubSystem))
					{
						LinkView = view;
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
						PrintFDebugger.Debugging(this, "Caught an exception");
						ex.printStackTrace(System.out);
					}

				}
				//now we have both views, so we can set up the NetworkSwitcher.
				//if they are however null, we will add a silent network link since obviously the Network exists, but there is no view for it.
				if(TargetView != null && TargetView.getNodeView(TargetNode) != null)
				{
					PrintFDebugger.Debugging(this, "Creating an active link to node " + TargetID  + " in network " + TargetSubSystem + " from node " + node.network.getRow(node.node).get(IDAREProperties.IDARE_NODE_UID,Long.class)	);
					nvs.addLink(node.node, node.network, TargetView, TargetView.getNodeView(TargetNode));
				}
				else
				{
					//create a network link
					PrintFDebugger.Debugging(this, "Creating an inactive link to node " + TargetID  + " in network " + TargetSubSystem + " from node " + node.network.getRow(node.node).get(IDAREProperties.IDARE_NODE_UID,Long.class)	);
					nvs.addNetworkLink(node.node, node.network, 
							NetworkNameToNetwork.get(node.network.getRow(node.node).get(IDAREProperties.LINK_TARGET_SUBSYSTEM, String.class)) , TargetNode);
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
//		if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME) == null)
//		{
//			return false;			
//		}
		if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_TYPE) == null)
		{
			return false;			
		}
		if(NodeTable.getColumn(IDAREProperties.LINK_TARGET) == null)
		{
			return false;			
		}
		if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_UID) == null)
		{
			return false;			
		}
		if(NodeTable.getColumn(IDAREProperties.LINK_TARGET_SUBSYSTEM) == null)
		{
			return false;			
		}
//		if(network.getDefaultEdgeTable().getColumn(IDAREProperties.IDARE_EDGE_PROPERTY) == null)
//		{
//			return false;			
//		}
		return true;
	}

//	@Override
//	public void handleEvent(SessionAboutToBeSavedEvent e) {
//		// TODO Auto-generated method stub
//		String NetworkNames = "";
//		File f =  IOUtils.getTemporaryFile(IDAREProperties.SUBSYSTEMS_SAVE_FILE, "");
//		List<File> appfilelist = new LinkedList<File>();
//		appfilelist.add(f);
//		try{
//			BufferedWriter br = new BufferedWriter(new FileWriter(f));
//			for(CyNetwork network : nvs.getExistingNetworks().keySet())
//			{
//				br.write(network.getRow(network).get(CyNetwork.NAME, String.class) + "\n");
//			}
//			br.close();
//			e.addAppFiles(IDAREProperties.SUBSYSTEMS_SAVE_ID, appfilelist);
//		}
//		catch(Exception ex)
//		{
//			//"This Should not happen..."
//			PrintFDebugger.Debugging(this, "Exception during saving the IDARE Subsystem information");
//		}
//	}
//		
//	
}
