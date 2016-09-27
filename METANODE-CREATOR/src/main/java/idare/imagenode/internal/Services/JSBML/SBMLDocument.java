package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SBMLDocument {

	private Object SBMLDocument;
	private Method model;
	public SBMLDocument(Object o) 
	{
		SBMLDocument= o;
		try{
			model = o.getClass().getMethod("getModel");
		}
		catch(NoSuchMethodException e)
		{
			
		}
	}
	public Model getModel()
	{
		try{
			return new Model(model.invoke(SBMLDocument));
			
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	
	}
}
