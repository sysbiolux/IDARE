package idare.imagenode.internal.GUI.NetworkSetup.Tasks;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

public class NetworkSetupGUIHandlerFactory implements GUITunableHandlerFactory<NetworkSetupGUIHandler> {
		
	/**
	 * Necessary fields for this Factory
	 */
	NodeManager nmgr;
	IDARESettingsManager idmgr;
	CyApplicationManager appmgr;

	
	public NetworkSetupGUIHandlerFactory(NodeManager nmgr,
			IDARESettingsManager idmgr, CyApplicationManager appmgr) {
		super();
		this.nmgr = nmgr;
		this.idmgr = idmgr;
		this.appmgr = appmgr;
	}

	@Override
	public NetworkSetupGUIHandler createTunableHandler(Field arg0, Object arg1,
			Tunable arg2) {
		// TODO Auto-generated method stub
		if(!NetworkSetupProperties.class.isAssignableFrom(arg0.getType()))
		{
//			PrintFDebugger.Debugging(this, "Obtained a Request for tunable handling for type " + arg0.getType().getSimpleName() );
			return null;
		}
//		PrintFDebugger.Debugging(this, "Generating new Handler");
		return new NetworkSetupGUIHandler(arg0,arg1,arg2,idmgr,nmgr,appmgr.getCurrentNetwork());
	}

	@Override
	public NetworkSetupGUIHandler createTunableHandler(Method arg0,
			Method arg1, Object arg2, Tunable arg3) {
		if(!NetworkSetupProperties.class.isAssignableFrom(arg0.getReturnType()))
		{
//			PrintFDebugger.Debugging(this, "Obtained a Request for tunable handling for type " + arg0.getReturnType().getSimpleName() );
			return null;
		}
//		PrintFDebugger.Debugging(this, "Generating new Handler");
		return new NetworkSetupGUIHandler(arg0,arg1,arg2,arg3,idmgr,nmgr,appmgr.getCurrentNetwork());
	}


}
