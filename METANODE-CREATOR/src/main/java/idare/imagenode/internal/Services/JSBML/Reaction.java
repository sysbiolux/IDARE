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
			getExtension = o.getClass().getMethod("getExtension", String.class);
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
			Object fbcreac = getFBCExtension();
			if(fbcreac == null)
			{
				return null;
			}
			Method getGPRAssociation = fbcreac.getClass().getMethod("getGeneProductAssociation");
			Object gprassoc = getGPRAssociation.invoke(fbcreac);
			if(gprassoc == null)
			{
				return null;
			}
			Method getAssoc = gprassoc.getClass().getMethod("getAssociation");			
			Object assoc = getAssoc.invoke(gprassoc);
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
			e.printStackTrace(System.out);
		}
		return null;
	}
	
}
