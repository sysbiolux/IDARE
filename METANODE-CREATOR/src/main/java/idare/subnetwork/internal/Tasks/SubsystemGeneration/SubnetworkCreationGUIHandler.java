package idare.subnetwork.internal.Tasks.SubsystemGeneration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;

import idare.subnetwork.internal.NetworkViewSwitcher;
import idare.subnetwork.internal.SubNetworkUtils;
import idare.subnetwork.internal.GUI.SubnetworkPropertiesSelectionGUI;


public class SubnetworkCreationGUIHandler extends AbstractGUITunableHandler {

	
	private SubnetworkPropertiesSelectionGUI mypanel;
	private NetworkViewSwitcher nvs;
	private CyNetwork network;
	private CyNetworkView currentNetworkView;
	private HashMap<String,CyLayoutAlgorithm> layoutMap;
	private String IDCol;
	
	protected SubnetworkCreationGUIHandler(Method getter, Method setter,
			Object instance, Tunable tunable, NetworkViewSwitcher nvs, CyNetwork network, CyNetworkView view,  HashMap<String,CyLayoutAlgorithm> layoutMap, String IDCol) {
		super(getter, setter, instance, tunable);
		this.IDCol = IDCol;
		this.nvs = nvs;
		this.currentNetworkView = view;
		this.network = network;
		this.layoutMap = layoutMap;
		init();
	}

	protected SubnetworkCreationGUIHandler(Field field, Object instance,
			Tunable tunable, NetworkViewSwitcher nvs, CyNetwork network, CyNetworkView view,  HashMap<String,CyLayoutAlgorithm> layoutMap, String IDCol) {
		super(field, instance, tunable);
		this.IDCol = IDCol;
		this.nvs = nvs;
		this.currentNetworkView = view;
		this.network = network;
		this.layoutMap = layoutMap;
		init();
	}

	/**
	 * Generate the GUI and replace the panel with the generated GUI.
	 */
	private void init()
	{
//		PrintFDebugger.Debugging(this, "Initializing the Panel for the Handler");
		try{
			mypanel = new SubnetworkPropertiesSelectionGUI(layoutMap.keySet(), SubNetworkUtils.getNodeTableColumnNames(network), network, nvs,IDCol);
			panel = mypanel;
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
	@Override
	public void handle() {
//		PrintFDebugger.Debugging(this, "Trying to handle the request");
		try{
		try{
//			PrintFDebugger.Debugging(this, "Getting the selected properties");
			SubNetworkProperties props = mypanel.getProperties();
			if(props != null)
			{
//				PrintFDebugger.Debugging(this, "Properties are non null");					
				props.layoutAlgorithm = layoutMap.get(props.selectedLayoutAlgorithmName);				
				props.currentNetworkView = currentNetworkView;
			}
			setValue(props);
		}
		catch(IllegalAccessException| InvocationTargetException e)
		{
			e.printStackTrace(System.out);
		}
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			throw e;
		}

	}
}
