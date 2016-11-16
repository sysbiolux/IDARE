package idare.imagenode.internal.Layout.Manual.GUI.Tasks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;

public class NetworkSetupGUIHandlerFactory implements GUITunableHandlerFactory<LayoutPropertiesGUIHandler> {
		
	/**
	 * Necessary fields for this Factory
	 */
	DataSetManager dsm;

	
	public NetworkSetupGUIHandlerFactory(DataSetManager dsm,
			IDARESettingsManager idmgr, CyApplicationManager appmgr) {
		super();
		this.dsm = dsm;
	}

	@Override
	public LayoutPropertiesGUIHandler createTunableHandler(Field arg0, Object arg1,
			Tunable arg2) {
		// TODO Auto-generated method stub
		if(!DataSetLayoutInfoBundle.class.isAssignableFrom(arg0.getType()))
		{
//			PrintFDebugger.Debugging(this, "Obtained a Request for tunable handling for type " + arg0.getType().getSimpleName() );
			return null;
		}
//		PrintFDebugger.Debugging(this, "Generating new Handler");
		return new LayoutPropertiesGUIHandler(arg0,arg1,arg2,dsm);
	}

	@Override
	public LayoutPropertiesGUIHandler createTunableHandler(Method arg0,
			Method arg1, Object arg2, Tunable arg3) {
		if(!DataSetLayoutInfoBundle.class.isAssignableFrom(arg0.getReturnType()))
		{
//			PrintFDebugger.Debugging(this, "Obtained a Request for tunable handling for type " + arg0.getReturnType().getSimpleName() );
			return null;
		}
//		PrintFDebugger.Debugging(this, "Generating new Handler");
		return new LayoutPropertiesGUIHandler(arg0,arg1,arg2,arg3,dsm);
	}


}
