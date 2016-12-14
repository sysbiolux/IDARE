package idare.sbmlannotator.internal.Tasks;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Tunable;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.Services.JSBML.SBMLDocument;
import idare.imagenode.internal.Services.JSBML.SBMLManagerHolder;

public class SBMLAnnotatorTaskFBC extends SBMLAnnotatorTask {

	@Tunable(description="Should FBC Nodes and edges be removed", dependsOn="generateGeneNodes=true")
	public boolean removeFBCNodes = true;

	
	public SBMLAnnotatorTaskFBC(SBMLManagerHolder holder, CyNetwork network,
			CyNetworkView networkView, IDARESettingsManager ism, CyServiceRegistrar reg, SBMLDocument doc) {
		super(holder, network, networkView, ism, reg,doc);
		// TODO Auto-generated constructor stub
	}

}
