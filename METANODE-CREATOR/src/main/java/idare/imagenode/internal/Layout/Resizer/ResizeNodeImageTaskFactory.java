package idare.imagenode.internal.Layout.Resizer;

import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Layout.Resizer.Tasks.FreeScalingResizeNodeImageTask;
import idare.imagenode.internal.Layout.Resizer.Tasks.ResizeNodeImageTask;

public class ResizeNodeImageTaskFactory extends AbstractTaskFactory implements NodeViewTaskFactory,NetworkViewTaskFactory{

		
	private Double scalingFactor;
	private NodeManager nm;
	private boolean resizeAllStyles;	
	/**
	 * Constructor for a ResizeNodeTaskFactor
	 * @param nm The {@link NodeManager} to look up node properties
	 * @param scalingFactor The scaling factor to use for resize 
	 * @param allStyles whether to apply to all Styles.
	 */
	public ResizeNodeImageTaskFactory(NodeManager nm, Double scalingFactor, boolean allStyles) {
		this.scalingFactor = scalingFactor;
		this.resizeAllStyles = allStyles;
		this.nm = nm;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(arg0.getModel(),"selected",true);
		if(selectedNodes.size() > 1 || selectedNodes.size() == 0)
		{
			return getApplicableTask(null);
		}
		else
		{
			return getApplicableTask(arg0.getModel().getRow(selectedNodes.get(0)));
		}		
	}

	@Override
	public boolean isReady(CyNetworkView arg0) {
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(arg0.getModel(),"selected",true);
		if(selectedNodes.size() > 1 || selectedNodes.size() == 0)
		{
			return getReadyStatus(null);
		}
		else
		{
			return getReadyStatus(arg0.getModel().getRow(selectedNodes.get(0)));
		}
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> arg0, CyNetworkView arg1) {
		
		return getApplicableTask(arg1.getModel().getRow(arg0.getModel()));
	}

	@Override
	public boolean isReady(View<CyNode> arg0, CyNetworkView arg1) {
		//Check the choosen node.
		return getReadyStatus(arg1.getModel().getRow(arg0.getModel()));
	}

	
	private boolean getReadyStatus(CyRow Node)
	{
		//if all are resized, we only have to check, if there are ANY layouts
		if(resizeAllStyles)
		{
			return !nm.getCurrentLayouts().isEmpty();
		}
		else
		{
			//otherwise, if no node is selected, nothing can happen.
			if(Node == null)
			{
				return false;
			}
			else
			{
				//and if a node is selected it has to be layed out.
				return nm.getLayoutForNode(nm.getIDAREName(Node)) != null;
			}
		}
	}
	
	private TaskIterator getApplicableTask(CyRow Node)
	{
		if(resizeAllStyles)
		{
			if(scalingFactor == null)
			{
				return new TaskIterator(new FreeScalingResizeNodeImageTask(nm, null));
			}
			else
			{
				return new TaskIterator(new ResizeNodeImageTask(nm,null,scalingFactor));
			}
		}
		else
		{
			String nodeID = nm.getIDAREName(Node);
			if(scalingFactor == null)
			{
				return new TaskIterator(new FreeScalingResizeNodeImageTask(nm,nm.getLayoutForNode(nodeID)));
			}
			else
			{
				return new TaskIterator(new ResizeNodeImageTask(nm,nm.getLayoutForNode(nodeID),scalingFactor));
			}
		}
	}

	@Override
	public TaskIterator createTaskIterator() {		
		return getApplicableTask(null);
	}
}
