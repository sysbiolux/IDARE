package idare.subnetwork.internal.Tasks.SubsystemGeneration;

import java.util.Set;
import java.util.Vector;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;

public class SubNetworkProperties {

	/**
	 * IDs for the individual types
	 */
	public String selectedLayoutAlgorithmName;
	public Vector<Object> subSystems;
	public Set<CyNode> ignoredNodes;
	public Set<CyNode> noBranchNodes;
	public CyNetwork currentNetwork;
	public CyNetworkView currentNetworkView;
	public CyLayoutAlgorithm layoutAlgorithm;
	public String ColumnName;

}
