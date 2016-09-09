package idare.subnetwork.internal.Tasks.SubsystemGeneration;

import idare.subnetwork.internal.NetworkViewSwitcher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

public class SubnetworkCreationGUIHandlerFactory implements GUITunableHandlerFactory<SubnetworkCreationGUIHandler> {
		
	/**
	 * Necessary fields for this Factory
	 */
	CyApplicationManager appmgr;
	NetworkViewSwitcher nvs;
	CyLayoutAlgorithmManager cyLayMgr;
	String IDCol;
	
	public SubnetworkCreationGUIHandlerFactory(NetworkViewSwitcher nvs, CyApplicationManager appmgr, CyLayoutAlgorithmManager cyLayMgr) {
		super();
		this.nvs = nvs;
		this.appmgr = appmgr;		
		this.cyLayMgr = cyLayMgr;
	}

	@Override
	public SubnetworkCreationGUIHandler createTunableHandler(Field arg0, Object arg1,
			Tunable arg2) {
		// TODO Auto-generated method stub
		if(!SubNetworkProperties.class.isAssignableFrom(arg0.getType()))
		{
//			PrintFDebugger.Debugging(this, "Obtained a Request for tunable handling for type " + arg0.getType().getSimpleName() );
			return null;
		}
//		PrintFDebugger.Debugging(this, "Generating new Handler");
		return new SubnetworkCreationGUIHandler(arg0,arg1,arg2,nvs,appmgr.getCurrentNetworkView().getModel(), appmgr.getCurrentNetworkView(), getCurrentLayouts(), IDCol);
	}

	@Override
	public SubnetworkCreationGUIHandler createTunableHandler(Method arg0,
			Method arg1, Object arg2, Tunable arg3) {
		if(!SubNetworkProperties.class.isAssignableFrom(arg0.getReturnType()))
		{
//			PrintFDebugger.Debugging(this, "Obtained a Request for tunable handling for type " + arg0.getReturnType().getSimpleName() );
			return null;
		}
//		PrintFDebugger.Debugging(this, "Generating new Handler");
		return new SubnetworkCreationGUIHandler(arg0,arg1,arg2,arg3,nvs,appmgr.getCurrentNetworkView().getModel(), appmgr.getCurrentNetworkView(), getCurrentLayouts(), IDCol);
	}

	private HashMap<String,CyLayoutAlgorithm> getCurrentLayouts()
	{
		HashMap<String,CyLayoutAlgorithm> layoutAlgos = new HashMap<String, CyLayoutAlgorithm>();
		for(CyLayoutAlgorithm algo : cyLayMgr.getAllLayouts())
		{
			layoutAlgos.put(algo.getName(), algo);
		}
		return layoutAlgos;
	}
	
	public void setIDCol(String ColName)
	{
		IDCol = ColName;
	}
}
