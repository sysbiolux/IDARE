package idare.subnetwork.internal.Tasks.propertySelection;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

public class SubnetworkPropertyColumnGUIHandlerFactory implements GUITunableHandlerFactory<SubnetworkPropertyColumnGUIHandler> {
		
	/**
	 * Necessary fields for this Factory
	 */
	CyApplicationManager appmgr;

	
	public SubnetworkPropertyColumnGUIHandlerFactory(CyApplicationManager appmgr) {
		super();
		this.appmgr = appmgr;
	}

	@Override
	public SubnetworkPropertyColumnGUIHandler createTunableHandler(Field arg0, Object arg1,
			Tunable arg2) {
		// TODO Auto-generated method stub
		if(!SubnetworkColumnProperties.class.isAssignableFrom(arg0.getType()))
		{
			PrintFDebugger.Debugging(this, "Obtained a Request for tunable handling for type " + arg0.getType().getSimpleName() );
			return null;
		}
		if(appmgr.getCurrentNetwork() == null)
		{
			PrintFDebugger.Debugging(this, "Got a request without a selected network. Returning null");
			return null;
		}
		PrintFDebugger.Debugging(this, "Generating new Handler");
		return new SubnetworkPropertyColumnGUIHandler(arg0,arg1,arg2,appmgr.getCurrentNetwork());
	}

	@Override
	public SubnetworkPropertyColumnGUIHandler createTunableHandler(Method arg0,
			Method arg1, Object arg2, Tunable arg3) {
		if(!SubnetworkColumnProperties.class.isAssignableFrom(arg0.getReturnType()))
		{
			PrintFDebugger.Debugging(this, "Obtained a Request for tunable handling for type " + arg0.getReturnType().getSimpleName() );
			return null;
		}
		if(appmgr.getCurrentNetwork() == null)
		{
			PrintFDebugger.Debugging(this, "Got a request without a selected network. Returning null");
			return null;
		}
		PrintFDebugger.Debugging(this, "Generating new Handler");	
		return new SubnetworkPropertyColumnGUIHandler(arg0,arg1,arg2,arg3,appmgr.getCurrentNetwork());
		
	}


}
