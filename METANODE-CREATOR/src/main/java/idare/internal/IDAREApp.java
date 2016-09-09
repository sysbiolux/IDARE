package idare.internal;

import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.IDAREImageNodeApp;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.subnetwork.internal.SubnetworkSessionManager;
/**
 * This is a class that manages the behaviour of the IDAREapp upon loading a session. 
 * Since the order in which the different parts of the app get restored is relevant this class handles this issue.
 *    
 * @author Thomas Pfau
 *
 */
public class IDAREApp implements SessionLoadedListener{

	private final IDARESettingsManager mgr;
	private SubnetworkSessionManager snsm;
	private IDAREImageNodeApp imageapp;
	
	/**
	 * A Default constructor that initializes the {@link IDARESettingsManager} used in this app.
	 * The {@link IDAREImageNodeApp} and the {@link SubnetworkSessionManager} have to be provided using the corresponding setter methods. 
	 */
	public IDAREApp()
	{
		mgr = new IDARESettingsManager();		
	}
	
	
	
	@Override
	public void handleEvent(SessionLoadedEvent arg0) {
		PrintFDebugger.Debugging(this, "Restoring the ID Manager");
		mgr.handleSessionLoadedEvent(arg0);
		PrintFDebugger.Debugging(this, "Restoring the SubNetworks");
		snsm.handleSessionLoadedEvent(arg0);
		PrintFDebugger.Debugging(this, "Restoring the ImageNode App");
		imageapp.handleSessionLoadedEvent(arg0);
	}
	/**
	 * Get the {@link IDARESettingsManager} used in this app
	 * @return The {@link IDARESettingsManager} used by the bundle.
	 */
	public IDARESettingsManager getSettingsManager()
	{
		return mgr;
	}
	
	/**
	 * Set the {@link SubnetworkSessionManager} used in this app.
	 * @param sssm The {@link SubnetworkSessionManager} to be used
	 */
	public void setSubsysManager(SubnetworkSessionManager sssm)	
	{
		this.snsm = sssm;
	}
	
	/**
	 * Set the {@link IDAREImageNodeApp} object used in this app.
	 * @param app The used {@link IDAREImageNodeApp}
	 */
	public void setImageApp(IDAREImageNodeApp app)
	{
		this.imageapp = app;
	}
	
	/**
	 *	Since the {@link IDAREImageNodeApp} provides a couple of services itself, this function allows to obtain the used copy 
	 * @return The {@link IDAREImageNodeApp} object used in this app.
	 */
	public IDAREImageNodeApp getImageNodeApp()
	{
		return imageapp;
	}
}
