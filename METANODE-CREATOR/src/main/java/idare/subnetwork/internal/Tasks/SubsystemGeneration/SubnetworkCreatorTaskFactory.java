package idare.subnetwork.internal.Tasks.SubsystemGeneration;

import idare.Properties.IDARESettingsManager;
import idare.subnetwork.internal.NetworkViewSwitcher;
import idare.subnetwork.internal.Tasks.propertySelection.SubnetworkColumnPropertiesSelectionTask;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


/**
 * Task Factory for Subnetwork Creation
 * @author Thomas Pfau
 *
 */
public class SubnetworkCreatorTaskFactory extends AbstractTaskFactory implements NetworkViewTaskFactory{

	CyServiceRegistrar reg;
	NetworkViewSwitcher nvs;
	IDARESettingsManager ism;
	SubnetworkCreationGUIHandlerFactory sncghf;	
	
	public SubnetworkCreatorTaskFactory(CyServiceRegistrar reg,
			NetworkViewSwitcher nvs, IDARESettingsManager ism, SubnetworkCreationGUIHandlerFactory sncghf) {
		super();
		this.reg = reg;
		this.nvs = nvs;
		this.ism = ism;
		this.sncghf = sncghf;
	}


	@Override
	public TaskIterator createTaskIterator() {
		SubnetworkColumnPropertiesSelectionTask task = new SubnetworkColumnPropertiesSelectionTask(reg, nvs, ism, sncghf);
		return new TaskIterator(task);
	}
	
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		return createTaskIterator();
	}
	
	/**
	 * If we obtain a view, this has to be possible, as we are obviously in a view.
	 */
	@Override
	public boolean isReady(CyNetworkView arg0) {
		return arg0 != null;
	}

}
