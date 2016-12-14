package idare.sbmlannotator.internal.Tasks;

import idare.imagenode.internal.IDAREImageNodeApp;
import idare.imagenode.internal.Services.JSBML.SBMLDocument;
import idare.imagenode.internal.Services.JSBML.SBMLManagerHolder;

import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class SBMLAnnotatorTaskFactory extends AbstractTaskFactory implements
NetworkViewTaskFactory {

	protected CyServiceRegistrar reg;
	private SBMLManagerHolder SBMLListener;
	private IDAREImageNodeApp app;	
	public SBMLAnnotatorTaskFactory(CyServiceRegistrar reg,SBMLManagerHolder SBMLListener, IDAREImageNodeApp app) {
		this.reg = reg;
		this.SBMLListener = SBMLListener;
		this.app = app;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return createTask();
	}

	@Override
	public boolean isReady()
	{
		return reg.getService(CyApplicationManager.class).getCurrentNetwork() != null;
	}
	@Override
	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		return createTask();
	}

	@Override
	public boolean isReady(CyNetworkView arg0) {
		if(arg0 != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private TaskIterator createTask()
	{	
		CyNetworkView view = reg.getService(CyApplicationManager.class).getCurrentNetworkView();
		CyNetwork network = view.getModel();
		
		SBMLDocument doc = SBMLListener.readSBML(network);
		return new TaskIterator(new SBMLDocumentSelectionTask(SBMLListener, network, view, app.getSettingsManager(), reg ,doc));
	}

	
	
	
}
