package idare.sbmlannotator.internal.Tasks;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.Services.JSBML.SBMLDocument;
import idare.imagenode.internal.Services.JSBML.SBMLManagerHolder;
import idare.sbmlannotator.internal.gpr.GPRManager;

public class SBMLDocumentSelectionTask extends AbstractTask{

	private CyNetwork network;
	IDARESettingsManager ism;	
	CyEventHelper eventHelper;
	CyNetworkView networkView;	
	SBMLManagerHolder holder;
	SBMLDocument doc;
	SBMLDocumentSelectionTask(SBMLManagerHolder holder, CyNetwork network, IDARESettingsManager ism,
			CyNetworkView networkView, CyEventHelper eventHelper, SBMLDocument doc) {

		this.eventHelper = eventHelper;
		this.networkView = networkView;		
		this.network = network;
		this.ism = ism;
		this.holder = holder;
		this.doc = doc;
	}	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		//SBMLDocument doc = holder.readSBML(network);
		if(doc != null)
		{
			boolean hasFBCNodes = doc.getModel().isFBCPackageEnabled();
			if(hasFBCNodes)
			{
				insertTasksAfterCurrentTask(new SBMLAnnotatorTaskFBC(holder,network,ism,networkView,eventHelper,doc));
			}
			else
			{
				insertTasksAfterCurrentTask(new SBMLAnnotatorTask(holder,network,ism,networkView,eventHelper,doc));
			}
		}
		else
		{
			throw new Exception(holder.getSBMLStatus());
		}

	}

}
