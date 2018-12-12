package idare.NodeDuplicator.internal;

import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;

public class NodeMergerFactory implements NodeViewTaskFactory {

	CyServiceRegistrar reg;
	
	public NodeMergerFactory(CyServiceRegistrar reg) {
		// TODO Auto-generated constructor stub
		this.reg = reg;
	}
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> arg0, CyNetworkView arg1) {
		// TODO Auto-generated method stub
		return new TaskIterator(new NodeMergerImpl(arg0.getModel(), arg1.getModel(), reg));
	}

	@Override
	public boolean isReady(View<CyNode> arg0, CyNetworkView arg1) {
		if(IDARESettingsManager.isSetupNetwork(arg1.getModel()))
		{
			
			return arg1.getModel().getRow(arg0.getModel()).isSet(IDAREProperties.IDARE_DUPLICATED_NODE) && arg1.getModel().getRow(arg0.getModel()).get(IDAREProperties.IDARE_DUPLICATED_NODE, Boolean.class);
		}		
		return false;
	}

}
