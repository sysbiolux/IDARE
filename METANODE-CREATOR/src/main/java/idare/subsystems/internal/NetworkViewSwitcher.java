package idare.subsystems.internal;
import idare.Properties.IDAREProperties;
import idare.metanode.internal.Debug.PrintFDebugger;

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
																				NetworkAboutToBeDestroyedListener, RowsSetListener, NetworkAddedListener{
	//TODO: Think about whether to include the following listeners and how to implement them... AboutToRemoveNodeViewsListener, AboutToRemoveNodesListener

	public static String PREFERRED_OPTION = "OPEN";
	public static String Title = "Switch to Network View";
	
	private HashMap<CyNode, NodeViewLink> targetViews;
	private HashMap<CyNode, NodeAndNetworkStruct> nodeNetworks;
	//private CyApplicationManager applicationManager;
	//private CyNetworkManager networkManager;
	//private CyEventHelper eventhelper;
	private CyServiceRegistrar registrar;
	
	//lists for each CyNetwork the nodes pointing to that CyNetworkView	
	private HashMap<CyNetworkView,List<CyNode>> listenedNetworks;
	private HashMap<CyNetwork,List<CyNode>> SilentNodes;
	private HashMap<CyNetwork,String> NetworkNames;
	private HashMap<CyNetwork,CyNetworkView> SubNetworks;
	private HashMap<CyNetwork,Set<NodeAndNetworkStruct>> nodesPointingToNetwork;
	/**
	 * Constructs a NetworkView Switcher for the current application. 
	 * @param applicationManager - The current Cytoscape {@link CyApplicationManager} 
	 */
	public NetworkViewSwitcher(CyServiceRegistrar reg)
	{
		registrar = reg;
		//this.applicationManager = applicationManager; 
		targetViews = new HashMap<CyNode, NodeViewLink>();
		nodeNetworks = new HashMap<CyNode, NodeAndNetworkStruct>();
		listenedNetworks = new HashMap<CyNetworkView, List<CyNode>>();
		SilentNodes = new HashMap<CyNetwork,List<CyNode>>();
		NetworkNames = new HashMap<CyNetwork, String>();
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
		NetworkNames = new HashMap<CyNetwork, String>();
		nodesPointingToNetwork = new HashMap<CyNetwork, Set<NodeAndNetworkStruct>>();
		SubNetworks = new HashMap<CyNetwork, CyNetworkView>();
	}
	/**
	 * Add a new Network to the list of existing networks and also add its view.
	 * @param network - The new {@link CyNetwork}
	 * @param view - A {@link CyNetworkView} for the provided {@link CyNetwork}
	 */
	public synchronized void addSubNetwork(CyNetwork network, CyNetworkView view)
	{
		SubNetworks.put(network, view);
	}
	

	@SuppressWarnings("unchecked")
	public synchronized HashMap<CyNetwork, CyNetworkView> getExistingNetworks()
	{		
		return (HashMap<CyNetwork, CyNetworkView>)SubNetworks.clone();
	}
	
	
	public synchronized HashMap<CyNetwork, CyNetworkView> getExistingNetworks(Collection<String> options)
	{		
		HashMap<CyNetwork, CyNetworkView> existing = new HashMap<CyNetwork, CyNetworkView>();
		for(CyNetwork network : SubNetworks.keySet())
		{
			if(options.contains(network.getRow(network).get(CyNetwork.NAME, String.class)))
			{
				existing.put(network, SubNetworks.get(network));
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
		targetViews.put(node, new NodeViewLink(TargetNodeView, TargetNetworkView,TargetNetworkView.getModel(),TargetNodeView.getModel()));
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
		targetViews.put(node, new NodeViewLink(null, null, target, TargetNode));
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
	 * Get the Names of all SubNetworks managed by this NetworkViewSwitcher
	 * @return A {@link Set} of String with al the subnetwork Names used 
	 */
	public synchronized Set<String> getSubNetworkNames(CyNetwork parentnetwork)
	{
		HashSet<String> subnetworkNames = new HashSet<String>();
		PrintFDebugger.Debugging(this,"There are " + SubNetworks.size() +  " Subnetworks");
		// Do a check whether 
		for(CyNetwork network : SubNetworks.keySet())
		{
		//TODO: Change to specific subnetworks	
			String parentName = parentnetwork.getRow(parentnetwork).get(CyNetwork.NAME,String.class);
			String SubNetworkName = network.getRow(network).get(CyNetwork.NAME,String.class);
			if(SubNetworkName.startsWith(parentName))
			{
				subnetworkNames.add(SubNetworkName.replace(parentName + SubNetworkCreationTask.subnetworkNameSeparator, ""));
			}
		}
		PrintFDebugger.Debugging(this,subnetworkNames.size() +  " of those belong to the current network");
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
		System.out.println("Calling NetworkAboutToBeDestroyed");
		CyNetwork tobeDestroyed = e.getNetwork();
		//now we need to get all linkers to this network and remove them.
		try
		{
		Set<NodeAndNetworkStruct> LinksToRemove = new HashSet<NodeAndNetworkStruct>(); 
		for(CyNode node : targetViews.keySet())
		{
			NodeViewLink Link = targetViews.get(node);
			if(Link.getTargetNetwork().equals(tobeDestroyed)){
				LinksToRemove.add(nodeNetworks.get(node));
			}
		}
		for(NodeAndNetworkStruct node : LinksToRemove)
		{						
			CyNetwork orignetwork = node.network;
			Collection<CyEdge> edges = orignetwork.getAdjacentEdgeList(node.node, CyEdge.Type.ANY);
			orignetwork.removeEdges(edges);
			orignetwork.removeNodes(Collections.singletonList(node.node));
			//check whether the other view still exists and if, update it. Otherwise 
			if(targetViews.get(node.node).getTargetNetworkView() != null)
			{ 
				targetViews.get(node.node).getTargetNetworkView().updateView();
			}
			else
			{
				System.out.println(node.node);
			}
			targetViews.remove(node);			
			nodeNetworks.remove(node);
		}
		}
		catch(Exception ex)
		{
			ex.printStackTrace(System.out);
		}
		//remove the network from the local fields
		System.out.println("Removing Network for: "  + tobeDestroyed.getRow(tobeDestroyed).get(CyNetwork.NAME, String.class));
		NetworkNames.remove(tobeDestroyed);
		nodesPointingToNetwork.remove(tobeDestroyed);
		SilentNodes.remove(tobeDestroyed);
		SubNetworks.remove(tobeDestroyed);
	}
	
	@Override
	public synchronized void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		CyNetworkView view = e.getNetworkView();
		CyNetwork network = view.getModel();
		System.out.println("Removing view for: "  + network.getRow(network).get(CyNetwork.NAME, String.class));
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
				System.out.println("Setting NetworkView for: "  + network.getRow(network).get(CyNetwork.NAME, String.class) + " to null");
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
				PrintFDebugger.Debugging(this, "Updating Links to network: " + network.getRow(network).get(CyNetwork.NAME, String.class) );

				String newName = rec.getValue().toString(); 
				
				for(NodeAndNetworkStruct nodeAndNetwork : nodesPointingToNetwork.get(network))
				{
					changed = true;																	
					nodeAndNetwork.network.getRow(nodeAndNetwork.node).set(IDAREProperties.LINK_TARGET_SUBSYSTEM, newName);
					nodeAndNetwork.network.getRow(nodeAndNetwork.node).set(IDAREProperties.IDARE_NODE_NAME, newName);
					nodeAndNetwork.network.getRow(nodeAndNetwork.node).set(CyNetwork.NAME, newName);
					NetworkNames.put(network, network.getRow(network).get(CyNetwork.NAME, String.class));
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
		CyNetwork addedNetwork = e.getNetwork();
		NetworkNames.put(addedNetwork,addedNetwork.getRow(addedNetwork).get(CyNetwork.NAME, String.class));
	}

}
