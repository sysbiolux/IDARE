package idare.imagenode;

import idare.imagenode.Interfaces.Plugin.IDAREPlugin;
import idare.imagenode.internal.IDAREImageNodeApp;
/**
 * The {@link IDAREImageNodeAppService} is the interaction point for plugins that want to provide additional features for the IDARE
 * image generation tool. It is registered with the cytoscape framework as an {@link IDAREImageNodeAppService}.class service. 
 * The two functionalities are to register and deregister apps. The apps will then be requested to provide their respective 
 * information by the {@link IDAREImageNodeApp}.  
 * @author Thomas Pfau
 *
 */
public class IDAREImageNodeAppService {

	IDAREImageNodeApp app;
	/**
	 * Default constructor obtaining an IDAREImageNodeApp. this should never be called by an external program.
	 * Instead the registered service should be requested from the Cytoscape framework.
	 * @param app
	 */
	public IDAREImageNodeAppService(IDAREImageNodeApp app) {
		// TODO Auto-generated constructor stub
		this.app = app;
	}
	
	/**
	 * Register a plugin to the IDARE App;
	 * @param plugin The plugin to register
	 */
	public void registerPlugin(IDAREPlugin plugin)
	{
		app.registerPlugin(plugin);
	}
	
	/**
	 * Deregister a plugin from the IDARE App;
	 * @param plugin The plugin to deregister
	 */
	public void deRegisterPlugin(IDAREPlugin plugin)
	{
		app.deRegisterPlugin(plugin);
	}
	
}
