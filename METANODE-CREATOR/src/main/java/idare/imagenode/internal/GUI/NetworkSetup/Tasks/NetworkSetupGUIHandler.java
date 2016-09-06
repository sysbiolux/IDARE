package idare.imagenode.internal.GUI.NetworkSetup.Tasks;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.GUI.NetworkSetup.NetworkSetupTunableGUI;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


public class NetworkSetupGUIHandler extends AbstractGUITunableHandler {

	
	private NetworkSetupTunableGUI mypanel;
	private IDARESettingsManager mgr;
	private NodeManager nm;
	private CyNetwork network;
	
	protected NetworkSetupGUIHandler(Method getter, Method setter,
			Object instance, Tunable tunable, IDARESettingsManager mgr, NodeManager nm, CyNetwork network) {
		super(getter, setter, instance, tunable);
		this.nm = nm;
		this.mgr = mgr;
		this.network = network;
		init();
	}

	protected NetworkSetupGUIHandler(Field field, Object instance,
			Tunable tunable, IDARESettingsManager mgr, NodeManager nm, CyNetwork network) {
		super(field, instance, tunable);
		this.nm = nm;
		this.mgr = mgr;
		this.network = network;
		init();
	}

	/**
	 * Generate the GUI and replace the panel with the generated GUI.
	 */
	private void init()
	{
		PrintFDebugger.Debugging(this, "Initializing the Panel for the Handler");
		mypanel = new NetworkSetupTunableGUI(network);
		panel = mypanel;
	}
	@Override
	public void handle() {
		try{
		try{
			NetworkSetupProperties props = mypanel.getNetworkSetupProperties();
			if(props != null)
			{
				props.nm = nm;
				props.mgr = mgr;
				props.network = network;
				
			}
//			System.out.println(props.toString());
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
