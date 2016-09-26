package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reaction extends SBase{


	private Method getExtension;	
	private Method reversible;
	private Method getName;
	
	public Reaction(Object o) 
	{
		super(o);
		try{
			reversible = o.getClass().getMethod("isReversible");
			Class[] cArg = new Class[1];
		    cArg[0] = String.class;
			getExtension = o.getClass().getMethod("getExtension", cArg);
			getName = o.getClass().getMethod("getName");
		}
		catch(NoSuchMethodException e)
		{
			
		}
	}
	



	public boolean isReversible()
	{
		try{
			return (Boolean) reversible.invoke(SBMLBase);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return false;
		}
	}
	
	public Object getFBCExtension()
	{
		try{
			return getExtension.invoke(SBMLBase, "fbc");
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
	public Association getAssociation()
	{
		try{
			Object r = getFBCExtension();
			if(r == null)
			{
				return null;
			}
			Method getGPRAssociation = r.getClass().getMethod("getGeneProductAssociation");
			Object o = getGPRAssociation.invoke(r);
			if(o == null)
			{
				return null;
			}
			Method getAssoc = o.getClass().getMethod("getAssociation");			
			Object assoc = getAssoc.invoke(o);
			if(assoc != null)
			{
				if(assoc.getClass().getSimpleName().equals("And"))
				{
					return new And(assoc);
				}
				if(assoc.getClass().getSimpleName().equals("Or"))
				{
					return new Or(assoc);
				}
				if(assoc.getClass().getSimpleName().equals("GeneProductRef"))
				{
					return new GeneProductRef(assoc);
				}
				else
				{
					//We do not parse "Not" associations.
					return null;
				}
			}
		}
		catch(Exception e)
		{
			
		}
		return null;
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
