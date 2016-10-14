package idare.sbmlannotator.internal.Tasks;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Tunable;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.Services.JSBML.SBMLDocument;
import idare.imagenode.internal.Services.JSBML.SBMLManagerHolder;

public class SBMLAnnotatorTaskFBC extends SBMLAnnotatorTask {

	@Tunable(description="Should FBC Nodes and edges be removed", dependsOn="generateGeneNodes=true")
	public boolean removeFBCNodes = true;

	
	public SBMLAnnotatorTaskFBC(SBMLManagerHolder holder, CyNetwork network, IDARESettingsManager ism,
			CyNetworkView networkView, CyEventHelper eventHelper, SBMLDocument doc) {
		super(holder, network, ism, networkView, eventHelper,doc);
		// TODO Auto-generated constructor stub
	}

}
