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

	private	IDARESettingsManager IDAREIDMgr;


	public SubnetworkSessionManager(NetworkViewSwitcher nvs, IDARESettingsManager iDAREIDMgr) {
		super();
		this.nvs = nvs;
		IDAREIDMgr = iDAREIDMgr;
	}

	// restore the networkview links from the CyTables
	public void handleSessionLoadedEvent(SessionLoadedEvent e){
		nvs.handleEvent(e);
	}



}
