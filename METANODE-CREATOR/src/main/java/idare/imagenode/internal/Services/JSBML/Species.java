package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Species extends SBase{
	

	private Method compartment;
	public Species(Object o) 
	{
		super(o);
		try{
			compartment = o.getClass().getMethod("getCompartment");
		}
		catch(NoSuchMethodException e)
		{
			
		}
	}
	

	
	public String getCompartment()
	{
		{
			try{
				return compartment.invoke(SBMLBase).toString();
			}
			catch(InvocationTargetException | IllegalAccessException e)
			{
				return "";
			}
		}
	}
	
}

