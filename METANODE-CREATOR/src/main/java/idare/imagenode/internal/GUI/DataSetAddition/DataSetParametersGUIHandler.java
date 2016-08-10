package idare.imagenode.internal.GUI.DataSetAddition;


import idare.imagenode.internal.DataManagement.DataSetManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;

public class DataSetParametersGUIHandler extends AbstractGUITunableHandler {

	private TunableDataSetAdderGUI mypanel;
	private FileUtil util;
	private DataSetManager dsm;
	
	protected DataSetParametersGUIHandler(Method getter, Method setter,
			Object instance, Tunable tunable, FileUtil util, DataSetManager dsm) {
		super(getter, setter, instance, tunable);
		this.util = util;
		this.dsm = dsm;
		init();
	}

	protected DataSetParametersGUIHandler(Field field, Object instance,
			Tunable tunable, FileUtil util, DataSetManager dsm) {
		super(field, instance, tunable);
		this.util = util;
		this.dsm = dsm;
		init();
	}

	private void init()
	{
		mypanel = new TunableDataSetAdderGUI(dsm,util);
		panel = mypanel;
	}
	@Override
	public void handle() {
		try{
			setValue(mypanel.getDataSetParameters());
		}
		catch(IllegalAccessException| InvocationTargetException e)
		{
			e.printStackTrace(System.out);
		}

	}

}
