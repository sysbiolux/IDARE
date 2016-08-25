package idare.imagenode.internal.GUI.NetworkSetup.Tasks;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class NetworkSetupTaskFactory extends AbstractTaskFactory implements
		NetworkViewTaskFactory {

	
	CyApplicationManager appmgr;
	
	public NetworkSetupTaskFactory(CyApplicationManager appmgr) {
		this.appmgr = appmgr;
		 
	}
	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new NetworkSetupTask());
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		// TODO Auto-generated method stub
		return new TaskIterator(new NetworkSetupTask());
	}

	/**
	 * This factory is in general ready if there is a network selected.
	 */
	@Override
	public boolean isReady(CyNetworkView arg0) {
		return appmgr.getCurrentNetwork() != null;
	}
	
	/**
	 * This factory is in general ready if there is a network selected.
	 */
	@Override 
	public boolean isReady()
	{
		return appmgr.getCurrentNetwork() != null;
	}

}
