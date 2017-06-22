package idare.NodeDuplicator.Internal;

import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;

public class NodeDuplicatorFactory implements NodeViewTaskFactory {

	CyServiceRegistrar reg;
	public NodeDuplicatorFactory(CyServiceRegistrar reg) {
		// TODO Auto-generated constructor stub
		this.reg = reg;
	}	
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> arg0, CyNetworkView arg1) {
		return new TaskIterator(new NodeDuplicatorImpl(arg0.getModel(),arg1.getModel(), reg, true));		
	}
	

	@Override
	public boolean isReady(View<CyNode> arg0, CyNetworkView arg1) {
		//We can duplicate, if this is not a duplicated node and the network is set up for IDARE.
		return IDARESettingsManager.isSetupNetwork(arg1.getModel()) && !arg1.getModel().getRow(arg0.getModel()).isSet(IDAREProperties.IDARE_DUPLICATED_NODE) || !(arg1.getModel().getRow(arg0.getModel()).get(IDAREProperties.IDARE_DUPLICATED_NODE,Boolean.class));
	}

}
