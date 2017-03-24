package idare.NodeDuplicator.Internal;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

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
import idare.ThirdParty.DelayedVizProp;

public class NodeDuplicatorImpl extends AbstractCyEdit implements Task {

	CyNetworkViewManager cyViewMgr;
	CyRootNetworkManager RootNetworkManager;
	CyNode OriginalNode;
	VisualMappingManager visualMappingManager;
	UndoSupport undoSup;
	HashMap<CyNode,CyEdge> OriginalEdges = new HashMap<>(); //Maps Target Nodes to the original Edges
	HashMap<CyNode,CyEdge> createdNodes = new HashMap<>(); // Map from new Nodes to the corresponding Edges
	HashMap<CyEdge,CyNode> replacementNodes = new HashMap<>(); // Maps from original Edges to the New Nodes 
	Vector<CySubNetwork> relevantnetworks = new Vector<>();	
	HashMap<CyNetworkView, Point2D> originalPos = new HashMap<>();
	HashMap<CySubNetwork,Collection<CyEdge>> removedEdges = new HashMap<>();
	
	private Map<CyIdentifiable, Map<CyNetworkView, Map<VisualProperty<?>, Object>>> bypassMap = new HashMap<>();
	CyRootNetwork rootnetwork;
	CyEventHelper helper;
	
	
	public NodeDuplicatorImpl(CyNode OrigNode, CyNetwork sourceNetwork, CyServiceRegistrar reg) {
		// TODO Auto-generated constructor stub
		super("Duplicate Nodes");
		helper = reg.getService(CyEventHelper.class);
		cyViewMgr = reg.getService(CyNetworkViewManager.class);
		RootNetworkManager = reg.getService(CyRootNetworkManager.class);
		rootnetwork = RootNetworkManager.getRootNetwork(sourceNetwork);
		visualMappingManager = reg.getService(VisualMappingManager.class);
		OriginalNode = OrigNode;
		undoSup = reg.getService(UndoSupport.class);
	}
	
	private void init()
	{
		OriginalEdges = new HashMap<>(); //Maps Target Nodes to the original Edges
		createdNodes = new HashMap<>();
		replacementNodes = new HashMap<>();
		relevantnetworks = new Vector<>();
		originalPos = new HashMap<>();
		removedEdges = new HashMap<>();
		bypassMap = new HashMap<>();

		
	}
	@Override
	public void redo() {
		// TODO Auto-generated method stub
		//Get the Edges around the given node.
		CyNetwork network = OriginalNode.getNetworkPointer();
		HashMap<CyNetworkView,Vector<CyNode>> addedNodes= new HashMap<>();
		HashMap<CyNetworkView,Vector<CyEdge>> addedEdges= new HashMap<>();
		
		
		for(CyEdge Edge : rootnetwork.getAdjacentEdgeList(OriginalNode, CyEdge.Type.ANY))
		{
			CyNode targetNode = Edge.getSource().equals(OriginalNode) ? Edge.getTarget() : Edge.getSource();
			OriginalEdges.put(targetNode, Edge);
			CyNode newNode = rootnetwork.addNode();
			
			CyTable nodeTable = rootnetwork.getDefaultNodeTable();
			CyTable edgeTable = rootnetwork.getDefaultEdgeTable();
			CyRow originalNodeRow = nodeTable.getRow(OriginalNode.getSUID());
			CyRow newNodeRow = nodeTable.getRow(newNode.getSUID());
			for(CyColumn col : nodeTable.getColumns())
			{
				//Don't copy the primary Key and dont copy the UID.
				if(col.isPrimaryKey() || col.getName().equals(IDAREProperties.IDARE_NODE_UID))
				{
					continue;
				}				
				else
				{
					newNodeRow.set(col.getName(), originalNodeRow.get(col.getName(), col.getType()));
				}
				//set the properties.
				newNodeRow.set(IDAREProperties.DUPLICATED_NODE,true);
				newNodeRow.set(IDAREProperties.ORIGINAL_NODE,originalNodeRow.get(IDAREProperties.IDARE_NODE_UID, Long.class));				
				
			}
			CyEdge newEdge = Edge.getSource().equals(OriginalNode) ? rootnetwork.addEdge(newNode,Edge.getTarget(), Edge.isDirected()) : rootnetwork.addEdge(Edge.getSource(), newNode, Edge.isDirected());
			createdNodes.put(newNode, newEdge);
			replacementNodes.put(Edge, newNode);
			CyRow newEdgeRow = edgeTable.getRow(newEdge.getSUID());
			CyRow originalEdgeRow = edgeTable.getRow(Edge.getSUID());
			for(CyColumn col : edgeTable.getColumns())
			{
				//Don't copy the primary Key and dont copy the UID.
				if(col.isPrimaryKey() || col.getName().equals(IDAREProperties.IDARE_NODE_UID))
				{
					continue;
				}
				else
				{
					newEdgeRow.set(col.getName(), originalEdgeRow.get(col.getName(), col.getType()));
				}				
			}
		}

		//Now, add all Nodes created to the relevant subnetworks, and remove the corresponding nodes.
		HashMap<CyNetworkView,Vector<DelayedVizProp>> vizproperties = new HashMap<>();
		for(CySubNetwork subnetwork : rootnetwork.getSubNetworkList())				
		{
			//if its not in this subnetwork, we don't need to do anything.
			if(!subnetwork.containsNode(OriginalNode))
			{
				continue;
			}
			else
			{
				//Determine the Edges to remove in this subnetwork
				Collection<CyEdge> EdgesToRemove = subnetwork.getAdjacentEdgeList(OriginalNode, CyEdge.Type.ANY);				
				relevantnetworks.add(subnetwork);				
				for(CyNetworkView view : cyViewMgr.getNetworkViews(subnetwork))
				{
					//Add the view to those modified	
					double x = view.getNodeView(OriginalNode).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
					double y = view.getNodeView(OriginalNode).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
					originalPos.put(view, new Point2D.Double(x,y));
					addedNodes.put(view, new Vector<CyNode>());
					addedEdges.put(view, new Vector<CyEdge>());
				}
				removedEdges.put(subnetwork,EdgesToRemove);
				//Removing the Views will happen automatically. 						
				//Remove the original node view

				//Add the node views to the new nodes
				for(CyEdge edge : EdgesToRemove)
				{
					//Add the Node and the corresponding Edge from the generated set.
					CyNode nodeToAdd = replacementNodes.get(edge);
					CyEdge edgeToAdd = createdNodes.get(replacementNodes.get(edge));
					subnetwork.addNode(nodeToAdd);
					subnetwork.addEdge(edgeToAdd);
					
					for(CyNetworkView view : cyViewMgr.getNetworkViews(subnetwork))
					{
						addedNodes.get(view).add(nodeToAdd);
						addedEdges.get(view).add(edgeToAdd);
						if(!vizproperties.containsKey(view))
						{
							vizproperties.put(view, new Vector<>());
						}
						View<CyNode> originalView = view.getNodeView(OriginalNode);
						vizproperties.get(view).add(new DelayedVizProp(nodeToAdd, BasicVisualLexicon.NODE_X_LOCATION, originalView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION), false));
						vizproperties.get(view).add(new DelayedVizProp(nodeToAdd, BasicVisualLexicon.NODE_Y_LOCATION, originalView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION), false));												
						vizproperties.get(view).addAll(generateLockedValuesDelayedVizPropsCopy(originalView, nodeToAdd, visualMappingManager.getAllVisualLexicon().iterator().next().getAllDescendants(BasicVisualLexicon.NODE)));
						vizproperties.get(view).addAll(generateLockedValuesDelayedVizPropsCopy(view.getEdgeView(edge), edgeToAdd, visualMappingManager.getAllVisualLexicon().iterator().next().getAllDescendants(BasicVisualLexicon.EDGE)));
						saveLockedValues(originalView,view, visualMappingManager.getAllVisualLexicon().iterator().next().getAllDescendants(BasicVisualLexicon.NODE));
						saveLockedValues(view.getEdgeView(edge),view, visualMappingManager.getAllVisualLexicon().iterator().next().getAllDescendants(BasicVisualLexicon.EDGE));	
					}
				}				
				//finally remove the original nodes.
				subnetwork.removeEdges(EdgesToRemove);
				subnetwork.removeNodes(Collections.singleton(OriginalNode));
				helper.flushPayloadEvents();
			}
			
		}
		for(CyNetworkView view : vizproperties.keySet())
		{
			DelayedVizProp.applyAll(view, vizproperties.get(view));
			VisualStyle style = visualMappingManager.getVisualStyle(view);
			for(CyNode item : addedNodes.get(view))
			{
				style.apply(view.getModel().getRow(item),view.getNodeView(item));
			}
			for(CyEdge item : addedEdges.get(view))
			{
				style.apply(view.getModel().getRow(item),view.getEdgeView(item));
			}						
			view.updateView();
			
		}
		
	}

	
	@Override
	public void undo() {
		//We have to do the following:
		//restore the Node and edges to the relevant views
		//remove the created Nodes from the root Network (and all subnetworks)
		//restore the visual properties
		for(CySubNetwork subNetwork  : relevantnetworks)
		{
			//restore the original Nodes and edges
			subNetwork.addNode(OriginalNode);
			for(CyEdge edge : removedEdges.get(subNetwork))
			{
				subNetwork.addEdge(edge);
			}
			Vector<CyNode> nodesToRemove = new Vector<>();
			Vector<CyEdge> edgesToRemove = new Vector<>();
			for(CyNode node : createdNodes.keySet())
			{
				if(subNetwork.containsNode(node))
				{
					nodesToRemove.add(node);
					edgesToRemove.add(createdNodes.get(node));
				}
			}
			subNetwork.removeEdges(edgesToRemove);
			subNetwork.removeNodes(nodesToRemove);
			helper.flushPayloadEvents();

			final Collection<CyNetworkView> views = cyViewMgr.getNetworkViews(subNetwork);
			
			for(CyNetworkView view : cyViewMgr.getNetworkViews(subNetwork))
			{
				View<CyNode> nodeView = view.getNodeView(OriginalNode);
				VisualStyle style = visualMappingManager.getVisualStyle(view);
				if (nodeView == null) continue;
				nodeView.setVisualProperty(NODE_X_LOCATION, originalPos.get(view).getX());
				nodeView.setVisualProperty(NODE_Y_LOCATION, originalPos.get(view).getY() );
				setLockedValues(nodeView, view);
				style.apply(subNetwork.getRow(OriginalNode),nodeView);
				for (CyEdge edge: removedEdges.get(subNetwork)) {
					View<CyEdge> edgeView = view.getEdgeView(edge);
					if (edgeView == null) continue;
					setLockedValues(edgeView,view);
					style.apply(subNetwork.getRow(edge), edgeView);
				}
				
				view.updateView();
			}			
		}
		//finally, also remove the created nodes from the root network
		rootnetwork.removeEdges(createdNodes.values());
		rootnetwork.removeNodes(createdNodes.keySet());
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
