package idare.subnetwork.internal.Tasks.propertySelection;

import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.subnetwork.internal.SubNetworkUtils;
import idare.subnetwork.internal.GUI.SubnetworkPropertyColumnChooser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


public class SubnetworkPropertyColumnGUIHandler extends AbstractGUITunableHandler {

	
	private SubnetworkPropertyColumnChooser mypanel;
	private CyNetwork network;
	
	protected SubnetworkPropertyColumnGUIHandler(Method getter, Method setter,
			Object instance, Tunable tunable, CyNetwork network) {
		super(getter, setter, instance, tunable);		
		this.network = network;
		init();
	}

	protected SubnetworkPropertyColumnGUIHandler(Field field, Object instance,
			Tunable tunable, CyNetwork network) {
		super(field, instance, tunable);
		this.network = network;
		init();
	}

	/**
	 * Generate the GUI and replace the panel with the generated GUI.
	 */
	private void init()
	{
//		PrintFDebugger.Debugging(this, "Initializing the Panel for the Handler");
		
		mypanel = new SubnetworkPropertyColumnChooser(SubNetworkUtils.getNodeTableColumnNames(network),network);
		panel = mypanel;
	}
	

	@Override
	public void handle() {
		try{
		try{
			SubnetworkColumnProperties props = mypanel.getColumnProperties();
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
