package idare.NodeDuplicator.internal;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

import idare.Properties.IDAREProperties;
import idare.ThirdParty.CytoscapeUtils;
import idare.ThirdParty.DelayedVizProp;
import idare.imagenode.internal.Debug.PrintFDebugger;

public class NodeMergerImpl extends AbstractCyEdit implements Task {

	
	//Services
	CyNetworkViewManager cyViewMgr;
	CyRootNetworkManager RootNetworkManager;
	VisualMappingManager visualMappingManager;
	UndoSupport undoSup;
	NodeRegistry nodeReg;
	CyEventHelper helper;
	
	//Undosupport related fields
	private Map<CyIdentifiable, Map<CyNetworkView, Map<VisualProperty<?>, Object>>> bypassMap = new HashMap<>();
	
	

	//Fields connected to the actual task
	Vector<CySubNetwork> relevantnetworks = new Vector<>();	
	HashMap<CyNetworkView, HashMap<CyNode, Point2D>> originalPos = new HashMap<>();
	HashMap<CySubNetwork,Collection<CyEdge>> removedEdges = new HashMap<>();
	HashMap<CySubNetwork,Collection<CyNode>> removedNodes= new HashMap<>();
	CyNode addedNode;
	HashMap<CyEdge,CyEdge> mappedEdges;
	Set<CyNode> originalNodes;
	
	
	//Fields which should stay constant
	final CyRootNetwork rootnetwork;		
	final CyNode selectedNode;
	
	
	public NodeMergerImpl(CyNode OrigNode, CyNetwork sourceNetwork, CyServiceRegistrar reg) {
		// TODO Auto-generated constructor stub
		super("Merge Nodes");
		helper = reg.getService(CyEventHelper.class);
		cyViewMgr = reg.getService(CyNetworkViewManager.class);
		RootNetworkManager = reg.getService(CyRootNetworkManager.class);
		rootnetwork = RootNetworkManager.getRootNetwork(sourceNetwork);
		visualMappingManager = reg.getService(VisualMappingManager.class);
		selectedNode = OrigNode;
		undoSup = reg.getService(UndoSupport.class);
		nodeReg = reg.getService(NodeRegistry.class);
	}

	private void init()
	{
		relevantnetworks = new Vector<>();
		originalPos = new HashMap<>();
		removedEdges = new HashMap<>();
		removedNodes = new HashMap<>();		
		bypassMap = new HashMap<>();
		mappedEdges = new HashMap<>();
		addedNode = null;
	}

	@Override
	public void redo() {
		init();
		//Turn of the node registry so that it ignores the events issues due to the modificationy by this class.
		nodeReg.deactivate();
		// TODO Auto-generated method stub
		//Get the Edges around the given node.

		CyTable NodeTable = rootnetwork.getDefaultNodeTable();
		//Collect all Nodes, which have the same IDARE_UID
		Long IDARE_ID = NodeTable.getRow(selectedNode.getSUID()).get(IDAREProperties.IDARE_ORIGINAL_NODE, Long.class);			

		//get all Nodes that share this UID		
		originalNodes = CytoscapeUtils.getNodesWithValue(rootnetwork, NodeTable, IDAREProperties.IDARE_ORIGINAL_NODE, IDARE_ID);
		PrintFDebugger.Debugging(this, "There were " + originalNodes.size() + " nodes with ORIGINAL_ID: " + IDARE_ID);
		//And create a new Node in the Root network.
		addedNode = rootnetwork.addNode();
		HashSet<CyEdge> neighbourNodesFrom = new HashSet<>();
		HashSet<CyEdge> neighbourNodesTo = new HashSet<>();
		//Collect all Edges which will be removed
		boolean nodeSetUp = false;
		HashMap<CyNetworkView,Vector<Point2D>> viewpositions = new HashMap<>();
		HashMap<CyNetworkView,Vector<DelayedVizProp>> vizproperties = new HashMap<>();

		for(CyNode nodeToRemove : originalNodes)
		{
			for(CySubNetwork subnetwork : rootnetwork.getSubNetworkList())				
			{
				//if its not in this subnetwork, we don't need to do anything.
				if(!subnetwork.containsNode(nodeToRemove))
				{
					continue;
				}
				else
				{

					//Determine the Edges to remove in this subnetwork
					Collection<CyEdge> EdgesToRemove = subnetwork.getAdjacentEdgeList(nodeToRemove, CyEdge.Type.ANY);
					if(!relevantnetworks.contains(subnetwork))
					{
						//only add the node once.
						//PrintFDebugger.Debugging(this, "Added the node to network with name " + subnetwork.getRow(subnetwork).get(CyNetwork.NAME, String.class));
						relevantnetworks.add(subnetwork);		
						subnetwork.addNode(addedNode);
					}
					//if the node did not yet get all necessary data, we will add it.
					//The only change is that this is NOT a duplicated node and its idare id is the Original Node  
					if(!nodeSetUp)
					{
						CyRow originalNodeRow = subnetwork.getRow(nodeToRemove);
						CyRow newNodeRow = rootnetwork.getRow(addedNode);
						for(CyColumn col : NodeTable.getColumns())
						{
							//Don't copy the primary Key and dont copy the Duplication fields.
							if(col.isPrimaryKey() 
									|| col.getName().equals(IDAREProperties.IDARE_DUPLICATED_NODE) 
									|| col.getName().equals(IDAREProperties.IDARE_ORIGINAL_NODE))
							{
								continue;
							}				
							else
							{
							//	PrintFDebugger.Debugging(this, "Updating Column " + col.getName() + " to " + originalNodeRow.get(col.getName(), col.getType()));

								newNodeRow.set(col.getName(), originalNodeRow.get(col.getName(), col.getType()));
							}
							//set the properties.							
							newNodeRow.set(IDAREProperties.IDARE_NODE_UID,originalNodeRow.get(IDAREProperties.IDARE_ORIGINAL_NODE, Long.class));				
						}
						//PrintFDebugger.Debugging(this,"Name Col: " + subnetwork.getRow(addedNode).get("name",String.class) );
						nodeSetUp = true;
					}
					//Also add the name in the subnetwork...
					subnetwork.getRow(addedNode).set(CyNetwork.NAME, subnetwork.getRow(nodeToRemove).get(CyNetwork.NAME, String.class));
					//PrintFDebugger.Debugging(this,"Name Col: " + subnetwork.getRow(addedNode).get("name",String.class) );
					if(!removedNodes.containsKey(subnetwork))
					{
						removedNodes.put(subnetwork, new HashSet<CyNode>());
						removedEdges.put(subnetwork, new HashSet<CyEdge>());
					}
					removedNodes.get(subnetwork).add(nodeToRemove);
					removedEdges.get(subnetwork).addAll(EdgesToRemove);
					//PrintFDebugger.Debugging(this, "Added nodes and Edges to the set of Removed NOdes and Edges");
					//PrintFDebugger.Debugging(this,"Name Col: " + subnetwork.getRow(addedNode).get("name",String.class) );
					for(CyNetworkView view : cyViewMgr.getNetworkViews(subnetwork))
					{
						saveLockedValues(view.getNodeView(nodeToRemove),view, visualMappingManager.getAllVisualLexicon().iterator().next().getAllDescendants(BasicVisualLexicon.NODE));													
						//PrintFDebugger.Debugging(this, "Updating a view");
						if(!originalPos.containsKey(view))
						{	
							originalPos.put(view, new HashMap<CyNode,Point2D>());							
						}
						if(!viewpositions.containsKey(view))
						{
							viewpositions.put(view, new Vector<>());
						}
						View<CyNode> selectedView = view.getNodeView(nodeToRemove);
						//Add the view to those modified
						//PrintFDebugger.Debugging(this, "Retrieving properties");
						double x = selectedView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
						double y = selectedView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
						saveLockedValues(selectedView,view, visualMappingManager.getAllVisualLexicon().iterator().next().getAllDescendants(BasicVisualLexicon.NODE));
						//PrintFDebugger.Debugging(this, "Assigning directionalities of Edges");
						for(CyEdge edge : subnetwork.getAdjacentEdgeList(nodeToRemove, CyEdge.Type.ANY))
						{
							saveLockedValues(view.getEdgeView(edge),view, visualMappingManager.getAllVisualLexicon().iterator().next().getAllDescendants(BasicVisualLexicon.EDGE));							
							if(edge.getSource().equals(nodeToRemove))
							{								
								neighbourNodesFrom.add(edge); 
							}
							else
							{
								neighbourNodesTo.add(edge);
							}
						}					
						originalPos.get(view).put(nodeToRemove,new Point2D.Double(x,y));
						viewpositions.get(view).add(new Point2D.Double(x,y));
					}
//					PrintFDebugger.Debugging(this,"Name Col: " + subnetwork.getRow(addedNode).get("name",String.class) );
				}
			}
		}
		//Now, we have stored all Adjacent Edges in all subnetworks, and the neighbouring nodes.
		//We also added the new node.
		//So we now remove all Nodes, calculate the central point of those nodes, move the new node to that point and 
		//connect the new node with all neighbours.
		mappedEdges = new HashMap<>();

		//Add the new Edges to the Root Network
		//Connect the new Node to its neighbours.
		for(CyEdge edge : neighbourNodesFrom)
		{
			mappedEdges.put(edge, rootnetwork.addEdge(addedNode,edge.getTarget(),edge.isDirected()));		
		}
		for(CyEdge edge : neighbourNodesTo)
		{
			mappedEdges.put(edge,rootnetwork.addEdge(edge.getSource(),addedNode,edge.isDirected()));
		}
		

		for(CySubNetwork subnetwork : relevantnetworks)				
		{			
			for(CyEdge edge : mappedEdges.keySet())
			{
				if(subnetwork.containsEdge(edge))
				{
					subnetwork.addEdge(mappedEdges.get(edge));
				}
			}
			for(CyNetworkView cview : originalPos.keySet())
			{
				Point2D pos = getCenter(viewpositions.get(cview));
				if(!vizproperties.containsKey(cview))
				{
					vizproperties.put(cview, new Vector<>());
				}				

				vizproperties.get(cview).add(new DelayedVizProp(addedNode, BasicVisualLexicon.NODE_X_LOCATION, pos.getX(), false));
				vizproperties.get(cview).add(new DelayedVizProp(addedNode, BasicVisualLexicon.NODE_Y_LOCATION, pos.getY(), false));								
				helper.flushPayloadEvents();
				cview.updateView();
			}
			//Remove the original nodes.
			subnetwork.removeEdges(removedEdges.get(subnetwork));
			subnetwork.removeNodes(removedNodes.get(subnetwork));
//			PrintFDebugger.Debugging(this,"Name Col: " + subnetwork.getRow(addedNode).get("name",String.class) );
		}

		for(CyNetworkView view : vizproperties.keySet())
		{
			DelayedVizProp.applyAll(view, vizproperties.get(view));
			VisualStyle style = visualMappingManager.getVisualStyle(view);

			style.apply(view.getModel().getRow(addedNode),view.getNodeView(addedNode));
			for(CyEdge item : mappedEdges.keySet())
			{
				if(view.getModel().containsEdge(mappedEdges.get(item)))
				{
					style.apply(view.getModel().getRow(mappedEdges.get(item)),view.getEdgeView(mappedEdges.get(item)));
				}
			}						
			view.updateView();

		}
		//Finally, flush all stored events, and reactivate the registry
		helper.flushPayloadEvents();
		nodeReg.activate();
	}


	@Override
	public void undo() {
		//First, deactive the Node Registry
		nodeReg.deactivate();
		//We have to do the following:
		//restore the Nodes and edges to the relevant views
		//remove the created Node from the root Network (and all subnetworks)
		//restore the visual properties


		//restore the nodes and edges to the networks.
		//and remove the added nodes.
		for(CySubNetwork subNetwork  : relevantnetworks)
		{
			//add the original nodes
			for(CyNode orignode : removedNodes.get(subNetwork))
			{
				subNetwork.addNode(orignode);				
			}

			for(CyEdge origEdge : removedEdges.get(subNetwork))
			{				
				subNetwork.addEdge(origEdge);				
			}
			//Then remove all Edges leading to the added node
			subNetwork.removeEdges(subNetwork.getAdjacentEdgeList(addedNode, CyEdge.Type.ANY));
			//And the node itself
			subNetwork.removeNodes(Collections.singleton(addedNode));

		}			
		//Now, propagate all events (which also generates and destroy the views
		helper.flushPayloadEvents();
		for(CySubNetwork subNetwork : relevantnetworks)
		{
			for(CyNetworkView cview : cyViewMgr.getNetworkViews(subNetwork))
			{
				VisualStyle style = visualMappingManager.getVisualStyle(cview);
				for(CyNode node : removedNodes.get(subNetwork))
				{
					setLockedValues(cview.getNodeView(node), cview);
					cview.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, originalPos.get(cview).get(node).getX());
					cview.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, originalPos.get(cview).get(node).getY());
					style.apply(cview.getModel().getRow(node),cview.getNodeView(node));
				}
				
				for(CyEdge edge : removedEdges.get(subNetwork))
				{
					setLockedValues(cview.getEdgeView(edge), cview);
					style.apply(cview.getModel().getRow(edge),cview.getEdgeView(edge));
				}											


				cview.updateView();
			}
		}		
		rootnetwork.removeNodes(Collections.singleton(addedNode));
		rootnetwork.removeEdges(mappedEdges.values());
		//Finally, flush all stored events, and reactivate the registry
		helper.flushPayloadEvents();
		nodeReg.activate();
	}


	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		undoSup.postEdit(this);
		this.redo();
	}



	private Vector<DelayedVizProp> generateLockedValuesDelayedVizPropsCopy(final View<? extends CyIdentifiable> sourceview, CyIdentifiable target,
			final Collection<VisualProperty<?>> visualProps) {
		Vector<DelayedVizProp> copiedprops = new Vector<>();
		for (final VisualProperty<?> vp : visualProps) {
			if (sourceview.isValueLocked(vp)) {								
				copiedprops.add(new DelayedVizProp(target, vp, sourceview.getVisualProperty(vp), true));				
			}
		}
		return copiedprops;
	}

	private Point2D getCenter(Vector<Point2D> positions)
	{
		double xval = 0;
		double yval = 0;
		for(Point2D pos :positions)
		{
			xval += pos.getX();
			yval += pos.getY();
		}
		xval /= positions.size();
		yval /= positions.size();
		return new Point2D.Double(xval,yval);
	}

	/*
	 * #%L
	 * The following functions are modified copies from DeleteEdit in Cytoscape 
	 * $Id:$
	 * $HeadURL:$
	 * %%
	 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
	 * %%
	 * This program is free software: you can redistribute it and/or modify
	 * it under the terms of the GNU Lesser General Public License as 
	 * published by the Free Software Foundation, either version 2.1 of the 
	 * License, or (at your option) any later version.
	 * 
	 * This program is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	 * GNU General Lesser Public License for more details.
	 * 
	 * You should have received a copy of the GNU General Lesser Public 
	 * License along with this program.  If not, see
	 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
	 * #L%
	 */

	private void saveLockedValues(final View<? extends CyIdentifiable> view, CyNetworkView netView,
			final Collection<VisualProperty<?>> visualProps) {
		for (final VisualProperty<?> vp : visualProps) {
			if (view.isValueLocked(vp)) {								

				Map<CyNetworkView,Map<VisualProperty<?>, Object>> vp_per_view_Map = bypassMap.get(view.getModel());

				if (vp_per_view_Map == null)
					bypassMap.put(view.getModel(), vp_per_view_Map = new HashMap<CyNetworkView,Map<VisualProperty<?>, Object>>());

				Map<VisualProperty<?>, Object> vpMap = vp_per_view_Map.get(netView);

				if (vpMap == null)
					vp_per_view_Map.put(netView,vpMap = new HashMap<VisualProperty<?>, Object>());

				vpMap.put(vp, view.getVisualProperty(vp));
			}
		}
	}

	private void setLockedValues(final View<? extends CyIdentifiable> view, CyNetworkView netView) {

		if(bypassMap.containsKey(view.getModel()))
		{
			if(bypassMap.get(view.getModel()).containsKey(netView))
			{
				for (final Entry<VisualProperty<?>, Object> entry : bypassMap.get(view.getModel()).get(netView).entrySet())
					view.setLockedValue(entry.getKey(), entry.getValue());
			}
		}
	}	




}

