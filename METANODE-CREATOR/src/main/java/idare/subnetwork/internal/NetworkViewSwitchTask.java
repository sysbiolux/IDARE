package idare.subnetwork.internal;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
/**
 * Simple Task to switch between two network views 
 * @author Thomas Pfau
 *
 */
public class NetworkViewSwitchTask extends AbstractTask{

	private CyApplicationManager appmgr;
	private NodeViewLink targetview;
	/**
	 * A Task switching the current view to the View in the TargetView and focusing on the NodeView listed.
	 * @param appmgr - The {@link CyApplicationManager} of this application
	 * @param targetview - The {@link NodeViewLink} struct containing the target View information. 
	 */
	public NetworkViewSwitchTask(CyApplicationManager appmgr,
			NodeViewLink targetview) {
		super();
		this.appmgr = appmgr;
		this.targetview = targetview;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// If we get a valid object AND the object contains a Valid NetworkView
		arg0.setTitle("Switching View via Linker Node");
		if(targetview != null && targetview.getTargetNetworkView() != null) 
		{
			appmgr.setCurrentNetworkView(targetview.getTargetNetworkView());
			//CyNetworkView view = appmgr.getCurrentNetworkView();
			//if the NodeView can be established, center it...
			
			if(targetview.getNodeView() != null)
			{
				targetview.getTargetNetworkView().setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, targetview.getNodeView().getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
				targetview.getTargetNetworkView().setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, targetview.getNodeView().getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
				//Deselect all currently selected nodes.
				List<CyNode> selectedNodes = CyTableUtil.getNodesInState(targetview.getTargetNetwork(), "selected", true);
				for(CyNode node : selectedNodes)
				{
					targetview.getTargetNetwork().getRow(node).set(CyNetwork.SELECTED, Boolean.FALSE);	
				}
				targetview.getTargetNetwork().getRow(targetview.getTargetNode()).set(CyNetwork.SELECTED, Boolean.TRUE);
				
			}
			targetview.getTargetNetworkView().updateView();
		}
	}

}
