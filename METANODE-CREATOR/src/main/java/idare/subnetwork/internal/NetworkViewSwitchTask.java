
package idare.subnetwork.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import idare.Properties.IDAREProperties;
import idare.ThirdParty.CytoscapeUtils;
/**
 * Simple Task to switch between two network views 
 * @author Thomas Pfau
 *
 */
public class NetworkViewSwitchTask extends AbstractTask{
	
	private CyNode node;
	CyServiceRegistrar reg;
	CyNetwork origin;
	/**
	 * A Task switching the current view to the View in the TargetView and focusing on the NodeView listed.
	 * @param appmgr - The {@link CyApplicationManager} of this application
	 * @param targetview - The {@link NodeViewLink} struct containing the target View information. 
	 */
	public NetworkViewSwitchTask(CyServiceRegistrar reg,
			CyNode node, CyNetwork origin) {
		super();
		this.node = node;
		this.reg = reg;
		this.origin = origin;
		
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		//First, get The target subsystem and target link.
		Long TargetNetworkID = origin.getDefaultNodeTable().getRow(node.getSUID()).get(IDAREProperties.IDARE_LINK_TARGET_SUBSYSTEM,Long.class);
		Long TargetNodeID = origin.getDefaultNodeTable().getRow(node.getSUID()).get(IDAREProperties.IDARE_LINK_TARGET,Long.class);
		//Now, get the actual target network.
		CyNetwork TargetNetwork = getTargetNetwork(TargetNetworkID, origin);
		//And the Target node
		Set<CyNode> TargetNodes = new HashSet<>();
		if(TargetNetwork != null)
		{
			TargetNodes = getNode(TargetNodeID, TargetNetwork);
		}
		
		if(TargetNodes.isEmpty())
		{
			return;
		}
		//Now, get the Target View
		CyApplicationManager appmgr = reg.getService(CyApplicationManager.class);
		CyNetworkViewManager viewmgr = reg.getService(CyNetworkViewManager.class);
		//We will switch to the next best view, that contains the node
		for(CyNetworkView targetNetworkView : viewmgr.getNetworkViews(TargetNetwork))
		{
			Vector<CyNode> NodesToSelect = new Vector<>();
			for(CyNode TargetNode : TargetNodes)
			{
				View<CyNode> targetNodeView = targetNetworkView.getNodeView(TargetNode);
				if(targetNodeView != null)
				{
					//Update the position of the center
					targetNetworkView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, targetNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
					targetNetworkView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, targetNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
					NodesToSelect.add(TargetNode);
					//and the selected nodes
				}								
			
			}
			if(!NodesToSelect.isEmpty())
			{
				appmgr.setCurrentNetworkView(targetNetworkView);
				List<CyNode> selectedNodes = CyTableUtil.getNodesInState(TargetNetwork, "selected", true);
				for(CyNode node : selectedNodes)
				{
					TargetNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.FALSE);	
				}
				for(CyNode node : NodesToSelect)
				{
					TargetNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.TRUE);	
				}				

				targetNetworkView.updateView();
				return;
			}
		}		
	}

	private CyNetwork getTargetNetwork(Long IDAREId, CyNetwork sourcenetwork)
	{		
				
		CyRootNetwork rootnetwork = reg.getService(CyRootNetworkManager.class).getRootNetwork(sourcenetwork);
		for(CySubNetwork subnetwork: rootnetwork.getSubNetworkList())
		{
			Collection<CyRow> matchingNetworks = subnetwork.getDefaultNetworkTable().getMatchingRows(IDAREProperties.IDARE_NETWORK_ID, IDAREId);			
			for(CyRow row : matchingNetworks)
			{				
				CyNetwork targetNetwork = reg.getService(CyNetworkManager.class).getNetwork(row.get(CyNetwork.SUID, Long.class));				
				if(targetNetwork != null)
				{					
					return targetNetwork;
				}
			}
		}
		return null;
	}
	
	//This will return the first possible node.
	private Set<CyNode> getNode(Long IDAREId, CyNetwork targetNetwork)
	{		
		//Get both non duplicated and duplicated nodes 
		Set<CyNode> matchingNodes = CytoscapeUtils.getNodesWithValue(targetNetwork, targetNetwork.getDefaultNodeTable(), IDAREProperties.IDARE_NODE_UID, IDAREId);
		matchingNodes.addAll(CytoscapeUtils.getNodesWithValue(targetNetwork, targetNetwork.getDefaultNodeTable(), IDAREProperties.IDARE_ORIGINAL_NODE, IDAREId));
		if(matchingNodes.size() > 0)
		{
			return matchingNodes;
		}
		return null;
	}
}