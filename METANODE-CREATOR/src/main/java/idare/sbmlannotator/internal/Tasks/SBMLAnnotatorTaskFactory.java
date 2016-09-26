package idare.sbmlannotator.internal.Tasks;

import idare.imagenode.internal.IDAREImageNodeApp;
import idare.imagenode.internal.Services.JSBML.SBMLManagerHolder;

import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class SBMLAnnotatorTaskFactory extends AbstractTaskFactory implements
NetworkViewTaskFactory {

	protected CyApplicationManager cyAppMgr;
	protected CyEventHelper eventHelper;
	protected final FileUtil fileutil;	
	protected CySwingApplication cySwingApp;
	private SBMLManagerHolder SBMLListener;
	private IDAREImageNodeApp app;	
	public SBMLAnnotatorTaskFactory(final CyApplicationManager applicationManager,
			CyEventHelper eventHelper, FileUtil fileutil, CySwingApplication cySwingApp,SBMLManagerHolder SBMLListener, IDAREImageNodeApp app) {
		this.cyAppMgr = applicationManager;
		this.fileutil = fileutil;
		this.eventHelper = eventHelper;
		this.cySwingApp = cySwingApp;
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
		return cyAppMgr.getCurrentNetwork() != null;
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
		
		CyNetworkView view = cyAppMgr.getCurrentNetworkView();
		CyNetwork network = view.getModel();
		//SBMLDocument doc = SBMLListener.readSBML(network);
		return new TaskIterator(new SBMLAnnotatorTask(SBMLListener, network, app.getSettingsManager(), view, eventHelper));
	}

	
	
	
}
