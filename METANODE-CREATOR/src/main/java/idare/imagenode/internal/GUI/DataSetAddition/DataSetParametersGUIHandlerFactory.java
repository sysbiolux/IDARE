package idare.imagenode.internal.GUI.DataSetAddition;

import idare.imagenode.internal.DataManagement.DataSetManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

public class DataSetParametersGUIHandlerFactory implements GUITunableHandlerFactory<DataSetParametersGUIHandler> {
	FileUtil util;
	DataSetManager dsm;
	public DataSetParametersGUIHandlerFactory(FileUtil util, DataSetManager dsm) {
		// TODO Auto-generated constructor stub
		this.dsm = dsm;
		this.util = util;
	}
	@Override
	public DataSetParametersGUIHandler createTunableHandler(Field arg0, Object arg1,
			Tunable arg2) {
		// TODO Auto-generated method stub
		if(!DataSetGenerationParameters.class.isAssignableFrom(arg0.getType()))
		{
			return null;
		}
		return new DataSetParametersGUIHandler(arg0,arg1,arg2,util,dsm);
	}

	@Override
	public DataSetParametersGUIHandler createTunableHandler(Method arg0,
			Method arg1, Object arg2, Tunable arg3) {
		if(!DataSetGenerationParameters.class.isAssignableFrom(arg0.getReturnType()))
		{
			return null;
		}
		return new DataSetParametersGUIHandler(arg0,arg1,arg2,arg3,util,dsm);
	}


}