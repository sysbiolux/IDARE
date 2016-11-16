package idare.imagenode.internal.Layout.Manual.GUI.Tasks;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.GUI.NetworkSetup.NetworkSetupTunableGUI;
import idare.imagenode.internal.GUI.NetworkSetup.Tasks.NetworkSetupProperties;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;


public class LayoutPropertiesGUIHandler extends AbstractGUITunableHandler {

	
	private LayoutPropertiesGUI mypanel;
	private DataSetManager dsm;
	
	protected LayoutPropertiesGUIHandler(Method getter, Method setter,
			Object instance, Tunable tunable, DataSetManager dsm) {
		super(getter, setter, instance, tunable);
		this.dsm = dsm;
		init();
	}

	protected LayoutPropertiesGUIHandler(Field field, Object instance,
			Tunable tunable, DataSetManager dsm) {
		super(field, instance, tunable);
		this.dsm = dsm;
		init();
	}

	/**
	 * Generate the GUI and replace the panel with the generated GUI.
	 */
	private void init()
	{
		PrintFDebugger.Debugging(this, "Initializing the Panel for the Handler");
		mypanel = new LayoutPropertiesGUI(dsm);
		panel = mypanel;
	}
	@Override
	public void handle() {
		try{
		try{
			DataSetLayoutInfoBundle props = mypanel.getNetworkSetupProperties();
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
