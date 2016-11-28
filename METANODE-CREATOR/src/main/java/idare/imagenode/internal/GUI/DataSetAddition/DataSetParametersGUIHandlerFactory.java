package idare.imagenode.internal.GUI.DataSetAddition;

import idare.imagenode.internal.DataManagement.DataSetManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

public class DataSetParametersGUIHandlerFactory implements GUITunableHandlerFactory<DataSetParametersGUIHandler> {
	FileUtil util;
	DataSetManager dsm;
	CySwingApplication cySwingApp;
	
	public DataSetParametersGUIHandlerFactory(FileUtil util, DataSetManager dsm, CySwingApplication cySwingApp) {
		// TODO Auto-generated constructor stub
		this.dsm = dsm;
		this.util = util;
		this.cySwingApp = cySwingApp;
	}
	@Override
	public DataSetParametersGUIHandler createTunableHandler(Field arg0, Object arg1,
			Tunable arg2) {
		if(!DataSetGenerationParameters.class.isAssignableFrom(arg0.getType()))
		{
//			PrintFDebugger.Debugging(this, "Received a request for handling");
			return null;
		}
		return new DataSetParametersGUIHandler(arg0,arg1,arg2,util,dsm, cySwingApp);
	}

	@Override
	public DataSetParametersGUIHandler createTunableHandler(Method arg0,
			Method arg1, Object arg2, Tunable arg3) {
		if(!DataSetGenerationParameters.class.isAssignableFrom(arg0.getReturnType()))
		{
//			PrintFDebugger.Debugging(this, "Received a request for handling");
			return null;
		}
		return new DataSetParametersGUIHandler(arg0,arg1,arg2,arg3,util,dsm, cySwingApp);
	}


}
