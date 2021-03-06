package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GeneProduct extends SBase{

	
	private Method getLabel;


	public GeneProduct(Object o)
	{
		super(o);
		try{
			getLabel = o.getClass().getMethod("getLabel");
		}
		catch(NoSuchMethodException e)
		{

		}
	}

	
	public String getLabel()
	{
		try{
			Object res = getLabel.invoke(SBMLBase);
			return res == null ? null : (String) res;							
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	
	}
	
	@Override
	public String getName()
	{
		return getLabel();
	}
}
