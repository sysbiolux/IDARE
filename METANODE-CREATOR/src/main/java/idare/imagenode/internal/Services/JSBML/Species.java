package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Species extends SBase{
	
	private Method notesSet;
	private Method id;
	private Method notesString;
	private Method annotation;
	private Method compartment;
	private Method getName;
	public Species(Object o) 
	{
		super(o);
		try{
			compartment = o.getClass().getMethod("getCompartment");
			getName = o.getClass().getMethod("getName");
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



	@Override
	public String getName()
	{
		{
			try{
				return getName.invoke(SBMLBase).toString();
			}
			catch(InvocationTargetException | IllegalAccessException e)
			{
				return "";
			}
		}
	}
	
}

