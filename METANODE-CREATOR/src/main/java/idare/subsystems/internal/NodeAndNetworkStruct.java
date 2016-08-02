package idare.subsystems.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * Helper Struct to bundle a Node and its containing network since it seems, that the network pointer in a node is not always set...
 * @author thomas
 *
 */
public class NodeAndNetworkStruct {

	public CyNode node;
	public CyNetwork network;
	/**
	 * Default constructor using a {@link CyNode} and a {@link CyNetwork}
	 * @param Node
	 * @param network
	 */
	public NodeAndNetworkStruct(CyNode Node, CyNetwork network)
	{
		this.node = Node;
		this.network = network;
	}
}
