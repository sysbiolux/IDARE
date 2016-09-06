package idare.imagenode.internal.DataManagement;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.EOOMarker;
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.internal.DataManagement.Events.DataSetChangeListener;
import idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.imagenode.internal.DataManagement.Events.NodeChangedListener;
import idare.imagenode.internal.DataManagement.Events.NodeUpdateEvent;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Layout.ColorMapDataSetBundle;
import idare.imagenode.internal.Layout.NodeLayout;
import idare.imagenode.internal.VisualStyle.IDAREVisualStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.prefs.NodeChangeListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/**
 * NodeManager manages the nodes used in the IDARE App. 
 * @author Thomas Pfau
 *
 */
public class NodeManager implements DataSetChangeListener{

	private HashMap<String,ImageNodeModel> Nodes = new HashMap<>();
	private HashMap<String,NodeLayout> activeLayouts = new HashMap<String, NodeLayout>();
	private Set<String> NetworkIDs = new HashSet<String>();
	private DataSetManager dsm;
	
	private Vector<NodeChangedListener> listeners = new Vector<NodeChangedListener>();
	private CyNetworkManager cyNetMgr;
	public NodeManager(CyNetworkManager cyNetMgr)
	{
		this.cyNetMgr = cyNetMgr;
	}

	/**
	 * Add a listener that listens to changes in nodes in this manager.
	 * @param listener the {@link NodeChangeListener} to add
	 */
	public void addNodeChangeListener(NodeChangedListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove a listener that had listened to changes in nodes in this manager.
	 * @param listener the {@link NodeChangeListener} to remove
	 */
	public void removeNodeChangeListener(NodeChangedListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Set the DatasetManager used by this {@link NodeManager}.
	 * This is mainly to allow nodelayouts to listen to changes in Datasets.
	 * @param dsm the {@link DataSetManager} to assign
	 */
	public void setDataSetManager(DataSetManager dsm)
	{
		this.dsm = dsm;
	}
	
	/**
	 * Reset this NodeManager clearing fields
	 */
	public void reset()
	{
		activeLayouts.clear();
		Nodes.clear();
		NetworkIDs.clear();
	}
	/**
	 * Update the set of NetworkNode IDs. This is to allow a minimal number of updates for graphics.
	 */
	public void updateNetworkNodes()
	{		
		Set<String> NodesToUpdate = new HashSet<String>();		
		Set<String> NewNodeIDs = new HashSet<String>();
		//new node ids contains all node ids from all networks
		for(CyNetwork network : cyNetMgr.getNetworkSet())
		{
			NewNodeIDs.addAll(IDARESettingsManager.getNetworkIDAREIDs(network, IDAREProperties.IDARE_NODE_NAME));
			NodesToUpdate.addAll(NewNodeIDs);
		}
		//Remove those which were present before from the nodes to update
		NodesToUpdate.removeAll(NetworkIDs);
		//Remove the new ids from the old ones.
		NetworkIDs.removeAll(NewNodeIDs);
		//and add the discrepancy to those to update (i.e. those should reease their associated images)
		NodesToUpdate.addAll(NetworkIDs);
		for(String s: NodesToUpdate)
		{
		}
		for(String s: NewNodeIDs)
		{
		}	
		//Fire update events for those nodes and reassign the current nodes.
		fireNetworkNodesChanged(NodesToUpdate);		
		NetworkIDs = NewNodeIDs;		
	}
	/**
	 * Update nodes to to a change in the network nodes.
	 * @param updatedNodeIDs The nodeIds that were Updated
	 */
	private void fireNetworkNodesChanged(Collection<String> updatedNodeIDs)
	{		
		//only update those were Layouts exist.
		updatedNodeIDs.retainAll(activeLayouts.keySet());
		//we only need to update things, that are actually in the networks.		
		NodeUpdateEvent e = new NodeUpdateEvent(updatedNodeIDs);
		//Create a copy in case some listener is unregistering itself, while updating.
		Vector<NodeChangedListener> clisteners = new Vector<NodeChangedListener>(); 
			clisteners.addAll(listeners);
		for(NodeChangedListener listener : clisteners)
		{
			listener.handleNodeUpdate(e);
		}
	}
	/**
	 * update nodes due to a change in the Nodes set.
	 * @param updatedNodeIDs The nodeIds that were Updated
	 */
	private void fireNodesChanged(Collection<String> updatedNodeIDs)
	{		
		//we only need to update things, that are actually in the networks.
		updatedNodeIDs.retainAll(NetworkIDs);
		NodeUpdateEvent e = new NodeUpdateEvent(updatedNodeIDs);		
		Vector<NodeChangedListener> clisteners = new Vector<NodeChangedListener>(); 
		clisteners.addAll(listeners);
		for(NodeChangedListener listener : clisteners)
		{
			PrintFDebugger.Debugging(listener, "Handling update Event from NodeManager (line from NodeManager)");
			listener.handleNodeUpdate(e);
			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.DataManagement.Events.DataSetChangeListener#datasetChanged(idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent)
	 */
	@Override
	public void datasetChanged(DataSetChangedEvent e)
	{
		if(e.wasAdded())
		{
			update(e.getSet(), true);
		}
		if(e.wasRemoved())
		{
			update(e.getSet(),false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.DataManagement.Events.DataSetChangeListener#datasetsChanged(idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent)
	 */
	@Override
	public void datasetsChanged(DataSetsChangedEvent e)
	{
		if(e.wasAdded())
		{
			//This is the same as a simple update for each Dataset
			for(DataSet ds : e.getSet())
				update(ds,true);
		}
		else
		{
			//Now, we can avoid multiple 
		}
	}


	/**
	 * Update the Manager/Nodes with the new DataSet, either adding it or removing it form the nodes.
	 * @param ds
	 * @param added
	 */
	public void update(DataSet ds, boolean added)
	{
		if(added)
		{
			for(String currentID : ds.getNodeIDs())
			{
				if(!Nodes.containsKey(currentID))
				{
					Nodes.put(currentID, new ImageNodeModel(currentID));
				}
				Nodes.get(currentID).addData(ds);
			}
		}
		else
		{
			for(String currentID : ds.getNodeIDs())
			{
				if(activeLayouts.containsKey(currentID))
				{
					Nodes.get(currentID).removeData(ds);					
					if(!activeLayouts.get(currentID).isValid())
					{
						//Nodes.remove(currentID);
						activeLayouts.remove(currentID);
					}
					//if the last dataset pointing to this node was removed also remove the node.
					if(!Nodes.get(currentID).isValid())
					{
						Nodes.remove(currentID);
					}

				}
			}
			Vector<String> NodesToUpdate = new Vector<String>();
			NodesToUpdate.addAll(ds.getNodeIDs());
			fireNodesChanged(NodesToUpdate);
		}
	}

	/**
	 * Get the Node corresponding to the given ID
	 * @param ID The ID of the node
	 * @return a node, if it exists, or <code>null</code>, if the ID is not linked to a known node.
	 */
	public ImageNodeModel getNode(String ID)
	{
		return Nodes.get(ID);
	}

	
	/**
	 * Generate Layouts for All nodes in the {@link DataSet}s present in the {@link ColorMapDataSetBundle}s provided.
	 * This version is to be used if the created nodes are created by a {@link Task}.
	 * @param datasets The datasets to create nodes for 
	 * @param monitor The TaskMonitor that keeps track of progress.
	 * @throws TooManyItemsException
	 * @throws ContainerUnplaceableExcpetion
	 * @throws DimensionMismatchException
	 */
	public void generateLayoutsForNodes(Collection<ColorMapDataSetBundle> datasets,TaskMonitor monitor) throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException
	{
		//create a new layout
		NodeLayout newLayout = new NodeLayout();					
		//produce the Layout, this is where errors are likely to raise.
		newLayout.generateLayoutForDataSets(datasets);
		//get all Nodes, that need to be updated
		Set<String> NodeIDs = new HashSet<String>();
		for(ColorMapDataSetBundle set : datasets)
		{
			NodeIDs.addAll(set.dataset.getNodeIDs());	
		}
		//add the new layout to all those nodes.
		for(String id : NodeIDs)
		{
			activeLayouts.put(id,newLayout);
		}
		monitor.setProgress(0.2);
		monitor.setStatusMessage("Updating Image Nodes");
		// if everything went fine, register the layout.
		dsm.addDataSetAboutToBeChangedListener(newLayout);
		fireNodesChanged(NodeIDs);		
	}
	
	/**
	 * Generate Layouts for All nodes in the {@link DataSet}s present in the {@link ColorMapDataSetBundle}s provided.
	 * @param datasets
	 * @throws TooManyItemsException
	 * @throws ContainerUnplaceableExcpetion
	 * @throws DimensionMismatchException
	 */
	public void generateLayoutsForNodes(Collection<ColorMapDataSetBundle> datasets) throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException
	{
		//create a new layout
		NodeLayout newLayout = new NodeLayout();					
		//produce the Layout, this is where errors are likely to raise.
		newLayout.generateLayoutForDataSets(datasets);
		//get all Nodes, that need to be updated
		Set<String> NodeIDs = new HashSet<String>();
		for(ColorMapDataSetBundle set : datasets)
		{
			NodeIDs.addAll(set.dataset.getNodeIDs());	
		}
		//add the new layout to all those nodes.
		for(String id : NodeIDs)
		{
			activeLayouts.put(id,newLayout);
		}
		// if everything went fine, register the layout.
		dsm.addDataSetAboutToBeChangedListener(newLayout);
		fireNodesChanged(NodeIDs);
	}
	/**
	 * Check whether a tge node with the given ID is layouted
	 * @param id
	 * @return true if there is a layout for the given ID, or false otherwise. THis does not check, whether the node exists at all.
	 */
	public boolean isNodeLayouted(String id)
	{
		return activeLayouts.containsKey(id);
	}
	/**
	 * Get the layout for a specific id
	 * @param id
	 * @return the layout associated with the ID, or null if the ID has no associated layout.
	 */
	public NodeLayout getLayoutForNode(String id)
	{
		return activeLayouts.get(id);
	}
	/**
	 * Get a Collection of IDs for which layouts exist.
	 * @return A {@link Collection} of Strings containing all IDs with an associated Layout
	 */
	public Collection<String> getLayoutedIDs()
	{
		HashSet<String> layoutedNodeIDs = new HashSet<String>();
		layoutedNodeIDs.addAll(activeLayouts.keySet());
		layoutedNodeIDs.retainAll(NetworkIDs);
		return layoutedNodeIDs;
	}
	/**
	 * Handle a {@link SessionAboutToBeSavedEvent}. Since the order in which the event is handles by the different components of the app is important,
	 * This Object does not itself implement the listener, but requires another function to call the handling operation.
	 * @param arg0
	 */
	public void handleEvent(SessionAboutToBeSavedEvent arg0) {

		LinkedList<File> LayoutList = new LinkedList<File>();	
		File LayoutFile = new File(System.getProperty("java.io.tmpdir") + File.separator + IMAGENODEPROPERTIES.LAYOUT_FILE_NAME);
		LayoutList.add(LayoutFile);
		try{
			writeNodeLayouts(LayoutFile);
		}
		catch(IOException e)
		{
			
			PrintFDebugger.Debugging(this, "Could not save the Layouts.\n " + e.toString());
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}
		try{
			arg0.addAppFiles(IMAGENODEPROPERTIES.LAYOUT_FILES, LayoutList);			
		}
		catch(Exception e)
		{
			PrintFDebugger.Debugging(this, "Could not save the Layouts.\n " + e.toString());
			e.printStackTrace(System.out);
			throw new RuntimeException(e);			
		}
	}

	/**
	 * Restore the data of this nodemanager.
	 * Since we need to have a specific order for {@link DataSetManager} and {@link NodeManager} restoration, this cannot simply be achieved
	 * by implementing the {@link SessionLoadedListener} interface
	 * @param arg0
	 */
	public void handleEvent(SessionLoadedEvent arg0) {
		
		//First, obtain all nodeids present in the networks in this session. 
		updateNetworkNodes();

		List<File> LayoutFiles = arg0.getLoadedSession().getAppFileListMap().get(IMAGENODEPROPERTIES.LAYOUT_FILES);		
		if(LayoutFiles == null || LayoutFiles.isEmpty())
		{
			//There is nothing to load!
			return;
		}
		//There should only ever be one entry in the properties!
		File LayoutFile = LayoutFiles.get(0);
		try
		{
			readNodeLayouts(LayoutFile);
		}
		catch(IOException e)
		{
			PrintFDebugger.Debugging(this, "Could not read the Layouts.\n " + e.toString());
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}

	}
	/**
	 * Create a Collection of IDs that are associated with a specific Layout.
	 * @param Layout
	 * @return the Nodes IDs assigned to the provided {@link NodeLayout} 
	 */
	public Collection<String> getNodesForLayout(NodeLayout Layout)
	{
		HashMap<NodeLayout,Set<String>> NodesLayoutedByID = new HashMap<NodeLayout, Set<String>>();
		for(String node : activeLayouts.keySet())
		{
			if(!NodesLayoutedByID.containsKey(activeLayouts.get(node)))
			{
				NodesLayoutedByID.put(activeLayouts.get(node), new HashSet<String>());			
			}
			NodesLayoutedByID.get(activeLayouts.get(node)).add(node);
		}
		return NodesLayoutedByID.get(Layout);
	}
	/**
	 * Write a structure that can be read again by a nodemanager to restore the current state of the manager.
	 * @param LayoutFile
	 * @throws IOException
	 */
	public void writeNodeLayouts(File LayoutFile) throws IOException
	{
		//Set up the Layout to Node map:
		HashMap<NodeLayout,Set<String>> NodesLayoutedByID = new HashMap<NodeLayout, Set<String>>();
		for(String node : activeLayouts.keySet())
		{
			if(!NodesLayoutedByID.containsKey(activeLayouts.get(node)))
			{
				NodesLayoutedByID.put(activeLayouts.get(node), new HashSet<String>());			
			}
			NodesLayoutedByID.get(activeLayouts.get(node)).add(node);
		}
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(LayoutFile));

		for(NodeLayout layout : NodesLayoutedByID.keySet())
		{
			layout.writeLayout(os);
			os.writeObject(NodesLayoutedByID.get(layout));			
		}		
		os.writeObject(new EOOMarker());
		os.close();
	}
	/**
	 * Read Layout data saved in a given File to restore old nodemanager data.
	 * @param LayoutFile
	 * @throws IOException
	 */
	public void readNodeLayouts(File LayoutFile) throws IOException
	{
		//Set up the Layout to Node map:
		ObjectInputStream os = new ObjectInputStream(new FileInputStream(LayoutFile));
		boolean notdone = true;
		Set<String> NodeIDsToUpdate = new HashSet<String>(); 
		try{		
			Object currentobject = os.readObject(); 
			while(!(currentobject instanceof EOOMarker)) {
				NodeLayout layout = new NodeLayout();

				notdone = layout.readLayout(dsm, os,currentobject);
				if(notdone)
				{
					Set<String> nodeids = (Set<String>)os.readObject();
					for(String id : nodeids)
					{
						activeLayouts.put(id, layout);
						NodeIDsToUpdate.add(id);
					}
				}
				dsm.addDataSetAboutToBeChangedListener(layout);
				currentobject = os.readObject();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			os.close();
		}
		os.close();
		fireNodesChanged(NodeIDsToUpdate);
		//imagestore.invalidate(NodeIDsToUpdate);		
	}
}
