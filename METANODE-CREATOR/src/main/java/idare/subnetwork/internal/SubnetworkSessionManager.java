package idare.subnetwork.internal;

import org.cytoscape.session.events.SessionLoadedEvent;

import idare.Properties.IDARESettingsManager;
/**
 * A SessionListener that restores the SubNetwork Interaction Links from a former Session. 
 * @author thomas
 *
 */
public class SubnetworkSessionManager{

	private NetworkViewSwitcher nvs;	


	public SubnetworkSessionManager(NetworkViewSwitcher nvs, IDARESettingsManager iDAREIDMgr) {
		super();
		this.nvs = nvs;
	}

	// restore the networkview links from the CyTables
	public void handleSessionLoadedEvent(SessionLoadedEvent e){
		nvs.handleEvent(e);
		//First check whether this system is set up for IDARE use.				
	}



}
