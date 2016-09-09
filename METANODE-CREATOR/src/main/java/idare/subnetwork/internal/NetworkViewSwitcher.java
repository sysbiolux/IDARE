package idare.subnetwork.internal;
import idare.Properties.IDAREProperties;
import idare.imagenode.Utilities.EOOMarker;
import idare.imagenode.Utilities.IOUtils;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.work.TaskIterator;

/**
 * A Class that acts as switcher between networks.
 * @author Thomas Pfau
 *
 */
public class NetworkViewSwitcher extends AbstractNodeViewTaskFactory implements NetworkViewAboutToBeDestroyedListener, NetworkViewAddedListener,
																				NetworkAboutToBeDestroyedListener, RowsSetListener, NetworkAddedListener, SessionAboutToBeSavedListener{
	//TODO: Think about whether to include the following listeners and how to implement them... AboutToRemoveNodeViewsListener, AboutToRemoveNodesListener
	private static String SaveFileName = "NETWORKHIERARCHY";
	private static String appName = "IDARE_NETWORKHIERARCHY";
	public static String PREFERRED_OPTION = "OPEN";
	public static String Title = "Switch to Network View";
	private HashMap<CyNetwork,HashMap<String,HashMap<String,NetworkNode>>> NetworkHierarchy = new HashMap<CyNetwork, HashMap<String,HashMap<String,NetworkNode>>>();
	private HashMap<CyNetwork,NetworkNode> NetworkNodes = new HashMap<CyNetwork, NetworkNode>();
	private HashMap<CyNode, NodeViewLink> targetViews;
	private HashMap<CyNode, NodeAndNetworkStruct> nodeNetworks;
	private CyServiceRegistrar registrar;
	
	//lists for each CyNetwork the nodes pointing to that CyNetworkView	
	private HashMap<CyNetworkView,List<CyNode>> listenedNetworks;
	//A Map of networks which have no view and the nodes in that network that are links
	private HashMap<CyNetwork,List<CyNode>> SilentNodes;
	private HashMap<CyNetwork,CyNetworkView> SubNetworks;
	private HashMap<CyNetwork,Set<NodeAndNetworkStruct>> nodesPointingToNetwork;
	
	
	/**
	 * Constructs a NetworkView Switcher for the current application. 
	 * @param reg - A CyServiceRegistrar to obtain relevant services 
	 */
	public NetworkViewSwitcher(CyServiceRegistrar reg)
	{
		registrar = reg;
		//this.applicationManager = applicationManager; 
		targetViews = new HashMap<CyNode, NodeViewLink>();
		nodeNetworks = new HashMap<CyNode, NodeAndNetworkStruct>();
		listenedNetworks = new HashMap<CyNetworkView, List<CyNode>>();
		SilentNodes = new HashMap<CyNetwork,List<CyNode>>();
		//NetworkNames = new HashMap<CyNetwork, String>();
		nodesPointingToNetwork = new HashMap<CyNetwork, Set<NodeAndNetworkStruct>>();
		//this.eventhelper = eventhelper;
		//networkManager= nm;
		SubNetworks = new HashMap<CyNetwork, CyNetworkView>();
	}
	@Override
	public synchronized TaskIterator createTaskIterator(View<CyNode> arg0, CyNetworkView arg1) {
		CyNode node = arg0.getModel();
		
		return new TaskIterator(new NetworkViewSwitchTask(registrar.getService(CyApplicationManager.class),targetViews.get(node)));
	}
	/**
	 * Reset the Switcher (e.g. upon loading a session).
	 */
	public synchronized void reset()
	{
		targetViews = new HashMap<CyNode, NodeViewLink>();
		nodeNetworks = new HashMap<CyNode, NodeAndNetworkStruct>();
		listenedNetworks = new HashMap<CyNetworkView, List<CyNode>>();
		SilentNodes = new HashMap<CyNetwork,List<CyNode>>();
		//NetworkNames = new HashMap<CyNetwork, String>();
		nodesPointingToNetwork = new HashMap<CyNetwork, Set<NodeAndNetworkStruct>>();
		SubNetworks = new HashMap<CyNetwork, CyNetworkView>();
	}
	/**
	 * Add a new Network to the list of existing networks and also add its view.
	 * @param network - The new {@link CyNetwork}
	 * @param view - A {@link CyNetworkView} for the provided {@link CyNetwork}
	 * 
	 */
	public synchronized void addSubNetworkView(CyNetwork network, CyNetworkView view)
	{
		SubNetworks.put(network, view);
	}
	

	@SuppressWarnings("unchecked")
	public synchronized HashMap<CyNetwork, CyNetworkView> getExistingNetworks()
	{		
		return (HashMap<CyNetwork, CyNetworkView>)SubNetworks.clone();
	}
	

	/**
	 * Get the subnetworks that currently exist for the provided column present in any parent network 
	 * @param selectedColumn The Column to obtain the Network-> View pairs for
	 * @return A {@link Map} of {@link CyNetwork} -> {@link CyNetworkView} pairs. The Views can be null, if there is no existing view for the given network. 
	 */
	public synchronized HashMap<CyNetwork, CyNetworkView> getExistingNetworksForColumn(String selectedColumn)
	{		
		HashMap<CyNetwork, CyNetworkView> existing = new HashMap<CyNetwork, CyNetworkView>();
		for(CyNetwork network : NetworkHierarchy.keySet())
		{
			if(NetworkHierarchy.get(network).containsKey(selectedColumn))
			{
				PrintFDebugger.Debugging(this, "Found a Network for Column " + selectedColumn + " with name " + network);
				for(NetworkNode node : NetworkHierarchy.get(network).get(selectedColumn).values())
				{
					existing.put(node.getNetwork(),SubNetworks.get(node.getNetwork()));
				}
			}
		}
		return existing;
	}
	/**
	 * Add a Link between the {@link CyNode} and the target network {@link CyNetworkView}.
	 * To keep track of these nodes and react properly to closing {@link CyNetworkView}s we need the original {@link CyNetwork} and the target NodeView
	 * @param node - The {@link CyNode} that will function as a link. Double clicking on a View of this node will switch the current NetworkView to the target  view (if available)
	 * @param origin - The {@link CyNetwork} of the {@link CyNode}. This is for managing this node depending on the presence of the target network.
	 * @param TargetNetworkView - The View this link is pointing to
	 * @param TargetNodeView - The Node this Link will focus on in the target network.
	 */
	public synchronized void addLink(CyNode node, CyNetwork origin, CyNetworkView TargetNetworkView, View<CyNode> TargetNodeView)
	{
		targetViews.put(node, new NodeViewLink(TargetNodeView, TargetNetworkView,TargetNetworkView.getModel(),TargetNodeView.getModel(),origin));
		PrintFDebugger.Debugging(this, "Adding node " + origin.getRow(node).get(IDAREProperties.IDARE_NODE_UID, Long.class) + " with Network " +  origin.getDefaultNetworkTable().getRow(origin.getSUID()).get(CyNetwork.NAME, String.class ) + " to nodeAndNetwork");
		nodeNetworks.put(node, new NodeAndNetworkStruct(node,origin));
		CyNetwork targetNetwork = TargetNetworkView.getModel();
		if(!nodesPointingToNetwork.containsKey(targetNetwork))
		{
			nodesPointingToNetwork.put(targetNetwork, new HashSet<NodeAndNetworkStruct>());
		}
		nodesPointingToNetwork.get(targetNetwork).add(new NodeAndNetworkStruct(node,origin));
		if(!listenedNetworks.containsKey(TargetNetworkView))
		{
			listenedNetworks.put(TargetNetworkView, new LinkedList<CyNode>());			
		}
		listenedNetworks.get(TargetNetworkView).add(node);		
	}
	
	/**
	 * Add a Link between the {@link CyNode} and the target network {@link CyNetwork}.
	 * This is for a situation were no view exists for the target of this node. If a view is created for the target network, this link will become active
	 * @param node - The CyNode that will function as a link. Double clicking on a View of this node will switch the current NetworkView to the target  view (if available)
	 * @param origin - The {@link CyNetwork} of the node. This is for managing this node depending on the presence of the target network.
	 * @param target - The target network.
	 * @param TargetNode - The target node in the target network, this link will focus on.
	 */
	public synchronized void addNetworkLink(CyNode node,CyNetwork origin, CyNetwork target, CyNode TargetNode)
	{
		targetViews.put(node, new NodeViewLink(null, null, target, TargetNode,origin));
		nodeNetworks.put(node, new NodeAndNetworkStruct(node,origin));
		if(!nodesPointingToNetwork.containsKey(target))
		{
			nodesPointingToNetwork.put(target, new HashSet<NodeAndNetworkStruct>());
		}
		nodesPointingToNetwork.get(target).add(new NodeAndNetworkStruct(node,origin));
		if(!SilentNodes.containsKey(target))
		{
			SilentNodes.put(target, new LinkedList<CyNode>());			
		}
		SilentNodes.get(target).add(node);			
	
	}
	
	/**
	 * Gives a copy of the TargetView Map. ITs only a copy to keep this class consistent internally
	 * @return A Copy of the TargetViews {@link Map} of this network Switcher 
	 */
	@SuppressWarnings("unchecked")
	public synchronized Map<CyNode,NodeViewLink> getTargetViewCopy()
	{
		// we dont want to give out the actual TargetViews map but only a copy structure. 
		return (Map<CyNode,NodeViewLink>)targetViews.clone();
	}	
	
	/**
	 * Get the Names of all subnetworks for a specific parent network and a specific column.
	 * @param parentnetwork the parent network to retrieve subnetwork names for
	 * @param ColName the currently selected Column (to only return names specific for this network)
	 * @return A {@link Set} of String with al the subnetwork Names for the provided parent network created using the specified column. 
	 */
	public synchronized Set<String> getSubNetworkWorksForNetwork(CyNetwork parentnetwork, String ColName)
	{
		HashSet<String> subnetworkNames = new HashSet<String>();
		if(NetworkHierarchy.containsKey(parentnetwork) && NetworkHierarchy.get(parentnetwork).containsKey(ColName)){
			for(NetworkNode node : NetworkHierarchy.get(parentnetwork).get(ColName).values())
			{
				//if the current network exists, then we return it.
				if(node.getNetwork() != null)
				{
					subnetworkNames.add(node.networkID);
				}
			}
		}
		return subnetworkNames;
	}
	
	@Override
	public synchronized void handleEvent(NetworkViewAddedEvent e) {
		//here we have to check whether this fits...
		CyNetworkView view = e.getNetworkView();
		CyNetwork network = view.getModel();
		if(SilentNodes.containsKey(network))
		{
			//correct the SubNetwors field
			SubNetworks.put(network, view);
			//there were nodes pointing to this View so we have to deactivate them.
			// we also have to remove this View from the ListenedNetworks
			for(CyNode node : SilentNodes.get(network))
			{
				PrintFDebugger.Debugging(this, "Restoring visual link between " + network + " and " + targetViews.get(node).getTargetNetwork() + " using node " + node);
				//reset the TargetNodeView -> we have to retrieve the target node information from the TargetViews Structure.
				targetViews.get(node).setTargetNodeView(view.getNodeView(targetViews.get(node).getTargetNode()));
				//Set the Target view
				targetViews.get(node).setTargetNetworkView(view);
				// and now we have to add the links to the silent Links
				if(!listenedNetworks.containsKey(view))
				{					
					listenedNetworks.put(view,new LinkedList<CyNode>());
				}
				//and add the View to the Known views. and the node to the nodes pointing to this view.
				listenedNetworks.get(network).add(node);
			}			
			SilentNodes.remove(network);			
		}		
	}
	
	
	@Override
	public synchronized void handleEvent(NetworkAboutToBeDestroyedEvent e) {
//		System.out.println("Calling NetworkAboutToBeDestroyed");		
		CyNetwork tobeDestroyed = e.getNetwork();
		PrintFDebugger.Debugging(this, "The Network to be destroyed is " + tobeDestroyed);
		NetworkNode netNode = NetworkNodes.get(tobeDestroyed);
		if(netNode != null)
		{
			netNode.setNetwork(null);
		}
		//now we need to get all linkers to this network and remove them.
		try
		{
		Set<NodeAndNetworkStruct> LinksToRemove = new HashSet<NodeAndNetworkStruct>();
		Set<CyNode> LinkNodes = new HashSet<CyNode>();
		LinkNodes.addAll(targetViews.keySet());
		for(CyNode node : LinkNodes)
		{
			NodeViewLink Link = targetViews.get(node);
			if(Link.getTargetNetwork().equals(tobeDestroyed)){
				LinksToRemove.add(nodeNetworks.get(node));
			}
			if(Link.getSourceNetwork().equals(tobeDestroyed))
			{
				targetViews.remove(node);
				nodeNetworks.remove(node);
			}
		}
		for(NodeAndNetworkStruct nodeAndNet : LinksToRemove)
		{						
			CyNetwork orignetwork = nodeAndNet.network;
			Collection<CyEdge> edges = orignetwork.getAdjacentEdgeList(nodeAndNet.node, CyEdge.Type.ANY);
			orignetwork.removeEdges(edges);
			PrintFDebugger.Debugging(this, "The corresponding network is " + orignetwork);
			PrintFDebugger.Debugging(this, "Trying to remove node " + nodeAndNet.node );
			PrintFDebugger.Debugging(this," from Network " + nodeAndNet.network.getDefaultNetworkTable().getRow(nodeAndNet.network.getSUID()).get(CyNetwork.NAME, String.class));
			orignetwork.removeNodes(Collections.singletonList(nodeAndNet.node));
			//check whether the other view still exists and if, update it. Otherwise 
			if(targetViews.get(nodeAndNet.node).getTargetNetworkView() != null)
			{ 
				targetViews.get(nodeAndNet.node).getTargetNetworkView().updateView();
			}
			else
			{
				System.out.println(nodeAndNet.node);
			}
			targetViews.remove(nodeAndNet.node);			
			nodeNetworks.remove(nodeAndNet.node);
		}
		}
		catch(Exception ex)
		{
			ex.printStackTrace(System.out);
		}
		//remove the network from the local fields
//		System.out.println("Removing Network for: "  + tobeDestroyed.getRow(tobeDestroyed).get(CyNetwork.NAME, String.class));
		NetworkNode node = NetworkNodes.get(tobeDestroyed);
		//remove the network reference, but keep the NetworkNode. This node could be repopulated later on.
		if(node != null)
		{
			node.setNetwork(null);
		}
		//NetworkNames.remove(tobeDestroyed);
		nodesPointingToNetwork.remove(tobeDestroyed);
		SilentNodes.remove(tobeDestroyed);
		SubNetworks.remove(tobeDestroyed);
	}
	
	@Override
	public synchronized void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		CyNetworkView view = e.getNetworkView();
		CyNetwork network = view.getModel();
//		System.out.println("Removing view for: "  + network.getRow(network).get(CyNetwork.NAME, String.class));
		if(listenedNetworks.containsKey(view))
		{
			//there were nodes pointing to this View so we have to deactivate them.
			// we also have to remove this View from the ListenedNetworks
			for(CyNode node : listenedNetworks.get(view))
			{
				targetViews.get(node).setTargetNodeView(null);
				targetViews.get(node).setTargetNetworkView(null);
				// and now we have to add the links to the silent Links
				if(!SilentNodes.containsKey(network))
				{
					SilentNodes.put(network,new LinkedList<CyNode>());
				}
				SilentNodes.get(network).add(node);
			}			
			listenedNetworks.remove(view);	
			if(SubNetworks.containsKey(network))
			{
//				System.out.println("Setting NetworkView for: "  + network.getRow(network).get(CyNetwork.NAME, String.class) + " to null");
				SubNetworks.put(network, null);
			}
			else
			{
				System.out.println("Network: "  + network.getRow(network).get(CyNetwork.NAME, String.class) + " was already removed");
			}
		}		
	}
	/**
	 * Check whether the set row is a network row and if, check whether it refers to a network, 
	 * we are listening to and update all links accordingly 
	 */
	@Override
	public synchronized void handleEvent(RowsSetEvent e) {

		CyTable tab = e.getSource();
		CyNetworkTableManager netTblMgr = registrar.getService(CyNetworkTableManager.class);
		CyNetwork network = netTblMgr.getNetworkForTable(tab);
		if(network == null)
		{
			//skip if the original item is not a network.
			return;					
		}

		
		if(e.containsColumn(CyNetwork.NAME))
		{		
			Collection<RowSetRecord> temp = e.getColumnRecords(CyNetwork.NAME);
			
			boolean changed = false; 
			
			for(RowSetRecord rec : temp)
			{				
				Long NetworkID = rec.getRow().get(CyNetwork.SUID, Long.class);
				CyNetworkManager netmgr = registrar.getService(CyNetworkManager.class);
				network = netmgr.getNetwork(NetworkID);
				if(network == null)
				{
					//skip if the changed item is not a network
					continue;					
				}		

				String newName = rec.getValue().toString(); 
				
				for(NodeAndNetworkStruct nodeAndNetwork : nodesPointingToNetwork.get(network))
				{
					changed = true;																	
					nodeAndNetwork.network.getRow(nodeAndNetwork.node).set(IDAREProperties.IDARE_NODE_NAME, newName);
					nodeAndNetwork.network.getRow(nodeAndNetwork.node).set(CyNetwork.NAME, newName);
//					NetworkNames.put(network, network.getRow(network).get(CyNetwork.NAME, String.class));
				}

			}
			
			if(changed)
			{	//update the views only if there actually have been any changes.
				CyEventHelper eventhelper = registrar.getService(CyEventHelper.class);
				eventhelper.flushPayloadEvents();
				for(CyNetworkView view : this.listenedNetworks.keySet())
				{
					view.updateView();
				}
			}
		}

	}
	
	@Override
	public synchronized void handleEvent(NetworkAddedEvent e) {
	}
	
	public void addNetworkToTree(CyNetwork parent, CyNetwork child, String ColumnID, String NetworkID)
	{
		if(!NetworkNodes.containsKey(parent))
		{
			NetworkNodes.put(parent,new NetworkNode(parent));
		}
		if(!NetworkHierarchy.containsKey(parent))
		{
			NetworkHierarchy.put(parent, new HashMap<String, HashMap<String,NetworkNode>>());			
		}
		if(!NetworkHierarchy.get(parent).containsKey(ColumnID))
		{
			NetworkHierarchy.get(parent).put(ColumnID, new HashMap<String,NetworkNode>());
		}
		if(NetworkHierarchy.get(parent).get(ColumnID).get(NetworkID) != null)
		{
			//we restore an old item!
			NetworkHierarchy.get(parent).get(ColumnID).get(NetworkID).setNetwork(child);
			NetworkNodes.put(child,NetworkHierarchy.get(parent).get(ColumnID).get(NetworkID));
		}
		else
		{
			NetworkNode childnode = new NetworkNode(NetworkNodes.get(parent),child,ColumnID,NetworkID);
			NetworkNodes.get(parent).addChild(childnode);
			NetworkNodes.put(child, childnode);
			NetworkHierarchy.get(parent).get(ColumnID).put(NetworkID,childnode);
		}
	}
	
	public String getSubNetworkName(CyNetwork network)
	{
		NetworkNode currentNode = NetworkNodes.get(network);
		if(currentNode == null)
		{
			throw new RuntimeException("No Node exists for the Network " +network.getRow(network).get(CyNetwork.NAME, String.class));
		}
		else
		{
			return currentNode.getNetworkID();
		}
	}
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent arg0) {		
		File NetworkHierarchyFile = IOUtils.getTemporaryFile(SaveFileName, "");
		writeHierachyFile(NetworkHierarchyFile);
		List<File> filelist = new LinkedList<File>();
		filelist.add(NetworkHierarchyFile);
		try{
			arg0.addAppFiles(appName, filelist);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		
	}
	
	private void writeHierachyFile(File outputfile)
	{
		try{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputfile));			
			Set<NetworkNode> topNodes = new HashSet<NetworkNode>();
			for(NetworkNode node : NetworkNodes.values())
			{
				if(node.parent == null)
				{
					topNodes.add(node);
				}
			}
			for(NetworkNode root : topNodes)
			{
				oos.writeObject(root);
			}
			oos.writeObject(new EOOMarker());			
		}
		catch(IOException e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	
	/**
	 * This is to restore the Network Hiearchy and is called from the SubSystemSaver class.
	 * @param arg0
	 */
	void handleEvent(SessionLoadedEvent arg0) {
		// TODO Auto-generated method stub
		try
		{
			List<File> appfiles = arg0.getLoadedSession().getAppFileListMap().get(appName);
			File appFile = null;
			if(appfiles != null && appfiles.size() > 0)
			{
				appFile = appfiles.get(0);
			}
			if(appFile == null)
			{
				return;
			}
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(appFile));			
			Object current = ois.readObject();
			while(!(current instanceof EOOMarker) && current != null)
			{
				NetworkNode root = (NetworkNode) current;
				handleNetworkNode(root);
				current = ois.readObject();
				
			}
			
			System.out.println("Network Hierarchy Restored");
		}		
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	private void handleNetworkNode(NetworkNode node)
	{
		CyNetworkManager mgr = registrar.getService(CyNetworkManager.class);
		PrintFDebugger.Debugging(this, "The manager is: " + mgr);
		node.setupNetworkReferences(mgr);
		if(node.getNetwork() != null)
		{
			System.out.println("Adding associated network node for network " + node.networkID);
			NetworkNodes.put(node.getNetwork(), node);		
		}
		
		if(node.parent != null)
		{			
			
			if(node.parent.getNetwork() != null)
			{
				if(!NetworkHierarchy.containsKey(node.parent.getNetwork()))
				{
					NetworkHierarchy.put(node.parent.getNetwork(), new HashMap<String, HashMap<String,NetworkNode>>());
				}
				if(!NetworkHierarchy.get(node.parent.getNetwork()).containsKey(node.colName))
				{
					NetworkHierarchy.get(node.parent.getNetwork()).put(node.colName,new HashMap<String, NetworkNode>());
				}
				System.out.println("Adding SubNetwork " + node.networkID + " for column " + node.colName + " to network " + node.parent.networkID);
				NetworkHierarchy.get(node.parent.getNetwork()).get(node.colName).put(node.networkID, node);
			}
		}
		for(NetworkNode child : node.getChildren())
		{
			handleNetworkNode(child);
		}								
	}
}
