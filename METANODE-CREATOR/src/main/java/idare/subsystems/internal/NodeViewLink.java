package idare.subsystems.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/**
 * A Struct class to bundle information for a target node.

 */
public class NodeViewLink {

	private View<CyNode> targetNodeView;
	private CyNode targetNode;
	private CyNetworkView targetNetworkView;
	private CyNetwork targetNetwork;
	/**
	 * Default Constructor to Combine the {@link View} of a target {@link CyNode} wth the respective {@link CyNetwork}, {@link CyNetworkView} and the {@link CyNode} 
	 * @param targetNodeView
	 * @param targetNetworkView
	 * @param targetnetwork
	 * @param targetnode
	 */
	public NodeViewLink(View<CyNode> targetNodeView, CyNetworkView targetNetworkView,CyNetwork targetnetwork, CyNode targetnode) {
		this.targetNodeView = targetNodeView;
		this.targetNetworkView = targetNetworkView;
		this.targetNetwork = targetnetwork;
		this.targetNode = targetnode;
	}
	/**
	 * Get the View of the Node
	 * @return The {@link View} of the target node 
	 */
	public View<CyNode> getNodeView() {
		return targetNodeView;
	}
	/**
	 * Get the CyNode
	 * @return the target {@link CyNode}
	 */
	public CyNode getTargetNode() {
		return targetNode;
	}
//	/**
//	 * Set the target {@link CyNode};
//	 * @param node
//	 */
//	public void setTargetNode(CyNode node) {
//		this.targetNode = node;
//	}
	/**
	 * Get the target {@link CyNetwork}
	 * @return the target {@link CyNetwork}
	 */
	public CyNetwork getTargetNetwork() {
		return targetNetwork;
	}

//	public void setTargetNetwork(CyNetwork network) {
//		this.targetNetwork = network;
//	}
	/**
	 * Set the target {@link View} for the node of this Link.
	 * @param nodeView
	 */
	public void setTargetNodeView(View<CyNode> nodeView) {
		this.targetNodeView = nodeView;
	}
	/**
	 * Set the target {@link CyNetworkView} for the node of this Link.
	 * @param networkView
	 */
	public void setTargetNetworkView(CyNetworkView networkView) {
		this.targetNetworkView = networkView;
	}
	/**
	 * Get the Target {@link CyNetworkView} for this Link.
	 * @return the target {@link CyNetworkView}
	 */
	public CyNetworkView getTargetNetworkView() {
		return targetNetworkView;
	}
	
	
	
}
