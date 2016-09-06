package idare.internal;

import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.IDAREImageNodeApp;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.subsystems.internal.SubSystemSessionManager;

public class IDAREApp implements SessionLoadedListener{

	private IDARESettingsManager mgr;
	private SubSystemSessionManager sssm;
	private IDAREImageNodeApp imageapp;
	
	public IDAREApp()
	{
		mgr = new IDARESettingsManager();		
	}
	
	
	
	@Override
	public void handleEvent(SessionLoadedEvent arg0) {
		PrintFDebugger.Debugging(this, "Restoring the ID Manager");
		mgr.handleSessionLoadedEvent(arg0);
		PrintFDebugger.Debugging(this, "Restoring the SubNetworks");
		sssm.handleSessionLoadedEvent(arg0);
		PrintFDebugger.Debugging(this, "Restoring the ImageNode App");
		imageapp.handleSessionLoadedEvent(arg0);
	}
	
	public IDARESettingsManager getSettingsManager()
	{
		return mgr;
	}
	
	public void setSubsysManager(SubSystemSessionManager sssm)	
	{
		this.sssm = sssm;
	}
	
	public void setImageApp(IDAREImageNodeApp app)
	{
		this.imageapp = app;
	}
	
	public IDAREImageNodeApp getImageNodeApp()
	{
		return imageapp;
	}
}
